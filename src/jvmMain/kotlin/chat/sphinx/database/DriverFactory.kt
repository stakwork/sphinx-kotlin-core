package chat.sphinx.database

import chat.sphinx.concepts.authentication.encryption_key.EncryptionKey
import chat.sphinx.concepts.coredb.SphinxDatabase
import com.squareup.sqldelight.db.SqlDriver
import com.squareup.sqldelight.sqlite.driver.JdbcSqliteDriver

actual class DriverFactory {

    actual fun createDriver(encryptionKey: EncryptionKey): SqlDriver {
        // TODO: use the encryptionKey...
        val driver: SqlDriver = JdbcSqliteDriver(JdbcSqliteDriver.IN_MEMORY)
        SphinxDatabase.Schema.create(driver)
        return driver
    }
}