package mikhail.shell.education.security.client.transfer

import io.ktor.client.*
import io.ktor.client.engine.cio.*
import io.ktor.client.plugins.websocket.*
import io.ktor.websocket.*
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.collectLatest
import mikhail.shell.education.security.client.common.Client
import java.io.File
import kotlin.math.min

enum class State {
    TRANSFERRING, LISTENING
}

class EStreamClient(state: State) : Client {
    private val coroutineScope = CoroutineScope(Dispatchers.IO + SupervisorJob())
    private companion object {
        const val BUFFER_SIZE = 1024
    }
    private lateinit var session: DefaultWebSocketSession
    private val state = MutableStateFlow(state)
    private lateinit var listeningJob: Job
    private val httpClient = HttpClient(CIO) {
        install(WebSockets)
    }
    override suspend fun connect() {
        coroutineScope.launch {
            httpClient.webSocket("ws://localhost:9999/transfer") {
                session = this
                print((if (state.value == State.LISTENING) "Receiver" else "Sender") + " is connected")
                while(isActive) {
                    if (state.value == State.LISTENING) {
                        val fileSize = (incoming.receive() as Frame.Text).readText().toLong()
                        print("Received file size: ${fileSize}b")
                        val fileName = (incoming.receive() as Frame.Text).readText()
                        print("Received file name: $fileName")
                        val file = File("./$fileName")
                        file.createNewFile()
                        var bytesLeft = fileSize
                        while (bytesLeft > 0) {
                            val bytesPart = (incoming.receive() as Frame.Binary).readBytes()
                            println("Received data part: ${bytesPart.size}b")
                            file.appendBytes(bytesPart)
                            bytesLeft -= bytesPart.size
                        }
                    }
                    delay(1000)
                }
            }
        }
        while(!::session.isInitialized) {
            delay(1000)
        }
    }
    override suspend fun transfer(meta: Map<String, Any>, data: ByteArray) {
        val length = data.size
        var bytesLeft = length
        session.outgoing.send(Frame.Text(length.toString()))
        print("Sent file size: ${length}b")
        session.outgoing.send(Frame.Text(meta["name"].toString()))
        print("Sent file name: ${meta["name"]}")
        while (bytesLeft > 0) {
            val isFinal = bytesLeft < BUFFER_SIZE
            session.outgoing.send(
                Frame.Binary(
                    fin = isFinal,
                    data = data
                )
            )
            println("Sent byte data")
            bytesLeft -= BUFFER_SIZE
        }
    }
    override suspend fun disconnect() = Unit
}