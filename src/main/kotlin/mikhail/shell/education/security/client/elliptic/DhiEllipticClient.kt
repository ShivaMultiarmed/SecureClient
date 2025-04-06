package mikhail.shell.education.security.client.elliptic

import io.ktor.client.plugins.websocket.*
import io.ktor.websocket.*
import mikhail.shell.education.security.client.common.evaluateComposition
import mikhail.shell.education.security.client.common.generateSecretKey

class DhiEllipticClient(name: String): BaseEllipticClient(name) {
    override suspend fun connect() {
        client.webSocket("ws://127.0.0.1:9876/handshake") {
            send(Frame.Text(name))
            p = receiveNumber()
            println("$name: p = $p")
            q = receiveNumber()
            println("$name: q = $q")
            a = receiveNumber()
            println("$name: a = $a")
            b = receiveNumber()
            println("$name: b = $b")
            val xG = receiveNumber()
            val yG = receiveNumber()
            G = xG to yG
            println("$name: G = $G")
            secretKey = generateSecretKey(q!!)
            println("$name: секретный ключ = $secretKey")
            publicKey = G!!.evaluateComposition(secretKey!!, p!!, a!!)
            println("$name: свой открытый ключ = $publicKey")
            sendNumber(publicKey!!.first)
            sendNumber(publicKey!!.second)
            val otherPublicKeyX = receiveNumber()
            val otherPublicKeyY = receiveNumber()
            val otherPublicKey = otherPublicKeyX to otherPublicKeyY
            println("$name: чужой открытый ключ = $otherPublicKey")
            sharedSecretKey = otherPublicKey.evaluateComposition(secretKey!!, p!!, a!!)
            println("$name: общий секретный ключ = $sharedSecretKey")
        }
    }

    override suspend fun transfer(meta: Map<String, Any>, data: ByteArray) {
        TODO("Not yet implemented")
    }
}