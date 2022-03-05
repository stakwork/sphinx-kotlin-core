package chat.sphinx.utils.platform

import chat.sphinx.concepts.network.query.contact.model.ContactDto
import chat.sphinx.concepts.network.query.contact.model.PostContactDto
import chat.sphinx.concepts.network.query.message.model.MessageDto
import chat.sphinx.concepts.network.relay_call.RelayResponse
import chat.sphinx.features.network.query.contact.model.ContactRelayResponse
import kotlinx.serialization.KSerializer
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

fun<Result: Any, Input: Any, Output: RelayResponse<Result>> method(
    responseJsonSerializer: KSerializer<Output>,
    requestBodyPair: Pair<Input, KSerializer<Input>>?,
): Result? {
    requestBodyPair?.let { (requestBody, requestBodySerializer) ->
        val str = Json.encodeToString(requestBodySerializer, requestBody)
    }

    return Json.decodeFromString(responseJsonSerializer, "").response
}


fun caller() {

    method(
        ContactRelayResponse.serializer(),
        Pair(
            PostContactDto(
                "",
                "",
                0
            ),
            PostContactDto.serializer()
        )
    )
}