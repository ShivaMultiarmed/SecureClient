package mikhail.shell.security.education.client

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import java.io.BufferedReader
import java.io.PrintWriter
import java.math.BigInteger
import java.net.Socket

class User {
    val scope = CoroutineScope(Dispatchers.IO + SupervisorJob())
    private lateinit var x: BigInteger
    lateinit var X: BigInteger
    private lateinit var sharedSecretKey: BigInteger
    lateinit var q: BigInteger
    lateinit var g: BigInteger
    lateinit var p: BigInteger
    lateinit var socket: Socket
    private lateinit var writer: PrintWriter
    private lateinit var reader: BufferedReader
    fun connect(port: Int) {
        socket = Socket("127.0.0.1", port)
        writer = PrintWriter(socket.getOutputStream(), true)
        reader = BufferedReader(socket.getInputStream().reader())
        writer.println("CONNECT")
        q = reader.readLine()?.toBigInteger() ?: BigInteger.TWO
        p = reader.readLine()?.toBigInteger() ?: BigInteger.TWO
        g = reader.readLine()?.toBigInteger() ?: BigInteger.TWO
        x = generateSecretKey(q)
        X = g.modPow(x, p)
        writer = PrintWriter(socket.getOutputStream())
        reader = BufferedReader(socket.getInputStream().reader())
        handshake()
    }

    fun handshake() {
        writer.println("HANDSHAKE")
        writer.println(X)
        reader.readLine()
        val Y: BigInteger = reader.readLine().toBigInteger()
        sharedSecretKey = Y.modPow(x, p)
    }
}