package chat.sphinx.wrapper.bridge

import chat.sphinx.utils.SphinxJson
import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

///BRIDGE AUTHORIZE MESSAGE
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

///BRIDGE SET BUDGET MESSAGE
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

///BRIDGE GET LSAT MESSAGE
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

///BRIDGE SIGN MESSAGE
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

///BRIDGE KEYSEND MESSAGE
@Serializable
data class BridgeKeysendMessage(
    val type: String,
    val application: String,
    val dest: String,
    val amt: Int
)

@Suppress("NOTHING_TO_INLINE")
inline fun String.toBridgeKeysendMessageOrNull(): BridgeKeysendMessage? =
    try {
        this.toBridgeKeysendMessage()
    } catch (e: Exception) {
        null
    }

fun String.toBridgeKeysendMessage(): BridgeKeysendMessage =
    SphinxJson.decodeFromString<BridgeKeysendMessage>(this).let {
        BridgeKeysendMessage(
            it.type,
            it.application,
            it.dest,
            it.amt
        )
    }

@Throws(AssertionError::class)
fun BridgeKeysendMessage.toJson(): String =
    Json.encodeToString(
        BridgeKeysendMessage(
            type,
            application,
            dest,
            amt
        )
    )

///BRIDGE GETBUDGET MESSAGE
@Serializable
data class BridgeGetBudgetMessage(
    val type: String,
    val application: String
)

@Suppress("NOTHING_TO_INLINE")
inline fun String.toBridgeGetBudgetMessageOrNull(): BridgeGetBudgetMessage? =
    try {
        this.toBridgeGetBudgetMessage()
    } catch (e: Exception) {
        null
    }

fun String.toBridgeGetBudgetMessage(): BridgeGetBudgetMessage =
    SphinxJson.decodeFromString<BridgeGetBudgetMessage>(this).let {
        BridgeGetBudgetMessage(
            it.type,
            it.application
        )
    }

@Throws(AssertionError::class)
fun BridgeGetBudgetMessage.toJson(): String =
    Json.encodeToString(
        BridgeGetBudgetMessage(
            type,
            application
        )
    )

///BRIDGE LSAT MESSAGE
@Serializable
data class BridgeLSatMessage(
    val type: String,
    val application: String,
    val paymentRequest: String,
    val macaroon: String,
    val issuer: String
)

@Suppress("NOTHING_TO_INLINE")
inline fun String.toBridgeLSatMessageOrNull(): BridgeLSatMessage? =
    try {
        this.toBridgeLSatMessage()
    } catch (e: Exception) {
        null
    }

fun String.toBridgeLSatMessage(): BridgeLSatMessage =
    SphinxJson.decodeFromString<BridgeLSatMessage>(this).let {
        BridgeLSatMessage(
            it.type,
            it.application,
            it.paymentRequest,
            it.macaroon,
            it.issuer
        )
    }

@Throws(AssertionError::class)
fun BridgeLSatMessage.toJson(): String =
    Json.encodeToString(
        BridgeLSatMessage(
            type,
            application,
            paymentRequest,
            macaroon,
            issuer
        )
    )

///BRIDGE UPDATE LSAT MESSAGE
@Serializable
data class BridgeUpdateLSatMessage(
    val type: String,
    val application: String,
    val identifier: String,
    val status: String,
)

@Suppress("NOTHING_TO_INLINE")
inline fun String.toBridgeUpdateLSatMessageOrNull(): BridgeUpdateLSatMessage? =
    try {
        this.toBridgeUpdateLSatMessage()
    } catch (e: Exception) {
        null
    }

fun String.toBridgeUpdateLSatMessage(): BridgeUpdateLSatMessage =
    SphinxJson.decodeFromString<BridgeUpdateLSatMessage>(this).let {
        BridgeUpdateLSatMessage(
            it.type,
            it.application,
            it.identifier,
            it.status
        )
    }

@Throws(AssertionError::class)
fun BridgeUpdateLSatMessage.toJson(): String =
    Json.encodeToString(
        BridgeUpdateLSatMessage(
            type,
            application,
            identifier,
            status
        )
    )

///SEND MESSAGES
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
data class SendSetBudgetMessage(
    val pubkey: String,
    val type: String,
    val application: String,
    val password: String,
    val budget: Int?,
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
            SendSetBudgetMessage(
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

@Serializable
data class SendActiveLSatMessage(
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
data class SendActiveLSatFailedMessage(
    val type: String,
    val application: String,
    val password: String,
    val success: Boolean
)

@Throws(AssertionError::class)
fun SendActiveLSatMessage.toJson(): String =
    Json.encodeToString(
        SendActiveLSatMessage(
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
fun SendActiveLSatFailedMessage.toJson(): String =
    Json.encodeToString(
        SendActiveLSatFailedMessage(
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

@Serializable
data class SendKeysendMessage(
    val type: String,
    val application: String,
    val success: Boolean
)

@Throws(AssertionError::class)
fun SendKeysendMessage.toJson(): String =
    Json.encodeToString(
        SendKeysendMessage(
            type,
            application,
            success
        )
    )

@Serializable
data class SendGetBudgetMessage(
    val type: String,
    val application: String,
    val budget: Int,
    val success: Boolean
)

@Throws(AssertionError::class)
fun SendGetBudgetMessage.toJson(): String =
    Json.encodeToString(
        SendGetBudgetMessage(
            type,
            application,
            budget,
            success
        )
    )

@Serializable
data class SendLSatMessage(
    val type: String,
    val application: String,
    val paymentRequest: String,
    val macaroon: String,
    val issuer: String,
    val lsat: String,
    val budget: Int?,
    val success: Boolean
)

@Throws(AssertionError::class)
fun SendLSatMessage.toJson(): String =
    Json.encodeToString(
        SendLSatMessage(
            type,
            application,
            lsat,
            paymentRequest,
            macaroon,
            issuer,
            budget,
            success
        )
    )

@Serializable
data class SendLSatFailedMessage(
    val type: String,
    val application: String,
    val paymentRequest: String,
    val macaroon: String,
    val issuer: String,
    val success: Boolean
)

@Throws(AssertionError::class)
fun SendLSatFailedMessage.toJson(): String =
    Json.encodeToString(
        SendLSatFailedMessage(
            type,
            application,
            paymentRequest,
            macaroon,
            issuer,
            success
        )
    )

@Serializable
data class SendUpdateLSatMessage(
    val type: String,
    val application: String,
    val identifier: String,
    val status: String,
    val lsat: String,
    val success: Boolean
)

@Throws(AssertionError::class)
fun SendUpdateLSatMessage.toJson(): String =
    Json.encodeToString(
        SendUpdateLSatMessage(
            type,
            application,
            identifier,
            status,
            lsat,
            success
        )
    )

@Serializable
data class SendUpdateLSatFailedMessage(
    val type: String,
    val application: String,
    val identifier: String,
    val status: String,
    val success: Boolean
)

@Throws(AssertionError::class)
fun SendUpdateLSatFailedMessage.toJson(): String =
    Json.encodeToString(
        SendUpdateLSatMessage(
            type,
            application,
            identifier,
            status,
            success
        )
    )