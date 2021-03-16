/*******************************************************************************
* Watchr
* ------
* Copyright 2021 National Technology & Engineering Solutions of Sandia, LLC (NTESS).
* Under the terms of Contract DE-NA0003525 with NTESS, the U.S. Government retains
* certain rights in this software.
******************************************************************************/
package gov.sandia.sems.jenkinsutilities.treeaccessor;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.Serializable;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import com.thoughtworks.xstream.XStream;
import com.thoughtworks.xstream.annotations.XStreamOmitField;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;

import gov.sandia.sems.jenkinsutilities.db.IDatabaseAccessor;
import gov.sandia.sems.jenkinsutilities.utilities.FileUtil;
import gov.sandia.sems.jenkinsutilities.utilities.LogUtil;
import gov.sandia.sems.jenkinsutilities.utilities.XStreamSingleton;

/**
 * DiskFilterAccessor is a disk-based implementation of {@link IFilterAccessor}.
 * It allows for read/write access to stored {@link FilterData} objects.
 * 
 * @author Elliott Ridgway
 */
public class DiskFilterAccessor implements IFilterAccessor, Serializable {

    ////////////
    // FIELDS //
    ////////////

    public static final long serialVersionUID = "DiskFilterAccessor".hashCode();

    @XStreamOmitField
    private final IDatabaseAccessor parent;
    @XStreamOmitField
    private final File logFile;
    @XStreamOmitField
    private static final String FILTER_XML_FILE  = "tree_filter_state.xml"; // Name for a file containing the serialized data about filtered data.
    @XStreamOmitField
    private boolean open = false;
    
    private final String filterDirAbsPath;

    private String filterStateXmlFileAbsPath = null;
    private Set<FilterData> filterData = new HashSet<>();

    /////////////////
    // CONSTRUCTOR //
    /////////////////
    
    public DiskFilterAccessor(IDatabaseAccessor parent, String filterDirAbsPath, File logFile) {
        this.parent = parent;
        this.filterDirAbsPath = filterDirAbsPath;
        this.logFile = logFile;
    }

    //////////////
    // OVERRIDE //
    //////////////

    @Override
    public IDatabaseAccessor getParentDatabase() {
        return parent;
    }

    @Override
    public Set<FilterData> getFilterData() {
        return filterData;
    }

    @Override
    public FilterData getFilterDataByPath(String path, boolean exactMatch) {
        Iterator<FilterData> iter = filterData.iterator();
        while(iter.hasNext()) {
            FilterData nextFilterData = iter.next();
            if(nextFilterData != null) {
                if(exactMatch && nextFilterData.getPath().equals(path)) {
                    return nextFilterData;
                } else if(!exactMatch && StringUtils.isNotBlank(path) && nextFilterData.getPath().endsWith(path)) {
                    return nextFilterData;
                }
            }
        }
        return null;
    }

    @Override
    public boolean open() {
        if(isOpen()) {
            return false;
        }

        try {
            File rootPathFile = new File(filterDirAbsPath);
            if(!rootPathFile.exists()) {
                rootPathFile.mkdirs();
            }
            loadFilterStateFile();
            open = true;

            return true;
        } catch(IOException e) {
            LogUtil.writeErrorToLog(logFile, e);
        }
        return false;
    }

    @Override
    public boolean close() {       
        if(!isOpen()) {
            return false;
        }

        try {
            // LogUtil.writeToLog(logFile, "Saving " + filterData.size() + " filters");
            FileUtil.serializeObjToFile(filterStateXmlFileAbsPath, this);
            open = false;
        } catch(IOException e) {
            LogUtil.writeErrorToLog(logFile, e);
        }
                
        return true;
    }

    ////////////////////
    // PACKAGE ACCESS //
    ////////////////////

    /*package*/ void loadFilterStateFile() throws IOException {
        File filterStateXmlFile = new File(filterDirAbsPath, FILTER_XML_FILE);
        // LogUtil.writeToLog(logFile, "File: " + filterStateXmlFile.getAbsolutePath());

        if(!filterStateXmlFile.exists()) {
            filterStateXmlFile.createNewFile();
            FileUtil.setFilePermissions(filterStateXmlFile, true, true, true);
            // LogUtil.writeToLog(logFile, "Creating missing file");
        } else {
            XStream xstream = XStreamSingleton.getInstance();
            String fileContents = FileUtils.readFileToString(filterStateXmlFile);
            if(StringUtils.isNotBlank(fileContents)) {
                try(InputStream is = FileUtils.openInputStream(filterStateXmlFile)){
                    Object deSzObj = xstream.fromXML(is);
                    if(deSzObj instanceof DiskFilterAccessor) {
                        DiskFilterAccessor filterAccessor = (DiskFilterAccessor) deSzObj;
                        filterData.clear();
                        filterData.addAll(filterAccessor.getFilterData());
                        // LogUtil.writeToLog(logFile, "Loading filter data: " + filterAccessor.getFilterData().size());
                    }
                }
            }
        }
        filterStateXmlFileAbsPath = filterStateXmlFile.toPath().toString();
    }

    @Override
    public boolean isOpen() {
        return open;
    }
}