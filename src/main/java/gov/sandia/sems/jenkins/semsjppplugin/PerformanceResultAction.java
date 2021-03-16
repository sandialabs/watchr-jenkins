/*******************************************************************************
* Watchr
* ------
* Copyright 2021 National Technology & Engineering Solutions of Sandia, LLC (NTESS).
* Under the terms of Contract DE-NA0003525 with NTESS, the U.S. Government retains
* certain rights in this software.
******************************************************************************/
package gov.sandia.sems.jenkins.semsjppplugin;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.List;

import org.kohsuke.stapler.StaplerProxy;

import gov.sandia.sems.jenkinsutilities.xml.PerformanceReport;
import gov.sandia.sems.jenkinsutilities.xml.XmlDataElement;
import hudson.model.Action;
import hudson.model.Run;
import jenkins.tasks.SimpleBuildStep;

/**
 * Represents the post-build action of generating performance reports.
 * Adapted from code written by Kohsuke Kawaguchi.
 *
 * @author Elliott Ridgway, Lawrence Allen
 */

public class PerformanceResultAction extends AbstractPerformanceResultAction<PerformanceResultAction>
        implements StaplerProxy, SimpleBuildStep.LastBuildAction {

    ////////////
    // FIELDS //
    ////////////
    
    private final String defaultMeasurableUnit;    
    private final String[] userDefinedMeasurables;
    private final int rollingRange;
    private final boolean avgFailIfGreater;
    private final boolean stdDevFailIfGreater;
    private final boolean recalculateAllDerivedLines;
    private final boolean useHiddenValuesForDerivedLines;
    
    private String dataDump;
    List<Action> actions;
    
    private final Run<?,?> cachedRun;
    
    //////////////////
    // CONSTRUCTORS //
    //////////////////

    public PerformanceResultAction(
            Run<?,?> owner, List<PerformanceReport> reports, String[] userDefinedMeasurables,
            String defaultMeasurableUnit, int rollingRange,
            boolean avgFailIfGreater, boolean stdDevFailIfGreater,
            boolean recalculateAllDerivedLines,
            boolean useHiddenValuesForDerivedLines) {
       
        this.cachedRun = owner;

        this.userDefinedMeasurables = userDefinedMeasurables;
        this.defaultMeasurableUnit = defaultMeasurableUnit;
        this.rollingRange = rollingRange;
        this.avgFailIfGreater = avgFailIfGreater;
        this.stdDevFailIfGreater = stdDevFailIfGreater;
        this.recalculateAllDerivedLines = recalculateAllDerivedLines;
        this.useHiddenValuesForDerivedLines = useHiddenValuesForDerivedLines;
        
        if(reports != null) {
            this.dataDump = generateDataDump(reports);
        }
    }
        
    /////////////
    // GETTERS //
    /////////////
    
    public String getDefaultMeasurableUnit() {
        return defaultMeasurableUnit;
    }
    
    public Date getTime(){
        return cachedRun.getTime();
    }
    
    public String getId(){
        return cachedRun.getId();
    }
    
    public String getDataDump() {
    	return this.dataDump;
    }
    
    public int getRollingRange() {
        return rollingRange;
    }
    
    public boolean getAvgFailIfGreater() {
        return avgFailIfGreater;
    }
    
    public boolean getStdDevFailIfGreater() {
        return stdDevFailIfGreater;
    }

    public boolean shouldRecalculateAllDerivedLines() {
        return recalculateAllDerivedLines;
    }

    public boolean shouldUseHiddenValuesForDerivedLines() {
        return useHiddenValuesForDerivedLines;
    }
    
    public String[] getUserDefinedMeasurables() {
        return userDefinedMeasurables;
    }
    
    public void getReportData(StringBuilder holder, PerformanceReport report) {
        holder.append("<u>Report </u>: "); 
        holder.append(report.getName()); 
        holder.append("<br>");
        this.genIndent(holder, 1);
        holder.append("<b>Units </b>: ");
        holder.append(report.getUnits()); 
        holder.append("<br>");
        this.genIndent(holder, 1);
        
        for(String attribute : report.getAttributes().keySet()) {
            holder.append("<b>").append(attribute).append("</b> : ");
            holder.append(report.getAttributes().get(attribute));
        }

        if(!report.getChildren().isEmpty()){
            List<XmlDataElement> children = report.getChildren();
            for (int i = 0; i < children.size(); i++){
                blockReport(holder, children.get(i), 1);
            }
        }
    }
    
    //////////////
    // OVERRIDE //
    //////////////
    
    @Override 
    public Collection<? extends Action> getProjectActions() {
        if(actions == null && cachedRun != null) {
            actions = new ArrayList<>();
            actions.add(new PerformanceResultsProjectAction(cachedRun.getParent()));
            actions.add(new PerformanceResultViewsProjectAction(cachedRun.getParent()));
        }
        return actions;
    }
        
    @Override
    public int getTotalCount() {
        return 3;
    }
    
    @Override
    public int getFailCount() {
    	return 0;
    }
    
    @Override
    public Object getResult() {
        // Not implemented here.
	    return null;
    }    
    
    @Override
    public PerformanceResultAction getTarget() {
        // changed from JUnit's return getResult();
        return this;
    }
    
    /////////////
    // UTILITY //
    /////////////
    
    private String generateDataDump(List<PerformanceReport> reports){
        return "Run-specific performance report information coming soon.";
    }
    
    private void genIndent(StringBuilder holder, int level){
        for (int i = 0; i < level; i ++){
            holder.append("&nbsp;&nbsp;&nbsp;&nbsp;");
        }
    }
    
    private void blockReport(StringBuilder holder, XmlDataElement element, int level) {
        holder.append("<br>");
        genIndent(holder, level);
        holder.append("<u>Name :");
        holder.append(element.getName());
        holder.append("</u>");
        holder.append("<br>");
        genIndent(holder, level);
        holder.append("<u>Date :");
        holder.append(element.getDate());
        holder.append("</u>");
        holder.append("<br>");
        genIndent(holder, level);
        holder.append("<u>Units :");
        holder.append(element.getUnits());
        holder.append("<br>");
        
        for(String attribute : element.getAttributes().keySet()) {
            genIndent(holder, level + 1);
            holder.append("<b>").append(attribute).append("</b> : ");
            holder.append(element.getAttributes().get(attribute));
        }
        
        if(!element.getChildren().isEmpty()){
            List<XmlDataElement> children = element.getChildren();
            for(int i = 0; i < children.size(); i++){
                blockReport(holder, children.get(i), 1);
            }
        }
    }
}