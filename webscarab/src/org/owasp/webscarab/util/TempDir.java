/*
 * TempDir.java
 *
 * Created on 26 December 2004, 04:53
 */

package org.owasp.webscarab.util;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;

import java.util.Random;
import java.security.AccessControlException;

/**
 *
 * @author  rogan
 */
public class TempDir {
    
    /** Creates a new instance of TempDir */
    private TempDir() {
    }
    
    /* -- Temporary files -- */
    
    private static final Object tmpFileLock = new Object();
    
    private static int counter = -1; /* Protected by tmpFileLock */
    
    private static File generateFile(String prefix, String suffix, File dir) {
        if (counter == -1) {
            counter = new Random().nextInt() & 0xffff;
        }
        counter++;
        return new File(dir, prefix + Integer.toString(counter) + suffix);
    }
    
    private static String tmpdir; /* Protected by tmpFileLock */
    
    private static String getTempDir() {
        if (tmpdir == null) {
            tmpdir = System.getProperty("java.io.tmpdir");
        }
        return tmpdir;
    }
    
    private static boolean checkAndCreate(File file, SecurityManager sm) {
        if (sm != null) {
            try {
                sm.checkWrite(file.getPath());
            } catch (AccessControlException x) {
                /* Throwing the original AccessControlException could disclose
                   the location of the default temporary directory, so we
                   re-throw a more innocuous SecurityException */
                throw new SecurityException("Unable to create temporary file");
            }
        }
        if (file.exists()) return false;
        return file.mkdirs();
    }
    
    /**
     * <p> Creates a new directory in the specified directory, using the
     * given prefix and suffix strings to generate its name.  If this method
     * returns successfully then it is guaranteed that:
     *
     * <ol>
     * <li> The directory denoted by the returned abstract pathname did not exist
     *      before this method was invoked, and
     * <li> Neither this method nor any of its variants will return the same
     *      abstract pathname again in the current invocation of the virtual
     *      machine.
     * </ol>
     *
     * This method provides only part of a temporary-file facility.  To arrange
     * for a file created by this method to be deleted automatically, use the
     * <code>{@link #deleteOnExit}</code> method.
     *
     * <p> The <code>prefix</code> argument must be at least three characters
     * long.  It is recommended that the prefix be a short, meaningful string
     * such as <code>"hjb"</code> or <code>"mail"</code>.  The
     * <code>suffix</code> argument may be <code>null</code>, in which case the
     * suffix <code>".tmp"</code> will be used.
     *
     * <p> To create the new file, the prefix and the suffix may first be
     * adjusted to fit the limitations of the underlying platform.  If the
     * prefix is too long then it will be truncated, but its first three
     * characters will always be preserved.  If the suffix is too long then it
     * too will be truncated, but if it begins with a period character
     * (<code>'.'</code>) then the period and the first three characters
     * following it will always be preserved.  Once these adjustments have been
     * made the name of the new file will be generated by concatenating the
     * prefix, five or more internally-generated characters, and the suffix.
     *
     * <p> If the <code>directory</code> argument is <code>null</code> then the
     * system-dependent default temporary-file directory will be used.  The
     * default temporary-file directory is specified by the system property
     * <code>java.io.tmpdir</code>.  On UNIX systems the default value of this
     * property is typically <code>"/tmp"</code> or <code>"/var/tmp"</code>; on
     * Microsoft Windows systems it is typically <code>"c:\\temp"</code>.  A different
     * value may be given to this system property when the Java virtual machine
     * is invoked, but programmatic changes to this property are not guaranteed
     * to have any effect upon the the temporary directory used by this method.
     *
     * @param  prefix     The prefix string to be used in generating the file's
     *                    name; must be at least three characters long
     *
     * @param  suffix     The suffix string to be used in generating the file's
     *                    name; may be <code>null</code>, in which case the
     *                    suffix <code>".tmp"</code> will be used
     *
     * @param  directory  The directory in which the file is to be created, or
     *                    <code>null</code> if the default temporary-file
     *                    directory is to be used
     *
     * @return  An abstract pathname denoting a newly-created empty file
     *
     * @throws  IllegalArgumentException
     *          If the <code>prefix</code> argument contains fewer than three
     *          characters
     *
     * @throws  IOException  If a file could not be created
     *
     * @throws  SecurityException
     *          If a security manager exists and its <code>{@link
     *          java.lang.SecurityManager#checkWrite(java.lang.String)}</code>
     *          method does not allow a file to be created
     *
     * @since 1.2
     */
    public static File createTempDir(String prefix, String suffix,
            File directory) {
        if (prefix == null) throw new NullPointerException();
        if (prefix.length() < 3)
            throw new IllegalArgumentException("Prefix string too short");
        String s = (suffix == null) ? ".tmp" : suffix;
        synchronized (tmpFileLock) {
            if (directory == null) {
                directory = new File(getTempDir());
            }
            SecurityManager sm = System.getSecurityManager();
            File f;
            do {
                f = generateFile(prefix, s, directory);
            } while (!checkAndCreate(f, sm));
            return f;
        }
    }
    
    /**
     * Recursive delete files.
     */
    public static boolean recursiveDelete(File dir) {
        String[] ls = dir.list();
        
        for (int i = 0; i < ls.length; i++) {
            File file = new File(dir, ls[i]);
            if (file.isDirectory()) {
                if (!recursiveDelete(file)) return false;
            } else {
                if (!file.delete()) return false;
            }
        }
        return dir.delete();
    }
    
    public static void recursiveCopy(File source, File dest) throws IOException {
        if (dest.exists())
            throw new IOException("Directory already exists: " + dest);
        if (!source.isDirectory())
            throw new IOException("Source is not a directory " + source);
        if (!dest.mkdirs()) 
            throw new IOException("Could not create destination directory " + dest);
        
        File[] ls = source.listFiles();
        
        for (int i = 0; i < ls.length; i++) {
            if (ls[i].isDirectory()) {
                recursiveCopy(ls[i], new File(dest, ls[i].getName()));
            } else {
                File newDest = new File(dest, ls[i].getName());
                FileInputStream fis = new FileInputStream(ls[i]);
                FileOutputStream fos = new FileOutputStream(newDest);
                byte[] buff = new byte[1024];
                int got;
                while ((got=fis.read(buff))>0) {
                    fos.write(buff, 0, got);
                }
                fis.close();
                fos.close();
            }
        }
    }
    
    
}