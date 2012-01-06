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
package org.webpki.webproxy;

import java.io.Serializable;

/**
 * HTTP proxy object containing a serialized HTTP response which is tunneled
 * through the proxy out to the requester.
 */
public class ResponseObject extends ClientObject implements Serializable
  {
    private static final long serialVersionUID = 1L;

    byte[] data;

    String mime_type;

    // //////////////////////////////////////////////////////
    // Due to the multi-channel proxy, calls need IDs
    // //////////////////////////////////////////////////////
    long caller_id;

    public ResponseObject (byte[] data, String mime_type, RequestObject ro)
      {
        super (ro.client_id);
        this.data = data;
        this.mime_type = mime_type;
        this.caller_id = ro.caller_id;
      }

  }
