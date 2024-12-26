package chat.sphinx.wrapper.user

@JvmInline
value class UserState(val value: String) {
    init {
        require(value.isNotEmpty()) {
            "UserState cannot be empty"
        }
    }
}