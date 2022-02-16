package chat.sphinx.concepts.network.query.verify_external.model

data class VerifyExternalDto(
    val token: String,
    val info: VerifyExternalInfoDto,
)