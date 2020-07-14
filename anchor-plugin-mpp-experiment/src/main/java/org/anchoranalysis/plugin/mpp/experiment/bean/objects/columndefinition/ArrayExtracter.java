package org.anchoranalysis.plugin.mpp.experiment.bean.objects.columndefinition;

import org.anchoranalysis.core.geometry.Point3i;

import com.google.common.base.Preconditions;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

/** Extracts element(s) from the array as particular types */
@NoArgsConstructor(access=AccessLevel.PRIVATE)
class ArrayExtracter {
	
	/**
	 * Extracts a point from three indices in string-array
	 * 
	 * @param array the array to extract from
	 * @param indices a three element array with the indices for X, Y, Z component of the point respectively
	 * @return a point constructed from converting the elements for X, Y, Z into integers
	 */
	public static Point3i getAsPoint(String[] array, int[] indices) {
		Preconditions.checkArgument(indices.length==3);
		return new Point3i(
			getAsInt(array, indices[0]),
			getAsInt(array, indices[1]),
			getAsInt(array, indices[2])
		);
	}
	
	/**
	 * Gets a particular index in a string-array, and converts it to an integer
	 * 
	 * @param array the array to extract from
	 * @param index the particular index to extract
	 * @return the element at that index converted into an {@code int}
	 */
	public static int getAsInt( String[] array, int index ) {
		return Integer.parseInt( array[index] );
	}
}
