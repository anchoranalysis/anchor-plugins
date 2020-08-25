package org.anchoranalysis.plugin.image.provider;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

/**
 * Creates reference (i.e. Providers that reference an existing object) of different types
 *
 * <p>This is particularly useful to avoid to referring to multiple identically named {@code
 * Reference} classes in the same file, necessitating the use of the fully qualified class name.
 *
 * @author Owen Feehan
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class ReferenceFactory {

    public static org.anchoranalysis.plugin.image.bean.stack.provider.Reference stack(String id) {
        return new org.anchoranalysis.plugin.image.bean.stack.provider.Reference(id);
    }

    public static org.anchoranalysis.plugin.image.bean.channel.provider.Reference channel(
            String id) {
        return new org.anchoranalysis.plugin.image.bean.channel.provider.Reference(id);
    }

    public static org.anchoranalysis.plugin.image.bean.object.provider.Reference objects(
            String id) {
        return new org.anchoranalysis.plugin.image.bean.object.provider.Reference(id);
    }

    public static org.anchoranalysis.plugin.image.bean.mask.provider.Reference mask(String id) {
        return new org.anchoranalysis.plugin.image.bean.mask.provider.Reference(id);
    }

    public static org.anchoranalysis.plugin.image.bean.histogram.provider.Reference histogram(
            String id) {
        return new org.anchoranalysis.plugin.image.bean.histogram.provider.Reference(id);
    }
}
