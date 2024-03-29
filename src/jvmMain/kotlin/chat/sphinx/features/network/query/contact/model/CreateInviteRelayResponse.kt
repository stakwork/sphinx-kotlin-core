package chat.sphinx.features.network.query.contact.model

import chat.sphinx.concepts.network.query.contact.model.ContactDto
import chat.sphinx.concepts.network.relay_call.RelayResponse
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonNames

@Serializable
class CreateInviteRelayResponse(
    override val success: Boolean,

    @SerialName("contact")
    @JsonNames("contact")
    override val response: ContactDto? = null,

    override val error: String? = null
) : RelayResponse<ContactDto>()
