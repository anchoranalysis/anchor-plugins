package org.anchoranalysis.plugin.image.feature.bean.shared;



/*
 * #%L
 * anchor-plugin-image-feature
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
import org.anchoranalysis.core.error.InitException;
import org.anchoranalysis.core.name.provider.NamedProviderGetException;
import org.anchoranalysis.core.params.KeyValueParams;
import org.anchoranalysis.feature.cache.SessionInput;
import org.anchoranalysis.feature.calc.FeatureCalcException;
import org.anchoranalysis.feature.input.FeatureInput;
import org.anchoranalysis.feature.input.descriptor.FeatureInputDescriptor;
import org.anchoranalysis.feature.input.descriptor.FeatureInputGenericDescriptor;
import org.anchoranalysis.image.feature.bean.FeatureShared;
import org.anchoranalysis.image.feature.init.FeatureInitParamsShared;

/**
 * Retrieves a parameter from a collection in shared-objects.
 * 
 * <p>This differs from {@link org.anchoranalysis.plugin.operator.feature.bean.Param} which reads
 * the parameter from the nrg-stack, whereas this from a specific parameters collection.</p>
 * 
 * @author Owen Feehan
 *
 * @param <T> feature-input-type
 */
public class ParamFromCollection<T extends FeatureInput> extends FeatureShared<T> {

	// START BEAN PROPERTIES
	@BeanField
	private String collectionID = "";
	
	@BeanField
	private String key = "";
	// END BEAN PROPERTIES

	private double val;
	
	@Override
	public void beforeCalcCast(FeatureInitParamsShared params) throws InitException {
		try {
			KeyValueParams kpv = params.getSharedObjects().getParams()
				.getNamedKeyValueParamsCollection()
				.getException(collectionID);
			this.val = kpv.getPropertyAsDouble(key);
			
		} catch (NamedProviderGetException e) {
			throw new InitException(e.summarize());
		}
	}
	
	@Override
	public double calc(SessionInput<T> input) throws FeatureCalcException {
		return val;
	}

	public String getCollectionID() {
		return collectionID;
	}

	public void setCollectionID(String collectionID) {
		this.collectionID = collectionID;
	}

	public String getKey() {
		return key;
	}

	public void setKey(String key) {
		this.key = key;
	}
	
	@Override
	public FeatureInputDescriptor inputDescriptor() {
		return FeatureInputGenericDescriptor.instance;
	}
}