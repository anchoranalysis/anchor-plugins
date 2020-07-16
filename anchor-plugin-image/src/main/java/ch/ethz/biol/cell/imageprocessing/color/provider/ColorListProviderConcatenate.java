/* (C)2020 */
package ch.ethz.biol.cell.imageprocessing.color.provider;

import java.util.ArrayList;
import java.util.List;
import lombok.Getter;
import lombok.Setter;
import org.anchoranalysis.bean.annotation.BeanField;
import org.anchoranalysis.core.color.ColorList;
import org.anchoranalysis.core.error.CreateException;
import org.anchoranalysis.image.bean.provider.ColorListProvider;

public class ColorListProviderConcatenate extends ColorListProvider {

    // START BEAN PROPERTIES
    @BeanField @Getter @Setter private List<ColorListProvider> list = new ArrayList<>();
    // END BEAN PROPERTIES

    @Override
    public ColorList create() throws CreateException {

        ColorList out = new ColorList();

        for (ColorListProvider provider : list) {
            out.addAll(provider.create());
        }

        return out;
    }
}
