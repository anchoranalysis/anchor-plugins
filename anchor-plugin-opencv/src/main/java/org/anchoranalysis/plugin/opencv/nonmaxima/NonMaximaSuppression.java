/* (C)2020 */
package org.anchoranalysis.plugin.opencv.nonmaxima;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.PriorityQueue;
import java.util.function.Predicate;

/**
 * Non-maxima suppression, that keeps highest-confidence bounding boxes removing overlapping boxes.
 *
 * <ol>
 *   <li>Select highest-confidence proposal, remove from list, add to output
 *   <li>Compare this proposal with remainder, and remove any remainder with a similarity score
 *       above a threshold
 *   <li>If there are remaining proposals in the queue, goto Step 1
 * </ol>
 *
 * @param T the type-of-object to which the algorithm applies
 * @author Owen Feehan
 */
public abstract class NonMaximaSuppression<T> {

    /**
     * Reduce a set of bounding-boxes (each with a confidence score) to a set with minimal overlap.
     *
     * <p>See the class description for details of algorithm
     *
     * @param proposals proposed bounding-boxes with scores
     * @param overlapThreshold after a proposal is accepted, other proposals with overlap
     *     greater-equal than this threshold are removed
     * @return accepted proposals
     */
    public List<WithConfidence<T>> apply(
            Collection<WithConfidence<T>> proposals, double overlapThreshold) {
        init(proposals);

        PriorityQueue<WithConfidence<T>> pq = new PriorityQueue<>(proposals);

        List<WithConfidence<T>> out = new ArrayList<>();

        while (!pq.isEmpty()) {

            WithConfidence<T> highestConfidence = pq.poll();

            removeOverlapAboveScore(highestConfidence, pq, overlapThreshold);

            out.add(highestConfidence);
        }

        return out;
    }

    /** Called before the operation begins with all proposals */
    protected abstract void init(Collection<WithConfidence<T>> allProposals);

    /** A score calculating the overlap between two items */
    protected abstract double overlapScoreFor(T item1, T item2);

    /** Finds possible neighbors for a particular object efficiently */
    protected abstract Predicate<T> possibleOverlappingObjects(
            T src, Iterable<WithConfidence<T>> others);

    private void removeOverlapAboveScore(
            WithConfidence<T> proposal,
            PriorityQueue<WithConfidence<T>> others,
            double scoreThreshold) {

        Predicate<T> pred = possibleOverlappingObjects(proposal.getObject(), others);

        Iterator<WithConfidence<T>> othersItr = others.iterator();
        while (othersItr.hasNext()) {

            T other = othersItr.next().getObject();

            if (pred.test(other)) {

                double score = overlapScoreFor(proposal.getObject(), other);

                // These objects are deemed highly-similar and removed
                if (score >= scoreThreshold) {
                    // Remove from the queue if above the threshold
                    othersItr.remove();
                }
            }
        }
    }
}
