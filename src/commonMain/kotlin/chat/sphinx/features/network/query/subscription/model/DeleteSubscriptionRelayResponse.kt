package chat.sphinx.features.network.query.subscription.model

import chat.sphinx.concepts.network.relay_call.RelayResponse
import kotlinx.serialization.Serializable

@Serializable
data class DeleteSubscriptionRelayResponse(
    override val success: Boolean,
    override val response: Any?,
    override val error: String?
): RelayResponse<Any>()
