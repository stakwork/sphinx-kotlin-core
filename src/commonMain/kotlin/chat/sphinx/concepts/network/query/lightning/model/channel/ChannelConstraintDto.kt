package chat.sphinx.concepts.network.query.lightning.model.channel

import kotlinx.serialization.Serializable

@Serializable
data class ChannelConstraintDto(
    val csv_delay: Int,
    val chan_reserve_sat: Long,
    val dust_limit_sat: Long,
    val max_pending_amt_msat: Long,
    val min_htlc_msat: Long,
    val max_accepted_htlcs: Int,
)
