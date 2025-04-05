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

enum class State {
    TRANSFERRING, LISTENING
}

class EStreamClient : Client {
    private val coroutineScope = CoroutineScope(Dispatchers.IO + SupervisorJob())
    private companion object {
        const val BUFFER_SIZE = 1024
    }
    private lateinit var session: DefaultWebSocketSession
    private val state = MutableStateFlow(State.LISTENING)
    private lateinit var listeningJob: Job
    private val httpClient = HttpClient(CIO) {
        install(WebSockets)
    }
    override suspend fun connect() {
        httpClient.webSocket("ws://localhost:8000/transfer") {
            session = this
            listeningJob = coroutineScope.launch {
                val fileSize = (incoming.receive() as Frame.Text).readText().toLong()
                val fileName = (incoming.receive() as Frame.Text).readText()
                val file = File("./$fileName")
                file.createNewFile()
                var bytesLeft = fileSize
                while (bytesLeft > 0) {
                    val bytesPart = (incoming.receive() as Frame.Binary).readBytes()
                    file.appendBytes(bytesPart)
                    bytesLeft -= BUFFER_SIZE
                }
            }
            state.collectLatest {
                if (it == State.TRANSFERRING) {
                    listeningJob.cancel()
                } else {
                    listeningJob.start()
                }
            }
        }
    }
    override suspend fun transfer(meta: Map<String, Any>, data: ByteArray) {
        val length = data.size
        var bytesLeft = length
        session.outgoing.send(Frame.Text(length.toString()))
        session.outgoing.send(Frame.Text(meta["name"].toString()))
        while (bytesLeft > 0) {
            val isFinal = bytesLeft < BUFFER_SIZE
            session.outgoing.send(
                Frame.Binary(
                    fin = isFinal,
                    data = data
                )
            )
            bytesLeft -= BUFFER_SIZE
        }
    }
    override suspend fun disconnect() = Unit
}