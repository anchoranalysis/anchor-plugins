package ch.ethz.biol.cell.mpp.nrg.feature.objmask;

/*
 * #%L
 * anchor-plugin-image
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


import org.anchoranalysis.bean.annotation.AllowEmpty;
import org.anchoranalysis.bean.annotation.BeanField;
import org.anchoranalysis.feature.calc.FeatureCalcException;
import org.anchoranalysis.image.feature.bean.stack.nrg.FeatureNRGStack;
import org.anchoranalysis.image.feature.stack.nrg.FeatureNRGStackParams;

/**
 * Same behaviour as NRGParam, except uses an id composed of three parts that are concatenated
 * 
 * @author Owen Feehan
 *
 */
public class NRGParamThree extends FeatureNRGStack {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	// START BEAN PROPERTIES
	@BeanField @AllowEmpty
	private String idLeft;
	
	@BeanField @AllowEmpty
	private String idMiddle;
	
	@BeanField @AllowEmpty
	private String idRight;
	// END BEAN PROPERTIES
	
	private String createID() {
		StringBuilder sb = new StringBuilder();
		sb.append(idLeft);
		sb.append(idMiddle);
		sb.append(idRight);
		return sb.toString();
	}
	
	@Override
	public double calcCast(FeatureNRGStackParams params)
			throws FeatureCalcException {
		
		String id = createID();
		return NRGParam.calcForPropertyID(params, id);
	}

	public String getIdLeft() {
		return idLeft;
	}

	public void setIdLeft(String idLeft) {
		this.idLeft = idLeft;
	}

	public String getIdMiddle() {
		return idMiddle;
	}

	public void setIdMiddle(String idMiddle) {
		this.idMiddle = idMiddle;
	}

	public String getIdRight() {
		return idRight;
	}

	public void setIdRight(String idRight) {
		this.idRight = idRight;
	}

	@Override
	public String getParamDscr() {
		return String.format("%s %s %s", idLeft, idMiddle, idRight);
	}
}
