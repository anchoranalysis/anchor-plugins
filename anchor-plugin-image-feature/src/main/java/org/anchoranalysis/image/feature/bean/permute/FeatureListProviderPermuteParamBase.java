package org.anchoranalysis.image.feature.bean.permute;

import java.util.ArrayList;

import org.anchoranalysis.bean.BeanInstanceMap;
import org.anchoranalysis.bean.annotation.BeanField;
import org.anchoranalysis.bean.error.BeanMisconfiguredException;
import org.anchoranalysis.bean.permute.property.PermutePropertySequenceInteger;
import org.anchoranalysis.core.error.CreateException;
import org.anchoranalysis.feature.bean.Feature;
import org.anchoranalysis.feature.bean.list.FeatureList;
import org.anchoranalysis.feature.input.FeatureInputNRGStack;

import ch.ethz.biol.cell.mpp.nrg.feature.objmask.NRGParamThree;

public abstract class FeatureListProviderPermuteParamBase extends FeatureListProviderPermuteBase<FeatureInputNRGStack> {

	// START BEAN PROPERTIES
	@BeanField
	private String paramPrefix;
	
	@BeanField
	private PermutePropertySequenceInteger permuteProperty;
	// END BEAN PROPERTIES
	
	// Possible defaultInstances for beans......... saved from checkMisconfigured for delayed checks elsewhere
	private BeanInstanceMap defaultInstances;
	
	@Override
	public void checkMisconfigured(BeanInstanceMap defaultInstances) throws BeanMisconfiguredException {
		super.checkMisconfigured(defaultInstances);
		this.defaultInstances = defaultInstances;
	}
		
	@Override
	protected FeatureList<FeatureInputNRGStack> createPermutedFeaturesFor(Feature<FeatureInputNRGStack> feature) throws CreateException {
		FeatureListProviderPermute<Integer,FeatureInputNRGStack> delegate = createDelegate(feature);
		
		configurePermutePropertyOnDelegate(delegate);
		try {
			delegate.checkMisconfigured( defaultInstances );
		} catch (BeanMisconfiguredException e) {
			throw new CreateException(e);
		}
				
		return delegate.create();
	}
	
	protected abstract FeatureListProviderPermute<Integer,FeatureInputNRGStack> createDelegate(Feature<FeatureInputNRGStack> feature) throws CreateException;
	
	protected abstract PermutePropertySequenceInteger configurePermuteProperty( PermutePropertySequenceInteger permuteProperty );
	
	protected Feature<FeatureInputNRGStack> createNRGParam(
		String suffix,
		boolean appendNumber
	) {
		NRGParamThree paramMean = new NRGParamThree();
		paramMean.setIdLeft(paramPrefix);
		if (appendNumber) {
			paramMean.setIdMiddle(
				Integer.toString(permuteProperty.getSequence().getStart())
			);
		} else {
			paramMean.setIdMiddle("");
		}
		paramMean.setIdRight(suffix);
		return paramMean;
	}

	private void configurePermutePropertyOnDelegate( FeatureListProviderPermute<Integer,FeatureInputNRGStack> delegate ) {
		PermutePropertySequenceInteger permutePropertyConfigured = configurePermuteProperty(
			(PermutePropertySequenceInteger) permuteProperty.duplicateBean()
		);
		
		delegate.setListPermuteProperty( new ArrayList<>() );
		delegate.getListPermuteProperty().add( permutePropertyConfigured );		
	}
	
	public String getParamPrefix() {
		return paramPrefix;
	}

	public void setParamPrefix(String paramPrefix) {
		this.paramPrefix = paramPrefix;
	}
	
	public PermutePropertySequenceInteger getPermuteProperty() {
		return permuteProperty;
	}

	public void setPermuteProperty(PermutePropertySequenceInteger permuteProperty) {
		this.permuteProperty = permuteProperty;
	}
}
