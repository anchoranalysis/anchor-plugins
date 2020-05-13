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
import org.anchoranalysis.feature.bean.provider.FeatureProvider;
import org.anchoranalysis.feature.calc.FeatureCalcException;
import org.anchoranalysis.feature.calc.FeatureInitParams;
import org.anchoranalysis.feature.session.FeatureSession;
import org.anchoranalysis.feature.session.calculator.FeatureCalculatorSingle;
import org.anchoranalysis.image.bean.provider.BinaryChnlProvider;
import org.anchoranalysis.image.binary.BinaryChnl;
import org.anchoranalysis.image.chnl.Chnl;
import org.anchoranalysis.image.feature.objmask.FeatureInputSingleObj;
import org.anchoranalysis.image.objmask.ObjMask;
import org.anchoranalysis.image.objmask.factory.CreateFromEntireChnlFactory;

// Treats the entire binaryimgchnl as an object, and sees if it passes an ObjMaskFilter
public class BinaryImgChnlProviderObjMaskRelateFeatures extends BinaryImgChnlProviderChnlSource {

	// START BEAN PROPERTIES
	@BeanField
	private BinaryChnlProvider binaryChnlMain;
	
	@BeanField
	private BinaryChnlProvider binaryChnlCompareTo;
	
	@BeanField
	private BinaryChnlProvider binaryChnlElse;
	
	@BeanField
	private FeatureProvider<FeatureInputSingleObj> featureProvider;
	
	@BeanField
	private RelationBean relation;
	// END BEAN PROPERTIES
	
	@Override
	protected BinaryChnl createFromSource(Chnl chnlSource) throws CreateException {

		BinaryChnl chnlMain = binaryChnlMain.create();
		
		ObjMask omMain = CreateFromEntireChnlFactory.createObjMask( chnlMain );
		ObjMask omCompareTo = CreateFromEntireChnlFactory.createObjMask(
			binaryChnlCompareTo.create()
		);
			
		FeatureCalculatorSingle<FeatureInputSingleObj> session = createSession();
		
		return calcRelation(
			omMain,
			omCompareTo,
			chnlMain,
			NRGStackUtilities.addNrgStack(session, chnlSource)
		);
	}
	
	private FeatureCalculatorSingle<FeatureInputSingleObj> createSession() throws CreateException {
		try {
			return FeatureSession.with(
				featureProvider.create(),
				new FeatureInitParams(),
				getSharedObjects().getFeature().getSharedFeatureSet(),
				getLogger()
			);
		} catch (FeatureCalcException e1) {
			throw new CreateException(e1);
		}
	}
	
	private BinaryChnl calcRelation( ObjMask omMain, ObjMask omCompareTo, BinaryChnl chnlMain, FeatureCalculatorSingle<FeatureInputSingleObj> session ) throws CreateException {
		try {
			double valMain = session.calc(
				new FeatureInputSingleObj(omMain)
			);
			double valCompareTo = session.calc(
				new FeatureInputSingleObj(omCompareTo)
			);
			
			if (relation.create().isRelationToValueTrue(valMain, valCompareTo)) {
				return chnlMain;
			} else {
				return binaryChnlElse.create();
			}
		} catch (FeatureCalcException e) {
			throw new CreateException(e);
		}
	}

	public FeatureProvider<FeatureInputSingleObj> getFeatureProvider() {
		return featureProvider;
	}

	public void setFeatureProvider(FeatureProvider<FeatureInputSingleObj> featureProvider) {
		this.featureProvider = featureProvider;
	}

	public RelationBean getRelation() {
		return relation;
	}

	public void setRelation(RelationBean relation) {
		this.relation = relation;
	}

	public BinaryChnlProvider getBinaryChnlMain() {
		return binaryChnlMain;
	}

	public void setBinaryChnlMain(BinaryChnlProvider binaryChnlMain) {
		this.binaryChnlMain = binaryChnlMain;
	}

	public BinaryChnlProvider getBinaryChnlCompareTo() {
		return binaryChnlCompareTo;
	}

	public void setBinaryChnlCompareTo(BinaryChnlProvider binaryChnlCompareTo) {
		this.binaryChnlCompareTo = binaryChnlCompareTo;
	}

	public BinaryChnlProvider getBinaryChnlElse() {
		return binaryChnlElse;
	}

	public void setBinaryChnlElse(BinaryChnlProvider binaryChnlElse) {
		this.binaryChnlElse = binaryChnlElse;
	}

}
