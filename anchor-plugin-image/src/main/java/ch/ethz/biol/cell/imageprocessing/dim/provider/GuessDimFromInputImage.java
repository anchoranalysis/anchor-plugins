/* (C)2020 */
package ch.ethz.biol.cell.imageprocessing.dim.provider;

import java.util.Set;
import org.anchoranalysis.core.error.CreateException;
import org.anchoranalysis.core.name.provider.NamedProviderGetException;
import org.anchoranalysis.core.name.store.NamedProviderStore;
import org.anchoranalysis.image.bean.provider.ImageDimProvider;
import org.anchoranalysis.image.channel.Channel;
import org.anchoranalysis.image.experiment.identifiers.ImgStackIdentifiers;
import org.anchoranalysis.image.extent.ImageDimensions;
import org.anchoranalysis.image.stack.Stack;

/**
 * Guesses dimensions from the input-image if it exists.
 *
 * <p>Otherwise throws an exception indicating they should be set explicitly.
 *
 * @author Owen Feehan
 */
public class GuessDimFromInputImage extends ImageDimProvider {

    private ImageDimensions dimensions;

    @Override
    public ImageDimensions create() throws CreateException {

        if (dimensions == null) {
            dimensions =
                    takeDimFromStackCollection(getInitializationParameters().getStackCollection());
        }

        return dimensions;
    }

    private ImageDimensions takeDimFromStackCollection(NamedProviderStore<Stack> stackCollection)
            throws CreateException {

        Set<String> keys = stackCollection.keys();

        if (!keys.contains(ImgStackIdentifiers.INPUT_IMAGE)) {
            throw new CreateException(
                    String.format(
                            "No input-image (%s) exists so cannot guess Image Dimensions. Please set the dimensions explicitly.",
                            ImgStackIdentifiers.INPUT_IMAGE));
        }

        return takeDimFromSpecificStack(stackCollection, ImgStackIdentifiers.INPUT_IMAGE);
    }

    /** Takes the ImageDim from a particular stack in the collection */
    private ImageDimensions takeDimFromSpecificStack(
            NamedProviderStore<Stack> stackCollection, String keyThatExists)
            throws CreateException {
        Stack stack;
        try {
            stack = getInitializationParameters().getStackCollection().getException(keyThatExists);
        } catch (NamedProviderGetException e) {
            throw new CreateException(e);
        }

        Channel chnl = stack.getChnl(0);
        if (chnl == null) {
            throw new CreateException(
                    String.format(
                            "Stack %s has no channels, so dimensions cannot be inferred.",
                            keyThatExists));
        }
        return chnl.getDimensions();
    }
}
