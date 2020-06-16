package org.anchoranalysis.plugin.image.bean.obj.merge;



import java.util.Optional;

import org.anchoranalysis.bean.OptionalFactory;

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
import org.anchoranalysis.bean.annotation.OptionalBean;
import org.anchoranalysis.core.error.CreateException;
import org.anchoranalysis.core.error.OperationFailedException;
import org.anchoranalysis.core.graph.EdgeTypeWithVertices;
import org.anchoranalysis.core.log.LogReporter;
import org.anchoranalysis.image.bean.provider.ObjMaskProvider;
import org.anchoranalysis.image.extent.ImageRes;
import org.anchoranalysis.image.feature.evaluator.PayloadCalculator;
import org.anchoranalysis.image.objectmask.ObjectMaskCollection;
import org.anchoranalysis.plugin.image.obj.merge.MergeGraph;
import org.anchoranalysis.plugin.image.obj.merge.ObjVertex;
import org.anchoranalysis.plugin.image.obj.merge.condition.AndCondition;
import org.anchoranalysis.plugin.image.obj.merge.condition.NeighbourhoodCond;
import org.anchoranalysis.plugin.image.obj.merge.condition.UpdatableBeforeCondition;
import org.anchoranalysis.plugin.image.obj.merge.condition.WrapAsUpdatable;
import org.anchoranalysis.plugin.image.obj.merge.priority.AssignPriority;
import org.anchoranalysis.plugin.image.obj.merge.priority.PrioritisedVertex;

public abstract class ObjMaskProviderMergeWithFeature extends ObjMaskProviderMergeOptionalDistance {

	// START BEAN PROPERTIES
	/** Requires for any potential merge that the bounding-boxes of the two objects must intersect or touch */
	@BeanField
	private boolean requireBBoxNeighbours = true;
	
	
	/** Requires the object-masks to touch. More expensive to calculate than the requireBBoxNeighbours condition. */
	@BeanField
	private boolean requireTouching = true;
		 
	/**
	 * Saves all objects that are inputs to the merge, outputs from the merge, or intermediate merges along the way
	 */
	@BeanField @OptionalBean
	private ObjMaskProvider objsSave;
	// END BEAN PROPERTIES
		
	@Override
	public ObjectMaskCollection createFromObjs(ObjectMaskCollection objsSource) throws CreateException {
		
		Optional<ObjectMaskCollection> saveObjs = OptionalFactory.create(objsSave);
		saveObjs.ifPresent( so
			->so.addAll(objsSource)
		);


		getLogger().getLogReporter().logFormatted("There are %d input objects", objsSource.size() );
		
		try {
			ObjectMaskCollection merged = mergeMultiplex(
				objsSource,
				a -> mergeConnectedComponents( a, saveObjs )
			);
					
			getLogger().getLogReporter().logFormatted("There are %d final objects", merged.size() );
			
			return merged;
			
		} catch (OperationFailedException e) {
			throw new CreateException(e);
		}
	}

	/** Determines the payload for any given or potential vertex */
	protected abstract PayloadCalculator createPayloadCalculator() throws OperationFailedException;
	
	/** Determines the priority (and selection criteria) used to allow merges between neighbours */
	protected abstract AssignPriority createPrioritizer() throws OperationFailedException;

	/**
	 * Is the payload considered in making decisions? (iff FALSE, payload of nodes is irrelvant)
	 * 
	 */
	protected abstract boolean isPlayloadUsed();
	
	
	/**
	 * Tries to merge objs
	 * 
	 * @param objs objects to be merged
	 * @param saveObjs if non-NULL, all merged objects are added to saveObjs
	 * @return
	 * @throws OperationFailedException
	 */
	private ObjectMaskCollection mergeConnectedComponents( ObjectMaskCollection objs, Optional<ObjectMaskCollection> saveObjs ) throws OperationFailedException {
		
		LogReporter logger = getLogger().getLogReporter();
		
		MergeGraph graph;
		try {
			graph = createGraph(
				objs,
				calcResOptional()
			);
		} catch (CreateException e) {
			throw new OperationFailedException(e);
		}

		logger.log("\nBefore");
		graph.logGraphDescription();
		
		while( tryMerge(graph, saveObjs ) ) {
			// NOTHING TO DO, we just keep merging until we cannot merge any moore
		}
		
		logger.log("After");
		graph.logGraphDescription();
		
		return graph.verticesAsObjects();
	}
	

	/**
	 * Search for suitable merges in graph, and merges them
	 * 
	 * @param graph the graph containing objects that can maybe be merged
	 * @param saveObjs if defined, all merged objects are added to saveObjs
	 * @return
	 * @throws OperationFailedException
	 */
	private boolean tryMerge( MergeGraph graph, Optional<ObjectMaskCollection> saveObjs ) throws OperationFailedException {
		
		// Find the edge with the best improvement
		EdgeTypeWithVertices<ObjVertex,PrioritisedVertex> edgeToMerge = graph.findMaxPriority();
		
		if (edgeToMerge==null) {
			return false;
		}

		// When we decide to merge, we save the merged object
		saveObjs.ifPresent( so->
			so.add(
				edgeToMerge.getEdge().getOmWithFeature().getObjMask() 
			)
		);

		
		graph.merge(edgeToMerge,getLogger());

		return true;
	}
	
	private MergeGraph createGraph(
		ObjectMaskCollection objs,
		Optional<ImageRes> res
	) throws CreateException {
			
		try {
			MergeGraph graph = new MergeGraph(
				createPayloadCalculator(),
				beforeConditions(),
				res,
				createPrioritizer(),
				getLogger(),
				isPlayloadUsed()
			);
						
			graph.addObjsToGraph(objs);
				
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
