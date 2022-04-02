package it.tarczynski.coroutineschat.chat.infrastructure

import it.tarczynski.coroutineschat.chat.domain.Chat
import kotlinx.coroutines.flow.Flow
import org.springframework.http.MediaType
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/channels")
class ChatEndpoint(
    private val chat: Chat,
) {

    @GetMapping("/{channel}", produces = [MediaType.TEXT_EVENT_STREAM_VALUE])
    suspend fun everyone(@PathVariable channel: String): Flow<Message> = chat.forChannel(channel)

    @PostMapping("/{channel}/send")
    suspend fun sendMessage(@PathVariable channel: String, @RequestBody message: Message) {
        chat.send(channel, message)
    }
}

data class Message(val payload: String)

