package chat.sphinx.features.network.query.contact.model

import chat.sphinx.concepts.network.query.contact.model.ContactDto
import chat.sphinx.concepts.network.relay_call.RelayResponse
import kotlinx.serialization.Serializable

@Serializable
class ContactRelayResponse(
    override val success: Boolean,
    override val response: ContactDto?,
    override val error: String?
) : RelayResponse<ContactDto>()
