package org.anchoranalysis.plugin.mpp.experiment.bean.objs.columndefinition;

import java.util.Collection;

import org.anchoranalysis.core.error.InitException;
import org.anchoranalysis.core.functional.StreamWithException;
import org.apache.commons.lang.ArrayUtils;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

/** Finds indices for particular headers in an array */
@NoArgsConstructor(access=AccessLevel.PRIVATE)
class HeaderFinder {
	
	/**
	 * Finds headers in a CSV file that describe a particular object-mask
	 * 
	 * @param headers the headers
	 * @param columnNameNumberVoxels the column describing the exact number of voxels the object has
	 * @param columnNamesPoint three columns (X, Y, Z elements) describing a point that must lie on the object 
	 * @return the indices describing these columns if they exist
	 * @throws InitException if any one column-name does not exist
	 */
	public static ObjectInCsvRowIndices findHeadersToDescribeObject(
		String[] headers,
		String columnNameNumberVoxels,
		Collection<String> columnNamesPoint
	) throws InitException {
		return new ObjectInCsvRowIndices(
			findHeaderIndex(headers, columnNameNumberVoxels),
			findHeaderIndices(headers, columnNamesPoint)
		);
	}
	
	/**
	 * Finds the index of a particular column from the headers of a CSV file
	 * 
	 * @param headers the headers
	 * @param columnName the column whose index to find
	 * @return the index of the first column to be equal (case-sensitive) to {@code columnName}
	 * @throws InitException if the column-name does not exist in the headers
	 */
	public static int findHeaderIndex( String[] headers, String columnName ) throws InitException {
		int index = ArrayUtils.indexOf( headers, columnName );
		if (index==ArrayUtils.INDEX_NOT_FOUND) {
			throw new InitException( String.format("Cannot find column '%s' among CSV file headers", columnName) );
		}
		return index;
	}
	
	/**
	 * Like {@link #findHeaderIndex} but can find several headers from a collection.
	 * 
	 * @param headers the headers
	 * @param columnNames the column-names to find indices for
	 * @return an array of indices, each element corresponding to that in {@link columnNames} respectively
	 * @throws InitException if any column-name does not exist in the headers
	 */
	private static int[] findHeaderIndices(String[] headers, Collection<String> columnNames) throws InitException {
		return StreamWithException.mapToIntWithException(
			columnNames.stream(),
			InitException.class,
			columnName -> findHeaderIndex(headers, columnName)
		).toArray();
	}
}
