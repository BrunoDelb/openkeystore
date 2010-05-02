/*
 *  Copyright 2006-2010 WebPKI.org (http://webpki.org).
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
package org.webpki.sks;

public class EnumeratedKey
  {
    public static final int INIT_EXIT = 0xFFFFFFFF;
    
    int key_handle;
    
    public int getKeyHandle ()
      {
        return key_handle;
      }
    int provisioning_handle;
    
    public int getProvisioningHandle ()
      {
        return provisioning_handle;
      }
    
    String id;
    
    public String getID ()
      {
        return id;
      }
    

    public EnumeratedKey ()
      {
        key_handle = INIT_EXIT;
      }
    
    
    public boolean isValid ()
      {
        return key_handle != INIT_EXIT;
      }
    
    // TODO - a LOT!!!!
    
    public EnumeratedKey (int key_handle, 
                          String id,
                          int provisioning_handle)
      {
        this.key_handle = key_handle;
        this.id = id;
        this.provisioning_handle = provisioning_handle;
      }

  }
