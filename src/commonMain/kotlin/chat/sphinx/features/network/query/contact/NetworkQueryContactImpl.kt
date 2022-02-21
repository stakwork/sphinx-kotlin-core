package chat.sphinx.features.network.query.contact

import chat.sphinx.concepts.network.query.contact.NetworkQueryContact
import chat.sphinx.concepts.network.query.contact.model.*
import chat.sphinx.concepts.network.relay_call.NetworkRelayCall
import chat.sphinx.features.network.query.contact.model.*
import chat.sphinx.response.LoadResponse
import chat.sphinx.response.Response
import chat.sphinx.response.ResponseError
import chat.sphinx.wrapper.DateTime
import chat.sphinx.wrapper.contact.Blocked
import chat.sphinx.wrapper.contact.isTrue
import chat.sphinx.wrapper.dashboard.ChatId
import chat.sphinx.wrapper.dashboard.ContactId
import chat.sphinx.wrapper.message.MessagePagination
import chat.sphinx.wrapper.relay.AuthorizationToken
import chat.sphinx.wrapper.relay.RelayUrl
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.collect

class NetworkQueryContactImpl(
    private val networkRelayCall: NetworkRelayCall,
): NetworkQueryContact() {

    companion object {
        private const val ENDPOINT_CONTACTS = "/contacts"
        private const val ENDPOINT_LATEST_CONTACTS = "/latest_contacts"
        private const val ENDPOINT_DELETE_CONTACT = "/contacts/%d"
        private const val ENDPOINT_TRIBE_MEMBERS = "/contacts/%d"
        private const val ENDPOINT_GENERATE_TOKEN = "/contacts/tokens"

        private const val ENDPOINT_CREATE_INVITE = "/invites"
        private const val HUB_URL = "https://hub.sphinx.chat"

        private const val ENDPOINT_BLOCK_CONTACT = "/%s/%d"
        private const val BLOCK_CONTACT = "block"
        private const val UN_BLOCK_CONTACT = "unblock"
    }

    ///////////
    /// GET ///
    ///////////
    private val getContactsFlowNullData: Flow<LoadResponse<GetContactsResponse, ResponseError>> by lazy {
        networkRelayCall.relayGet(
            responseJsonClass = GetContactsRelayResponse::class.java,
            relayEndpoint = "$ENDPOINT_CONTACTS?from_group=false",
            relayData = null,
            useExtendedNetworkCallClient = true,
        )
    }

    override fun getContacts(
        relayData: Pair<AuthorizationToken, RelayUrl>?
    ): Flow<LoadResponse<GetContactsResponse, ResponseError>> =
        if (relayData == null) {
            getContactsFlowNullData
        } else {
            networkRelayCall.relayGet(
                responseJsonClass = GetContactsRelayResponse::class.java,
                relayEndpoint = "$ENDPOINT_CONTACTS?from_group=false",
                relayData = relayData,
                useExtendedNetworkCallClient = true,
            )
        }

    override fun getLatestContacts(
        date: DateTime?,
        relayData: Pair<AuthorizationToken, RelayUrl>?
    ): Flow<LoadResponse<GetLatestContactsResponse, ResponseError>> =
        networkRelayCall.relayGet(
            responseJsonClass = GetLatestContactsRelayResponse::class.java,
            relayEndpoint = if (date != null) {
                "$ENDPOINT_LATEST_CONTACTS?date=${MessagePagination.getFormatPaginationPercentEscaped().format(date?.value)}"
            } else {
                ENDPOINT_LATEST_CONTACTS
            },
            relayData = relayData,
            useExtendedNetworkCallClient = true,
        )

    override fun getTribeMembers(
        chatId: ChatId,
        offset: Int,
        limit: Int,
        relayData: Pair<AuthorizationToken, RelayUrl>?
    ): Flow<LoadResponse<GetTribeMembersResponse, ResponseError>> =
        networkRelayCall.relayGet(
            responseJsonClass = GetTribeMembersRelayResponse::class.java,
            relayEndpoint = "/contacts/${chatId.value}?offset=$offset&limit=$limit",
            relayData = relayData
        )

    ///////////
    /// PUT ///
    ///////////
    override fun updateContact(
        contactId: ContactId,
        putContactDto: PutContactDto,
        relayData: Pair<AuthorizationToken, RelayUrl>?
    ): Flow<LoadResponse<ContactDto, ResponseError>> =
        networkRelayCall.relayPut(
            responseJsonClass = ContactRelayResponse::class.java,
            relayEndpoint = "/contacts/${contactId.value}",
            requestBodyJsonClass = PutContactDto::class.java,
            requestBody = putContactDto,
            relayData = relayData,
        )

    override fun toggleBlockedContact(
        contactId: ContactId,
        blocked: Blocked,
        relayData: Pair<AuthorizationToken, RelayUrl>?
    ): Flow<LoadResponse<ContactDto, ResponseError>> =
        toggleBlockedContactImpl(
            endpoint = "/${if (blocked.isTrue()) UN_BLOCK_CONTACT else BLOCK_CONTACT}/${contactId.value}",
            relayData = relayData
        )

    private fun toggleBlockedContactImpl(
        endpoint: String,
        relayData: Pair<AuthorizationToken, RelayUrl>?
    ): Flow<LoadResponse<ContactDto, ResponseError>> =
        networkRelayCall.relayPut(
            responseJsonClass = ContactRelayResponse::class.java,
            relayEndpoint = endpoint,
            requestBodyJsonClass = Map::class.java,
            requestBody = mapOf(Pair("", "")),
            relayData = relayData
        )

    ////////////
    /// POST ///
    ////////////
//    app.post('/contacts/:id/keys', contacts.exchangeKeys)
//    app.post('/contacts', contacts.createContact)

    override fun generateToken(
        relayUrl: RelayUrl,
        token: AuthorizationToken,
        password: String?,
        pubkey: String?
    ): Flow<LoadResponse<GenerateTokenResponse, ResponseError>> {
        return networkRelayCall.relayUnauthenticatedPost(
            responseJsonClass = GenerateTokenRelayResponse::class.java,
            relayEndpoint = ENDPOINT_GENERATE_TOKEN,
            requestBodyJsonClass = Map::class.java,
            requestBody = mapOf(
                Pair("token", token.value),
                Pair("password", password),
                Pair("pubkey", pubkey),
            ),
            relayUrl = relayUrl
        )
    }

    override fun createContact(
        postContactDto: PostContactDto,
        relayData: Pair<AuthorizationToken, RelayUrl>?
    ): Flow<LoadResponse<ContactDto, ResponseError>> =
        networkRelayCall.relayPost(
            responseJsonClass = ContactRelayResponse::class.java,
            relayEndpoint = ENDPOINT_CONTACTS,
            requestBodyJsonClass = PostContactDto::class.java,
            requestBody = postContactDto,
            relayData = relayData
        )

    //////////////
    /// DELETE ///
    //////////////
    override suspend fun deleteContact(
        contactId: ContactId,
        relayData: Pair<AuthorizationToken, RelayUrl>?
    ): Response<Any, ResponseError> {

        var response: Response<Any, ResponseError> = Response.Success(true)

        networkRelayCall.relayDelete(
            DeleteContactRelayResponse::class.java,
            "/contacts/${contactId.value}",
            requestBody = null
        ).collect { loadResponse ->
            if (loadResponse is Response.Error<*>) {
                response = loadResponse
            }
        }

        return response
    }

    override fun createNewInvite(
        nickname: String,
        welcomeMessage: String,
        relayData: Pair<AuthorizationToken, RelayUrl>?
    ): Flow<LoadResponse<ContactDto, ResponseError>> =
        networkRelayCall.relayPost(
            responseJsonClass = CreateInviteRelayResponse::class.java,
            relayEndpoint = ENDPOINT_CREATE_INVITE,
            requestBodyJsonClass = Map::class.java,
            requestBody = mapOf(
                Pair("nickname", nickname),
                Pair("welcome_message", welcomeMessage),
            ),
            relayData = relayData
        )
}
