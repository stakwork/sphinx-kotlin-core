package chat.sphinx.concepts.network.client

import io.ktor.client.*


abstract class NetworkClient {
    abstract suspend fun getClient(): HttpClient

    abstract fun addListener(listener: NetworkClientClearedListener): Boolean
    abstract fun removeListener(listener: NetworkClientClearedListener): Boolean
}
