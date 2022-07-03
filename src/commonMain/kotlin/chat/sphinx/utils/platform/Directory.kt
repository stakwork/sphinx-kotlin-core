package chat.sphinx.utils.platform

import okio.Path

expect fun getUserHomeDirectory(): Path

expect fun getSphinxDirectory(): Path