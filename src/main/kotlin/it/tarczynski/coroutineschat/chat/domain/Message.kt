package it.tarczynski.coroutineschat.chat.domain

data class Message(
    val payload: String,
    val type: Type
) {

    enum class Type {
        CHAT_MESSAGE, HEART_BEAT,
    }
}
