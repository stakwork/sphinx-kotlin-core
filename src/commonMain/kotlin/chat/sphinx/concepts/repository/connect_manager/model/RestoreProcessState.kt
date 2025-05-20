package chat.sphinx.concepts.repository.connect_manager.model

import chat.sphinx.wrapper.mqtt.MsgsCounts

sealed class RestoreProcessState{
    data class MessagesCounts(val msgsCounts: MsgsCounts) : RestoreProcessState()
    object RestoreMessages : RestoreProcessState()

}
