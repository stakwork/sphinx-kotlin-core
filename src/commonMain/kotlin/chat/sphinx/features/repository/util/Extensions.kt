package chat.sphinx.features.repository.util

import chat.sphinx.concepts.network.query.chat.model.NewTribeDto
import chat.sphinx.concepts.network.query.chat.model.TribeDto
import chat.sphinx.concepts.network.query.chat.model.feed.FeedDto
import chat.sphinx.concepts.network.query.contact.model.ContactDto
import chat.sphinx.concepts.network.query.invite.model.InviteDto
import chat.sphinx.concepts.network.query.lightning.model.balance.BalanceDto
import chat.sphinx.concepts.network.query.message.model.MessageDto
import chat.sphinx.concepts.network.query.subscription.model.SubscriptionDto
import chat.sphinx.database.core.SphinxDatabaseQueries
import chat.sphinx.wrapper.*
import chat.sphinx.wrapper.chat.*
import chat.sphinx.wrapper.contact.*
import chat.sphinx.wrapper.dashboard.ChatId
import chat.sphinx.wrapper.dashboard.ContactId
import chat.sphinx.wrapper.dashboard.InviteId
import chat.sphinx.wrapper.feed.*
import chat.sphinx.wrapper.invite.*
import chat.sphinx.wrapper.lightning.*
import chat.sphinx.wrapper.message.*
import chat.sphinx.wrapper.message.media.*
import chat.sphinx.wrapper.rsa.RsaPublicKey
import chat.sphinx.wrapper.subscription.Cron
import chat.sphinx.wrapper.subscription.EndNumber
import chat.sphinx.wrapper.subscription.SubscriptionCount
import chat.sphinx.wrapper.subscription.SubscriptionId
import chat.sphinx.wrapper.user.UserState
import chat.sphinx.wrapper.user.UserStateId
import chat.sphinx.wrapper_chat.NotificationLevel
import chat.sphinx.wrapper_chat.toNotificationLevel
import chat.sphinx.wrapper_message.toThreadUUID
import com.squareup.sqldelight.TransactionCallbacks

@Suppress("NOTHING_TO_INLINE")
inline fun BalanceDto.toNodeBalanceOrNull(): NodeBalance? =
    try {
        toNodeBalance()
    } catch (e: IllegalArgumentException) {
        null
    }

@Suppress("NOTHING_TO_INLINE")
inline fun BalanceDto.toNodeBalance(): NodeBalance =
    NodeBalance(
        Sat(balance),
    )

@Suppress("NOTHING_TO_INLINE")
inline fun Long.toNodeBalance(): NodeBalance? {
    return try {
        NodeBalance(Sat(this))
    } catch (e: NumberFormatException) {
        null
    }
}

inline val MessageDto.updateChatDboLatestMessage: Boolean
    get() = type.toMessageType().show &&
            status != MessageStatus.DELETED

inline val Message.updateChatNewLatestMessage: Boolean
    get() = type.show &&
            type != MessageType.BotRes &&
            status != MessageStatus.Deleted

@Suppress("NOTHING_TO_INLINE")
inline fun TransactionCallbacks.updateChatDboLatestMessage(
    messageDto: MessageDto,
    chatId: ChatId,
    latestMessageUpdatedTimeMap: MutableMap<ChatId, DateTime>,
    queries: SphinxDatabaseQueries,
) {
    val dateTime = messageDto.created_at.toDateTime()

    if (
        messageDto.updateChatDboLatestMessage &&
        (latestMessageUpdatedTimeMap[chatId]?.time ?: 0L) <= dateTime.time
    ) {
        queries.chatUpdateLatestMessage(
            MessageId(messageDto.id),
            chatId,
        )
        queries.dashboardUpdateLatestMessage(
            dateTime,
            MessageId(messageDto.id),
            chatId,
        )
        latestMessageUpdatedTimeMap[chatId] = dateTime
    }
}

@Suppress("NOTHING_TO_INLINE")
inline fun TransactionCallbacks.updateChatNewLatestMessage(
    message: Message,
    chatId: ChatId,
    latestMessageUpdatedTimeMap: MutableMap<ChatId, DateTime>,
    queries: SphinxDatabaseQueries,
) {
    val dateTime = message.date

    if (
        message.updateChatNewLatestMessage &&
        (latestMessageUpdatedTimeMap[chatId]?.time ?: 0L) <= dateTime.time
    ) {
        queries.chatUpdateLatestMessage(
            message.id,
            chatId,
        )
        queries.dashboardUpdateLatestMessage(
            dateTime,
            message.id,
            chatId,
        )
        latestMessageUpdatedTimeMap[chatId] = dateTime
    }
}


@Suppress("NOTHING_TO_INLINE")
inline fun TransactionCallbacks.updateChatDboLatestMessage(
    messageDto: MessageDto,
    chatId: ChatId,
    latestMessageUpdatedTimeMap: SynchronizedMap<ChatId, DateTime>,
    queries: SphinxDatabaseQueries,
) {
    latestMessageUpdatedTimeMap.withLock { map ->
        updateChatDboLatestMessage(messageDto, chatId, map, queries)
    }
}

@Suppress("NOTHING_TO_INLINE")
inline fun TransactionCallbacks.updateChatNewLatestMessage(
    message: Message,
    chatId: ChatId,
    latestMessageUpdatedTimeMap: SynchronizedMap<ChatId, DateTime>,
    queries: SphinxDatabaseQueries,
) {
    latestMessageUpdatedTimeMap.withLock { map ->
        updateChatNewLatestMessage(message, chatId, map, queries)
    }
}

@Suppress("NOTHING_TO_INLINE")
inline fun TransactionCallbacks.updateChatMuted(
    chatId: ChatId,
    muted: ChatMuted,
    queries: SphinxDatabaseQueries
) {
    queries.chatUpdateMuted(muted, chatId)
    queries.dashboardUpdateMuted(muted, chatId)
}

@Suppress("NOTHING_TO_INLINE")
inline fun TransactionCallbacks.updateChatNotificationLevel(
    chatId: ChatId,
    notificationLevel: NotificationLevel?,
    queries: SphinxDatabaseQueries
) {
    queries.chatUpdateNotificationLevel(notificationLevel, chatId)
}

@Suppress("NOTHING_TO_INLINE", "SpellCheckingInspection")
inline fun TransactionCallbacks.updateNewChatTribeData(
    tribe: NewTribeDto,
    chatId: ChatId,
    queries: SphinxDatabaseQueries,
) {
    // Needs to implement the rest of args

    val pricePerMessage = tribe.getPricePerMessageInSats().toSat()
    val escrowAmount = tribe.getEscrowAmountInSats().toSat()
    val name = tribe.name.toChatName()
    val photoUrl = tribe.img?.toPhotoUrl()
    val pinMessage = tribe.pin?.toMessageUUID()
    val secondBrainUrl = tribe.second_brain_url?.toSecondBrainUrl()

    queries.chatUpdateTribeData(
        pricePerMessage,
        escrowAmount,
        name,
        photoUrl,
        secondBrainUrl,
        chatId
    )

    queries.dashboardUpdateTribe(
        name?.value ?: "",
        photoUrl,
        chatId
    )
}

@Suppress("NOTHING_TO_INLINE", "SpellCheckingInspection")
inline fun TransactionCallbacks.updateChatTribeData(
    tribe: TribeDto,
    chatId: ChatId,
    queries: SphinxDatabaseQueries,
) {
    val pricePerMessage = tribe.price_per_message.toSat()
    val escrowAmount = tribe.escrow_amount.toSat()
    val name = tribe.name.toChatName()
    val photoUrl = tribe.img?.toPhotoUrl()
    val secondBrainUrl = tribe.second_brain_url?.toSecondBrainUrl()

    queries.chatUpdateTribeData(
        pricePerMessage,
        escrowAmount,
        name,
        photoUrl,
        secondBrainUrl,
        chatId,
    )

    queries.dashboardUpdateTribe(
        name?.value ?: "",
        photoUrl,
        chatId
    )
}

inline fun TransactionCallbacks.upsertNewChat(
    chat: Chat, // Replaced ChatDto with Chat
    chatSeenMap: SynchronizedMap<ChatId, Seen>,
    queries: SphinxDatabaseQueries,
    contact: Contact? = null, // Replaced ContactDto with Contact
    ownerPubKey: LightningNodePubKey? = null
) {
    val seen = chat.seen
    val chatId = chat.id
    val chatType = chat.type
    val createdAt = chat.createdAt
    val contactIds = chat.contactIds
    val muted = chat.isMuted
    val chatPhotoUrl = chat.photoUrl
    val pricePerMessage = chat.pricePerMessage
    val escrowAmount = chat.escrowAmount
    val chatName = chat.name
    val adminPubKey = chat.ownerPubKey

    queries.chatUpsert(
        chatName,
        chatPhotoUrl,
        chat.status,
        contactIds,
        muted,
        chat.groupKey,
        chat.host,
        chat.unlisted,
        chat.privateTribe,
        chat.ownerPubKey,
        seen,
        null,
        chat.myPhotoUrl,
        chat.myAlias,
        chat.pendingContactIds,
        chat.notify,
        chatId,
        chat.uuid,
        chatType,
        createdAt,
        pricePerMessage,
        escrowAmount,
        null
    )

    if (
        chatType.isTribe() &&
        (ownerPubKey == adminPubKey) &&
        (pricePerMessage != null || escrowAmount != null)
    ) {
        queries.chatUpdateTribeData(
            pricePerMessage,
            escrowAmount,
            chatName,
            chatPhotoUrl,
            null,
            chatId
        )
    }

    val conversationContactId: ContactId? = if (chatType.isConversation()) {
        contactIds.elementAtOrNull(1)?.let { contactId ->
            queries.dashboardUpdateIncludeInReturn(false, contactId)
            contactId
        }
    } else {
        null
    }

    queries.dashboardUpsert(
        if (conversationContactId != null && contact != null) {
            contact.alias?.value
        } else {
            contact?.alias?.value ?: " "
        },
        muted,
        seen,
        if (conversationContactId != null && contact != null) {
            contact.photoUrl
        } else {
            chatPhotoUrl
        },
        chatId,
        conversationContactId,
        createdAt
    )

    chatSeenMap.withLock { it[ChatId(chat.id.value)] = seen }
}

@Suppress("NOTHING_TO_INLINE", "SpellCheckingInspection")
inline fun TransactionCallbacks.upsertNewContact(contact: Contact, queries: SphinxDatabaseQueries) {

    if (contact.fromGroup.isTrue()) {
        return
    }

    val routeHint = contact.routeHint
    val nodePubKey = contact.nodePubKey
    val nodeAlias = contact.nodeAlias
    val alias = contact.alias
    val photoUrl = contact.photoUrl
    val privatePhoto = contact.privatePhoto
    val status = contact.status
    val rsaPublicKey = contact.rsaPublicKey
    val deviceId = contact.deviceId
    val updatedAt = contact.updatedAt
    val notificationSound = contact.notificationSound
    val tipAmount = contact.tipAmount
    val blocked = contact.blocked

    // Perform the upsert operation
    queries.contactUpsert(
        routeHint,
        nodePubKey,
        nodeAlias,
        alias,
        photoUrl,
        privatePhoto,
        status,
        rsaPublicKey,
        deviceId,
        updatedAt,
        notificationSound,
        tipAmount,
        blocked,
        contact.id,
        contact.isOwner,
        contact.createdAt
    )

    if (!contact.isOwner.isTrue()) {
        queries.dashboardUpsert(
            contact.alias?.value,
            ChatMuted.False,
            Seen.True,
            contact.photoUrl,
            contact.id,
            null,
            contact.createdAt
        )
        queries.dashboardUpdateConversation(
            contact.alias?.value,
            contact.photoUrl,
            contact.id
        )
    }
}

@Suppress("NOTHING_TO_INLINE", "SpellCheckingInspection")
inline fun TransactionCallbacks.upsertContact(dto: ContactDto, queries: SphinxDatabaseQueries) {

    if (dto.fromGroupActual) {
        return
    }

    val contactId = ContactId(dto.id)
    val createdAt = dto.created_at.toDateTime()
    val isOwner = dto.isOwnerActual.toOwner()
    val photoUrl = dto.photo_url?.toPhotoUrl()

    queries.contactUpsert(
        dto.route_hint?.toLightningRouteHint(),
        dto.public_key?.toLightningNodePubKey(),
        dto.node_alias?.toLightningNodeAlias(),
        dto.alias?.toContactAlias(),
        photoUrl,
        dto.privatePhotoActual.toPrivatePhoto(),
        dto.status.toContactStatus(),
        dto.contact_key?.let { RsaPublicKey(it.toCharArray()) },
        dto.device_id?.toDeviceId(),
        dto.updated_at.toDateTime(),
        dto.notification_sound?.toNotificationSound(),
        dto.tip_amount?.toSat(),
        dto.blockedActual.toBlocked(),
        contactId,
        isOwner,
        createdAt,
    )

    dto.invite?.let { inviteDto ->
        upsertInvite(inviteDto, queries)
    }

    if (!isOwner.isTrue()) {
        queries.dashboardUpsert(
            dto.alias,
            ChatMuted.False,
            Seen.True,
            photoUrl,
            contactId,
            null,
            createdAt,
        )
        queries.dashboardUpdateConversation(
            dto.alias,
            photoUrl,
            contactId
        )
    }
}

@Suppress("NOTHING_TO_INLINE")
inline fun TransactionCallbacks.upsertNewInvite(invite: Invite, queries: SphinxDatabaseQueries) {

    queries.inviteUpsert(
        invite_string = invite.inviteString,
        invoice = invite.paymentRequest,
        status = invite.status,
        price = invite.price,
        id = invite.id,
        contact_id = invite.contactId,
        created_at = invite.createdAt,
        invite_code = invite.inviteCode,
    )

    queries.contactUpdateInvite(
        invite.status,
        invite.id,
        invite.contactId
    )
}


@Suppress("NOTHING_TO_INLINE")
inline fun TransactionCallbacks.upsertInvite(dto: InviteDto, queries: SphinxDatabaseQueries) {

    val inviteStatus = dto.status.toInviteStatus().let { dtoStatus ->
        if (dtoStatus.isPaymentPending()) {

            val invite = queries.inviteGetById(InviteId(dto.id)).executeAsOneOrNull()

            if (invite?.status?.isProcessingPayment() == true) {
                InviteStatus.ProcessingPayment
            } else {
                dtoStatus
            }

        } else {
            dtoStatus
        }
    }

    queries.inviteUpsert(
        InviteString(dto.invite_string),
        null,
        dto.invoice?.toLightningPaymentRequestOrNull(),
        inviteStatus,
        dto.price?.toSat(),
        InviteId(dto.id),
        ContactId(dto.contact_id),
        dto.created_at.toDateTime(),
    )

    queries.contactUpdateInvite(
        inviteStatus,
        InviteId(dto.id),
        ContactId(dto.contact_id)
    )

// TODO: Work out what status needs to be included to be shown on the dashboard

//        when (inviteStatus) {
//            is InviteStatus.Complete -> TODO()
//            is InviteStatus.Delivered -> TODO()
//            is InviteStatus.Expired -> TODO()
//            is InviteStatus.InProgress -> TODO()
//            is InviteStatus.PaymentPending -> TODO()
//            is InviteStatus.Pending -> TODO()
//            is InviteStatus.Ready -> TODO()
//            is InviteStatus.Unknown -> TODO()
//        }
//        queries.dashboardInsert(
//            InviteId(it.id),
//            DateTime.nowUTC().toDateTime(),
//        )
}

@Suppress("NOTHING_TO_INLINE")
inline fun TransactionCallbacks.updateInviteStatus(
    inviteId: InviteId,
    inviteStatus: InviteStatus,
    queries: SphinxDatabaseQueries,
) {
    queries.inviteUpdateStatus(inviteStatus, inviteId)
}

fun TransactionCallbacks.upsertNewMessage(
    message: NewMessage,
    queries: SphinxDatabaseQueries,
    fileName: FileName? = null
) {
    val chatId: ChatId = message.chatId

    message.messageMedia?.mediaToken?.let { mediaToken ->

        if (mediaToken.value.isEmpty()) return

        queries.messageMediaUpsert(
            (message.messageMedia?.mediaKey?.value ?: "").toMediaKey(),
            (message.messageMedia?.mediaType?.value ?: "").toMediaType(),
            MediaToken(mediaToken.value),
            MessageId(message.id.value),
            chatId,
            (message.messageMedia?.mediaKeyDecrypted?.value ?: "").toMediaKeyDecrypted(),
            message.messageMedia?.localFile,
            fileName
        )
    }

    queries.messageUpsert(
        message.status,
        message.seen,
        message.senderAlias,
        message.senderPic,
        message.originalMUID,
        message.replyUUID,
        message.type,
        message.recipientAlias,
        message.recipientPic,
        Push.False,
        message.person,
        message.threadUUID,
        message.tagMessage,
        message.errorMessage,
        MessageId(message.id.value),
        message.uuid,
        chatId,
        message.sender,
        message.receiver?.let { ContactId(it.value) },
        message.amount,
        message.paymentHash,
        message.paymentRequest,
        message.date,
        message.expirationDate,
        message.messageContent,
        message.messageContentDecrypted,
        message.messageMedia?.mediaToken?.getMUIDFromMediaToken()?.value?.toMessageMUID(),
        message.flagged.value.toFlagged()
    )

    if (message.type.isInvoicePayment()) {
        message.paymentHash?.let {
            queries.messageUpdateInvoiceAsPaidByPaymentHash(LightningPaymentHash(it.value))
        }
    }
}



@Suppress("SpellCheckingInspection")
fun TransactionCallbacks.upsertMessage(
    dto: MessageDto,
    queries: SphinxDatabaseQueries,
    fileName: FileName? = null
) {

    val chatId: ChatId = dto.chat_id?.let {
        ChatId(it)
    } ?: dto.chat?.id?.let {
        ChatId(it)
    } ?: ChatId(ChatId.NULL_CHAT_ID.toLong())

    dto.media_token?.let { mediaToken ->

        if (mediaToken.isEmpty()) return

        queries.messageMediaUpsert(
            (dto.media_key ?: "").toMediaKey(),
            (dto.media_type ?: "").toMediaType(),
            MediaToken(mediaToken),
            MessageId(dto.id),
            chatId,
            dto.mediaKeyDecrypted?.toMediaKeyDecrypted(),
            dto.mediaLocalFile,
            fileName
        )

    }

    queries.messageUpsert(
        dto.status.toMessageStatus(),
        dto.seenActual.toSeen(),
        dto.sender_alias?.toSenderAlias(),
        dto.sender_pic?.toPhotoUrl(),
        dto.original_muid?.toMessageMUID(),
        dto.reply_uuid?.toReplyUUID(),
        dto.type.toMessageType(),
        dto.recipient_alias?.toRecipientAlias(),
        dto.recipient_pic?.toPhotoUrl(),
        dto.pushActual.toPush(),
        dto.person?.toMessagePerson(),
        dto.thread_uuid?.toThreadUUID(),
        null,
        null,
        MessageId(dto.id),
        dto.uuid?.toMessageUUID(),
        chatId,
        ContactId(dto.sender),
        dto.receiver?.let { ContactId(it) },
        Sat(dto.amount),
        dto.payment_hash?.toLightningPaymentHash(),
        dto.payment_request?.toLightningPaymentRequestOrNull(),
        dto.date.toDateTime(),
        dto.expiration_date?.toDateTime(),
        dto.message_content?.toMessageContent(),
        dto.messageContentDecrypted?.toMessageContentDecrypted(),
        dto.media_token?.toMediaToken()?.getMUIDFromMediaToken()?.value?.toMessageMUID(),
        false.toFlagged()
    )

    if (dto.type.toMessageType()?.isInvoicePayment() == true) {
        dto.payment_hash?.toLightningPaymentHash()?.let {
            queries.messageUpdateInvoiceAsPaidByPaymentHash(it)
        }
    }
}

@Suppress("NOTHING_TO_INLINE")
inline fun SphinxDatabaseQueries.updateSeen(chatId: ChatId) {
    transaction {
        chatUpdateSeen(Seen.True, chatId)
        messageUpdateSeenByChatId(Seen.True, chatId)
    }
}

@Suppress("NOTHING_TO_INLINE")
inline fun TransactionCallbacks.deleteChatById(
    chatId: ChatId?,
    queries: SphinxDatabaseQueries,
    latestMessageUpdatedTimeMap: SynchronizedMap<ChatId, DateTime>?,
) {
    queries.messageDeleteByChatId(chatId ?: return)
    queries.messageMediaDeleteByChatId(chatId)
    queries.chatDeleteById(chatId)
    queries.dashboardDeleteById(chatId)
    latestMessageUpdatedTimeMap?.withLock { it.remove(chatId) }
    deleteFeedsByChatId(chatId, queries)
}

@Suppress("NOTHING_TO_INLINE")
inline fun TransactionCallbacks.deleteContactById(
    contactId: ContactId,
    queries: SphinxDatabaseQueries
) {
    queries.contactDeleteById(contactId)
    queries.inviteDeleteByContactId(contactId)
    queries.dashboardDeleteById(contactId)
    queries.subscriptionDeleteByContactId(contactId)
}

@Suppress("NOTHING_TO_INLINE")
inline fun TransactionCallbacks.deleteMessageById(
    messageId: MessageId,
    queries: SphinxDatabaseQueries
) {
    queries.messageDeleteById(messageId)
    queries.messageMediaDeleteById(messageId)
}

@Suppress("NOTHING_TO_INLINE")
inline fun TransactionCallbacks.upsertSubscription(subscriptionDto: SubscriptionDto, queries: SphinxDatabaseQueries) {
    queries.subscriptionUpsert(
        id = SubscriptionId(subscriptionDto.id),
        amount = Sat(subscriptionDto.amount),
        contact_id = ContactId(subscriptionDto.contact_id),
        chat_id = ChatId(subscriptionDto.chat_id),
        count = SubscriptionCount(subscriptionDto.count.toLong()),
        cron = Cron(subscriptionDto.cron),
        end_date = subscriptionDto.end_date?.toDateTime(),
        end_number = subscriptionDto.end_number?.let { EndNumber(it.toLong()) },
        created_at = subscriptionDto.created_at.toDateTime(),
        updated_at = subscriptionDto.updated_at.toDateTime(),
        ended = subscriptionDto.endedActual,
        paused = subscriptionDto.pausedActual,
    )
}

@Suppress("NOTHING_TO_INLINE")
inline fun TransactionCallbacks.deleteSubscriptionById(
    subscriptionId: SubscriptionId,
    queries: SphinxDatabaseQueries
) {
    queries.subscriptionDeleteById(subscriptionId)
}

fun TransactionCallbacks.upsertFeed(
    feedDto: FeedDto,
    feedUrl: FeedUrl,
    searchResultDescription: FeedDescription? = null,
    searchResultImageUrl: PhotoUrl? = null,
    chatId: ChatId,
    currentItemId: FeedId?,
    subscribed: Subscribed,
    queries: SphinxDatabaseQueries
) {

    if (feedDto.items.count() == 0) {
        return
    }

    var cItemId: FeedId? = null

    if (chatId.value != ChatId.NULL_CHAT_ID.toLong()) {
        //Deleting old feed associated with chat
        queries.feedGetAllByChatId(chatId).executeAsList()?.forEach { feedDbo ->
            if (feedDbo.feed_url.value != feedUrl.value) {
                deleteFeedById(
                    feedDbo.id,
                    queries
                )
            } else {
                //Using existing current item id on update if param is null
                cItemId = currentItemId ?: feedDbo.current_item_id
            }

        }
    }

    val feedId = FeedId(feedDto.id)

    feedDto.value?.let { feedValueDto ->
        queries.feedModelUpsert(
            type = FeedModelType(feedValueDto.model.type),
            suggested = FeedModelSuggested(feedValueDto.model.suggested),
            id = feedId
        )
    }

    val itemIds: MutableList<FeedId> = mutableListOf()

    for (item in feedDto.items) {
        val itemId = FeedId(item.id)

        itemIds.add(itemId)

        queries.feedItemUpsert(
            title = FeedTitle(item.title),
            description = item.description?.toFeedDescription(),
            date_published = item.datePublished?.secondsToDateTime(),
            date_updated = item.dateUpdated?.secondsToDateTime(),
            author = item.author?.toFeedAuthor(),
            content_type = item.contentType?.toFeedContentType(),
            enclosure_length = item.enclosureLength?.toFeedEnclosureLength(),
            enclosure_url = FeedUrl(item.enclosureUrl),
            enclosure_type = item.enclosureType?.toFeedEnclosureType(),
            image_url = item.imageUrl?.toPhotoUrl(),
            thumbnail_url = item.thumbnailUrl?.toPhotoUrl(),
            link = item.link?.toFeedUrl(),
            feed_id = feedId,
            id = itemId,
            duration = item.duration?.toFeedItemDuration(),
        )
    }

    queries.feedItemsDeleteOldByFeedId(feedId, itemIds)

    for (destination in feedDto.value?.destinations ?: listOf()) {
        queries.feedDestinationUpsert(
            address = FeedDestinationAddress(destination.address),
            split = FeedDestinationSplit(destination.split.toDouble()),
            type = FeedDestinationType(destination.type),
            feed_id = feedId
        )
    }

    val description = searchResultDescription
        ?: if (feedDto.description?.toFeedDescription() != null) {
            feedDto.description?.toFeedDescription()
        } else {
            null
        }

    val imageUrl = searchResultImageUrl
        ?: if (feedDto.imageUrl?.toPhotoUrl() != null) {
            feedDto.imageUrl?.toPhotoUrl()
        } else {
            null
        }

    queries.feedUpsert(
        feed_type = feedDto.feedType.toInt().toFeedType(),
        title = FeedTitle(feedDto.title),
        description = description,
        feed_url = feedUrl,
        author = feedDto.author?.toFeedAuthor(),
        image_url = imageUrl,
        owner_url = feedDto.ownerUrl?.toFeedUrl(),
        link = feedDto.link?.toFeedUrl(),
        date_published = feedDto.datePublished?.secondsToDateTime(),
        date_updated = feedDto.dateUpdated?.secondsToDateTime(),
        content_type = feedDto.contentType?.toFeedContentType(),
        language = feedDto.language?.toFeedLanguage(),
        items_count = FeedItemsCount(feedDto.items.count().toLong()),
        current_item_id = cItemId,
        chat_id = chatId,
        subscribed = subscribed,
        id = feedId,
        generator = feedDto.generator?.toFeedGenerator(),
    )
}


fun TransactionCallbacks.deleteFeedsByChatId(
    chatId: ChatId,
    queries: SphinxDatabaseQueries
) {
    queries.feedGetAllByChatId(chatId).executeAsList()?.forEach { feedDbo ->
        queries.feedItemsDeleteByFeedId(feedDbo.id)
        queries.feedModelDeleteById(feedDbo.id)
        queries.feedDestinationDeleteByFeedId(feedDbo.id)
        queries.feedDeleteById(feedDbo.id)
    }
}

fun TransactionCallbacks.deleteFeedById(
    feedId: FeedId,
    queries: SphinxDatabaseQueries
) {
    queries.feedGetById(feedId).executeAsOneOrNull()?.let { feedDbo ->
        queries.feedItemsDeleteByFeedId(feedDbo.id)
        queries.feedModelDeleteById(feedDbo.id)
        queries.feedDestinationDeleteByFeedId(feedDbo.id)
        queries.feedDeleteById(feedDbo.id)
    }
}

fun TransactionCallbacks.deleteAll(
    queries: SphinxDatabaseQueries
) {
    queries.chatDeleteAll()
    queries.contactDeleteAll()
    queries.inviteDeleteAll()
    queries.dashboardDeleteAll()
    queries.messageDeleteAll()
    queries.messageMediaDeleteAll()
    queries.subscriptionDeleteAll()
    queries.feedDeleteAll()
    queries.feedDeleteAll()
    queries.feedItemDeleteAll()
    queries.feedModelDeleteAll()
    queries.feedDestinationDeleteAll()
}

@Suppress("NOTHING_TO_INLINE")
fun TransactionCallbacks.upsertUserState(
    id: UserStateId,
    userState: UserState,
    queries: SphinxDatabaseQueries,
) {
    queries.userStateUpsert(
        id = id,
        user_state = userState,
    )
}
