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
import java.io.Serializable;

import org.webpki.crypto.KeyAlgorithms;

import org.webpki.util.ArrayUtil;

public class KeySpecifier implements Serializable
  {
    private static final long serialVersionUID = 1L;

    byte[] parameters;
    
    KeyAlgorithms key_algorithm;
    
    public KeySpecifier (KeyAlgorithms key_algorithm)
      {
        this.key_algorithm = key_algorithm;
      }


    KeySpecifier (KeyAlgorithms key_algorithm, byte[] optional_parameter) throws IOException
      {
        this (key_algorithm);
        if (optional_parameter != null)
          {
            if (!key_algorithm.hasParameter ())
              {
                throw new IOException ("Algorithm '" + key_algorithm.toString () + "' does not use a \"Parameters\"");
              }
            if (key_algorithm.isRSAKey ())
              {
                parameters = optional_parameter; 
              }
            else
              {
                throw new IOException ("Algorithm '" + key_algorithm.toString () + "' not fu implemented");
              }
          }
      }


    public KeySpecifier (KeyAlgorithms key_algorithm, int int_parameter) throws IOException
      {
        this (key_algorithm, ArrayUtil.add (short2bytes (int_parameter >>> 16), short2bytes (int_parameter)));
      }


    public KeySpecifier (String uri, byte[] optional_parameters) throws IOException
      {
        this (KeyAlgorithms.getKeyAlgorithmFromURI (uri), optional_parameters);
      }


    public byte[] getParameters () throws IOException
      {
        return parameters;
      }


    public KeyAlgorithms getKeyAlgorithm ()
      {
        return key_algorithm;
      }


    static byte[] short2bytes (int s)
      {
        return new byte[]{(byte)(s >>> 8), (byte)s};
      }
  }
