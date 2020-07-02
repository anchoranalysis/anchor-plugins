package org.anchoranalysis.plugin.operator.feature.bean;

import org.anchoranalysis.bean.BeanInstanceMap;
import org.anchoranalysis.bean.annotation.AllowEmpty;

/*
 * #%L
 * anchor-feature
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


import org.anchoranalysis.bean.annotation.BeanField;
import org.anchoranalysis.bean.error.BeanMisconfiguredException;
import org.anchoranalysis.core.error.InitException;
import org.anchoranalysis.core.params.KeyValueParams;
import org.anchoranalysis.feature.bean.Feature;
import org.anchoranalysis.feature.cache.SessionInput;
import org.anchoranalysis.feature.calc.FeatureCalcException;
import org.anchoranalysis.feature.input.FeatureInputParams;
import org.anchoranalysis.feature.input.descriptor.FeatureInputDescriptor;
import org.anchoranalysis.feature.input.descriptor.FeatureInputParamsDescriptor;

/**
 * Extracts a key-value-param as a double
 * 
 * <p>This differs from {@link org.anchoranalysis.plugin.image.feature.bean.stack.ParamFromCollection} which reads
 * the parameter from a collection in the shared-objects, rather than from the nrg-stack.</p>
 * 
 * <p>Note the key has an optional prefix and suffix, so that the actual key used is
 * <code>${keyPrefix}${key}${keySuffix}</code></p>
 * 
 * @author Owen Feehan
 *
 * @param <T> feature-input type
 */
public class Param<T extends FeatureInputParams> extends Feature<T> {

	// START BEAN PROPERTIES
	/** Prefix prepended to key */
	@BeanField @AllowEmpty
	String keyPrefix = "";
	
	@BeanField @AllowEmpty
	private String key = "";
	
	@BeanField @AllowEmpty
	String keySuffix = "";
	// END BEAN PROPERTIES

	private String keyAggregated;
	
	@Override
	public void checkMisconfigured(BeanInstanceMap defaultInstances) throws BeanMisconfiguredException {
		super.checkMisconfigured(defaultInstances);
		if (keyPrefix.isEmpty() && key.isEmpty() && keySuffix.isEmpty()) {
			throw new BeanMisconfiguredException("At least one of keyPrefix, key and keySuffix must be non-empty");
		}
	}
	
	@Override
	public void beforeCalc() throws InitException {
		super.beforeCalc();
		keyAggregated = keyAggregated();
	}
	
	@Override
	public double calc(SessionInput<T> input) throws FeatureCalcException {
		
		KeyValueParams kvp = input.get().getParamsRequired();
		
		if (kvp.containsKey(keyAggregated)) {
			return kvp.getPropertyAsDouble(keyAggregated);
		} else {
			throw new FeatureCalcException(
				String.format("Param '%s' is missing", keyAggregated)	
			);
		}
	}
	
	private String keyAggregated() {
		return keyPrefix + key + keySuffix;
	}

	@Override
	public FeatureInputDescriptor inputDescriptor() {
		return FeatureInputParamsDescriptor.instance;
	}

	public String getKey() {
		return key;
	}

	public void setKey(String key) {
		this.key = key;
	}

	public String getKeyPrefix() {
		return keyPrefix;
	}

	public void setKeyPrefix(String keyPrefix) {
		this.keyPrefix = keyPrefix;
	}

	public String getKeySuffix() {
		return keySuffix;
	}

	public void setKeySuffix(String keySuffix) {
		this.keySuffix = keySuffix;
	}
}
