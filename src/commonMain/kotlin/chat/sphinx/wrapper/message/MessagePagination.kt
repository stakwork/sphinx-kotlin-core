package chat.sphinx.wrapper.message

import chat.sphinx.wrapper.DateTime
import com.soywiz.klock.DateFormat
import kotlinx.atomicfu.locks.SynchronizedObject
import kotlinx.atomicfu.locks.synchronized
import kotlin.jvm.JvmInline
import kotlin.jvm.Volatile

@JvmInline
value class MessagePagination private constructor(val value: String) {

    companion object {

        const val FORMAT_PAGINATION_PERCENT_ESCAPED = "yyyy-MM-dd'%20'HH:mm:ss"
        private val lock = SynchronizedObject()
        @Volatile
        private var formatPercentEscapedPagination: DateFormat? = null

        fun getFormatPaginationPercentEscaped(): DateFormat =
            formatPercentEscapedPagination ?: synchronized(lock) {
                formatPercentEscapedPagination ?: DateFormat(
                    FORMAT_PAGINATION_PERCENT_ESCAPED
                ).also {
//                        it.timeZone = TimeZone.getTimeZone(DateTime.UTC)
                        formatPercentEscapedPagination = it
                }
            }

        @Throws(IllegalArgumentException::class)
        fun instantiate(
            limit: Int,
            offset: Int,
            date: DateTime?
        ): MessagePagination {
            require(limit > 0) {
                "MessagePagination limit must be greater than 0"
            }
            require(offset >= 0) {
                "MessagePagination offset must be greater than or equal to 0"
            }

            val dateString = date?.let {
                "&date=${getFormatPaginationPercentEscaped().format(it.value)}"
            } ?: ""

            return MessagePagination("?limit=$limit&offset=${offset}$dateString")
        }

    }
}
