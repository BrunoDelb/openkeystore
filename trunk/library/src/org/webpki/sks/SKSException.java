package org.webpki.sks;

import java.io.IOException;

@SuppressWarnings("serial")
public class SKSException extends IOException
  {
    /* Non-fatal error returned when there is something wrong with a
       supplied  PIN or PUK code.  See getKeyProtectionInfo */
    public static final int ERROR_AUTHORIZATION  = 0x01;
    
    /* Operation is not allowed */
    public static final int ERROR_NOT_ALLOWED    = 0x02;

    /* No persistent storage available for the operation */
    public static final int ERROR_STORAGE        = 0x03;

    /* MAC does not match supplied data */
    public static final int ERROR_MAC            = 0x04;

    /* Various cryptographic errors */
    public static final int ERROR_CRYPTO         = 0x05;

    /* Provisioning session not found */
    public static final int ERROR_NO_SESSION     = 0x06;

    /* closeProvisioningSession failed to verify */
    public static final int ERROR_SESSION_VERIFY = 0x07;

    /* Key not found */
    public static final int ERROR_NO_KEY         = 0x08;


    /* Unknown or not fitting algorithm */
    public static final int ERROR_ALGORITHM      = 0x09;

    /* Invalid or unsupported option */
    public static final int ERROR_OPTION         = 0x0A;

    /* Internal error */
    public static final int ERROR_INTERNAL       = 0x0B;

    int error;
    
    public SKSException (String e, int error)
      {
        super (e);
        this.error = error;
      }

    public SKSException (Throwable t, int error)
      {
        super (t);
        this.error = error;
      }

    public SKSException (String e)
      {
        this (e, ERROR_INTERNAL);
      }
    
    public int getError ()
      {
        return error;
      }
  }
