package com.yakirarie.chatapp


class ChatMessage(
    val id: String,
    val text: String,
    val fromId: String,
    val toId: MutableList<String>,
    val dateAndTime: String,
    val messageType: String,
    var seen: Boolean
) {
    constructor() : this("", "", "", mutableListOf<String>(""), "", "", false)
}