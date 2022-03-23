package chat.sphinx.features.coredb


import chat.sphinx.concepts.authentication.encryption_key.EncryptionKey
import chat.sphinx.concepts.coredb.*
import chat.sphinx.features.coredb.adapters.invite.InviteStringAdapter
import chat.sphinx.features.coredb.adapters.media.MediaKeyAdapter
import chat.sphinx.features.coredb.adapters.media.MediaKeyDecryptedAdapter
import chat.sphinx.features.coredb.adapters.media.MediaTokenAdapter
import chat.sphinx.features.coredb.adapters.media.MediaTypeAdapter
import chat.sphinx.features.coredb.adapters.subscription.CronAdapter
import chat.sphinx.features.coredb.adapters.subscription.EndNumberAdapter
import chat.sphinx.features.coredb.adapters.subscription.SubscriptionCountAdapter
import chat.sphinx.features.coredb.adapters.chat.ChatAliasAdapter
import chat.sphinx.features.coredb.adapters.chat.ChatGroupKeyAdapter
import chat.sphinx.features.coredb.adapters.chat.ChatHostAdapter
import chat.sphinx.features.coredb.adapters.chat.ChatMetaDataAdapter
import chat.sphinx.features.coredb.adapters.chat.ChatMutedAdapter
import chat.sphinx.features.coredb.adapters.chat.ChatNameAdapter
import chat.sphinx.features.coredb.adapters.chat.ChatPrivateAdapter
import chat.sphinx.features.coredb.adapters.chat.ChatStatusAdapter
import chat.sphinx.features.coredb.adapters.chat.ChatTypeAdapter
import chat.sphinx.features.coredb.adapters.chat.ChatUUIDAdapter
import chat.sphinx.features.coredb.adapters.chat.ChatUnlistedAdapter
import chat.sphinx.features.coredb.adapters.common.*
import chat.sphinx.features.coredb.adapters.common.ChatIdAdapter
import chat.sphinx.features.coredb.adapters.common.ContactIdAdapter
import chat.sphinx.features.coredb.adapters.common.ContactIdsAdapter
import chat.sphinx.features.coredb.adapters.common.DashboardIdAdapter
import chat.sphinx.features.coredb.adapters.common.DateTimeAdapter
import chat.sphinx.features.coredb.adapters.common.InviteIdAdapter
import chat.sphinx.features.coredb.adapters.common.InviteStatusAdapter
import chat.sphinx.features.coredb.adapters.common.LightningNodePubKeyAdapter
import chat.sphinx.features.coredb.adapters.common.LightningPaymentHashAdapter
import chat.sphinx.features.coredb.adapters.common.LightningPaymentRequestAdapter
import chat.sphinx.features.coredb.adapters.common.MessageIdAdapter
import chat.sphinx.features.coredb.adapters.common.PhotoUrlAdapter
import chat.sphinx.features.coredb.adapters.common.SatAdapter
import chat.sphinx.features.coredb.adapters.common.SeenAdapter
import chat.sphinx.features.coredb.adapters.common.SubscriptionIdAdapter
import chat.sphinx.features.coredb.adapters.contact.BlockedAdapter
import chat.sphinx.features.coredb.adapters.contact.ContactAliasAdapter
import chat.sphinx.features.coredb.adapters.contact.ContactOwnerAdapter
import chat.sphinx.features.coredb.adapters.contact.ContactPublicKeyAdapter
import chat.sphinx.features.coredb.adapters.contact.ContactStatusAdapter
import chat.sphinx.features.coredb.adapters.contact.DeviceIdAdapter
import chat.sphinx.features.coredb.adapters.contact.LightningNodeAliasAdapter
import chat.sphinx.features.coredb.adapters.contact.LightningRouteHintAdapter
import chat.sphinx.features.coredb.adapters.contact.NotificationSoundAdapter
import chat.sphinx.features.coredb.adapters.contact.PrivatePhotoAdapter
import chat.sphinx.features.coredb.adapters.feed.*
import chat.sphinx.features.coredb.adapters.feed.FeedAuthorAdapter
import chat.sphinx.features.coredb.adapters.feed.FeedContentTypeAdapter
import chat.sphinx.features.coredb.adapters.feed.FeedDescriptionAdapter
import chat.sphinx.features.coredb.adapters.feed.FeedDestinationAddressAdapter
import chat.sphinx.features.coredb.adapters.feed.FeedDestinationSplitAdapter
import chat.sphinx.features.coredb.adapters.feed.FeedDestinationTypeAdapter
import chat.sphinx.features.coredb.adapters.feed.FeedEnclosureLengthAdapter
import chat.sphinx.features.coredb.adapters.feed.FeedEnclosureTypeAdapter
import chat.sphinx.features.coredb.adapters.feed.FeedGeneratorAdapter
import chat.sphinx.features.coredb.adapters.feed.FeedIdAdapter
import chat.sphinx.features.coredb.adapters.feed.FeedItemDurationAdapter
import chat.sphinx.features.coredb.adapters.feed.FeedItemsCountAdapter
import chat.sphinx.features.coredb.adapters.feed.FeedLanguageAdapter
import chat.sphinx.features.coredb.adapters.feed.FeedModelSuggestedAdapter
import chat.sphinx.features.coredb.adapters.feed.FeedModelTypeAdapter
import chat.sphinx.features.coredb.adapters.feed.FeedTitleAdapter
import chat.sphinx.features.coredb.adapters.feed.FeedTypeAdapter
import chat.sphinx.features.coredb.adapters.feed.FeedUrlAdapter
import chat.sphinx.features.coredb.adapters.feed.SubscribedAdapter
import chat.sphinx.features.coredb.adapters.message.FlaggedAdapter
import chat.sphinx.features.coredb.adapters.message.MessageContentAdapter
import chat.sphinx.features.coredb.adapters.message.MessageContentDecryptedAdapter
import chat.sphinx.features.coredb.adapters.message.MessageMUIDAdapter
import chat.sphinx.features.coredb.adapters.message.MessageStatusAdapter
import chat.sphinx.features.coredb.adapters.message.MessageTypeAdapter
import chat.sphinx.features.coredb.adapters.message.MessageUUIDAdapter
import chat.sphinx.features.coredb.adapters.message.ReplyUUIDAdapter
import chat.sphinx.features.coredb.adapters.message.SenderAliasAdapter
import com.squareup.sqldelight.db.SqlDriver
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.collect

abstract class CoreDBImpl: CoreDB() {

    companion object {
        const val DB_NAME = "sphinx.db"
    }

    private val sphinxDatabaseQueriesStateFlow: MutableStateFlow<SphinxDatabaseQueries?> =
        MutableStateFlow(null)

    override val isInitialized: Boolean
        get() = sphinxDatabaseQueriesStateFlow.value != null

    override fun getSphinxDatabaseQueriesOrNull(): SphinxDatabaseQueries? {
        return sphinxDatabaseQueriesStateFlow.value
    }

    protected abstract fun getSqlDriver(encryptionKey: EncryptionKey): SqlDriver

    private val initializationLock = Object()

    fun initializeDatabase(encryptionKey: EncryptionKey) {
        if (isInitialized) {
            return
        }

        synchronized(initializationLock) {

            if (isInitialized) {
                return
            }

            sphinxDatabaseQueriesStateFlow.value = SphinxDatabase(
                driver = getSqlDriver(encryptionKey),
                chatDboAdapter = ChatDbo.Adapter(
                    idAdapter = ChatIdAdapter.getInstance(),
                    uuidAdapter = ChatUUIDAdapter(),
                    nameAdapter = ChatNameAdapter(),
                    photo_urlAdapter = PhotoUrlAdapter.getInstance(),
                    typeAdapter = ChatTypeAdapter(),
                    statusAdapter = ChatStatusAdapter(),
                    contact_idsAdapter = ContactIdsAdapter.getInstance(),
                    is_mutedAdapter = ChatMutedAdapter.getInstance(),
                    created_atAdapter = DateTimeAdapter.getInstance(),
                    group_keyAdapter = ChatGroupKeyAdapter(),
                    hostAdapter = ChatHostAdapter(),
                    price_per_messageAdapter = SatAdapter.getInstance(),
                    escrow_amountAdapter = SatAdapter.getInstance(),
                    unlistedAdapter = ChatUnlistedAdapter(),
                    private_tribeAdapter = ChatPrivateAdapter(),
                    owner_pub_keyAdapter = LightningNodePubKeyAdapter.getInstance(),
                    seenAdapter = SeenAdapter.getInstance(),
                    meta_dataAdapter = ChatMetaDataAdapter(),
                    my_photo_urlAdapter = PhotoUrlAdapter.getInstance(),
                    my_aliasAdapter = ChatAliasAdapter(),
                    pending_contact_idsAdapter = ContactIdsAdapter.getInstance(),
                    latest_message_idAdapter = MessageIdAdapter.getInstance(),
                    content_seen_atAdapter = DateTimeAdapter.getInstance()
                ),
                contactDboAdapter = ContactDbo.Adapter(
                    idAdapter = ContactIdAdapter.getInstance(),
                    route_hintAdapter = LightningRouteHintAdapter(),
                    node_pub_keyAdapter = LightningNodePubKeyAdapter.getInstance(),
                    node_aliasAdapter = LightningNodeAliasAdapter(),
                    aliasAdapter = ContactAliasAdapter(),
                    photo_urlAdapter = PhotoUrlAdapter.getInstance(),
                    private_photoAdapter = PrivatePhotoAdapter(),
                    ownerAdapter = ContactOwnerAdapter(),
                    statusAdapter = ContactStatusAdapter(),
                    public_keyAdapter = ContactPublicKeyAdapter(),
                    device_idAdapter = DeviceIdAdapter(),
                    created_atAdapter = DateTimeAdapter.getInstance(),
                    updated_atAdapter = DateTimeAdapter.getInstance(),
                    notification_soundAdapter = NotificationSoundAdapter(),
                    tip_amountAdapter = SatAdapter.getInstance(),
                    invite_idAdapter = InviteIdAdapter.getInstance(),
                    invite_statusAdapter = InviteStatusAdapter.getInstance(),
                    blockedAdapter = BlockedAdapter.getInstance(),
                ),
                inviteDboAdapter = InviteDbo.Adapter(
                    idAdapter = InviteIdAdapter.getInstance(),
                    invite_stringAdapter = InviteStringAdapter(),
                    invoiceAdapter = LightningPaymentRequestAdapter.getInstance(),
                    contact_idAdapter = ContactIdAdapter.getInstance(),
                    statusAdapter = InviteStatusAdapter.getInstance(),
                    priceAdapter = SatAdapter.getInstance(),
                    created_atAdapter = DateTimeAdapter.getInstance(),
                ),
                dashboardDboAdapter = DashboardDbo.Adapter(
                    idAdapter = DashboardIdAdapter(),
                    contact_idAdapter = ContactIdAdapter.getInstance(),
                    dateAdapter = DateTimeAdapter.getInstance(),
                    mutedAdapter = ChatMutedAdapter.getInstance(),
                    seenAdapter = SeenAdapter.getInstance(),
                    photo_urlAdapter = PhotoUrlAdapter.getInstance(),
                    latest_message_idAdapter = MessageIdAdapter.getInstance()
                ),
                messageDboAdapter = MessageDbo.Adapter(
                    idAdapter = MessageIdAdapter.getInstance(),
                    uuidAdapter = MessageUUIDAdapter(),
                    chat_idAdapter = ChatIdAdapter.getInstance(),
                    typeAdapter = MessageTypeAdapter(),
                    senderAdapter = ContactIdAdapter.getInstance(),
                    receiver_Adapter = ContactIdAdapter.getInstance(),
                    amountAdapter = SatAdapter.getInstance(),
                    payment_hashAdapter = LightningPaymentHashAdapter.getInstance(),
                    payment_requestAdapter = LightningPaymentRequestAdapter.getInstance(),
                    dateAdapter = DateTimeAdapter.getInstance(),
                    expiration_dateAdapter = DateTimeAdapter.getInstance(),
                    message_contentAdapter = MessageContentAdapter(),
                    message_content_decryptedAdapter = MessageContentDecryptedAdapter(),
                    statusAdapter = MessageStatusAdapter(),
                    seenAdapter = SeenAdapter.getInstance(),
                    sender_aliasAdapter = SenderAliasAdapter(),
                    sender_picAdapter = PhotoUrlAdapter.getInstance(),
                    original_muidAdapter = MessageMUIDAdapter(),
                    reply_uuidAdapter = ReplyUUIDAdapter(),
                    muidAdapter = MessageMUIDAdapter(),
                    flaggedAdapter = FlaggedAdapter.getInstance(),
                ),
                messageMediaDboAdapter = MessageMediaDbo.Adapter(
                    idAdapter = MessageIdAdapter.getInstance(),
                    chat_idAdapter = ChatIdAdapter.getInstance(),
                    media_keyAdapter = MediaKeyAdapter(),
                    media_key_decryptedAdapter = MediaKeyDecryptedAdapter(),
                    media_typeAdapter = MediaTypeAdapter(),
                    media_tokenAdapter = MediaTokenAdapter(),
                    local_fileAdapter = PathAdapter.getInstance(),
                ),
                subscriptionDboAdapter = SubscriptionDbo.Adapter(
                    idAdapter = SubscriptionIdAdapter.getInstance(),
                    cronAdapter = CronAdapter(),
                    amountAdapter = SatAdapter.getInstance(),
                    end_numberAdapter = EndNumberAdapter(),
                    countAdapter = SubscriptionCountAdapter(),
                    end_dateAdapter = DateTimeAdapter.getInstance(),
                    created_atAdapter = DateTimeAdapter.getInstance(),
                    updated_atAdapter = DateTimeAdapter.getInstance(),
                    chat_idAdapter = ChatIdAdapter.getInstance(),
                    contact_idAdapter = ContactIdAdapter.getInstance()
                ),
                feedDboAdapter = FeedDbo.Adapter(
                    idAdapter = FeedIdAdapter(),
                    feed_typeAdapter = FeedTypeAdapter(),
                    titleAdapter = FeedTitleAdapter(),
                    descriptionAdapter = FeedDescriptionAdapter(),
                    feed_urlAdapter = FeedUrlAdapter.getInstance(),
                    authorAdapter = FeedAuthorAdapter(),
                    generatorAdapter = FeedGeneratorAdapter(),
                    image_urlAdapter = PhotoUrlAdapter.getInstance(),
                    owner_urlAdapter = FeedUrlAdapter.getInstance(),
                    linkAdapter = FeedUrlAdapter.getInstance(),
                    date_publishedAdapter = DateTimeAdapter.getInstance(),
                    date_updatedAdapter = DateTimeAdapter.getInstance(),
                    content_typeAdapter = FeedContentTypeAdapter(),
                    languageAdapter = FeedLanguageAdapter(),
                    items_countAdapter = FeedItemsCountAdapter(),
                    current_item_idAdapter = FeedIdAdapter(),
                    chat_idAdapter = ChatIdAdapter.getInstance(),
                    subscribedAdapter = SubscribedAdapter.getInstance()
                ),
                feedItemDboAdapter = FeedItemDbo.Adapter(
                    idAdapter = FeedIdAdapter(),
                    titleAdapter = FeedTitleAdapter(),
                    descriptionAdapter = FeedDescriptionAdapter(),
                    date_publishedAdapter = DateTimeAdapter.getInstance(),
                    date_updatedAdapter = DateTimeAdapter.getInstance(),
                    authorAdapter = FeedAuthorAdapter(),
                    content_typeAdapter = FeedContentTypeAdapter(),
                    enclosure_lengthAdapter = FeedEnclosureLengthAdapter(),
                    enclosure_urlAdapter = FeedUrlAdapter.getInstance(),
                    enclosure_typeAdapter = FeedEnclosureTypeAdapter(),
                    image_urlAdapter = PhotoUrlAdapter.getInstance(),
                    thumbnail_urlAdapter = PhotoUrlAdapter.getInstance(),
                    linkAdapter = FeedUrlAdapter.getInstance(),
                    feed_idAdapter = FeedIdAdapter(),
                    durationAdapter = FeedItemDurationAdapter(),
                    local_fileAdapter = PathAdapter.getInstance(),
                ),
                feedModelDboAdapter = FeedModelDbo.Adapter(
                    idAdapter = FeedIdAdapter(),
                    typeAdapter = FeedModelTypeAdapter(),
                    suggestedAdapter = FeedModelSuggestedAdapter()
                ),
                feedDestinationDboAdapter = FeedDestinationDbo.Adapter(
                    addressAdapter = FeedDestinationAddressAdapter(),
                    splitAdapter = FeedDestinationSplitAdapter(),
                    typeAdapter = FeedDestinationTypeAdapter(),
                    feed_idAdapter = FeedIdAdapter()
                )
            ).sphinxDatabaseQueries
        }
    }

    private class Hackery(val hack: SphinxDatabaseQueries): Exception()

    override suspend fun getSphinxDatabaseQueries(): SphinxDatabaseQueries {
        sphinxDatabaseQueriesStateFlow.value?.let { queries ->
            return queries
        }

        var queries: SphinxDatabaseQueries? = null

        try {
            sphinxDatabaseQueriesStateFlow.collect { queriesState ->
                if (queriesState != null) {
                    queries = queriesState
                    throw Hackery(queriesState)
                }
            }
        } catch (e: Hackery) {
            return e.hack
        }

        // Will never make it here, but to please the IDE just in case...
        delay(25L)
        return queries!!
    }
}
