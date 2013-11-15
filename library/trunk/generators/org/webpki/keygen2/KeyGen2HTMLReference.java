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
package org.webpki.keygen2;

import static org.webpki.keygen2.KeyGen2Constants.*;

import java.io.IOException;

import org.webpki.json.JSONBaseHTML;
import org.webpki.json.JSONBaseHTML.ProtocolTable.Row.Column;
import org.webpki.json.JSONSignatureEncoder;

import org.webpki.sks.SecureKeyStore;

/**
 * Create an HTML description of the current KeyGen2 protocol.
 * 
 * @author Anders Rundgren
 */
public class KeyGen2HTMLReference implements JSONBaseHTML.Types
  {
    static final String KEYGEN2_NAME_SPACE            = "KeyGen2 name space";
    static final String OBJECT_ID                     = "Actual KeyGen2 message type";
    static final String NOT_READY                     = "DOCUMENTATION NOT READY!!!";
    static final String URI_LIST                      = "[<code>\"</code><i>List of URIs</i><code>\"</code>]";
    
    static JSONBaseHTML json;
    
    static Column preAmble (String qualifier) throws IOException
      {
        return json.addProtocolTable (PROVISIONING_INITIALIZATION_REQUEST_JSON)
        .newRow ()
          .newColumn ()
            .addContext (KEYGEN2_NS)
          .newColumn ()
            .setType (TYPE_URI)
          .newColumn ()
          .newColumn ()
            .addString (KEYGEN2_NAME_SPACE)
          .newRow ()
          .newColumn ()
            .addQualifier (qualifier)
          .newColumn ()
          .newColumn ()
          .newColumn ()
            .addString (OBJECT_ID);
      }
    
    public static void main (String args[]) throws IOException
      {
        if (args.length != 1)
          {
            new RuntimeException ("Missing file argument");
          }
        json = new JSONBaseHTML ();
        preAmble (PROVISIONING_INITIALIZATION_REQUEST_JSON)
          .newRow ()
            .newColumn ()
              .addProperty (SESSION_KEY_ALGORITHM_JSON)
              .addValue (SecureKeyStore.ALGORITHM_SESSION_ATTEST_1)
            .newColumn ()
              .setType (TYPE_URI)
            .newColumn ()
            .newColumn ()
              .addString ("See <code>SKS:createProvisioningSession." + SESSION_KEY_ALGORITHM_JSON + "</code>")
          .newRow ()
            .newColumn ()
              .addProperty (SERVER_SESSION_ID_JSON)
              .addSymbolicValue (SERVER_SESSION_ID_JSON)
            .newColumn ()
            .newColumn ()
            .newColumn ()
              .addString ("See <code>SKS:createProvisioningSession." + SERVER_SESSION_ID_JSON + "</code> and ")
              .addLink (PLATFORM_NEGOTIATION_REQUEST_JSON)
          .newRow ()
            .newColumn ()
              .addProperty (SERVER_TIME_JSON)
              .addSymbolicValue (SERVER_TIME_JSON)
            .newColumn ()
              .setType (TYPE_DATE)
            .newColumn ()
            .newColumn ()
              .addString ("Server time which the client can verify for &quot;sanity&quot;")
          .newRow ()
            .newColumn ()
              .addProperty (SUBMIT_URL_JSON)
              .addSymbolicValue (SUBMIT_URL_JSON)
            .newColumn ()
              .setType (TYPE_URI)
            .newColumn ()
            .newColumn ()
              .addString ("Where to POST the response")
          .newRow ()
            .newColumn ()
              .addProperty (SESSION_KEY_LIMIT_JSON)
              .addIntegerValue (SESSION_KEY_LIMIT_JSON)
            .newColumn ()
              .setType (TYPE_SHORT)
            .newColumn ()
            .newColumn ()
              .addString ("See <code>SKS:createProvisioningSession." + SESSION_KEY_LIMIT_JSON + "</code>")
          .newRow ()
            .newColumn ()
              .addProperty (SESSION_LIFE_TIME_JSON)
              .addIntegerValue (SESSION_LIFE_TIME_JSON)
            .newColumn ()
              .setType (TYPE_INT)
            .newColumn ()
            .newColumn ()
              .addString ("See <code>SKS:createProvisioningSession." + SESSION_LIFE_TIME_JSON + "</code>")
          .newRow ()
            .newColumn ()
              .addProperty (SERVER_EPHEMERAL_KEY_JSON)
              .addLink (SERVER_EPHEMERAL_KEY_JSON)
            .newColumn ()
              .setType (TYPE_OBJECT)
            .newColumn ()
            .newColumn ()
              .addString ("See <code>SKS:createProvisioningSession." + SERVER_EPHEMERAL_KEY_JSON + "</code>")
          .newRow ()
            .newColumn ()
              .addProperty (KEY_MANAGEMENT_KEY_JSON)
              .addLink (KEY_MANAGEMENT_KEY_JSON)
            .newColumn ()
              .setType (TYPE_OBJECT)
            .newColumn ()
              .setUsage (false)
            .newColumn ()
              .addString ("See <code>SKS:createProvisioningSession." + KEY_MANAGEMENT_KEY_JSON + "</code>")
          .newRow ()
            .newColumn ()
              .addProperty (REQUESTED_CLIENT_ATTRIBUTES_JSON)
              .addString (URI_LIST)
            .newColumn ()
              .setType (TYPE_URI)
            .newColumn ()
              .setUsage (false, 1)
            .newColumn ()
              .addString ("List of client attribute types (expresesed as URI strings) that the client <i>may</i> honor. See ")
              .addLink (PROVISIONING_INITIALIZATION_RESPONSE_JSON)
          .newRow ()
            .newColumn ()
              .addProperty (VIRTUAL_MACHINE_JSON)
              .addLink (VIRTUAL_MACHINE_JSON)
            .newColumn ()
              .setType (TYPE_OBJECT)
            .newColumn ()
              .setUsage (false)
            .newColumn ()
              .addString (NOT_READY + " Note that the <code>" +
                          VIRTUAL_MACHINE_JSON +
                          "</code> option presumes that the <code>" +
                          PROVISIONING_INITIALIZATION_REQUEST_JSON + "</code> is <i>signed</i>")
          .newRow ()
            .newColumn ()
              .addProperty (NONCE_JSON)
              .addSymbolicValue (NONCE_JSON)
            .newColumn ()
              .setType (TYPE_BASE64)
            .newColumn ()
              .setUsage (false)
            .newColumn ()
              .addString ("<i>Optional</i> 1-32 byte nonce. The <code>" +
                           NONCE_JSON + "</code> value <i>must</i> be identical to the <code>" +
                           NONCE_JSON + "</code> specified in ")
               .addLink (PLATFORM_NEGOTIATION_RESPONSE_JSON)
               .addString (". Also see <code>" + JSONSignatureEncoder.SIGNATURE_JSON + "</code>")
          .newRow ()
            .newColumn ()
              .addProperty (JSONSignatureEncoder.SIGNATURE_JSON)
              .addLink (JSONSignatureEncoder.SIGNATURE_JSON)
            .newColumn ()
              .setType (TYPE_OBJECT)
            .newColumn ()
              .setUsage (false)
            .newColumn ()
              .addString ("<i>Optional</i> signature covering the request.  Note that <code>" +
                          NONCE_JSON + "</code> <i>must</i> be specified for signed requests");

        json.addProtocolTable (PROVISIONING_FINALIZATION_REQUEST_JSON)
          .newRow ()
            .newColumn ()
              .addQualifier (KEYGEN2_NS)
            .newColumn ()
            .newColumn ()
            .newColumn ()
              .addString (KEYGEN2_NAME_SPACE)
            .newRow ()
            .newColumn ()
              .addContext (PROVISIONING_FINALIZATION_REQUEST_JSON)
            .newColumn ()
            .newColumn ()
            .newColumn ()
              .addString (OBJECT_ID)
          .newRow ()
            .newColumn ()
              .addProperty (SESSION_KEY_ALGORITHM_JSON)
              .addValue (SecureKeyStore.ALGORITHM_SESSION_ATTEST_1)
            .newColumn ()
            .newColumn ()
            .newColumn ()
              .addString ("See SKS:createProvisioningSession." + SESSION_KEY_ALGORITHM_JSON)
          .newRow ()
            .newColumn ()
              .addProperty (SERVER_SESSION_ID_JSON)
              .addSymbolicValue (SERVER_SESSION_ID_JSON)
            .newColumn ()
            .newColumn ()
            .newColumn ()
              .addString ("See SKS:createProvisioningSession." + SERVER_SESSION_ID_JSON + " and ")
              .addLink (PLATFORM_NEGOTIATION_REQUEST_JSON)
          .newRow ()
            .newColumn ()
              .addProperty (SERVER_TIME_JSON)
              .addSymbolicValue (SERVER_TIME_JSON)
            .newColumn ()
            .newColumn ()
            .newColumn ()
              .addString ("Server time in ISO format (yyyy-mm-ddThh:mm:ss+mm:ss)")
          .newRow ()
            .newColumn ()
              .addProperty (SUBMIT_URL_JSON)
              .addSymbolicValue (SUBMIT_URL_JSON)
            .newColumn ()
            .newColumn ()
            .newColumn ()
              .addString ("Where to POST the result")
          .newRow ()
            .newColumn ()
              .addProperty (SESSION_KEY_LIMIT_JSON)
              .addIntegerValue (SESSION_KEY_LIMIT_JSON)
            .newColumn ()
            .newColumn ()
            .newColumn ()
              .addString ("See SKS:createProvisioningSession." + SESSION_KEY_LIMIT_JSON)
          .newRow ()
            .newColumn ()
              .addProperty (SESSION_LIFE_TIME_JSON)
              .addIntegerValue (SESSION_LIFE_TIME_JSON)
            .newColumn ()
            .newColumn ()
            .newColumn ()
              .addString ("See SKS:createProvisioningSession." + SESSION_LIFE_TIME_JSON)
          .newRow ()
            .newColumn ()
              .addProperty (SERVER_EPHEMERAL_KEY_JSON)
              .addLink (SERVER_EPHEMERAL_KEY_JSON)
            .newColumn ()
            .newColumn ()
            .newColumn ()
              .addString ("See SKS:createProvisioningSession." + SERVER_EPHEMERAL_KEY_JSON)
          .newRow ()
            .newColumn ()
              .addProperty (ISSUED_CREDENTIALS_JSON)
              .addArrayLink (ISSUED_CREDENTIALS_JSON)
            .newColumn ()
            .newColumn ()
              .setUsage (false, 1)
            .newColumn ()
              .addString ("<i>Optional:</i> List of issued credentials");

        json.addSubItemTable (KEY_MANAGEMENT_KEY_JSON)
          .newRow ()
            .newColumn ()
              .addProperty (JSONSignatureEncoder.PUBLIC_KEY_JSON)
              .addLink (JSONSignatureEncoder.PUBLIC_KEY_JSON)
            .newColumn ()
              .setType (TYPE_OBJECT)
            .newColumn ()
            .newColumn ()
              .addString ("Actual key management key")
          .newRow ()
            .newColumn ()
              .addProperty (UPDATABLE_KEY_MANAGEMENT_KEYS_JSON)
              .addArrayLink (UPDATABLE_KEY_MANAGEMENT_KEYS_JSON)
            .newColumn ()
              .setType (TYPE_OBJECT)
            .newColumn ()
              .setUsage (false, 1)
            .newColumn ()
              .addString ("<i>Optional:</i> List of the previous generation of key management keys");

        json.addSubItemTable (UPDATABLE_KEY_MANAGEMENT_KEYS_JSON)
          .newRow ()
            .newColumn ()
              .addProperty (JSONSignatureEncoder.PUBLIC_KEY_JSON)
              .addLink (JSONSignatureEncoder.PUBLIC_KEY_JSON)
            .newColumn ()
              .setType (TYPE_OBJECT)
            .newColumn ()
            .newColumn ()
              .addString ("Previous generation key management key. Note that <code>SKS:updateKeyManagementKey.KeyManagementKey</code>" +
                          " refers to the <i>new</i> key management key specified in the object <i>immediately above</i> (=embedding) this ")
              .addLink (UPDATABLE_KEY_MANAGEMENT_KEYS_JSON)
              .addString (" object")
          .newRow ()
            .newColumn ()
              .addProperty (AUTHORIZATION_JSON)
              .addSymbolicValue (AUTHORIZATION_JSON)
            .newColumn ()
              .setType (TYPE_BASE64)
            .newColumn ()
            .newColumn ()
              .addString ("Authorization of the new key management key. See <code>SKS:updateKeyManagementKey.Authorization</code>")
          .newRow ()
            .newColumn ()
              .addProperty (UPDATABLE_KEY_MANAGEMENT_KEYS_JSON)
              .addArrayLink (UPDATABLE_KEY_MANAGEMENT_KEYS_JSON)
            .newColumn ()
              .setType (TYPE_OBJECT)
            .newColumn ()
              .setUsage (false, 1)
            .newColumn ()
              .addString ("<i>Optional:</i> List of the previous generation of key management keys");

        json.addSubItemTable (VIRTUAL_MACHINE_JSON)
          .newRow ()
            .newColumn ()
              .addProperty (JSONSignatureEncoder.PUBLIC_KEY_JSON)
              .addLink (JSONSignatureEncoder.PUBLIC_KEY_JSON)
            .newColumn ()
              .setType (TYPE_OBJECT)
            .newColumn ()
            .newColumn ()
              .addString ("Actual key management key")
          .newRow ()
            .newColumn ()
              .addProperty (UPDATABLE_KEY_MANAGEMENT_KEYS_JSON)
              .addArrayLink (UPDATABLE_KEY_MANAGEMENT_KEYS_JSON)
            .newColumn ()
              .setType (TYPE_OBJECT)
            .newColumn ()
              .setUsage (false, 1)
            .newColumn ()
              .addString ("<i>Optional:</i> List of the previous generation of key management keys");

        json.addSubItemTable (ISSUED_CREDENTIALS_JSON)
          .newRow ()
            .newColumn ()
              .addProperty (ID_JSON)
              .addSymbolicValue (ID_JSON)
            .newColumn ()
            .newColumn ()
            .newColumn ()
              .addString ("Must match the identifier used in ")
              .addLink (KEY_CREATION_REQUEST_JSON)
              .addString (" for a specific key")
          .newRow ()
            .newColumn ()
              .addProperty (ISSUED_CREDENTIALS_JSON)
              .addArrayLink (ISSUED_CREDENTIALS_JSON)
            .newColumn ()
            .newColumn ()
              .setUsage (false, 1)
            .newColumn ();

        json.addSubItemTable (SERVER_EPHEMERAL_KEY_JSON)
          .newRow ()
            .newColumn ()
              .addProperty (JSONSignatureEncoder.PUBLIC_KEY_JSON)
              .addLink (JSONSignatureEncoder.PUBLIC_KEY_JSON)
            .newColumn ()
              .setType (TYPE_OBJECT)
            .newColumn ()
            .newColumn ()
              .addString ("Must be an EC key matching the capabilities of the SKS");
        
        json.addJSONSignatureDefinitions ();

        json.writeHTML (args[0]);
      }
  }
