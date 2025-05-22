package chat.sphinx.wrapper.podcast

import chat.sphinx.wrapper.DateTime
import chat.sphinx.wrapper.PhotoUrl
import chat.sphinx.wrapper.feed.*
import chat.sphinx.wrapper.time
import okio.Path

data class PodcastEpisode(
    override val id: FeedId,
    val title: FeedTitle,
    val description: FeedDescription?,
    val image: PhotoUrl?,
    val link: FeedUrl?,
    val podcastId: FeedId,
    override val enclosureUrl: FeedUrl,
    override val enclosureLength: FeedEnclosureLength?,
    override val enclosureType: FeedEnclosureType?,
    override var localFile: Path?,
    val date: DateTime? = null,
    val feedType: String = FeedRecommendation.PODCAST_TYPE,
    val showTitle: FeedTitle? = null,
    val clipStartTime: Int? = null,
    val clipEndTime: Int? = null,
    val topics: List<String> = listOf(),
    val people: List<String> = listOf(),
    val recommendationPubKey: String? = null,
    val referenceId: FeedReferenceId? = null,
    val chaptersData: FeedChaptersData? = null
) : DownloadableFeedItem {

    companion object {
        private const val _17 = 17
        private const val _31 = 31
    }

    override fun equals(other: Any?): Boolean {
        return other is PodcastEpisode && other.id == id
    }

    override fun hashCode(): Int {
        var result = _17
        result = _31 * result + id.hashCode()
        return result
    }

    var contentEpisodeStatus: ContentEpisodeStatus? = null

    fun getUpdatedContentEpisodeStatus(): ContentEpisodeStatus {
        contentEpisodeStatus?.let { return it }

        contentEpisodeStatus = ContentEpisodeStatus(
            podcastId,
            this.id,
            FeedItemDuration(0),
            FeedItemDuration((clipStartTime?.toLong() ?: 0) / 1000),
            null
        )
        return contentEpisodeStatus!!
    }

    var durationMilliseconds: Long? = null
        get() {
            getUpdatedContentEpisodeStatus().duration.value.let {
                return if (it > 0) it * 1000 else null
            }
        }

    var currentTimeSeconds: Long = 0
        get() = (currentTimeMilliseconds ?: 0) / 1000

    var currentTimeMilliseconds: Long? = null
        get() {
            getUpdatedContentEpisodeStatus().currentTime.value.let {
                return if (it > 0) it * 1000 else null
            }
        }

    var played: Boolean
        get() = getUpdatedContentEpisodeStatus().played ?: false
        set(value) {
            contentEpisodeStatus = getUpdatedContentEpisodeStatus().copy(played = value)
        }

    var titleToShow: String = ""
        get() = title.value.trim()

    var showTitleToShow: String = ""
        get() = showTitle?.value?.trim() ?: ""

    var descriptionToShow: String = ""
        get() = description?.value?.htmlToPlainText()?.trim() ?: ""

    var playing: Boolean = false

    var imageUrlToShow: PhotoUrl? = null
        get() = image


    val downloaded: Boolean
        get() = localFile != null

    val episodeUrl: String
        get() = localFile?.toString() ?: enclosureUrl.value

    val isTwitterSpace: Boolean
        get() = feedType == FeedRecommendation.TWITTER_TYPE

    val isPodcast: Boolean
        get() = feedType == FeedRecommendation.PODCAST_TYPE

    val isYouTubeVideo: Boolean
        get() = feedType == FeedRecommendation.YOUTUBE_VIDEO_TYPE

    val isMusicClip: Boolean
        get() = feedType == FeedRecommendation.PODCAST_TYPE || feedType == FeedRecommendation.TWITTER_TYPE

    val longType: Long
        get() = when {
            isMusicClip -> FeedType.PODCAST.toLong()
            isYouTubeVideo -> FeedType.VIDEO.toLong()
            else -> FeedType.PODCAST.toLong()
        }

    var datePublishedTime: Long = 0
        get() = date?.time ?: 0

    var isBoostAllowed: Boolean = false
        get() = recommendationPubKey?.toFeedDestinationAddress() != null

    var chapters: ChapterResponseDto? = null
}
