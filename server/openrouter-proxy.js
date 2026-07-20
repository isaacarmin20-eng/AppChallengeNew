const http = require('node:http');

const PORT = Number(process.env.PORT || 8080);
const OPENROUTER_API_KEY = process.env.OPENROUTER_API_KEY;
const MODEL = process.env.OPENROUTER_MODEL || 'google/gemma-4-26b-a4b-it:free';
const MAX_BODY_BYTES = 3 * 1024 * 1024;
const REQUEST_TIMEOUT_MS = 45_000;

if (!OPENROUTER_API_KEY) {
  throw new Error('OPENROUTER_API_KEY must be set before starting the proxy.');
}

function sendJson(response, status, body) {
  response.writeHead(status, { 'Content-Type': 'application/json; charset=utf-8' });
  response.end(JSON.stringify(body));
}

function readBody(request) {
  return new Promise((resolve, reject) => {
    let size = 0;
    const chunks = [];
    request.on('data', (chunk) => {
      size += chunk.length;
      if (size > MAX_BODY_BYTES) {
        reject(new Error('Request body is too large.'));
        request.destroy();
        return;
      }
      chunks.push(chunk);
    });
    request.on('end', () => resolve(Buffer.concat(chunks).toString('utf8')));
    request.on('error', reject);
  });
}

function parseAnalysis(content) {
  const normalized = content.replace(/^```json\s*|\s*```$/g, '').trim();
  if (!normalized) throw new Error('Cloud model returned an empty response.');
  const jsonText = normalized.match(/\{[\s\S]*\}/)?.[0] || normalized;
  const json = JSON.parse(jsonText);
  const face = ['normal', 'possible_droop', 'not_visible'].includes(json.faceAssessment)
    ? json.faceAssessment
    : 'not_visible';
  const arms = ['both_raised_evenly', 'possible_drift', 'not_visible'].includes(json.armsAssessment)
    ? json.armsAssessment
    : 'not_visible';
  const speech = ['clear', 'possible_impairment', 'not_assessable'].includes(json.speechAssessment)
    ? json.speechAssessment
    : 'not_assessable';
  const assessments = [
    `Face: ${face.replaceAll('_', ' ')}`,
    `Arms: ${arms.replaceAll('_', ' ')}`,
    `Speech: ${speech.replaceAll('_', ' ')}`,
  ];
  return {
    // The app uses this deterministic rule rather than trusting a free model's
    // top-level conclusion: only a specific structured FAST finding is urgent.
    concerning: face === 'possible_droop' || arms === 'possible_drift' || speech === 'possible_impairment',
    uncertain: json.uncertain === true || face === 'not_visible' || arms === 'not_visible',
    observations: [...assessments, ...(Array.isArray(json.observations)
      ? json.observations.filter((item) => typeof item === 'string' && item.trim()).slice(0, 2)
      : [])],
  };
}

async function analyze(payload) {
  const { faceImageBase64, armsImageBase64, speechObservation } = payload;
  if (typeof faceImageBase64 !== 'string' || typeof armsImageBase64 !== 'string') {
    throw new Error('Both captured images are required.');
  }

  const controller = new AbortController();
  const timeout = setTimeout(() => controller.abort(), REQUEST_TIMEOUT_MS);
  try {
    const upstream = await fetch('https://openrouter.ai/api/v1/chat/completions', {
      method: 'POST',
      signal: controller.signal,
      headers: {
        Authorization: `Bearer ${OPENROUTER_API_KEY}`,
        'Content-Type': 'application/json',
      },
      body: JSON.stringify({
        model: MODEL,
        temperature: 0,
        max_tokens: 80,
        response_format: { type: 'json_object' },
        messages: [
          {
            role: 'system',
            content: 'You are a conservative visual screening assistant for a stroke emergency demo, not a medical diagnostician. Analyze the labeled face and arms images separately. Do not treat raised arms as a warning sign: assess whether both arms appear lifted evenly or whether one arm visibly drifts or is weak. Set concerning=true only for a visible face droop, visible unilateral arm drift/weakness, or a speech observation that describes slurred, confused, or impaired speech. Set uncertain=true only when the face or arms cannot be visually assessed because they are absent, obscured, or too poor quality; do not set it merely because the photo is ordinary or because a person is raising both arms. Return only JSON: {"concerning":boolean,"uncertain":boolean,"faceAssessment":"normal|possible_droop|not_visible","armsAssessment":"both_raised_evenly|possible_drift|not_visible","speechAssessment":"clear|possible_impairment|not_assessable","observations":string[]}. Use at most two brief evidence observations.',
          },
          {
            role: 'user',
            content: [
              { type: 'text', text: `Image 1 is the FACE check. Determine whether the face appears symmetric or has a visible droop. Image 2 is the ARMS check. Determine whether both arms are raised evenly or one visibly drifts. User speech observation: ${String(speechObservation || '')}` },
              { type: 'image_url', image_url: { url: `data:image/jpeg;base64,${faceImageBase64}` } },
              { type: 'image_url', image_url: { url: `data:image/jpeg;base64,${armsImageBase64}` } },
            ],
          },
        ],
      }),
    });
    const responseBody = await upstream.json();
    if (!upstream.ok) {
      throw new Error(responseBody.error?.message || `OpenRouter returned ${upstream.status}.`);
    }
    return parseAnalysis(responseBody.choices?.[0]?.message?.content || '');
  } finally {
    clearTimeout(timeout);
  }
}

http.createServer(async (request, response) => {
  if (request.method === 'GET' && request.url === '/health') {
    return sendJson(response, 200, { ok: true, model: MODEL });
  }
  if (request.method !== 'POST' || request.url !== '/stroke-screen') {
    return sendJson(response, 404, { error: 'Not found.' });
  }
  try {
    const payload = JSON.parse(await readBody(request));
    return sendJson(response, 200, await analyze(payload));
  } catch (error) {
    console.error('Stroke-screen request failed:', error.message);
    return sendJson(response, 502, { error: error.name === 'AbortError' ? 'Cloud analysis timed out.' : error.message });
  }
}).listen(PORT, '127.0.0.1', () => {
  console.log(`OpenRouter proxy listening at http://127.0.0.1:${PORT}`);
});
