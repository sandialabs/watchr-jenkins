/*******************************************************************************
* Watchr
* ------
* Copyright 2021 National Technology & Engineering Solutions of Sandia, LLC (NTESS).
* Under the terms of Contract DE-NA0003525 with NTESS, the U.S. Government retains
* certain rights in this software.
******************************************************************************/
package gov.sandia.watchr.actions;

import hudson.Extension;
import hudson.Functions;
import hudson.model.Action;
import hudson.model.Build;
import hudson.model.HealthReport;
import hudson.model.HealthReportingAction;
import hudson.model.ResultTrend;
import hudson.model.AbstractBuild;
import hudson.model.Api;
import hudson.model.Project;
import hudson.model.Run;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import jenkins.model.RunAction2;
import jenkins.model.lazy.LazyBuildMixIn;

import org.kohsuke.stapler.export.Exported;
import org.kohsuke.stapler.export.ExportedBean;

/**
 * Common base class for recording test result.
 *
 * <p>
 * {@link Project} and {@link Build} recognizes {@link Action}s that derive from this,
 * and displays it nicely (regardless of the underlying implementation.)
 *
 * @author Kohsuke Kawaguchi
 */

@ExportedBean
public abstract class AbstractPerformanceResultAction<T extends AbstractPerformanceResultAction<T>> implements HealthReportingAction, RunAction2 {

    /**
     * @since 1.2-beta-1
     */
    public transient Run<?,?> run;
    public transient AbstractBuild<?,?> owner;

    private Map<String,String> descriptions = new ConcurrentHashMap<>();

    /** @since 1.545 */
    protected AbstractPerformanceResultAction() {}

    /**
     * @deprecated Use the default constructor and just call {@link Run#addAction} to associate the build with the action.
     * @since 1.2-beta-1
     * @param owner Need to describe this.
     */
    @Deprecated
    @SuppressWarnings("rawtypes")
    protected AbstractPerformanceResultAction(Run owner) {
        onAttached(owner);
    }

    @SuppressWarnings("rawtypes")
    protected AbstractPerformanceResultAction(AbstractBuild owner) {
        this((Run) owner);
    }
    
    @Override public void onAttached(Run<?, ?> r) {
        connectRunAndOwner(r);
    }

    @Override public void onLoad(Run<?, ?> r) {
        connectRunAndOwner(r);
    }

    private void connectRunAndOwner(Run<?,?> r) {
        this.run = r;
        this.owner = r instanceof AbstractBuild ? (AbstractBuild<?,?>) r : null;
    }

    /**
     * Gets the number of failed tests.
     * @return The fail count.
     */
    @Exported(visibility=2)
    public abstract int getFailCount();

    /**
     * Gets the number of skipped tests.
     * @return The skip count.
     */
    @Exported(visibility=2)
    public int getSkipCount() {
        // Not all sub-classes will understand the concept of skipped tests.
        // This default implementation is for them, so that they don't have
        // to implement it (this avoids breaking existing plug-ins - i.e. those
        // written before this method was added in 1.178).
        // Sub-classes that do support skipped tests should over-ride this method.
        return 0;
    }

    /**
     * Gets the total number of tests.
     * @return The total test count.
     */
    @Exported(visibility=2)
    public abstract int getTotalCount();

    /**
     * Gets the diff string of failures.
     * @return The failure diff string.
     */
    public final String getFailureDiffString() {
        T prev = getPreviousResult();
        if(prev==null)
         {
            return "";  // no record
        }

        return " / "+Functions.getDiffString(this.getFailCount()-prev.getFailCount());
    }

    public String getDisplayName() {
        return "Build Information";
    }

    @Exported(visibility=2)
    public String getUrlName() {
        return "performanceReport";
    }

    public String getIconFileName() {
        return "/plugin/watchr-jenkins/watchr48x48.png";
    }

    @SuppressWarnings("deprecation")
    public HealthReport getBuildHealth() {
        final int totalCount = getTotalCount();
        final int failCount = getFailCount();

        String description;
        int score;

        if (totalCount == 0) {
            description = "No performance reports exist.";
            score = 100;
        } else {
            score = (int) (100.0 * Math.max(0.0, Math.min(1.0, 1.0 - ((double)failCount / (double)totalCount))));
            description = score + "% of plots are succeeding.";
        }
        return new HealthReport(score, description);
    }

    /**
     * Exposes this object to the remote API.
     * @return The {@link Api} object.
     */
    public Api getApi() {
        return new Api(this);
    }

    /**
     * Returns the object that represents the actual test result.
     * This method is used by the remote API so that the XML/JSON
     * that we are sending won't contain unnecessary indirection
     * (that is, {@link AbstractPerformanceResultAction} in between.
     *
     * <p>
     * If such a concept doesn't make sense for a particular subtype,
     * return <tt>this</tt>.
     * 
     */
    public abstract Object getResult();

    /**
     * Gets the test result of the previous build, if it's recorded, or null.
     */
    @SuppressWarnings("unchecked")
    public T getPreviousResult() {
        return (T) getPreviousResult(getClass(), true);
    }

    @SuppressWarnings("rawtypes")
    private <U extends AbstractPerformanceResultAction> U getPreviousResult(Class<U> type, boolean eager) {
        Run<?,?> b = run;
        Set<Integer> loadedBuilds;
        if (!eager && run.getParent() instanceof LazyBuildMixIn.LazyLoadingJob) {
            loadedBuilds = ((LazyBuildMixIn.LazyLoadingJob<?,?>) run.getParent()).getLazyBuildMixIn()._getRuns().getLoadedBuilds().keySet();
        } else {
            loadedBuilds = null;
        }
        while(true) {
            b = loadedBuilds == null || loadedBuilds.contains(b.number - /* assuming there are no gaps */1) ? b.getPreviousBuild() : null;
            if(b==null) {
                return null;
            }
            U r = b.getAction(type);
            if (r != null) {
                if (r == this) {
                    throw new IllegalStateException(this + " was attached to both " + b + " and " + run);
                }
                if (r.run.number != b.number) {
                    throw new IllegalStateException(r + " was attached to both " + b + " and " + r.run);
                }
                return r;
            }
        }
    }

    /**
     * A shortcut for summary.jelly
     *
     * @return List of failed tests from associated test result.
     */
    public List<Object> getFailedTests() {
        return Collections.emptyList();
    }

    public Object readResolve() {
        if (descriptions == null) {
            descriptions = new ConcurrentHashMap<>();
        }
        return this;
    }

    @Extension
    public static final class Summarizer extends Run.StatusSummarizer {
        @Override
        @SuppressWarnings("rawtypes")
        public Run.Summary summarize(Run<?,?> run, ResultTrend trend) {
            AbstractPerformanceResultAction<?> trN = run.getAction(AbstractPerformanceResultAction.class);
            if (trN == null) {
                return null;
            }
            Boolean worseOverride;
            switch (trend) {
            case NOW_UNSTABLE:
                worseOverride = false;
                break;
            case UNSTABLE:
                worseOverride = true;
                break;
            case STILL_UNSTABLE:
                worseOverride = null;
                break;
            default:
                return null;
            }
            Run prev = run.getPreviousBuild();
            AbstractPerformanceResultAction<?> trP = prev == null ? null : prev.getAction(AbstractPerformanceResultAction.class);
            if (trP == null) {
                if (trN.getFailCount() > 0) {
                    return new Run.Summary(worseOverride != null ? worseOverride : true, "xxtf");
                }
            } else {
                if (trN.getFailCount() != 0) {
                    if (trP.getFailCount() == 0) {
                        return new Run.Summary(worseOverride != null ? worseOverride : true, "xxtstf");
                    }
                    if (trP.getFailCount() < trN.getFailCount()) {
                        return new Run.Summary(worseOverride != null ? worseOverride : true, "xxmtf");
                    }
                    if (trP.getFailCount() > trN.getFailCount()) {
                        return new Run.Summary(worseOverride != null ? worseOverride : false, "xxltf");
                    }

                    return new Run.Summary(worseOverride != null ? worseOverride : false, "xxtsf");
                }
            }
            return null;
        }
    }

}
