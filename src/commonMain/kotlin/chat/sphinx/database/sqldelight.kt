package chat.sphinx.database

import chat.sphinx.concepts.authentication.encryption_key.EncryptionKey
import chat.sphinx.concepts.coredb.SphinxDatabase
import com.squareup.sqldelight.db.SqlDriver

expect class DriverFactory {
    fun createDriver(encryptionKey: EncryptionKey): SqlDriver
}
