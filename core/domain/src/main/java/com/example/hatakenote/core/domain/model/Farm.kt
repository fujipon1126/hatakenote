package com.example.hatakenote.core.domain.model

import kotlinx.datetime.Instant

data class Farm(
    val id: String,
    val name: String,
    val ownerId: String,
    val memberIds: List<String>,
    val inviteCode: String?,
    val createdAt: Instant,
)
