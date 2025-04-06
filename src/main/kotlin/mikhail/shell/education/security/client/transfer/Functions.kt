package mikhail.shell.education.security.client.transfer

import java.security.MessageDigest

fun ByteArray.hash(): ByteArray {
    val md = MessageDigest.getInstance("SHA-256")
    val hash = md.digest(this)
    return hash.copyOf(16)
}