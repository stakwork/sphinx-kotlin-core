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