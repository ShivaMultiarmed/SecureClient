package mikhail.shell.education.security.client.common

import kotlinx.coroutines.*
import mikhail.shell.education.security.client.elliptic.DhiEllipticClient
import mikhail.shell.education.security.client.elliptic.MqvEllipticClient
import mikhail.shell.education.security.client.field.DFFieldClient
import mikhail.shell.education.security.client.field.DFIFieldClient
import mikhail.shell.education.security.client.field.MQVFieldClient
import kotlin.system.measureTimeMillis

fun main(args: Array<String>) = runBlocking {
    var type = Protocol.DEFFIE_HELLMAN
    var subType = MathType.ALGEBRAIC
    var name = "ALICE"
    for (i in 0..<args.size) {
        when(args[i]) {
            "--dh" -> type = Protocol.DEFFIE_HELLMAN
            "--dhi" -> type = Protocol.DEFFIE_HELLMAN_IMPROVED
            "--mqv" -> type = Protocol.MQV
            "--name" -> {
                name = args[i + 1]
                continue
            }
            "--alg" -> subType = MathType.ALGEBRAIC
            "--el" -> subType = MathType.ELLIPTIC
        }
    }
    val client: Client = when (type) {
        Protocol.DEFFIE_HELLMAN -> DFFieldClient(name)
        Protocol.DEFFIE_HELLMAN_IMPROVED -> when (subType) {
            MathType.ALGEBRAIC -> DFIFieldClient(name)
            MathType.ELLIPTIC -> DhiEllipticClient(name)
        }
        Protocol.MQV -> when(subType) {
            MathType.ALGEBRAIC -> MQVFieldClient(name)
            MathType.ELLIPTIC -> MqvEllipticClient(name)
        }

    }
    val timeSpent = measureTimeMillis {
        client.connect()
    }
    println("Результат выполнен за $timeSpent мс.")
}

enum class Protocol {
    DEFFIE_HELLMAN, DEFFIE_HELLMAN_IMPROVED, MQV
}

enum class MathType {
    ALGEBRAIC, ELLIPTIC
}