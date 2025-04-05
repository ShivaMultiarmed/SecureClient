package mikhail.shell.education.security.client.common

interface Client {
    suspend fun connect()
    suspend fun transfer(meta: Map<String, Any>, data: ByteArray)
    suspend fun disconnect()
}