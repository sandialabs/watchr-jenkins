/*******************************************************************************
* Watchr
* ------
* Copyright 2021 National Technology & Engineering Solutions of Sandia, LLC (NTESS).
* Under the terms of Contract DE-NA0003525 with NTESS, the U.S. Government retains
* certain rights in this software.
******************************************************************************/
package gov.sandia.sems.jenkinsutilities.utilities;

import org.junit.Assert;
import org.junit.Test;

/**
 *
 * @author Elliott Ridgway
 */
public class StatUtilTest {
    
    private final double[] dataset = {10, 2, 38, 23, 38, 23, 21};
    
    @Test
    public void testAvg() {
        Assert.assertEquals(22.142857, StatUtil.avg(dataset), 1.0e-4);
    }
    
    @Test
    public void testStdDev() {
        Assert.assertEquals(12.298996, StatUtil.stdDev(dataset), 1.0e-4);
    }
}
