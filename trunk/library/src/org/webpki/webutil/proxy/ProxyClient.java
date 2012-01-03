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
package org.webpki.webutil.proxy;

import java.io.IOException;
import java.io.OutputStream;
import java.io.ByteArrayOutputStream;
import java.io.BufferedInputStream;
import java.io.ByteArrayInputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

import java.util.Vector;

import java.util.logging.Level;
import java.util.logging.Logger;

import java.net.HttpURLConnection;
import java.net.URL;
import java.net.Proxy;
import java.net.InetSocketAddress;
import java.net.InetAddress;

import java.security.GeneralSecurityException;
import java.security.KeyStore;
import java.security.SecureRandom;

import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.KeyManager;
import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.TrustManager;
import javax.net.ssl.TrustManagerFactory;

import org.webpki.crypto.KeyStoreReader;

/**
 * HTTP proxy client.
 */
public abstract class ProxyClient
  {
    static Logger logger = Logger.getLogger (ProxyClient.class.getCanonicalName ());

    protected ProxyClient (String init)
      {

      }

    private class ProxyChannel implements Runnable
      {
        // //////////////////////////////////
        // Instance variables
        // //////////////////////////////////
        long proxy_id;

        ClientObject send_object;

        boolean hanging;

        boolean running = true;

        private void badReturn (String what)
          {
            if (running)
              {
                System.out.println (what + " Proxy id: " + proxy_id);
              }
          }

        public void run ()
          {
            int error_count = 0;
            if (debug)
              {
                logger.info ("Proxy channel[" + proxy_id + "] started");
              }
            while (running)
              {
                boolean talking_to_proxy = true;
                boolean throwed_an_iox = true;
                HttpURLConnection conn = null;
                try
                  {
                    // //////////////////////////////////////////////////////////////////////////////////
                    // This how the proxy client starts its work-day, by
                    // launching a call to
                    // the proxy server. Usually the call contains nothing but
                    // sometimes
                    // there is a response from the local service included. The
                    // very first call
                    // contains a "master reset" which clears any resuidal
                    // proxies in the server
                    // which may be left after a network or client proxy error.
                    // //////////////////////////////////////////////////////////////////////////////////
                    conn = (proxy == null) ? (HttpURLConnection) new URL (proxy_url).openConnection () : (HttpURLConnection) new URL (proxy_url).openConnection (proxy);

                    if (proxy_url.startsWith ("https:") && (proxy_service_truststore != null || proxy_service_keystore != null))
                      {
                        ((HttpsURLConnection) conn).setSSLSocketFactory (fixSSL (proxy_service_truststore, proxy_service_keystore, proxy_service_key_password));
                      }
                    if (send_object instanceof IdleObject)
                      {
                        synchronized (upload_objects)
                          {
                            if (!upload_objects.isEmpty ())
                              {
                                send_object = upload_objects.remove (0);
                                if (debug)
                                  {
                                    logger.info ("Upload client");
                                  }
                              }
                          }
                      }

                    // //////////////////////////////////////////////////////////////////////
                    // The following only occurrs if there is some kind of
                    // network problem
                    // //////////////////////////////////////////////////////////////////////
                    conn.setReadTimeout ((cycle_time * 3) / 2 + 30000);

                    // //////////////////////////////////////////////////////////////////////
                    // Serialize the data object to send (Conf, Idle, Response)
                    // //////////////////////////////////////////////////////////////////////
                    ByteArrayOutputStream baos = new ByteArrayOutputStream ();
                    new ObjectOutputStream (baos).writeObject (send_object);
                    byte[] send_data = baos.toByteArray ();

                    // //////////////////////////////////////////////////////////////////////
                    // Write Serialized object
                    // //////////////////////////////////////////////////////////////////////
                    conn.setDoOutput (true);
                    OutputStream ostream = conn.getOutputStream ();
                    ostream.write (send_data);
                    ostream.flush ();
                    ostream.close ();

                    // /////////////////////////////////////////////////////////////////////
                    // Set the default object for the next round
                    // /////////////////////////////////////////////////////////////////////
                    send_object = idle_object;

                    // //////////////////////////////////////////////////////////////////////////////////
                    // This is where the proxy client spends most its time -
                    // Waiting for some action
                    // //////////////////////////////////////////////////////////////////////////////////
                    hanging = true;
                    BufferedInputStream istream = new BufferedInputStream (conn.getInputStream ());
                    ByteArrayOutputStream out = new ByteArrayOutputStream ();
                    byte[] temp = new byte[1024];
                    int len;
                    while ((len = istream.read (temp)) != -1)
                      {
                        out.write (temp, 0, len);
                      }
                    byte[] data = out.toByteArray ();
                    int status = conn.getResponseCode ();
                    if (status != HttpURLConnection.HTTP_OK)
                      {
                        throw new IOException ("Bad HTTP return:" + status);
                      }
                    istream.close ();
                    conn.disconnect ();
                    hanging = false;
                    throwed_an_iox = false;
                    if (data.length == 0)
                      {
                        // ////////////////////////////////////////////////////
                        // No request data. See if it is time to just die..
                        // ////////////////////////////////////////////////////
                        if (upload_objects.isEmpty ())
                          {
                            if (unneededProxy (proxy_id))
                              {
                                if (debug)
                                  {
                                    logger.info ("No data. Channel[" + proxy_id + "] deleted");
                                  }
                                return;
                              }
                            if (debug)
                              {
                                logger.info ("No data but keep channel[" + proxy_id + "] going");
                              }
                          }
                      }
                    else
                      {
                        // ///////////////////////////////////////////////////////////////////////////////////
                        // We do have a request in progress. Check that we have
                        // enough workers in action
                        // ///////////////////////////////////////////////////////////////////////////////////
                        checkForProxyDemand (false);

                        // //////////////////////////////////
                        // Read the request object
                        // //////////////////////////////////
                        RequestObject ro = (RequestObject) new ObjectInputStream (new ByteArrayInputStream (data)).readObject ();
                        ro.setClientID (client_id);

                        // ////////////////////////////////////////////////////
                        // Now do the request/response to the local server
                        // ////////////////////////////////////////////////////
                        talking_to_proxy = false;
                        send_object = handleRequest (ro);
                      }

                    // ///////////////////////////////////////////////
                    // A round without errors. Reset error counter
                    // ///////////////////////////////////////////////
                    error_count = 0;
                  }
                catch (ClassNotFoundException cnfe)
                  {
                    badReturn ("Unexpected object!");
                  }
                catch (IOException ioe)
                  {
                    badReturn ("IOX when talking to " + (talking_to_proxy ? "proxy=" : "local service=") + ioe.getMessage ());
                    ioe.printStackTrace ();
                    try
                      {
                        if (talking_to_proxy && throwed_an_iox && running)
                          {
                            String err = conn.getResponseMessage ();
                            if (err != null)
                              {
                                System.out.println (err);
                              }
                          }
                      }
                    catch (IOException ioe2)
                      {
                      }
                    if (talking_to_proxy && running)
                      {
                        // ////////////////////////////////////////////////
                        // Kill and remove all proxy channels (threads)
                        // ////////////////////////////////////////////////
                        killProxy ();

                        if (++error_count == MAX_ERRORS)
                          {
                            // /////////////////////////
                            // We give up completely!
                            // /////////////////////////
                            System.out.println ("Hard error.  Shut down the proxy!");
                            return;
                          }

                        // /////////////////////////////////////////////////////////////////////
                        // It looks bad but we try restarting before shutting
                        // down the proxy
                        // /////////////////////////////////////////////////////////////////////
                        running = true;
                        send_object = server_configuration;
                        proxies.add (this);
                        try
                          {
                            if (debug)
                              {
                                logger.info ("Proxy: " + proxy_id + " waits " + retry_timeout + " ms for a new try...");
                              }
                            Thread.sleep (retry_timeout);
                          }
                        catch (InterruptedException ie)
                          {
                          }
                      }
                  }
                hanging = false;
              }
          }
      }

    // //////////////////////////////////
    // Configurables
    // //////////////////////////////////
    private String proxy_url;

    private Proxy proxy;

    private int proxy_max;

    private int cycle_time;

    private int retry_timeout;

    private boolean debug;

    private boolean server_debug;

    private boolean chunked_support;

    private KeyStore proxy_service_truststore;
    private KeyStore proxy_service_keystore;
    private String proxy_service_key_password;

    // //////////////////////////////////
    // App-wide "globals"
    // //////////////////////////////////
    private long last_proxy_id;

    private String client_id;

    private ServerConfiguration server_configuration;

    private IdleObject idle_object;

    private Vector<ProxyChannel> proxies = new Vector<ProxyChannel> ();

    private Vector<UploadObject> upload_objects = new Vector<UploadObject> ();

    // //////////////////////////////////
    // Defaults
    // //////////////////////////////////
    private static final int REQUEST_TIMEOUT = 60 * 1000;

    private static final int MAX_ERRORS = 1000;

    private SSLSocketFactory fixSSL (KeyStore trust_store, KeyStore client_key_store, String client_key_password)
      {
        try
          {
            TrustManager[] trust_managers = null;
            KeyManager[] key_managers = null;
            if (client_key_store != null)
              {
                KeyManagerFactory kmf = KeyManagerFactory.getInstance ("SunX509");
                kmf.init (client_key_store, client_key_password.toCharArray ());
                key_managers = kmf.getKeyManagers ();
              }
            if (trust_store != null)
              {
                TrustManagerFactory tmf = TrustManagerFactory.getInstance ("SunX509");
                tmf.init (trust_store);
                trust_managers = tmf.getTrustManagers ();
              }
            SSLContext ssl_context = SSLContext.getInstance ("TLS");
            ssl_context.init (key_managers, trust_managers, null);
            return ssl_context.getSocketFactory ();
          }
        catch (GeneralSecurityException gse)
          {
            logger.log (Level.SEVERE, "SSL setup issues", gse);
          }
        return null;
      }

    public abstract ClientObject handleRequest (RequestObject ro);

    private static char hex (int i)
      {
        if (i < 10)
          {
            return (char) (i + 48);
          }
        return (char) (i + 55);
      }

    static String toHexString (byte indata[])
      {
        StringBuffer res = new StringBuffer ();
        int i = 0;
        while (i < indata.length)
          {
            int v = indata[i++] & 0xFF;
            res.append (hex (v / 16));
            res.append (hex (v % 16));
          }
        return res.toString ();
      }

    private void spawnProxy () throws IOException
      {
        synchronized (proxies)
          {
            ProxyChannel proxy = new ProxyChannel ();
            proxy.proxy_id = last_proxy_id++;

            // ///////////////////////////////////////////////////////////////////////////////////////
            // If it is the first proxy - issue a master reset + configuration
            // to the proxy server
            // ///////////////////////////////////////////////////////////////////////////////////////
            if (proxy.proxy_id == 0)
              {
                byte[] cid = new byte[10];
                new SecureRandom ().nextBytes (cid);
                client_id = toHexString (cid);

                server_configuration = new ServerConfiguration (cycle_time, REQUEST_TIMEOUT, REQUEST_TIMEOUT, chunked_support, client_id, server_debug);
                idle_object = new IdleObject (client_id);
                proxy.send_object = server_configuration;
                if (debug)
                  {
                    logger.info ("Proxy " + client_id + " initiated");
                  }
              }
            else
              {
                proxy.send_object = idle_object;
              }
            proxies.add (proxy);
            new Thread (proxy).start ();
          }
      }

    private void checkForProxyDemand (boolean increase) throws IOException
      {
        // //////////////////////////////////////////////////////////////////////////////
        // Check that there is ample of free proxies in order to keep up with
        // requests
        // //////////////////////////////////////////////////////////////////////////////
        synchronized (proxies)
          {
            if (proxies.size () < proxy_max)
              {
                // ////////////////////////////////////////
                // We have not yet reached the ceiling
                // ////////////////////////////////////////
                int q = 0;
                for (ProxyChannel proxy : proxies)
                  {
                    if (proxy.hanging) // = Most likely to be idle
                      {
                        q++;
                      }
                  }
                if (increase)
                  {
                    q -= 2;
                  }

                // ////////////////////////////////////////
                // The margin checker
                // ////////////////////////////////////////
                if (q < 2 || q < (proxy_max / 5))
                  {
                    // ////////////////////////////////////////
                    // We could use a helping hand here...
                    // ////////////////////////////////////////
                    spawnProxy ();
                  }
              }
          }
      }

    private boolean unneededProxy (long test_proxy_id) throws IOException
      {
        synchronized (proxies)
          {
            if (proxies.size () == 1)
              {
                // ////////////////////////////////////////////
                // We must at least have one living thread...
                // ////////////////////////////////////////////
                return false;
              }

            // ////////////////////////////////////////////
            // Ooops. We are probably redundant...
            // ////////////////////////////////////////////
            int q = 0;
            for (ProxyChannel proxy : proxies)
              {
                if (proxy.proxy_id == test_proxy_id)
                  {
                    proxies.remove (q);
                    return true;
                  }
                q++;
              }
            throw new IOException ("Internal error.  Missing proxy_id: " + test_proxy_id);
          }
      }

    public void setProxyServiceTruststore (String truststore, String password) throws IOException
      {
        proxy_service_truststore = KeyStoreReader.loadKeyStore (truststore, password);
      }

    public void setProxyServiceClientKey (String keystore, String key_password) throws IOException
      {
        proxy_service_keystore = KeyStoreReader.loadKeyStore (keystore, key_password);
        proxy_service_key_password = key_password;
      }

    /**
     * Sets proxy web-proxy parameters. This method needs to be called for usage
     * of the proxy scheme where local LAN rules require outbound HTTP calls to
     * go through a web-proxy server.
     * <p>
     * Note: <i>The proxy scheme does currently not support web-proxy
     * authentication.</i>
     * 
     * @param address
     *          The host name or IP address of the web-proxy server.
     * @param port
     *          The TCP port number to use.
     */
    public void setWebProxy (String address, int port) throws IOException
      {
        if (proxy_url != null)
          {
            throw new IOException ("setWebProxy must be called before initProxy!");
          }
        proxy = new Proxy (Proxy.Type.HTTP, new InetSocketAddress (InetAddress.getByName (address), port));
      }

    /**
     * Terminates and clears the proxy connection&nbsp;(s).
     */
    public void killProxy ()
      {
        synchronized (proxies)
          {
            while (!proxies.isEmpty ())
              {
                ProxyChannel pc = proxies.remove (0);
                pc.running = false;
                if (debug)
                  {
                    System.out.println ("Killing proxy: " + pc.proxy_id);
                  }
              }
          }
        upload_objects.clear ();
      }

    /**
     * Sets proxy core parameters and initializes the proxy channel.
     * <p>
     * 
     * @param proxy_url
     *          The URL to the proxy channel.
     * @param proxy_max
     *          The maximum number of parallel proxy channels to use.
     * @param cycle_time
     *          The timeout in seconds for the &quot;waiting&quot; state.
     * @param debug
     *          Defines if debug output is to be created or not.
     * @param chunked_support
     *          Defines if &quot;chunked&quot; HTTP is to be supported or not.
     */
    public void initProxy (String proxy_url, int proxy_max, int cycle_time, int retry_timeout, boolean debug, boolean server_debug, boolean chunked_support) throws IOException
      {
        killProxy ();
        last_proxy_id = 0;
        this.proxy_url = proxy_url;
        this.proxy_max = proxy_max;
        this.cycle_time = cycle_time * 1000;
        this.retry_timeout = retry_timeout * 1000;
        this.debug = debug;
        this.server_debug = server_debug;
        this.chunked_support = chunked_support;
        spawnProxy ();
      }

    public void addUploadObject (UploadPayloadObject upload_payload_object) throws IOException
      {
        synchronized (upload_objects)
          {
            upload_objects.add (new UploadObject (client_id, upload_payload_object));
          }
        checkForProxyDemand (true);
      }

  }
