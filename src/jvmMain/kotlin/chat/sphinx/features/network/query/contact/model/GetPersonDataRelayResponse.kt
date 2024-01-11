package chat.sphinx.features.network.query.contact.model

import chat.sphinx.concepts.network.query.contact.model.PersonDataDto
import chat.sphinx.concepts.network.relay_call.RelayResponse
import kotlinx.serialization.Serializable

@Serializable
data class GetPersonDataRelayResponse(
    override val success: Boolean,
    override val response: PersonDataDto? = null,
    override val error: String? = null
): RelayResponse<PersonDataDto>()
