/* (C)2020 */
package ch.ethz.biol.cell.mpp.cfg.provider;

import lombok.Getter;
import lombok.Setter;
import org.anchoranalysis.anchor.mpp.bean.cfg.CfgProvider;
import org.anchoranalysis.anchor.mpp.cfg.Cfg;
import org.anchoranalysis.anchor.mpp.mark.points.MarkPointListFactory;
import org.anchoranalysis.bean.annotation.BeanField;
import org.anchoranalysis.core.error.CreateException;
import org.anchoranalysis.image.bean.provider.ObjectCollectionProvider;
import org.anchoranalysis.image.object.ObjectCollection;
import org.anchoranalysis.image.object.ObjectMask;
import org.anchoranalysis.image.points.PointsFromObject;

public class CfgProviderPointsFromObjects extends CfgProvider {

    // START BEAN PROPERTIES
    @BeanField @Getter @Setter private ObjectCollectionProvider objects;
    // END BEAN PROPERTIES

    @Override
    public Cfg create() throws CreateException {
        return createMarksFromObjects(objects.create());
    }

    private static Cfg createMarksFromObjects(ObjectCollection objectCollection) {

        Cfg out = new Cfg();

        for (int i = 0; i < objectCollection.size(); i++) {

            ObjectMask object = objectCollection.get(i);

            out.add(
                    MarkPointListFactory.create(
                            PointsFromObject.fromAsDouble(object),
                            i // We assign an unique integer ID
                            ));
        }
        return out;
    }
}
