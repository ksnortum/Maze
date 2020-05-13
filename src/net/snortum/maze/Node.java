package net.snortum.maze;

import java.util.*;
import java.util.stream.Collectors;

/**
 * <p>A Node is a node number and a list of {@link Edge}s (implemented as a Map).
 * A complete edge is the node's number and one Edge's node number and weight</p>
 *
 * The Map of Edges has a key of the Edge's node number and the entire Edge as
 * the value.  This makes it faster to lookup the Edge.
 */
class Node {
    private final int nodeNumber;
    private final Map<Integer, Edge> edges = new HashMap<>();

    public Node(int nodeNumber) {
        this.nodeNumber = nodeNumber;
    }

    public int getNodeNumber() {
        return nodeNumber;
    }

    public Set<Edge> getEdges() {
        return new HashSet<>(edges.values());
    }

    public List<FullEdge> getFullEdges() {
        return edges
                .values()
                .stream()
                .map(edge -> new FullEdge(nodeNumber, edge))
                .collect(Collectors.toList());
    }

    public void addEdge(Edge edge) {
        edges.put(edge.getNodeNumber(), edge);
    }

    public OptionalInt getEdgesWeight(int nodeNumber) {
        if (edges.containsKey(nodeNumber)) {
            return OptionalInt.of(edges.get(nodeNumber).getWeight());
        }

        return OptionalInt.empty();
    }

    @SuppressWarnings("unused")
    public Edge findEdgeFromNodeNumber(int nodeNumber) {
        return edges.get(nodeNumber);
    }
}
