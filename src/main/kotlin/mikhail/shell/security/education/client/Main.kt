package mikhail.shell.security.education.client

import kotlinx.coroutines.*

fun main() = runBlocking {
    val scope = CoroutineScope(Dispatchers.IO + SupervisorJob())
    val alice = User()
    val bob = User()
    val client1job = scope.launch {
        alice.connect(12345)
    }
    val client2job = scope.launch {
        bob.connect(12345)
    }
    delay(1000000000000000000)
}