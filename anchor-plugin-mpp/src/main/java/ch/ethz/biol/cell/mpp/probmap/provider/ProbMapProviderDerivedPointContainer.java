package ch.ethz.biol.cell.mpp.probmap.provider;

/*-
 * #%L
 * anchor-plugin-mpp
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

import org.anchoranalysis.bean.annotation.BeanField;
import org.anchoranalysis.core.error.CreateException;
import org.anchoranalysis.core.error.OptionalOperationUnsupportedException;
import org.anchoranalysis.core.geometry.Point3d;
import org.anchoranalysis.core.random.RandomNumberGenerator;
import org.anchoranalysis.image.bean.provider.BinaryImgChnlProvider;
import org.anchoranalysis.image.binary.BinaryChnl;
import org.anchoranalysis.image.extent.ImageDim;

import ch.ethz.biol.cell.mpp.pair.IUpdatableMarkSet;
import ch.ethz.biol.cell.mpp.probmap.ProbMap;
import ch.ethz.biol.cell.mpp.probmap.container.UpdatablePoint3dContainer;

public class ProbMapProviderDerivedPointContainer extends ProbMapProvider {

	/**
	 * 
	 */
	private static final long serialVersionUID = -6881986528956653861L;
	
	// START BEAN PARAMETERS
	@BeanField
	private BinaryImgChnlProvider binaryImgChnlProvider;
	
	@BeanField
	private UpdatablePoint3dContainer pointContainer;
	
	@BeanField
	private boolean updatable = true;
	// END BEAN PARAMETERS
	
	public ProbMapProviderDerivedPointContainer() {
	}

	public UpdatablePoint3dContainer getPointContainer() {
		return pointContainer;
	}

	public void setPointContainer(UpdatablePoint3dContainer pointContainer) {
		this.pointContainer = pointContainer;
	}

	public boolean isUpdatable() {
		return updatable;
	}


	public void setUpdatable(boolean updatable) {
		this.updatable = updatable;
	}

	@Override
	public String getBeanDscr() {
		return getBeanName();
	}

	public BinaryImgChnlProvider getBinaryImgChnlProvider() {
		return binaryImgChnlProvider;
	}

	public void setBinaryImgChnlProvider(BinaryImgChnlProvider binaryImgChnlProvider) {
		this.binaryImgChnlProvider = binaryImgChnlProvider;
	}

	@Override
	public ProbMap create() throws CreateException {
		if (pointContainer.size()==0) {
			throw new CreateException("PointContainer contains 0 points");
		}
		return new ProbMapDerivedPointContainer(updatable, binaryImgChnlProvider.create(), pointContainer);
	}
	
	
	public static class ProbMapDerivedPointContainer extends ProbMap {
	
		private BinaryChnl binaryImgChnl;
		private UpdatablePoint3dContainer pointContainer;
		private boolean updatable;
		
		public ProbMapDerivedPointContainer(boolean updatable,
				BinaryChnl binaryImgChnl,
				UpdatablePoint3dContainer pointContainer) {
			super();
			this.binaryImgChnl = binaryImgChnl;
			this.pointContainer = pointContainer;
			this.updatable = updatable;
		}

		// Randomise location
	    @Override
		public Point3d sample( RandomNumberGenerator re ) {
	    	return pointContainer.sample(re);
	    }
		
		@Override
		public ImageDim getDimensions() {
			return binaryImgChnl.getDimensions();
		}
		
		@Override
		public IUpdatableMarkSet updater() {
			if (updatable) {
				return pointContainer;
			} else {
				return null;
			}
		}
	
		
		@Override
		public BinaryChnl visualization() throws OptionalOperationUnsupportedException {
			throw new OptionalOperationUnsupportedException();
		}
		
//		private static class Updater implements IUpdatableMarkSet {
//	
//			private UpdatablePoint3dContainer pointContainer;
//			
//			public Updater(UpdatablePoint3dContainer pointContainer) {
//				super();
//				this.pointContainer = pointContainer;
//			}
//
//			@Override
//			public void initUpdatableMarkSet(IGetMemoForIndex marks, NrgStack nrgStack, LogReporter logReporter) throws InitException {
//				
//				try {
//					pointContainer.initUpdatableMarkSet(marks, nrgStack, logReporter);
//					
////					for (int i=0; i<marks.size(); i++) {
////						
////						PxlMarkMemo pmm = marks.getMemoForIndex(i);
////						
////						// We ignore the existing marks
////						pointContainer.add(null, pmm);
////						//pointContainer.rmvPntsInMark( pmm );
////					}
//
//				} catch (UpdateMarkSetException e) {
//					throw new InitException(e);
//				}
//			}
//		
//			@Override
//			public void add(IGetMemoForIndex marksExisting, PxlMarkMemo newMark) throws UpdateMarkSetException {
//		
//				pointContainer.add(marksExisting, newMark);
//			}
//		
//			@Override
//			public void exchange(IGetMemoForIndex pxlMarkMemoList, PxlMarkMemo oldMark,
//					int indexOldMark, PxlMarkMemo newMark) throws UpdateMarkSetException {
//				
//				pointContainer.exchange(pxlMarkMemoList, oldMark, indexOldMark, newMark);
//			}
//		
//			@Override
//			public void rmv(IGetMemoForIndex marksExisting, PxlMarkMemo mark) throws UpdateMarkSetException {
//				
//				pointContainer.rmv(marksExisting, mark);
//			}
//
//			
//			@Override
//			public void setOutputManager(BoundOutputManagerRouteErrors outputManager) {
//				
//			}
//		}


	}


}
