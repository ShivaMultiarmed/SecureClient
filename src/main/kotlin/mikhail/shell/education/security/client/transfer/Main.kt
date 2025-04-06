package mikhail.shell.education.security.client.transfer

import kotlinx.coroutines.delay
import kotlinx.coroutines.runBlocking
import mikhail.shell.education.security.client.common.Client

fun main(args: Array<String>) = runBlocking {
    val state = when(args[0]) {
        "transfer" -> State.TRANSFERRING
        "listen" -> State.LISTENING
        else -> throw IllegalStateException()
    }
    val client: Client = EStreamClient(state)
    client.connect()
    if (state == State.TRANSFERRING) {
        client.transfer(mapOf("name" to "some.txt"), "Some information to be transfered".encodeToByteArray())
    } else {
        while (true) {
            delay(1000)
        }
    }
    client.disconnect()
}