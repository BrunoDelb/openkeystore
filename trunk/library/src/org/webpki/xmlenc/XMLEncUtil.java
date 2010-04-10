package org.webpki.xmlenc;

import java.io.IOException;

import org.webpki.xml.DOMWriterHelper;
import org.webpki.xml.DOMReaderHelper;

import org.webpki.xmldsig.XMLSignatureWrapper;

import org.webpki.crypto.EncryptionAlgorithms;


class XMLEncUtil
  {

    public static final String XML_ENC_NS                  = "http://www.w3.org/2001/04/xmlenc#";

    public static final String REDUCED_XML_ENC_SCHEMA_FILE = "reduced-xenc-schema.xsd";

    public static final String XML_ENC_NS_PREFIX           = "xenc";

    // XML Encryption

    public static final String ENCRYPTED_KEY_ELEM                = "EncryptedKey";

    public static final String ENCRYPTED_DATA_ELEM               = "EncryptedData";

    public static final String ENCRYPTION_METHOD_ELEM            = "EncryptionMethod";

    public static final String CARRIED_KEY_NAME_ELEM             = "CarriedKeyName";

    public static final String CIPHER_DATA_ELEM                  = "CipherData";

    public static final String CIPHER_VALUE_ELEM                 = "CipherValue";

    public static void setEncryptionMethod (DOMWriterHelper wr, EncryptionAlgorithms algorithm) throws IOException
      {
        wr.addChildElement (ENCRYPTION_METHOD_ELEM);
        wr.setStringAttribute (XMLSignatureWrapper.ALGORITHM_ATTR, algorithm.getURI ());
        wr.getParent ();
      }


    public static void setCipherData (DOMWriterHelper wr, byte[] key) throws IOException
      {
        wr.addChildElement (CIPHER_DATA_ELEM);
        wr.addBinary (CIPHER_VALUE_ELEM, key);
        wr.getParent ();
      }


    public static void addXMLEncNS (DOMWriterHelper wr) throws IOException
      {
        wr.current ().setAttributeNS ("http://www.w3.org/2000/xmlns/", "xmlns:" + XML_ENC_NS_PREFIX, XML_ENC_NS);
      }


    public static byte[] getCipherValue (DOMReaderHelper rd) throws IOException
      {
        rd.getNext (CIPHER_DATA_ELEM);
        rd.getChild ();
        byte[] data = rd.getBinary (CIPHER_VALUE_ELEM);
        rd.getParent ();
        return data;
      }


    public static EncryptionAlgorithms getEncryptionMethod (DOMReaderHelper rd, EncryptionAlgorithms[] wanted_algorithms) throws IOException
      {
        rd.getNext (ENCRYPTION_METHOD_ELEM);
        String algo = rd.getAttributeHelper ().getString (XMLSignatureWrapper.ALGORITHM_ATTR);
        for (EncryptionAlgorithms enc_algo : wanted_algorithms)
          {
            if (algo.equals (enc_algo.getURI ()))
              {
                return enc_algo;
              }
          }
        throw new IOException ("Unexpected key encryption algorithm: " + algo);
      }



  }
