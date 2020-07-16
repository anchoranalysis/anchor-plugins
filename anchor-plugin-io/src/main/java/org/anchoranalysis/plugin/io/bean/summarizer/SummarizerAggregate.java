/* (C)2020 */
package org.anchoranalysis.plugin.io.bean.summarizer;

import java.util.List;
import org.anchoranalysis.bean.annotation.BeanField;
import org.anchoranalysis.core.error.OperationFailedException;

/**
 * Collects summary data about a set of input files that will eventually be outputted to the user
 */
public class SummarizerAggregate<T> extends Summarizer<T> {

    private static final String BULLET_POINT = "-> ";

    // START BEAN PROPERTIES
    @BeanField private List<Summarizer<T>> list;

    /** Iff TRUE no bullet is added for the very first-item in the list */
    @BeanField private boolean avoidBulletOnFirst = false;
    // END BEAN PROPERTIES

    public void add(T element) throws OperationFailedException {

        for (Summarizer<T> summarizer : list) {
            summarizer.add(element);
        }
    }

    @Override
    public String describe() throws OperationFailedException {
        StringBuilder sb = new StringBuilder();

        for (int i = 0; i < list.size(); i++) {

            if (i != 0) {
                sb.append(System.lineSeparator());
            }

            if (!(avoidBulletOnFirst && i == 0)) {
                sb.append(BULLET_POINT);
            }

            Summarizer<T> element = list.get(i);
            sb.append(element.describe());
        }

        return sb.toString();
    }

    public List<Summarizer<T>> getList() {
        return list;
    }

    public void setList(List<Summarizer<T>> list) {
        this.list = list;
    }

    public boolean isAvoidBulletOnFirst() {
        return avoidBulletOnFirst;
    }

    public void setAvoidBulletOnFirst(boolean avoidBulletOnFirst) {
        this.avoidBulletOnFirst = avoidBulletOnFirst;
    }
}
