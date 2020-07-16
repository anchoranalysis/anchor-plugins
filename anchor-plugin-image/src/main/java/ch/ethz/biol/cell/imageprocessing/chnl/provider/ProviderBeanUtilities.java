/* (C)2020 */
package ch.ethz.biol.cell.imageprocessing.chnl.provider;

import java.util.ArrayList;
import java.util.List;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.anchoranalysis.bean.Provider;
import org.anchoranalysis.core.error.CreateException;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
class ProviderBeanUtilities {

    /**
     * Creates a list of ProviderType from Beans
     *
     * @param listIn input-list
     * @return a newly-created list containing the newly created items
     * @throws CreateException if a provider fails to create
     */
    public static <T> List<T> listFromBeans(List<? extends Provider<T>> listIn)
            throws CreateException {
        List<T> listOut = new ArrayList<>();
        addFromBeanList(listIn, listOut);
        return listOut;
    }

    /**
     * Creates items from a list of providers, and adds them to an output-list
     *
     * @param listIn input-list
     * @param listOut output-list
     * @throws CreateException if a provider fails to create
     */
    private static <T> void addFromBeanList(List<? extends Provider<T>> listIn, List<T> listOut)
            throws CreateException {
        for (Provider<T> provider : listIn) {
            listOut.add(provider.create());
        }
    }
}
