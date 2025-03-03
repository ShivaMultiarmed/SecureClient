package mikhail.shell.education.security.client

import io.ktor.client.plugins.websocket.*
import io.ktor.websocket.*

class DiffieHellmanEllipticClient(name: String): BaseEllipticClient(name) {
    override suspend fun connect() {
        client.webSocket("ws://127.0.0.1:9876/handshake") {
            send(Frame.Text(name))
            p = receiveNumber()
            println("p = $p")
            q = receiveNumber()
            println("q = $q")
            a = receiveNumber()
            println("a = $a")
            b = receiveNumber()
            println("b = $b")
            val xG = receiveNumber()
            val yG = receiveNumber()
            G = xG to yG
            println("G = $G")
            secretKey = generateSecretKey(q!!)
            println("секретный ключ = $secretKey")
            publicKey = G!!.evaluateComposition(secretKey!!, p!!, a!!)
            println("свой открытый ключ = $publicKey")
            sendNumber(publicKey!!.first)
            sendNumber(publicKey!!.second)
            val otherPublicKeyX = receiveNumber()
            val otherPublicKeyY = receiveNumber()
            val otherPublicKey = otherPublicKeyX to otherPublicKeyY
            println("чужой открытый ключ = $otherPublicKey")
            val sharedSecretKey = otherPublicKey.evaluateComposition(secretKey!!, p!!, a!!)
            println("общий секретный ключ = $sharedSecretKey")
        }
    }
}