/*******************************************************************************
* Watchr
* ------
* Copyright 2021 National Technology & Engineering Solutions of Sandia, LLC (NTESS).
* Under the terms of Contract DE-NA0003525 with NTESS, the U.S. Government retains
* certain rights in this software.
******************************************************************************/
package gov.sandia.sems.jenkinsutilities.utilities;

import hudson.util.HeapSpaceStringConverter;
import hudson.util.XStream2;

/**
 *
 * @author emridgw
 */
public class XStreamSingleton {
    private static final XStream2 XSTREAM;
    static {
        XSTREAM = new XStream2();
        // XSTREAM.registerConverter(new OneLevelTreeConverter());
        XSTREAM.registerConverter(new HeapSpaceStringConverter(),100);
    }
    
    public static XStream2 getInstance() {
        return XSTREAM;
    }
}
