package mikhail.shell.education.security.client.transfer

import io.ktor.client.*
import io.ktor.client.engine.cio.*
import io.ktor.client.plugins.websocket.*
import io.ktor.network.selector.*
import io.ktor.network.sockets.*
import io.ktor.utils.io.*
import io.ktor.websocket.*
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.MutableStateFlow
import mikhail.shell.education.security.client.common.Client
import java.io.File
import java.math.BigInteger
import java.util.*

enum class State {
    TRANSFERRING, LISTENING
}

class EStreamClient(state: State) : Client {
    private val coroutineScope = CoroutineScope(Dispatchers.IO + SupervisorJob())
    private companion object {
        const val BUFFER_SIZE = 1024
        val K = BigInteger("43327941536451757547021212229086144792243993750900499115474182045548247479320").toByteArray().hash()
    }
    private lateinit var session: DefaultWebSocketSession
    private val state = MutableStateFlow(state)
    private lateinit var socket: Socket
    private lateinit var ecnryptReadChannel: ByteReadChannel
    private lateinit var ecnryptWriteChannel: ByteWriteChannel
    private lateinit var listeningJob: Job
    private val httpClient = HttpClient(CIO) {
        install(WebSockets)
    }
    @OptIn(ExperimentalStdlibApi::class)
    override suspend fun connect() {
        coroutineScope.launch {
            val port = if (state.value == State.LISTENING) 9000 else 8000
            launch {
                socket = aSocket(SelectorManager()).tcp().connect("localhost", port)
                ecnryptReadChannel = socket.openReadChannel()
                ecnryptWriteChannel = socket.openWriteChannel(autoFlush = true)
            }
            httpClient.webSocket("ws://localhost:9999/transfer") {
                session = this
                print((if (state.value == State.LISTENING) "Получатель" else "Отправитель") + " подключен\n")
                while(isActive) {
                    if (state.value == State.LISTENING) {
                        val fileSize = (incoming.receive() as Frame.Text).readText().toLong()
                        print("Полученный размер файла: ${fileSize}b\n")
                        val fileName = (incoming.receive() as Frame.Text).readText()
                        print("Полученное имя файла: $fileName\n")
                        val file = File("D:/Downloads/$fileName")
                        file.createNewFile()
                        var bytesLeft = fileSize
                        while (bytesLeft > 0) {
                            val iv = (incoming.receive() as Frame.Binary).readBytes()
                            println("Полученный IV: ${iv.toHexString()}\n")
                            val encryptedBytesPart = (incoming.receive() as Frame.Binary).readBytes()
                            println("Полученные зашифрованные байты: ${encryptedBytesPart.toHexString()}\n")
                            val bytesPart = encryptedBytesPart.encrypt(K, iv)
                            println("Полученные исходные байты: ${bytesPart.toHexString()}\n")
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
    @OptIn(ExperimentalStdlibApi::class)
    override suspend fun transfer(meta: Map<String, Any>, data: ByteArray) {
        val length = data.size
        var bytesLeft = length
        session.outgoing.send(Frame.Text(length.toString()))
        print("Отправленный размер файла: ${length}b\n")
        session.outgoing.send(Frame.Text(meta["name"].toString()))
        print("Отправленное название файла: ${meta["name"]}\n")
        while (bytesLeft > 0) {
            val isFinal = bytesLeft < BUFFER_SIZE
            val start = length - bytesLeft
            val end = (start + BUFFER_SIZE).coerceAtMost(length)
            val dataPart = data.sliceArray(start until end)
            println("Исходные байты данных: ${dataPart.toHexString()}\n")
            val iv = generateIV()
            val encryptedDataPart = dataPart.encrypt(K, iv)
            session.outgoing.send(
                Frame.Binary(
                    fin = true,
                    data = iv
                )
            )
            println("Отправленный IV: ${iv.toHexString()}\n")
            session.outgoing.send(
                Frame.Binary(
                    fin = true,
                    data = encryptedDataPart
                )
            )
            println("Отправленные зашифрованные байты: ${encryptedDataPart.toHexString()}\n")
            bytesLeft -= encryptedDataPart.size
        }
    }
    override suspend fun disconnect() {
        socket.close()
    }
    private suspend fun ByteArray.encrypt(key: ByteArray, iv: ByteArray): ByteArray {
        ecnryptWriteChannel.writeFully(key, 0, key.size)
        ecnryptWriteChannel.writeFully(iv, 0, iv.size)
        ecnryptWriteChannel.writeFully(this, 0, size)
        val receivedData = ByteArray(size)
        ecnryptReadChannel.readAvailable(receivedData, 0, receivedData.size)
        return receivedData
    }
    private fun generateIV(): ByteArray {
        return BigInteger.probablePrime(1024, Random()).toByteArray().hash()
    }
}