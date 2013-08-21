/*
 *  Copyright 2006-2013 WebPKI.org (http://webpki.org).
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 *
 */
package org.webpki.json;

import java.io.IOException;

import java.security.cert.X509Certificate;

import org.webpki.crypto.KeyAlgorithms;
import org.webpki.crypto.SignatureAlgorithms;
import org.webpki.crypto.SignerInterface;

/**
 * Initiatiator object for X.509 signatures.
 */
public class JSONX509Signer implements JSONEnvelopedSignatureEncoder.JSONSigner
  {
    public static final String X509_CERTIFICATE_PATH_JSON = "X509CertificatePath";

    static final String SIGNATURE_CERTIFICATE_JSON = "SignatureCertificate";

    static final String ISSUER_JSON = "Issuer";
    static final String SERIAL_JSON = "SerialNumber";
    static final String SUBJECT_JSON = "Subject";

    SignatureAlgorithms algorithm;

    SignerInterface signer;
    
    X509Certificate[] certificate_path;
    
    class SignatureCertificate implements JSONObject
      {
        @Override
        public void writeObject (JSONWriter wr) throws IOException
          {
            X509Certificate signer_cert = certificate_path[0];
            wr.setString (ISSUER_JSON, signer_cert.getIssuerX500Principal ().getName ());
            wr.setString (SERIAL_JSON, signer_cert.getSerialNumber ().toString ());
            wr.setString (SUBJECT_JSON, signer_cert.getSubjectX500Principal ().getName ());
          }
      }

    public void setSignatureAlgorithm (SignatureAlgorithms algorithm)
      {
        this.algorithm = algorithm;
      }

    public JSONX509Signer (SignerInterface signer) throws IOException
      {
        this.signer = signer;
        certificate_path = signer.prepareSigning (true);
        algorithm = KeyAlgorithms.getKeyAlgorithm (certificate_path[0].getPublicKey ()).getRecommendedSignatureAlgorithm ();
      }

    @Override
    public void writeObject (JSONWriter wr) throws IOException
      {
        wr.setObject (SIGNATURE_CERTIFICATE_JSON, new SignatureCertificate ());
        wr.setString (X509_CERTIFICATE_PATH_JSON, "MMIB");
      }

    @Override
    public String getAlgorithm ()
      {
        return algorithm.getURI ();
      }

    @Override
    public byte[] signData (byte[] data) throws IOException
      {
        return signer.signData (data, algorithm);
      }
  }
