package site.pegasis.ta.fetch.modes.server.wsproxy

import java.io.FileInputStream
import java.security.cert.CertificateException
import java.security.cert.CertificateFactory
import java.security.cert.X509Certificate
import javax.net.ssl.X509TrustManager

class TATrustManager : X509TrustManager {
    override fun checkClientTrusted(certs: Array<out X509Certificate>?, authType: String?) {}

    override fun checkServerTrusted(certs: Array<out X509Certificate>?, authType: String?) {
        certs ?: throw IllegalArgumentException("null or zero-length certificate chain")

        if (certs[0] != taCertificate) {
            try {
                certs[0].verify(taCertificate.publicKey)
            } catch (e: Throwable) {
                throw  CertificateException("Certificate not trusted", e);
            }
        }
        try {
            certs[0].checkValidity()
        } catch (e: Throwable) {
            throw CertificateException("Certificate not trusted. It has expired", e);
        }
    }

    override fun getAcceptedIssuers() = arrayOf(taCertificate)

    companion object {
        val taCertificate = kotlin.run {
            val cf = CertificateFactory.getInstance("X.509")
            val finStream = FileInputStream("data/ta.cer")
            cf.generateCertificate(finStream) as X509Certificate
        }
    }
}