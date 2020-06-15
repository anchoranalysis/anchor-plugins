package org.anchoranalysis.plugin.image.task.bean.feature;

/*-
 * #%L
 * anchor-plugin-image-task
 * %%
 * Copyright (C) 2010 - 2019 Owen Feehan, ETH Zurich, University of Zurich, Hoffmann la Roche
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

import org.anchoranalysis.core.name.MultiName;

class GroupAndImageNames implements MultiName {
	
	private String groupName;
	private String imageName;
	
	public GroupAndImageNames(String groupName, String imageName) {
		super();
		this.groupName = groupName;
		this.imageName = imageName;
	}

	@Override
	public int numParts() {
		return 2;
	}

	@Override
	public String getPart(int index) {
		switch(index) {
		case 0:
			return imageName;
		case 1:
			return groupName;
		default:
			assert false;
			return "";
		}
	}

	@Override
	public String getAggregateKeyName() {
		// On a high level, images combining to the same group can be combined
		return groupName;
	}

	@Override
	public int compareTo(MultiName arg0) {
		int comparisonFirst = imageName.compareTo(arg0.getPart(0));
		if (comparisonFirst!=0) {
			return comparisonFirst;
		}
		
		if (arg0.numParts()>1) {
			return groupName.compareTo(arg0.getPart(1));
		}
		
		return 0;
	}
	
	
}
