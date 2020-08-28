package org.anchoranalysis.plugin.image.feature.object;

import java.util.List;
import java.util.Optional;
import org.anchoranalysis.plugin.image.thumbnail.ThumbnailBatch;
import lombok.Getter;

/**
 * A list of items with an associated thumbnail-batch
 * 
 * @author Owen Feehan
 * @param <T> list-type
 * @param <S> thumbnail input type
 */
public class ListWithThumbnails<T,S> {

    private final List<T> list;
    @Getter private final Optional<ThumbnailBatch<S>> thumbnailBatch;
    
    public ListWithThumbnails(List<T> list) {
        this.list = list;
        this.thumbnailBatch = Optional.empty();
    }
    
    public ListWithThumbnails(List<T> list, ThumbnailBatch<S> thumbnailBatch) {
        this.list = list;
        this.thumbnailBatch = Optional.of(thumbnailBatch);
    }

    public T get(int arg0) {
        return list.get(arg0);
    }

    public int size() {
        return list.size();
    }
}
