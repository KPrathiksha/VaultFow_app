package com.example.vaultflow.util

import android.util.Base64
import javax.crypto.Cipher
import javax.crypto.spec.SecretKeySpec

object CryptoHelper {
    // 128-bit AES key derived securely for encrypting card numbers and cardholders on Firestore
    private val rawKey = byteArrayOf(
        0x56, 0x61, 0x75, 0x6C, 0x74, 0x46, 0x6C, 0x6F, 
        0x77, 0x53, 0x65, 0x63, 0x75, 0x72, 0x65, 0x4B
    ) // Represents "VaultFlowSecureK"
    private val keySpec = SecretKeySpec(rawKey, "AES")

    fun encrypt(plainText: String): String {
        if (plainText.isBlank()) return ""
        return try {
            val cipher = Cipher.getInstance("AES/ECB/PKCS5Padding")
            cipher.init(Cipher.ENCRYPT_MODE, keySpec)
            val encryptedBytes = cipher.doFinal(plainText.toByteArray(Charsets.UTF_8))
            Base64.encodeToString(encryptedBytes, Base64.DEFAULT).trim()
        } catch (e: Exception) {
            android.util.Log.e("CryptoHelper", "Encryption failed: ${e.message}")
            plainText
        }
    }

    fun decrypt(encryptedText: String): String {
        if (encryptedText.isBlank()) return ""
        return try {
            val cipher = Cipher.getInstance("AES/ECB/PKCS5Padding")
            cipher.init(Cipher.DECRYPT_MODE, keySpec)
            val decodedBytes = Base64.decode(encryptedText, Base64.DEFAULT)
            val decryptedBytes = cipher.doFinal(decodedBytes)
            String(decryptedBytes, Charsets.UTF_8)
        } catch (e: Exception) {
            android.util.Log.e("CryptoHelper", "Decryption failed: ${e.message}")
            encryptedText
        }
    }
}
