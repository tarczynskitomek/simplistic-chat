package it.tarczynski.coroutineschat

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication

@SpringBootApplication
class CoroutinesChatApplication

fun main(args: Array<String>) {
    runApplication<CoroutinesChatApplication>(*args)
}
