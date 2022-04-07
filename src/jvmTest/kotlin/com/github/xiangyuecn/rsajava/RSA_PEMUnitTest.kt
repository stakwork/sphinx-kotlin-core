package com.github.xiangyuecn.rsajava

import io.matthewnelson.component.base64.encodeBase64
import java.lang.Exception
import java.nio.charset.Charset
import java.security.KeyPairGenerator
import java.security.SecureRandom
import java.security.Signature
import javax.crypto.Cipher
import kotlin.test.*

class RSA_PEMUnitTest {
    @Test
    fun testing() {
        try {
            val keygen = KeyPairGenerator.getInstance("RSA")
            keygen.initialize(512, SecureRandom())
            val keyPair = keygen.generateKeyPair()
            val pemRawTxt = (""
                    + "-----BEGIN PRIVATE KEY-----"
                    + keyPair.private.encoded.encodeBase64()
                    + "-----END PRIVATE KEY-----")
            //使用PEM PKCS#8文件的文本构造出pem对象
            val pem = RSA_PEM.FromPEM(pemRawTxt)
            val isEqRaw = pem.ToPEM_PKCS8(false).replace("\\r|\\n".toRegex(), "") == pemRawTxt
            //生成PKCS#1和XML
            println("【" + pem.keySize() + "私钥（XML）】：")
            println(pem.ToXML(false))
            println()
            println("【" + pem.keySize() + "私钥（PKCS#1）】：是否和KeyPair生成的相同" + isEqRaw)
            println(pem.ToPEM_PKCS1(false))
            println()
            println("【" + pem.keySize() + "公钥（PKCS#8）】：")
            println(pem.ToPEM_PKCS8(true))
            println()
            val str = "abc内容123"
            //加密内容
            val enc = Cipher.getInstance("RSA")
            enc.init(Cipher.ENCRYPT_MODE, pem.rsaPublicKey)
            val en = enc.doFinal(str.toByteArray(charset("utf-8")))
            println("【加密】：")
            println(en.encodeBase64())

            //解密内容
            val dec = Cipher.getInstance("RSA")
            dec.init(Cipher.DECRYPT_MODE, pem.rsaPrivateKey)
            val de = dec.doFinal(en)
            println("【解密】：")
            println(String(de, Charset.defaultCharset()))


            //私钥签名
            val signature = Signature.getInstance("SHA1WithRSA")
            signature.initSign(pem.rsaPrivateKey)
            signature.update(str.toByteArray(charset("utf-8")))
            val sign = signature.sign()
            println("【SHA1签名】：")
            println("签名：" + sign.encodeBase64())

            //公钥校验
            val signVerify = Signature.getInstance("SHA1WithRSA")
            signVerify.initVerify(pem.rsaPublicKey)
            signVerify.update(str.toByteArray(charset("utf-8")))
            val verify = signVerify.verify(sign)
            println("校验：$verify")
            println()


            //使用PEM PKCS#1构造pem对象
            val pem2 = RSA_PEM.FromPEM(pem.ToPEM_PKCS1(false))
            println("【用PEM新创建的RSA是否和上面的一致】：")
            println("XML：" + (pem2.ToXML(false) == pem.ToXML(false)))
            println("PKCS1：" + (pem2.ToPEM_PKCS1(false) == pem.ToPEM_PKCS1(false)))
            println("PKCS8：" + (pem2.ToPEM_PKCS8(false) == pem.ToPEM_PKCS8(false)))

            //使用XML构造pem对象
            val pem3 = RSA_PEM.FromXML(pem.ToXML(false))
            println("【用XML新创建的RSA是否和上面的一致】：")
            println("XML：" + (pem3.ToXML(false) == pem.ToXML(false)))
            println("PKCS1：" + (pem3.ToPEM_PKCS1(false) == pem.ToPEM_PKCS1(false)))
            println("PKCS8：" + (pem3.ToPEM_PKCS8(false) == pem.ToPEM_PKCS8(false)))


            //--------RSA_PEM验证---------
            //使用PEM全量参数构造pem对象
            val pemX = RSA_PEM(
                pem.Key_Modulus!!,
                pem.Key_Exponent,
                pem.Key_D!!,
                pem.Val_P!!,
                pem.Val_Q!!,
                pem.Val_DP!!,
                pem.Val_DQ!!,
                pem.Val_InverseQ!!
            )
            println("【RSA_PEM是否和原始RSA一致】：")
            println(pem.keySize().toString() + "位")
            println("XML：" + (pemX.ToXML(false) == pem.ToXML(false)))
            println("PKCS1：" + (pemX.ToPEM_PKCS1(false) == pem.ToPEM_PKCS1(false)))
            println("PKCS8：" + (pemX.ToPEM_PKCS8(false) == pem.ToPEM_PKCS8(false)))
            println("仅公钥：")
            println("XML：" + (pemX.ToXML(true) == pem.ToXML(true)))
            println("PKCS1：" + (pemX.ToPEM_PKCS1(true) == pem.ToPEM_PKCS1(true)))
            println("PKCS8：" + (pemX.ToPEM_PKCS8(true) == pem.ToPEM_PKCS8(true)))

            //使用n、e、d构造pem对象
            val pem4 = RSA_PEM(pem.Key_Modulus!!, pem.Key_Exponent!!, pem.Key_D)
            val dec4 = Cipher.getInstance("RSA")
            dec4.init(Cipher.DECRYPT_MODE, pem4.rsaPrivateKey)
            println("【用n、e、d构造解密】")
            println(String(dec4.doFinal(en), Charset.defaultCharset()))
            println()
            println()
            println("【" + pem.keySize() + "私钥（PKCS#8）】：")
            println(pem.ToPEM_PKCS8(false))
            println()
            println("【" + pem.keySize() + "公钥（PKCS#1）】：不常见的公钥格式")
            println(pem.ToPEM_PKCS1(true))
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}