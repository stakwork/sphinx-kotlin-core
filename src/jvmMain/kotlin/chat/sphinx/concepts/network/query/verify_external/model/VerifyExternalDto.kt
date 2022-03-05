package chat.sphinx.concepts.network.query.verify_external.model

import kotlinx.serialization.Serializable

@Serializable
data class VerifyExternalDto(
    val token: String,
    val info: VerifyExternalInfoDto,
)