package org.anchoranalysis.plugin.mpp.experiment.bean.feature;

import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.nio.file.Path;
import java.util.Optional;

import org.anchoranalysis.bean.error.BeanMisconfiguredException;
import org.anchoranalysis.bean.xml.RegisterBeanFactories;
import org.anchoranalysis.core.error.OperationFailedException;
import org.anchoranalysis.experiment.ExperimentExecutionArguments;
import org.anchoranalysis.experiment.ExperimentExecutionException;
import org.anchoranalysis.experiment.JobExecutionException;
import org.anchoranalysis.experiment.bean.logreporter.LogReporterBean;
import org.anchoranalysis.experiment.log.reporter.StatefulLogReporter;
import org.anchoranalysis.experiment.task.ParametersExperiment;
import org.anchoranalysis.experiment.task.ParametersUnbound;
import org.anchoranalysis.experiment.task.Task;
import org.anchoranalysis.io.error.AnchorIOException;
import org.anchoranalysis.io.input.InputFromManager;
import org.anchoranalysis.io.output.bound.BoundOutputManager;
import org.anchoranalysis.io.output.bound.BoundOutputManagerRouteErrors;
import org.anchoranalysis.test.image.OutputManagerFixture;

/**
 * Executes a task on a single-input outputting into a specific directory
 * 
 * @author owen
 *
 */
class TaskSingleInputHelper {
	
	private TaskSingleInputHelper() {
	}

	
	/**
	 * Executes a task on a single-input
	 * 
	 * @param <T> input type
	 * @param <S> shared-state type
	 * @param <V> task type
	 * @param input the input for the task
	 * @param task the task to run
	 * @param pathDirOutput an absolute path to a directory where outputs of the task will be placed
	 * @param pathDirSaved a path (relative to the src/test/resources)  to a directory of saved-results to compare with
	 * @param pathsFileToCompare paths (relative to the src/test/resources) to check that are identical
	 * @return true if successful, false otherwise
	 * @throws OperationFailedException if anything goes wrong 
	 */
	public static <T extends InputFromManager, S, V extends Task<T, S>> void runTaskAndCompareOutputs(
		T input,
		V task,
		Path pathDirOutput,
		String pathDirSaved,
		String[] pathsFileToCompare
	) throws OperationFailedException {
		
		boolean successful = runTaskOnSingleInput(
			input,
			task,
			pathDirOutput
		);
		// Successful outcome
		assertTrue("Sucessful execution of task", successful);
		
		CompareHelper.compareOutputWithSaved(
			pathDirOutput,
			pathDirSaved,
			pathsFileToCompare
		);
	}
	
	/**
	 * Executes a task on a single-input
	 * 
	 * @param <T> input type
	 * @param <S> shared-state type
	 * @param <V> task type
	 * @param input the input for the task
	 * @param task the task to run
	 * @param pathForOutputs a directory where outputs of the task will be placed
	 * @return true if successful, false otherwise
	 * @throws OperationFailedException if anything goes wrong 
	 */
	private static <T extends InputFromManager, S, V extends Task<T, S>> boolean runTaskOnSingleInput(
		T input,
		V task,
		Path pathForOutputs
	) throws OperationFailedException {
		try {
			task.checkMisconfigured( RegisterBeanFactories.getDefaultInstances() );
			
			BoundOutputManagerRouteErrors bom = OutputManagerFixture.outputManagerForRouterErrors(pathForOutputs);
			
			StatefulLogReporter logReporter = createStatefulLogReporter();
			
			ParametersExperiment paramsExp = createParametersExperiment(
				pathForOutputs,
				bom.getDelegate(),
				logReporter
			);
			
			S sharedState = task.beforeAnyJobIsExecuted(
				bom,
				paramsExp
			);
			
			boolean successful = task.executeJob(
				createParamsUnbound(
					input,
					sharedState,
					paramsExp,
					bom.getDelegate(),
					pathForOutputs
				)
			);
			
			task.afterAllJobsAreExecuted(bom, sharedState, logReporter);
			
			return successful;
		} catch (AnchorIOException | ExperimentExecutionException | JobExecutionException | BeanMisconfiguredException e) {
			throw new OperationFailedException(e);
		}
	}
	
	
	private static <T extends InputFromManager, S> ParametersUnbound<T, S> createParamsUnbound(
			T input,
			S sharedState,
			ParametersExperiment paramsExp,
			BoundOutputManager outputManager,
			Path pathTempFolder
		) throws AnchorIOException {

					
			ParametersUnbound<T, S> paramsUnbound = new ParametersUnbound<>(
				paramsExp
			);
			paramsUnbound.setInputObject( input );
			paramsUnbound.setSharedState( sharedState );
			paramsUnbound.setSupressExceptions(false);
			return paramsUnbound;
		}

	
	private static ParametersExperiment createParametersExperiment( Path pathTempFolder, BoundOutputManager outputManager, StatefulLogReporter logReporter ) throws AnchorIOException {
		ParametersExperiment params = new ParametersExperiment(
			new ExperimentExecutionArguments(),
			"arbitraryExperimentName",
			Optional.empty(),
			outputManager,
			logReporter,
			false
		);
		params.setLogReporterTaskCreator(
			createLogReporterBean()
		);
		return params;
	}
	
	
	private static LogReporterBean createLogReporterBean() {
		LogReporterBean logReporter = mock(LogReporterBean.class);
		when(
			logReporter.create(
				any(),
				any(),
				any(),
				any(),
				anyBoolean()
			)
		).thenReturn(
			createStatefulLogReporter()
		);
		return logReporter;
	}
	
	
	private static StatefulLogReporter createStatefulLogReporter() {
		return mock(StatefulLogReporter.class);
	}
	
}
