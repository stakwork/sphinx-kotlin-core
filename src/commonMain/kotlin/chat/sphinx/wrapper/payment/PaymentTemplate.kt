package chat.sphinx.wrapper.payment

import java.io.File

class PaymentTemplate(
    val muid: String,
    val width: Int,
    val height: Int,
    val token: String
) {

    var localFile: File? = null

    fun getTemplateUrl(mediaHost: String): String {
        return "https://$mediaHost/template/$muid"
    }

    fun getDimensions(): String {
        return "${width}x${height}"
    }

    fun getMediaType(): String {
        return "image/png"
    }

}