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
package org.webpki.crypto;

import java.security.SecureRandom;

public class URLFriendlyRandom
  {
    private static final char[] MODIFIED_BASE64 = {'A','B','C','D','E','F','G','H',
                                                   'I','J','K','L','M','N','O','P',
                                                   'Q','R','S','T','U','V','W','X',
                                                   'Y','Z','a','b','c','d','e','f',
                                                   'g','h','i','j','k','l','m','n',
                                                   'o','p','q','r','s','t','u','v',
                                                   'w','x','y','z','0','1','2','3',
                                                   '4','5','6','7','8','9','-','_'};

    public static String generate (int length_in_characters)
      {
        byte[] random = new byte[length_in_characters];
        new SecureRandom ().nextBytes (random);
        StringBuffer buffer = new StringBuffer ();
        while (--length_in_characters >= 0)
          {
            char c = MODIFIED_BASE64[random[length_in_characters] & 0x3F];
            if (c == '-' && length_in_characters == 0)
              {
                c = '_'; // URLs ending with "-" are mistreated by UAs :-(
              }
            buffer.append (c);
          }
        return buffer.toString ();
      }
  }
