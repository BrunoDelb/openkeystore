package org.webpki.sks;

import java.io.IOException;

import java.sql.SQLException;

import java.security.GeneralSecurityException;

import java.security.cert.X509Certificate;

import org.webpki.crypto.CertificateInfo;
import org.webpki.crypto.SignatureAlgorithms;
import org.webpki.crypto.SignerInterface;

import org.webpki.util.WrappedException;


/**
 * PKI signature support.
 */
public class SignatureProvider extends CertificateSupport implements SignerInterface
  {
    /**
     * Initializes the object for a specific keystore.
     */
    public SignatureProvider (SecureKeyStore sks)
      {
        super (sks);
      }


    /**
     * Returns the certificate path associated with the key.
     */
    public X509Certificate[] prepareSigning (boolean include_cert_path) throws IOException
      {
        testKey ();
        try
          {
            if (include_cert_path)
              {
                return getCertPath (key_id, true);
              }
          }
        catch (GeneralSecurityException gse)
          {
            throw new WrappedException (gse);
          }
        catch (SQLException sqle)
          {
            throw new WrappedException (sqle);
          }
        return cert_path;
      }


    /**
     * Signs data using the key.
     */
    public byte[] signData (byte[] data, SignatureAlgorithms algorithm) throws IOException
      {
        checkPrivateKey ();
        byte[] result = sks.privateKeyDigestSign (Digester.digestAll (data, algorithm),
                                                  key_id,
                                                  algorithm,
                                                  binary_optional_pin,
                                                  key_auth_callback);
        conditionalClose ();
        return result;
      }

 
    /**
     * Returns a descriptor of the signer certificate.
     */
    public CertificateInfo getSignerCertificateInfo () throws IOException
      {
        return new CertificateInfo (cert_path[0]);
      }

  }
