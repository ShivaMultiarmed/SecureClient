package mikhail.shell.education.security.client

import kotlinx.coroutines.*

fun main(args: Array<String>) = runBlocking {
    var type = Protocol.DEFFIE_HELLMAN
    var name = "ALICE"
    for (i in 0..<args.size) {
        when(args[i]) {
            "--dh" -> type = Protocol.DEFFIE_HELLMAN
            "--dfi" -> type = Protocol.DEFFIE_HELLMAN_IMPROVED
            "--mqv" -> type = Protocol.MQV
            "--name" -> {
                name = args[i + 1]
                continue
            }
        }
    }
    val client: Client = DFIClient(name)
    client.connect()
}

enum class Protocol {
    DEFFIE_HELLMAN, DEFFIE_HELLMAN_IMPROVED, MQV
}