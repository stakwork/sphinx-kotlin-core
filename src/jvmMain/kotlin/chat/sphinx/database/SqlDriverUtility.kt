package chat.sphinx.database

import chat.sphinx.concepts.authentication.encryption_key.EncryptionKey
import chat.sphinx.concepts.coredb.CoreDB
import chat.sphinx.database.core.SphinxDatabase
import chat.sphinx.utils.platform.getFileSystem
import chat.sphinx.utils.platform.getSphinxDirectory
import chat.sphinx.utils.platform.getUserHomeDirectory
import com.squareup.sqldelight.db.SqlDriver
import com.squareup.sqldelight.sqlite.driver.JdbcSqliteDriver
import okio.FileSystem
import okio.Path
import okio.Path.Companion.toPath


actual class SqlDriverUtility {
    val sphinxDatabaseFilepath = getSphinxDirectory().resolve(CoreDB.DB_NAME)

    private fun getVersion(driver: SqlDriver): Int {
        val sqlCursor = driver.executeQuery(null, "PRAGMA user_version;", 0, null)
        return sqlCursor.getLong(0)?.toInt() ?: 0
    }

    private fun setVersion(version: Int, driver: SqlDriver) {
        driver.execute(null, "PRAGMA user_version = $version;", 0, null)
    }

    private fun handleMigration(driver: SqlDriver) {
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
    }

    private fun getJournalMode(driver: SqlDriver): String {
        val sqlCursor = driver.executeQuery(null, "PRAGMA journal_mode;", 0, null)
        return sqlCursor.getString(0) ?: ""
    }

    private fun setJournalMode(journalMode: String, driver: SqlDriver) {
        driver.execute(null, "PRAGMA journal_mode = $journalMode;", 0, null)
    }

    /**
     * Sphinx app is multi-threaded. This journal mode should allow us to run the app without the SQLITE_BUSY exception
     */
    val SPHINX_JOURNAL_MODE = "WAL" // Journal Mode to handle multi threading

    private fun handleJournalMode(driver: SqlDriver) {
        setJournalMode(SPHINX_JOURNAL_MODE, driver)
        // Would've liked to check journal mode before setting it but that seems to not work...
//        val currentJournalMode = getJournalMode(driver)
//
//        if (currentJournalMode != SPHINX_JOURNAL_MODE) {
//            driver.close()
//            setJournalMode(SPHINX_JOURNAL_MODE, driver)
//        }
    }

    actual fun createDriver(encryptionKey: EncryptionKey): SqlDriver {
        val driver = JdbcSqliteDriver("jdbc:sqlite:$sphinxDatabaseFilepath")

        handleJournalMode(driver)
        handleMigration(driver)

        return driver
    }

    actual fun deleteDatabase() {
        getFileSystem().delete(sphinxDatabaseFilepath)
    }
}