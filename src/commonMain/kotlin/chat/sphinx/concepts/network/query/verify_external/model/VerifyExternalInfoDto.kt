package chat.sphinx.concepts.network.query.verify_external.model

import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonNames

@Serializable
data class VerifyExternalInfoDto(
    val price_to_meet: Long? = null,
    val jwt: String? = null,
    val photo_url: String? = null,
    val contact_key: String? = null,
    val route_hint: String? = null,
    val pubkey: String? = null,
    val alias: String? = null,
) {

    var url: String? = null

    @JsonNames("verification_signature")
    var verificationSignature: String? = null
}