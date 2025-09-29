package chat.sphinx.features.repository.mappers.chat

import chat.sphinx.concepts.coroutines.CoroutineDispatchers
import chat.sphinx.database.core.ChatDbo
import chat.sphinx.features.repository.mappers.ClassMapper
import chat.sphinx.wrapper.chat.Chat

@Suppress("NOTHING_TO_INLINE")
inline fun ChatDbo.toChat(): Chat =
    Chat(
        id = id,
        uuid = uuid,
        name = name,
        photoUrl = photo_url,
        type = type,
        status = status,
        contactIds = contact_ids,
        isMuted = is_muted,
        createdAt = created_at,
        groupKey = group_key,
        host = host,
        pricePerMessage = price_per_message,
        escrowAmount = escrow_amount,
        unlisted = unlisted,
        privateTribe = private_tribe,
        ownerPubKey = owner_pub_key,
        seen = seen,
        metaData = meta_data,
        myPhotoUrl = my_photo_url,
        myAlias = my_alias,
        pendingContactIds = pending_contact_ids,
        latestMessageId = latest_message_id,
        contentSeenAt = content_seen_at,
        notify = notify,
        pinedMessage = pin_message,
        secondBrainUrl = second_brain_url,
        timezoneEnabled = timezone_enabled,
        timezoneIdentifier = timezone_identifier,
        remoteTimezoneIdentifier = remote_timezone_identifier,
        timezoneUpdated = timezone_updated,
        ownedTribe = is_my_tribe,
    )

internal class ChatDboPresenterMapper(
    dispatchers: CoroutineDispatchers
): ClassMapper<ChatDbo, Chat>(dispatchers) {

    override suspend fun mapFrom(value: ChatDbo): Chat {
        return Chat(
            id = value.id,
            uuid = value.uuid,
            name = value.name,
            photoUrl = value.photo_url,
            type = value.type,
            status = value.status,
            contactIds = value.contact_ids,
            isMuted = value.is_muted,
            createdAt = value.created_at,
            groupKey = value.group_key,
            host = value.host,
            pricePerMessage = value.price_per_message,
            escrowAmount = value.escrow_amount,
            unlisted = value.unlisted,
            privateTribe = value.private_tribe,
            ownerPubKey = value.owner_pub_key,
            seen = value.seen,
            metaData = value.meta_data,
            myPhotoUrl = value.my_photo_url,
            myAlias = value.my_alias,
            pendingContactIds = value.pending_contact_ids,
            latestMessageId = value.latest_message_id,
            contentSeenAt = value.content_seen_at,
            notify = value.notify,
            secondBrainUrl = value.second_brain_url,
            pinedMessage = value.pin_message,
            timezoneEnabled = value.timezone_enabled,
            timezoneIdentifier = value.timezone_identifier,
            remoteTimezoneIdentifier = value.remote_timezone_identifier,
            timezoneUpdated = value.timezone_updated,
            ownedTribe = value.is_my_tribe,
        )
    }

    override suspend fun mapTo(value: Chat): ChatDbo {
        return ChatDbo(
            id = value.id,
            uuid = value.uuid,
            name = value.name,
            photo_url = value.photoUrl,
            type = value.type,
            status = value.status,
            contact_ids = value.contactIds,
            is_muted = value.isMuted,
            created_at = value.createdAt,
            group_key = value.groupKey,
            host = value.host,
            price_per_message = value.pricePerMessage,
            escrow_amount = value.escrowAmount,
            unlisted = value.unlisted,
            private_tribe = value.privateTribe,
            owner_pub_key = value.ownerPubKey,
            seen = value.seen,
            meta_data = value.metaData,
            my_photo_url = value.myPhotoUrl,
            my_alias = value.myAlias,
            pending_contact_ids = value.pendingContactIds,
            latest_message_id = value.latestMessageId,
            content_seen_at = value.contentSeenAt,
            notify = value.notify,
            second_brain_url = value.secondBrainUrl,
            pin_message = value.pinedMessage,
            timezone_enabled = value.timezoneEnabled,
            timezone_identifier = value.timezoneIdentifier,
            remote_timezone_identifier = value.remoteTimezoneIdentifier,
            timezone_updated = value.timezoneUpdated,
            is_my_tribe = value.ownedTribe,
        )
    }
}
