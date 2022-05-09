package chat.sphinx.features.network.query.contact.model

import chat.sphinx.concepts.network.query.contact.model.GetLatestContactsResponse
import chat.sphinx.concepts.network.relay_call.RelayResponse
import kotlinx.serialization.Serializable

@Serializable
internal data class GetLatestContactsRelayResponse(
    override val success: Boolean,
    override val response: GetLatestContactsResponse? = null,
    override val error: String? = null
) : RelayResponse<GetLatestContactsResponse>()
