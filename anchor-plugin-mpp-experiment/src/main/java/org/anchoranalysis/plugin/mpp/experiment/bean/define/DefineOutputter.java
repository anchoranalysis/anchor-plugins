package org.anchoranalysis.plugin.mpp.experiment.bean.define;

import java.util.Optional;

import org.anchoranalysis.bean.AnchorBean;
import org.anchoranalysis.bean.annotation.BeanField;
import org.anchoranalysis.bean.annotation.OptionalBean;
import org.anchoranalysis.bean.define.Define;
import org.anchoranalysis.bean.shared.params.keyvalue.KeyValueParamsProvider;
import org.anchoranalysis.core.error.CreateException;
import org.anchoranalysis.core.error.InitException;
import org.anchoranalysis.core.error.OperationFailedException;
import org.anchoranalysis.core.log.LogErrorReporter;
import org.anchoranalysis.experiment.task.InputBound;
import org.anchoranalysis.feature.nrg.NRGStackWithParams;
import org.anchoranalysis.image.bean.provider.stack.StackProvider;
import org.anchoranalysis.image.init.ImageInitParams;
import org.anchoranalysis.io.output.bound.BoundIOContext;
import org.anchoranalysis.mpp.io.input.InputForMPPBean;
import org.anchoranalysis.mpp.io.input.MPPInitParamsFactory;
import org.anchoranalysis.mpp.io.output.NRGStackWriter;
import org.anchoranalysis.plugin.mpp.experiment.outputter.SharedObjectsUtilities;

/** 
 * Helper for tasks that uses a {@link Define} in association with an input to execute some tasks, and then outputs results
 *  * 
 * @param <T> input-object type
 * */
public class DefineOutputter extends AnchorBean<DefineOutputter> {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	// START BEAN PROPERTIES
	@BeanField @OptionalBean
	private Define define;
	
	@BeanField
	private StackProvider nrgStackProvider;
	
	@BeanField @OptionalBean
	private KeyValueParamsProvider nrgParamsProvider;
	// END BEAN PROPERTIES
	
	@FunctionalInterface
	public interface OperationWithInitAndNRGStack {
		void process(ImageInitParams initParams,NRGStackWithParams nrgStack) throws OperationFailedException;
	}
	
	public void processInput(
		InputBound<? extends InputForMPPBean,?> input,
		OperationWithInitAndNRGStack operation
	) throws OperationFailedException {

		BoundIOContext context = input.context();
		try {
			ImageInitParams initParams = MPPInitParamsFactory.createFromInputAsImage(
				input,
				Optional.ofNullable(define)
			);
			
			NRGStackWithParams nrgStack = createNRGStack(initParams, context.getLogger());
			
			operation.process(initParams, nrgStack);
			
			outputSharedObjs(initParams, nrgStack, context);
			
		} catch (InitException | CreateException e) {
			throw new OperationFailedException(e);
		}
	}
	
	// General objects can be outputted
	private void outputSharedObjs(ImageInitParams initParams, NRGStackWithParams nrgStack, BoundIOContext context) {
		SharedObjectsUtilities.output(initParams, false, context);
		
		NRGStackWriter.writeNRGStack(
			nrgStack,
			context
		);
	}
		
	private NRGStackWithParams createNRGStack(
		ImageInitParams so,
		LogErrorReporter logger
	) throws InitException, CreateException {

		// Extract the NRG stack
		StackProvider nrgStackProviderLoc = nrgStackProvider.duplicateBean();
		nrgStackProviderLoc.initRecursive(so, logger);
		NRGStackWithParams stack = new NRGStackWithParams(nrgStackProviderLoc.create());

		if(nrgParamsProvider!=null) {
			nrgParamsProvider.initRecursive(so.getParams(), logger);
			stack.setParams(nrgParamsProvider.create());
		}
		return stack;
	}
	
	public StackProvider getNrgStackProvider() {
		return nrgStackProvider;
	}

	public void setNrgStackProvider(StackProvider nrgStackProvider) {
		this.nrgStackProvider = nrgStackProvider;
	}
	
	public KeyValueParamsProvider getNrgParamsProvider() {
		return nrgParamsProvider;
	}

	public void setNrgParamsProvider(KeyValueParamsProvider nrgParamsProvider) {
		this.nrgParamsProvider = nrgParamsProvider;
	}
	
	public Define getDefine() {
		return define;
	}

	public void setDefine(Define define) {
		this.define = define;
	}

}
