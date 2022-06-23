package chat.sphinx.serialization

@kotlinx.serialization.Serializable(with = SphinxBooleanSerializable::class)
data class SphinxBoolean(
    val value: Boolean
) {

    constructor(number: Int) : this(number == 1) {
        // TODO: Verify number range...
    }

    override fun toString(): String {
        return value.toString()
    }

}