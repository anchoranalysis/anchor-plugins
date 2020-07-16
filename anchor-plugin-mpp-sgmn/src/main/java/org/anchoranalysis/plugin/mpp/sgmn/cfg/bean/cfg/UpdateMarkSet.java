/* (C)2020 */
package org.anchoranalysis.plugin.mpp.sgmn.cfg.bean.cfg;

import org.anchoranalysis.anchor.mpp.bean.init.MPPInitParams;
import org.anchoranalysis.anchor.mpp.feature.mark.ListUpdatableMarkSetCollection;
import org.anchoranalysis.anchor.mpp.feature.mark.MemoList;
import org.anchoranalysis.anchor.mpp.mark.Mark;
import org.anchoranalysis.anchor.mpp.pair.Pair;
import org.anchoranalysis.anchor.mpp.pair.PairCollection;
import org.anchoranalysis.core.error.InitException;
import org.anchoranalysis.core.error.OperationFailedException;
import org.anchoranalysis.core.log.Logger;
import org.anchoranalysis.core.name.provider.NamedProviderGetException;
import org.anchoranalysis.feature.nrg.NRGStackWithParams;

class UpdateMarkSet {

    private MPPInitParams psoImage;
    private NRGStackWithParams nrgStack;
    private ListUpdatableMarkSetCollection updatableMarkSetCollection;
    private Logger logger;

    public UpdateMarkSet(
            MPPInitParams psoImage,
            NRGStackWithParams nrgStack,
            ListUpdatableMarkSetCollection updatableMarkSetCollection,
            Logger logger) {
        super();
        this.psoImage = psoImage;
        this.nrgStack = nrgStack;
        this.updatableMarkSetCollection = updatableMarkSetCollection;
        this.logger = logger;
    }

    public void apply() throws OperationFailedException {
        makePairsUpdatable();
    }

    private void makePairsUpdatable() throws OperationFailedException {

        try {
            for (String key : psoImage.getSimplePairCollection().keys()) {
                PairCollection<Pair<Mark>> pair =
                        psoImage.getSimplePairCollection().getException(key);
                pair.initUpdatableMarkSet(
                        new MemoList(),
                        nrgStack,
                        logger,
                        psoImage.getFeature().getSharedFeatureSet());
                updatableMarkSetCollection.add(pair);
            }
        } catch (InitException e) {
            throw new OperationFailedException(e);
        } catch (NamedProviderGetException e) {
            throw new OperationFailedException(e.summarize());
        }
    }
}
