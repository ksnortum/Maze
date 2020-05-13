package net.snortum.maze;

class Point {
    final int row;
    final int column;

    public Point(int row, int column) {
        this.row = row;
        this.column = column;
    }

    public int getRow() {
        return row;
    }

    public int getColumn() {
        return column;
    }

    public Point moveNorth() {
        return new Point(row - 1, column);
    }

    public Point moveEast() {
        return new Point(row, column + 1);
    }

    public Point moveSouth() {
        return new Point(row + 1, column);
    }

    public Point moveWest() {
        return new Point(row, column - 1);
    }

    @Override
    public String toString() {
        return String.format("row = %d, column = %d", row, column);
    }
}