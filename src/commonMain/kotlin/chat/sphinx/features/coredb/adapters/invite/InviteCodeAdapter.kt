package chat.sphinx.features.coredb.adapters.invite

import chat.sphinx.wrapper.invite.InviteCode
import com.squareup.sqldelight.ColumnAdapter

internal class InviteCodeAdapter: ColumnAdapter<InviteCode, String> {
    override fun decode(databaseValue: String): InviteCode {
        return InviteCode(databaseValue)
    }

    override fun encode(value: InviteCode): String {
        return value.value
    }
}
