/* (C)2020 */
package ch.ethz.biol.cell.imageprocessing.histogram.provider;

import org.anchoranalysis.bean.annotation.BeanField;
import org.anchoranalysis.core.error.InitException;
import org.anchoranalysis.core.name.provider.NamedProviderGetException;
import org.anchoranalysis.image.bean.nonbean.init.ImageInitParams;
import org.anchoranalysis.image.bean.provider.HistogramProvider;
import org.anchoranalysis.image.histogram.Histogram;

public class HistogramProviderReference extends HistogramProvider {

    // START BEAN PROPERTIES
    @BeanField private String id = "";
    // END BEAN PROPERTIES

    private Histogram histogram;

    @Override
    public void onInit(ImageInitParams so) throws InitException {
        super.onInit(so);
        try {
            histogram = so.getHistogramCollection().getException(id);
        } catch (NamedProviderGetException e) {
            throw new InitException(e.summarize());
        }
    }

    @Override
    public Histogram create() {
        return histogram;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }
}
