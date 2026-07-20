package com.example.mediscreen.data.model

import java.io.Serializable

data class ResultPayload(
    val urgent: Boolean,
    val conditionId: String,
    val displayName: String,
    val instructions: List<String>,
    val seekCareMessage: String,
    val resultHeadline: String? = null
) : Serializable
