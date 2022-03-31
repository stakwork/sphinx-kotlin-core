package chat.sphinx.database

import chat.sphinx.concepts.authentication.encryption_key.EncryptionKey
import com.squareup.sqldelight.db.SqlDriver

expect class DriverFactory {
    fun createDriver(encryptionKey: EncryptionKey): SqlDriver
}
