package model;

import java.util.Optional;

public enum Network implements Interval{

    UserPorts(new Interval.Closed().withLowerBound(1024).withUpperBound(65535));

    private final Interval interval;

    Network(Interval interval) {
        this.interval = interval;
    }

    @Override
    public boolean contains(int value) {
        return interval.contains(value);
    }

    @Override
    public int clamp(int value) {
        return interval.clamp(value);
    }

    @Override
    public Interval withLowerBound(int lowerBound) {
        return interval.withLowerBound(lowerBound);
    }

    @Override
    public Interval withLowerBound(Optional<Integer> lowerBound) {
        return interval.withLowerBound(lowerBound);
    }

    @Override
    public Interval withUpperBound(int upperBound) {
        return interval.withUpperBound(upperBound);
    }

    @Override
    public Interval withUpperBound(Optional<Integer> upperBound) {
        return interval.withUpperBound(upperBound);
    }


    @Override
    public String toString() {
        return super.toString() + " " + interval.toString();
    }
}
