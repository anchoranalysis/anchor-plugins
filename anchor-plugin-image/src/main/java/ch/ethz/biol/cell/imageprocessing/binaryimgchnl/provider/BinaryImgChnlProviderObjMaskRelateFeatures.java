package ch.ethz.biol.cell.imageprocessing.binaryimgchnl.provider;

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
import org.anchoranalysis.bean.shared.relation.RelationBean;
import org.anchoranalysis.core.error.CreateException;
import org.anchoranalysis.core.error.InitException;
import org.anchoranalysis.feature.bean.Feature;
import org.anchoranalysis.feature.bean.provider.FeatureProvider;
import org.anchoranalysis.feature.calc.FeatureCalcException;
import org.anchoranalysis.feature.nrg.NRGStackWithParams;
import org.anchoranalysis.image.bean.provider.BinaryImgChnlProvider;
import org.anchoranalysis.image.bean.provider.ChnlProvider;
import org.anchoranalysis.image.binary.BinaryChnl;
import org.anchoranalysis.image.chnl.Chnl;
import org.anchoranalysis.image.feature.session.FeatureSessionCreateParamsSingle;
import org.anchoranalysis.image.objmask.ObjMask;
import org.anchoranalysis.image.objmask.factory.CreateFromEntireChnlFactory;

// Treats the entire binaryimgchnl as an object, and sees if it passes an ObjMaskFilter
public class BinaryImgChnlProviderObjMaskRelateFeatures extends BinaryImgChnlProvider {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	// START BEAN PROPERTIES
	@BeanField
	private BinaryImgChnlProvider binaryImgChnlProviderMain;
	
	@BeanField
	private BinaryImgChnlProvider binaryImgChnlProviderCompareTo;
	
	@BeanField
	private BinaryImgChnlProvider binaryImgChnlProviderElse;
	
	@BeanField
	private FeatureProvider featureProvider;
	
	@BeanField
	private ChnlProvider chnlProvider;
	
	@BeanField
	private RelationBean relation;
	// END BEAN PROPERTIES
	
	@Override
	public BinaryChnl create() throws CreateException {
		
		BinaryChnl chnlMain = binaryImgChnlProviderMain.create();
		
		ObjMask omMain = CreateFromEntireChnlFactory.createObjMask( chnlMain );
		ObjMask omCompareTo = CreateFromEntireChnlFactory.createObjMask( binaryImgChnlProviderCompareTo.create() );
		
		Feature feature = featureProvider.create();
		
		FeatureSessionCreateParamsSingle session = new FeatureSessionCreateParamsSingle(
			feature,
			getSharedObjects().getFeature().getSharedFeatureSet()
		);
		
		try {
			session.start( getLogger() );
		} catch (InitException e1) {
			throw new CreateException(e1);
		}
		
		if (chnlProvider!=null) {
			Chnl chnl = chnlProvider.create();
			NRGStackWithParams nrgStack = new NRGStackWithParams(chnl);
			session.setNrgStack(nrgStack);
		}
		
		try {
			double valMain = session.calc( omMain );
			double valCompareTo = session.calc( omCompareTo );
			
			if (relation.create().isRelationToValueTrue(valMain, valCompareTo)) {
				return chnlMain;
			} else {
				return binaryImgChnlProviderElse.create();
			}
		} catch (FeatureCalcException e) {
			throw new CreateException(e);
		}
	}

	public BinaryImgChnlProvider getBinaryImgChnlProviderMain() {
		return binaryImgChnlProviderMain;
	}

	public void setBinaryImgChnlProviderMain(
			BinaryImgChnlProvider binaryImgChnlProviderMain) {
		this.binaryImgChnlProviderMain = binaryImgChnlProviderMain;
	}

	public BinaryImgChnlProvider getBinaryImgChnlProviderCompareTo() {
		return binaryImgChnlProviderCompareTo;
	}

	public void setBinaryImgChnlProviderCompareTo(
			BinaryImgChnlProvider binaryImgChnlProviderCompareTo) {
		this.binaryImgChnlProviderCompareTo = binaryImgChnlProviderCompareTo;
	}

	public BinaryImgChnlProvider getBinaryImgChnlProviderElse() {
		return binaryImgChnlProviderElse;
	}

	public void setBinaryImgChnlProviderElse(
			BinaryImgChnlProvider binaryImgChnlProviderElse) {
		this.binaryImgChnlProviderElse = binaryImgChnlProviderElse;
	}

	public FeatureProvider getFeatureProvider() {
		return featureProvider;
	}

	public void setFeatureProvider(FeatureProvider featureProvider) {
		this.featureProvider = featureProvider;
	}

	public ChnlProvider getChnlProvider() {
		return chnlProvider;
	}

	public void setChnlProvider(ChnlProvider chnlProvider) {
		this.chnlProvider = chnlProvider;
	}

	public RelationBean getRelation() {
		return relation;
	}

	public void setRelation(RelationBean relation) {
		this.relation = relation;
	}
}
