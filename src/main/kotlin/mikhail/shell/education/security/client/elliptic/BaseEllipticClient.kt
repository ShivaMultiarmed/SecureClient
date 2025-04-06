package mikhail.shell.education.security.client.elliptic

import io.ktor.client.*
import io.ktor.client.plugins.websocket.*
import io.ktor.websocket.*
import mikhail.shell.education.security.client.common.Client
import java.math.BigInteger
import java.nio.ByteBuffer

abstract class BaseEllipticClient(val name: String): Client {
    protected val client = HttpClient {
        install(WebSockets.Plugin)
    }
    protected var p: BigInteger? = null
    protected var q: BigInteger? = null
    protected var a: BigInteger? = null
    protected var b: BigInteger? = null

    protected var G: Pair<BigInteger, BigInteger>? = null
    protected var secretKey: BigInteger? = null
    protected var sharedSecretKey: Pair<BigInteger, BigInteger>? = null
    override suspend fun disconnect() {
        TODO("Not yet implemented")
    }

    var publicKey: Pair<BigInteger, BigInteger>? = null
    protected suspend fun WebSocketSession.receiveNumber(): BigInteger {
        return BigInteger((incoming.receive() as Frame.Binary).readBytes())
    }
    protected suspend fun WebSocketSession.sendNumber(number: BigInteger) {
        send(
            Frame.Binary(
                true,
                ByteBuffer.wrap(number.toByteArray())
            )
        )
    }
}