import java.awt.Color;
import java.awt.event.KeyEvent;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Scanner;

/**
 * Simple maze solver.  Can generate a random maze (possibly solvable) or
 * load a maze from a file, determine if it's solvable, and if so, find the
 * shortest path to the exit.
 * <p>
 * Maze text file format: First line has 2 ints, separated by a space, for the maze
 * dimensions (rows cols).
 * The remaining lines in the file have the maze rooms, using the values in the
 * constants EMPTY, WALL, START, and EXIT.  Example below:
 * <pre>
 *   3 5
 *   S...*
 *   ***.*
 *   ....E
 * </pre>
 * 
 * @author Mr. Jacoby
 * @version 3/15/19
 */
public class SimpleMaze {
  // CONSTANTS
  /** Empty room in maze. */
  public static final char EMPTY = '.';
  /** Wall in maze. */
  public static final char WALL = '*';
  /** Start of maze. */
  public static final char START = 'S';
  /** End of maze. */
  public static final char EXIT = 'E';
  
  /** In randomly generated maze, what percent of the rooms should be walls. */
  public static final double PERCENT_WALLS = 0.25;
  
  // FIELDS
  /**
   * Draw window for displaying maze.
   */
  private final Draw window;
  
  /**
   * "rooms" in maze must have one of the values listed above: EMPTY, WALL, START, or
   * EXIT.
   */
  private final char[][] rooms;

  /**
   * Tracks which rooms have been visited.
   */
  private boolean[][] visited;

  /**
   * Tracks shortest path to exit from corresponding room (-1 means no path possible).
   * null until updateDistances() called.
   */
  private int[][] distances;

  // CONSTRUCTORS
  /** Creates new random maze with given dimensions and percent walls. */
  public SimpleMaze(int rows, int cols, double percentWalls) {
    window = new Draw(String.format("Random Maze: %dx%d %2.0f%%", rows, cols,
          (percentWalls*100)));
    rooms = new char[rows][cols];
    makeRandomMaze(percentWalls);
  } // end SimpleMaze(rows, cols)

  /**
   * Creates new random maze with room's current dimensions and given percent walls,
   * then draws the maze. 
   */
  private void makeRandomMaze(double percentWalls) {
    // Fill all rooms as empty
    for (int r = 0; r < rooms.length; r++) {
      for (int c = 0; c < rooms[0].length; c++) {
        rooms[r][c] = EMPTY;
      }
    }
    // Randomly add some walls
    for (int r = 0; r < rooms.length; r++) {
      for (int c = 0; c < rooms[0].length; c++) {
        if (Math.random() < percentWalls) {
          rooms[r][c] = WALL;
        }
      }
    }
    // Add start and exit
    rooms[getRows()-1][0] = START; // bottom left
    rooms[0][getCols()-1] = EXIT; // top right
    
    // Initialize visited and distances
    visited = new boolean[getRows()][getCols()];
    distances = null;
    
    draw(); 
  } // makeRandomMaze(percentWalls)

  /** Loads maze from given text file. */
  public SimpleMaze(String filename) throws IOException {
    Scanner in = new Scanner(new File(filename));
    int rows = in.nextInt();
    int cols = in.nextInt();
    window = new Draw(filename + ": " + rows + "x" + cols);
    in.nextLine();
    rooms = new char[rows][cols];
    int row = 0;
    while (in.hasNextLine()) {
      String line = in.nextLine();
      for (int col = 0; col < line.length(); col++) {
        rooms[row][col] = line.charAt(col);
      }
      row++;
    }
    // TODO: throw IllegalStateException if no START or EXIT found?
    in.close();
    // Note: For a more robust, cleaner solution, check out:
    // Succinct: https://www.baeldung.com/java-try-with-resources
    // Official: https://docs.oracle.com/javase/tutorial/essential/exceptions/tryResourceClose.html
    // Comprehensive: https://dzone.com/articles/java-code-bytes-be-resourceful-with-try-with-resou

    visited = new boolean[rows][cols];
    distances = null;
  } // end SimpleMaze(filename)

  public int getRows() {
    return rooms.length;
  }

  public int getCols() {
    return rooms[0].length;
  }

  /**
   * Returns true if maze can be solved from given position in the maze.
   * Note: Given position is not guaranteed to be valid (if invalid, isSolvable()
   * must return false).
   * Note: Method is made superfluous by updateDistances, since maze is only
   * solvable if distance from START is >= 0.  Method kept as example of
   * simpler solution, though it doesn't lead to shortest path.
   */
  public boolean isSolvable(int row, int col) {
    // Return false if location invalid
    if (row < 0 || row >= getRows() || col < 0 || col >= getCols()) { return false; }
    
    char room = rooms[row][col];
    if (room == WALL || visited[row][col]) { return false; }
    if (room == EXIT) { return true; }
    visited[row][col] = true;
    
    // Recursively search for solution up, right, down, and left
    return isSolvable(row-1, col) || isSolvable(row, col+1) ||
           isSolvable(row+1, col) || isSolvable(row, col-1);
  }
  
  /**
   * Returns true if maze can be solved from one of its START positions.
   * Convenience method so you don't need to always worry about the starting location
   * when calling isSolvable(row, col).
   */
  public boolean solve() {
    // Searches through rooms, looking for a START.  If found, will try solving
    // maze from that location.  If not solvable from that location, keeps trying.
    boolean solvable = false;
    for (int r = 0; r < getRows(); r++) {
      for (int c = 0; c < getCols(); c++) {
        if (rooms[r][c] == START) {
          if (isSolvable(r, c)) {
            solvable = true;
            break;
          }
        }
      }
    }
    updateDistances();
    draw();
    System.out.println("Shortest path: " + getShortestPath());
    return solvable;
  }

  /**
   * Figures out the shortest distance from each room to the exit.  Stores
   * results in distances field.
   */
  public void updateDistances() {
    distances = new int[getRows()][getCols()];
    // Searches through rooms, looking for an EXIT.  If found, will updated
    // distances array with each room's distance from that exit.
    for (int row = 0; row < getRows(); row++) {
      for (int col = 0; col < getCols(); col++) {
        if (rooms[row][col] == EXIT) {
          updateDistances(row-1, col, 1); // up
          updateDistances(row, col+1, 1); // right
          updateDistances(row+1, col, 1); // down
          updateDistances(row, col-1, 1); // left
        }
      }
    }
    // Now make unreachable rooms distance -1
    for (int row = 0; row < getRows(); row++) {
      for (int col = 0; col < getCols(); col++) {
        if (rooms[row][col] != EXIT && distances[row][col] == 0) {
          distances[row][col] = -1;
        }
      }
    }
  }

  /**
   * If row/col is a valid room with current distance not set or greater
   * than given distance, set its distance to the given one and update its
   * four neighbors recursively with given distance + 1.
   */
  private void updateDistances(int row, int col, int dist) {
    // Return if location invalid
    if (row < 0 || row >= getRows() || col < 0 || col >= getCols()) { return; }

    char room = rooms[row][col];
    if (room == EXIT) { return; } // distance stays 0
    if (room == WALL) { distances[row][col] = -1; return; }
    
    // If distance currently 0 or greater than dist, update dist, then update neighbors
    if (distances[row][col] == 0 || distances[row][col] > dist) {
      distances[row][col] = dist;
      updateDistances(row-1, col, dist+1); // up
      updateDistances(row, col+1, dist+1); // right
      updateDistances(row+1, col, dist+1); // down
      updateDistances(row, col-1, dist+1); // left
    }
  }

  /**
   * Returns list of directions from the start to the exit, or empty list if
   * not solvable from start.
   */
  public List<Direction> getShortestPath() {
    if (distances == null) { updateDistances(); }
    int[] startCoordinates = getStartCoordinates();
    int r = startCoordinates[0], c = startCoordinates[1];
    if (distances[r][c] == -1) { // No path to exit!
      return Collections.emptyList();
    }
    List<Direction> path = new ArrayList<>(distances[r][c]);
    while (rooms[r][c] != EXIT) {
      Direction dir = findDirToMinNeighbor(r, c);
      path.add(dir);
      switch (dir) {
        case UP:    r--; break;
        case RIGHT: c++; break;
        case DOWN:  r++; break;
        case LEFT:  c--; break;
      }
    }
    drawPath(startCoordinates[0], startCoordinates[1], path);
    return path;
  } // getShortestPath

  /** Returns direction neighbor with shortest path to exit. */
  private Direction findDirToMinNeighbor(int row, int col) {
    int min = Integer.MAX_VALUE;
    Direction minDir = Direction.UP;
    // UP
    if (row > 0) {
      int dist = distances[row-1][col];
      if (dist >= 0) {
        min = dist;
        minDir = Direction.UP;
      }
    }
    // RIGHT
    if (col < getCols()-1) {
      int dist = distances[row][col+1];
      if (dist >= 0 && dist < min) {
        min = dist;
        minDir = Direction.RIGHT;
      }
    }
    // DOWN
    if (row < getRows()-1) {
      int dist = distances[row+1][col];
      if (dist >= 0 && dist < min) {
        min = dist;
        minDir = Direction.DOWN;
      }
    }
    // LEFT
    if (col > 0) {
      int dist = distances[row][col-1];
      if (dist >= 0 && dist < min) {
        min = dist;
        minDir = Direction.LEFT;
      }
    }
    
    return minDir;
  } // end findDirToMinNeighbor

  /** Returns {row, col} of first START room found. */
  private int[] getStartCoordinates() {
    for (int r = 0; r < rooms.length; r++) {
      for (int c = 0; c < rooms[r].length; c++) {
        if (rooms[r][c] == START) {
          return new int[] {r, c};
        }
      }
    }
    print();
    throw new IllegalStateException("No START room found for maze.");
  }

  /** Prints maze to System.out for debugging. */
  public void print() {
    for (int r = 0; r < rooms.length; r++) {
      for (int c = 0; c < rooms.length; c++) {
        char room = rooms[r][c];
        if (room != START && visited[r][c]) {
          room = 'o';
        }
        System.out.print(room);
      }
      System.out.println();
    }
  }

  /** Draws maze to its window. */
  public void draw() {
    window.clear();
    setScale();
    for (int r = 0; r < getRows(); r++) {
      for (int c = 0; c < getCols(); c++) {
        double xCtr = c + 0.5;
        double yCtr = r + 0.5;
        Color color;
        switch (rooms[r][c]) {
          case EMPTY: color = Color.WHITE; break;
          case WALL: color = Color.BLUE; break;
          case START: color = Color.MAGENTA; break;
          case EXIT: color = Color.RED; break;
          default:
            throw new IllegalStateException("Unexpected room type: " + rooms[r][c]);
        }
        window.setPenColor(color);
        window.filledRectangle(xCtr, yCtr, 0.5, 0.5);
        if (visited[r][c]) {
          window.setPenColor(Color.GREEN);
          window.filledCircle(xCtr, yCtr, 0.25);
        }
        if (distances != null) {
          window.setPenColor();
          window.text(xCtr, yCtr, "" + distances[r][c]);
        }
      }
    }
  } // draw()

  /**
   * Set window scale so that rooms appear square (assuming window itself
   * is square), and maze is centered in window.
   */
  private void setScale() {
    int range = Math.max(getRows(), getCols());
    double extraX = range - getCols();
    double xPadding = extraX / 2;
    window.setXscale(-xPadding, getCols() + xPadding);
    double extraY = range - getRows();
    double yPadding = extraY / 2;
    window.setYscale(getRows() + yPadding, 0 - yPadding);
  }

  /**
   * Draws given path from given starting point (with small magenta dots).
   */
  private void drawPath(int startRow, int startCol, List<Direction> path) {
    int r = startRow, c = startCol;
    window.setPenColor(Color.MAGENTA);
    for (Direction dir : path) {
      double xCtr = c + 0.5;
      double yCtr = r + 0.5;
      window.filledCircle(xCtr, yCtr, 0.1);
      switch (dir) {
        case UP:    r--; break;
        case RIGHT: c++; break;
        case DOWN:  r++; break;
        case LEFT:  c--; break;
      }
    }
  }

  /**
   * Creates random maze of given dimensions (defaults to 10x10),
   * prints it, and draws it.
   */
  public static void main(String[] args) throws IOException {
    // Random Maze
    System.out.println("Random maze...");
    int rows = (args.length > 0) ? Integer.parseInt(args[0]) : 10;
    int cols = (args.length > 1) ? Integer.parseInt(args[1]) : 10;
    double percentWalls = (args.length >2)? Double.parseDouble(args[2]) : PERCENT_WALLS;
    SimpleMaze randomMaze = new SimpleMaze(rows, cols, percentWalls);
    // randomMaze.print();  // uncomment to debug
    
    // Solve or regenerate a new random maze every time they window is clicked.
    // Stop if they type "q".
    while (true) {
      if (randomMaze.window.isMousePressed()) {
        if (randomMaze.distances == null) {
          randomMaze.solve();
        } else {
          randomMaze.makeRandomMaze(percentWalls);
        }
        randomMaze.window.pause(500); // long pause so click isn't registered multiple times
      } else if (randomMaze.window.isKeyPressed(KeyEvent.VK_Q)) {
        break;
      } else {
        randomMaze.window.pause(100); // short pause so we don't hog the CPU
      }
    }
    
    // Mazes from files
    // String[] filenames = {}; // Don't load files
    String[] filenames = {"maze0.txt", "maze1.txt", "maze2.txt"};
    // Note: maze0 and maze1 are solvable, maze2 is not.
    for (String filename : filenames) {
      System.out.println("Loading: " + filename);
      SimpleMaze maze = new SimpleMaze(filename);
      // maze.print(); // uncomment to debug
      maze.solve();
    }
  } // main(args)
  
}
