/*******************************************************************************
* Watchr
* ------
* Copyright 2021 National Technology & Engineering Solutions of Sandia, LLC (NTESS).
* Under the terms of Contract DE-NA0003525 with NTESS, the U.S. Government retains
* certain rights in this software.
******************************************************************************/
package gov.sandia.sems.jenkinsutilities.utilities;

/**
* A collection of utility methods related to determining the current operating system.
* See https://www.mkyong.com/java/how-to-detect-os-in-java-systemgetpropertyosname/
*
* @author Elliott Ridgway
*
*/
public class OsUtil {
   
   ////////////
   // FIELDS //
   ////////////
   
   private static final String OS = System.getProperty("os.name").toLowerCase(); //$NON-NLS-1$
   
   /////////////
   // UTILITY //
   /////////////
   
   /**
    * @return True if the current operating system is Windows.
    */
   public static boolean isWindows() {
       return OS.indexOf("win") != -1; //$NON-NLS-1$
   } 
   
   /**
    * @return True if the current operating system is Mac.
    */
   public static boolean isMac() {
       return OS.indexOf("mac") != -1; //$NON-NLS-1$
   }
   
   /**
    * @return True if the current operating system is a Unix variant.
    */
   public static boolean isUnix() {
       return OS.indexOf("nix") >= 0 || OS.indexOf("nux") >= 0 || OS.indexOf("aix") > 0; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
   }
   
   /**
    * @return A Windows-style CRLF line ending if the current OS is Windows.  Otherwise,
    * return an LF line ending.
    */
   public static String getOSLineBreak() {
       return isWindows() ? "\r\n" : "\n"; //$NON-NLS-1$ //$NON-NLS-2$
   }
   
   /**
    * Given a file path string, convert the file separators to the correct slash direction
    * for the current operating system.
    * 
    * @param str A file path string.
    * @return The same file path string with file separator slashes proper for the
    * current operating system.
    */
   public static String convertToOsFileSeparators(String str) {
       if(isWindows()) {
           return str.replace("/", "\\"); //$NON-NLS-1$ //$NON-NLS-2$
       } else {
           return str.replace("\\", "/"); //$NON-NLS-1$ //$NON-NLS-2$
       }
   }

   /**
    * Given a file path string, convert the file separators to the correct slash direction
    * for the current operating system.  For Windows, this means an escaped backslash.
    * 
    * @param str A file path string.
    * @return The same file path string with file separator slashes proper for the
    * current operating system.
    */
    public static String convertToOsEscapedFileSeparators(String str) {
        if(isWindows()) {
            return str.replace("/", "\\\\"); //$NON-NLS-1$ //$NON-NLS-2$
        } else {
            return str.replace("\\\\", "/"); //$NON-NLS-1$ //$NON-NLS-2$
        }
    }
   
   /**
    * Given a string, convert any line ending characters to the correct characters
    * for the current operating system.
    * 
    * @param str The string to convert.
    * @return The same string with line endings converted for the current
    * operating system.
    */
   public static String convertToOsLineEndings(String str) {
       if(OsUtil.isWindows()) {
           str = str.replaceAll("(?<!\r)\n", "\r\n");
       } else {
           str = str.replaceAll("\r\n", "\n");
       }
       return str;
   }
}
