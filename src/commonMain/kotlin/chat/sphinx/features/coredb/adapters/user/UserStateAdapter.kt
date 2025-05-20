package chat.sphinx.features.coredb.adapters.user

import chat.sphinx.wrapper.user.UserState
import com.squareup.sqldelight.ColumnAdapter

internal class UserStateAdapter : ColumnAdapter<UserState, String> {
    override fun decode(databaseValue: String): UserState {
        return UserState(databaseValue)
    }

    override fun encode(value: UserState): String {
        return value.value
    }
}