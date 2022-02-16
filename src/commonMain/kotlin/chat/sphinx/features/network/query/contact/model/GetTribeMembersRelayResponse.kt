package chat.sphinx.features.network.query.contact.model

import chat.sphinx.concepts.network.query.contact.model.GetTribeMembersResponse
import chat.sphinx.concepts.network.relay_call.RelayResponse
import kotlinx.serialization.Serializable

@Serializable
data class GetTribeMembersRelayResponse(
    override val success: Boolean,
    override val response: GetTribeMembersResponse?,
    override val error: String?
): RelayResponse<GetTribeMembersResponse>()
