package chat.sphinx.wrapper.util
import java.time.Instant
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.format.TextStyle
import java.util.Locale

@Suppress("NOTHING_TO_INLINE")
inline fun String.getInitials(charLimit: Int = 2): String {
    val sb = StringBuilder()
    this.split(' ').let { splits ->
        for ((index, split) in splits.withIndex()) {
            if (index < charLimit) {
                sb.append(split.firstOrNull() ?: "")
            } else {
                break
            }
        }
    }
    return sb.toString()
}

//DateTime
@Suppress("NOTHING_TO_INLINE")
inline fun Long.getHHMMSSString(): String {
    val hours: Int
    val minutes: Int
    var seconds: Int = this.toInt() / 1000

    hours = seconds / 3600
    minutes = seconds / 60 % 60
    seconds %= 60

    val hoursString = if (hours < 10) "0${hours}" else "$hours"
    val minutesString = if (minutes < 10) "0${minutes}" else "$minutes"
    val secondsString = if (seconds < 10) "0${seconds}" else "$seconds"

    return "$hoursString:$minutesString:$secondsString"
}

@Suppress("NOTHING_TO_INLINE")
inline fun Long.getHHMMString(): String {
    val minutes = this / 1000 / 60
    val seconds = this / 1000 % 60

    return "${minutes.toString().padStart(2, '0')}:${seconds.toString().padStart(2, '0')}"
}

@Suppress("NOTHING_TO_INLINE")
inline fun Long.toFormattedDate(): String {
    // Convert the Long timestamp (in milliseconds) to LocalDateTime
    val dateTime = LocalDateTime.ofInstant(Instant.ofEpochMilli(this), ZoneId.systemDefault())

    val day = dateTime.dayOfMonth
    val month = dateTime.month.getDisplayName(TextStyle.FULL, Locale.ENGLISH)
    val year = dateTime.year

    // Determine the appropriate suffix for the day
    val daySuffix = when {
        day % 10 == 1 && day % 100 != 11 -> "st"
        day % 10 == 2 && day % 100 != 12 -> "nd"
        day % 10 == 3 && day % 100 != 13 -> "rd"
        else -> "th"
    }

    return "$month $day$daySuffix $year"
}