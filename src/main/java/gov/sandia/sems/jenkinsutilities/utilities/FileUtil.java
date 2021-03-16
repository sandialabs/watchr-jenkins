/*******************************************************************************
* Watchr
* ------
* Copyright 2021 National Technology & Engineering Solutions of Sandia, LLC (NTESS).
* Under the terms of Contract DE-NA0003525 with NTESS, the U.S. Government retains
* certain rights in this software.
******************************************************************************/
package gov.sandia.sems.jenkinsutilities.utilities;

import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.attribute.PosixFilePermission;
import java.util.HashSet;
import java.util.Set;

import com.thoughtworks.xstream.XStream;

import org.apache.commons.io.FileUtils;

/**
 *
 * @author Elliott Ridgway
 */
public class FileUtil {
    public static boolean hasChildDirectories(File file) {
        if(file.isDirectory()) {
            for(File childFile : file.listFiles()) {
                if(childFile.isDirectory()) {
                    return true;
                }
            }
        }
        return false;
    }
    
    public static void setFilePermissions(File f, boolean owner, boolean group, boolean others) throws IOException {
        if(OsUtil.isWindows()) {
            f.setReadable(true);
            f.setWritable(true);
            f.setExecutable(true);
        } else {
            Set<PosixFilePermission> perms = new HashSet<>();
        
            perms.addAll(setFileReadPermissions(f, owner, group, others));
            perms.addAll(setFileWritePermissions(f, owner, group, others));
            perms.addAll(setFileExecutePermissions(f, owner, group, others));
            
            Files.setPosixFilePermissions(f.toPath(), perms);
        }
    } 
    
    public static Set<PosixFilePermission> setFileReadPermissions(File f, boolean owner, boolean group, boolean others) {
        Set<PosixFilePermission> perms = new HashSet<>();
        
        if(owner) {
            perms.add(PosixFilePermission.OWNER_READ);
        }
        if(group) {
            perms.add(PosixFilePermission.GROUP_READ);
        }
        if(others) {
            perms.add(PosixFilePermission.OTHERS_READ);
        }
        return perms;
    }
    
    public static Set<PosixFilePermission> setFileWritePermissions(File f, boolean owner, boolean group, boolean others) {
        Set<PosixFilePermission> perms = new HashSet<>();
        
        if(owner) {
            perms.add(PosixFilePermission.OWNER_WRITE);
        }
        if(group) {
            perms.add(PosixFilePermission.GROUP_WRITE);
        }
        if(others) {
            perms.add(PosixFilePermission.OTHERS_WRITE);
        }
        return perms;
    }
    
    public static Set<PosixFilePermission> setFileExecutePermissions(File f, boolean owner, boolean group, boolean others) {
        Set<PosixFilePermission> perms = new HashSet<>();
        
        if(owner) {
            perms.add(PosixFilePermission.OWNER_EXECUTE);
        }
        if(group) {
            perms.add(PosixFilePermission.GROUP_EXECUTE);
        }
        if(others) {
            perms.add(PosixFilePermission.OTHERS_EXECUTE);
        }
        return perms;
    } 

    public static void serializeObjToFile(String destFilePath, Object obj) throws IOException {
        XStream xstream = XStreamSingleton.getInstance();
        File xmlFile = new File(destFilePath);
        if(!xmlFile.exists()) {
            xmlFile.createNewFile();
            FileUtil.setFilePermissions(xmlFile, true, true, true);
        }

        try(OutputStream os = FileUtils.openOutputStream(xmlFile)) {
            xstream.toXML(obj, os);
        }
    }
}
