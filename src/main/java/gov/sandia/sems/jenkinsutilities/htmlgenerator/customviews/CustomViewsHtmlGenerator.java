/*******************************************************************************
* Watchr
* ------
* Copyright 2021 National Technology & Engineering Solutions of Sandia, LLC (NTESS).
* Under the terms of Contract DE-NA0003525 with NTESS, the U.S. Government retains
* certain rights in this software.
******************************************************************************/
package gov.sandia.sems.jenkinsutilities.htmlgenerator.customviews;

import java.io.File;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;

import com.google.gson.JsonObject;

import org.apache.commons.lang3.StringUtils;

import gov.sandia.sems.jenkins.semsjppplugin.PerformanceResultViewsProjectAction;
import gov.sandia.sems.jenkinsutilities.db.IDatabaseAccessor;
import gov.sandia.sems.jenkinsutilities.db.IDatabasePartType;
import gov.sandia.sems.jenkinsutilities.htmlgenerator.AbstractHtmlGenerator;
import gov.sandia.sems.jenkinsutilities.htmlgenerator.DivModel;
import gov.sandia.sems.jenkinsutilities.utilities.LogUtil;
import gov.sandia.sems.jenkinsutilities.views.IViewAccessor;
import gov.sandia.sems.jenkinsutilities.views.View;
import gov.sandia.sems.jenkinsutilities.views.ViewDataset;
import gov.sandia.sems.jenkinsutilities.views.ViewDataset.DatasetType;
import hudson.model.Job;

/**
 * This class is responsible for displaying custom, user-defined graph views by
 * generating segments of HTML.
 * 
 * @author Elliott Ridgway
 */
public class CustomViewsHtmlGenerator extends AbstractHtmlGenerator {

    ////////////
    // FIELDS //
    ////////////

    public static final String DATASET_TYPE_DELIMITER = " - ";
    public static final String TYPE_SUFFIX_STRING_VALUE = "Value";
    public static final String TYPE_SUFFIX_STRING_AVG = "Average";
    public static final String TYPE_SUFFIX_STRING_STDDEV = "Std. Dev.";

    private static final String JSON_KEY_DATES     = "dates";
    private static final String JSON_KEY_GRAPHDATA = "graphData";
    private static final String JSON_KEY_STATS     = "stats";

    private final Job<?,?> job;
    private final IDatabaseAccessor db;

    /////////////////
    // CONSTRUCTOR //
    /////////////////

    public CustomViewsHtmlGenerator(
            CustomViewConfigContext context, Job<?,?> job,
            IDatabaseAccessor db, String[] measurables, File logFile) {
                
        super(context, measurables, "", logFile);
        this.job = job;
        this.db = db;

        if(StringUtils.isBlank(context.getGraphSelectedMeasurable()) && measurables.length > 0) {
            context.setGraphSelectedMeasurable(measurables[0]);
        }
        PerformanceResultViewsProjectAction.putConfigContext(job, context);
    }

    ////////////
    // PUBLIC //
    ////////////

    @Override
    public String getMenuBar() {
        CustomViewConfigContext context = PerformanceResultViewsProjectAction.getConfigContext(job);
        return CustomViewsHtmlFragmentGenerator.buildMenuBar(context, measurables);
    }

    @Override
    public String getGraphHTML() {
        CustomViewConfigContext context = PerformanceResultViewsProjectAction.getConfigContext(job);
        List<DivModel> divModels = createDivTuples();
        return CustomViewsHtmlFragmentGenerator.buildGraphs(divModels, context);
    }

    @Override
    public String getDataFromPath() {
        JsonObject dataJson = new JsonObject();
        dataJson.add(JSON_KEY_GRAPHDATA, constructGraphDataJson());
        // LogUtil.writeToLog(logFile, "Data sent:" + dataJson.toString());
        return dataJson.toString();
    }

    public View saveViewFromEditorPageCache() {
        IViewAccessor viewAccessor = (IViewAccessor) db.getAndOpenDatabasePart(IDatabasePartType.VIEWS);

        CustomViewConfigContext context = PerformanceResultViewsProjectAction.getConfigContext(job);
        View viewToSave = makeViewFromCache();
        View editedView = context.getCurrentView();
        if(editedView == null) {
            // Add mode
            viewAccessor.addView(viewToSave);
        } else {
            // Edit mode
            viewAccessor.replaceView(editedView.getUUID(), viewToSave);
        }
        viewAccessor.close();
        viewAccessor.open();

        return viewToSave;
    }

    ////////////////////
    // PACKAGE ACCESS //
    ////////////////////

    /*package*/ JsonObject constructGraphDataJson() {
        IViewAccessor viewAccessor = (IViewAccessor) db.getAndOpenDatabasePart(IDatabasePartType.VIEWS);

        JsonObject graphDataJson = new JsonObject();
        for(View view : viewAccessor.getAllViews()) {
            try {
                graphDataJson.add(view.getName(), constructViewGraphJson(view));
            } catch(ParseException e) {
                LogUtil.writeErrorToLog(logFile, e);
            }
        }
        return graphDataJson;
    }

    /*package*/ JsonObject constructViewGraphJson(View view) throws ParseException {
        IViewAccessor viewAccessor = (IViewAccessor) db.getAndOpenDatabasePart(IDatabasePartType.VIEWS);

        ConcreteViewLoader concreteViewLoader = new ConcreteViewLoader(db)
            .setMeasurable(context.getGraphSelectedMeasurable(), measurables[0])
            .setTimescale(context.getGraphSelectedTimeScale())
            .setViews(viewAccessor.getAllViews());
        concreteViewLoader.cacheOldestAndNewestDatesForView(view);

        CustomViewsJsonBuilder jsonBuilder = new CustomViewsJsonBuilder();
        JsonObject viewGraphJson = new JsonObject();
        viewGraphJson.add(JSON_KEY_DATES,
            jsonBuilder.constructDateJsonForView(concreteViewLoader, view));
        viewGraphJson.add(JSON_KEY_STATS,
            jsonBuilder.constructStatsJsonForView(concreteViewLoader, view, context.getGraphHeight()));
        return viewGraphJson;
    }

    /*package*/ List<DivModel> createDivTuples() {
        IViewAccessor viewAccessor = (IViewAccessor) db.getAndOpenDatabasePart(IDatabasePartType.VIEWS);
        List<View> views = new ArrayList<>(viewAccessor.getAllViews());
        Collections.sort(views);

        List<DivModel> divModels = new ArrayList<>();
        for(View view : views) {
            CustomViewDivModel divModel = new CustomViewDivModel(view.getName());
            divModels.add(divModel);
        }
        return divModels;
    }

    /*package*/ View makeViewFromCache() {
        CustomViewConfigContext context = PerformanceResultViewsProjectAction.getConfigContext(job);
        String name = context.getNewCustomViewName();
        Set<String> selectedDatasets = context.getNewCustomViewSelectedDatasets();

        List<ViewDataset> viewDatasets = new ArrayList<>();
        for(String selectedDataset : selectedDatasets) {
            DatasetType datasetType = getDatasetTypeSuffixFromSearchResultString(selectedDataset);
            String datasetName = getDatasetNamePrefixFromSearchResultString(selectedDataset);
            ViewDataset viewDataset = new ViewDataset(datasetName, datasetType);
            viewDatasets.add(viewDataset);
        }
        return new View(name, viewDatasets);
    }

    /*package*/ DatasetType getDatasetTypeSuffixFromSearchResultString(String selectedDataset) {
        String[] splitStrings = selectedDataset.split(DATASET_TYPE_DELIMITER);
        String lastStringFragment = splitStrings[splitStrings.length - 1];
        DatasetType datasetType = null;
        if(lastStringFragment.equals(TYPE_SUFFIX_STRING_VALUE)) {
            datasetType = DatasetType.DATA;
        } else if(lastStringFragment.equals(TYPE_SUFFIX_STRING_AVG)) {
            datasetType = DatasetType.AVERAGE;
        } else if(lastStringFragment.equals(TYPE_SUFFIX_STRING_STDDEV)) {
            datasetType = DatasetType.STD_DEV;
        }
        return datasetType;
    }

    /*package*/ String getDatasetNamePrefixFromSearchResultString(String selectedDataset) {
        String[] splitStrings = selectedDataset.split(DATASET_TYPE_DELIMITER);
        String datasetNameWithoutTypeSuffix = new String();
        for(int i = 0; i < splitStrings.length - 1; i++) {
            datasetNameWithoutTypeSuffix += splitStrings[i];
            if(i < splitStrings.length - 2) {
                datasetNameWithoutTypeSuffix += DATASET_TYPE_DELIMITER;
            }
        }
        return datasetNameWithoutTypeSuffix;
    }
}