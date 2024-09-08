package com.example.ekchanti_chat_sdk.model.chat

data class ChatData(
    val __v: Int,
    val _id: String,
    val channelID: String,
    val createdAt: String,
    val instanceId: String,
    val messages: List<Message>,
    val updatedAt: String
)