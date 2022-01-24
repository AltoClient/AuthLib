package com.jacobtread.mck.authlib.properties

import java.nio.charset.StandardCharsets
import java.security.GeneralSecurityException
import java.security.PublicKey
import java.security.Signature
import java.util.*

class Property(val name: String, val value: String, val signature: String? = null) {

    val hasSignature: Boolean get() = signature != null

    fun isSignatureValid(publicKey: PublicKey): Boolean {
        try {
            val signature = Signature.getInstance("SHA1withRSA")
            signature.initVerify(publicKey)
            signature.update(value.toByteArray(StandardCharsets.UTF_8))
            return signature.verify(Base64.getDecoder().decode(this.signature))
        } catch (e: GeneralSecurityException) {
            e.printStackTrace()
        }
        return false
    }
}