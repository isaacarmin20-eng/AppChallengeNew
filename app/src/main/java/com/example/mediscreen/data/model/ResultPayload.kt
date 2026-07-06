package com.example.mediscreen.data.model

data class ResultPayload(
    val urgent: Boolean,
    val conditionId: String,
    val displayName: String,
    val instructions: List<String>,
    val seekCareMessage: String
)
