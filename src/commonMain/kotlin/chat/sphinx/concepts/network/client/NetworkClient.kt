package chat.sphinx.concepts.network.client

import okhttp3.OkHttpClient

abstract class NetworkClient {
    abstract suspend fun getClient(): OkHttpClient

    abstract fun addListener(listener: NetworkClientClearedListener): Boolean
    abstract fun removeListener(listener: NetworkClientClearedListener): Boolean
}
