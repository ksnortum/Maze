package net.snortum.maze;

class FullEdge implements Comparable<FullEdge> {
    private final int nodeNumber;
    private final Edge edge;

    public FullEdge(int nodeNumber, Edge edge) {
        this.nodeNumber = nodeNumber;
        this.edge = edge;
    }

    public int getNodeNumber() {
        return nodeNumber;
    }

    public Edge getEdge() {
        return edge;
    }

    public int getWeight() {
        return edge.getWeight();
    }

    @Override
    public int compareTo(FullEdge other) {
        int compareWeight = edge.getWeight() - other.edge.getWeight();

        if (compareWeight != 0) {
            return compareWeight;
        }

        int compareNodeX = nodeNumber - other.nodeNumber;

        if (compareNodeX != 0) {
            return compareNodeX;
        }

        return edge.getNodeNumber() - other.edge.getNodeNumber();
    }

    @Override
    public String toString() {
        return String.format("x:%d, y:%d, w:%d;", nodeNumber, edge.getNodeNumber(), edge.getWeight());
    }
}