package it.tarczynski.coroutineschat.chat.domain

import it.tarczynski.coroutineschat.chat.infrastructure.Message
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import org.springframework.stereotype.Component
import java.util.concurrent.ConcurrentHashMap

@Component
class Chat {

    private val channels: MutableMap<String, MutableSharedFlow<Message>> = ConcurrentHashMap()

    suspend fun send(
        channel: String,
        message: Message,
    ) {
        val channelMessageFlow = channels.computeIfAbsent(channel) { MutableSharedFlow() }
        channelMessageFlow.emit(message)
    }


    fun forChannel(
        channel: String,
    ): Flow<Message> = channels.computeIfAbsent(channel) { MutableSharedFlow() }
}
