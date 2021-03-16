/*******************************************************************************
* Watchr
* ------
* Copyright 2021 National Technology & Engineering Solutions of Sandia, LLC (NTESS).
* Under the terms of Contract DE-NA0003525 with NTESS, the U.S. Government retains
* certain rights in this software.
******************************************************************************/
package gov.sandia.sems.jenkinsutilities.htmlgenerator;

public abstract class DivModel {
    
    public abstract String toDiv(int graphWidth, int graphHeight);
    protected abstract int addDivWidthSpacing(int initialValue);
    protected abstract int addDivHeightSpacing(int initialValue);
    protected abstract String createDivOptionsHtml();
}