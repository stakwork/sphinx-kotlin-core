package chat.sphinx.database

import chat.sphinx.concepts.authentication.encryption_key.EncryptionKey
import chat.sphinx.database.core.SphinxDatabase
import com.squareup.sqldelight.db.SqlDriver
import com.squareup.sqldelight.sqlite.driver.JdbcSqliteDriver

actual class DriverFactory {

    private fun getVersion(driver: SqlDriver): Int {
        val sqlCursor = driver.executeQuery(null, "PRAGMA user_version;", 0, null)
        return sqlCursor.getLong(0)?.toInt() ?: 0
    }

    private fun setVersion(version: Int, driver: SqlDriver) {
        driver.execute(null, String.format("PRAGMA user_version = %d;", version), 0, null)
    }

    actual fun createDriver(encryptionKey: EncryptionKey): SqlDriver {
        val driver: SqlDriver = JdbcSqliteDriver("jdbc:sqlite:dev.db")
        val currentVersion = getVersion(driver)
        if (currentVersion == 0) {
            SphinxDatabase.Schema.create(driver)
            setVersion(SphinxDatabase.Schema.version, driver)
        } else {
            val schemeVersion = SphinxDatabase.Schema.version
            if (schemeVersion > currentVersion) {
                SphinxDatabase.Schema.migrate(driver, currentVersion, schemeVersion)
                setVersion(schemeVersion, driver)
            }
        }
        return driver
    }
}