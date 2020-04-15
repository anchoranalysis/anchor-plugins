package org.anchoranalysis.gui.annotation.bean.label;

/*-
 * #%L
 * anchor-plugin-annotation
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

import org.anchoranalysis.bean.AnchorBean;
import org.anchoranalysis.bean.annotation.AllowEmpty;
import org.anchoranalysis.bean.annotation.BeanField;
import org.anchoranalysis.bean.annotation.OptionalBean;
import org.anchoranalysis.io.bean.color.RGBColorBean;

public class AnnotationLabel extends AnchorBean<AnnotationLabel> {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	// START BEAN PROPERTIES
	/** Label that uniquely identifies the ID (for machine purposes) */
	@BeanField
	private String uniqueLabel;
	
	@BeanField
	/** Descriptive user-friendly label displayed via GUI */
	private String userFriendlyLabel;
	
	@BeanField @OptionalBean
	/** An optional color associated with the label when displayed via GUI */
	private RGBColorBean color;
	
	/** Specifies a group for the label (similar labels that are displayed together) */
	@BeanField @AllowEmpty
	private String group;
	// END BEAN PROPERTIES

	public String getUniqueLabel() {
		return uniqueLabel;
	}

	public void setUniqueLabel(String uniqueLabel) {
		this.uniqueLabel = uniqueLabel;
	}

	public String getUserFriendlyLabel() {
		return userFriendlyLabel;
	}

	public void setUserFriendlyLabel(String userFriendlyLabel) {
		this.userFriendlyLabel = userFriendlyLabel;
	}

	public RGBColorBean getColor() {
		return color;
	}

	public void setColor(RGBColorBean color) {
		this.color = color;
	}

	public String getGroup() {
		return group;
	}

	public void setGroup(String group) {
		this.group = group;
	}
}
