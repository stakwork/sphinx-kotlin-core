package chat.sphinx.concepts.network.query.save_profile.model

import kotlinx.serialization.Serializable

@Serializable
data class TribeMemberProfileDto(
    val id: Int,
    val description: String,
    val img: String,
    val owner_alias: String,
    val owner_contact_key: String,
    val owner_route_hint: String,
    val price_to_meet: Int,
    val unique_name: String,
    val uuid: String,
    val extras: TribeMemberProfileExtrasDto?,
)

@Serializable
data class TribeMemberProfileExtrasDto(
    val coding_languages: List<TribeMemberProfileCodingLanguageDto>? = null,
    val github: List<ProfileAttributeDto>? = null,
    val twitter: List<ProfileAttributeDto>? = null,
    val post: List<TribeMemberProfilePostDto>? = null,
    val tribes: List<ProfileAttributeDto>? = null,
) {
    val codingLanguages: String
        get() {
            coding_languages?.let { nnCodingLanguages ->
                if (nnCodingLanguages.isNotEmpty()) {
                    return nnCodingLanguages.joinToString(",") {
                        it.value
                    }
                }
            }
            return "-"
        }
}

@Serializable
data class TribeMemberProfileCodingLanguageDto(
    val label: String,
    val value: String
)

@Serializable
data class TribeMemberProfilePostDto(
    val content: String,
    val title: String,
    val created: Int
)

@Serializable
data class ProfileAttributeDto(
    val value: String
) {
    val formattedValue: String
        get() {
            if (value.isEmpty()) {
                return "-"
            }
            return value
        }
}
