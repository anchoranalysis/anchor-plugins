package org.anchoranalysis.plugin.mpp.sgmn.cfg.bean.cfg;

import org.anchoranalysis.anchor.mpp.bean.cfg.CfgGen;
import org.anchoranalysis.anchor.mpp.bean.init.GeneralInitParams;
import org.anchoranalysis.anchor.mpp.bean.init.MPPInitParams;
import org.anchoranalysis.anchor.mpp.feature.bean.nrgscheme.NRGScheme;
import org.anchoranalysis.anchor.mpp.feature.bean.nrgscheme.NRGSchemeCreator;
import org.anchoranalysis.anchor.mpp.feature.nrg.cfg.CfgNRGPixelized;
import org.anchoranalysis.anchor.mpp.feature.nrg.scheme.NRGSchemeWithSharedFeatures;

/*-
 * #%L
 * anchor-plugin-mpp-experiment
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

import org.anchoranalysis.bean.define.Define;
import org.anchoranalysis.core.cache.CacheMonitor;
import org.anchoranalysis.core.error.CreateException;
import org.anchoranalysis.core.error.InitException;
import org.anchoranalysis.core.error.OperationFailedException;
import org.anchoranalysis.core.log.LogErrorReporter;
import org.anchoranalysis.core.name.provider.INamedProvider;
import org.anchoranalysis.core.name.provider.NamedProviderGetException;
import org.anchoranalysis.core.name.store.SharedObjects;
import org.anchoranalysis.core.params.KeyValueParams;
import org.anchoranalysis.feature.nrg.NRGStack;
import org.anchoranalysis.feature.nrg.NRGStackWithParams;
import org.anchoranalysis.feature.shared.SharedFeaturesInitParams;
import org.anchoranalysis.image.experiment.identifiers.ImgStackIdentifiers;
import org.anchoranalysis.image.init.CreateCombinedStack;
import org.anchoranalysis.image.init.ImageInitParams;
import org.anchoranalysis.image.io.stack.StackCollectionOutputter;
import org.anchoranalysis.image.objmask.ObjMaskCollection;
import org.anchoranalysis.image.stack.NamedImgStackCollection;
import org.anchoranalysis.image.stack.Stack;
import org.anchoranalysis.io.output.bound.BoundOutputManagerRouteErrors;
import org.anchoranalysis.mpp.io.output.NRGStackWriter;
import org.anchoranalysis.mpp.io.output.StackOutputKeys;
import org.anchoranalysis.mpp.sgmn.bean.kernel.proposer.KernelProposer;

class SgmnMPPHelper {

	public static void writeStacks(
		ImageInitParams so,
		NRGStackWithParams nrgStack,
		LogErrorReporter logger,
		BoundOutputManagerRouteErrors outputManager
	) {
		
		StackCollectionOutputter.output(
			StackCollectionOutputter.subset(
				CreateCombinedStack.apply(so),
				outputManager.outputAllowedSecondLevel(StackOutputKeys.STACK)
			),
			outputManager.getDelegate(),
			"stackCollection",
			"stack_",
			logger.getErrorReporter(),
			false
		);

		NRGStackWriter.writeNRGStack( nrgStack, outputManager, logger );		
	}
	
	public static MPPInitParams createParamsMPP(
		Define namedDefinitions,
		NamedImgStackCollection stackCollection,
		INamedProvider<ObjMaskCollection> objMaskCollection,
		KeyValueParams params,
		SharedObjects so,
		GeneralInitParams paramsGeneral
	) throws CreateException {
		
		MPPInitParams soMPP = MPPInitParams.create(
			so,
			namedDefinitions,
			paramsGeneral
		);
		
		try {
			soMPP.getImage().copyStackCollectionFrom( stackCollection );
			soMPP.getImage().copyObjMaskCollectionFrom( objMaskCollection );
			
			// TODO replace with a constant
			if (params!=null) {
				soMPP.getImage().addToKeyValueParamsCollection("input_params", params);
			}
		} catch (OperationFailedException e) {
			throw new CreateException(e);
		}
		
		return soMPP;
	}
	
	public static NRGSchemeWithSharedFeatures initNRG(
		NRGSchemeCreator nrgSchemeCreator,
		int nrgSchemeIndCacheSize,
		SharedFeaturesInitParams featureInit,
		LogErrorReporter logger
	) throws InitException {
		
		nrgSchemeCreator.initRecursive(featureInit, logger);
		
		try {
			NRGScheme nrgScheme = nrgSchemeCreator.create();
			
			NRGSchemeWithSharedFeatures nrgSchemeShared = new NRGSchemeWithSharedFeatures(
				nrgScheme,
				featureInit.getSharedFeatureSet(),
				nrgSchemeIndCacheSize,
				new CacheMonitor(),
				logger
			);
			
			return nrgSchemeShared;
		} catch (CreateException e) {
			throw new InitException(e);
		}
	}
		
	public static NRGStackWithParams createNRGStack( INamedProvider<Stack> stackCollection, KeyValueParams params ) throws CreateException {
		try {
			NRGStack nrgStack = new NRGStack( stackCollection.getException(ImgStackIdentifiers.NRG_STACK) );
			return new NRGStackWithParams( nrgStack, params );
		} catch (NamedProviderGetException e) {
			throw new CreateException(e);
		}
	}
	
	public static void initKernelProposers( KernelProposer<CfgNRGPixelized> kernelProposer, CfgGen cfgGen, MPPInitParams soMPP, LogErrorReporter logger ) throws InitException {
		// The initial initiation to establish the kernelProposer
		kernelProposer.init();
		kernelProposer.initWithProposerSharedObjects( soMPP, logger );
		
		// Check that the kernelProposer is compatible with our marks
		kernelProposer.checkCompatibleWith( cfgGen.getTemplateMark().create() );
	}
}
