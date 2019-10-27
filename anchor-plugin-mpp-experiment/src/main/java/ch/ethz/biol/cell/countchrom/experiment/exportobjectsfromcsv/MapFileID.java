package ch.ethz.biol.cell.countchrom.experiment.exportobjectsfromcsv;

/*
 * #%L
 * anchor-io
 * %%
 * Copyright (C) 2016 ETH Zurich, University of Zurich, Owen Feehan
 * %%
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 * 
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 * 
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 * #L%
 */


import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * Maps FileID (String) to MapChild
 * 
 * @author Owen Feehan
 *
 */
class MapFileID {
	
	// Maps the fileID to a further map
	private Map<String,MapGroupToRow> delegate = new HashMap<String,MapGroupToRow>();
	
	// Maintains a set of all groups used
	private Set<String> setGroups = new HashSet<String>();

	public void put( String fileID, String groupID, CSVRow row ) {
		setGroups.add(groupID);
		
		MapGroupToRow map = delegate.get(fileID);
		
		// Create a GroupToRow if necessary
		if (map==null) {
			map = new MapGroupToRow();
			delegate.put(fileID, map);
		}
		
		map.put( groupID, row );
	}
			
	public Set<String> getSetGroups() {
		return setGroups;
	}

	public MapGroupToRow get(String key) {
		return delegate.get(key);
	}
}