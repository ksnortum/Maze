package net.snortum.maze;

/**
 * An Edge holds a node number and a weight.  The other node is determined by
 * the List of Edges in the {@link Node} class.  Therefore a complete edge is described
 * by the Node's number and the Edge's node number.
 */
class Edge {
    private final int nodeNumber;
    private final int weight;

    public Edge(int nodeNumber, int weight) {
        this.nodeNumber = nodeNumber;
        this.weight = weight;
    }

    public int getNodeNumber() {
        return nodeNumber;
    }

    public int getWeight() {
        return weight;
    }

    @Override
    public String toString() {
        return String.format("n:%d, w:%d;", nodeNumber, weight);
    }
}
