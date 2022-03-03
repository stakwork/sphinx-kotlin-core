import chat.sphinx.concepts.coredb.SphinxDatabase
import com.squareup.sqldelight.db.SqlDriver

expect class DriverFactory {
    fun createDriver(): SqlDriver
}
