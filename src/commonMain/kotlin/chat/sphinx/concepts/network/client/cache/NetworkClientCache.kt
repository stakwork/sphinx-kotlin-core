package chat.sphinx.concepts.network.client.cache

import chat.sphinx.concepts.network.client.NetworkClient
import io.ktor.client.*

abstract class NetworkClientCache: NetworkClient() {

    companion object {
        const val MAX_STALE_VALUE = 60 * 60 * 24 * 30 // 1 month
    }

    abstract suspend fun getCachingClient(): HttpClient
}
