import chat.sphinx.concepts.coredb.SphinxDatabase
import com.squareup.sqldelight.db.SqlDriver
import com.squareup.sqldelight.drivers.native.NativeSqliteDriver

actual class DriverFactory {
    actual fun createDriver(): SqlDriver {
        return NativeSqliteDriver(SphinxDatabase.Schema, "sphinx.db")
    }
}