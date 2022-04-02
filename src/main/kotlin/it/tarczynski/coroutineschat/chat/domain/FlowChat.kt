package it.tarczynski.coroutineschat.chat.domain

import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import org.springframework.stereotype.Component
import java.util.concurrent.ConcurrentHashMap

@Component
class FlowChat {

    /*
    MutableSharedFlows are hot and never collect, thus lifecycle callbacks triggered by terminal operations like
    collect are never called (onEach, onStart, onComplete) hence we don't know when a client is disconnected, and can't easily remove the channel
    from the map.

    On the other hand, it's possible to use channel + flow, but in this case we don't have broadcast - flow subscribers seem to be handled
    in round-robin fashion starting with the one that joined first.
     */
    private val channels: MutableMap<String, MutableSharedFlow<Message>> = ConcurrentHashMap()

    suspend fun send(
        channel: String,
        message: Message,
    ) = coroutineScope {
        val channelMessageFlow = channels.computeIfAbsent(channel) { MutableSharedFlow() }
        channelMessageFlow.emit(message)
    }

    fun forChannel(
        channel: String,
    ): Flow<Message> = channels.computeIfAbsent(channel) { MutableSharedFlow() }
}
