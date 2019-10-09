package model;

import java.util.Optional;

public interface Interval {

    Optional<Integer> Infinity = Optional.empty(), NegativeInfinity = Optional.empty();

    boolean contains(int value);
    int clamp(int value);

    Interval withLowerBound(int lowerBound);
    Interval withLowerBound(Optional<Integer> lowerBound);
    Interval withUpperBound(int lowerBound);
    Interval withUpperBound(Optional<Integer> lowerBound);

    final class Closed implements Interval {

        private final Optional<Integer> lowerBound, upperBound;

        public Closed() {
            this.lowerBound = NegativeInfinity;
            this.upperBound = Infinity;
        }

        public Closed(Optional<Integer> lowerBound, Optional<Integer> upperBound) {
            this.lowerBound = lowerBound;
            this.upperBound = upperBound;
        }

        public final boolean contains(int value) {
            return lowerBound.flatMap(min -> Optional.of(value >= min)).orElse(true)
                && upperBound.flatMap(max -> Optional.of(value <= max)).orElse(true);
        }

        public final int clamp(int value) {

            int least = lowerBound.flatMap(min
                    -> Optional.of(Math.max(min, value))).orElse(value);

            return upperBound.flatMap(max
                    -> Optional.of(Math.min(max, least))).orElse(value);
        }

        public final Interval withLowerBound(int lowerBound) {
            return new Closed(Optional.of(lowerBound), upperBound);
        }

        public final Interval withLowerBound(Optional<Integer> lowerBound) {
            return new Closed(lowerBound, upperBound);
        }

        public final Interval withUpperBound(int upperBound) {
            return new Closed(lowerBound, Optional.of(upperBound));
        }

        public final Interval withUpperBound(Optional<Integer> upperBound) {
            return new Closed(lowerBound, upperBound);
        }

        @Override
        public String toString() {
            return String.format("[%s, %s]",
                    lowerBound.flatMap(min -> Optional.of(String.valueOf(min))).orElse("-∞"),
                    upperBound.flatMap(max -> Optional.of(String.valueOf(max))).orElse("∞"));
        }
    }
}
