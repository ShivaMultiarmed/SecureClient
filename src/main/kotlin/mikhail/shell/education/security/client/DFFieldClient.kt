package mikhail.shell.education.security.client

import io.ktor.client.plugins.websocket.*
import io.ktor.websocket.*

class DFFieldClient(name: String): BaseFieldClient(name) {
    override suspend fun connect() {
        client.webSocket("ws://127.0.0.1:9876/handshake") {
            send(Frame.Text(name))
            p = receiveNumber()
            g = receiveNumber()
            println("p = $p")
            println("g = $g")

            secretKey = generateSecretKey(p)
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