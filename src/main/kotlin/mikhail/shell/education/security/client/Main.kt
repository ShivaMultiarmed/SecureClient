package mikhail.shell.education.security.client

import kotlinx.coroutines.*

fun main(args: Array<String>) = runBlocking {
    //val scope = CoroutineScope(Dispatchers.IO + SupervisorJob())
    val name = args.getOrNull(0)?: "ALICE"
    val user = User(name)
    val userJob = launch {
        user.connect()
    }
}