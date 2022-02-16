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
import kotlinx.coroutines.flow.StateFlow

interface RepositoryDashboard {
    suspend fun getAccountBalance(): StateFlow<NodeBalance?>

    val getAllChats: Flow<List<Chat>>
    val getAllContactChats: Flow<List<Chat>>
    val getAllTribeChats: Flow<List<Chat>>
    fun getConversationByContactId(contactId: ContactId): Flow<Chat?>

    fun getUnseenMessagesByChatId(chatId: ChatId): Flow<Long?>
    fun getUnseenActiveConversationMessagesCount(): Flow<Long?>
    fun getUnseenTribeMessagesCount(): Flow<Long?>

    val accountOwner: StateFlow<Contact?>
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

    val networkRefreshBalance: Flow<LoadResponse<Boolean, ResponseError>>
    val networkRefreshContacts: Flow<LoadResponse<Boolean, ResponseError>>
    val networkRefreshLatestContacts: Flow<LoadResponse<Boolean, ResponseError>>
    val networkRefreshMessages: Flow<LoadResponse<RestoreProgress, ResponseError>>

    suspend fun didCancelRestore()
}