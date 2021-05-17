/*******************************************************************************
* Watchr
* ------
* Copyright 2021 National Technology & Engineering Solutions of Sandia, LLC (NTESS).
* Under the terms of Contract DE-NA0003525 with NTESS, the U.S. Government retains
* certain rights in this software.
******************************************************************************/
package gov.sandia.watchr.actions;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.kohsuke.stapler.StaplerProxy;

import gov.sandia.watchr.WatchrJenkinsApp;
import gov.sandia.watchr.model.JenkinsConfigContext;
import hudson.model.Action;
import hudson.model.Job;
import hudson.model.Run;
import jenkins.tasks.SimpleBuildStep;

/**
 * Represents the post-build action of generating performance reports.
 * Adapted from code written by Kohsuke Kawaguchi.
 *
 * @author Elliott Ridgway, Lawrence Allen
 */

public class PerformanceResultAction
            extends AbstractPerformanceResultAction<PerformanceResultAction>
            implements StaplerProxy, SimpleBuildStep.LastBuildAction {

    ////////////
    // FIELDS //
    ////////////
    
    public final List<Action> actions = new ArrayList<>();
    private Job<?,?> job;
    
    /////////////////
    // CONSTRUCTOR //
    /////////////////

    public PerformanceResultAction(Run<?,?> owner) {
        if(owner != null) {            
            this.job = owner.getParent();
            this.actions.add(new PerformanceResultsProjectAction(owner.getParent()));
        }
    }
    
    //////////////
    // OVERRIDE //
    //////////////
    
    @Override 
    public Collection<? extends Action> getProjectActions() {
        return actions;
    }
        
    @Override
    public int getTotalCount() {
        if(job != null) {
            JenkinsConfigContext context = WatchrJenkinsApp.getConfigContextOrDefault(job);
            return context.getNumberOfPlots();
        }
        return 1;
    }
    
    @Override
    public int getFailCount() {
        if(job != null) {
            JenkinsConfigContext context = WatchrJenkinsApp.getConfigContextOrDefault(job);
            return context.getNumberOfFailedPlots();
        }        
    	return 0;
    }
    
    @Override
    public Object getResult() {
        // Not implemented here.
	    return null;
    }    
    
    @Override
    public PerformanceResultAction getTarget() {
        return this;
    }

    public String generateDataDump(){
        return "This page has been left intentionally blank.";
    }
}