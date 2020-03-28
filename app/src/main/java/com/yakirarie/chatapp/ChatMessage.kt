package com.yakirarie.chatapp

import java.util.*


class ChatMessage(
    val id: String,
    val text: String,
    val fromId: String,
    val toId: String,
    val timestamp: String,
    val image: Boolean
) {
    constructor() : this("", "", "", "", "", false)
}