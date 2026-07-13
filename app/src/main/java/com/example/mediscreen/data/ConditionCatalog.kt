package com.example.mediscreen.data

import androidx.compose.ui.graphics.Color
import com.example.mediscreen.data.model.ConditionQuestionnaire
import com.example.mediscreen.data.model.SymptomQuestion

object ConditionCatalog {

    private val questionnaires by lazy {
        listOf(
            heartAttack,
            choking,
            diabeticEmergency,
            asthmaAttack,
            poisoning
        ).associateBy { it.conditionId }
    }

    fun findById(conditionId: String): ConditionQuestionnaire? = questionnaires[conditionId]

    fun all(): List<ConditionQuestionnaire> = questionnaires.values.toList()

    val heartAttack = ConditionQuestionnaire(
        conditionId = "heart_attack",
        displayName = "Chest Pain / Heart Attack",
        protocolBasis = "Standard cardiac emergency signs",
        footnote = "Women often experience atypical signs such as nausea, fatigue, or jaw pain without classic chest pressure.",
        themeColor = Color(0xFFD64550),
        waitingTip = "Keep the person sitting or lying down and calm while you finish this screening.",
        questions = listOf(
            SymptomQuestion(
                id = "chest_pain",
                prompt = "Is there chest pain, pressure, squeezing, or fullness?"
            ),
            SymptomQuestion(
                id = "radiating_pain",
                prompt = "Is the pain spreading to an arm, shoulder, neck, or jaw?",
                rationale = "Pain that spreads to the arm, neck, or jaw is a classic warning sign of a heart attack."
            ),
            SymptomQuestion(
                id = "shortness_of_breath",
                prompt = "Is there shortness of breath?"
            ),
            SymptomQuestion(
                id = "nausea",
                prompt = "Is there nausea, unexplained fatigue, or feeling faint?",
                rationale = "These signs are often overlooked, especially in women, but can indicate a heart attack."
            )
        ),
        urgentThreshold = 2,
        urgentInstructions = listOf(
            "Call 911 immediately. Do not drive yourself to the hospital.",
            "If available and not allergic, chew one adult aspirin (325 mg) or four baby aspirin (81 mg each) unless told otherwise by a doctor.",
            "Sit or lie down and stay calm while waiting for help.",
            "If the person becomes unresponsive and is not breathing normally, begin CPR if trained."
        ),
        nonUrgentInstructions = listOf(
            "Some concerning signs were not reported, but cardiac symptoms can be subtle or atypical.",
            "If chest discomfort, pressure, or unusual fatigue continues or returns, seek emergency care immediately.",
            "When in doubt, call 911 — it is better to be evaluated and find nothing serious than to wait."
        )
    )

    val choking = ConditionQuestionnaire(
        conditionId = "choking",
        displayName = "Choking",
        protocolBasis = "Heimlich/choking response",
        themeColor = Color(0xFF0288D1),
        waitingTip = "If the person cannot breathe at all, don't wait for these questions — act immediately.",
        questions = listOf(
            SymptomQuestion(
                id = "cant_speak",
                prompt = "Can the person NOT speak, cough, or breathe effectively?"
            ),
            SymptomQuestion(
                id = "clutching_throat",
                prompt = "Is the person clutching their throat (universal choking sign)?",
                rationale = "Clutching the throat is the universally recognized sign of choking."
            )
        ),
        urgentThreshold = 1,
        urgentInstructions = listOf(
            "Call 911 immediately or have someone else call while you act.",
            "If the person cannot cough, speak, or breathe: give 5 firm back blows between the shoulder blades, then 5 abdominal thrusts (Heimlich maneuver).",
            "Alternate back blows and abdominal thrusts until the object is expelled or the person becomes unresponsive.",
            "If the person becomes unresponsive, begin CPR and continue until help arrives."
        ),
        nonUrgentInstructions = listOf(
            "Some severe choking signs were not reported, but choking can worsen quickly.",
            "If coughing is weak, breathing becomes difficult, or the person cannot speak clearly, call 911 immediately.",
            "Do not leave the person alone while symptoms are present."
        )
    )

    val diabeticEmergency = ConditionQuestionnaire(
        conditionId = "diabetic_emergency",
        displayName = "Diabetic Emergency",
        protocolBasis = "Hypoglycemia response",
        themeColor = Color(0xFF7B61FF),
        waitingTip = "Stay nearby and keep the person from driving or operating machinery while you finish this screening.",
        questions = listOf(
            SymptomQuestion(
                id = "known_diabetic",
                prompt = "Is the person known to have diabetes or take insulin/diabetes medication?"
            ),
            SymptomQuestion(
                id = "confusion",
                prompt = "Is the person confused, unusually drowsy, or difficult to wake?",
                rationale = "Confusion or drowsiness can signal dangerously low blood sugar that needs urgent care."
            ),
            SymptomQuestion(
                id = "shakiness",
                prompt = "Is the person shaky, jittery, or unsteady?"
            ),
            SymptomQuestion(
                id = "sweating",
                prompt = "Is the person sweating, pale, or clammy?"
            )
        ),
        urgentThreshold = 2,
        urgentInstructions = listOf(
            "Call 911 if the person is confused, unresponsive, or not improving.",
            "If awake and able to swallow safely, give a fast-acting sugar source (juice, regular soda, glucose gel/tablets).",
            "Recheck in 15 minutes. If symptoms persist, give more sugar and seek emergency care.",
            "Do not give food or drink to someone who is unconscious or cannot swallow safely."
        ),
        nonUrgentInstructions = listOf(
            "Some warning signs were not reported, but blood sugar problems can change quickly.",
            "If shakiness, confusion, sweating, or unusual behavior continues or worsens, call 911.",
            "People with diabetes should follow their care plan and seek help if symptoms do not improve with sugar."
        )
    )

    val asthmaAttack = ConditionQuestionnaire(
        conditionId = "asthma_attack",
        displayName = "Severe Asthma Attack",
        protocolBasis = "Asthma emergency protocol",
        themeColor = Color(0xFF00897B),
        waitingTip = "Help the person stay upright and keep their rescue inhaler within reach while you finish this screening.",
        questions = listOf(
            SymptomQuestion(
                id = "known_asthma",
                prompt = "Does the person have asthma or a history of breathing problems?"
            ),
            SymptomQuestion(
                id = "cant_catch_breath",
                prompt = "Is the person unable to catch their breath or speak in full sentences?"
            ),
            SymptomQuestion(
                id = "inhaler_not_helping",
                prompt = "Is a rescue inhaler not helping or unavailable?",
                rationale = "If a rescue inhaler isn't helping, the airway may be closing further and needs emergency treatment."
            )
        ),
        urgentThreshold = 2,
        urgentInstructions = listOf(
            "Call 911 immediately.",
            "Help the person sit upright and stay calm.",
            "Use a rescue inhaler as directed (often 1 puff every 30–60 seconds up to 10 puffs while waiting for help, if their action plan allows).",
            "If breathing stops or the person becomes unresponsive, begin CPR if trained."
        ),
        nonUrgentInstructions = listOf(
            "Some severe asthma signs were not reported, but breathing can deteriorate rapidly.",
            "If wheezing, chest tightness, or shortness of breath worsens or the inhaler is not working, call 911.",
            "Follow the person's asthma action plan and do not delay emergency care if symptoms persist."
        )
    )

    val poisoning = ConditionQuestionnaire(
        conditionId = "poisoning",
        displayName = "Poisoning / Overdose",
        protocolBasis = "Poison Control/911 guidance — recognition only, no substance-specific advice",
        themeColor = Color(0xFFEF6C00),
        waitingTip = "Stay with the person and keep them calm while you finish this screening.",
        questions = listOf(
            SymptomQuestion(
                id = "unresponsive",
                prompt = "Is the person unresponsive or not waking up?",
                rationale = "Unresponsiveness is one of the strongest signs of a life-threatening emergency."
            ),
            SymptomQuestion(
                id = "vomiting",
                prompt = "Is the person vomiting repeatedly or unable to keep fluids down?"
            ),
            SymptomQuestion(
                id = "confusion",
                prompt = "Is the person confused, agitated, or behaving unusually?"
            ),
            SymptomQuestion(
                id = "known_exposure",
                prompt = "Is there a known or suspected exposure to a medication, drug, chemical, or toxic substance?"
            )
        ),
        urgentIfAnyOf = listOf("unresponsive"),
        urgentThreshold = 2,
        urgentInstructions = listOf(
            "Call 911 immediately.",
            "If the person is not breathing or has no pulse, begin CPR if trained.",
            "In the U.S., you can also contact Poison Control at 1-800-222-1222 for guidance while waiting for help.",
            "Do not give food, drink, or home remedies unless instructed by emergency personnel or Poison Control."
        ),
        nonUrgentInstructions = listOf(
            "Some overdose or poisoning signs were not reported, but symptoms can escalate without warning.",
            "If vomiting, confusion, drowsiness, or unusual behavior continues or worsens, call 911.",
            "When exposure to a substance is possible, seek professional guidance immediately — do not wait for symptoms to peak."
        )
    )
}