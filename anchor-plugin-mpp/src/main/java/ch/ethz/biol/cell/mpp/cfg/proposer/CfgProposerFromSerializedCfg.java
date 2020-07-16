/* (C)2020 */
package ch.ethz.biol.cell.mpp.cfg.proposer;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Optional;
import lombok.Getter;
import lombok.Setter;
import org.anchoranalysis.anchor.mpp.bean.cfg.CfgGen;
import org.anchoranalysis.anchor.mpp.bean.proposer.CfgProposer;
import org.anchoranalysis.anchor.mpp.cfg.Cfg;
import org.anchoranalysis.anchor.mpp.mark.Mark;
import org.anchoranalysis.anchor.mpp.mark.conic.MarkEllipse;
import org.anchoranalysis.anchor.mpp.proposer.ProposerContext;
import org.anchoranalysis.bean.annotation.BeanField;
import org.anchoranalysis.io.bean.deserializer.ObjectInputStreamDeserializer;
import org.anchoranalysis.io.deserializer.DeserializationFailedException;

public class CfgProposerFromSerializedCfg extends CfgProposer {

    // START BEAN PROPERTIES
    @BeanField @Getter @Setter private String filePath;
    // END BEAN PROPERTIES

    @Override
    public Optional<Cfg> propose(CfgGen cfgGen, ProposerContext context) {

        ObjectInputStreamDeserializer<Cfg> deserializer = new ObjectInputStreamDeserializer<>();
        try {
            Path path = Paths.get(filePath);
            return Optional.of(deserializer.deserialize(path));
        } catch (DeserializationFailedException e) {
            return Optional.empty();
        }
    }

    @Override
    public String getBeanDscr() {
        return toString();
    }

    @Override
    public boolean isCompatibleWith(Mark testMark) {
        return testMark instanceof MarkEllipse;
    }
}
