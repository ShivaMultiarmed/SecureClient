package mikhail.shell.education.security.client.elliptic

import io.ktor.client.plugins.websocket.*
import io.ktor.websocket.*
import mikhail.shell.education.security.client.common.evaluateComposition
import mikhail.shell.education.security.client.common.generateSecretKey
import java.math.BigInteger

class MqvEllipticClient(name: String): BaseEllipticClient(name) {
    private var secretSessionKey: BigInteger? = null
    var publicSessionKey: Pair<BigInteger, BigInteger>? = null
    override suspend fun transfer(meta: Map<String, Any>, data: ByteArray) {
        TODO("Not yet implemented")
    }

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
            secretSessionKey = generateSecretKey(q!!)
            println("$name: секретный сеансовый ключ = $secretKey")
            publicSessionKey = G!!.evaluateComposition(secretSessionKey!!, p!!, a!!)
            println("$name: открытый сеансовый ключ = $publicSessionKey")
            sendNumber(publicSessionKey!!.first)
            sendNumber(publicSessionKey!!.second)
            val otherPublicSessionKeyX = receiveNumber()
            val otherPublicSessionKeyY = receiveNumber()
            val otherPublicSessionKey = otherPublicSessionKeyX to otherPublicSessionKeyY
            println("$name: чужой открытый сеансовый ключ = $otherPublicSessionKey")
            val l = q!!.bitLength() / 2
            val d = (BigInteger.TWO.pow(l) + publicSessionKey!!.first.mod(BigInteger.TWO.pow(l))).mod(p)
            val e = (BigInteger.TWO.pow(l) + otherPublicSessionKeyX.mod(BigInteger.TWO.pow(l))).mod(p)
            sharedSecretKey = otherPublicKey.evaluateComposition(d, p!!, a!!)
                .evaluateComposition(otherPublicSessionKey, p!!, a!!)
                .evaluateComposition(secretSessionKey!! + e * secretKey!!, p!!, a!!)
            println("$name: общий секретный ключ = $sharedSecretKey")
        }
    }
}