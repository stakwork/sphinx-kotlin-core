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
import kotlinx.serialization.PolymorphicSerializer

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
            responseJsonSerializer = GetContactsRelayResponse.serializer(),
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
                responseJsonSerializer = GetContactsRelayResponse.serializer(),
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
            responseJsonSerializer = GetLatestContactsRelayResponse.serializer(),
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
            responseJsonSerializer = GetTribeMembersRelayResponse.serializer(),
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
            responseJsonSerializer = ContactRelayResponse.serializer(),
            relayEndpoint = "/contacts/${contactId.value}",
            requestBodyPair = Pair(
                putContactDto,
                PutContactDto.serializer()
            ),
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
            responseJsonSerializer = ContactRelayResponse.serializer(),
            relayEndpoint = endpoint,
            Pair(
                mapOf(Pair("", "")),
                PolymorphicSerializer(Any::class)
            ),
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
            responseJsonSerializer = GenerateTokenRelayResponse.serializer(),
            relayEndpoint = ENDPOINT_GENERATE_TOKEN,
            requestBodyPair = Pair(
                mapOf(
                    Pair("token", token.value),
                    Pair("password", password),
                    Pair("pubkey", pubkey),
                ),
                PolymorphicSerializer(Any::class)
            ),
            relayUrl = relayUrl
        )
    }

    override fun createContact(
        postContactDto: PostContactDto,
        relayData: Pair<AuthorizationToken, RelayUrl>?
    ): Flow<LoadResponse<ContactDto, ResponseError>> =
        networkRelayCall.relayPost(
            responseJsonSerializer = ContactRelayResponse.serializer(),
            relayEndpoint = ENDPOINT_CONTACTS,
            requestBodyPair = Pair(
                postContactDto,
                PostContactDto.serializer()
            ),
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
            responseJsonSerializer = DeleteContactRelayResponse.serializer(),
            "/contacts/${contactId.value}",
//            requestBodyPair = null
        ).collect { loadResponse ->
            if (loadResponse is Response.Error<*>) {
                response = loadResponse as Response<Any, ResponseError>
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
            responseJsonSerializer = CreateInviteRelayResponse.serializer(),
            relayEndpoint = ENDPOINT_CREATE_INVITE,
            requestBodyPair = Pair(
                mapOf(
                    Pair("nickname", nickname),
                    Pair("welcome_message", welcomeMessage),
                ),
                PolymorphicSerializer(Any::class)
            ),
            relayData = relayData
        )
}
