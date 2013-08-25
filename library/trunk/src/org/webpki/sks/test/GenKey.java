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
package org.webpki.sks.test;

import java.io.IOException;

import java.math.BigInteger;

import java.security.GeneralSecurityException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.Signature;

import java.security.cert.CertificateEncodingException;
import java.security.cert.X509Certificate;

import java.util.Date;
import java.util.GregorianCalendar;

import org.webpki.asn1.cert.DistinguishedName;

import org.webpki.ca.CA;
import org.webpki.ca.CertSpec;

import org.webpki.crypto.AsymEncryptionAlgorithms;
import org.webpki.crypto.AsymKeySignerInterface;
import org.webpki.crypto.MACAlgorithms;
import org.webpki.crypto.AsymSignatureAlgorithms;
import org.webpki.crypto.SymEncryptionAlgorithms;

import org.webpki.crypto.test.DemoKeyStore;

import org.webpki.sks.EnumeratedKey;
import org.webpki.sks.KeyProtectionInfo;
import org.webpki.sks.SKSException;
import org.webpki.sks.SecureKeyStore;

import org.webpki.sks.test.ProvSess.MacGenerator;
import org.webpki.util.ArrayUtil;

public class GenKey
  {
    String id;
    int key_handle;
    PublicKey public_key;
    X509Certificate[] cert_path;
    ProvSess prov_sess;
    
    static long serial_number = 10000;
    
    public GenKey setCertificate (int length) throws IOException, GeneralSecurityException
      {
        StringBuffer dn = new StringBuffer ("CN=");
        for (int i = 1; i < length; i++)
          {
            if (i % 64 == 0)
              {
                dn.append (",CN=");
              }
            dn.append ('Y');
          }
        return setCertificate (dn.toString (), public_key);
      }

    public GenKey setCertificate (String dn) throws IOException, GeneralSecurityException
      {
        return setCertificate (dn, public_key);
      }

    public GenKey setCertificate (String dn, PublicKey public_key) throws IOException, GeneralSecurityException
      {
        CertSpec cert_spec = new CertSpec ();
        cert_spec.setEndEntityConstraint ();
        cert_spec.setSubject (dn);

        GregorianCalendar start = new GregorianCalendar ();
        GregorianCalendar end = (GregorianCalendar) start.clone ();
        end.set (GregorianCalendar.YEAR, end.get (GregorianCalendar.YEAR) + 25);
        
        X509Certificate certificate = 
            new CA ().createCert (cert_spec,
                                  DistinguishedName.subjectDN ((X509Certificate)DemoKeyStore.getSubCAKeyStore ().getCertificate ("mykey")),
                                  BigInteger.valueOf (serial_number++).shiftLeft (64).add (BigInteger.valueOf (new Date ().getTime ())),
                                  start.getTime (),
                                  end.getTime (), 
                                  AsymSignatureAlgorithms.RSA_SHA256,
                                  new AsymKeySignerInterface ()
            {
    
              @Override
              public PublicKey getPublicKey () throws IOException, GeneralSecurityException
                {
                  return ((X509Certificate)DemoKeyStore.getSubCAKeyStore ().getCertificate ("mykey")).getPublicKey ();
                }
    
              @Override
              public byte[] signData (byte[] data, AsymSignatureAlgorithms algorithm) throws IOException, GeneralSecurityException
                {
                  Signature signer = Signature.getInstance (algorithm.getJCEName ());
                  signer.initSign ((PrivateKey) DemoKeyStore.getSubCAKeyStore ().getKey ("mykey", DemoKeyStore.getSignerPassword ().toCharArray ()));
                  signer.update (data);
                  return signer.sign ();
                }
              
            }, public_key);
        return setCertificatePath (new X509Certificate[]{certificate});
      }
    
    public GenKey setCertificatePath (X509Certificate[] cert_path) throws IOException, GeneralSecurityException
      {
        this.cert_path = cert_path;
        prov_sess.setCertificate (key_handle, id, public_key, cert_path);
        return this;
      }
    
    public PublicKey getPublicKey ()
      {
        return cert_path == null ? public_key : cert_path[0].getPublicKey ();
      }

    public X509Certificate[] getCertificatePath ()
      {
        return cert_path;
      }
    
    void setSymmetricKey (byte[] symmetric_key) throws IOException, GeneralSecurityException
      {
        MacGenerator symk_mac = getEECertMacBuilder ();
        byte[] encrypted_symmetric_key = prov_sess.server_sess_key.encrypt (symmetric_key);
        symk_mac.addArray (encrypted_symmetric_key);
        prov_sess.sks.importSymmetricKey (key_handle, encrypted_symmetric_key, prov_sess.mac4call (symk_mac.getResult (), SecureKeyStore.METHOD_IMPORT_SYMMETRIC_KEY));
      }

    void setPrivateKey (PrivateKey private_key) throws IOException, GeneralSecurityException
      {
        MacGenerator privk_mac = getEECertMacBuilder ();
        byte[] encrypted_private_key = prov_sess.server_sess_key.encrypt (private_key.getEncoded ());
        privk_mac.addArray (encrypted_private_key);
        prov_sess.sks.importPrivateKey (key_handle, encrypted_private_key, prov_sess.mac4call (privk_mac.getResult (), SecureKeyStore.METHOD_IMPORT_PRIVATE_KEY));
      }

    void addExtension (String type, byte sub_type, String qualifier, byte[] extension_data) throws IOException, GeneralSecurityException
      {
        MacGenerator ext_mac = getEECertMacBuilder ();
        if (sub_type == SecureKeyStore.SUB_TYPE_ENCRYPTED_EXTENSION)
          {
            extension_data = prov_sess.server_sess_key.encrypt (extension_data);
          }
        ext_mac.addString (type);
        ext_mac.addByte (sub_type);
        ext_mac.addString (qualifier);
        ext_mac.addBlob (extension_data);
        prov_sess.sks.addExtension (key_handle, type, sub_type, qualifier, extension_data, prov_sess.mac4call (ext_mac.getResult (), SecureKeyStore.METHOD_ADD_EXTENSION));
      }

    public byte[] getPostProvMac (MacGenerator upd_mac, ProvSess current) throws IOException, GeneralSecurityException
      {
        Integer kmk_id = current.kmk_id;
        if (kmk_id == null)
          {
            kmk_id = 0;  // Just for JUnit...
          }
        PublicKey kmk = current.server_sess_key.enumerateKeyManagementKeys ()[kmk_id];
        byte[] authorization = current.server_sess_key.generateKeyManagementAuthorization (kmk,
                                                                                           ArrayUtil.add (SecureKeyStore.KMK_TARGET_KEY_REFERENCE,
                                                                                                          current.mac (cert_path[0].getEncoded (),
                                                                                                                       current.getDeviceID ())));
        upd_mac.addArray (authorization);
        return authorization;
      }
    
    ProvSess.MacGenerator getEECertMacBuilder () throws CertificateEncodingException, IOException
      {
        ProvSess.MacGenerator ee_mac = new ProvSess.MacGenerator ();
        ee_mac.addArray (cert_path[0].getEncoded ());
        return ee_mac;
      }

    public boolean exists () throws SKSException
      {
        EnumeratedKey ek = new EnumeratedKey ();
        while ((ek = prov_sess.sks.enumerateKeys (ek.getKeyHandle ())) != null)
          {
            if (ek.getKeyHandle () == key_handle)
              {
                return true;
              }
          }
        return false;
      }

    public EnumeratedKey getUpdatedKeyInfo () throws SKSException
      {
        EnumeratedKey ek = new EnumeratedKey ();
        while ((ek = prov_sess.sks.enumerateKeys (ek.getKeyHandle ())) != null)
          {
            if (ek.getKeyHandle () == key_handle)
              {
                return ek;
              }
          }
        throw new SKSException ("Bad state");
      }
    
    public KeyProtectionInfo getKeyProtectionInfo() throws SKSException
      {
        return prov_sess.sks.getKeyProtectionInfo (key_handle);
      }
    
    public void changePIN (String old_pin, String new_pin) throws SKSException, IOException
      {
        prov_sess.sks.changePIN (key_handle, getConditionalAuthorization (old_pin), getConditionalAuthorization (new_pin));
      }
    
    public byte[] signData (AsymSignatureAlgorithms alg_id, String pin, byte[] data) throws IOException
      {
        return prov_sess.sks.signHashedData (key_handle,
                                             alg_id.getURI (),
                                             null,
                                             getConditionalAuthorization (pin),
                                             alg_id.getDigestAlgorithm ().digest (data));
      }

    public byte[] asymmetricKeyDecrypt (AsymEncryptionAlgorithms alg_id, String pin, byte[] data) throws SKSException
      {
        return prov_sess.sks.asymmetricKeyDecrypt (key_handle,
                                                   alg_id.getURI (), 
                                                   null,
                                                   getConditionalAuthorization (pin), 
                                                   data);
      }

    public byte[] symmetricKeyEncrypt (SymEncryptionAlgorithms alg_id, boolean mode, byte[] parameters, String pin, byte[] data) throws SKSException
      {
        return prov_sess.sks.symmetricKeyEncrypt (key_handle,
                                                  alg_id.getURI (),
                                                  mode,
                                                  parameters,
                                                  getConditionalAuthorization (pin),
                                                  data);
      }

    public byte[] performHMAC (MACAlgorithms alg_id, String pin, byte[] data) throws SKSException
      {
        return prov_sess.sks.performHMAC (key_handle,
                                          alg_id.getURI (),
                                          null,
                                          getConditionalAuthorization (pin),
                                          data);
      }

    public void postUpdateKey (GenKey target_key) throws SKSException, IOException, GeneralSecurityException
      {
        MacGenerator upd_mac = getEECertMacBuilder ();
        byte[] authorization = target_key.getPostProvMac (upd_mac, prov_sess);
        prov_sess.sks.postUpdateKey (key_handle, 
                                     target_key.key_handle,
                                     authorization,
                                     prov_sess.mac4call (upd_mac.getResult (), SecureKeyStore.METHOD_POST_UPDATE_KEY));
      }
  
    public void postCloneKey (GenKey target_key) throws SKSException, IOException, GeneralSecurityException
      {
        MacGenerator upd_mac = getEECertMacBuilder ();
        byte[] authorization = target_key.getPostProvMac (upd_mac, prov_sess);
        prov_sess.sks.postCloneKeyProtection (key_handle, 
                                              target_key.key_handle,
                                              authorization,
                                              prov_sess.mac4call (upd_mac.getResult (), SecureKeyStore.METHOD_POST_CLONE_KEY_PROTECTION));
      }

    public void unlockKey (String puk) throws SKSException
      {
        prov_sess.sks.unlockKey (key_handle, getConditionalAuthorization (puk));
      }

    public void setPIN (String puk, String pin) throws SKSException
      {
        prov_sess.sks.setPIN (key_handle, getConditionalAuthorization (puk), getConditionalAuthorization (pin));
      }

    public void deleteKey (String authorization) throws SKSException
      {
        prov_sess.sks.deleteKey (key_handle, getConditionalAuthorization (authorization));
      }

    private byte[] getConditionalAuthorization (String authorization) throws SKSException
      {
        if (authorization == null) return null;
        try
          {
            return authorization.getBytes ("UTF-8");
          }
        catch (IOException e)
          {
            throw new SKSException (e);
          }
      }
  }
