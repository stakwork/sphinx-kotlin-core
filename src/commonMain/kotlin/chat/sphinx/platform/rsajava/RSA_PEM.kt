package chat.sphinx.platform.rsajava

import kotlin.jvm.JvmField

expect class RSA_PEM {
    /**modulus 模数，公钥、私钥都有 */
    @JvmField
    var Key_Modulus: ByteArray?

    /**publicExponent 公钥指数，公钥、私钥都有 */
    @JvmField
    var Key_Exponent: ByteArray?

    /**privateExponent 私钥指数，只有私钥的时候才有 */
    var Key_D: ByteArray?
    //以下参数只有私钥才有 https://docs.microsoft.com/zh-cn/dotnet/api/system.security.cryptography.rsaparameters?redirectedfrom=MSDN&view=netframework-4.8
    /**prime1 */
    @JvmField
    var Val_P: ByteArray?

    /**prime2 */
    @JvmField
    var Val_Q: ByteArray?

    /**exponent1 */
    @JvmField
    var Val_DP: ByteArray?

    /**exponent2 */
    @JvmField
    var Val_DQ: ByteArray?

    /**coefficient */
    @JvmField
    var Val_InverseQ: ByteArray?

    companion object {
        @Throws(Exception::class)
        fun FromPEM(base64: CharArray, privateKey: Boolean): RSA_PEM
    }
}