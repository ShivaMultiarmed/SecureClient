package mikhail.shell.education.security.client

import kotlinx.coroutines.*
import kotlin.system.measureTimeMillis

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
    val client: Client = when (type) {
        Protocol.DEFFIE_HELLMAN -> DFClient(name)
        Protocol.DEFFIE_HELLMAN_IMPROVED -> DFIClient(name)
        else -> MQVClient(name)
    }
    val timeSpent = measureTimeMillis {
        client.connect()
    }
    println("Результат выполнен за $timeSpent мс.")
}

enum class Protocol {
    DEFFIE_HELLMAN, DEFFIE_HELLMAN_IMPROVED, MQV
}