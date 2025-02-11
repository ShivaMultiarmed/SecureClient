package mikhail.shell.education.security.client

import io.ktor.client.*
import io.ktor.client.plugins.websocket.*
import io.ktor.websocket.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import java.io.BufferedReader
import java.io.PrintWriter
import java.math.BigInteger
import java.net.Socket
import java.nio.ByteBuffer

class User(val name: String) {
    val scope = CoroutineScope(Dispatchers.IO + SupervisorJob())
    private val client = HttpClient {
        install(WebSockets.Plugin)
    }
    private lateinit var x: BigInteger
    lateinit var X: BigInteger
    private lateinit var sharedSecretKey: BigInteger
    lateinit var q: BigInteger
    lateinit var g: BigInteger
    lateinit var p: BigInteger

    suspend fun connect() {
        client.webSocket("ws://127.0.0.1:9876/handshake") {
            send(Frame.Text(name))
            q = receiveNumber()
            p = receiveNumber()
            g = receiveNumber()

            x = generateSecretKey(q)
            X = g.modPow(x, p)
            println("$X\n")
            sendNumber(X)
            val Y = receiveNumber()
            println("$Y\n")
            sharedSecretKey = Y.modPow(x, p)
            println("Shared secret key is $sharedSecretKey")
        }
    }
}
private suspend fun WebSocketSession.receiveNumber(): BigInteger {
    return BigInteger((incoming.receive() as Frame.Binary).readBytes())
}
private suspend fun WebSocketSession.sendNumber(number: BigInteger) {
    send(
        Frame.Binary(
            true,
            ByteBuffer.wrap(number.toByteArray())
        )
    )
}