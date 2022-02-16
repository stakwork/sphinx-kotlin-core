package chat.sphinx.wrapper.lightning

data class NodeBalance(
    val reserve: Sat,
    val fullBalance: Sat,
    val balance: Sat,
    val pendingOpenBalance: Sat,
)
