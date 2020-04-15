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
import org.anchoranalysis.feature.calc.ResultsVector;
import org.anchoranalysis.feature.calc.params.FeatureCalcParams;
import org.anchoranalysis.feature.init.FeatureInitParams;
import org.anchoranalysis.feature.name.FeatureNameList;
import org.anchoranalysis.feature.nrg.NRGStackWithParams;
import org.anchoranalysis.feature.session.SessionFactory;
import org.anchoranalysis.feature.session.calculator.FeatureCalculatorCachedResults;
import org.anchoranalysis.feature.session.calculator.FeatureCalculatorMulti;
import org.anchoranalysis.feature.session.calculator.FeatureCalculatorMultiReuse;
import org.anchoranalysis.feature.shared.SharedFeatureSet;
import org.anchoranalysis.feature.shared.SharedFeaturesInitParams;
import org.anchoranalysis.image.feature.init.FeatureInitParamsImageInit;
import org.anchoranalysis.image.feature.objmask.FeatureObjMaskParams;
import org.anchoranalysis.image.feature.objmask.pair.merged.FeatureObjMaskPairMergedParams;
import org.anchoranalysis.image.feature.stack.FeatureStackParams;
import org.anchoranalysis.image.init.ImageInitParams;

public class FeatureSessionMergedPairs extends FeatureSessionFlexiFeatureTable<FeatureObjMaskPairMergedParams> {

	private boolean includeFirst;
	private boolean includeSecond;
	
	// Our sessions
	private FeatureCalculatorMulti<FeatureStackParams> sessionImage;
	
	// We avoid using seperate sessions for First and Second, as we want them
	//  to share the same Vertical-Cache for object calculation.
	private FeatureCalculatorMulti<FeatureObjMaskParams> sessionFirstSecond;
	private FeatureCalculatorMulti<FeatureObjMaskParams> sessionMerged;
	private FeatureCalculatorMulti<FeatureObjMaskPairMergedParams> sessionPair;

	// The lists we need
	private FeatureList<FeatureStackParams> listImage;
	private FeatureList<FeatureObjMaskParams> listSingle;
	private FeatureList<FeatureObjMaskPairMergedParams> listPair;
	private boolean checkInverse = false;
	private boolean suppressErrors = false;
	
	// Prefixes that are ignored
	private Collection<String> ignoreFeaturePrefixes;
	
	public FeatureSessionMergedPairs(
		boolean includeFirst,
		boolean includeSecond,
		FeatureList<FeatureStackParams> listImage,
		FeatureList<FeatureObjMaskParams> listSingle,
		FeatureList<FeatureObjMaskPairMergedParams> listPair,
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
		
		FeatureInitParamsImageInit paramsInitPair = new FeatureInitParamsImageInit( soImage );
		paramsInitPair.setKeyValueParams( nrgStack.getParams() );
		paramsInitPair.setNrgStack(nrgStack.getNrgStack());
		
		logErrorReporter.getLogReporter().log("Setting up: Image Features");

		
		sessionImage = new FeatureCalculatorMultiReuse<FeatureStackParams>( 
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
	public FeatureSessionFlexiFeatureTable<FeatureObjMaskPairMergedParams> duplicateForNewThread() {
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
	public ResultsVector calcMaybeSuppressErrors(FeatureObjMaskPairMergedParams params, ErrorReporter errorReporter)
			throws FeatureCalcException {
		
		ResultsVector rv = calcForParams(params,errorReporter);
		
		if (checkInverse) {
			//DEBUG
			//System.out.printf("Calculating inverse for %s\n", params ); 
			
			FeatureCalcParams paramsInv = params.createInverse();
			assert( paramsInv!=null );
			ResultsVector rvInverse = calcForParams(paramsInv,errorReporter);
			
			assert(rvInverse!=null);
			
			
			InverseChecker checker = new InverseChecker(
				includeFirst,
				includeSecond,
				listImage.size(),
				listSingle.size(),
				() -> createFeatureNames()
			);
						
			StringBuilder sb = new StringBuilder();
			if (!checker.isInverseEqual(rv, rvInverse, sb)) {
				throw new FeatureCalcException(
					String.format("Feature values are not equal to the inverse for %s:%n%s", params, sb.toString() )
				);
			}
		}
		
		return rv;
	}


	private ResultsVector calcForParams(FeatureCalcParams params, ErrorReporter errorReporter) throws FeatureCalcException {
		
		assert(params instanceof FeatureObjMaskPairMergedParams);
		FeatureObjMaskPairMergedParams paramsCast = (FeatureObjMaskPairMergedParams) params;
		
		ResultsVectorBuilder helper = new ResultsVectorBuilder(size(), suppressErrors, errorReporter);
		
		// First we calculate the Image features (we can pick any object)
		// TODO ignoring image features. Fix.
		//calcAndInsert(paramsCast, (a)->a.getObjMask1(), sessionImage );
		
		// First features
		if (includeFirst) {
			helper.calcAndInsert( paramsCast, (a)->a.getObjMask1(), sessionFirstSecond );
		}
		
		// Second features
		if (includeSecond) {
			helper.calcAndInsert( paramsCast, (a)->a.getObjMask2(), sessionFirstSecond );
		}
		
		// Pair features
		helper.calcAndInsert(paramsCast, sessionPair );
		
		// Merged. Because we know we have FeatureObjMaskPairMergedParams, we don't need to change params
		helper.calcAndInsert(paramsCast, (a)->a.getObjMaskMerged(), sessionMerged );
		
		assert(helper.getResultsVector().hasNoNulls());
		return helper.getResultsVector();
	}
	
	
	
	private <T extends FeatureCalcParams> FeatureCalculatorMulti<T> createCalculator( FeatureList<T> features, ImageInitParams soImage, NRGStackWithParams nrgStack, LogErrorReporter logErrorReporter ) throws InitException {

		try {
			return SessionFactory.createAndStart(
				features,
				createInitParams(soImage, nrgStack),
				new SharedFeatureSet<>(),
				logErrorReporter,
				ignoreFeaturePrefixes
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
		
		int cnt = listImage.size() + listPair.size() + (numSingle * listSingle.size());
		
		return cnt;
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