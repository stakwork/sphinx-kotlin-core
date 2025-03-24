package chat.sphinx.wrapper.chat

import chat.sphinx.wrapper.*
import chat.sphinx.wrapper.dashboard.ChatId
import chat.sphinx.wrapper.dashboard.ContactId
import chat.sphinx.wrapper.lightning.LightningNodePubKey
import chat.sphinx.wrapper.lightning.Sat
import chat.sphinx.wrapper.message.MessageId
import chat.sphinx.wrapper.message.MessageUUID
import chat.sphinx.wrapper.message.RemoteTimezoneIdentifier
import chat.sphinx.wrapper_chat.NotificationLevel
import chat.sphinx.wrapper_chat.isMuteChat
import chat.sphinx.wrapper_chat.isOnlyMentions
import chat.sphinx.wrapper_chat.isSeeAll

@Suppress("NOTHING_TO_INLINE")
inline fun Chat.isMuted(): Boolean {
    return notify?.isMuteChat() == true
}

@Suppress("NOTHING_TO_INLINE")
inline fun Chat.isOnlyMentions(): Boolean {
    return notify?.isOnlyMentions() == true
}

@Suppress("NOTHING_TO_INLINE")
inline fun Chat.isSeeAll(): Boolean {
    return notify?.isSeeAll() == true
}

@Suppress("NOTHING_TO_INLINE")
inline fun Chat.notifyActualValue(): NotificationLevel =
    notify ?: (if (isMuted.isTrue()) NotificationLevel.MuteChat else NotificationLevel.SeeAll)

@Suppress("NOTHING_TO_INLINE")
inline fun Chat.isUnlisted(): Boolean =
    unlisted.isTrue()

@Suppress("NOTHING_TO_INLINE")
inline fun Chat.isPrivateTribe(): Boolean =
    privateTribe.isTrue()

@Suppress("NOTHING_TO_INLINE")
inline fun Chat.hasBeenSeen(): Boolean =
    seen.isTrue()

@Suppress("NOTHING_TO_INLINE")
inline fun Chat.hasPendingContacts(): Boolean =
    !pendingContactIds.isNullOrEmpty()

@Suppress("NOTHING_TO_INLINE")
inline fun Chat.getColorKey(): String {
    return "chat-${id.value}-color"
}

@Suppress("NOTHING_TO_INLINE")
inline fun Chat.isTribe(): Boolean =
    type.isTribe()

@Suppress("NOTHING_TO_INLINE")
inline fun Chat.isConversation(): Boolean =
    type.isConversation()

@Suppress("NOTHING_TO_INLINE")
inline fun Chat.isTribeOwnedByAccount(accountOwnerNodePubKey: LightningNodePubKey?): Boolean =
    type.isTribe() && ownerPubKey == accountOwnerNodePubKey

@Suppress("NOTHING_TO_INLINE")
inline fun Chat.isTribeNotOwnedByAccount(accountOwnerNodePubKey: LightningNodePubKey?): Boolean =
    type.isTribe() && ownerPubKey != accountOwnerNodePubKey

data class Chat(
    val id: ChatId,
    val uuid: ChatUUID,
    val name: ChatName?,
    val photoUrl: PhotoUrl?,
    val type: ChatType,
    val status: ChatStatus,
    val contactIds: List<ContactId>,
    val isMuted: ChatMuted,
    val createdAt: DateTime,
    val groupKey: ChatGroupKey?,
    val host: ChatHost?,
    val pricePerMessage: Sat?,
    val escrowAmount: Sat?,
    val unlisted: ChatUnlisted,
    val privateTribe: ChatPrivate,
    val ownerPubKey: LightningNodePubKey?,
    val seen: Seen,
    val metaData: ChatMetaData?,
    val myPhotoUrl: PhotoUrl?,
    val myAlias: ChatAlias?,
    val pendingContactIds: List<ContactId>?,
    val latestMessageId: MessageId?,
    val contentSeenAt: DateTime?,
    val notify: NotificationLevel?,
    val secondBrainUrl: SecondBrainUrl?,
    val pinedMessage: MessageUUID?,
    val timezoneEnabled: TimezoneEnabled?,
    val timezoneIdentifier: TimezoneIdentifier?,
    val remoteTimezoneIdentifier: RemoteTimezoneIdentifier?,
    val timezoneUpdated: TimezoneUpdated?
    )
