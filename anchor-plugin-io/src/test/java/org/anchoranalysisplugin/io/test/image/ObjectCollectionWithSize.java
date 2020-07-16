/* (C)2020 */
package org.anchoranalysisplugin.io.test.image;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.anchoranalysis.image.object.ObjectCollection;

@AllArgsConstructor
class ObjectCollectionWithSize {

    @Getter private ObjectCollection objects;
    private long size;

    public double relativeSize(ObjectCollectionWithSize other) {
        return ((double) size) / other.size;
    }
}
