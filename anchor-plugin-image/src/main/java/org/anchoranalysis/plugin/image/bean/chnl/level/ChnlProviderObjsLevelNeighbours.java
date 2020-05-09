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
import org.anchoranalysis.image.chnl.Chnl;
import org.anchoranalysis.image.histogram.Histogram;
import org.anchoranalysis.image.objmask.ObjMaskCollection;
import org.anchoranalysis.image.objmask.ObjMaskWithHistogram;
import org.anchoranalysis.image.voxel.box.VoxelBox;
import org.anchoranalysis.image.voxel.nghb.CreateNghbGraph;

/*
 *  Calculates a threshold-level for each object collectively based on other objects
 *  
 *  <p>
 *  A neighbourhood-graph is compiled of objects that touch each other. The threshold for each objects is determined by the object itself and neigbours that have dist < nghbDist (e.g. nghbDist==1 are all the immediate neighbours).
 *  </p>
 */
public class ChnlProviderObjsLevelNeighbours extends ChnlProviderObjsLevelBase {

	// START BEAN
	/** How many neighbours to include by distance (distance==1 -> all directly touching neighbours, distance==2 -> those touching the directly touching etc.) */
	@BeanField @Positive
	private int nghbDist;		// Determines the neighbour distance
	// END BEAN
	
	@Override
	protected Chnl createFor(Chnl chnlIntensity, ObjMaskCollection objs, Chnl chnlOutput) throws CreateException {
		try {
			setAgainstNghb( chnlIntensity, chnlOutput, objs, nghbDist );
			
			return chnlOutput;
			
		} catch (OperationFailedException e) {
			throw new CreateException(e);
		}
	}

	private static Collection<Histogram> collection( Collection<ObjMaskWithHistogram> edges ) {
		List<Histogram> out = new ArrayList<>();
		for ( ObjMaskWithHistogram om : edges) {
			out.add( om.getHistogram() );
		}
		return out;
	}
	
	private static void visit(
		GraphWithEdgeTypes<ObjMaskWithHistogram,Integer> graph,
		List<ObjMaskWithHistogram> currentVisit,
		List<ObjMaskWithHistogram> toVisit,
		Set<ObjMaskWithHistogram> visited
	) {
		for( ObjMaskWithHistogram omLocal : currentVisit) {
			
			Collection<ObjMaskWithHistogram> adjacent = graph.adjacentVertices(omLocal);
			for( ObjMaskWithHistogram omAdjacent : adjacent ) {
				if (!visited.contains(omAdjacent)) {
					toVisit.add(omAdjacent);
				}
			}
			
			visited.add(omLocal);
		}
	}
	
	private static Collection<ObjMaskWithHistogram> verticesWithinDist(
		GraphWithEdgeTypes<ObjMaskWithHistogram,Integer> graph,
		ObjMaskWithHistogram om,
		int nghbDist
	) {
		if (nghbDist==1) {
			return graph.adjacentVertices(om);
		} else {
			
			Set<ObjMaskWithHistogram> visited = new HashSet<>();
			
			List<ObjMaskWithHistogram> toVisit = new ArrayList<>();
			toVisit.add(om);
			
			for( int i=0; i<nghbDist; i++) {
				List<ObjMaskWithHistogram> currentVisit = toVisit;
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
	
	private void setAgainstNghb( Chnl chnlIntensity, Chnl chnlOutput, ObjMaskCollection objMasks, int nghbDist ) throws OperationFailedException {
		
		try {
			CreateNghbGraph<ObjMaskWithHistogram> graphCreator = new CreateNghbGraph<ObjMaskWithHistogram>( false );
			
			GraphWithEdgeTypes<ObjMaskWithHistogram,Integer> graph = graphCreator.createGraphWithNumPixels(
				addHistogramToList(objMasks, chnlIntensity),
				(ObjMaskWithHistogram vt) -> vt.getObjMask(),
				chnlIntensity.getDimensions().getExtnt(),
				true
			);
			
			VoxelBox<?> vbOutput = chnlOutput.getVoxelBox().any();
			
			// We don't need this for the computation, used only for outputting debugging
			Map<ObjMaskWithHistogram,Integer> mapLevel = new HashMap<>();
			
			Collection<ObjMaskWithHistogram> objs = graph.vertexSet();
			for( ObjMaskWithHistogram om : objs ) {
		
				getLogger().getLogReporter().logFormatted("Setting for %s against neighbourhood", om.getObjMask().centerOfGravity() );
				
				// Get the neighbours of the current object
				Collection<ObjMaskWithHistogram> vertices = verticesWithinDist(graph, om, nghbDist);
				
				for( ObjMaskWithHistogram nghb : vertices ) {
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
	
	private static List<ObjMaskWithHistogram> addHistogramToList( ObjMaskCollection objMasks, Chnl chnlIntensity ) {
		return objMasks.asList().stream().map(	om ->
			{
				try {
					return new ObjMaskWithHistogram(om,chnlIntensity);
				} catch (CreateException e) {
					throw new RuntimeException(e);
				}
			}
		).collect( Collectors.toList() );	
	}
	
	private int calcLevelCombinedHist( ObjMaskWithHistogram om, Collection<ObjMaskWithHistogram> vertices ) throws OperationFailedException {
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
