package chat.sphinx.wrapper

import chat.sphinx.utils.platform.getCurrentTimeInMillis
import com.soywiz.klock.*
import kotlinx.atomicfu.locks.SynchronizedObject
import kotlinx.atomicfu.locks.synchronized
import java.util.*
import kotlin.jvm.JvmInline
import kotlin.jvm.Volatile

/**
 * Will always format to [DateTime.formatRelay] which is:
 *
 *  - [Locale] = [Locale.ENGLISH]
 *  - [TimeZone] UTC
 * */
@Suppress("NOTHING_TO_INLINE")
inline fun String.toDateTime(): DateTime =
    DateTime(DateTime.getFormatRelay().parse(this))

@Suppress("NOTHING_TO_INLINE")
inline fun String.toDateTimeWithFormat(format: DateFormat): DateTime =
    DateTime(format.parse(this))

@Suppress("NOTHING_TO_INLINE")
inline fun Long.toDateTime(): DateTime =
    DateTime(DateTimeTz.fromUnixLocal(this))

@Suppress("NOTHING_TO_INLINE")
inline fun Long.secondsToDateTime(): DateTime =
    DateTime(DateTimeTz.fromUnix(this * 1000))

/**
 * Returns:
 *  - `hh:mm AM or PM` if [this] is after 00:00:00.000 for today's date,
 *  - `EEE dd` if [this] is before 00:00:00.000 for today's date and the same month
 *  - `EEE dd MMM` if [this] is before 00:00:00.000 for today's date and **not** the same month
 *
 * @param [today00] can be retrieved from [DateTime.getToday00] and passed here in order to
 * reduce resource consumption if desired.
 * */
@Suppress("NOTHING_TO_INLINE", "SpellCheckingInspection")
inline fun DateTime.hhmmElseDate(today00: DateTime = DateTime.getToday00()): String {
    return if (today00.before(this)) {
        DateTime.getFormathmma().format(value)
    } else {
        val dtMonth: String = DateTime.getFormatMMM().format(value)
        val todayMonth: String = DateTime.getFormatMMM().format(today00.value)

        if (dtMonth == todayMonth) {
            DateTime.getFormatEEEdd().format(value)
        } else {
            DateTime.getFormatEEEdd().format(value) + " $dtMonth"
        }
    }
}

@Suppress("NOTHING_TO_INLINE", "SpellCheckingInspection")
inline fun DateTime.chatTimeFormat(
    today00: DateTime = DateTime.getToday00(),
    sixDaysAgo: DateTime = DateTime.getSixDaysAgo()
): String {
    val offset: Int = TimeZone.getDefault().rawOffset
    val dateWithOffset = value.addOffset(TimeSpan(offset.toDouble()))

    return when {
        today00.before(this) -> {
            DateTime.getFormathmma().format(dateWithOffset)
        }
        sixDaysAgo.before(this) -> {
            DateTime.getFormatEEEhmma().format(dateWithOffset)
        }
        else -> {
            DateTime.getFormatddmmmhhmm().format(dateWithOffset)
        }
    }
}

@Suppress("NOTHING_TO_INLINE", "SpellCheckingInspection")
inline fun DateTime.localDateTimeString(format: DateFormat): String {
    val offset: Int = TimeZone.getDefault().rawOffset
    val dateWithOffset = value.addOffset(TimeSpan(offset.toDouble()))
    return format.format(dateWithOffset)
}

@Suppress("NOTHING_TO_INLINE", "SpellCheckingInspection")
inline fun DateTime.invoiceExpirationTimeFormat(): String =
    DateTime.getFormathmma().format(value)

@Suppress("NOTHING_TO_INLINE", "SpellCheckingInspection")
inline fun DateTime.invoicePaymentDateFormat(): String =
    DateTime.getFormatMMMEEEdd().format(value)

@Suppress("NOTHING_TO_INLINE", "SpellCheckingInspection")
inline fun DateTime.eeemmddhmma(): String =
    DateTime.getFormateeemmddhmma().format(value)

inline val DateTime.time: Long
    get() = value.local.unixMillisLong

@Suppress("NOTHING_TO_INLINE")
inline fun DateTime.after(dateTime: DateTime): Boolean =
    value.local.unixMillis > dateTime.value.local.unixMillis

@Suppress("NOTHING_TO_INLINE")
inline fun DateTime.before(dateTime: DateTime): Boolean =
    value.local.unixMillis < dateTime.value.local.unixMillis

@Suppress("NOTHING_TO_INLINE")
inline fun DateTime.getMinutesDifferenceWithDateTime(dateTime: DateTime): Double {
    val diff: Long = this.time - dateTime.time
    val seconds = diff.toDouble() / 1000
    return seconds / 60
}

/**
 * DateTime format from Relay: 2021-02-26T10:48:20.025Z
 *
 * See https://www.datetimeformatter.com/how-to-format-date-time-in-java-7/#examples
 * */
@JvmInline
value class DateTime(val value: com.soywiz.klock.DateTimeTz) {
    companion object {
        private val lock = SynchronizedObject()
        const val UTC = "UTC"

        private const val FORMAT_RELAY = "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'"
        private const val FORMAT_TODAY_00 = "yyyy-MM-dd'T00:00:00.000Z'"
        private const val FORMAT_H_MM_A = "h:mm a"
        private const val FORMAT_EEE_H_MM_A = "EEE h:mm a"
        private const val FORMAT_MMM = "MMM"
        private const val FORMAT_EEE_DD = "EEE dd"
        private const val FORMAT_MMM_EEE_DD = "EEE, MMM dd"
        private const val FORMAT_EEE_MM_DD_H_MM_A = "EEE MMM dd, h:mm a"
        private const val FORMAT_DD_MMM_HH_MM = "dd MMM, HH:mm"
        private const val FORMAT_MMM_DD_YYYY = "MMM dd, yyyy"

        private const val SIX_DAYS_IN_MILLISECONDS = 518_400_000L

        @Volatile
        private var formatRelay: DateFormat? = null
        fun getFormatRelay(): DateFormat =
            formatRelay ?: synchronized(lock) {
                formatRelay ?: DateFormat(FORMAT_RELAY)
                    .also {
                        formatRelay = it
                    }
            }

        /**
         * Returns a string value using [FORMAT_RELAY]
         * */
        fun nowUTC(): String =
            getFormatRelay().format(com.soywiz.klock.DateTime.now())

        @Volatile
        private var formatToday00: DateFormat? = null
        fun getFormatToday00(): DateFormat =
            formatToday00  ?: synchronized(lock) {
                DateFormat(FORMAT_TODAY_00)
                .also {
                    formatToday00 = it
                }
            }

        /**
         * Uses the [getFormatToday00] to create a [DateTime] formatted
         * for the current date, in the local timezone, in local language
         * at 00:00:00.000
         * */
        fun getToday00(): DateTime =
            getFormatToday00()
            .format(
                DateTimeTz.nowLocal()
            )
            .toDateTime()

        /**
         * Create a [DateTime] that is 6 days from the current time
         * */
        fun getSixDaysAgo(): DateTime =
            DateTime(DateTimeTz.fromUnix(getCurrentTimeInMillis() - SIX_DAYS_IN_MILLISECONDS))

        @Volatile
        private var formateeemmddhmma: DateFormat? = null
        @Suppress("SpellCheckingInspection")
        fun getFormateeemmddhmma(): DateFormat =
            formateeemmddhmma ?: synchronized(lock) {
                DateFormat(FORMAT_EEE_MM_DD_H_MM_A)
                .also {
                    formateeemmddhmma = it
                }
            }

        @Volatile
        private var formatddmmmhhmm: DateFormat? = null
        @Suppress("SpellCheckingInspection")
        fun getFormatddmmmhhmm(): DateFormat =
            formatddmmmhhmm ?: synchronized(lock) {
                DateFormat(FORMAT_DD_MMM_HH_MM)
                .also {
                    formatddmmmhhmm = it
                }
            }

        @Volatile
        private var formathmma: DateFormat? = null
        @Suppress("SpellCheckingInspection")
        fun getFormathmma(): DateFormat =
            formathmma ?: synchronized(lock) {
                 DateFormat(FORMAT_H_MM_A)
                    .also {
                        formathmma = it
                    }
            }

        @Volatile
        private var formateeehmma: DateFormat? = null
        @Suppress("SpellCheckingInspection")
        fun getFormatEEEhmma(): DateFormat =
            formateeehmma ?: synchronized(lock) {
                DateFormat(FORMAT_EEE_H_MM_A)
                .also {
                    formateeehmma = it
                }
            }

        @Volatile
        private var formatMMM: DateFormat? = null
        fun getFormatMMM(): DateFormat =
            formatMMM ?: synchronized(lock) {
                DateFormat(FORMAT_MMM)
                .also {
                    formatMMM = it
                }
            }

        @Volatile
        private var formatEEEdd: DateFormat? = null
        fun getFormatEEEdd(): DateFormat =
            formatEEEdd ?: synchronized(lock) {
                 DateFormat(FORMAT_EEE_DD)
                    .also {
                        formatEEEdd = it
                    }
            }

        @Volatile
        private var formatMMMEEEdd: DateFormat? = null
        fun getFormatMMMEEEdd(): DateFormat =
            formatMMMEEEdd ?: synchronized(lock) {
                formatMMMEEEdd ?: DateFormat(FORMAT_MMM_EEE_DD)
                    .also {
                        formatMMMEEEdd = it
                    }
            }

        @Volatile
        private var formatMMMddyyyy: DateFormat? = null
        fun getFormatMMMddyyyy(timeZone: TimezoneOffset = DateTimeTz.nowLocal().local.localOffset): DateFormat =
            formatMMMddyyyy ?: synchronized(lock) {
                DateFormat(FORMAT_MMM_DD_YYYY)
                .also {
                    formatMMMddyyyy = it
                }
            }
    }

    override fun toString(): String {
        return getFormatRelay().format(value)
    }
}
