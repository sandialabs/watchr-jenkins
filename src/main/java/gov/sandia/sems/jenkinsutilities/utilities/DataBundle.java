/*******************************************************************************
* Watchr
* ------
* Copyright 2021 National Technology & Engineering Solutions of Sandia, LLC (NTESS).
* Under the terms of Contract DE-NA0003525 with NTESS, the U.S. Government retains
* certain rights in this software.
******************************************************************************/
package gov.sandia.sems.jenkinsutilities.utilities;

import java.util.List;
import java.util.Map;

import gov.sandia.sems.jenkinsutilities.xml.TimingBlock;

public interface DataBundle {
    public double getCpuSum();
    public double getWallSum();
    public double getCpuMax();
    public double getWallMax();
    public double getCpuMin();
    public double getWallMin();
    public double getCpuMean();
    public double getWallMean();
    public Map<String, String> getMetadata();
    public double getCount();
    public boolean isParallel();
    public String getInstance();
    
    List<TimingBlock> getChildBlocks();
}
