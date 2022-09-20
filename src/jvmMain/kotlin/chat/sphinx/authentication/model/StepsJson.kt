package chat.sphinx.authentication.model

import chat.sphinx.utils.SphinxJson
import chat.sphinx.wrapper.lightning.toLightningNodePubKey
import chat.sphinx.wrapper.relay.AuthorizationToken
import chat.sphinx.wrapper.relay.RelayHMacKey
import chat.sphinx.wrapper.relay.RelayUrl
import chat.sphinx.wrapper.rsa.RsaPublicKey
import kotlinx.serialization.Serializable
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

@Suppress("NOTHING_TO_INLINE")
internal inline fun OnBoardInviterData.toInviteDataJson(): Step1Json.InviteDataJson =
    Step1Json.InviteDataJson(
        nickname,
        pubkey?.value,
        routeHint,
        message,
        action,
        pin
    )

@Suppress("NOTHING_TO_INLINE")
internal inline fun Step1Json.InviteDataJson.toOnBoardInviteData(): OnBoardInviterData =
    OnBoardInviterData(
        nickname,
        pubkey?.toLightningNodePubKey(),
        route_hint,
        message,
        action,
        pin,
    )

@Suppress("NOTHING_TO_INLINE")
internal inline fun OnBoardStep.Step1_WelcomeMessage.toStep1Json(): Step1Json =
    Step1Json(
        relay_url = relayUrl.value,
        authorization_token = authorizationToken.value,
        transport_key = transportKey?.value?.joinToString("") ?: "",
        h_mac_key = hMacKey?.value ?: "",
        invite_data_json = inviterData.toInviteDataJson(),
    )

@Suppress("NOTHING_TO_INLINE")
internal inline fun Step1Json.toOnboardStep1(): OnBoardStep.Step1_WelcomeMessage =
    OnBoardStep.Step1_WelcomeMessage(
        RelayUrl(relay_url),
        AuthorizationToken(authorization_token),
        RsaPublicKey(transport_key.toCharArray()),
        RelayHMacKey(h_mac_key),
        invite_data_json.toOnBoardInviteData(),
    )

@Serializable
data class Step1Json(
    val relay_url: String,
    val authorization_token: String,
    val transport_key: String,
    val h_mac_key: String,
    val invite_data_json: InviteDataJson,
) {

    @Serializable
    class InviteDataJson(
        val nickname: String?,
        val pubkey: String?,
        val route_hint: String?,
        val message: String?,
        val action: String?,
        val pin: String?
    )
}

@Throws(AssertionError::class)
fun Step1Json.toJsonString(): String =
    Json.encodeToString(
        Step1Json(
            relay_url,
            authorization_token,
            transport_key,
            h_mac_key,
            invite_data_json
        )
    )
@Throws(AssertionError::class)
fun Step2Json.toJsonString(): String =
    Json.encodeToString(
        Step2Json(invite_data_json)
    )

@Throws(AssertionError::class)
fun Step3Json.toJsonString(): String =
    Json.encodeToString(
        Step3Json(invite_data_json)
    )
@Throws(AssertionError::class)
fun Step4Json.toJsonString(): String =
    Json.encodeToString(
        Step4Json(invite_data_json)
    )

@Serializable
data class Step2Json(val invite_data_json: Step1Json.InviteDataJson)
@Serializable
data class Step3Json(val invite_data_json: Step1Json.InviteDataJson)
@Serializable
data class Step4Json(val invite_data_json: Step1Json.InviteDataJson)

fun String.toStep1Json(): Step1Json =
    SphinxJson.decodeFromString<Step1Json>(this).let {
        Step1Json(
            relay_url = it.relay_url,
            authorization_token = it.authorization_token,
            transport_key = it.transport_key,
            h_mac_key = it.h_mac_key,
            invite_data_json = it.invite_data_json
        )
    }

fun String.toStep2Json(): Step2Json =
    SphinxJson.decodeFromString<Step2Json>(this).let {
        Step2Json(
            invite_data_json = it.invite_data_json
        )
    }

fun String.toStep3Json(): Step3Json =
    SphinxJson.decodeFromString<Step3Json>(this).let {
        Step3Json(
            invite_data_json = it.invite_data_json
        )
    }

fun String.toStep4Json(): Step4Json =
    SphinxJson.decodeFromString<Step4Json>(this).let {
        Step4Json(
            invite_data_json = it.invite_data_json
        )
    }