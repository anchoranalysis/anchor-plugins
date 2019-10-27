package org.anchoranalysis.bean.provider.objs.merge;

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


import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.anchoranalysis.bean.provider.objs.merge.NeighbourGraph.EdgeCreator;
import org.anchoranalysis.bean.provider.objs.merge.condition.UpdatableBeforeCondition;
import org.anchoranalysis.core.arithmetic.FloatUtilities;
import org.anchoranalysis.core.error.OperationFailedException;
import org.anchoranalysis.core.geometry.Comparator3i;
import org.anchoranalysis.core.geometry.Point3i;
import org.anchoranalysis.core.graph.GraphWithEdgeTypes.EdgeTypeWithVertices;
import org.anchoranalysis.core.log.LogErrorReporter;
import org.anchoranalysis.feature.calc.FeatureCalcException;
import org.anchoranalysis.image.extent.ImageRes;
import org.anchoranalysis.image.feature.evaluator.EvaluateSingleObjMask;
import org.anchoranalysis.image.objmask.ObjMask;
import org.anchoranalysis.image.objmask.ObjMaskCollection;
import org.anchoranalysis.image.objmask.ops.ObjMaskMerger;

import objmaskwithfeature.ObjMaskWithFeature;
import objmaskwithfeature.ObjMaskWithFeatureAndPriority;


/**
 * A graph that stores objects with a feature-value associated with them
 * 
 * It allows merges between objects if they increase this feature-value
 * 
 * These merges occur in order of the maximum improvement offered
 * 
 * @author Owen Feehan
 *
 */
public class MergeGraph {
	
	// START: SET FROM CONSTRUCTOR
	private EvaluateSingleObjMask featureEvaluatorSingleObj;
	private AssignPriority assignPriority;
	
	// END: SET FROM CONSTRUCTOR
	
	private NeighbourGraph graph;
	private DescribeGraph describeGraph;
	
	private class MergedEdgeCreator implements EdgeCreator {

		@Override
		public ObjMaskWithFeatureAndPriority createPriority(ObjMaskWithFeature omSrcWithFeature,
				ObjMaskWithFeature omDestWithFeature, LogErrorReporter logger) throws OperationFailedException {
						
			// Do merge
			ObjMask omMerge = ObjMaskMerger.merge(
				omSrcWithFeature.getObjMask(),
				omDestWithFeature.getObjMask()
			);
			
			// Remove all existing edges
			
			assert( (omMerge.numPixels()!=omSrcWithFeature.getObjMask().numPixels()) && (omMerge.numPixels()!=omDestWithFeature.getObjMask().numPixels()) );
		
			ObjMaskWithFeatureAndPriority withPriority = assignPriority.assignPriority(
				omSrcWithFeature,
				omDestWithFeature,
				omMerge,
				featureEvaluatorSingleObj,
				logger
			);
			
			logger.getLogReporter().log(
				describeGraph.describeEdge(omSrcWithFeature,omDestWithFeature, withPriority.getOmWithFeature(), withPriority.getPriority(), withPriority.isConsiderForMerge() )
			);
			
			return withPriority;
		}
		
	}
	
	
	public static interface AssignPriority {
		
		ObjMaskWithFeatureAndPriority assignPriority(
			ObjMaskWithFeature omSrcWithFeature,
			ObjMaskWithFeature omDestWithFeature,
			ObjMask omMerge,
			EvaluateSingleObjMask featureEvaluatorSingleObj,
			LogErrorReporter logErrorReporter
		) throws OperationFailedException;
		
		void end() throws OperationFailedException;
	}
	
	public MergeGraph(
			EvaluateSingleObjMask featureEvaluator,
			UpdatableBeforeCondition beforeCondition,
			ImageRes res,
			AssignPriority assignPriority,
			boolean logSingleFeature
		) {
		super();
		this.featureEvaluatorSingleObj = featureEvaluator;

		this.assignPriority = assignPriority;
				
				
		graph = new NeighbourGraph( beforeCondition, res, new MergedEdgeCreator() );
		describeGraph = new DescribeGraph(graph, logSingleFeature);
	}
	
	
	public List<ObjMaskWithFeature> addObjsToGraph( ObjMaskCollection objs, LogErrorReporter logger ) throws OperationFailedException {
		
		List<ObjMaskWithFeature> listAdded = new ArrayList<>();				
		
		for( int i=0; i<objs.size(); i++ ) {
			
			ObjMask omSrc = objs.get(i);

			ObjMaskWithFeature omSrcWithFeature = wrapWithFeature(omSrc);
			graph.addVertex( omSrcWithFeature, listAdded, logger);
			listAdded.add( omSrcWithFeature );
		}
		
		return listAdded;
	}
	
	public ObjMaskWithFeature merge( EdgeTypeWithVertices<ObjMaskWithFeature,ObjMaskWithFeatureAndPriority> bestImprovement, LogErrorReporter logErrorReporter ) throws OperationFailedException {
		
		Set<ObjMaskWithFeature> setPossibleNghbs = graph.neighbourNodesFor( bestImprovement );
		graph.removeVertex( bestImprovement.getNode1() );
		graph.removeVertex( bestImprovement.getNode2() );
		
		ObjMaskWithFeature omMerged = bestImprovement.getEdge().getOmWithFeature();
		graph.addVertex( omMerged, setPossibleNghbs, logErrorReporter );
		
		if (logErrorReporter!=null) {
			logErrorReporter.getLogReporter().log(
				describeGraph.describeMerge(omMerged, bestImprovement)	
			);
			//logErrorReporter.getLogReporter().logFormatted("Possible neighbours: %s", setPossibleNghbs );
		}
		
		return omMerged;
	}
	
	public EdgeTypeWithVertices<ObjMaskWithFeature,ObjMaskWithFeatureAndPriority> findMaxPriority() {
		
		EdgeTypeWithVertices<ObjMaskWithFeature,ObjMaskWithFeatureAndPriority> max = null;
		
		Comparator3i<Point3i> comparator = new Comparator3i<>();
		
		for( EdgeTypeWithVertices<ObjMaskWithFeature,ObjMaskWithFeatureAndPriority> entry : graph.edgeSetUnique() ) {
			
			ObjMaskWithFeatureAndPriority edge = entry.getEdge();
			
			// We skip any edges that don't off an improvement
			if (!edge.isConsiderForMerge()) {
				continue;
			}
			
			if (max==null || edge.getPriority()>max.getEdge().getPriority()) {
				max = entry;
			} else if ( FloatUtilities.areEqual(edge.getPriority(),max.getEdge().getPriority())) {
				
				// If we have equal values, we impose an arbitrary ordering
				// so as to keep the output of the algorithm as deterministic as possible
				int cmp = comparator.compare(
					max.getEdge().getOmWithFeature().getObjMask().findAnyPntOnMask(),
					edge.getOmWithFeature().getObjMask().findAnyPntOnMask()
				);
				if (cmp>0) {
					max = entry;
				}

				// It should only be possible to ever have cmp==0 unless objects overlap. If we get
				//  this far, we resign ourselves to non-determinism
			}
		}
		
		return max;
	}

	public void end() throws OperationFailedException {
		assignPriority.end();
	}

	public String graphDescription() {
		return describeGraph.description();
	}

	public ObjMaskCollection createObjMaskCollectionFromVertices() {
		return graph.createObjMaskCollectionFromVertices();
	}
	
	private ObjMaskWithFeature wrapWithFeature( ObjMask omSrc ) throws OperationFailedException {
		try {
			double featureSrc = featureEvaluatorSingleObj.calc(omSrc);
			return new ObjMaskWithFeature(omSrc,featureSrc); 
		} catch (FeatureCalcException e) {
			throw new OperationFailedException(e);
		}
	}
}