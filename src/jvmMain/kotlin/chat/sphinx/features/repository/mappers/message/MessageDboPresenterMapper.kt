package chat.sphinx.features.repository.mappers.message

import chat.sphinx.concepts.coroutines.CoroutineDispatchers
import chat.sphinx.database.core.MessageDbo
import chat.sphinx.features.repository.mappers.ClassMapper
import chat.sphinx.features.repository.model.message.MessageDboWrapper
import chat.sphinx.wrapper.message.*
import io.matthewnelson.component.base64.decodeBase64ToArray
import kotlinx.coroutines.withContext

internal class MessageDboPresenterMapper(
    dispatchers: CoroutineDispatchers,
): ClassMapper<MessageDbo, MessageDboWrapper>(dispatchers) {
    override suspend fun mapFrom(value: MessageDbo): MessageDboWrapper {
        return MessageDboWrapper(value).also { message ->
            value.message_content_decrypted?.let { decrypted ->
                if (message.type.isMessage()) {
                    when {
                        //Old Podcast boost with message type and text format
                        decrypted.isPodBoost -> {
                            withContext(default) {
                                decrypted.value.replaceFirst(FeedBoost.MESSAGE_PREFIX, "")
                                    .toPodBoostOrNull()
                                    ?.let { podBoost ->
                                        message._feedBoost = podBoost
                                    }
                            }
                        }
                        decrypted.isPodcastClip -> {
                            withContext(default) {
                                decrypted.value.replaceFirst(PodcastClip.MESSAGE_PREFIX, "")
                                    .toPodcastClipOrNull()
                                    ?.let { podcastClip ->
                                        message._podcastClip = podcastClip
                                    }
                            }
                        }
                        decrypted.isPodcastClip -> {
                            withContext(default) {
                                decrypted.value.replaceFirst(PodcastClip.MESSAGE_PREFIX, "")
                                    .toPodcastClipOrNull()
                                    ?.let { podcastClip ->
                                        message._podcastClip = podcastClip
                                    }
                            }
                        }

                        decrypted.isGiphy -> {
                            withContext(default) {
                                decrypted.value.replaceFirst(GiphyData.MESSAGE_PREFIX, "")
                                    .decodeBase64ToArray()
                                    ?.decodeToString()
                                    ?.toGiphyDataOrNull()
                                    ?.let { giphy ->
                                        message._giphyData = giphy
                                    }
                            }
                        }
                        // TODO: Handle podcast audio clips
                        // clip::{"ts":8818,"feedID":226249,"text":"Marty, I agree there is no climate emergency.","pubkey":"02683c5d0cf435fe8a0f42ba9a5999a98291476e82947707313cef69612000f718","itemID":2361506482,"title":"Rabbit Hole Recap: Bitcoin Week of 2021.05.10","url":"https://anchor.fm/s/558f520/podcast/play/33445131/https%3A%2F%2Fd3ctxlq1ktw2nl.cloudfront.net%2Fstaging%2F2021-4-13%2F186081984-44100-2-d892325769c3.m4a"}
                    }
                } else if (message.type.isBoost() && message.replyUUID == null) {
                    //New Podcast boost with boost type (29) and null uuid
                    withContext(default) {
                        decrypted.value.replaceFirst(FeedBoost.MESSAGE_PREFIX, "")
                            .toPodBoostOrNull()?.let { podBoost ->
                                message._feedBoost = podBoost
                            }
                    }
                } else if (message.type.isCallLink()) {
                    withContext(default) {
                        decrypted.value.toCallLinkMessageOrNull()
                            ?.let { callLink ->
                                message._callLinkMessage = callLink
                            }
                    }
                }

                message._messageContentDecrypted = decrypted
            }
        }
    }

    override suspend fun mapTo(value: MessageDboWrapper): MessageDbo {
        return value.messageDbo
    }
}