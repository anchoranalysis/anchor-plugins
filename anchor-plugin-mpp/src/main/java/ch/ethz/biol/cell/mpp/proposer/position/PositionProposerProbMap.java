/* (C)2020 */
package ch.ethz.biol.cell.mpp.proposer.position;

import java.util.Optional;
import lombok.Getter;
import lombok.Setter;
import org.anchoranalysis.anchor.mpp.bean.init.MPPInitParams;
import org.anchoranalysis.anchor.mpp.bean.proposer.PositionProposerBean;
import org.anchoranalysis.anchor.mpp.probmap.ProbMap;
import org.anchoranalysis.anchor.mpp.proposer.ProposerContext;
import org.anchoranalysis.bean.annotation.BeanField;
import org.anchoranalysis.core.error.InitException;
import org.anchoranalysis.core.geometry.Point3d;
import org.anchoranalysis.core.name.provider.NamedProviderGetException;

public class PositionProposerProbMap extends PositionProposerBean {

    // BEAN PROPERTIES
    @BeanField @Getter @Setter private String probMapID = "";
    // END BEAN PROPERTIES

    private ProbMap probMap;

    @Override
    public void onInit(MPPInitParams pso) throws InitException {
        super.onInit(pso);
        try {
            this.probMap = getInitializationParameters().getProbMapSet().getException(probMapID);
        } catch (NamedProviderGetException e) {
            throw new InitException(e.summarize());
        }
    }

    @Override
    public Optional<Point3d> propose(ProposerContext context) {
        return probMap.sample(context.getRandomNumberGenerator());
    }
}
