package mikhail.shell.education.security.client

import io.ktor.client.*
import io.ktor.client.plugins.websocket.*
import io.ktor.websocket.*
import java.math.BigInteger
import java.nio.ByteBuffer

abstract class BaseClient(val name: String): Client {
    protected val client = HttpClient {
        install(WebSockets.Plugin)
    }
    protected lateinit var secretKey: BigInteger
    lateinit var publicKey: BigInteger
    protected lateinit var sharedSecretKey: BigInteger
    lateinit var g: BigInteger
    lateinit var p: BigInteger
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
