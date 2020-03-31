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
import org.anchoranalysis.feature.session.ISequentialSessionSingleParams;
import org.anchoranalysis.feature.session.SequentialSession;
import org.anchoranalysis.feature.session.SequentialSessionRepeatFirst;
import org.anchoranalysis.feature.session.SequentialSessionVerticallyCached;
import org.anchoranalysis.feature.shared.SharedFeatureSet;
import org.anchoranalysis.feature.shared.SharedFeaturesInitParams;
import org.anchoranalysis.image.feature.init.FeatureInitParamsImageInit;
import org.anchoranalysis.image.feature.objmask.FeatureObjMaskParams;
import org.anchoranalysis.image.feature.objmask.pair.merged.FeatureObjMaskPairMergedParams;
import org.anchoranalysis.image.init.ImageInitParams;
import org.anchoranalysis.image.objmask.ObjMask;
import org.anchoranalysis.plugin.mpp.experiment.bean.feature.flexi.Simple;

public class FeatureSessionMergedPairs extends FeatureSessionFlexiFeatureTable {

	private boolean includeFirst;
	private boolean includeSecond;
	
	// Our sessions
	private ISequentialSessionSingleParams sessionImage;
	
	// We avoid using seperate sessions for First and Second, as we want them
	//  to share the same Vertical-Cache for object calculation.
	private ISequentialSessionSingleParams sessionFirstSecond;
	private ISequentialSessionSingleParams sessionMerged;
	private ISequentialSessionSingleParams sessionPair;

	// The lists we need
	private FeatureList listImage;
	private FeatureList listSingle;
	private FeatureList listPair;
	private boolean checkInverse = false;
	private boolean suppressErrors = false;
	
	// Prefixes that are ignored
	private Collection<String> ignoreFeaturePrefixes;
	
	public FeatureSessionMergedPairs(
		boolean includeFirst,
		boolean includeSecond,
		FeatureList listImage,
		FeatureList listSingle,
		FeatureList listPair,
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
	public void start(ImageInitParams soImage, SharedFeaturesInitParams soFeature, NRGStackWithParams nrgStack, LogErrorReporter logErrorReporter) throws InitException {
		
		// We create our SharedFeatures including anything from the NamedDefinitions, and all our additional features
		SharedFeatureSet sharedFeatures = soFeature.getSharedFeatureSet();
		// sharedFeatures = createSharedFeatures(soFeature,listSingle);
		//listImage.copyToCustomName(sharedFeatures.getSet(),false);
		//listSingle.copyToCustomName(sharedFeatures.getSet(),false);
		//listPair.copyToCustomName(sharedFeatures.getSet(),false);
		
		// Init all the features
		FeatureInitParams paramsInitImage = Simple.createInitParams(soImage,nrgStack.getNrgStack(), nrgStack.getParams());
		FeatureInitParams paramsInitFirstSecond = Simple.createInitParams(soImage, nrgStack.getNrgStack(), nrgStack.getParams() );
		FeatureInitParams paramsInitMerged = Simple.createInitParams(soImage,nrgStack.getNrgStack(), nrgStack.getParams() );
		
		// We create more caches for the includeFirst and includeSecond Features and merged features.
		
		FeatureInitParamsImageInit paramsInitPair = new FeatureInitParamsImageInit( soImage );
		paramsInitPair.setKeyValueParams( nrgStack.getParams() );
		paramsInitPair.setNrgStack(nrgStack.getNrgStack());
		
		logErrorReporter.getLogReporter().log("Setting up: Image Features");

		sessionImage = new SequentialSessionRepeatFirst( listImage, ignoreFeaturePrefixes );
		sessionImage.start(paramsInitImage, sharedFeatures.duplicate(), logErrorReporter);

		
		if (includeFirst || includeSecond) {
			logErrorReporter.getLogReporter().log("Setting up: First/Second Features");
			sessionFirstSecond = new SequentialSessionVerticallyCached( listSingle, suppressErrors, ignoreFeaturePrefixes );
			sessionFirstSecond.start(paramsInitFirstSecond, sharedFeatures.duplicate(), logErrorReporter);
		}
			
		
		logErrorReporter.getLogReporter().log("Setting up: Pair Features");
		sessionPair = new SequentialSession( listPair, ignoreFeaturePrefixes );
		
		// TODO to make this more efficient, it would be better if we could re-use the cached-operations
		//  from the calculation of the First and Second individual features, as they appear again
		//  as additionalCaches of sessionPair
		sessionPair.start(paramsInitPair, sharedFeatures.duplicate(), logErrorReporter);
			
		
		// We keep a seperate session for merges, as there is no need to do caching. But we copy features
		//  to make sure there's no collision of the caches
		//System.out.println("SessionMerged");
		logErrorReporter.getLogReporter().log("Setting up: Merged Features");
		sessionMerged = new SequentialSession( listSingle.duplicateBean(), ignoreFeaturePrefixes );
		sessionMerged.start(paramsInitMerged, sharedFeatures.duplicate(), logErrorReporter);

		
//		System.out.printf("Session Image = %d\n", sessionImage );
//		System.out.printf("Session First = %d\n", sessionFirst );
//		System.out.printf("Session Second = %d\n", sessionSecond);
//		System.out.printf("Session Image = %d\n", sessionImage);
//		System.out.printf("Session Merged = %d\n", sessionMerged);
	}

	@Override
	public FeatureSessionFlexiFeatureTable duplicateForNewThread() {
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

	@FunctionalInterface
	private static interface ExtractObj {
		ObjMask extractObj(FeatureObjMaskPairMergedParams params);
	}
	
	private static FeatureObjMaskParams createNewSpecificParams( FeatureObjMaskPairMergedParams params, FeatureSessionMergedPairs.ExtractObj extractObj ) {
		FeatureObjMaskParams paramsOut = new FeatureObjMaskParams( extractObj.extractObj(params) );
		paramsOut.setNrgStack( params.getNrgStack() );
		return paramsOut;
	}
	
	// Create new more specific params
	private static int calcAndInsert( FeatureObjMaskPairMergedParams params, FeatureSessionMergedPairs.ExtractObj extractObj, ISequentialSessionSingleParams session, int start, ResultsVector out, ErrorReporter errorReporter, boolean suppressErrors ) throws FeatureCalcException {
		FeatureObjMaskParams paramsSpecific = createNewSpecificParams(params,extractObj);
		return calcAndInsert(paramsSpecific, session, start, out, errorReporter, suppressErrors);
	}
	
	/**
	 * Calculates the parameters belong to a particular session and inserts into a ResultsVector
	 * 
	 * @param params
	 * @param session
	 * @param start
	 * @param out
	 * @param errorReporter
	 * @return length(resultsVector)
	 * @throws FeatureCalcException
	 */
	private static int calcAndInsert( FeatureCalcParams params, ISequentialSessionSingleParams session, int start, ResultsVector out, ErrorReporter errorReporter, boolean suppressErrors ) throws FeatureCalcException {
		ResultsVector rvImage =  suppressErrors ? session.calcSuppressErrors( params, errorReporter ) : session.calc(params) ;
		out.set(start, rvImage);
		return rvImage.length();
	}
	


	private ResultsVector calcForParams(FeatureCalcParams params, ErrorReporter errorReporter) throws FeatureCalcException {
		
		assert(params instanceof FeatureObjMaskPairMergedParams);
		FeatureObjMaskPairMergedParams paramsCast = (FeatureObjMaskPairMergedParams) params;
		
		ResultsVector out = new ResultsVector( size() );
		
		int cnt = 0;
		
		// First we calculate the Image features (we can pick any object)
		cnt += calcAndInsert(paramsCast, (a)->a.getObjMask1(), sessionImage, cnt, out, errorReporter, suppressErrors );
		
		// First features
		if (includeFirst) {
			cnt += calcAndInsert( paramsCast, (a)->a.getObjMask1(), sessionFirstSecond, cnt, out, errorReporter, suppressErrors );
		}
		
		// Second features
		if (includeSecond) {
			cnt += calcAndInsert( paramsCast, (a)->a.getObjMask2(), sessionFirstSecond, cnt, out, errorReporter, suppressErrors );
		}
		
		// Pair features
		cnt += calcAndInsert(paramsCast, sessionPair, cnt, out, errorReporter, suppressErrors );
		
		// Merged. Because we know we have FeatureObjMaskPairMergedParams, we don't need to change params
		cnt += calcAndInsert(paramsCast, (a)->a.getObjMaskMerged(), sessionMerged, cnt, out, errorReporter, suppressErrors );
		
		assert(out.hasNoNulls());
		return out;
	}
	
	
	/**
	 * Create a ResultsVector where the values for first and second are switched
	 * @param in
	 * @return
	 */
	private ResultsVector switchFirstAndSecond( ResultsVector in ) {
		ResultsVector out = new ResultsVector( in.length() );
		
		assert( includeFirst );
		assert( includeSecond );
		
		int cnt = 0;
		
		// Image features
		out.copyFrom(0, listImage.size(), in, 0);
		
		cnt += listImage.size();
		
		// First features
		out.copyFrom(cnt, listSingle.size(), in, cnt + listSingle.size() );
		
		// Second features
		out.copyFrom(cnt + listSingle.size(), listSingle.size(), in, cnt );
		
		cnt += (listSingle.size()*2);
		
		// Copy the rest unchanged
		out.copyFrom(cnt, in.length()-cnt, in, cnt);
		
		return out;
	}
	
	@Override
	public ResultsVector calcMaybeSuppressErrors(FeatureCalcParams params, ErrorReporter errorReporter)
			throws FeatureCalcException {
		
		ResultsVector rv = calcForParams(params,errorReporter);
		
		if (checkInverse) {
			//DEBUG
			//System.out.printf("Calculating inverse for %s\n", params ); 
			
			FeatureCalcParams paramsInv = params.createInverse();
			assert( paramsInv!=null );
			ResultsVector rvInverse = calcForParams(paramsInv,errorReporter);
			
			assert(rvInverse!=null);
			
			StringBuilder sb = new StringBuilder();
			if (!isInverseEqual(rv,rvInverse,sb)) {
				throw new FeatureCalcException(
					String.format("Feature values are not equal to the inverse for %s:%n%s", params, sb.toString() )
				);
			}
		}
		
		return rv;
	}
	

	
	/**
	 * Generates a multi-line string describing which values are different.
	 * 
	 * Values are compared. Errors are treated as nulls.
	 * 
	 * @param other
	 * @return
	 * @throws FeatureCalcException 
	 */
	private boolean isInverseEqual( ResultsVector rv1, ResultsVector rv2, StringBuilder sb) throws FeatureCalcException {
		
		if (includeFirst!=includeSecond) {
			throw new FeatureCalcException("Cannot compare with inverse, as includeFirst!=includeSecond");
		}
		
		if (rv1.length()!=rv2.length()) {
			sb.append( String.format("lengths are different: %d vs %d", rv1.length(), rv2.length()) );
			return false;
		}
		
		if (includeFirst) {
			rv2 = switchFirstAndSecond(rv2);
		}
		
		// we initialise the f
		FeatureNameList featureNames = null; 
		
		boolean allEqual = true;
		
		for( int i=0; i<rv1.length(); i++) {
			
			Double val1 = rv1.getDoubleOrNull(i);
			Double val2 = rv2.getDoubleOrNull(i);
			
			if (!areDoubleOrNullEquals(val1,val2)) {
				
				// Lazy creation, as we only need if an error occurs
				if (allEqual==true) {
					featureNames = createFeatureNames();
					allEqual = false;
				}
				
				String featName = featureNames.get(i);
				
				String diffFeat = String.format("Feature %s is different:\t%f\tvs\t%f%n", featName, val1, val2);
				sb.append(diffFeat);
			}
		}
		
		return allEqual;
	}
	
	private static boolean areDoubleOrNullEquals( Double val1, Double val2 ) {
		if (val1==null) {
			return (val2==null);
		}
		if (val2==null) {
			return (val1==null);
		}
		return val1.equals(val2);
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