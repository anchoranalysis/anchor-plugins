/* (C)2020 */
package org.anchoranalysis.plugin.mpp.bean.proposer.mark;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Optional;
import org.anchoranalysis.anchor.mpp.bean.proposer.MarkProposer;
import org.anchoranalysis.anchor.mpp.mark.Mark;
import org.anchoranalysis.anchor.mpp.mark.conic.MarkEllipse;
import org.anchoranalysis.anchor.mpp.proposer.ProposerContext;
import org.anchoranalysis.anchor.mpp.proposer.visualization.CreateProposalVisualization;
import org.anchoranalysis.anchor.mpp.pxlmark.memo.VoxelizedMarkMemo;
import org.anchoranalysis.bean.annotation.BeanField;
import org.anchoranalysis.io.bean.deserializer.ObjectInputStreamDeserializer;
import org.anchoranalysis.io.deserializer.DeserializationFailedException;

public class FromSerializedObject extends MarkProposer {

    // START BEAN
    @BeanField private String filePath;
    // END BEAN

    @Override
    public boolean isCompatibleWith(Mark testMark) {
        return testMark instanceof MarkEllipse;
    }

    @Override
    public boolean propose(VoxelizedMarkMemo inputMark, ProposerContext context) {

        ObjectInputStreamDeserializer<Mark> deserializer = new ObjectInputStreamDeserializer<>();
        try {
            Path path = Paths.get(filePath);
            inputMark.assignFrom(deserializer.deserialize(path));
            return true;

        } catch (DeserializationFailedException e) {
            context.getErrorNode().addFormatted("Exception when deserializing: %s", e.toString());
            return false;
        }
    }

    public String getFilePath() {
        return filePath;
    }

    public void setFilePath(String filePath) {
        this.filePath = filePath;
    }

    @Override
    public Optional<CreateProposalVisualization> proposalVisualization(boolean detailed) {
        return Optional.empty();
    }
}
