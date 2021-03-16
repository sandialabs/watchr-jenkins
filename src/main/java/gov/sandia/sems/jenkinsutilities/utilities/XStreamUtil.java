/*******************************************************************************
* Watchr
* ------
* Copyright 2021 National Technology & Engineering Solutions of Sandia, LLC (NTESS).
* Under the terms of Contract DE-NA0003525 with NTESS, the U.S. Government retains
* certain rights in this software.
******************************************************************************/
package gov.sandia.sems.jenkinsutilities.utilities;

import com.thoughtworks.xstream.XStream;
import gov.sandia.sems.jenkinsutilities.treeaccessor.OneLevelTree;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import org.apache.commons.io.FileUtils;

/**
 *
 * @author Elliott Ridgway
 */
public class XStreamUtil {
    
    public static OneLevelTree deSzOneLevelTree(File targetFile) throws IOException {
        OneLevelTree tree = null;
        try(InputStream is = FileUtils.openInputStream(targetFile)){
            XStream xstream = XStreamSingleton.getInstance();
            Object obj = xstream.fromXML(is);         
            if(obj != null && obj instanceof OneLevelTree) {
                tree = (OneLevelTree) obj;
            }
        }
        
        return tree;
    }
}
