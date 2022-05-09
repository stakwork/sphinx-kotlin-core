package chat.sphinx.features.network.query.subscription.model

import chat.sphinx.concepts.network.query.subscription.model.SubscriptionDto
import chat.sphinx.concepts.network.relay_call.RelayResponse
import kotlinx.serialization.Serializable

@Serializable
internal data class GetSubscriptionsRelayResponse(
    override val success: Boolean,
    override val response: List<SubscriptionDto>? = null,
    override val error: String? = null
): RelayResponse<List<SubscriptionDto>>()
