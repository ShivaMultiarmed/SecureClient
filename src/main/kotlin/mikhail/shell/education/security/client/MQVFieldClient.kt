package mikhail.shell.education.security.client

import io.ktor.client.plugins.websocket.*
import io.ktor.websocket.*
import java.math.BigInteger

class MQVFieldClient(name: String): BaseFieldClient(name) {
    private var q: BigInteger? = null
    private var secretSessionKey: BigInteger? = null
    private var publicSessionKey: BigInteger? = null
    override suspend fun connect() {
        client.webSocket("ws://127.0.0.1:9876/handshake") {
            send(Frame.Text(name))
            q = receiveNumber()
            val l = q!!.bitLength() / 2
            p = receiveNumber()
            g = receiveNumber()
            println("q = $q")
            println("p = $p")
            println("g = $g")

            secretKey = generateSecretKey(q!!)
            println("секретный ключ x = $secretKey")
            publicKey = g.modPow(secretKey, p)
            println("открытый ключ X = $publicKey")
            sendNumber(publicKey)
            val Y = receiveNumber()
            println("открытый ключ Y = $Y")
            secretSessionKey = generateSecretKey(q!!)
            println("секретный ключ a = $secretSessionKey")
            publicSessionKey = g.modPow(secretSessionKey, p)
            println("открытый ключ A = $publicSessionKey")
            sendNumber(publicSessionKey!!)
            val B = receiveNumber()
            println("открытый ключ B = $B")
            val h = BigInteger.TWO.pow(l)
            val d = h + publicSessionKey!!.mod(h)
            println("d = $d")
            val e = h + B.mod(h)
            println("e = $e")
            val SA = (B * Y.modPow(e, p)).modPow((secretSessionKey!! + d * secretKey).mod(q), p)
            println("Общий секретный ключ: $SA")
        }
    }
}