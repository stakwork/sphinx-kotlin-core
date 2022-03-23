package chat.sphinx.utils

import chat.sphinx.concepts.coroutines.CoroutineDispatchers
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers

class SphinxDispatchers(
    override val default: CoroutineDispatcher,
    override val io: CoroutineDispatcher,
    override val main: CoroutineDispatcher,
    override val mainImmediate: CoroutineDispatcher,
    override val unconfined: CoroutineDispatcher
): CoroutineDispatchers {
    constructor(): this(
        Dispatchers.Default,
        Dispatchers.Default, // TODO: Get the IO Dispatcher...
        Dispatchers.Main,
        Dispatchers.Main.immediate,
        Dispatchers.Unconfined
    )
}