package chat.sphinx.concepts.notification

import com.soywiz.krypto.SecureRandom

interface SphinxNotificationManager {

    companion object {
        const val CHANNEL_ID = "SphinxNotification"
        const val CHANNEL_DESCRIPTION = "Notifications for Sphinx Chat"
        const val MEDIA_NOTIFICATION_ID = 1984
        const val DOWNLOAD_NOTIFICATION_ID = 1985L

        val SERVICE_INTENT_FILTER: String by lazy {
            SecureRandom.nextBits(130).toString(32)
        }
    }

    fun notify(
        notificationId: Long,
        groupId: String? = null,
        title: String,
        message: String
    )

    fun clearNotification(notificationId: Long)

    suspend fun toast(
        windowTitle: String,
        message: String,
        color: ULong,
        delay: Long = 2000L
    )

    suspend fun confirmAlert(
        windowTitle: String,
        title: String,
        message: String,
        confirm: () -> Unit
    )
}
