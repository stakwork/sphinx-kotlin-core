package chat.sphinx.wrapper.mqtt

sealed class ConnectManagerError {

    object GenerateXPubError: ConnectManagerError()
    object GenerateMnemonicError: ConnectManagerError()
    object ProcessInviteError: ConnectManagerError()
    object SubscribeOwnerError: ConnectManagerError()
    data class MqttConnectError(val error: String?): ConnectManagerError()
    object MqttClientError: ConnectManagerError()
    data class MqttInitError(val logs: String) : ConnectManagerError()
    object SendMessageError: ConnectManagerError()
    object DeleteMessageError: ConnectManagerError()
    object JoinTribeError: ConnectManagerError()
    object CreateTribeError: ConnectManagerError()
    object CreateInviteError: ConnectManagerError()
    object DeleteInviteError: ConnectManagerError()
    object ConcatNodesError: ConnectManagerError()
    object CreateInvoiceError: ConnectManagerError()
    object ServerPubKeyError: ConnectManagerError()
    object PayContactInvoiceError: ConnectManagerError()
    object PayInvoiceError: ConnectManagerError()
    object PaymentHashError: ConnectManagerError()
    object ListTribeMembersError: ConnectManagerError()
    object FetchMessageError: ConnectManagerError()
    object FetchFirstMessageError: ConnectManagerError()
    object MessageCountError: ConnectManagerError()
    object SetDeviceIdError: ConnectManagerError()
    object MediaTokenError: ConnectManagerError()
    object ReadMessageError: ConnectManagerError()
    object GetReadMessagesError: ConnectManagerError()
    object SignBytesError: ConnectManagerError()
    object SetMuteError: ConnectManagerError()
    object LoadTransactionsError: ConnectManagerError()
    object SendKeySendError: ConnectManagerError()
    object SendBoostError: ConnectManagerError()
    object MqttReconnectError: ConnectManagerError()
    object XPubOrSignError: ConnectManagerError()
}