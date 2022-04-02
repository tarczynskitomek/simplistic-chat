package it.tarczynski.coroutineschat.chat.domain

import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.channels.ChannelResult
import kotlinx.coroutines.channels.onFailure
import kotlinx.coroutines.channels.onSuccess
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.reactor.asFlux
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component
import reactor.core.publisher.Flux
import java.util.concurrent.ConcurrentHashMap

@Component
class FluxChat {

    private val channels: MutableMap<ChannelName, FluxEmitter> = ConcurrentHashMap()

    fun send(channel: ChannelName, message: Message) {
        channels
            .computeIfAbsent(channel) { createEmitterFor(channel) }
            .emit(message)
            .onSuccess { logger.info("Successfully sent message [{}] to channel [{}]", message, channel) }
            .onFailure { logger.warn("Failed to publish message [{}] to channel [{}]", message, channel) }
    }

    fun forChannel(channel: ChannelName): Flux<Message> =
        channels
            .computeIfAbsent(channel) { createEmitterFor(channel) }
            .messageFlux

    private fun createEmitterFor(channel: ChannelName) = FluxEmitter(channel) { channels.remove(channel) }

    companion object {
        val logger: Logger = LoggerFactory.getLogger(FluxChat::class.java)
    }

    private class FluxEmitter(
        private val channelName: ChannelName,
        private val cleanup: () -> Unit,
    ) {
        private val channel: Channel<Message> = Channel()

        val messageFlux: Flux<Message> by lazy {
            channel.receiveAsFlow()
                .asFlux() // main flux of messages to given channel
                .doOnCancel { closeSourceChannel() } // when the last subscriber cancels, close the source channel
                .doOnComplete { executeCleanup() } // on closing the source channel, the stream is completed - remove the emitter from the map
                .share() // multicast, yay! Creates multiple downstream fluxes, one for each subscriber
                .doOnSubscribe { logger.info("Subscribed to channel [{}]", channelName) }
                .doOnCancel { logger.info("Unsubscribed from channel [{}]", channelName) }
        }

        private fun closeSourceChannel() {
            logger.info("Last subscriber disconnected, closing channel")
            channel.close()
        }

        private fun executeCleanup() {
            logger.info("Channel closed, cleaning up...")
            cleanup()
        }

        companion object {
            private val logger: Logger = LoggerFactory.getLogger(FluxEmitter::class.java)
        }

        /*
        channel.trySend instead of send because send will wait until there's at least one active reader from the channel
        trySent on the other hand will wait
         */
        fun emit(message: Message): ChannelResult<Unit> = channel.trySend(message)
    }
}
