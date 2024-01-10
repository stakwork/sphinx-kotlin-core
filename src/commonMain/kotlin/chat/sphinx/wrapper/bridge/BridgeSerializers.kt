package chat.sphinx.wrapper.bridge

import chat.sphinx.utils.SphinxJson
import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

@Serializable
data class BridgeAuthorizeMessage(
    val type: String,
    val application: String,
)

@Suppress("NOTHING_TO_INLINE")
inline fun String.toBridgeAuthorizeMessageOrNull(): BridgeAuthorizeMessage? =
    try {
        this.toBridgeAuthorizeMessage()
    } catch (e: Exception) {
        null
    }

fun String.toBridgeAuthorizeMessage(): BridgeAuthorizeMessage =
    SphinxJson.decodeFromString<BridgeAuthorizeMessage>(this).let {
        BridgeAuthorizeMessage(
            it.type,
            it.application
        )
    }

@Throws(AssertionError::class)
fun BridgeAuthorizeMessage.toJson(): String =
    Json.encodeToString(
        BridgeAuthorizeMessage(
            type,
            application
        )
    )

@Serializable
data class BridgeSetBudgetMessage(
    val type: String,
    val application: String
)

@Suppress("NOTHING_TO_INLINE")
inline fun String.toBridgeSetBudgetMessageOrNull(): BridgeSetBudgetMessage? =
    try {
        this.toBridgeSetBudgetMessage()
    } catch (e: Exception) {
        null
    }

fun String.toBridgeSetBudgetMessage(): BridgeSetBudgetMessage =
    SphinxJson.decodeFromString<BridgeSetBudgetMessage>(this).let {
        BridgeSetBudgetMessage(
            it.type,
            it.application
        )
    }

@Throws(AssertionError::class)
fun BridgeSetBudgetMessage.toJson(): String =
    Json.encodeToString(
        BridgeSetBudgetMessage(
            type,
            application
        )
    )

@Serializable
data class BridgeGetLSATMessage(
    val type: String,
    val application: String,
    val issuer: String?
)

@Suppress("NOTHING_TO_INLINE")
inline fun String.toBridgeGetLSATMessageOrNull(): BridgeGetLSATMessage? =
    try {
        this.toBridgeGetLSATMessage()
    } catch (e: Exception) {
        null
    }

fun String.toBridgeGetLSATMessage(): BridgeGetLSATMessage =
    SphinxJson.decodeFromString<BridgeGetLSATMessage>(this).let {
        BridgeGetLSATMessage(
            it.type,
            it.application,
            it.issuer
        )
    }

@Throws(AssertionError::class)
fun BridgeGetLSATMessage.toJson(): String =
    Json.encodeToString(
        BridgeGetLSATMessage(
            type,
            application,
            issuer
        )
    )

@Serializable
data class BridgeSignMessage(
    val type: String,
    val application: String,
    val message: String
)

@Suppress("NOTHING_TO_INLINE")
inline fun String.toBridgeSignMessageOrNull(): BridgeSignMessage? =
    try {
        this.toBridgeSignMessage()
    } catch (e: Exception) {
        null
    }

fun String.toBridgeSignMessage(): BridgeSignMessage =
    SphinxJson.decodeFromString<BridgeSignMessage>(this).let {
        BridgeSignMessage(
            it.type,
            it.application,
            it.message
        )
    }

@Throws(AssertionError::class)
fun BridgeSignMessage.toJson(): String =
    Json.encodeToString(
        BridgeSignMessage(
            type,
            application,
            message
        )
    )

@Serializable
data class BridgeMessage(
    val pubkey: String,
    val type: String,
    val application: String,
    val password: String,
    val budget: Int?,
    val signature: String?
)

@Serializable
data class SendAuthMessage(
    val pubkey: String,
    val type: String,
    val application: String,
    val password: String,
)

@Serializable
data class SendAuthMessageWithSignature(
    val pubkey: String,
    val type: String,
    val application: String,
    val password: String,
    val signature: String?
)

@Serializable
data class SetBudgetMessage(
    val pubkey: String,
    val type: String,
    val application: String,
    val password: String,
    val budget: Int?,
)

@Serializable
data class LSatMessage(
    val type: String,
    val application: String,
    val password: String,
    val macaroon: String,
    val paymentRequest: String,
    val preimage: String,
    val identifier: String,
    val issuer: String,
    val success: Boolean,
    val status: Long,
    val paths: String,
)

@Serializable
data class LSatFailedMessage(
    val type: String,
    val application: String,
    val password: String,
    val success: Boolean
)

@Throws(AssertionError::class)
fun LSatMessage.toJson(): String =
    Json.encodeToString(
        LSatMessage(
            type,
            application,
            password,
            macaroon,
            paymentRequest,
            preimage,
            identifier,
            issuer,
            success,
            status,
            paths
        )
    )

@Throws(AssertionError::class)
fun LSatFailedMessage.toJson(): String =
    Json.encodeToString(
        LSatFailedMessage(
            type,
            application,
            password,
            success
        )
    )

@Serializable
data class SendSignMessage(
    val type: String,
    val application: String,
    val signature: String,
    val success: Boolean
)

@Serializable
data class SendFailedSignMessage(
    val type: String,
    val application: String,
    val success: Boolean
)

@Throws(AssertionError::class)
fun SendSignMessage.toJson(): String =
    Json.encodeToString(
        SendSignMessage(
            type,
            application,
            signature,
            success
        )
    )

@Throws(AssertionError::class)
fun SendFailedSignMessage.toJson(): String =
    Json.encodeToString(
        SendFailedSignMessage(
            type,
            application,
            success
        )
    )

@Throws(AssertionError::class)
fun BridgeMessage.toJson(): String {
    signature?.let {
        return Json.encodeToString(
            SendAuthMessageWithSignature(
                pubkey,
                type,
                application,
                password,
                it
            )
        )
    }

    budget?.let {
        return Json.encodeToString(
            SetBudgetMessage(
                pubkey,
                type,
                application,
                password,
                it
            )
        )
    }

    return Json.encodeToString(
        SendAuthMessage(
            pubkey,
            type,
            application,
            password
        )
    )
}