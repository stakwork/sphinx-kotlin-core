package chat.sphinx.concepts.network.query.lightning.model.invoice

import kotlinx.serialization.Serializable

@Serializable
data class FeatureDto(
    val name: String,
    val is_required: Boolean,
    val is_known: Boolean,
)
