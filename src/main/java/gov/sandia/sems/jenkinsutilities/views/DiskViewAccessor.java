/*******************************************************************************
* Watchr
* ------
* Copyright 2021 National Technology & Engineering Solutions of Sandia, LLC (NTESS).
* Under the terms of Contract DE-NA0003525 with NTESS, the U.S. Government retains
* certain rights in this software.
******************************************************************************/
package gov.sandia.sems.jenkinsutilities.views;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.UUID;

import com.thoughtworks.xstream.XStream;
import com.thoughtworks.xstream.annotations.XStreamOmitField;

import org.apache.commons.io.FileUtils;

import gov.sandia.sems.jenkinsutilities.db.IDatabaseAccessor;
import gov.sandia.sems.jenkinsutilities.utilities.FileUtil;
import gov.sandia.sems.jenkinsutilities.utilities.LogUtil;
import gov.sandia.sems.jenkinsutilities.utilities.XStreamSingleton;

/**
 * An implementation of {@link IViewAccessor} that stores View
 * information as serialized XML on disk.
 * 
 * @author Elliott Ridgway
 */
public class DiskViewAccessor implements IViewAccessor {

    ////////////
    // FIELDS //
    ////////////

    private final List<View> views;

    @XStreamOmitField
    private final IDatabaseAccessor parent;
    @XStreamOmitField
    private final File logFile;
    @XStreamOmitField
    private final String viewDirAbsPath;
    @XStreamOmitField
    private boolean open = false;

    // Name for the file containing the serialized version of the DiskViewAccessor object.
    @XStreamOmitField
    /*package*/ static final String STATE_XML_FILE  = "view_state.xml";

    /////////////////
    // CONSTRUCTOR //
    /////////////////

    public DiskViewAccessor(IDatabaseAccessor parent, String viewDirAbsPath, File logFile) {
        this.parent = parent;
        this.viewDirAbsPath = viewDirAbsPath;
        this.views = new ArrayList<>();
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
    public boolean open() {
        if(isOpen()) {
            return false;
        }

        try {
            File rootPathFile = new File(viewDirAbsPath);
            if(!rootPathFile.exists()) {
                rootPathFile.mkdirs();
            }

            File stateXmlFile = new File(viewDirAbsPath, STATE_XML_FILE);
            if(!stateXmlFile.exists()) {
                stateXmlFile.createNewFile();
                FileUtil.setFilePermissions(stateXmlFile, true, true, true);
            } else {
                String stateXmlFileContents = FileUtils.readFileToString(stateXmlFile);
                if(!stateXmlFileContents.isEmpty()) {
                    XStream xstream = XStreamSingleton.getInstance();
                    try(InputStream is = FileUtils.openInputStream(stateXmlFile)){
                        Object deSzObj = xstream.fromXML(is);
                        if(deSzObj instanceof DiskViewAccessor) {
                            DiskViewAccessor loadedViewAccessor = (DiskViewAccessor) deSzObj;
                            views.clear();
                            views.addAll(loadedViewAccessor.getAllViews());
                        }
                    } catch(Exception e) {
                        LogUtil.writeErrorToLog(logFile, e);
                    }
                }
            }
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
            XStream xstream = XStreamSingleton.getInstance();
            File stateXmlFile = new File(viewDirAbsPath, STATE_XML_FILE);
            if(!stateXmlFile.exists()) {
                stateXmlFile.createNewFile();
                FileUtil.setFilePermissions(stateXmlFile, true, true, true);
            }
            try(OutputStream os = FileUtils.openOutputStream(stateXmlFile)) {
                xstream.toXML(this, os);
                open = false;
                return true;
            } catch(Exception e) {
                LogUtil.writeToLog(logFile, "Error serializing file at " + stateXmlFile.toPath().toString());
                LogUtil.writeErrorToLog(logFile, e);
            }
        } catch(IOException e) {
            LogUtil.writeToLog(logFile, "Error serializing file at " + viewDirAbsPath);
            LogUtil.writeErrorToLog(logFile, e);
        }
        return false;
    }

    @Override
    public boolean isOpen() {
        return open;
    }

    @Override
    public Collection<View> getAllViews() {
        return views;
    }

    @Override
    public View getView(UUID uuid) {
        for(View view : views) {
            if(view.getUUID().equals(uuid)) {
                return view;
            }
        }
        return null;
    }

    @Override
    public View findView(String name) {
        for(View view : views) {
            if(view.getName().equals(name)) {
                return view;
            }
        }
        return null;
    }

    @Override
    public void addView(View viewToAdd) {
        views.add(viewToAdd);
    }

    @Override
    public void replaceView(UUID originalViewUUID, View viewToAdd) {
        View originalView = getView(originalViewUUID);
        if(originalView != null) {
            deleteView(originalView);
        }
        addView(viewToAdd);
    }

    @Override
    public boolean deleteView(View viewToDelete) {
        return views.remove(viewToDelete);
    }

    /////////////
    // GETTERS //
    /////////////

    public String getViewDirAbsPath() {
        return viewDirAbsPath;
    }
}