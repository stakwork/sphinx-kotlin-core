package chat.sphinx.concepts.repository.dashboard

import chat.sphinx.response.LoadResponse
import chat.sphinx.response.Response
import chat.sphinx.response.ResponseError
import chat.sphinx.wrapper.chat.Chat
import chat.sphinx.wrapper.contact.Contact
import chat.sphinx.wrapper.dashboard.ChatId
import chat.sphinx.wrapper.dashboard.ContactId
import chat.sphinx.wrapper.dashboard.InviteId
import chat.sphinx.wrapper.dashboard.RestoreProgress
import chat.sphinx.wrapper.feed.Feed
import chat.sphinx.wrapper.feed.FeedType
import chat.sphinx.wrapper.invite.Invite
import chat.sphinx.wrapper.lightning.NodeBalance
import chat.sphinx.wrapper.message.Message
import chat.sphinx.wrapper.message.MessageId
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

interface RepositoryDashboard {
    suspend fun getAccountBalanceStateFlow(): StateFlow<NodeBalance?>

    suspend fun getAllChats(): List<Chat>
    val getAllChatsFlow: Flow<List<Chat>>
    val getAllContactChatsFlow: Flow<List<Chat>>
    val getAllTribeChatsFlow: Flow<List<Chat>>
    fun getConversationByContactIdFlow(contactId: ContactId): Flow<Chat?>

    fun getUnseenMessagesByChatId(chatId: ChatId): Flow<Long?>
    fun getUnseenMentionsByChatId(chatId: ChatId): Flow<Long?>
    fun getUnseenActiveConversationMessagesCount(): Flow<Long?>
    fun getUnseenTribeMessagesCount(): Flow<Long?>

    val getAllNotBlockedContacts: Flow<List<Contact>>
    val getAllInvites: Flow<List<Invite>>
    fun getContactById(contactId: ContactId): Flow<Contact?>
    var updatedContactIds: MutableList<ContactId>

    fun getMessageById(messageId: MessageId): Flow<Message?>
    fun getInviteById(inviteId: InviteId): Flow<Invite?>

    suspend fun payForInvite(invite: Invite)
    suspend fun deleteInvite(invite: Invite): Response<Any, ResponseError>

    fun getAllFeedsOfType(feedType: FeedType): Flow<List<Feed>>
    fun getAllFeeds(): Flow<List<Feed>>

    suspend fun authorizeExternal(
        relayUrl: String,
        host: String,
        challenge: String
    ): Response<Boolean, ResponseError>

    suspend fun savePeopleProfile(
        body: String
    ): Response<Boolean, ResponseError>

    suspend fun deletePeopleProfile(
        body: String
    ): Response<Boolean, ResponseError>

    suspend fun redeemBadgeToken(
        body: String
    ): Response<Boolean, ResponseError>

    val networkRefreshBalance: MutableStateFlow<Long?>
    val networkRefreshContacts: Flow<LoadResponse<Boolean, ResponseError>>
    val networkRefreshLatestContacts: Flow<LoadResponse<RestoreProgress, ResponseError>>
    val networkRefreshMessages: Flow<LoadResponse<RestoreProgress, ResponseError>>

    suspend fun didCancelRestore()

}