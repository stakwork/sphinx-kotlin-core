/*
* MIT License
*
* Copyright (c) 2020 高坚果
*
* Permission is hereby granted, free of charge, to any person obtaining a copy
* of this software and associated documentation files (the "Software"), to deal
* in the Software without restriction, including without limitation the rights
* to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
* copies of the Software, and to permit persons to whom the Software is
* furnished to do so, subject to the following conditions:
*
* The above copyright notice and this permission notice shall be included in all
* copies or substantial portions of the Software.
*
* THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
* IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
* FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
* AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
* LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
* OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
* SOFTWARE.
* */
package chat.sphinx.platform.rsajava

import chat.sphinx.crypto.common.extensions.toByteArray
import com.soywiz.krypto.encoding.base64
import io.ktor.util.*
import java.io.ByteArrayOutputStream
import java.lang.Exception
import java.lang.RuntimeException
import java.lang.StringBuilder
import java.math.BigInteger
import java.nio.charset.Charset
import java.security.KeyFactory
import java.security.interfaces.RSAPrivateKey
import java.security.interfaces.RSAPublicKey
import java.security.spec.RSAPrivateKeySpec
import java.security.spec.RSAPublicKeySpec
import java.util.regex.Pattern
import kotlin.experimental.and
import kotlin.text.toCharArray

/**
 * RSA PEM格式秘钥对的解析和导出
 *
 * GitHub:https://github.com/xiangyuecn/RSA-java
 *
 * https://github.com/xiangyuecn/RSA-java/blob/master/RSA_PEM.java
 * 移植自：https://github.com/xiangyuecn/RSA-csharp/blob/master/RSA_PEM.cs
 */
actual class RSA_PEM {
    /**modulus 模数，公钥、私钥都有 */
    @JvmField
    actual var Key_Modulus: ByteArray? = null

    /**publicExponent 公钥指数，公钥、私钥都有 */
    @JvmField
    actual var Key_Exponent: ByteArray? = null

    /**privateExponent 私钥指数，只有私钥的时候才有 */
    @JvmField
    actual var Key_D: ByteArray? = null
    //以下参数只有私钥才有 https://docs.microsoft.com/zh-cn/dotnet/api/system.security.cryptography.rsaparameters?redirectedfrom=MSDN&view=netframework-4.8
    /**prime1 */
    @JvmField
    actual var Val_P: ByteArray? = null

    /**prime2 */
    @JvmField
    actual var Val_Q: ByteArray? = null

    /**exponent1 */
    @JvmField
    actual var Val_DP: ByteArray? = null

    /**exponent2 */
    @JvmField
    actual var Val_DQ: ByteArray? = null

    /**coefficient */
    @JvmField
    actual var Val_InverseQ: ByteArray? = null

    private constructor() {}

    /***
     * 通过公钥和私钥构造一个PEM
     * @param publicKey 必须提供公钥
     * @param privateKeyOrNull 私钥可以不提供，导出的PEM就只包含公钥
     */
    constructor(publicKey: RSAPublicKey, privateKeyOrNull: RSAPrivateKey?) : this(
        BigB(publicKey.modulus),
        BigB(publicKey.publicExponent),
        if (privateKeyOrNull == null) null else BigB(privateKeyOrNull.privateExponent)
    ) {
    }

    /***
     * 通过全量的PEM字段数据构造一个PEM，除了模数modulus和公钥指数exponent必须提供外，其他私钥指数信息要么全部提供，要么全部不提供（导出的PEM就只包含公钥）
     * 注意：所有参数首字节如果是0，必须先去掉
     */
    constructor(
        modulus: ByteArray,
        exponent: ByteArray?,
        d: ByteArray,
        p: ByteArray,
        q: ByteArray,
        dp: ByteArray,
        dq: ByteArray,
        inverseQ: ByteArray
    ) {
        Key_Modulus = modulus
        Key_Exponent = exponent
        Key_D = BigL(d, modulus.size)
        val keyLen = modulus.size / 2
        Val_P = BigL(p, keyLen)
        Val_Q = BigL(q, keyLen)
        Val_DP = BigL(dp, keyLen)
        Val_DQ = BigL(dq, keyLen)
        Val_InverseQ = BigL(inverseQ, keyLen)
    }

    /***
     * 通过公钥指数和私钥指数构造一个PEM，会反推计算出P、Q但和原始生成密钥的P、Q极小可能相同
     * 注意：所有参数首字节如果是0，必须先去掉
     * @param modulus 必须提供模数
     * @param exponent 必须提供公钥指数
     * @param dOrNull 私钥指数可以不提供，导出的PEM就只包含公钥
     */
    constructor(modulus: ByteArray?, exponent: ByteArray?, dOrNull: ByteArray?) {
        Key_Modulus = modulus //modulus
        Key_Exponent = exponent //publicExponent
        if (dOrNull != null) {
            Key_D = BigL(dOrNull, modulus!!.size) //privateExponent

            //反推P、Q
            val n = BigX(modulus)
            val e = BigX(exponent)
            val d = BigX(dOrNull)
            var p = findFactor(e, d, n)
            var q = n.divide(p)
            if (p.compareTo(q) > 0) {
                val t = p
                p = q
                q = t
            }
            val exp1 = d.mod(p.subtract(BigInteger.ONE))
            val exp2 = d.mod(q.subtract(BigInteger.ONE))
            val coeff = q.modInverse(p)
            val keyLen = modulus.size / 2
            Val_P = BigL(BigB(p), keyLen) //prime1
            Val_Q = BigL(BigB(q), keyLen) //prime2
            Val_DP = BigL(BigB(exp1), keyLen) //exponent1
            Val_DQ = BigL(BigB(exp2), keyLen) //exponent2
            Val_InverseQ = BigL(BigB(coeff), keyLen) //coefficient
        }
    }

    /**秘钥位数 */
    fun keySize(): Int {
        return Key_Modulus!!.size * 8
    }

    /**是否包含私钥 */
    fun hasPrivate(): Boolean {
        return Key_D != null
    }

    /**得到公钥Java对象 */
    @get:Throws(Exception::class)
    val rSAPublicKey: RSAPublicKey
        get() {
            val spec = RSAPublicKeySpec(BigX(Key_Modulus), BigX(Key_Exponent))
            val factory = KeyFactory.getInstance("RSA")
            return factory.generatePublic(spec) as RSAPublicKey
        }

    /**得到私钥Java对象 */
    @get:Throws(Exception::class)
    val rSAPrivateKey: RSAPrivateKey
        get() {
            if (Key_D == null) {
                throw Exception("当前为公钥，无法获得私钥")
            }
            val spec = RSAPrivateKeySpec(BigX(Key_Modulus), BigX(Key_D))
            val factory = KeyFactory.getInstance("RSA")
            return factory.generatePrivate(spec) as RSAPrivateKey
        }

    @Throws(Exception::class)
    fun ToPEM_PKCS1_Bytes(convertToPublic: Boolean): ByteArray {
        return ToPEM_Bytes(convertToPublic, false, false)
    }

    @Throws(Exception::class)
    fun ToPEM_PKCS8_Bytes(convertToPublic: Boolean): ByteArray {
        return ToPEM_Bytes(convertToPublic, true, true)
    }

    @OptIn(InternalAPI::class)
    @Throws(Exception::class)
    fun ToPEM_Bytes(
        convertToPublic: Boolean,
        privateUsePKCS8: Boolean,
        publicUsePKCS8: Boolean
    ): ByteArray {
        //https://www.jianshu.com/p/25803dd9527d
        //https://www.cnblogs.com/ylz8401/p/8443819.html
        //https://blog.csdn.net/jiayanhui2877/article/details/47187077
        //https://blog.csdn.net/xuanshao_/article/details/51679824
        //https://blog.csdn.net/xuanshao_/article/details/51672547
        val ms = ByteArrayOutputStream()
        return if (Key_D == null || convertToPublic) {
            /****生成公钥 */

            //写入总字节数，不含本段长度，额外需要24字节的头，后续计算好填入
            ms.write(0x30)
            val index1 = ms.size()

            //PKCS8 多一段数据
            var index2 = -1
            var index3 = -1
            if (publicUsePKCS8) {
                //固定内容
                // encoded OID sequence for PKCS #1 rsaEncryption szOID_RSA_RSA = "1.2.840.113549.1.1.1"
                ms.write(_SeqOID)

                //从0x00开始的后续长度
                ms.write(0x03)
                index2 = ms.size()
                ms.write(0x00)

                //后续内容长度
                ms.write(0x30)
                index3 = ms.size()
            }

            //写入Modulus
            writeBlock(Key_Modulus, ms)

            //写入Exponent
            writeBlock(Key_Exponent, ms)


            //计算空缺的长度
            var byts = ms.toByteArray()
            if (index2 != -1) {
                byts = writeLen(index3, byts, ms)
                byts = writeLen(index2, byts, ms)
            }
            byts = writeLen(index1, byts, ms)
            byts.encodeBase64().decodeBase64Bytes()
//            byts.encodeBase64ToByteArray(Base64.Default)
        } else {
            /****生成私钥 */

            //写入总字节数，后续写入
            ms.write(0x30)
            val index1 = ms.size()

            //写入版本号
            ms.write(_Ver)

            //PKCS8 多一段数据
            var index2 = -1
            var index3 = -1
            if (privateUsePKCS8) {
                //固定内容
                ms.write(_SeqOID)

                //后续内容长度
                ms.write(0x04)
                index2 = ms.size()

                //后续内容长度
                ms.write(0x30)
                index3 = ms.size()

                //写入版本号
                ms.write(_Ver)
            }

            //写入数据
            writeBlock(Key_Modulus, ms)
            writeBlock(Key_Exponent, ms)
            writeBlock(Key_D, ms)
            writeBlock(Val_P, ms)
            writeBlock(Val_Q, ms)
            writeBlock(Val_DP, ms)
            writeBlock(Val_DQ, ms)
            writeBlock(Val_InverseQ, ms)


            //计算空缺的长度
            var byts = ms.toByteArray()
            if (index2 != -1) {
                byts = writeLen(index3, byts, ms)
                byts = writeLen(index2, byts, ms)
            }
            byts = writeLen(index1, byts, ms)
            byts.base64.encodeToByteArray()
//            byts.encodeBase64ToByteArray(Base64.Default)
        }
    }

    /***
     * 将RSA中的密钥对转换成PEM PKCS#8格式
     * 。convertToPublic：等于true时含私钥的RSA将只返回公钥，仅含公钥的RSA不受影响
     * 。公钥如：-----BEGIN RSA PUBLIC KEY-----，私钥如：-----BEGIN RSA PRIVATE KEY-----
     * 。似乎导出PKCS#1公钥用的比较少，PKCS#8的公钥用的多些，私钥#1#8都差不多
     */
    @Throws(Exception::class)
    fun ToPEM_PKCS1(convertToPublic: Boolean): String {
        return ToPEM(convertToPublic, false, false)
    }

    /***
     * 将RSA中的密钥对转换成PEM PKCS#8格式
     * 。convertToPublic：等于true时含私钥的RSA将只返回公钥，仅含公钥的RSA不受影响
     * 。公钥如：-----BEGIN PUBLIC KEY-----，私钥如：-----BEGIN PRIVATE KEY-----
     */
    @Throws(Exception::class)
    fun ToPEM_PKCS8(convertToPublic: Boolean): String {
        return ToPEM(convertToPublic, true, true)
    }

    /***
     * 将RSA中的密钥对转换成PEM格式
     * 。convertToPublic：等于true时含私钥的RSA将只返回公钥，仅含公钥的RSA不受影响
     * 。privateUsePKCS8：私钥的返回格式，等于true时返回PKCS#8格式（-----BEGIN PRIVATE KEY-----），否则返回PKCS#1格式（-----BEGIN RSA PRIVATE KEY-----），返回公钥时此参数无效；两种格式使用都比较常见
     * 。publicUsePKCS8：公钥的返回格式，等于true时返回PKCS#8格式（-----BEGIN PUBLIC KEY-----），否则返回PKCS#1格式（-----BEGIN RSA PUBLIC KEY-----），返回私钥时此参数无效；一般用的多的是true PKCS#8格式公钥，PKCS#1格式公钥似乎比较少见
     */
    @Throws(Exception::class)
    fun ToPEM(convertToPublic: Boolean, privateUsePKCS8: Boolean, publicUsePKCS8: Boolean): String {
        val byts = ToPEM_Bytes(convertToPublic, privateUsePKCS8, publicUsePKCS8)
        var flag = ""
        if (Key_D == null || convertToPublic) {
            flag = " PUBLIC KEY"
            if (!publicUsePKCS8) {
                flag = " RSA$flag"
            }
        } else {
            flag = " PRIVATE KEY"
            if (!privateUsePKCS8) {
                flag = " RSA$flag"
            }
        }
        return """
             -----BEGIN$flag-----
             ${TextBreak(String(byts, Charset.forName("UTF-8")), 64)}
             -----END$flag-----
             """.trimIndent()
    }

    /***
     * 将RSA中的密钥对转换成XML格式
     * ，如果convertToPublic含私钥的RSA将只返回公钥，仅含公钥的RSA不受影响
     */
    @OptIn(InternalAPI::class)
    fun ToXML(convertToPublic: Boolean): String {
        val str = StringBuilder()
        str.append("<RSAKeyValue>")
        str.append("<Modulus>" + Key_Modulus!!.encodeBase64() + "</Modulus>")
        str.append("<Exponent>" + Key_Exponent!!.encodeBase64() + "</Exponent>")
        if (Key_D == null || convertToPublic) {
            /****生成公钥 */
            //NOOP
        } else {
            /****生成私钥 */
            str.append("<P>" + Val_P!!.encodeBase64() + "</P>")
            str.append("<Q>" + Val_Q!!.encodeBase64() + "</Q>")
            str.append("<DP>" + Val_DP!!.encodeBase64() + "</DP>")
            str.append("<DQ>" + Val_DQ!!.encodeBase64() + "</DQ>")
            str.append("<InverseQ>" + Val_InverseQ!!.encodeBase64() + "</InverseQ>")
            str.append("<D>" + Key_D!!.encodeBase64() + "</D>")
        }
        str.append("</RSAKeyValue>")
        return str.toString()
    }

    actual companion object {
        /**转成正整数，如果是负数，需要加前导0转成正整数 */
        fun BigX(bigb: ByteArray?): BigInteger {
            var bigb = bigb
            if (bigb!![0] < 0) {
                val c = ByteArray(bigb.size + 1)
                System.arraycopy(bigb, 0, c, 1, bigb.size)
                bigb = c
            }
            return BigInteger(bigb)
        }

        /**BigInt导出byte整数首字节>0x7F的会加0前导，保证正整数，因此需要去掉0 */
        fun BigB(bigx: BigInteger): ByteArray {
            var `val` = bigx.toByteArray()
            if (`val`[0].equals(0)) {
                val c = ByteArray(`val`.size - 1)
                System.arraycopy(`val`, 1, c, 0, c.size)
                `val` = c
            }
            return `val`
        }

        /**某些密钥参数可能会少一位（32个byte只有31个，目测是密钥生成器的问题，只在c#生成的密钥中发现这种参数，java中生成的密钥没有这种现象），直接修正一下就行；这个问题与BigB有本质区别，不能动BigB */
        fun BigL(bytes: ByteArray, keyLen: Int): ByteArray {
            return if (keyLen - bytes.size == 1) {
                 val c = ByteArray(bytes.size + 1)
                System.arraycopy(bytes, 0, c, 1, bytes.size)
                c
            } else {
                bytes
            }
        }

        /**
         * 由n e d 反推 P Q
         * 资料： https://stackoverflow.com/questions/43136036/how-to-get-a-rsaprivatecrtkey-from-a-rsaprivatekey
         * https://v2ex.com/t/661736
         */
        private fun findFactor(e: BigInteger, d: BigInteger, n: BigInteger): BigInteger {
            val edMinus1 = e.multiply(d).subtract(BigInteger.ONE)
            val s = edMinus1.lowestSetBit
            val t = edMinus1.shiftRight(s)
            val now = System.currentTimeMillis()
            var aInt = 2
            while (true) {
                if (aInt % 10 == 0 && System.currentTimeMillis() - now > 3000) {
                    throw RuntimeException("推算RSA.P超时") //测试最多循环2次，1024位的速度很快 8ms
                }
                var aPow = BigInteger.valueOf(aInt.toLong()).modPow(t, n)
                for (i in 1..s) {
                    if (aPow == BigInteger.ONE) {
                        break
                    }
                    if (aPow == n.subtract(BigInteger.ONE)) {
                        break
                    }
                    val aPowSquared = aPow.multiply(aPow).mod(n)
                    if (aPowSquared == BigInteger.ONE) {
                        return aPow.subtract(BigInteger.ONE).gcd(n)
                    }
                    aPow = aPowSquared
                }
                aInt++
            }
        }

        @Throws(Exception::class)
        actual fun FromPEM(base64: CharArray, privateKey: Boolean): RSA_PEM {
            val param = RSA_PEM()
            val dataX = base64.toByteArray() ?: throw Exception("PEM内容无效")
            val data = ShortArray(dataX.size) //转成正整数的bytes数组，不然byte是负数难搞
            for (i in dataX.indices) {
                data[i] = (dataX[i] and 0xff.toByte()) as Short
            }
            var idx = intArrayOf(0)
            if (!privateKey) {
                /****使用公钥 */
                //读取数据总长度
                readLen(0x30, data, idx)

                //检测PKCS8
                val idx2 = intArrayOf(idx[0])
                if (eq(_SeqOID, data, idx)) {
                    //读取1长度
                    readLen(0x03, data, idx)
                    idx[0]++ //跳过0x00
                    //读取2长度
                    readLen(0x30, data, idx)
                } else {
                    idx = idx2
                }

                //Modulus
                param.Key_Modulus = readBlock(data, idx)

                //Exponent
                param.Key_Exponent = readBlock(data, idx)
            } else {
                /****使用私钥 */
                //读取数据总长度
                readLen(0x30, data, idx)

                //读取版本号
                if (!eq(_Ver, data, idx)) {
                    throw Exception("PEM未知版本")
                }

                //检测PKCS8
                val idx2 = intArrayOf(idx[0])
                if (eq(_SeqOID, data, idx)) {
                    //读取1长度
                    readLen(0x04, data, idx)
                    //读取2长度
                    readLen(0x30, data, idx)

                    //读取版本号
                    if (!eq(_Ver, data, idx)) {
                        throw Exception("PEM版本无效")
                    }
                } else {
                    idx = idx2
                }

                //读取数据
                param.Key_Modulus = readBlock(data, idx)
                param.Key_Exponent = readBlock(data, idx)
                var keyLen = param.Key_Modulus!!.size
                param.Key_D = BigL(readBlock(data, idx), keyLen)
                keyLen = keyLen / 2
                param.Val_P = BigL(readBlock(data, idx), keyLen)
                param.Val_Q = BigL(readBlock(data, idx), keyLen)
                param.Val_DP = BigL(readBlock(data, idx), keyLen)
                param.Val_DQ = BigL(readBlock(data, idx), keyLen)
                param.Val_InverseQ = BigL(readBlock(data, idx), keyLen)
            }
            return param
        }

        /**
         * 用PEM格式密钥对创建RSA，支持PKCS#1、PKCS#8格式的PEM
         */
        @JvmStatic
        @Throws(Exception::class)
        fun FromPEM(pem: String): RSA_PEM {
            val base64 = _PEMCode.matcher(pem).replaceAll("")
            return if (pem.contains("PUBLIC KEY")) {
                FromPEM(base64.toCharArray(), false)
            } else if (pem.contains("PRIVATE KEY")) {
                FromPEM(base64.toCharArray(), true)
            } else {
                throw Exception("pem需要BEGIN END标头")
            }
        }

        private val _PEMCode = Pattern.compile("--+.+?--+|[\\s\\r\\n]+")
        private val _SeqOID = byteArrayOf(
            0x30,
            0x0D,
            0x06,
            0x09,
            0x2A,
            0x86.toByte(),
            0x48,
            0x86.toByte(),
            0xF7.toByte(),
            0x0D,
            0x01,
            0x01,
            0x01,
            0x05,
            0x00
        )
        private val _Ver = byteArrayOf(0x02, 0x01, 0x00)

        /**从数组start开始到指定长度复制一份 */
        private fun sub(arr: ShortArray, start: Int, count: Int): ByteArray {
            val `val` = ByteArray(count)
            for (i in 0 until count) {
                `val`[i] = arr[start + i].toByte()
            }
            return `val`
        }

        /**读取长度 */
        @Throws(Exception::class)
        private fun readLen(first: Int, data: ShortArray, idxO: IntArray): Int {
            var idx = idxO[0]
            try {
                if (data[idx] == first.toShort()) {
                    idx++
                    if (data[idx] == 0x81.toShort()) {
                        idx++
                        return data[idx++].toInt()
                    } else if (data[idx] == 0x82.toShort()) {
                        idx++
                        return (data[idx++].toInt() shl 8) + data[idx++]
                    } else if (data[idx] < 0x80) {
                        return data[idx++].toInt()
                    }
                }
                throw Exception("PEM未能提取到数据")
            } finally {
                idxO[0] = idx
            }
        }

        /**读取块数据 */
        @Throws(Exception::class)
        private fun readBlock(data: ShortArray, idxO: IntArray): ByteArray {
            var idx = idxO[0]
            return try {
                var len = readLen(0x02, data, idxO)
                idx = idxO[0]
                if (data[idx] == 0x00.toShort()) {
                    idx++
                    len--
                }
                val `val` = sub(data, idx, len)
                idx += len
                `val`
            } finally {
                idxO[0] = idx
            }
        }

        /**比较data从idx位置开始是否是byts内容 */
        private fun eq(byts: ByteArray, data: ShortArray, idxO: IntArray): Boolean {
            var idx = idxO[0]
            return try {
                var i = 0
                while (i < byts.size) {
                    if (idx >= data.size) {
                        return false
                    }
                    if ((byts[i] and 0xff.toByte()).toShort() != data[idx]) {
                        return false
                    }
                    i++
                    idx++
                }
                true
            } finally {
                idxO[0] = idx
            }
        }

        /**写入一个长度字节码 */
        private fun writeLenByte(len: Int, ms: ByteArrayOutputStream) {
            if (len < 0x80) {
                ms.write(len)
            } else if (len <= 0xff) {
                ms.write(0x81)
                ms.write(len)
            } else {
                ms.write(0x82)
                ms.write(len shr 8 and 0xff)
                ms.write(len and 0xff)
            }
        }

        /**写入一块数据 */
        @Throws(Exception::class)
        private fun writeBlock(byts: ByteArray?, ms: ByteArrayOutputStream) {
            val addZero: Boolean = byts!![0].toInt() and 0xff shr 4 >= 0x8
            ms.write(0x02)
            val len = byts.size + if (addZero) 1 else 0
            writeLenByte(len, ms)
            if (addZero) {
                ms.write(0x00)
            }
            ms.write(byts)
        }

        /**根据后续内容长度写入长度数据 */
        private fun writeLen(index: Int, byts: ByteArray, ms: ByteArrayOutputStream): ByteArray {
            val len = byts.size - index
            ms.reset()
            ms.write(byts, 0, index)
            writeLenByte(len, ms)
            ms.write(byts, index, len)
            return ms.toByteArray()
        }

        /**把字符串按每行多少个字断行 */
        private fun TextBreak(text: String, line: Int): String {
            var idx = 0
            val len = text.length
            val str = StringBuilder()
            while (idx < len) {
                if (idx > 0) {
                    str.append('\n')
                }
                if (idx + line >= len) {
                    str.append(text.substring(idx))
                } else {
                    str.append(text.substring(idx, idx + line))
                }
                idx += line
            }
            return str.toString()
        }

        /***
         * 将XML格式密钥转成PEM，支持公钥xml、私钥xml
         */
        @OptIn(InternalAPI::class)
        @JvmStatic
        @Throws(Exception::class)
        fun FromXML(xml: String?): RSA_PEM {
            val rtv = RSA_PEM()
            val xmlM = xmlExp.matcher(xml)
            if (!xmlM.find()) {
                throw Exception("XML内容不符合要求")
            }
            val tagM = xmlTagExp.matcher(xmlM.group(1))
            //        Base64.Decoder dec=Base64.getDecoder();
            while (tagM.find()) {
                val tag = tagM.group(1)
                val b64 = tagM.group(2)
                val `val` = b64.decodeBase64Bytes()
                when (tag) {
                    "Modulus" -> rtv.Key_Modulus = `val`
                    "Exponent" -> rtv.Key_Exponent = `val`
                    "D" -> rtv.Key_D = `val`
                    "P" -> rtv.Val_P = `val`
                    "Q" -> rtv.Val_Q = `val`
                    "DP" -> rtv.Val_DP = `val`
                    "DQ" -> rtv.Val_DQ = `val`
                    "InverseQ" -> rtv.Val_InverseQ = `val`
                }
            }
            if (rtv.Key_Modulus == null || rtv.Key_Exponent == null) {
                throw Exception("XML公钥丢失")
            }
            if (rtv.Key_D != null) {
                if (rtv.Val_P == null || rtv.Val_Q == null || rtv.Val_DP == null || rtv.Val_DQ == null || rtv.Val_InverseQ == null) {
                    return RSA_PEM(rtv.Key_Modulus, rtv.Key_Exponent, rtv.Key_D)
                }
            }
            return rtv
        }

        private val xmlExp =
            Pattern.compile("\\s*<RSAKeyValue>([<>\\/\\+=\\w\\s]+)</RSAKeyValue>\\s*")
        private val xmlTagExp = Pattern.compile("<(.+?)>\\s*([^<]+?)\\s*</")
    }
}