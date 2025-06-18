package chat.sphinx.features.network.query.verify_external

import chat.sphinx.concepts.network.query.verify_external.NetworkQueryAuthorizeExternal
import chat.sphinx.concepts.network.query.verify_external.model.PersonInfoDto
import chat.sphinx.concepts.network.query.verify_external.model.VerifyExternalInfoDto
import chat.sphinx.concepts.network.relay_call.NetworkRelayCall
import chat.sphinx.response.LoadResponse
import chat.sphinx.response.ResponseError
import kotlinx.coroutines.flow.Flow
import kotlinx.serialization.PolymorphicSerializer

class NetworkQueryAuthorizeExternalImpl(
    private val networkRelayCall: NetworkRelayCall,
): NetworkQueryAuthorizeExternal() {

    companion object {
        private const val ENDPOINT_VERIFY_EXTERNAL = "/verify_external"
    }

    override fun authorizeExternal(
        host: String,
        challenge: String,
        token: String,
        info: VerifyExternalInfoDto,
    ): Flow<LoadResponse<Any, ResponseError>> =
        networkRelayCall.post(
            url = "https://$host/verify/$challenge?token=$token",
            responseJsonSerializer = PolymorphicSerializer(Any::class),
            requestBodyPair = Pair(
                info,
                VerifyExternalInfoDto.serializer()
            ),
        )

    override fun getPersonInfo(
        host: String,
        publicKey: String
    ): Flow<LoadResponse<PersonInfoDto, ResponseError>> =
        networkRelayCall.get(
            url = "https://$host/person/$publicKey",
            responseJsonSerializer = PersonInfoDto.serializer(),
        )
}
