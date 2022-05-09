package chat.sphinx.concepts.coredb

import chat.sphinx.database.core.SphinxDatabaseQueries

abstract class CoreDB {

    abstract val isInitialized: Boolean

    abstract fun getSphinxDatabaseQueriesOrNull(): SphinxDatabaseQueries?

    /**
     * Suspends until [SphinxDatabaseQueries] is initialized. Initialization occurs
     * after decryption and retrieval of the user's encryption key, which allows
     * for the decrypting of the DB.
     * */
    abstract suspend fun getSphinxDatabaseQueries(): SphinxDatabaseQueries
}
