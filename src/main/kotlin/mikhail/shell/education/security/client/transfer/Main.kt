package mikhail.shell.education.security.client.transfer

import kotlinx.coroutines.*
import mikhail.shell.education.security.client.common.Client
import java.io.File

suspend fun main(args: Array<String>) {
    val state = when(args[0]) {
        "transfer" -> State.TRANSFERRING
        "listen" -> State.LISTENING
        else -> throw IllegalStateException()
    }
    val client: Client = EStreamClient(state)
    client.connect()
    if (state == State.TRANSFERRING) {
        val file = File("./image.png")
        client.transfer(mapOf("name" to file.name), file.readBytes())
    }
    while (true) {
        delay(1000)
    }
    client.disconnect()
}