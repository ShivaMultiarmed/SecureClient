package mikhail.shell.education.security.client.field

import io.ktor.client.*
import io.ktor.client.plugins.websocket.*
import io.ktor.websocket.*
import mikhail.shell.education.security.client.common.Client
import java.math.BigInteger
import java.nio.ByteBuffer

abstract class BaseFieldClient(val name: String): Client {
    protected val client = HttpClient {
        install(WebSockets.Plugin)
    }
    protected lateinit var secretKey: BigInteger
    lateinit var publicKey: BigInteger
    protected lateinit var sharedSecretKey: BigInteger
    lateinit var g: BigInteger
    lateinit var p: BigInteger
    override suspend fun disconnect() {
        TODO("Not yet implemented")
    }

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
