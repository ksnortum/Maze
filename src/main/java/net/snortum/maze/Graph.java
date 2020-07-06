package net.snortum.maze;

import java.util.Map;
import java.util.TreeMap;

/**
 * A Graph is a list (implemented as a Map) of {@link Node}s.  The key is
 * the node number and the value is the whole Node.  This makes it faster
 * to lookup Nodes.
 */
class Graph {
    private final Map<Integer, Node> nodes = new TreeMap<>();

    public Map<Integer, Node> getNodes() {
        return nodes;
    }

    public boolean isEmpty() {
        return nodes.size() == 0;
    }

    public void addNode(Node node) {
        nodes.put(node.getNodeNumber(), node);
    }

    public Node getNodeFromNumber(int nodeNumber){
        Node node = nodes.get(nodeNumber);

        return node == null ? new Node(nodeNumber) : node;
    }
}