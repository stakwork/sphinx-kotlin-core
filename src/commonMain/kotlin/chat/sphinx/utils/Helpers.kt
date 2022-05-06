package chat.sphinx.utils

import chat.sphinx.concepts.authentication.data.AuthenticationStorage
import chat.sphinx.concepts.coroutines.CoroutineDispatchers
import chat.sphinx.logger.SphinxLogger
import chat.sphinx.utils.build_config.BuildConfigDebug
import com.russhwolf.settings.Settings
import io.ktor.http.*
import io.matthewnelson.build_config.BuildConfigVersionCode
import io.matthewnelson.kmp.tor.manager.TorManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.serialization.StringFormat
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json
import kotlinx.serialization.serializer

fun checkFromIndexSize(fromIndex: Int, size: Int, length: Int): Int {
    return if (length or fromIndex or size >= 0 && size <= length - fromIndex) {
        fromIndex
    } else {
        throw IndexOutOfBoundsException("Range [$fromIndex, $size + $length) out of bounds for length $length")
    }
}

fun checkFromToIndex(fromIndex: Int, toIndex: Int, length: Int): Int {
    return if (fromIndex in 0..toIndex && toIndex <= length) {
        fromIndex
    } else {
        throw IndexOutOfBoundsException("Range [$fromIndex, $toIndex) out of bounds for length $length")
    }
}

inline fun String.toUrlOrNull(): Url? {
    return try {
        Url(this)
    } catch (e: URLParserException) {
        null
    }
}

fun arraycopy(src: ByteArray, srcPos: Int, dst: ByteArray, dstPos: Int, count: Int) = src.copyInto(dst, dstPos, srcPos, srcPos + count)
fun arraycopy(src: IntArray, srcPos: Int, dst: IntArray, dstPos: Int, count: Int) = src.copyInto(dst, dstPos, srcPos, srcPos + count)

expect fun createTorManager(
    applicationScope: CoroutineScope,
    authenticationStorage: AuthenticationStorage,
    buildConfigDebug: BuildConfigDebug,
    buildConfigVersionCode: BuildConfigVersionCode,
    dispatchers: CoroutineDispatchers,
    LOG: SphinxLogger,
): TorManager

expect fun createPlatformSettings(): Settings

val SphinxJson: Json = Json {
    ignoreUnknownKeys = true
}
