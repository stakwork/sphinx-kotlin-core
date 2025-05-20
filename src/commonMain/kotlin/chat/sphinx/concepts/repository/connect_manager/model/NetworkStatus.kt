package chat.sphinx.concepts.repository.connect_manager.model

sealed class NetworkStatus {

    object Loading: NetworkStatus()
    object Connected: NetworkStatus()
    object Disconnected: NetworkStatus()
}