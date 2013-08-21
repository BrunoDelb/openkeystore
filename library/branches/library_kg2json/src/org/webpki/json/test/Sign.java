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
package org.webpki.json.test;

import java.io.IOException;

import org.webpki.crypto.KeyStoreSigner;

import org.webpki.crypto.test.DemoKeyStore;

import org.webpki.json.JSONEnvelopedSignatureEncoder;
import org.webpki.json.JSONObject;
import org.webpki.json.JSONSerializer;
import org.webpki.json.JSONWriter;
import org.webpki.json.JSONX509Signer;

/**
 * Simple test program
 */
public class Sign extends JSONSerializer
  {
    class HT implements JSONObject
      {

        @Override
        public void writeObject (JSONWriter wr) throws IOException
          {
            wr.setString ("HTL", "656756#");
            wr.setInt ("INTEGER", -689);
          }
      }
    
    class RT implements JSONObject
      {
        @Override
        public void writeObject (JSONWriter wr) throws IOException
          {
            wr.setString ("RTl", "67");
            wr.setObject ("YT", new HT ());
            wr.setString ("er","33");
          }
      }

    @Override
    protected byte[] getJSONData () throws IOException
      {
        JSONWriter wr = new JSONWriter ("MyJSONMessage", "http://example.com");
        wr.setObject ("HRT", new RT ());
        wr.setObjectArray ("ARR", new JSONObject[]{});
        wr.setObjectArray ("BARR", new JSONObject[]{new HT ()});
        wr.setInt ("Intra", 78);
        KeyStoreSigner signer = new KeyStoreSigner (DemoKeyStore.getExampleDotComKeyStore (), null);
        signer.setKey (null, DemoKeyStore.getSignerPassword ());
        JSONEnvelopedSignatureEncoder signature = new JSONEnvelopedSignatureEncoder (new JSONX509Signer (signer));
        signature.sign (wr, "Intra", "78");
        return wr.serializeJSONStructure ();
      }
    
    public static void main (String[] argc)
      {
        try
          {
            System.out.println (new String (new Sign ().getJSONData (), "UTF-8"));
          }
        catch (IOException e)
          {
            e.printStackTrace ();
          }
      }
  }
