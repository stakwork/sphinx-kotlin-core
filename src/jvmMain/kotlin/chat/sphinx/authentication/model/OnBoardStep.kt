package chat.sphinx.authentication.model

import chat.sphinx.wrapper.relay.AuthorizationToken
import chat.sphinx.wrapper.relay.RelayHMacKey
import chat.sphinx.wrapper.relay.RelayUrl
import chat.sphinx.wrapper.rsa.RsaPublicKey


@Suppress("DataClassPrivateConstructor", "ClassName")
sealed class OnBoardStep(
    open val inviterData: OnBoardInviterData
) {

    data class Step1_WelcomeMessage private constructor(
        val relayUrl: RelayUrl,
        val authorizationToken: AuthorizationToken,
        val transportKey: RsaPublicKey?,
        val hMacKey: RelayHMacKey?,
        override val inviterData: OnBoardInviterData
    ): OnBoardStep(inviterData) {

        companion object {
            @JvmSynthetic
            internal operator fun invoke(
                relayUrl: RelayUrl,
                authorizationToken: AuthorizationToken,
                transportKey: RsaPublicKey?,
                hMacKey: RelayHMacKey?,
                inviterData: OnBoardInviterData
            ) : Step1_WelcomeMessage =
                Step1_WelcomeMessage(relayUrl, authorizationToken, transportKey, hMacKey, inviterData)
        }

    }

    data class Step2_Name private constructor(override val inviterData: OnBoardInviterData): OnBoardStep(inviterData) {

        companion object {
            @JvmSynthetic
            internal operator fun invoke(inviterData: OnBoardInviterData): Step2_Name =
                Step2_Name(inviterData)
        }
    }

    data class Step3_Picture private constructor(override val inviterData: OnBoardInviterData): OnBoardStep(inviterData) {

        companion object {
            @JvmSynthetic
            internal operator fun invoke(inviterData: OnBoardInviterData): Step3_Picture =
                Step3_Picture(inviterData)
        }
    }

    data class Step4_Ready private constructor(override val inviterData: OnBoardInviterData): OnBoardStep(inviterData) {

        companion object {
            @JvmSynthetic
            internal operator fun invoke(inviterData: OnBoardInviterData): Step4_Ready =
                Step4_Ready(inviterData)
        }
    }

}
