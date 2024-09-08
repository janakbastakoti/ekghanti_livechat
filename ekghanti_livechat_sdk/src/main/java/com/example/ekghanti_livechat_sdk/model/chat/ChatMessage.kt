package com.example.ekghanti_livechat_sdk.model.chat

data class ChatMessage(
    val chatSide: String,
    val displayType: String,
    val message: String
)