package chat.sphinx.concepts.repository.contact

import chat.sphinx.concepts.network.query.contact.model.PersonDataDto
import chat.sphinx.crypto.common.clazzes.Password
import chat.sphinx.response.LoadResponse
import chat.sphinx.response.Response
import chat.sphinx.response.ResponseError
import chat.sphinx.wrapper.PhotoUrl
import chat.sphinx.wrapper.contact.*
import chat.sphinx.wrapper.dashboard.ContactId
import chat.sphinx.wrapper.dashboard.InviteId
import chat.sphinx.wrapper.invite.Invite
import chat.sphinx.wrapper.lightning.LightningNodePubKey
import chat.sphinx.wrapper.lightning.LightningRouteHint
import chat.sphinx.wrapper.lightning.Sat
import chat.sphinx.wrapper.message.media.MediaType
import chat.sphinx.wrapper.relay.AuthorizationToken
import chat.sphinx.wrapper.relay.RelayUrl
import chat.sphinx.wrapper.relay.RequestSignature
import chat.sphinx.wrapper.relay.TransportToken
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.StateFlow
import okio.Path

/**
 * All [Contact]s are cached to the DB such that a network refresh will update
 * them, and thus proc and [Flow] being collected.
 * */
interface ContactRepository {
    val accountOwner: StateFlow<Contact?>

    fun createContact(
        contactAlias: ContactAlias,
        lightningNodePubKey: LightningNodePubKey,
        lightningRouteHint: LightningRouteHint?,
        contactKey: ContactKey? = null,
        photoUrl: PhotoUrl? = null
    ): Flow<LoadResponse<Any, ResponseError>>

    suspend fun connectToContact(
        contactAlias: ContactAlias,
        lightningNodePubKey: LightningNodePubKey,
        lightningRouteHint: LightningRouteHint?,
        contactKey: ContactKey,
        message: String,
        photoUrl: PhotoUrl?,
        priceToMeet: Sat,
    ): Response<ContactId?, ResponseError>

    val getAllContacts: Flow<List<Contact>>
    fun getContactById(contactId: ContactId): Flow<Contact?>
    fun getContactByPubKey(pubKey: LightningNodePubKey): Flow<Contact?>
    suspend fun getAllContactsByIds(contactIds: List<ContactId>): List<Contact>

    fun getInviteByContactId(contactId: ContactId): Flow<Invite?>
    fun getInviteById(inviteId: InviteId): Flow<Invite?>

    fun createNewInvite(nickname: String, welcomeMessage: String): Flow<LoadResponse<Any, ResponseError>>

    val networkRefreshContacts: Flow<LoadResponse<Boolean, ResponseError>>
    var updatedContactIds: MutableList<ContactId>

    suspend fun deleteContactById(contactId: ContactId): Response<Any, ResponseError>
    suspend fun updateOwnerDeviceId(deviceId: DeviceId): Response<Any, ResponseError>
    suspend fun updateOwnerNameAndKey(name: String, contactKey: Password): Response<Any, ResponseError>
    suspend fun updateOwner(alias: String?, privatePhoto: PrivatePhoto?, tipAmount: Sat?): Response<Any, ResponseError>

    suspend fun updateContact(
        contactId: ContactId,
        alias: ContactAlias?,
        routeHint: LightningRouteHint?
    ): Response<Any, ResponseError>

    suspend fun forceKeyExchange(
        contactId: ContactId,
    )

    // TODO: add chatId to argument to update alias photo
    suspend fun updateProfilePic(
//        chatId: ChatId?,
        path: Path,
        mediaType: MediaType,
        fileName: String,
        contentLength: Long?
    ): Response<Any, ResponseError>

    suspend fun toggleContactBlocked(contact: Contact): Response<Boolean, ResponseError>

    suspend fun setGithubPat(pat: String): Response<Boolean, ResponseError>

    suspend fun getPersonData(
        relayData: Triple<Pair<AuthorizationToken, TransportToken?>, RequestSignature?, RelayUrl>? = null
    ): Flow<LoadResponse<PersonDataDto, ResponseError>>


    // V2 methods

    suspend fun createOwner(
        okKey: String,
        routeHint: String,
        shortChannelId: String,
        ownerAlias: String
    )

    suspend fun createNewContact(contact: NewContact)
    suspend fun getNewContactIndex(): Flow<ContactId?>
    suspend fun updateOwnerAlias(alias: ContactAlias)


}
