package mikhail.shell.education.security.client.transfer

import kotlinx.coroutines.*
import mikhail.shell.education.security.client.common.Client

suspend fun main(args: Array<String>) {
    val state = when(args[0]) {
        "transfer" -> State.TRANSFERRING
        "listen" -> State.LISTENING
        else -> throw IllegalStateException()
    }
    val client: Client = EStreamClient(state)
    client.connect()
    if (state == State.TRANSFERRING) {
        client.transfer(mapOf("name" to "some.txt"), "Some information to be transfered".encodeToByteArray())
    }
    while (true) {
        delay(1000)
    }
    client.disconnect()
}