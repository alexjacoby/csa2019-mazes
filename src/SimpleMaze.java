import java.awt.Color;
import java.awt.event.KeyEvent;
import java.io.File;
import java.io.IOException;
import java.util.Scanner;

/**
 * Simple maze solver.  Can generate a random maze (possibly solvable) or
 * load a maze from a file and try to solve it.
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
 * TODO HW 3.17: drawShortestPath(): Use the distances array (provided, or you can
 * erase mine and try it yourself) to figure out the shortest path from START to
 * EXIT (*).  You should either draw arrows or a special color to show the path
 * visually.  If multiple shortest paths exist, any of them is fine.
 * 
 * (*) Theoretically a maze could have multiple STARTs or EXITs -- just use the
 *  first one you find.
 *  
 * Suggested algorithm: Starting at START, choose the neighbor with the smallest
 * distance from exit.  Continue recursively until you reach the exit.
 * 
 * Issues: Our current isSolvable replaces START with a BREADCRUMB, so you'll have
 * to save the START position another way.
 * 
 * There's another issue I thought of but it escapes me now :P
 * 
 * @author Mr. Jacoby
 * @version 3/11/19
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
   * "rooms" in maze must have one of the values listed above: EMPTY, WALL, START, or
   * EXIT.
   */
  private final char[][] rooms;

  /**
   * Tracks which rooms have been visited.
   */
  private final boolean[][] visited;

  /**
   * Tracks shortest path to exit from corresponding room (-1 means no path possible).
   * Initialized in initDistances().
   */
  private final int[][] distances;

  // CONSTRUCTORS
  /** Creates new random maze with given dimensions and percent walls. */
  public SimpleMaze(int rows, int cols, double percentWalls) {
    rooms = new char[rows][cols];
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
    
    visited = new boolean[getRows()][getCols()];
    distances = initDistances();
  } // end SimpleMaze(rows, cols)

  /** Loads maze from given text file. */
  public SimpleMaze(String filename) throws IOException {
    Scanner in = new Scanner(new File(filename));
    int rows = in.nextInt();
    int cols = in.nextInt();
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

    visited = new boolean[getRows()][getCols()];
    distances = initDistances();
  } // end SimpleMaze(filename)

  /**
   * Returns initialized distances array: all spots are 0 except walls, which are -1.
   * Rooms array must already be initialized.
   */
  private int[][] initDistances() {
    int[][] distances = new int[getRows()][getCols()];
    for (int row = 0; row < getRows(); row++) {
      for (int col = 0; col < getCols(); col++) {
        if (rooms[row][col] == WALL) {
          distances[row][col] = -1;
        }
      }
    }
    return distances;
  }

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
   */
  public boolean isSolvable(int row, int col) {
    // Return false if location invalid
    if (row < 0 || row >= getRows() || col < 0 || col >= getCols()) { return false; }
    
    char room = rooms[row][col];
    if (room == WALL || visited[row][col]) { return false; }
    if (room == EXIT) { return true; }
    visited[row][col] = true;
    
    // Recursively search for solution up, right, down, and left
    if (isSolvable(row-1, col)) {
      System.out.println("UP!");
      return true;
    } else if (isSolvable(row, col+1)) {
      System.out.println("RIGHT!");
      return true;
    } else if (isSolvable(row+1, col)) {
      System.out.println("DOWN!");
      return true;
    } else if (isSolvable(row, col-1)) {
      System.out.println("LEFT!");
      return true;
    } else {
      return false;
    }
    /*
    return isSolvable(row-1, col) || isSolvable(row, col+1) ||
           isSolvable(row+1, col) || isSolvable(row, col-1);
           */
  }
  
  /**
   * Returns true if maze can be solved from one of its START positions.
   * Convenience method so you don't need to always worry about the starting location
   * when calling isSolvable(row, col).
   */
  public boolean isSolvable() {
    // Searches through rooms, looking for a START.  If found, will try solving
    // maze from that location.  If not solvable from that location, keeps trying.
    for (int r = 0; r < getRows(); r++) {
      for (int c = 0; c < getCols(); c++) {
        if (rooms[r][c] == START) {
          if (isSolvable(r, c)) {
            return true;
          }
        }
      }
    }
    return false;
  }

  /**
   * Figures out the shortest distance from each room to the exit.  Stores
   * results in distances field.
   */
  public void updateDistances() {
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

  /** Draws maze to given window. */
  public void draw(Draw window) {
    window.setYscale(getRows(), 0);
    window.setXscale(0, getCols());
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
        window.setPenColor();
        window.text(xCtr, yCtr, "" + distances[r][c]);
      }
    }
  } // draw(window)

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
    Draw randomMazeWindow = new Draw(String.format("Random Maze: %dx%d %2.0f%%",rows, cols,
          (percentWalls*100)));
    randomMaze.print();  // uncomment to debug
    System.out.println("Solvable? " + randomMaze.isSolvable());
    randomMaze.updateDistances();
    randomMaze.draw(randomMazeWindow);
    
    // Generate a new random maze every time they window is clicked.
    // Stop if they type "q".
    while (true) {
      if (randomMazeWindow.isMousePressed()) {
        randomMazeWindow.clear();
        randomMaze = new SimpleMaze(rows, cols, percentWalls);
        System.out.println("Solvable? " + randomMaze.isSolvable());
        randomMaze.updateDistances();
        randomMaze.draw(randomMazeWindow);
        randomMazeWindow.pause(500); // long pause so click isn't registered multiple times
      } else if (randomMazeWindow.isKeyPressed(KeyEvent.VK_Q)) {
        break;
      } else {
        randomMazeWindow.pause(100); // short pause so we don't hog the CPU
      }
    }
    
    // Mazes from files
    String[] filenames = {}; // Don't load files
    // String[] filenames = {"maze0.txt", "maze1.txt", "maze2.txt"};
    // Note: maze0 and maze1 are solvable, maze2 is not.
    for (String filename : filenames) {
      System.out.println("Loading: " + filename);
      SimpleMaze maze = new SimpleMaze(filename);
      Draw mazeWindow = new Draw(filename + ": " + maze.getRows() + "x" + maze.getCols());
      // maze.print(); // uncomment to debug
      System.out.println("Solvable? " + maze.isSolvable());
      maze.draw(mazeWindow);
    }
  } // main(args)
  
}
