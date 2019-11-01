package ch.ethz.biol.cell.mpp.mark.proposer.merge;

import org.anchoranalysis.anchor.mpp.bean.init.MPPInitParams;
import org.anchoranalysis.anchor.mpp.bean.proposer.MarkMergeProposer;
import org.anchoranalysis.anchor.mpp.mark.Mark;
import org.anchoranalysis.anchor.mpp.proposer.ProposalAbnormalFailureException;
import org.anchoranalysis.anchor.mpp.proposer.ProposerContext;
import org.anchoranalysis.anchor.mpp.pxlmark.memo.PxlMarkMemo;
import org.anchoranalysis.bean.annotation.BeanField;
import org.anchoranalysis.core.error.InitException;
import org.anchoranalysis.core.index.GetOperationFailedException;

public class MarkMergeProposerReference extends MarkMergeProposer {

	/**
	 * 
	 */
	private static final long serialVersionUID = -4122761299434774623L;

	// Start BEAN
	@BeanField
	private String id;
	// End BEAN
	
	private MarkMergeProposer delegate = null;
	
	@Override
	public void onInit(MPPInitParams pso) throws InitException {
		super.onInit(pso);
		try {
			delegate = getSharedObjects().getMarkMergeProposerSet().getException(id);
		} catch (GetOperationFailedException e) {
			throw new InitException( String.format("Cannot find referenced markSplitProposer '%s'", id) );
		}
	}
	
	@Override
	public boolean isCompatibleWith(Mark testMark) {
		return delegate.isCompatibleWith(testMark);
	}

	@Override
	public Mark propose(PxlMarkMemo mark1, PxlMarkMemo mark2, ProposerContext context) throws ProposalAbnormalFailureException {
		return delegate.propose(mark1, mark2, context);
	}

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}
}
