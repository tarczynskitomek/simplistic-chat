package it.tarczynski.coroutineschat.chat.domain

import it.tarczynski.coroutineschat.chat.infrastructure.Message
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.consumeAsFlow
import kotlinx.coroutines.flow.onCompletion
import org.springframework.stereotype.Component
import java.util.concurrent.ConcurrentHashMap

@Component
class Chat {

    private val channels: MutableMap<String, Emitter> = ConcurrentHashMap()

    suspend fun send(
        channel: String,
        message: Message,
    ) {
        val channelMessageFlow = channels.computeIfAbsent(channel) { setupEmitter(channel) }
        channelMessageFlow.channel.send(message)
    }


    suspend fun forChannel(
        channel: String,
    ): Flow<Message> =
        coroutineScope {
            setupEmitter(channel).channelFlow
        }

    private fun setupEmitter(channel: String) = channels.computeIfAbsent(channel) {
        Emitter { channels.remove(channel) }
    }
}

data class Emitter(
    val channel: Channel<Message> = Channel(),
    val channelFlow: Flow<Message> = channel.consumeAsFlow().onCompletion { cleanup() },
    val cleanup: () -> Unit,
)
