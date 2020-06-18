package org.anchoranalysis.test.bean;

import org.anchoranalysis.bean.AnchorBean;
import org.anchoranalysis.bean.BeanInstanceMap;
import org.anchoranalysis.bean.error.BeanMisconfiguredException;
import org.anchoranalysis.bean.init.InitializableBean;
import org.anchoranalysis.bean.init.params.BeanInitParams;
import org.anchoranalysis.core.error.InitException;
import org.anchoranalysis.core.error.friendly.AnchorFriendlyRuntimeException;
import org.anchoranalysis.core.log.LogErrorReporter;

/**
 * Checks to see if a bean has been misconfigured, when created manually in tests
 * (thereby skipping the usual checks during the BeanXML loading process)
 * 
 * <p>Additionally wraps the exceptions thrown in {@link AnchorFriendlyRuntimeException} to make tests more readable,
 * rather than having too many different checked exception types in the test code, for non-operational failures.</p>
 * 
 * @author Owen Feehan
 *
 */
public class BeanTestChecker {

	private BeanTestChecker() {}
	
	/** 
	 * Checks if a bean has all necessary items, throwing a run-time exception if it does not
	 * 
	 * @param <T> bean-type
	 * @param bean bean to check
	 **/
	public static <T extends AnchorBean<?>> T check(T bean) {
		try {
			bean.checkMisconfigured( new BeanInstanceMap() );
		} catch (BeanMisconfiguredException e) {
			throw new AnchorFriendlyRuntimeException(e);
		}
		return bean;
	}

	/** 
	 * Checks if a bean has all necessary items like {@link check} and also initializes the bean
	 * 
	 * @param <T> bean-type
	 * @param <P> initialization-parameters-type accepted by the bean
	 * @param bean bean to check and initialize
	 * @param params initialization-parameters
	 * @param logger logger
	 **/
	public static <T extends InitializableBean<?,P>,P extends BeanInitParams> T checkAndInit(
		T bean,
		P params,
		LogErrorReporter logger
	) {
		check(bean);
		try {
			bean.initRecursive(params, logger);
		} catch (InitException e) {
			throw new AnchorFriendlyRuntimeException(e);
		}
		return bean;
	}
}
