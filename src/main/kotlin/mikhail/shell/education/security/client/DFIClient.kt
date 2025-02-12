package mikhail.shell.education.security.client

import io.ktor.client.plugins.websocket.*
import io.ktor.websocket.*
import java.math.BigInteger

class DFIClient(name: String): Client(name) {
    lateinit var q: BigInteger
    override suspend fun connect() {
        client.webSocket("ws://127.0.0.1:9876/handshake") {
            send(Frame.Text(name))
            q = receiveNumber()
            p = receiveNumber()
            g = receiveNumber()
            println("q = $q")
            println("p = $p")
            println("g = $g")

            secretKey = generateSecretKey(q)
            println("секретный ключ x = $secretKey")
            publicKey = g.modPow(secretKey, p)
            println("открытый ключ X = $publicKey")
            sendNumber(publicKey)
            val Y = receiveNumber()
            println("открытый ключ Y = $Y")
            sharedSecretKey = Y.modPow(secretKey, p)
            println("Общий секретный ключ $sharedSecretKey")
        }
    }
}