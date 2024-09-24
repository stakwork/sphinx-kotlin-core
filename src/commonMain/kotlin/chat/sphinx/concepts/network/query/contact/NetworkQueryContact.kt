package chat.sphinx.concepts.network.query.contact

import chat.sphinx.concepts.network.query.contact.model.*
import chat.sphinx.response.LoadResponse
import chat.sphinx.response.Response
import chat.sphinx.response.ResponseError
import chat.sphinx.wrapper.DateTime
import chat.sphinx.wrapper.contact.Blocked
import chat.sphinx.wrapper.dashboard.ChatId
import chat.sphinx.wrapper.dashboard.ContactId
import chat.sphinx.wrapper.lightning.LightningNodePubKey
import chat.sphinx.wrapper.relay.AuthorizationToken
import chat.sphinx.wrapper.relay.RelayUrl
import chat.sphinx.wrapper.relay.RequestSignature
import chat.sphinx.wrapper.relay.TransportToken
import kotlinx.coroutines.flow.Flow

abstract class NetworkQueryContact {

    ///////////
    /// GET ///
    ///////////
    abstract fun getContacts(
        relayData: Triple<Pair<AuthorizationToken, TransportToken?>, RequestSignature?, RelayUrl>? = null
    ): Flow<LoadResponse<GetContactsResponse, ResponseError>>

    abstract fun getLatestContacts(
        date: DateTime?,
        relayData: Triple<Pair<AuthorizationToken, TransportToken?>, RequestSignature?, RelayUrl>? = null
    ): Flow<LoadResponse<GetLatestContactsResponse, ResponseError>>

    abstract fun getTribeMembers(
        chatId: ChatId,
        offset: Int = 0,
        limit: Int = 50,
        relayData: Triple<Pair<AuthorizationToken, TransportToken?>, RequestSignature?, RelayUrl>? = null
    ): Flow<LoadResponse<GetTribeMembersResponse, ResponseError>>

    ///////////
    /// PUT ///
    ///////////
    abstract fun updateContact(
        contactId: ContactId,
        putContactDto: PutContactDto,
        relayData: Triple<Pair<AuthorizationToken, TransportToken?>, RequestSignature?, RelayUrl>? = null
    ): Flow<LoadResponse<ContactDto, ResponseError>>

    abstract fun exchangeKeys(
        contactId: ContactId,
        relayData: Triple<Pair<AuthorizationToken, TransportToken?>, RequestSignature?, RelayUrl>? = null
    ): Flow<LoadResponse<ContactDto, ResponseError>>

    abstract fun toggleBlockedContact(
        contactId: ContactId,
        blocked: Blocked,
        relayData: Triple<Pair<AuthorizationToken, TransportToken?>, RequestSignature?, RelayUrl>? = null
    ): Flow<LoadResponse<ContactDto, ResponseError>>

    ////////////
    /// POST ///
    ////////////
    abstract fun generateToken(
        password: String?,
        publicKey: String? = null,
        relayData: Triple<Pair<AuthorizationToken, TransportToken?>, RequestSignature?, RelayUrl>? = null
    ): Flow<LoadResponse<GenerateTokenResponse, ResponseError>>

    abstract fun generateToken(
        relayUrl: RelayUrl,
        token: AuthorizationToken,
        password: String?,
        pubkey: String? = null
    ): Flow<LoadResponse<GenerateTokenResponse, ResponseError>>

    abstract fun createContact(
        postContactDto: PostContactDto,
        relayData: Triple<Pair<AuthorizationToken, TransportToken?>, RequestSignature?, RelayUrl>? = null
    ): Flow<LoadResponse<ContactDto, ResponseError>>

    abstract fun generateGithubPAT(
        patDto: GithubPATDto,
        relayData: Triple<Pair<AuthorizationToken, TransportToken?>, RequestSignature?, RelayUrl>? = null
    ): Flow<LoadResponse<Any, ResponseError>>

    //////////////
    /// DELETE ///
    //////////////
    abstract suspend fun deleteContact(
        contactId: ContactId,
        relayData: Triple<Pair<AuthorizationToken, TransportToken?>, RequestSignature?, RelayUrl>? = null,
    ): Response<Any, ResponseError>

    abstract fun createNewInvite(
        nickname: String,
        welcomeMessage: String,
        relayData: Triple<Pair<AuthorizationToken, TransportToken?>, RequestSignature?, RelayUrl>? = null
    ): Flow<LoadResponse<ContactDto, ResponseError>>

    abstract fun getPersonData(
        relayData: Triple<Pair<AuthorizationToken, TransportToken?>, RequestSignature?, RelayUrl>? = null
    ): Flow<LoadResponse<PersonDataDto, ResponseError>>

    //    app.post('/contacts/:id/keys', contacts.exchangeKeys)
    //    app.post('/contacts', contacts.createContact)

    // V2 methods

    abstract fun hasAdmin(
        url: RelayUrl
    ): Flow<LoadResponse<Any, ResponseError>>

    abstract fun getAccountConfig(isProductionEnvironment: Boolean): Flow<LoadResponse<AccountConfigV2Response, ResponseError>>

    abstract fun getNodes(
        routerUrl: String
    ): Flow<LoadResponse<String, ResponseError>>

    abstract fun getRoutingNodes(
        routerUrl: String,
        lightningNodePubKey: LightningNodePubKey,
        milliSats: Long
    ): Flow<LoadResponse<String, ResponseError>>

}
