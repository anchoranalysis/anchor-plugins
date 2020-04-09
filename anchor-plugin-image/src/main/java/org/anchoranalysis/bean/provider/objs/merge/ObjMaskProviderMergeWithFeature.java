package org.anchoranalysis.bean.provider.objs.merge;

/*
 * #%L
 * anchor-plugin-image
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
import org.anchoranalysis.bean.annotation.Optional;
import org.anchoranalysis.bean.provider.objs.merge.MergeGraph.AssignPriority;
import org.anchoranalysis.bean.provider.objs.merge.condition.AndCondition;
import org.anchoranalysis.bean.provider.objs.merge.condition.NeighbourhoodCond;
import org.anchoranalysis.bean.provider.objs.merge.condition.UpdatableBeforeCondition;
import org.anchoranalysis.bean.provider.objs.merge.condition.WrapAsUpdatable;
import org.anchoranalysis.core.error.CreateException;
import org.anchoranalysis.core.error.OperationFailedException;
import org.anchoranalysis.core.graph.GraphWithEdgeTypes.EdgeTypeWithVertices;
import org.anchoranalysis.feature.session.SequentialSession;
import org.anchoranalysis.feature.session.FeatureCalculatorVector;
import org.anchoranalysis.feature.session.FeatureCalculatorVectorChangeParams;
import org.anchoranalysis.image.bean.provider.ObjMaskProvider;
import org.anchoranalysis.image.extent.ImageRes;
import org.anchoranalysis.image.feature.bean.evaluator.FeatureEvaluatorNrgStack;
import org.anchoranalysis.image.feature.evaluator.EvaluateSingleObjMask;
import org.anchoranalysis.image.feature.objmask.FeatureObjMaskParams;
import org.anchoranalysis.image.feature.session.FeatureSessionCreateParamsSinglePairMerged;
import org.anchoranalysis.image.objmask.ObjMaskCollection;

import objmaskwithfeature.ObjMaskWithFeature;
import objmaskwithfeature.ObjMaskWithFeatureAndPriority;

public abstract class ObjMaskProviderMergeWithFeature extends ObjMaskProviderMergeOptionalDistance {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	// START BEAN PROPERTIES
	@BeanField
	private FeatureEvaluatorNrgStack featureEvaluator;
	
	@BeanField
	private boolean requireBBoxNeighbours = true;	// Requires the bounding boxes to intersect, or touch for any potential merge
	
	@BeanField
	private boolean requireTouching = true;	// Requires the object-masks to touch. More expensive to calculate than just the bounding box
		 
	/**
	 * Saves all objects that are inputs to the merge, outputs from the merge, or intermediate merges along the way
	 */
	@BeanField @Optional
	private ObjMaskProvider objsSave;
	// END BEAN PROPERTIES
	
	
	protected abstract EvaluateSingleObjMask featureEvaluator(FeatureCalculatorVector<FeatureObjMaskParams> featureSession);
	
	protected abstract AssignPriority assignPriority(
	) throws CreateException;

	/**
	 * Is the single-feature (the unary feature calculated on each feature) used?
	 * 
	 */
	protected abstract boolean isSingleFeatureUsed();
	
	@Override
	public ObjMaskCollection create() throws CreateException {
		
		ObjMaskCollection objsSource = getObjs().create();
		
		ObjMaskCollection saveObjs = objsSave !=null ? objsSave.create() : null;
		if (saveObjs!=null) {
			saveObjs.addAll(objsSource);
		}

		getLogger().getLogReporter().logFormatted("There are %d input objects", objsSource.size() );
		
		try {
			ObjMaskCollection merged = mergeMultiplex(
				objsSource,
				a -> mergeConnectedComponents( a, saveObjs )
			);
					
			getLogger().getLogReporter().logFormatted("There are %d final objects", merged.size() );
			
			return merged;
			
		} catch (OperationFailedException e) {
			throw new CreateException(e);
		}
	}

	
	/**
	 * Tries to merge objs
	 * 
	 * @param objs objects to be merged
	 * @param saveObjs if non-NULL, all merged objects are added to saveObjs
	 * @return
	 * @throws OperationFailedException
	 */
	private ObjMaskCollection mergeConnectedComponents( ObjMaskCollection objs, ObjMaskCollection saveObjs ) throws OperationFailedException {
		
		ImageRes res = calcRes();
		
		MergeGraph graph;
		try {
			graph = createGraph(
				objs,
				res,
				featureEvaluator.createAndStartSession()
			);
		} catch (CreateException e) {
			throw new OperationFailedException(e);
		}
		
		if (getLogger()!=null) {
			getLogger().getLogReporter().log("\nBefore");
			getLogger().getLogReporter().log(graph.graphDescription());
		}
		
		while( tryMerge(graph, saveObjs ) ) {
			// NOTHING TO DO, we just keep merging until we cannot merge any moore
		}
		
		graph.end();
		
		if (getLogger()!=null) {
			getLogger().getLogReporter().log("After");
			getLogger().getLogReporter().log(graph.graphDescription());
			
		}
		
		return graph.createObjMaskCollectionFromVertices();
	}
	

	/**
	 * Search for suitable merges in graph, and merges them
	 * 
	 * @param graph the graph containing objects that can maybe be merged
	 * @param saveObjs if non-NULL, all merged objects are added to saveObjs
	 * @return
	 * @throws OperationFailedException
	 */
	private boolean tryMerge( MergeGraph graph, ObjMaskCollection saveObjs ) throws OperationFailedException {
		
		// Find the edge with the best improvement
		EdgeTypeWithVertices<ObjMaskWithFeature,ObjMaskWithFeatureAndPriority> edgeToMerge = graph.findMaxPriority();
		
		if (edgeToMerge==null) {
			return false;
		}
		
		if (saveObjs!=null) {
			// When we decide to merge, we save the merged object
			saveObjs.add( edgeToMerge.getEdge().getOmWithFeature().getObjMask() );
		}
		
		graph.merge(edgeToMerge,getLogger());

		return true;
	}
	
	private MergeGraph createGraph(
		ObjMaskCollection objs,
		ImageRes res,
		FeatureCalculatorVector<FeatureObjMaskParams> sessionSingle
	) throws CreateException {
			
		try {
			MergeGraph graph = new MergeGraph(
				featureEvaluator( sessionSingle ),
				beforeConditions(),
				res,
				assignPriority(),
				isSingleFeatureUsed()
			);
						
			graph.addObjsToGraph(objs, getLogger());
				
			return graph;
			
		} catch (OperationFailedException e) {
			throw new CreateException(e);
		}
	}
	
	private UpdatableBeforeCondition beforeConditions() {
		return new AndCondition(
			new WrapAsUpdatable(
				maybeDistanceCondition()	
			),
			new NeighbourhoodCond(requireBBoxNeighbours, requireTouching)
		);
	}
	
	

	public FeatureEvaluatorNrgStack getFeatureEvaluator() {
		return featureEvaluator;
	}

	public void setFeatureEvaluator(FeatureEvaluatorNrgStack featureEvaluator) {
		this.featureEvaluator = featureEvaluator;
	}

	public boolean isRequireBBoxNeighbours() {
		return requireBBoxNeighbours;
	}

	public void setRequireBBoxNeighbours(boolean requireBBoxNeighbours) {
		this.requireBBoxNeighbours = requireBBoxNeighbours;
	}

	public ObjMaskProvider getObjsSave() {
		return objsSave;
	}

	public void setObjsSave(ObjMaskProvider objsSave) {
		this.objsSave = objsSave;
	}

}
