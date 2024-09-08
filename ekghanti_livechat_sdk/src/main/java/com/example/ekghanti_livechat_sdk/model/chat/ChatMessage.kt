package com.example.ekchanti_chat_sdk.model.chat

data class ChatMessage(
    val chatSide: String,
    val displayType: String,
    val message: String
)