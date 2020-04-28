package org.anchoranalysis.plugin.mpp.experiment.feature;

import java.util.Collection;

import org.anchoranalysis.core.error.InitException;
import org.anchoranalysis.core.log.LogErrorReporter;
import org.anchoranalysis.feature.bean.list.FeatureList;
import org.anchoranalysis.feature.calc.FeatureCalcException;
import org.anchoranalysis.feature.calc.FeatureInitParams;
import org.anchoranalysis.feature.input.FeatureInputNRGStack;
import org.anchoranalysis.feature.nrg.NRGStackWithParams;
import org.anchoranalysis.feature.session.FeatureSession;
import org.anchoranalysis.feature.session.calculator.FeatureCalculatorCachedResults;
import org.anchoranalysis.feature.session.calculator.FeatureCalculatorMulti;
import org.anchoranalysis.feature.session.calculator.FeatureCalculatorMultiChangeInput;
import org.anchoranalysis.feature.shared.SharedFeatureSet;
import org.anchoranalysis.image.init.ImageInitParams;

class CreateCalculatorHelper {

	// Prefixes that are ignored
	private Collection<String> ignoreFeaturePrefixes;
	private NRGStackWithParams nrgStack;
	private LogErrorReporter logErrorReporter;
		
	public CreateCalculatorHelper(
		Collection<String> ignoreFeaturePrefixes,
		NRGStackWithParams nrgStack,
		LogErrorReporter logErrorReporter
	) {
		super();
		this.ignoreFeaturePrefixes = ignoreFeaturePrefixes;
		this.nrgStack = nrgStack;
		this.logErrorReporter = logErrorReporter;
	}	
		
	public <T extends FeatureInputNRGStack> FeatureCalculatorMulti<T> createCached(
		FeatureList<T> features,
		ImageInitParams soImage,
		boolean suppressErrors
	) throws InitException {
		return wrapWithNrg( 
			new FeatureCalculatorCachedResults<>(
				createWithoutNrg(features, soImage),
				suppressErrors
			)
		);		
	}
	
	public <T extends FeatureInputNRGStack> FeatureCalculatorMulti<T> create(
		FeatureList<T> features,
		ImageInitParams soImage
	) throws InitException {
		return wrapWithNrg(
			createWithoutNrg(features, soImage)
		);		
	}
	
	private <T extends FeatureInputNRGStack> FeatureCalculatorMulti<T> createWithoutNrg(
		FeatureList<T> features,
		ImageInitParams soImage
	) throws InitException {

		try {
			return FeatureSession.with(
				features,
				createInitParams(soImage),
				new SharedFeatureSet<>(),
				logErrorReporter,
				ignoreFeaturePrefixes
			);
			
		} catch (FeatureCalcException e) {
			throw new InitException(e);
		}
	}
	
	/** Ensures any input-parameters have the NRG-stack attached */
	private <T extends FeatureInputNRGStack> FeatureCalculatorMulti<T> wrapWithNrg(
		FeatureCalculatorMulti<T> calculator
	) {
		return new FeatureCalculatorMultiChangeInput<T>(
			calculator,
			params->params.setNrgStack(nrgStack)
		);
	}
	
	private FeatureInitParams createInitParams(ImageInitParams soImage) {
		return InitParamsHelper.createInitParams(
			soImage,
			nrgStack.getNrgStack(),
			nrgStack.getParams()
		);
	}
}
