package chat.sphinx.database

import chat.sphinx.concepts.authentication.encryption_key.EncryptionKey
import chat.sphinx.features.coredb.CoreDBImpl
import chat.sphinx.utils.build_config.BuildConfigDebug
import com.squareup.sqldelight.db.SqlDriver
import kotlinx.atomicfu.locks.SynchronizedObject
import kotlinx.atomicfu.locks.synchronized

class SphinxCoreDBImpl(
    private val driverFactory: DriverFactory,
    private val buildConfigDebug: BuildConfigDebug,
): CoreDBImpl() {
    private val lock = SynchronizedObject()

    @Volatile
    private var driver: SqlDriver? = null

    override fun getSqlDriver(encryptionKey: EncryptionKey): SqlDriver {
        return synchronized(lock) {
            driver ?: driverFactory.createDriver(encryptionKey)
                .also { driver = it }
        }
    }
}
