package com.xxx.zzz.clipherp

import android.util.Base64
import com.xxx.zzz.globp.Constantsfd.IV
import java.nio.charset.Charset
import java.security.GeneralSecurityException
import javax.crypto.Cipher
import javax.crypto.spec.IvParameterSpec
import javax.crypto.spec.SecretKeySpec

object Cryptorpo {

    private val ALGORITHM: String by lazy { base64Decode("QUVT") }
    private val MODE: String by lazy { base64Decode("QUVTL0NCQy9QS0NTNVBhZGRpbmc=") }

    @Throws(GeneralSecurityException::class)
    fun decrypt(encrypted: String, key: String): String {
        val encryptedBytes: ByteArray = Base64.decode(encrypted, Base64.DEFAULT)

        val secretKeySpec = SecretKeySpec(key.toByteArray(), ALGORITHM)
        val cipher: Cipher = Cipher.getInstance(MODE)
        cipher.init(Cipher.DECRYPT_MODE, secretKeySpec, IvParameterSpec(IV.toByteArray()))

        val resultBytes: ByteArray = cipher.doFinal(encryptedBytes)
        return String(resultBytes)
    }

    fun encrypt(value: String, key: String): String {
        val secretKeySpec = SecretKeySpec(key.toByteArray(), ALGORITHM)
        val cipher: Cipher = Cipher.getInstance(MODE)
        cipher.init(Cipher.ENCRYPT_MODE, secretKeySpec, IvParameterSpec(IV.toByteArray()))
        val values: ByteArray = cipher.doFinal(value.toByteArray())
        return Base64.encodeToString(values, Base64.DEFAULT)
    }

    private val KEY2: String by lazy { base64Decode("c29zaV9zb3Npc29uX19fXw==") } 

    @JvmStatic
    fun decryptstr(encrypted: String): String {
        val encryptedBytes = base64Decode(encrypted)
        val split: List<String> = encryptedBytes.split("::")
        val data = split[0]
        val iv = split[1]

        val secretKeySpec = SecretKeySpec(
            KEY2.toByteArray(),
            ALGORITHM
        )
        val cipher: Cipher = Cipher.getInstance(MODE)
        cipher.init(
            Cipher.DECRYPT_MODE,
            secretKeySpec,
            IvParameterSpec(Base64.decode(iv, Base64.DEFAULT))
        )

        val resultBytes: ByteArray = cipher.doFinal(Base64.decode(data, Base64.DEFAULT))
        return String(resultBytes)
    }

    fun base64Decode(str: String?): String {
        return try {
            val data = Base64.decode(str, Base64.DEFAULT)
            data.toString(Charset.forName("UTF-8"))
        } catch (ex: Exception) {
            ""
        }
    }
}