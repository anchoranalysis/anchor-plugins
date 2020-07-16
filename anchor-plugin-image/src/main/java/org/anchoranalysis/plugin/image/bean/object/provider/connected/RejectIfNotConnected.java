/* (C)2020 */
package org.anchoranalysis.plugin.image.bean.object.provider.connected;

import org.anchoranalysis.core.error.CreateException;
import org.anchoranalysis.core.error.OperationFailedException;
import org.anchoranalysis.image.bean.provider.ObjectCollectionProviderUnary;
import org.anchoranalysis.image.object.ObjectCollection;
import org.anchoranalysis.image.object.ObjectMask;

/**
 * Rejects a set of objects, if any object is not fully connected (pixels form two or more seperate
 * connected components)
 *
 * @author Owen Feehan
 */
public class RejectIfNotConnected extends ObjectCollectionProviderUnary {

    @Override
    public ObjectCollection createFromObjects(ObjectCollection objects) throws CreateException {

        for (ObjectMask objectMask : objects) {
            try {
                if (!objectMask.checkIfConnected()) {
                    throw new CreateException("At least one object is not connected");
                }
            } catch (OperationFailedException e) {
                throw new CreateException(e);
            }
        }

        return objects;
    }
}
