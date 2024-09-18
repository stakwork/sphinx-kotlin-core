package chat.sphinx.wrapper.mqtt

import chat.sphinx.wrapper.lightning.LightningNodePubKey
import chat.sphinx.wrapper.lightning.Sat
import chat.sphinx.wrapper.lightning.toLightningNodePubKey
import chat.sphinx.wrapper.lightning.toSat
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json

@Serializable
data class InvoiceBolt11(
    val value: Long?,
    val payment_hash: String?,
    val pubkey: String?,
    val description: String?,
    val expiry: Long?,
    val hop_hints: List<String>?
) {
    companion object {
        @Throws(Exception::class, IllegalArgumentException::class)
        fun String.toInvoiceBolt11(): InvoiceBolt11 {
            return Json.decodeFromString(this) ?: throw IllegalArgumentException("Invalid JSON for InvoiceBolt11")
        }
    }

    fun isExpired(currentTime: Long): Boolean {
        return expiry?.let { it < currentTime } == true
    }

    fun getMilliSatsAmount(): Sat? {
        value?.let { amount ->
            return amount.toSat()
        }
        return null
    }

    fun getSatsAmount(): Sat? {
        value?.let { amount ->
            return amount.div(1000).toSat()
        }
        return null
    }

    fun getMemo(): String {
        description?.let {
            return it
        }
        return ""
    }

    fun getExpiryTime(): Long? {
        expiry?.let {
            return it
        }
        return null
    }

    fun getPubKey(): LightningNodePubKey? {
        pubkey?.toLightningNodePubKey()?.let { nnPubKey ->
            return nnPubKey
        }
        return null
    }

    fun retrieveLspPubKey(): String? {
        return hop_hints?.getOrNull(0)?.substringBefore('_')
    }
}
