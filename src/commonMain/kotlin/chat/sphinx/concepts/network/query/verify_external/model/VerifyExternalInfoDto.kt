package chat.sphinx.concepts.network.query.verify_external.model

import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonNames

@Serializable
data class VerifyExternalInfoDto(
    val price_to_meet: Long?,
    val jwt: String?,
    val photo_url: String?,
    val contact_key: String?,
    val route_hint: String?,
    val pubkey: String?,
    val alias: String?,
) {

    var url: String? = null

    @JsonNames("verification_signature")
    var verificationSignature: String? = null
}