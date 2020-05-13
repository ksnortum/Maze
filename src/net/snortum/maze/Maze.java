package net.snortum.maze;

import java.io.*;
import java.util.*;
import java.util.List;

public class Maze {
    private static final Scanner STDIN = new Scanner(System.in);
    private static final Random random = new Random();
    private static final int PATH = 0;
    private static final int WALL = 1;
    private static final int ESCAPE = 2;
    private static final int BAD_PATH = 3;
    private static final String PRINT_PATH = "  ";
    private static final String PRINT_WALL = "\u2588\u2588";
    private static final String PRINT_ESCAPE = "//";
    private static final String PRINT_BAD_PATH = PRINT_PATH;
    private static final String INVALID_MESSAGE = "Incorrect option. Please try again";

    private int height;
    private int width;
    private int innerHeight;
    private int innerWidth;
    private int[][] maze;
    private boolean mazeIsInMemory = false;
    private Point start;
    private Point end;

    public static void main(String[] args) {
        new Maze().run();
    }

    private void run() {
        int choice;

        do {
            displayMenu();
            choice = STDIN.nextInt();
            STDIN.nextLine(); // consume <enter>

            switch (choice) {
                case 0:
                    break;
                case 1:
                    generateMaze();
                    displayMaze();
                    break;
                case 2:
                    loadMaze();
                    break;
                case 3:
                    if (mazeIsInMemory) {
                        saveMaze();
                    } else {
                        System.out.println(INVALID_MESSAGE);
                    }
                    break;
                case 4:
                    if (mazeIsInMemory) {
                        displayMaze();
                    } else {
                        System.out.println(INVALID_MESSAGE);
                    }
                    break;
                case 5:
                    if (mazeIsInMemory) {
                        findEscape();
                    } else {
                        System.out.println(INVALID_MESSAGE);
                    }
                    break;
                default:
                    System.out.println(INVALID_MESSAGE);
            }
        } while (choice != 0);

        System.out.println("Bye!");
    }

    private void displayMenu() {
        System.out.println();
        System.out.println("=== Menu ===");
        System.out.println("1. Generate a new maze");
        System.out.println("2. Load a maze");

        if (mazeIsInMemory) {
            System.out.println("3. Save the maze");
            System.out.println("4. Display the maze");
            System.out.println("5. Find the escape");
        }

        System.out.println("0. Exit");
    }

    private void generateMaze() {
        Scanner scanner = new Scanner(System.in);
        System.out.println("An odd height and width will give you a better looking maze");
        System.out.println("Enter height of a new maze");
        height = scanner.nextInt();
        width = height;

        if (height < 5) {
            throw new IllegalArgumentException("Maze size cannot be less than five");
        }

        innerHeight = height % 2 == 1 ? height / 2 : height / 2 - 1;
        innerWidth = innerHeight;
        buildMaze();
    }

    private void buildMaze() {
        Graph graph = buildWeightedGraph();
        Graph tree = createMST(graph);
        replaceNonEdgesWithWalls(tree);
        addEntranceAndExit();
        mazeIsInMemory = true;
    }

    private void loadMaze() {
        String fileName = STDIN.nextLine();
        File mazeFile = new File(fileName);

        if (!mazeFile.exists()) {
            System.out.printf("The file %s does not exist%n", fileName);
            return;
        }

        try (
                FileInputStream fi = new FileInputStream(mazeFile);
                ObjectInputStream oi = new ObjectInputStream(fi);
        ) {
            maze = (int[][]) oi.readObject();
        } catch (FileNotFoundException e) {
            System.out.printf("Cannot load the maze. File %s cannot be found%n", fileName);
            return;
        } catch (IOException | ClassNotFoundException e) {
            System.out.println("Cannot load the maze. It has an invalid format");
            return;
        }

        mazeIsInMemory = true;
        setStartAndEndOfMaze();
        setHeightAndWidth();
    }

    private void setStartAndEndOfMaze() {
        for (int row = 0; row < maze.length; row++) {
            if (maze[row][0] == PATH) {
                start = new Point(row, 0);
            }

            if (maze[row][maze[row].length - 1] == PATH) {
                end = new Point(row, maze[row].length - 1);
            }
        }
    }

    private void setHeightAndWidth() {
        height = maze.length;

        if (height > 0) {
            width = maze[0].length;
        } else {
            System.out.println("Maze is empty");
        }
    }

    private void saveMaze() {
        if (!mazeIsInMemory) {
            System.out.println("No maze has been generated or loaded");
            return;
        }

        String fileName = STDIN.nextLine();
        File mazeFile = new File(fileName);

        try (
                FileOutputStream fileOutputStream = new FileOutputStream(mazeFile);
                ObjectOutputStream objectOutputStream = new ObjectOutputStream(fileOutputStream);
        ) {
            objectOutputStream.writeObject(maze);
        } catch (FileNotFoundException e) {
            System.out.printf("The file %s could not be found%n", fileName);
        } catch (IOException e) {
            System.out.printf("There was an IO exception writing %s%n", fileName);
        }
    }

    private void findEscape() {
        if (!mazeIsInMemory) {
            System.out.println("No maze has been generated or loaded");
            return;
        }

        findPath(start);
        markAsPartialSolution(end);
        displayMaze();
    }

    // Build weighted graph and helper methods

    private Graph buildWeightedGraph() {
        Graph graph = new Graph();

        for (int row = 0; row < innerHeight; row++) {
            for (int column = 0; column < innerWidth; column++) {
                Node node = new Node(calculateNumber(row, column));

                // North
                if (row - 1 >= 0) {
                    node.addEdge(createNewEdge(graph, node, calculateNumber(row - 1, column)));
                }

                // West
                if (column - 1 >= 0) {
                    node.addEdge(createNewEdge(graph, node, calculateNumber(row, column - 1)));
                }

                // South
                if (row + 1 < innerHeight) {
                    node.addEdge(createNewEdge(graph, node, calculateNumber(row + 1, column)));
                }

                // East
                if (column + 1 < innerWidth) {
                    node.addEdge(createNewEdge(graph, node, calculateNumber(row, column + 1)));
                }

                graph.addNode(node);
            }
        }

        return graph;
    }

    private int calculateNumber(int row, int column) {
        return row * (innerWidth) + column;
    }

    private Edge createNewEdge(Graph graph, Node currentNode, int newNodeNumber) {
        int weight = random.nextInt(2);
        int currentNodeNumber = currentNode.getNodeNumber();

        // If graph already contains this new node number...
        if (graph.getNodes().containsKey(newNodeNumber)) {
            Node newNode = graph.getNodes().get(newNodeNumber);

            // If this edge is already in another node, get its weight
            OptionalInt weightOpt = newNode.getEdgesWeight(currentNodeNumber);

            if (weightOpt.isPresent()) {
                weight = weightOpt.getAsInt();
            }
        }

        return new Edge(newNodeNumber, weight);
    }

    // Create a minimum spanning tree (MST) and helper methods

    private Graph createMST(Graph graph) {
        if (graph == null || graph.isEmpty()) {
            throw new IllegalArgumentException("Graph cannot be null or empty");
        }

        // Initializations
        Graph tree = new Graph();
        int firstNodeNumber = 0;
        Node nodeX = graph.getNodes().get(firstNodeNumber);
        List<FullEdge> candidates = nodeX.getFullEdges();
        Map<Integer, String> visited = new HashMap<>();
        visited.put(firstNodeNumber, null);

        while (!candidates.isEmpty()) {
            Collections.sort(candidates);
            FullEdge fullEdge = candidates.get(0);
            candidates.remove(fullEdge);
            int nodeNumberY = fullEdge.getEdge().getNodeNumber();

            // This edge doesn't cause a cycle
            if (!visited.containsKey(nodeNumberY)) {
                visited.put(nodeNumberY, null);
                tree = setTreeNodeEdges(tree, fullEdge);
                Node newNodeX = graph.getNodeFromNumber(nodeNumberY);
                candidates.addAll(newNodeX.getFullEdges());
            }
        }

        return tree;
    }

    /**
     * Since tree is a {@link Graph}, both the node number and the edge's node number
     * need to be updated.
     */
    private Graph setTreeNodeEdges(Graph tree, FullEdge fullEdge) {
        int weight = fullEdge.getEdge().getWeight();
        int nodeNumberX = fullEdge.getNodeNumber();
        int nodeNumberY = fullEdge.getEdge().getNodeNumber();
        Edge edgeY = new Edge(nodeNumberY, weight);
        Node nodeX = tree.getNodeFromNumber(nodeNumberX);
        nodeX.addEdge(edgeY);
        tree.addNode(nodeX);

        Edge edgeX = new Edge(nodeNumberX, weight);
        Node nodeY = tree.getNodeFromNumber(nodeNumberY);
        nodeY.addEdge(edgeX);
        tree.addNode(nodeY);

        return tree;
    }

    // Draw walls where there aren't edges
    private void replaceNonEdgesWithWalls(Graph tree) {
        if (tree == null || tree.isEmpty()) {
            throw new IllegalArgumentException("Tree cannot be null or empty");
        }

        maze = new int[height][width];

        // Fill maze with walls
        for (int row = 0; row < height; row++) {
            for (int column = 0; column < width; column++) {
                maze[row][column] = WALL;
            }
        }

        // The path is where the edges are in the MST
        for (Node xNode : tree.getNodes().values()) {
            int nodeNumber = xNode.getNodeNumber();
            int row = (nodeNumber / innerWidth) * 2 + 1;

            for (Edge yEdge : xNode.getEdges()) {
                int column = (nodeNumber % innerWidth) * 2 + 1;

                // Nodes are always a path
                maze[row][column] = PATH;

                // Edge is to the right
                if (yEdge.getNodeNumber() == nodeNumber + 1) {
                    maze[row][column + 1] = PATH;
                }

                // Edge is below
                if (yEdge.getNodeNumber() == nodeNumber + innerWidth) {
                    maze[row + 1][column] = PATH;
                }
            }
        }
    }

    private void addEntranceAndExit() {
        boolean entranceNotPlaced = true;
        boolean exitNotPlaced = true;
        int row = 0;

        while(entranceNotPlaced || exitNotPlaced) {

            // Find a left-hand entrance from top down
            if (entranceNotPlaced) {

                // If width is even, the path only needs to be one wall away
                if (width % 2 == 1 && maze[row][1] == PATH) {
                    maze[row][0] = PATH;
                    start = new Point(row, 0);
                    entranceNotPlaced = false;
                } else if (width % 2 == 0 && maze[row][2] == PATH) {
                    maze[row][0] = PATH;
                    maze[row][1] = PATH;
                    start = new Point(row, 0);
                    entranceNotPlaced = false;
                }
            }

            // Find a right-hand entrance from bottom up
            if (exitNotPlaced) {

                // If width is even, the path only needs to be one wall away
                if (width % 2 == 1 && maze[height - row - 2][width - 2] == PATH) {
                    maze[height - row - 2][width - 1] = PATH;
                    end = new Point(height - row - 2, width - 1);
                    exitNotPlaced = false;
                } else if (width % 2 == 0 && maze[height - row - 2][width - 3] == PATH) {
                    maze[height - row - 2][width - 1] = PATH;
                    maze[height - row - 2][width - 2] = PATH;
                    end = new Point(height - row - 2, width - 1);
                    exitNotPlaced = false;
                }
            }

            row++;

            if (row > height - 1) {
                throw new ArrayIndexOutOfBoundsException("Didn't find an entrance or exit");
            }
        }
    }

    private void displayMaze() {
        if (!mazeIsInMemory) {
            System.out.println("No maze has been generated or loaded");
            return;
        }

        for (int[] ints : maze) {
            for (int code : ints) {
                if (code == PATH) {
                    System.out.print(PRINT_PATH);
                } else if (code == WALL){
                    System.out.print(PRINT_WALL);
                } else if (code == ESCAPE) {
                    System.out.print(PRINT_ESCAPE);
                } else if (code == BAD_PATH) {
                    System.out.print(PRINT_BAD_PATH);
                }
            }

            System.out.println();
        }
    }

    private boolean findPath(Point point) {
        if (outsideMaze(point) || notOpenPath(point)) { return false; }
        if (foundGoal(point)) { return true; }
        markAsPartialSolution(point);
        if (findPath(point.moveNorth())) { return true; }
        if (findPath(point.moveEast())) { return true; }
        if (findPath(point.moveSouth())) { return true; }
        if (findPath(point.moveWest())) { return true; }
        unmarkAsPartialSolution(point);

        return false;
    }

    private boolean outsideMaze(Point point) {
        return point.getRow() < 0 || point.getRow() >= height || point.getColumn() < 0 || point.getColumn() >= width;
    }

    private boolean notOpenPath(Point point) {
        return maze[point.getRow()][point.getColumn()] != PATH;
    }

    private boolean foundGoal(Point point) {
        return point.getRow() == end.getRow() && point.getColumn() == end.getColumn();
    }

    private void markAsPartialSolution(Point point) {
        maze[point.getRow()][point.getColumn()] = ESCAPE;
    }

    private void unmarkAsPartialSolution(Point point) {
        maze[point.getRow()][point.getColumn()] = BAD_PATH;
    }
}