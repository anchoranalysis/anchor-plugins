package org.anchoranalysis.plugin.image.bean.chnl.level;

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


import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.anchoranalysis.bean.annotation.BeanField;
import org.anchoranalysis.bean.annotation.Positive;
import org.anchoranalysis.core.error.CreateException;
import org.anchoranalysis.core.error.OperationFailedException;
import org.anchoranalysis.core.graph.GraphWithEdgeTypes;
import org.anchoranalysis.image.channel.Channel;
import org.anchoranalysis.image.histogram.Histogram;
import org.anchoranalysis.image.objectmask.ObjectCollection;
import org.anchoranalysis.image.objectmask.ObjectMaskWithHistogram;
import org.anchoranalysis.image.voxel.box.VoxelBox;
import org.anchoranalysis.image.voxel.nghb.CreateNghbGraph;

/*
 *  Calculates a threshold-level for each object collectively based on other objects
 *  
 *  <p>
 *  A neighbourhood-graph is compiled of objects that touch each other. The threshold for each objects is determined by the object itself and neigbours that have dist < nghbDist (e.g. nghbDist==1 are all the immediate neighbours).
 *  </p>
 */
public class ChnlProviderObjsLevelNeighbours extends ChnlProviderLevel {

	// START BEAN
	/** How many neighbours to include by distance (distance==1 -> all directly touching neighbours, distance==2 -> those touching the directly touching etc.) */
	@BeanField @Positive
	private int nghbDist;		// Determines the neighbour distance
	// END BEAN
	
	@Override
	protected Channel createFor(Channel chnlIntensity, ObjectCollection objs, Channel chnlOutput) throws CreateException {
		try {
			setAgainstNghb( chnlIntensity, chnlOutput, objs, nghbDist );
			
			return chnlOutput;
			
		} catch (OperationFailedException e) {
			throw new CreateException(e);
		}
	}

	private static Collection<Histogram> collection( Collection<ObjectMaskWithHistogram> edges ) {
		List<Histogram> out = new ArrayList<>();
		for ( ObjectMaskWithHistogram om : edges) {
			out.add( om.getHistogram() );
		}
		return out;
	}
	
	private static void visit(
		GraphWithEdgeTypes<ObjectMaskWithHistogram,Integer> graph,
		List<ObjectMaskWithHistogram> currentVisit,
		List<ObjectMaskWithHistogram> toVisit,
		Set<ObjectMaskWithHistogram> visited
	) {
		for( ObjectMaskWithHistogram omLocal : currentVisit) {
			
			Collection<ObjectMaskWithHistogram> adjacent = graph.adjacentVertices(omLocal);
			for( ObjectMaskWithHistogram omAdjacent : adjacent ) {
				if (!visited.contains(omAdjacent)) {
					toVisit.add(omAdjacent);
				}
			}
			
			visited.add(omLocal);
		}
	}
	
	private static Collection<ObjectMaskWithHistogram> verticesWithinDist(
		GraphWithEdgeTypes<ObjectMaskWithHistogram,Integer> graph,
		ObjectMaskWithHistogram om,
		int nghbDist
	) {
		if (nghbDist==1) {
			return graph.adjacentVertices(om);
		} else {
			
			Set<ObjectMaskWithHistogram> visited = new HashSet<>();
			
			List<ObjectMaskWithHistogram> toVisit = new ArrayList<>();
			toVisit.add(om);
			
			for( int i=0; i<nghbDist; i++) {
				List<ObjectMaskWithHistogram> currentVisit = toVisit;
				toVisit = new ArrayList<>();
				visit( graph, currentVisit, toVisit, visited);
			}
			return visited;
		}
	}
	
	/** Sums hist and everything in others 
	 * @throws OperationFailedException */
	private static Histogram createSumHistograms( Histogram hist, Collection<Histogram> others ) throws OperationFailedException {
		Histogram out = hist.duplicate();
		for( Histogram h : others ) {
			out.addHistogram(h);
		}
		return out;
	}
	
	private void setAgainstNghb( Channel chnlIntensity, Channel chnlOutput, ObjectCollection objMasks, int nghbDist ) throws OperationFailedException {
		
		try {
			CreateNghbGraph<ObjectMaskWithHistogram> graphCreator = new CreateNghbGraph<ObjectMaskWithHistogram>( false );
			
			GraphWithEdgeTypes<ObjectMaskWithHistogram,Integer> graph = graphCreator.createGraphWithNumPixels(
				addHistogramToList(objMasks, chnlIntensity),
				(ObjectMaskWithHistogram vt) -> vt.getObjMask(),
				chnlIntensity.getDimensions().getExtnt(),
				true
			);
			
			VoxelBox<?> vbOutput = chnlOutput.getVoxelBox().any();
			
			// We don't need this for the computation, used only for outputting debugging
			Map<ObjectMaskWithHistogram,Integer> mapLevel = new HashMap<>();
			
			Collection<ObjectMaskWithHistogram> objs = graph.vertexSet();
			for( ObjectMaskWithHistogram om : objs ) {
		
				getLogger().getLogReporter().logFormatted("Setting for %s against neighbourhood", om.getObjMask().centerOfGravity() );
				
				// Get the neighbours of the current object
				Collection<ObjectMaskWithHistogram> vertices = verticesWithinDist(graph, om, nghbDist);
				
				for( ObjectMaskWithHistogram nghb : vertices ) {
					getLogger().getLogReporter().logFormatted("Including neighbour %s", nghb.getObjMask().centerOfGravity() );
				}
				
				// Level calculated from combined histograms
				int level = calcLevelCombinedHist(om, vertices);
				
				vbOutput.setPixelsCheckMask(om.getObjMask(), level);
				
				getLogger().getLogReporter().logFormatted("Setting threshold %d at %s", level, om.getObjMask().centerOfGravity() );
				mapLevel.put(om, level);
			}

		} catch (CreateException e) {
			throw new OperationFailedException(e);
		}
	}
	
	private static List<ObjectMaskWithHistogram> addHistogramToList( ObjectCollection objMasks, Channel chnlIntensity ) {
		return objMasks.asList().stream().map(	om ->
			{
				try {
					return new ObjectMaskWithHistogram(om,chnlIntensity);
				} catch (CreateException e) {
					throw new RuntimeException(e);
				}
			}
		).collect( Collectors.toList() );	
	}
	
	private int calcLevelCombinedHist( ObjectMaskWithHistogram om, Collection<ObjectMaskWithHistogram> vertices ) throws OperationFailedException {
		Histogram histSum = createSumHistograms( om.getHistogram(),  collection( vertices ) );	
		return getCalculateLevel().calculateLevel(histSum);
	}
	
	public int getNghbDist() {
		return nghbDist;
	}

	public void setNghbDist(int nghbDist) {
		this.nghbDist = nghbDist;
	}
}
