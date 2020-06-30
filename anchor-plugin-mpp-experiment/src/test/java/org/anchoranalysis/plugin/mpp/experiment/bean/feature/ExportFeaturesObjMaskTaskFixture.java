package org.anchoranalysis.plugin.mpp.experiment.bean.feature;

/*-
 * #%L
 * anchor-plugin-mpp-experiment
 * %%
 * Copyright (C) 2010 - 2020 Owen Feehan
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

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import org.anchoranalysis.bean.NamedBean;
import org.anchoranalysis.bean.error.BeanMisconfiguredException;
import org.anchoranalysis.bean.xml.RegisterBeanFactories;
import org.anchoranalysis.core.error.CreateException;
import org.anchoranalysis.feature.input.FeatureInput;
import org.anchoranalysis.feature.nrg.NRGStack;
import org.anchoranalysis.image.bean.provider.ObjectCollectionProvider;
import org.anchoranalysis.io.bean.filepath.generator.FilePathGeneratorConstant;
import org.anchoranalysis.plugin.image.feature.bean.object.table.FeatureTableObjects;
import org.anchoranalysis.plugin.image.feature.bean.object.table.MergedPairs;
import org.anchoranalysis.plugin.image.feature.bean.object.table.Simple;
import org.anchoranalysis.test.TestLoader;
import org.anchoranalysis.test.image.NRGStackFixture;

import ch.ethz.biol.cell.imageprocessing.objmask.provider.ObjMaskProviderReference;

class ExportFeaturesObjMaskTaskFixture {
	
	private NRGStack nrgStack = createNRGStack(true);
	private FeatureTableObjects<?> flexiFeatureTable = new Simple();
	
	private final ExportFeaturesObjMaskFeatureLoader featureLoader;
	
	/**
	 * Constructor
	 * 
	 * <p>By default, use a big-sized NRG-stack that functions with our feature-lists</p>
	 * <p>By default, load the features from PATH_FEATURES</p>
	 * <p>By default, use Simple feature-mode. It can be changed to Merged-Pairs.</p>
	 * 
	 * @param loader
	 * @throws CreateException 
	 */
	public ExportFeaturesObjMaskTaskFixture(TestLoader loader) throws CreateException {
		this.nrgStack = createNRGStack(true);
		this.featureLoader = new ExportFeaturesObjMaskFeatureLoader(loader);
	}
	
	/** 
	 * Change to using a small nrg-stack that causes some features to throw errors
	 * */
	public void useSmallNRGInstead() {
		this.nrgStack = createNRGStack(false);
	}
	
	/** 
	 * Change to use Merged-Pairs mode rather than Simple mode
	 *
	 * @param includeFeaturesInPair iff TRUE "pair" features are populated in merged-pair mode
	 * @throws CreateException 
	 **/
	public void changeToMergedPairs(boolean includeFeaturesInPair, boolean includeImageFeatures) {
		flexiFeatureTable = createMergedPairs(includeFeaturesInPair, includeImageFeatures);
	}
	
	public ExportFeaturesObjMaskFeatureLoader featureLoader() {
		return featureLoader;
	}
	
	public NRGStack getNrgStack() {
		return nrgStack;
	}
	
	@SuppressWarnings("unchecked")
	public <T extends FeatureInput> ExportFeaturesObjMaskTask<T> createTask() throws CreateException {
				
		ExportFeaturesObjMaskTask<T> task = new ExportFeaturesObjMaskTask<>();
		task.setFeatures(featureLoader.single());
		task.setFeaturesAggregate(featureLoader.aggregated());
		task.setDefine(
			DefineFixture.create(
				nrgStack,
				Optional.of(featureLoader.shared())
			)		
		);
		task.setGroup( new FilePathGeneratorConstant("arbitraryGroup") );
		task.setTable( (FeatureTableObjects<T>) flexiFeatureTable);
		task.setListObjMaskProvider(
			createObjProviders(MultiInputFixture.OBJS_NAME)
		);
		
		try {
			task.checkMisconfigured( RegisterBeanFactories.getDefaultInstances() );
		} catch (BeanMisconfiguredException e) {
			throw new CreateException(e);
		}
		
		return task;
	}
		
	private static List<NamedBean<ObjectCollectionProvider>> createObjProviders(String objsName) {
		return Arrays.asList(
			new NamedBean<>(objsName, new ObjMaskProviderReference(objsName))	
		);
	}

	private MergedPairs createMergedPairs(boolean includeFeaturesInPair, boolean includeImageFeatures) {
		MergedPairs mergedPairs = new MergedPairs();
		if (includeFeaturesInPair) {
			mergedPairs.setFeaturesPair(featureLoader.pair());
		}
		if (includeImageFeatures) {
			mergedPairs.setFeaturesImage(featureLoader.image());
		}
		return mergedPairs;
	}
	
	private NRGStack createNRGStack( boolean bigSizeNrg ) {
		return NRGStackFixture.create(bigSizeNrg, false).getNrgStack();
	}
}
