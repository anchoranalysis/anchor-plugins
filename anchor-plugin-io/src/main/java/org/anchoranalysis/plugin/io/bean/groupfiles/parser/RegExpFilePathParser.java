package org.anchoranalysis.plugin.io.bean.groupfiles.parser;

import java.util.Optional;

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


import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.anchoranalysis.bean.annotation.BeanField;

import lombok.Getter;
import lombok.Setter;

public class RegExpFilePathParser extends FilePathParser {

	// START BEAN PARAMETERS
	@BeanField @Getter
	private String expression;
	
	@BeanField @Getter @Setter
	private int chnlGroupID = 0; // 0 Disables
	
	@BeanField @Getter @Setter
	private int zSliceGroupID = 0; // 0 Disables
	
	@BeanField @Getter @Setter
	private int timeIndexGroupID = 0; // 0 Disables
	
	@BeanField @Getter @Setter
	private int keyGroupID = 0;	// 0 Disables
	
	@BeanField @Getter @Setter
	private boolean keyRequired = false;
	// END BEAN PARAMETERS


	private int chnlNum = 0;
	private int zSliceNum = 0;
	private int timeIndex = 0;
	
	private String key;
	
	Pattern pattern = null;
	
	@Override
	public boolean setPath(String path) {
		
		// Replace backslashes with forward slashes
		path = path.replaceAll("\\\\", "/");
		
		Matcher matcher = pattern.matcher( path );
		
		if (!matcher.matches()) {
			return false;
		}
		
		chnlNum = ExtractGroup.maybeExtractInt(chnlGroupID, "chnl", matcher);
		zSliceNum = ExtractGroup.maybeExtractInt(zSliceGroupID, "zSlice", matcher);
		timeIndex = ExtractGroup.maybeExtractInt(timeIndexGroupID, "timeIndex", matcher);
		
		if (chnlNum==-1 || zSliceNum==-1 || timeIndex==-1) {
			return false;
		}
		
		if (keyGroupID > 0) {
			Optional<String> extractedKey = ExtractGroup.extractStr( keyGroupID, matcher, "key" );
			if (!extractedKey.isPresent() || (extractedKey.get().isEmpty() && keyRequired)) {
				return false;
			}
			key = extractedKey.get();
		}
		return true;
	}
	
	@Override
	public Optional<Integer> getChnlNum() {
		return asOptional(chnlGroupID, chnlNum);
	}

	@Override
	public Optional<Integer> getZSliceNum() {
		return asOptional(zSliceGroupID, zSliceNum);
	}
	

	@Override
	public Optional<Integer> getTimeIndex() {
		return asOptional(timeIndexGroupID, timeIndex);
	}
	

	@Override
	public String getKey() {
		return key;
	}

	private static Optional<Integer> asOptional(int groupID, int val) {
		if (groupID > 0) {
			return Optional.of(val);
		} else {
			return Optional.empty();
		}
	}

	public void setExpression(String expression) {
		this.expression = expression;
		this.pattern = Pattern.compile( expression );
	}
}
