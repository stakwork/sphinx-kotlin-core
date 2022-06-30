package chat.sphinx.utils

import androidx.annotation.ColorInt
import androidx.annotation.Size
import chat.sphinx.concepts.coroutines.CoroutineDispatchers
import com.russhwolf.settings.Settings
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext

internal data class ColorHolder(val key: String, val color: String)

class UserColorsHelper(
    private val dispatchers: CoroutineDispatchers
){
    private val settings: Settings = createPlatformSettings()

    companion object {
        private const val CACHE_SIZE = 100

        private val colors: MutableList<ColorHolder> = ArrayList(CACHE_SIZE)
        private val lock = Mutex()
        private var counter = 0
    }

    suspend fun getColorIntForKey(
        colorKey: String,
        randomHexColorCode: String
    ): Int {
        return lock.withLock {

            val cachedColor: String? = withContext(dispatchers.default) {
                for (color in colors) {
                    if (color.key == colorKey) {
                        return@withContext color.color
                    }
                }

                null
            }

            if (cachedColor != null) {
                return parseColor("#$cachedColor")
            }

            val colorHexCode: String = withContext(dispatchers.io) {
                settings.getStringOrNull(key = colorKey) ?: run {
                    settings.putString(key = colorKey, value = randomHexColorCode)

                    randomHexColorCode
                }
            }

            updateColorHolderCache(ColorHolder(colorKey, colorHexCode))

            parseColor("#$colorHexCode")
        }
    }

    private fun updateColorHolderCache(colorHolder: ColorHolder) {
        colors.add(counter, colorHolder)

        if (counter < CACHE_SIZE - 1 /* last index */) {
            counter++
        } else {
            counter = 0
        }
    }

    @ColorInt
    fun parseColor(@Size(min = 1) colorString: String): Int {
        if (colorString[0] == '#') { // Use a long to avoid rollovers on #ffXXXXXX
            var color = colorString.substring(1).toLong(16)
            if (colorString.length == 7) { // Set the alpha value
                color = color or -0x1000000
            } else require(colorString.length == 9) { "Unknown color" }
            return color.toInt()
        }
        throw IllegalArgumentException("Unknown color")
    }

}