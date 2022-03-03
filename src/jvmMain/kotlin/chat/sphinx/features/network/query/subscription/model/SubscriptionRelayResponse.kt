package chat.sphinx.features.network.query.subscription.model

import chat.sphinx.concepts.network.query.subscription.model.SubscriptionDto
import chat.sphinx.concepts.network.relay_call.RelayResponse
import kotlinx.serialization.Serializable

@Serializable
data class SubscriptionRelayResponse(
    override val success: Boolean,
    override val response: SubscriptionDto?,
    override val error: String?
): RelayResponse<SubscriptionDto>()
