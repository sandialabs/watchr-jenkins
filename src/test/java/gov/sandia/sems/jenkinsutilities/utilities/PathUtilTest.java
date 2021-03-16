/*******************************************************************************
* Watchr
* ------
* Copyright 2021 National Technology & Engineering Solutions of Sandia, LLC (NTESS).
* Under the terms of Contract DE-NA0003525 with NTESS, the U.S. Government retains
* certain rights in this software.
******************************************************************************/
package gov.sandia.sems.jenkinsutilities.utilities;

import org.junit.Test;
import static org.junit.Assert.assertEquals;

/**
 *
 * @author Elliott Ridgway
 */
public class PathUtilTest {
    @Test
    public void testRemoveLeadingSegments() {
        String result = PathUtil.removeLeadingSegments("/A/B/C/D", "/", 1);
        assertEquals("A/B/C/D", result);
        result = PathUtil.removeLeadingSegments("/A/B/C/D", "/", 2);
        assertEquals("B/C/D", result);
        result = PathUtil.removeLeadingSegments("/A/B/C/D", "/", 3);
        assertEquals("C/D", result);
        result = PathUtil.removeLeadingSegments("/A/B/C/D", "/", 4);
        assertEquals("D", result);
    }
}
