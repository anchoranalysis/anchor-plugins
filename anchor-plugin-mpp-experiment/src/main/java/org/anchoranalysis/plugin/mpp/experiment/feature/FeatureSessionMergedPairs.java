package org.anchoranalysis.plugin.mpp.experiment.feature;

/*
 * #%L
 * anchor-image-feature
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


import java.util.Collection;

import org.anchoranalysis.core.error.InitException;
import org.anchoranalysis.core.error.reporter.ErrorReporter;
import org.anchoranalysis.core.log.LogErrorReporter;
import org.anchoranalysis.feature.bean.list.FeatureList;
import org.anchoranalysis.feature.calc.FeatureCalcException;
import org.anchoranalysis.feature.calc.FeatureInitParams;
import org.anchoranalysis.feature.calc.results.ResultsVector;
import org.anchoranalysis.feature.input.FeatureInputNRGStack;
import org.anchoranalysis.feature.name.FeatureNameList;
import org.anchoranalysis.feature.nrg.NRGStackWithParams;
import org.anchoranalysis.feature.session.FeatureSession;
import org.anchoranalysis.feature.session.calculator.FeatureCalculatorCachedResults;
import org.anchoranalysis.feature.session.calculator.FeatureCalculatorMulti;
import org.anchoranalysis.feature.session.calculator.FeatureCalculatorMultiChangeInput;
import org.anchoranalysis.feature.session.calculator.FeatureCalculatorMultiReuse;
import org.anchoranalysis.feature.shared.SharedFeatureSet;
import org.anchoranalysis.feature.shared.SharedFeaturesInitParams;
import org.anchoranalysis.image.feature.init.FeatureInitParamsSharedObjs;
import org.anchoranalysis.image.feature.objmask.FeatureInputSingleObj;
import org.anchoranalysis.image.feature.objmask.pair.merged.FeatureInputPairObjsMerged;
import org.anchoranalysis.image.feature.stack.FeatureInputStack;
import org.anchoranalysis.image.init.ImageInitParams;

public class FeatureSessionMergedPairs extends FeatureSessionFlexiFeatureTable<FeatureInputPairObjsMerged> {

	private boolean includeFirst;
	private boolean includeSecond;
	
	// Our sessions
	private FeatureCalculatorMulti<FeatureInputStack> sessionImage;
	
	// We avoid using seperate sessions for First and Second, as we want them
	//  to share the same Vertical-Cache for object calculation.
	private FeatureCalculatorMulti<FeatureInputSingleObj> sessionFirstSecond;
	private FeatureCalculatorMulti<FeatureInputSingleObj> sessionMerged;
	private FeatureCalculatorMulti<FeatureInputPairObjsMerged> sessionPair;

	// The lists we need
	private FeatureList<FeatureInputStack> listImage;
	private FeatureList<FeatureInputSingleObj> listSingle;
	private FeatureList<FeatureInputPairObjsMerged> listPair;
	private boolean checkInverse = false;
	private boolean suppressErrors = false;
	
	// Prefixes that are ignored
	private Collection<String> ignoreFeaturePrefixes;
	
	public FeatureSessionMergedPairs(
		boolean includeFirst,
		boolean includeSecond,
		FeatureList<FeatureInputStack> listImage,
		FeatureList<FeatureInputSingleObj> listSingle,
		FeatureList<FeatureInputPairObjsMerged> listPair,
		Collection<String> ignoreFeaturePrefixes,
		boolean checkInverse,
		boolean suppressErrors
	) {
		this.includeFirst = includeFirst;
		this.includeSecond = includeSecond;
		this.listImage = listImage;
		this.listSingle = listSingle;
		this.listPair = listPair;
		this.checkInverse = checkInverse;
		this.suppressErrors = suppressErrors;
		this.ignoreFeaturePrefixes = ignoreFeaturePrefixes;
	}
	
	
//	private SharedFeatureList createSharedFeatures( SharedObjectsFeature soFeature, FeatureList fl ) {
//		SharedFeatureList out = new SharedFeatureList();
//		out.add( soFeature.getSharedFeatureSet() );
//		fl.copyToCustomName(out.getSet(),false);
//		return out;
//	}
	
	@Override
	public void start(
		ImageInitParams soImage,
		SharedFeaturesInitParams soFeature,
		NRGStackWithParams nrgStack,
		LogErrorReporter logErrorReporter
	) throws InitException {
		
		// We create our SharedFeatures including anything from the NamedDefinitions, and all our additional features
		// TODO fix
		//SharedFeatureSet<FeatureCalcParams> sharedFeatures = soFeature.getSharedFeatureSet();
		// sharedFeatures = createSharedFeatures(soFeature,listSingle);
		//listImage.copyToCustomName(sharedFeatures.getSet(),false);
		//listSingle.copyToCustomName(sharedFeatures.getSet(),false);
		//listPair.copyToCustomName(sharedFeatures.getSet(),false);
		
		
		// We create more caches for the includeFirst and includeSecond Features and merged features.
		
		FeatureInitParamsSharedObjs paramsInitPair = new FeatureInitParamsSharedObjs( soImage );
		paramsInitPair.setKeyValueParams( nrgStack.getParams() );
		paramsInitPair.setNrgStack(nrgStack.getNrgStack());
		
		logErrorReporter.getLogReporter().log("Setting up: Image Features");

		
		sessionImage = new FeatureCalculatorMultiReuse<FeatureInputStack>( 
			createCalculator(listImage, soImage, nrgStack, logErrorReporter)	
		);
		
		if (includeFirst || includeSecond) {
			logErrorReporter.getLogReporter().log("Setting up: First/Second Features");
			sessionFirstSecond = new FeatureCalculatorCachedResults<>(
				createCalculator(listSingle, soImage, nrgStack, logErrorReporter),
				suppressErrors
			);

		}
			
		
		logErrorReporter.getLogReporter().log("Setting up: Pair Features");
		
		// TODO to make this more efficient, it would be better if we could re-use the cached-operations
		//  from the calculation of the First and Second individual features, as they appear again
		//  as additionalCaches of sessionPair
		// TODO fix no shared features anymore, prev sharedFeatures.duplicate()		
		sessionPair = createCalculator(listPair, soImage, nrgStack, logErrorReporter);
		

			
		
		// We keep a seperate session for merges, as there is no need to do caching. But we copy features
		//  to make sure there's no collision of the caches
		//System.out.println("SessionMerged");
		logErrorReporter.getLogReporter().log("Setting up: Merged Features");
		sessionMerged =  createCalculator(listSingle.duplicateBean(), soImage, nrgStack, logErrorReporter );
		
		// TODO fix no shared features anymore, prev sharedFeatures.duplicate()

		
//		System.out.printf("Session Image = %d\n", sessionImage );
//		System.out.printf("Session First = %d\n", sessionFirst );
//		System.out.printf("Session Second = %d\n", sessionSecond);
//		System.out.printf("Session Image = %d\n", sessionImage);
//		System.out.printf("Session Merged = %d\n", sessionMerged);
	}

	@Override
	public FeatureSessionFlexiFeatureTable<FeatureInputPairObjsMerged> duplicateForNewThread() {
		return new FeatureSessionMergedPairs(
			includeFirst,
			includeSecond,
			listImage.duplicateBean(),
			listSingle.duplicateBean(),
			listPair.duplicateBean(),
			ignoreFeaturePrefixes,	// NOT DUPLICATED
			checkInverse,
			suppressErrors
		);
	}

	@Override
	public ResultsVector calcMaybeSuppressErrors(FeatureInputPairObjsMerged params, ErrorReporter errorReporter)
			throws FeatureCalcException {
		
		ResultsVector rv = calcForParams(params,errorReporter);
		
		if (checkInverse) {
			
			ResultsVector rvInverse = calcForParams(
				params.createInverse(),
				errorReporter
			);
			
			InverseChecker checker = new InverseChecker(
				includeFirst,
				includeSecond,
				listImage.size(),
				listSingle.size(),
				() -> createFeatureNames()
			);
			checker.checkInverseEqual(rv, rvInverse, params);
		}
		
		return rv;
	}


	private ResultsVector calcForParams(FeatureInputPairObjsMerged params, ErrorReporter errorReporter) throws FeatureCalcException {
		
		ResultsVectorBuilder helper = new ResultsVectorBuilder(size(), suppressErrors, errorReporter);
		
		// First we calculate the Image features (we rely on the NRG stack being added by the calculator)
		helper.calcAndInsert(new FeatureInputStack(), sessionImage );
		
		// First features
		if (includeFirst) {
			helper.calcAndInsert( params, FeatureInputPairObjsMerged::getObjMask1, sessionFirstSecond );
		}
		
		// Second features
		if (includeSecond) {
			helper.calcAndInsert( params, FeatureInputPairObjsMerged::getObjMask2, sessionFirstSecond );
		}
		
		// Pair features
		helper.calcAndInsert(params, sessionPair );
		
		// Merged. Because we know we have FeatureObjMaskPairMergedParams, we don't need to change params
		helper.calcAndInsert(params, FeatureInputPairObjsMerged::getObjMaskMerged, sessionMerged );
		
		assert(helper.getResultsVector().hasNoNulls());
		return helper.getResultsVector();
	}
	
	
	
	private <T extends FeatureInputNRGStack> FeatureCalculatorMulti<T> createCalculator( FeatureList<T> features, ImageInitParams soImage, NRGStackWithParams nrgStack, LogErrorReporter logErrorReporter ) throws InitException {

		try {
			FeatureCalculatorMulti<T> calculator = FeatureSession.with(
				features,
				createInitParams(soImage, nrgStack),
				new SharedFeatureSet<>(),
				logErrorReporter,
				ignoreFeaturePrefixes
			);
			return new FeatureCalculatorMultiChangeInput<T>(
				calculator,
				params->params.setNrgStack(nrgStack)
			);
			
		} catch (FeatureCalcException e) {
			throw new InitException(e);
		}
	}
	
	
	
	private static FeatureInitParams createInitParams(ImageInitParams soImage, NRGStackWithParams nrgStack) {
		return InitParamsHelper.createInitParams(
			soImage,
			nrgStack.getNrgStack(),
			nrgStack.getParams()
		);
	}
	

	@Override
	public FeatureNameList createFeatureNames() {
		FeatureNameList out = new FeatureNameList();
		
		out.addCustomNamesWithPrefix( "image.", listImage );
		
		if (includeFirst) {
			out.addCustomNamesWithPrefix( "first.", listSingle );
		}
		
		if (includeSecond) {
			out.addCustomNamesWithPrefix( "second.", listSingle );
		}
		
		out.addCustomNamesWithPrefix( "pair.", listPair );
		
		out.addCustomNamesWithPrefix( "merged.", listSingle );
		return out;
	}

	
	/**
	 * Integer value from boolean
	 * 
	 * @param b
	 * @return 0 for FALSE, 1 for TRUE
	 */
	private static int integerFromBoolean( boolean b ) {
		return b ? 1 : 0;
	}
	
	
	@Override
	public int size() {
		
		// Number of times we use the listSingle
		int numSingle = 1 + integerFromBoolean(includeFirst) + integerFromBoolean(includeSecond);
				
		return listImage.size() + listPair.size() + (numSingle * listSingle.size());
	}

	public boolean isCheckInverse() {
		return checkInverse;
	}

	public void setCheckInverse(boolean checkInverse) {
		this.checkInverse = checkInverse;
	}


	public boolean isSuppressErrors() {
		return suppressErrors;
	}


	public void setSuppressErrors(boolean suppressErrors) {
		this.suppressErrors = suppressErrors;
	}

}