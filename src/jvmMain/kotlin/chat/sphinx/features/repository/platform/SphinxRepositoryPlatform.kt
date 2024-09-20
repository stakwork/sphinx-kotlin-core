package chat.sphinx.features.repository.platform

import chat.sphinx.concepts.authentication.data.AuthenticationStorage
import chat.sphinx.concepts.connect_manager.ConnectManager
import chat.sphinx.concepts.coredb.CoreDB
import chat.sphinx.concepts.coroutines.CoroutineDispatchers
import chat.sphinx.concepts.crypto_rsa.RSA
import chat.sphinx.concepts.media_cache.MediaCacheHandler
import chat.sphinx.concepts.meme_input_stream.MemeInputStreamHandler
import chat.sphinx.concepts.meme_server.MemeServerTokenHandler
import chat.sphinx.concepts.network.query.chat.NetworkQueryChat
import chat.sphinx.concepts.network.query.contact.NetworkQueryContact
import chat.sphinx.concepts.network.query.feed_search.NetworkQueryFeedSearch
import chat.sphinx.concepts.network.query.invite.NetworkQueryInvite
import chat.sphinx.concepts.network.query.lightning.NetworkQueryLightning
import chat.sphinx.concepts.network.query.meme_server.NetworkQueryMemeServer
import chat.sphinx.concepts.network.query.message.NetworkQueryMessage
import chat.sphinx.concepts.network.query.redeem_badge_token.NetworkQueryRedeemBadgeToken
import chat.sphinx.concepts.network.query.relay_keys.NetworkQueryRelayKeys
import chat.sphinx.concepts.network.query.save_profile.NetworkQuerySaveProfile
import chat.sphinx.concepts.network.query.subscription.NetworkQuerySubscription
import chat.sphinx.concepts.network.query.verify_external.NetworkQueryAuthorizeExternal
import chat.sphinx.concepts.notification.SphinxNotificationManager
import chat.sphinx.concepts.relay.RelayDataHandler
import chat.sphinx.features.authentication.core.AuthenticationCoreManager
import chat.sphinx.features.repository.SphinxRepository
import chat.sphinx.logger.SphinxLogger
import chat.sphinx.wrapper.contact.Contact
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.StateFlow

class SphinxRepositoryPlatform(
    accountOwner: StateFlow<Contact?>,
    applicationScope: CoroutineScope,
    authenticationCoreManager: AuthenticationCoreManager,
    authenticationStorage: AuthenticationStorage,
    relayDataHandler: RelayDataHandler,
    coreDB: CoreDB,
    dispatchers: CoroutineDispatchers,
    mediaCacheHandler: MediaCacheHandler,
    memeInputStreamHandler: MemeInputStreamHandler,
    memeServerTokenHandler: MemeServerTokenHandler,
    networkQueryMemeServer: NetworkQueryMemeServer,
    networkQueryChat: NetworkQueryChat,
    networkQueryContact: NetworkQueryContact,
    networkQueryLightning: NetworkQueryLightning,
    networkQueryMessage: NetworkQueryMessage,
    networkQueryInvite: NetworkQueryInvite,
    networkQueryAuthorizeExternal: NetworkQueryAuthorizeExternal,
    networkQuerySaveProfile: NetworkQuerySaveProfile,
    networkQueryRedeemBadgeToken: NetworkQueryRedeemBadgeToken,
    networkQuerySubscription: NetworkQuerySubscription,
    networkQueryFeedSearch: NetworkQueryFeedSearch,
    networkQueryRelayKeys: NetworkQueryRelayKeys,
    connectManager: ConnectManager,
    rsa: RSA,
    sphinxNotificationManager: SphinxNotificationManager,
    LOG: SphinxLogger,
): SphinxRepository(
    accountOwner,
    applicationScope,
    authenticationCoreManager,
    authenticationStorage,
    relayDataHandler,
    coreDB,
    dispatchers,
    mediaCacheHandler,
    memeInputStreamHandler,
    memeServerTokenHandler,
    networkQueryMemeServer,
    networkQueryChat,
    networkQueryContact,
    networkQueryLightning,
    networkQueryMessage,
    networkQueryInvite,
    networkQueryAuthorizeExternal,
    networkQuerySaveProfile,
    networkQueryRedeemBadgeToken,
    networkQuerySubscription,
    networkQueryFeedSearch,
    networkQueryRelayKeys,
    connectManager,
    rsa,
    sphinxNotificationManager,
    LOG,
)
{
    companion object {
        const val PAGING_DASHBOARD_PAGE_SIZE = 30
        const val PAGING_DASHBOARD_PREFETCH_DISTANCE = PAGING_DASHBOARD_PAGE_SIZE / 2
        const val PAGING_DASHBOARD_INITIAL_LOAD_SIZE = PAGING_DASHBOARD_PREFETCH_DISTANCE
        const val PAGING_DASHBOARD_MAX_SIZE =
            (PAGING_DASHBOARD_PREFETCH_DISTANCE * 2) + PAGING_DASHBOARD_PAGE_SIZE
    }
}
