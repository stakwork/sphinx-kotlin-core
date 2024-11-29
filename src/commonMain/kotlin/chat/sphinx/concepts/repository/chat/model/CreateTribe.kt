package chat.sphinx.concepts.repository.chat.model

import chat.sphinx.concepts.network.query.chat.model.NewTribeDto
import chat.sphinx.concepts.network.query.chat.model.PostGroupDto
import chat.sphinx.concepts.network.query.chat.model.TribeDto
import chat.sphinx.wrapper.SecondBrainUrl
import chat.sphinx.wrapper.chat.AppUrl
import chat.sphinx.wrapper.chat.toAppUrl
import chat.sphinx.wrapper.feed.FeedType
import chat.sphinx.wrapper.feed.FeedUrl
import chat.sphinx.wrapper.feed.toFeedType
import chat.sphinx.wrapper.feed.toFeedUrl
import chat.sphinx.wrapper.mqtt.NewCreateTribe
import chat.sphinx.wrapper.toSecondBrainUrl
import okio.Path
import kotlin.jvm.Synchronized

class CreateTribe private constructor(
    val name: String,
    val description: String,
    val isTribe: Boolean?,
    val pricePerMessage: Long?,
    val priceToJoin: Long?,
    val escrowAmount: Long?,
    val escrowMillis: Long?,
    val img: Path?,
    val imgUrl: String?,
    val tags: Array<String>,
    val unlisted: Boolean?,
    val private: Boolean?,
    val appUrl: AppUrl?,
    val secondBrainUrl: SecondBrainUrl?,
    val feedUrl: FeedUrl?,
    val feedType: FeedType?,
) {

    class Builder(
        val tags: Array<Tag>
    ) {
        class Tag (
            val name: String,
            val image: String,
            var isSelected: Boolean = false
        ) {
            override fun toString(): String {
                return name
            }
        }

        private var name: String? = null
        private var description: String? = null
        private var isTribe: Boolean? = true
        private var pricePerMessage: Long? = 0L
        private var priceToJoin: Long? = 0L
        private var escrowAmount: Long? = 0L
        private var escrowMillis: Long? = 0L
        private var img: Path? = null
        private var imgUrl: String? = null
        private var unlisted: Boolean? = false
        private var private: Boolean? = false
        private var appUrl: AppUrl? = null
        private var secondBrainUrl: SecondBrainUrl? = null
        private var feedUrl: FeedUrl? = null
        private var feedType: FeedType? = null

        @get:Synchronized
        val hasRequiredFields: Boolean
            get() {
                val requiredFieldsFilled = !name.isNullOrEmpty() && !description.isNullOrEmpty()

                if (feedUrl != null) {
                    return (feedType != null) && requiredFieldsFilled
                }

                return requiredFieldsFilled
            }

        @Synchronized
        fun setName(name: String?): Builder {
            this.name = name
            return this
        }

        @Synchronized
        fun setImageUrl(imgUrl: String?): Builder {
            this.imgUrl = imgUrl
            return this
        }

        @Synchronized
        fun setDescription(description: String?): Builder {
            this.description = description
            return this
        }

        @Synchronized
        fun setIsTribe(isTribe: Boolean?): Builder {
            this.isTribe = isTribe
            return this
        }

        @Synchronized
        fun setPricePerMessage(pricePerMessage: Long?): Builder {
            this.pricePerMessage = pricePerMessage
            return this
        }

        @Synchronized
        fun setPriceToJoin(priceToJoin: Long?): Builder {
            this.priceToJoin = priceToJoin
            return this
        }
        @Synchronized
        fun setEscrowAmount(escrowAmount: Long?): Builder {
            this.escrowAmount = escrowAmount
            return this
        }

        @Synchronized
        fun setEscrowMillis(escrowMillis: Long?): Builder {
            this.escrowMillis = escrowMillis
            return this
        }
        @Synchronized
        fun setImg(imgPath: Path?): Builder {
            this.img = imgPath
            return this
        }

        @Synchronized
        fun selectTag(index: Int, isSelected: Boolean): Builder {
            this.tags[index].isSelected = isSelected
            return this
        }

        @Synchronized
        fun toggleTag(index: Int): Builder {
            this.tags[index].isSelected = !this.tags[index].isSelected
            return this
        }

        private fun selectedTags(): Array<Tag> {
            return tags.filter {
                it.isSelected
            }.toTypedArray()
        }

        @Synchronized
        fun setUnlisted(unlisted: Boolean?): Builder {
            this.unlisted = unlisted
            return this
        }

        @Synchronized
        fun setPrivate(private: Boolean?): Builder {
            this.private = private
            return this
        }
        @Synchronized
        fun setAppUrl(appUrl: String?): Builder {
            this.appUrl = appUrl?.toAppUrl()
            return this
        }

        @Synchronized
        fun setSecondBrainUrl(secondBrainUrl: String?): Builder {
            this.secondBrainUrl = secondBrainUrl?.toSecondBrainUrl()
            return this
        }

        @Synchronized
        fun setFeedUrl(feedUrl: String?): Builder {
            this.feedUrl = feedUrl?.toFeedUrl()
            return this
        }

        @Synchronized
        fun setFeedType(feedType: Int?): Builder {
            this.feedType = feedType?.toFeedType()
            return this
        }

        @Synchronized
        fun load(tribeDto: TribeDto) {
            name = tribeDto.name
            imgUrl = tribeDto.img
            img = null
            description = tribeDto.description
            tags.forEach { tag ->
                tag.isSelected = tribeDto.tags.contains(tag.name)
            }
            priceToJoin = tribeDto.price_to_join
            pricePerMessage = tribeDto.price_per_message
            escrowAmount = tribeDto.escrow_amount
            escrowMillis = tribeDto.escrow_millis
            appUrl = tribeDto.app_url?.toAppUrl()
            secondBrainUrl = tribeDto.second_brain_url?.toSecondBrainUrl()
            feedUrl = tribeDto.feed_url?.toFeedUrl()
            feedType = tribeDto.feed_type?.toFeedType()
            unlisted = tribeDto.unlisted?.value
        }

        @Synchronized
        fun newLoad(newTribeDto: NewTribeDto) {
            name = newTribeDto.name
            imgUrl = newTribeDto.img
            img = null
            description = newTribeDto.description
            tags.forEach { tag ->
                tag.isSelected = newTribeDto.tags.contains(tag.name)
            }
            priceToJoin = newTribeDto.getPriceToJoinInSats()
            pricePerMessage = newTribeDto.getPricePerMessageInSats()
            escrowAmount = newTribeDto.getEscrowAmountInSats()
            escrowMillis = newTribeDto.escrow_millis
            appUrl = newTribeDto.app_url?.toAppUrl()
            secondBrainUrl = newTribeDto.second_brain_url?.toSecondBrainUrl()
            feedUrl = newTribeDto.feed_url?.toFeedUrl()
            feedType = newTribeDto.feed_type?.toFeedType()
            unlisted = newTribeDto.unlisted
            private = newTribeDto.private
        }

        @Synchronized
        fun build(): CreateTribe? =
            if (!hasRequiredFields) {
                null
            } else {
                CreateTribe(
                    name = name!!,
                    description = description!!,
                    isTribe = isTribe,
                    pricePerMessage = pricePerMessage,
                    priceToJoin = priceToJoin,
                    escrowAmount = escrowAmount,
                    escrowMillis = escrowMillis,
                    img = img,
                    imgUrl = imgUrl,
                    tags = selectedTags().map {
                        it.name
                    }.toTypedArray(),
                    unlisted = unlisted,
                    private = private,
                    appUrl = appUrl,
                    secondBrainUrl = secondBrainUrl,
                    feedUrl = feedUrl,
                    feedType = feedType
                )
            }
    }

    fun toPostGroupDto(imageUrl: String? = null): PostGroupDto {
        return PostGroupDto(
            name = name,
            description = description,
            is_tribe = isTribe,
            price_per_message = pricePerMessage,
            price_to_join = priceToJoin,
            escrow_amount = escrowAmount,
            escrow_millis = escrowMillis,
            img = imageUrl ?: imgUrl,
            tags = tags,
            unlisted = unlisted,
            private = private,
            app_url = appUrl?.value,
            second_brain_url = secondBrainUrl?.value,
            feed_url = feedUrl?.value,
            feed_type = feedType?.value?.toLong()
        )
    }
    fun toNewCreateTribe(ownerAlias: String, image: String?, tribePubKey: String?): NewCreateTribe {
        return NewCreateTribe(
            pubkey = tribePubKey,
            route_hint = null,
            name = this.name,
            description = this.description,
            tags = this.tags.toList(),
            img = image ?: this.imgUrl,
            price_per_message = (this.pricePerMessage ?: 0L) * 1000,  // Convert Sats to milliSats
            price_to_join = (this.priceToJoin ?: 0L) * 1000,  // Convert Sats to milliSats
            escrow_amount = (this.escrowAmount ?: 0L) * 1000,  // Convert Sats to milliSats
            escrow_millis = this.escrowMillis,
            unlisted = this.unlisted,
            private = this.private,
            app_url = this.appUrl?.value,
            second_brain_url = this.secondBrainUrl?.value,
            feed_url = this.feedUrl?.value,
            feed_type = this.feedType?.value,
            created = null,
            updated = null,
            member_count = null,
            last_active = null,
            owner_alias = ownerAlias
        )
    }

}