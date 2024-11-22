package chat.sphinx.features.repository.mappers.chat

import chat.sphinx.concepts.coroutines.CoroutineDispatchers
import chat.sphinx.database.core.ChatDbo
import chat.sphinx.features.repository.mappers.ClassMapper
import chat.sphinx.wrapper.chat.Chat

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
            secondBrainUrl = value.second_brain_url
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
            second_brain_url = value.secondBrainUrl
        )
    }
}
