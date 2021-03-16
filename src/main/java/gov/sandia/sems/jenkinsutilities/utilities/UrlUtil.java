/*******************************************************************************
* Watchr
* ------
* Copyright 2021 National Technology & Engineering Solutions of Sandia, LLC (NTESS).
* Under the terms of Contract DE-NA0003525 with NTESS, the U.S. Government retains
* certain rights in this software.
******************************************************************************/
package gov.sandia.sems.jenkinsutilities.utilities;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import org.apache.commons.lang3.StringUtils;

/**
 *
 * @author Elliott Ridgway
 */
public class UrlUtil {
    public static String getProcessedURL(String filePathStr) throws UnsupportedEncodingException {
        String local = filePathStr;
        if(!StringUtils.isBlank(local)) {
            local = local.replaceAll("%2F", "/");
            local = URLDecoder.decode(local, StandardCharsets.UTF_8.name());
        }
        return local;
    }
}
