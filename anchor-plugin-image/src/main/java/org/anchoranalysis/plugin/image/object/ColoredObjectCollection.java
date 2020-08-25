package org.anchoranalysis.plugin.image.object;

import com.google.common.base.Preconditions;
import lombok.Getter;
import org.anchoranalysis.bean.OptionalFactory;
import org.anchoranalysis.core.color.ColorList;
import org.anchoranalysis.core.color.RGBColor;
import org.anchoranalysis.core.error.CreateException;
import org.anchoranalysis.core.error.OperationFailedException;
import org.anchoranalysis.image.bean.provider.ObjectCollectionProvider;
import org.anchoranalysis.image.object.ObjectCollection;
import org.anchoranalysis.image.object.ObjectCollectionFactory;
import org.anchoranalysis.image.object.ObjectMask;

/**
 * Associates a {@link ColorList} with a {@link ObjectCollection}
 *
 * <p>Operations ensure both maintain an identical number of objects
 *
 * @author Owen Feehan
 */
public class ColoredObjectCollection {

    /**
     * The objects in the collection. This will always have the same number of items as {@code
     * colors}.
     */
    @Getter private final ObjectCollection objects;

    /**
     * The colors in the collection. This will always have the same number of items as {@code
     * objects}.
     */
    @Getter private final ColorList colors;

    /** Constructor - create empty collection */
    public ColoredObjectCollection() {
        this.objects = new ObjectCollection();
        this.colors = new ColorList();
    }

    /**
     * Constructor - create for a single object and color
     *
     * @param object the object
     * @param color the color
     */
    public ColoredObjectCollection(ObjectMask object, RGBColor color) {
        this.objects = ObjectCollectionFactory.of(object);
        this.colors = new ColorList(color);
    }

    /**
     * Constructor - create for an existing object and color list
     *
     * <p>Both arguments are reused internally as data-structures.
     *
     * @param objects the object
     * @param colors the colors, which must have the same number of items as {@code objects}
     */
    public ColoredObjectCollection(ObjectCollection objects, ColorList colors) {
        Preconditions.checkArgument(objects.size() == colors.size());
        this.objects = objects;
        this.colors = colors;
    }

    /**
     * Adds objects from a {@link ObjectCollectionProvider} all with one specific color
     *
     * @param provider provides the objects
     * @param color the color
     * @throws OperationFailedException if the provider cannot create the objects
     */
    public void addObjectsWithColor(ObjectCollectionProvider provider, RGBColor color)
            throws OperationFailedException {

        try {
            // If objects were created, add some corresponding colors
            OptionalFactory.create(provider)
                    .ifPresent(
                            objectsCreated -> {
                                objects.addAll(objectsCreated);
                                colors.addMultiple(color, objectsCreated.size());
                            });
        } catch (CreateException e) {
            throw new OperationFailedException(e);
        }
    }
}
