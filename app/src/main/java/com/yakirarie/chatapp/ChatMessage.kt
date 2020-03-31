package com.yakirarie.chatapp


class ChatMessage(
    val id: String,
    val text: String,
    val fromId: String,
    val toId: String,
    val timestamp: String,
    val messageType: String
) {
    constructor() : this("", "", "", "", "", "")
}