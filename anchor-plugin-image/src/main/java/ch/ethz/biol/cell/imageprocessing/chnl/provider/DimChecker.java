/* (C)2020 */
package ch.ethz.biol.cell.imageprocessing.chnl.provider;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.anchoranalysis.core.error.CreateException;
import org.anchoranalysis.image.bean.provider.BinaryChnlProvider;
import org.anchoranalysis.image.bean.provider.ChnlProvider;
import org.anchoranalysis.image.binary.mask.Mask;
import org.anchoranalysis.image.channel.Channel;
import org.anchoranalysis.image.extent.ImageDimensions;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class DimChecker {

    public static void check(Channel chnl, Mask mask) throws CreateException {
        if (!chnl.getDimensions().equals(mask.getDimensions())) {
            throw new CreateException(
                    String.format(
                            "chnl (%s) and mask (%s) must have the same dimensions",
                            chnl.getDimensions().toString(), mask.getDimensions().toString()));
        }
    }

    /**
     * Checks a channel to make sure it's the same size as an an existing channel
     *
     * @param chnlToCheck the channel whose size will be compared
     * @param chnlToCheckName a user-meaningful string to identify the chnlToCheck in error messages
     * @param dimFromChnl the dimensions it must equal from chnl (identified as chnl in error
     *     messages)
     * @return the newly created channel
     * @throws CreateException
     */
    public static void check(
            Channel chnlToCheck, String chnlToCheckName, ImageDimensions dimFromChnl)
            throws CreateException {
        check(chnlToCheck.getDimensions(), chnlToCheckName, dimFromChnl);
    }

    /**
     * Checks a channel to make sure it's the same size as an an existing channel
     *
     * @param chnlToCheck the channel whose size will be compared
     * @param chnlToCheckName a user-meaningful string to identify the chnlToCheck in error messages
     * @param dimFromChnl the dimensions it must equal from chnl (identified as chnl in error
     *     messages)
     * @return the newly created channel
     * @throws CreateException
     */
    public static void check(Mask chnlToCheck, String chnlToCheckName, ImageDimensions dimFromChnl)
            throws CreateException {
        check(chnlToCheck.getDimensions(), chnlToCheckName, dimFromChnl);
    }

    /**
     * Creates a new channel from a provider, making sure it's the same size as an an existing
     * channel
     *
     * @param provider the provider to to create the channel
     * @param providerName a user-meaningful string to identify the provider in error messages
     * @param chnlSameSize the channel which it must be the same size as (referred to in error
     *     messages as "chnl")
     * @return the newly created channel
     * @throws CreateException
     */
    public static Channel createSameSize(
            ChnlProvider provider, String providerName, Channel chnlSameSize)
            throws CreateException {

        Channel chnlNew = provider.create();
        check(chnlNew, providerName, chnlSameSize.getDimensions());
        return chnlNew;
    }

    /**
     * Creates a new channel from a provider, making sure it's the same size as an an existing
     * channel
     *
     * @param provider the provider to to create the channel
     * @param providerName a user-meaningful string to identify the provider in error messages
     * @param chnlSameSize the channel which it must be the same size as (referred to in error
     *     messages as "chnl")
     * @return the newly created channel
     * @throws CreateException
     */
    public static Mask createSameSize(
            BinaryChnlProvider provider, String providerName, Channel chnlSameSize)
            throws CreateException {

        Mask chnlNew = provider.create();
        check(chnlNew, providerName, chnlSameSize.getDimensions());
        return chnlNew;
    }

    private static void check(
            ImageDimensions dimToCheck, String chnlToCheckName, ImageDimensions dimFromChnl)
            throws CreateException {
        if (!dimFromChnl.equals(dimToCheck)) {
            throw new CreateException(
                    String.format(
                            "chnl (%s) and %s (%s) must have the same dimensions",
                            dimFromChnl.toString(), chnlToCheckName, dimToCheck.toString()));
        }
    }
}
