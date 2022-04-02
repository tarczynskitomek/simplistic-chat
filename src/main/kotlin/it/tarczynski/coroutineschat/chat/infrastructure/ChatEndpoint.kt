package it.tarczynski.coroutineschat.chat.infrastructure

import it.tarczynski.coroutineschat.chat.domain.ChannelName
import it.tarczynski.coroutineschat.chat.domain.FluxChat
import it.tarczynski.coroutineschat.chat.domain.Message
import org.springframework.http.MediaType
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import reactor.core.publisher.Flux

@RestController
@RequestMapping("/channels")
class ChatEndpoint(
    private val chat: FluxChat,
) {

    @GetMapping(
        path = ["/{channel}"],
        produces = [MediaType.TEXT_EVENT_STREAM_VALUE],
    )
    fun subscribeTo(@PathVariable channel: String): Flux<Message> = chat.forChannel(ChannelName(channel))

    @PostMapping(
        path = ["/{channel}/send"],
        consumes = [MediaType.APPLICATION_JSON_VALUE],
    )
    fun sendFluxMessage(
        @PathVariable channel: String,
        @RequestBody message: Message,
    ) = chat.send(ChannelName(channel), message)
}


