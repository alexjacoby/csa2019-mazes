import java.awt.Color;
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
 * TODO HW 3.16: Implement isSolvable(row, col).
 * TODO Challenge: Draw or print directions to solve the maze.
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
  /** Room we've already visited while searching for exit (was previously EMPTY). */
  public static final char BREADCRUMB = 'b';
  
  /** In randomly generated maze, what percent of the rooms should be walls. */
  public static final double PERCENT_WALLS = 0.25; // Note: should be configurable at runtime
  
  // FIELDS
  /**
   * "rooms" in maze must have one of the values listed above: EMPTY, WALL, START
   * EXIT, or BREADCRUMB.
   */
  private final char[][] rooms;
  
  // CONSTRUCTORS
  /** Creates new random maze with given dimensions. */
  public SimpleMaze(int rows, int cols) {
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
        if (Math.random() < PERCENT_WALLS) {
          rooms[r][c] = WALL;
        }
      }
    }
    // Add start and exit
    rooms[getRows()-1][0] = START; // bottom left
    rooms[0][getCols()-1] = EXIT; // top right
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
   */
  public boolean isSolvable(int row, int col) {
    // Return false if location invalid
    if (row < 0 || row >= getRows() || col < 0 || col >= getCols()) { return false; }
    
    char room = rooms[row][col];
    if (room == WALL || room == BREADCRUMB) { return false; }
    if (room == EXIT) { return true; }
    rooms[row][col] = BREADCRUMB;
    
    // Recursively search for solution up, right, down, and left
    return isSolvable(row-1, col) || isSolvable(row, col+1) ||
          isSolvable(row+1, col) || isSolvable(row, col-1);
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
          if (isSolvable(r, c)) { return true; }
        }
      }
    }
    return false;
  }
  
  /** Prints maze to System.out for debugging. */
  public void print() {
    for (char[] row : rooms) {
      for (char r : row) {
        System.out.print(r);
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
          case BREADCRUMB: color = Color.GREEN; break;
          default:
            throw new IllegalStateException("Unexpected room type: " + rooms[r][c]);
        }
        window.setPenColor(color);
        window.filledRectangle(xCtr, yCtr, 0.5, 0.5);
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
    SimpleMaze randomMaze = new SimpleMaze(rows, cols);
    Draw randomMazeWindow = new Draw("Random Maze: " + rows + "x" + cols);
    // randomMaze.print();  // uncomment to debug
    System.out.println("Solvable? " + randomMaze.isSolvable());
    randomMaze.draw(randomMazeWindow);
    
    // Mazes from files
    String[] filenames = {"maze0.txt", "maze1.txt", "maze2.txt"};
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
