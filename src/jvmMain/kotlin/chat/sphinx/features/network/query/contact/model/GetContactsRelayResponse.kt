package chat.sphinx.features.network.query.contact.model

import chat.sphinx.concepts.network.query.contact.model.GetContactsResponse
import chat.sphinx.concepts.network.relay_call.RelayResponse
import kotlinx.serialization.Serializable

@Serializable
internal data class GetContactsRelayResponse(
    override val success: Boolean,
    override val response: GetContactsResponse?,
    override val error: String?
) : RelayResponse<GetContactsResponse>()
