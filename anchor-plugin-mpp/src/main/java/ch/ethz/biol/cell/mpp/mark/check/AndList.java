package ch.ethz.biol.cell.mpp.mark.check;

/*
 * #%L
 * anchor-plugin-mpp
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


import java.util.ArrayList;
import java.util.List;

import org.anchoranalysis.anchor.mpp.bean.regionmap.RegionMap;
import org.anchoranalysis.anchor.mpp.feature.bean.mark.CheckMark;
import org.anchoranalysis.anchor.mpp.feature.error.CheckException;
import org.anchoranalysis.anchor.mpp.list.OrderedListUtilities;
import org.anchoranalysis.anchor.mpp.mark.Mark;
import org.anchoranalysis.bean.annotation.BeanField;
import org.anchoranalysis.core.error.CreateException;
import org.anchoranalysis.core.error.OperationFailedException;
import org.anchoranalysis.feature.bean.list.FeatureList;
import org.anchoranalysis.feature.nrg.NRGStackWithParams;

public class AndList extends CheckMark {

	/**
	 * 
	 */
	private static final long serialVersionUID = 3477240370689161255L;
	
	// START BEAN PROPERTIES
	@BeanField
	private List<CheckMark> list = new ArrayList<>();
	// END BEAN PROPERTIES
		
	@Override
	public boolean check(Mark mark, RegionMap regionMap, NRGStackWithParams nrgStack) throws CheckException {
		
		for (CheckMark item : list) {
			if (!item.check(mark, regionMap, nrgStack)) {
				return false;
			}
		}
		return true;
	}

	public List<CheckMark> getList() {
		return list;
	}

	public void setList(List<CheckMark> list) {
		this.list = list;
	}

	@Override
	public boolean isCompatibleWith(Mark testMark) {
		for (CheckMark item : list) {
			if (!item.isCompatibleWith(testMark)) {
				return false;
			}
		}
		return true;
	}

	@Override
	public void start(NRGStackWithParams nrgStack) throws OperationFailedException {
		super.start(nrgStack);
		for (CheckMark item : list) {
			item.start(nrgStack);
		}
	}

	@Override
	public void end() {
		super.end();
		for (CheckMark item : list) {
			item.end();
		}
	}
}
