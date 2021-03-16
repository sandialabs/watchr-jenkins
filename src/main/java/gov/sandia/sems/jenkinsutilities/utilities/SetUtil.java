/*******************************************************************************
* Watchr
* ------
* Copyright 2021 National Technology & Engineering Solutions of Sandia, LLC (NTESS).
* Under the terms of Contract DE-NA0003525 with NTESS, the U.S. Government retains
* certain rights in this software.
******************************************************************************/
package gov.sandia.sems.jenkinsutilities.utilities;

import java.util.List;
import java.util.Set;

public class SetUtil {
    ///////////
	// CHECK //
	///////////
	
	/**
	 * Checks whether an {@link Object} is a {@link List} of a specific {@link Class} type.
	 * @param obj The object to test.
	 * @param clazz The Class to test for.
	 * @return True if the object is a List of type {@code clazz}.
	 */
	public static boolean isSetOf(Object obj, Class<?> clazz) {
		if(obj instanceof List || obj.getClass().isAssignableFrom(Set.class)) {
			Set<?> set = (Set<?>) obj;
			return everyElementInSetIsOfType(set, clazz);
		}
		return false;
	}
	
	/**
	 * Checks whether every element in a {@link List} is of a specific {@link Class} type.
	 * @param elements The List of elements.
	 * @param clazz The Class type to check for.
	 * @return True if all List elements are of a specific Class type.
	 */
	public static boolean everyElementInSetIsOfType(Set<?> elements, Class<?> clazz) {
		for(Object element : elements) {
			if(element.getClass() != clazz && !element.getClass().isAssignableFrom(clazz)) {
				return false;
			}
		}
		return true;
	}
}