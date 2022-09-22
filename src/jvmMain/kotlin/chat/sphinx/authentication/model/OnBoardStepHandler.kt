package chat.sphinx.authentication.model

import chat.sphinx.di.container.SphinxContainer
import chat.sphinx.logger.e
import chat.sphinx.wrapper.relay.AuthorizationToken
import chat.sphinx.wrapper.relay.RelayHMacKey
import chat.sphinx.wrapper.relay.RelayUrl
import chat.sphinx.wrapper.rsa.RsaPublicKey
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext
import java.io.IOException

class OnBoardStepHandler {
    private val authenticationStorage = SphinxContainer.authenticationModule.authenticationStorage
    private val LOG = SphinxContainer.appModule.sphinxLogger
    val scope = SphinxContainer.appModule.applicationScope
    val dispatchers = SphinxContainer.appModule.dispatchers

    companion object {
        private val lock = Mutex()

        private const val TAG = "OnBoardStepHandler"

        private const val KEY = "ON_BOARD_STEP"

        private const val STEP_1 = "STEP_1"
        private const val STEP_2 = "STEP_2"
        private const val STEP_3 = "STEP_3"
        private const val STEP_4 = "STEP_4"

        // Character lengths must stay the same for
        // onboard step retrieval to function properly
        private const val STEP_SIZE: Int = STEP_1.length
    }

    suspend fun persistOnBoardStep1Data(
        relayUrl: RelayUrl,
        authorizationToken: AuthorizationToken,
        transportKey: RsaPublicKey?,
        hMacKey: RelayHMacKey?,
        inviterData: OnBoardInviterData?
    ): OnBoardStep.Step1_WelcomeMessage? {
        lock.withLock {
            val inviterDataRealized: OnBoardInviterData = inviterData ?: getDefaultInviterData()

            val step1 = OnBoardStep.Step1_WelcomeMessage(
                relayUrl,
                authorizationToken,
                transportKey,
                hMacKey,
                inviterDataRealized
            )

            val step1Json: String = try {
                withContext(dispatchers.default) {
                    step1.toStep1Json().toJsonString()
                }
            } catch (e: Exception) {
                LOG.e(TAG, "Step1 Json Conversion Error", e)
                return null
            }

            authenticationStorage.putString(KEY, STEP_1 + step1Json)

            return step1
        }
    }

    suspend fun persistOnBoardStep2Data(inviterData: OnBoardInviterData?): OnBoardStep.Step2_Name? {
        lock.withLock {

            val inviterDataRealized: OnBoardInviterData = inviterData ?: getDefaultInviterData()

            val step2 = OnBoardStep.Step2_Name(inviterDataRealized)
            val step2Json: String = try {
                withContext(dispatchers.default) {
                    Step2Json(inviterDataRealized.toInviteDataJson()).toJsonString()
                }
            } catch (e: Exception) {
                LOG.e(TAG, "Step2 Json Conversion Error", e)
                return null
            }

            authenticationStorage.putString(KEY, STEP_2 + step2Json)

            return step2
        }
    }

    suspend fun persistOnBoardStep3Data(inviterData: OnBoardInviterData?): OnBoardStep.Step3_Picture? {
        lock.withLock {

            val inviterDataRealized: OnBoardInviterData = inviterData ?: getDefaultInviterData()
            val step3 = OnBoardStep.Step3_Picture(inviterDataRealized)
            val step3Json: String = try {
                withContext(dispatchers.default) {
                    Step3Json(inviterDataRealized.toInviteDataJson()).toJsonString()
                }
            } catch (e: Exception) {
                LOG.e(TAG, "Step3 Json Conversion Error", e)
                return null
            }

            authenticationStorage.putString(KEY, STEP_3 + step3Json)

            return step3
        }
    }

    suspend fun persistOnBoardStep4Data(inviterData: OnBoardInviterData?): OnBoardStep.Step4_Ready? {
        lock.withLock {

            val inviterDataRealized: OnBoardInviterData = inviterData ?: getDefaultInviterData()
            val step4 = OnBoardStep.Step4_Ready(inviterDataRealized)
            val step4Json: String = try {
                withContext(dispatchers.default) {
                    Step4Json(inviterDataRealized.toInviteDataJson()).toJsonString()
                }
            } catch (e: Exception) {
                LOG.e(TAG, "Step4 Json Conversion Error", e)
                return null
            }

            authenticationStorage.putString(KEY, STEP_4 + step4Json)

            return step4
        }
    }

    private fun getDefaultInviterData(): OnBoardInviterData {
        return OnBoardInviterData(
            nickname = "Sphinx Support",
            pubkey = null,
            routeHint = null,
            message = "Welcome to Sphinx",
            action = null,
            pin = null,
        )
    }

    suspend fun finishOnBoardSteps() {
        lock.withLock {
            authenticationStorage.removeString(KEY)
        }
    }

    suspend fun isSignupInProgress(): Boolean {
        return retrieveOnBoardStep() != null
    }

    @Suppress("BlockingMethodInNonBlockingContext")
    suspend fun retrieveOnBoardStep(): OnBoardStep? =
        lock.withLock {
            authenticationStorage.getString(KEY, null)?.let { stepString ->
                try {
                    when (stepString.take(STEP_SIZE)) {
                        STEP_1 -> {
                            withContext(dispatchers.default) {
                                stepString.drop(STEP_SIZE).toStep1Json().toOnboardStep1()
                            }
                        }
                        STEP_2 -> {
                            withContext(dispatchers.default) {
                                val inviterData = stepString.drop(STEP_SIZE).toStep2Json().invite_data_json.toOnBoardInviteData()
                                inviterData.let {
                                    OnBoardStep.Step2_Name(inviterData)
                                }
                            }
                        }
                        STEP_3 -> {
                            withContext(dispatchers.default) {
                                val inviterData = stepString.drop(STEP_SIZE).toStep3Json().invite_data_json.toOnBoardInviteData()
                                inviterData.let {
                                    OnBoardStep.Step3_Picture(inviterData)
                                }
                            }
                        }
                        STEP_4 -> {
                            withContext(dispatchers.default) {
                                val inviterData = stepString.drop(STEP_SIZE).toStep4Json().invite_data_json.toOnBoardInviteData()
                                inviterData.let {
                                    OnBoardStep.Step4_Ready(inviterData)
                                }
                            }
                        }
                        else -> {
                            null
                        }
                    }
                } catch (e: Exception) {
                    LOG.e(TAG, "Failed to retrieve and convert OnBoardStep data", e)
                    null
                }
            }
        }
}

