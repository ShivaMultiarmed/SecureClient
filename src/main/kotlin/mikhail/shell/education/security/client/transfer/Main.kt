package mikhail.shell.education.security.client.transfer

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.material.Button
import androidx.compose.material.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.key.Key.Companion.Window
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import kotlinx.coroutines.*
import mikhail.shell.education.security.client.common.Client
import java.awt.FileDialog
import java.awt.Frame
import java.io.File

suspend fun main(args: Array<String>) {
    val coroutineScope = CoroutineScope(Dispatchers.IO + SupervisorJob())
    val state = when(args[0]) {
        "transfer" -> State.TRANSFERRING
        "listen" -> State.LISTENING
        else -> throw IllegalStateException()
    }
    val client: Client = EStreamClient(state)
    client.connect()
    application {
        Window(
            onCloseRequest = ::exitApplication,
            title = if (state == State.TRANSFERRING) "Отправитель" else "Получатель"
        ) {
            ClientScreen(
                frame = window,
                onTransfer = {
                    val file = File(it)
                    coroutineScope.launch {
                        client.transfer(mapOf("name" to file.name), file.readBytes())
                    }
                }
            )
        }
    }
    while (true) {
        delay(1000)
    }
    client.disconnect()
}


@Composable
fun ClientScreen(
    frame: Frame,
    onTransfer: (String) -> Unit
) {
    var sentFilePath by remember { mutableStateOf(null as String?) }
    Column(
        modifier = Modifier
            .width(800.dp)
            .height(500.dp)
    ) {
        Button(
            onClick = {
                sentFilePath = openFile(frame)
            }
        ) {
            Text("Выберите файл")
        }
        sentFilePath?.let {
            val file = File(it)
            Text("Вы выбрали файл ${file.absolutePath}")
        }
        Button(
            onClick = {
                sentFilePath?.let {
                    onTransfer(it)
                }
            }
        ) {
            Text("Отправить")
        }
    }
}

fun openFile(
    parent: Frame,
    title: String = "Выберите файл"
): String? {
    val dialog = FileDialog(parent, title, FileDialog.LOAD)
    dialog.isVisible = true
    return if (dialog.file != null) "${dialog.directory}${dialog.file}" else null
}
