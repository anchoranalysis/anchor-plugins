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
import org.anchoranalysis.feature.calc.FeatureCalcException;
import org.anchoranalysis.feature.calc.results.ResultsVector;
import org.anchoranalysis.feature.name.FeatureNameList;
import org.anchoranalysis.feature.nrg.NRGStackWithParams;
import org.anchoranalysis.feature.session.calculator.FeatureCalculatorMulti;
import org.anchoranalysis.feature.session.calculator.FeatureCalculatorMultiReuse;
import org.anchoranalysis.feature.shared.SharedFeaturesInitParams;
import org.anchoranalysis.image.feature.init.FeatureInitParamsSharedObjs;
import org.anchoranalysis.image.feature.objmask.FeatureInputSingleObj;
import org.anchoranalysis.image.feature.objmask.pair.FeatureInputPairObjs;
import org.anchoranalysis.image.feature.stack.FeatureInputStack;
import org.anchoranalysis.image.init.ImageInitParams;


/**
 * A particular type of feature-session where successive pairs of objects are evaluated by features in five different ways:
 * 
 * <div>
 * <ul>
 * the image in which the object exists (on {@link #listImage}) i.e. the nrg-stack.
 * the left-object in the pair (on {@link #listSingle})
 * the right-object in the pair (on {@link #listSingle})
 * the pair (on {@link #listPair})
 * both objects merged together (on {@link #listSingle}}
 * </ul>
 * </div>
 * 
 * <p>Due to the predictable pattern, feature-calculations can be cached predictably and appropriately to avoid redundancies</p>.
 * 
 * @author Owen Feehan
 *
 */
public class MergedPairsSession extends FeatureSessionFlexiFeatureTable<FeatureInputPairObjs> {

	private boolean includeFirst;
	private boolean includeSecond;
	
	// Our sessions
	private FeatureCalculatorMulti<FeatureInputStack> sessionImage;
	
	// We avoid using seperate sessions for First and Second, as we want them
	//  to share the same Vertical-Cache for object calculation.
	private FeatureCalculatorMulti<FeatureInputSingleObj> sessionFirstSecond;
	private FeatureCalculatorMulti<FeatureInputSingleObj> sessionMerged;
	private FeatureCalculatorMulti<FeatureInputPairObjs> sessionPair;

	// The lists we need
	private MergedPairsFeatures features;
	private boolean checkInverse = false;
	private boolean suppressErrors = false;
	
	// Prefixes that are ignored
	private Collection<String> ignoreFeaturePrefixes;
	
	public MergedPairsSession(
		boolean includeFirst,
		boolean includeSecond,
		MergedPairsFeatures features,
		Collection<String> ignoreFeaturePrefixes,
		boolean checkInverse,
		boolean suppressErrors
	) {
		this.includeFirst = includeFirst;
		this.includeSecond = includeSecond;
		this.features = features;
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

		CreateCalculatorHelper cc = new CreateCalculatorHelper(ignoreFeaturePrefixes, nrgStack,	logErrorReporter);
		
		sessionImage = new FeatureCalculatorMultiReuse<FeatureInputStack>( 
			cc.create(features.getImage(), soImage)
		);
		
		if (includeFirst || includeSecond) {
			logErrorReporter.getLogReporter().log("Setting up: First/Second Features");
			sessionFirstSecond = cc.createCached(
				features.getSingle(),
				soImage,
				suppressErrors
			);
		}
		
		logErrorReporter.getLogReporter().log("Setting up: Pair Features");
		
		// TODO to make this more efficient, it would be better if we could re-use the cached-operations
		//  from the calculation of the First and Second individual features, as they appear again
		//  as additionalCaches of sessionPair
		// TODO fix no shared features anymore, prev sharedFeatures.duplicate()		
		sessionPair = cc.create(features.getPair(), soImage);
				
		// We keep a separate session for merges, there's no caches here that can be reused 
		sessionMerged =  cc.create(features.getSingle(), soImage);
	}

	@Override
	public FeatureSessionFlexiFeatureTable<FeatureInputPairObjs> duplicateForNewThread() {
		return new MergedPairsSession(
			includeFirst,
			includeSecond,
			features.duplicate(),
			ignoreFeaturePrefixes,	// NOT DUPLICATED
			checkInverse,
			suppressErrors
		);
	}

	@Override
	public ResultsVector calcMaybeSuppressErrors(FeatureInputPairObjs params, ErrorReporter errorReporter)
			throws FeatureCalcException {
		
		ResultsVector rv = calcForInput(params,errorReporter);
		
		if (checkInverse) {
			
			ResultsVector rvInverse = calcForInput(
				params.createInverse(),
				errorReporter
			);
			
			InverseChecker checker = new InverseChecker(
				includeFirst,
				includeSecond,
				features.numImageFeatures(),
				features.numSingleFeatures(),
				() -> createFeatureNames()
			);
			checker.checkInverseEqual(rv, rvInverse, params);
		}
		
		return rv;
	}

	@Override
	public FeatureNameList createFeatureNames() {
		FeatureNameList out = new FeatureNameList();
		
		out.addCustomNamesWithPrefix( "image.", features.getImage() );
		
		if (includeFirst) {
			out.addCustomNamesWithPrefix( "first.", features.getSingle() );
		}
		
		if (includeSecond) {
			out.addCustomNamesWithPrefix( "second.", features.getSingle() );
		}
		
		out.addCustomNamesWithPrefix( "pair.", features.getPair() );
		
		out.addCustomNamesWithPrefix( "merged.", features.getSingle() );
		return out;
	}
	
	@Override
	public int size() {
		
		// Number of times we use the listSingle
		int numSingle = 1 + integerFromBoolean(includeFirst) + integerFromBoolean(includeSecond);
				
		return features.numImageFeatures()
			+ features.numPairFeatures()
			+ (numSingle * features.numSingleFeatures());
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

	private ResultsVector calcForInput(FeatureInputPairObjs input, ErrorReporter errorReporter) throws FeatureCalcException {
		
		ResultsVectorBuilder helper = new ResultsVectorBuilder(size(), suppressErrors, errorReporter);
		
		// First we calculate the Image features (we rely on the NRG stack being added by the calculator)
		helper.calcAndInsert(new FeatureInputStack(), sessionImage );
		
		// First features
		if (includeFirst) {
			helper.calcAndInsert( input, FeatureInputPairObjs::getLeft, sessionFirstSecond );
		}
		
		// Second features
		if (includeSecond) {
			helper.calcAndInsert( input, FeatureInputPairObjs::getRight, sessionFirstSecond );
		}
		
		// Pair features
		helper.calcAndInsert(input, sessionPair );
		
		// Merged. Because we know we have FeatureObjMaskPairMergedParams, we don't need to change params
		helper.calcAndInsert(input, FeatureInputPairObjs::getMerged, sessionMerged );
		
		assert(helper.getResultsVector().hasNoNulls());
		return helper.getResultsVector();
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
}