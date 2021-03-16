/*******************************************************************************
* Watchr
* ------
* Copyright 2021 National Technology & Engineering Solutions of Sandia, LLC (NTESS).
* Under the terms of Contract DE-NA0003525 with NTESS, the U.S. Government retains
* certain rights in this software.
******************************************************************************/
package gov.sandia.sems.jenkinsutilities.treeaccessor;

import gov.sandia.sems.jenkinsutilities.db.IDatabasePart;
import gov.sandia.sems.jenkinsutilities.xml.PerformanceReport;
import java.util.List;
import java.util.Map;

/**
 * Interface to an arbitrary, hierarchical database storage mechanism that can
 * store performance report data.
 * 
 * @author Elliott Ridgway
 */
public interface ITreeAccessor extends IDatabasePart {
   
   /**
    * Get the root path of the tree accessor.  This should be the location
    * on disk of the "top" of the tree accessor.
    * 
    * @return The root path of the tree accessor.
    */
   public String getRootPath();
   
      
   /**
    * Returns a String that indicates a hierarchy delimiter (for instance,
    * a forward-slash).
    * 
    * @return The hierarchy delimiter for this tree accessor.
    */
   public String getLevelSeparator();
   
   /**
    * Adds one {@link PerformanceReport} to the tree.
    * 
    * @param report The report to add.
    */
   public void addReport(PerformanceReport report);
   
   /**
    * Returns the {@link OneLevelTree} object located at the provided path.
    * 
    * @param relativePath The path to retrieve the OneLevelTree object from.
    * @return The {@link OneLevelTree} object, or null if nothing could be found.
    */
   public OneLevelTree getNodeAt(String relativePath);

   /**
    * @return The {@link OneLevelTree} at the root of the structure.
    */
   public OneLevelTree getRootNode();
   
   /**
    * Returns all the child {@link OneLevelTree} nodes at the provided path.
    * 
    * @param relativePath The path to start from.
    * @param preferDescendants If no data exists for a given child, return its own children recursively instead.
    * @param measurable The data measurable to filter on.  Leave blank to return all trees regardless of data.
    * @return The list of child {@link OneLevelTree} nodes.
    */
   public List<OneLevelTree> getChildrenAt(String relativePath, boolean preferDescendants, String measurable);
   
   /**
    * Returns the {@link OneLevelTree} parent that is one level above the provided path.
    * 
    * @param relativePath The path to start from.
    * @return The parent {@link OneLevelTree} object that is one level up.
    */
   public OneLevelTree getParentAt(String relativePath);

   /**
    * Explore the ITreeAccessor object for child {@link OneLevelTree} objects whose names
    * match the search term {@code searchTerm}.
    * @param startPath The path in the tree to start from.
    * @param searchTerm The name to match on.  It is up to implementors to decide how to
    * interpret this string (i.e. whether or not it's read as a regular expression).
    * @param searchAllDescendants If true, then recursively search the entire tree for matches.
    * If false, only search the immediate children.
    * @param returnEmptyResults If true, a search result will be returned even if it is an
    * empty OneLevelTree.
    * @return A {@link List} of OneLevelTree objects found by the search.
    */
   public List<OneLevelTree> searchForChildren(
      String startPath, String searchTerm, boolean searchAllDescendants, boolean returnEmptyResults);
   
   /**
    * The build-to-hashes map is a data structure used to quickly determine whether
    * a given performance report has already been seen in a previous build, and which
    * build it was seen on.
    * @return The build-to-hashes map.
    */
   public Map<Integer, Map<String, List<String>>> getBuildToHashesMap();
}
