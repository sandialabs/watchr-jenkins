/*******************************************************************************
* Watchr
* ------
* Copyright 2021 National Technology & Engineering Solutions of Sandia, LLC (NTESS).
* Under the terms of Contract DE-NA0003525 with NTESS, the U.S. Government retains
* certain rights in this software.
******************************************************************************/
package gov.sandia.sems.jenkinsutilities.utilities;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.security.MessageDigest;
import java.util.HashMap;
import java.util.Map;



/**
 * Convenience methods for dealing with MD5.  This class
 * is supposed to make developers' lives easier.  To that end
 * these methods do not throw exceptions, which developers would
 * be forced to catch.  They return false or null when a
 * appropriate and set the thrown exception to a public static
 * reference that can be accessed after the method call.
 * This is only implemented as such with the assumption that
 * the errors are infrequent.  This is a debatable assumption
 * but often in file I/O processing, the developer has high
 * confidence that a given operation will be successful, based
 * on the state of the File objects in question (i.e. how they
 * were constructed, etc.).  In practice this tends to make
 * coding of I/O code much more simple, without taking away
 * the knowledge that an operation failed.  This design is
 * thread safe as the exceptions are kept in a thread to
 * exception map.
 *
 * @author Derek Trumbo
 */

public class Md5Util {
    
    // The last exception to occur in one of this
    // class's methods for each thread.
    private static Map<Long, Exception> lastExceptions =
        new HashMap<Long, Exception>();
    public static Exception getLastException() {
        return lastExceptions.get(Thread.currentThread().getId());
    }

    ////////////////
    // PRINCIPALS //
    ////////////////

    /**
     * @param file Need to describe this.
     * @return Need to describe this.
     * 
     * IOException assumed to be infrequent.
     * The null return value uniquely identifies
     * that there was one.
     */

    public static String getMd5(File file) {
        if(file == null) {
            throw new IllegalArgumentException("file cannot be null.");
        }
        InputStream is = null;
        try {
            MessageDigest digest = MessageDigest.getInstance("MD5");
            is = new FileInputStream(file);
            byte[] buffer = new byte[8192];
            int read = 0;
            while((read = is.read(buffer)) > 0) {
                digest.update(buffer, 0, read);
            }
            return digest2hex(digest);
        } catch(Exception e) {
            lastExceptions.put(Thread.currentThread().getId(), e);
            e.printStackTrace();
            return null;
        } finally {
            if (is != null) {
                try {
                    is.close();
                } catch(Exception e) {
                    e.printStackTrace();
                }
            }
        }
    }

    public static String getMd5(byte[] bytes) {
        if(bytes == null) {
            throw new IllegalArgumentException("bytes cannot be null.");
        }
        try {
            MessageDigest digest = MessageDigest.getInstance("MD5");
            digest.update(bytes);
            return digest2hex(digest);
        } catch(Exception e) {
            lastExceptions.put(Thread.currentThread().getId(), e);
            e.printStackTrace();
            return null;
        }
    }

    ////////////////////////
    // SUPPORTING METHODS //
    ////////////////////////

    private static String digest2hex(MessageDigest digest) {
        byte[] md5sum = digest.digest();
        StringBuilder hexString = new StringBuilder(32);
        for(int i = 0; i < md5sum.length; i++) {
            hexString.append(hexDigit(md5sum[i]));
        }
        return hexString.toString();
    }

    private static char[] hexDigit(byte x) {

        // First nibble.
        char c1 = (char) ((x >> 4) & 0xf);
        if(c1 > 9) {
            c1 = (char) ((c1 - 10) + 'a');
        } else {
            c1 = (char) (c1 + '0');
        }

        // Second nibble.
        char c2 = (char) (x & 0xf);
        if(c2 > 9) {
            c2 = (char) ((c2 - 10) + 'a');
        } else {
            c2 = (char) (c2 + '0');
        }

        return new char[] {c1, c2};
    }

}
