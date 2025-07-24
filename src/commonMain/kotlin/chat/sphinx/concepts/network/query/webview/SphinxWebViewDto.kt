package chat.sphinx.concepts.network.query.webview

import chat.sphinx.utils.SphinxJson
import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

@Serializable
data class SphinxWebViewDto(
    val application: String,
    val type: String,
    val challenge: String? = null,
    val paymentRequest: String? = null,
    val macaroon: String? = null,
    val issuer: String? = null,
    val dest: String? = null,
    val amt: Int? = null,
    val message: String? = null,
    val identifier: String? = null,
    val status: String? = null
) {
    companion object {
        const val APPLICATION_NAME = "Sphinx"

        const val TYPE_AUTHORIZE = "AUTHORIZE"
        const val TYPE_GET_LSAT = "GETLSAT"
        const val TYPE_KEYSEND = "KEYSEND"
        const val TYPE_SET_BUDGET = "SETBUDGET"
        const val TYPE_LSAT = "LSAT"
        const val TYPE_UPDATE_LSAT = "UPDATELSAT"
        const val TYPE_GET_BUDGET = "GETBUDGET"
        const val TYPE_PAYMENT = "PAYMENT"
        const val TYPE_SIGN = "SIGN"
        const val TYPE_GET_PERSON_DATA = "GETPERSONDATA"
        const val TYPE_GET_SECOND_BRAIN_LIST = "GETSECONDBRAINLIST"
    }
}

@Suppress("NOTHING_TO_INLINE")
inline fun String.toSphinxWebViewDtoOrNull(): SphinxWebViewDto? =
    try {
        this.toSphinxWebViewDto()
    } catch (e: Exception) {
        null
    }

fun String.toSphinxWebViewDto(): SphinxWebViewDto =
    SphinxJson.decodeFromString<SphinxWebViewDto>(this)

@Throws(AssertionError::class)
fun SphinxWebViewDto.toJson(): String =
    Json.encodeToString(this)