/*
 *  Copyright 2006-2012 WebPKI.org (http://webpki.org).
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
package org.webpki.keygen2;

import java.io.IOException;

import java.util.LinkedHashMap;

import java.security.GeneralSecurityException;
import java.security.PublicKey;

import org.webpki.crypto.KeyAlgorithms;
import org.webpki.util.ArrayUtil;

import org.webpki.xml.DOMReaderHelper;
import org.webpki.xml.DOMAttributeReaderHelper;
import org.webpki.xml.ServerCookie;

import org.webpki.xmldsig.XMLSignatureWrapper;

import static org.webpki.keygen2.KeyGen2Constants.*;

public class KeyCreationResponseDecoder extends KeyCreationResponse
  {
    LinkedHashMap<String,GeneratedPublicKey> generated_keys = new LinkedHashMap<String,GeneratedPublicKey> ();

    class GeneratedPublicKey
      {
        private GeneratedPublicKey () {}

        String id;

        PublicKey public_key;

        byte[] attestation;
      }


    public ServerCookie getServerCookie ()
      {
        return server_cookie;
      }
    
    
    /////////////////////////////////////////////////////////////////////////////////////////////
    // XML Reader
    /////////////////////////////////////////////////////////////////////////////////////////////

    protected void fromXML (DOMReaderHelper rd) throws IOException
      {
        DOMAttributeReaderHelper ah = rd.getAttributeHelper ();
        //////////////////////////////////////////////////////////////////////////
        // Get the top-level attributes
        //////////////////////////////////////////////////////////////////////////
        client_session_id = ah.getString (ID_ATTR);

        server_session_id = ah.getString (SERVER_SESSION_ID_ATTR);

        rd.getChild ();

        //////////////////////////////////////////////////////////////////////////
        // Get the child elements
        //////////////////////////////////////////////////////////////////////////
        do
          {
            GeneratedPublicKey gk = new GeneratedPublicKey ();
            rd.getNext (PUBLIC_KEY_ELEM);
            gk.id = ah.getString (ID_ATTR);
            gk.attestation = ah.getBinaryConditional (ATTESTATION_ATTR);
            rd.getChild ();
            gk.public_key = XMLSignatureWrapper.readPublicKey (rd);
            rd.getParent ();
            if (generated_keys.put (gk.id, gk) != null)
              {
                ServerState.bad ("Duplicate key id:" + gk.id);
              }
          }
        while (rd.hasNext (PUBLIC_KEY_ELEM));

        if (rd.hasNext ())  // If not ServerCookie XML validation has gone wrong
          {
            server_cookie = ServerCookie.read (rd);
          }
      }
  }
