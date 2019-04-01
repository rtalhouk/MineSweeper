import java.util.ArrayList;
import java.util.HashMap;
import java.util.Random;

import tester.*;
import java.awt.Color;
import javalib.worldimages.*;

//Represents an element in Minesweeper, whether a Cell or Wall
interface IGamePiece {

  //Makes this GamePiece into a mine if it is a Cell
  void makeMine();

  //Returns whether this GamePiece is a Mine
  boolean mineStatus();

  void floodFill();
  
}

//Represents one tile of a Minesweeper game
class Cell implements IGamePiece {
  boolean isMine = false;
  boolean isRevealed = false;
  boolean isFlagged = false;
  HashMap<String, IGamePiece> neighbors = new HashMap<String, IGamePiece>();
  int neighborMines;
  static int BLOCK_SIZE = 20;

  Cell() {}

  //Turns this Cell into a Mine
  public void makeMine() {
    this.isMine = true;
  }

  //Counts the number of this Cell's neighbors that are mines
  int countNeighborMines() {
    int numMines = 0;
    NeighborIterator map = new NeighborIterator(this.neighbors);
    IGamePiece lookingAt = new Wall();

    for (int i = 0; i < 8; i++) {
      lookingAt = map.next();
      if (lookingAt.mineStatus()) {
        numMines += 1;
      }
    }

    return numMines;
  }

  //Assigns the cells in the given array list to this Cell's neighbors based on their positions
  //in the ArrayList of Cells
  void assignToCell(int idx, ArrayList<Cell> cells, int rowMax, int colMax) {

    boolean leftCol = (idx % colMax == 0);
    boolean topRow = (idx < colMax);
    boolean rightCol = (idx % colMax == colMax - 1);
    boolean bottomRow = (idx + colMax >= rowMax * colMax);

    if (leftCol) {
      if (topRow) {
        this.neighbors.putIfAbsent("BottomRight", cells.get(idx + (colMax + 1))); 
      }
      if (!topRow && !bottomRow) {
        this.neighbors.putIfAbsent("TopRight", cells.get(idx - (colMax - 1)));  
        this.neighbors.putIfAbsent("BottomRight", cells.get(idx + (colMax + 1))); 
      }
      this.neighbors.put("TopLeft", new Wall());
      this.neighbors.put("Left", new Wall());
      this.neighbors.put("BottomLeft", new Wall());
    }
    else {
      this.neighbors.put("Left", cells.get(idx - 1));
    }
    if (topRow) {
      if (rightCol) {
        this.neighbors.putIfAbsent("BottomLeft", cells.get(idx + (colMax - 1)));
      }
      else if (!leftCol) {
        this.neighbors.putIfAbsent("BottomRight", cells.get(idx + (colMax + 1))); 
        this.neighbors.putIfAbsent("BottomLeft", cells.get(idx + (colMax - 1)));
      }
      this.neighbors.put("TopLeft", new Wall());
      this.neighbors.put("Top", new Wall());
      this.neighbors.put("TopRight", new Wall());
    }
    else {
      this.neighbors.put("Top", cells.get(idx - colMax));
    }
    if (rightCol) {
      this.neighbors.put("TopRight", new Wall());
      this.neighbors.put("Right", new Wall());
      this.neighbors.put("BottomRight", new Wall());
      if (!topRow && !bottomRow) {
        this.neighbors.putIfAbsent("BottomLeft", cells.get(idx + (colMax - 1)));
        this.neighbors.putIfAbsent("TopLeft", cells.get(idx - (colMax + 1)));
      }
    }
    else {
      this.neighbors.put("Right", cells.get(idx + 1));
    }
    if (bottomRow) {
      if (leftCol) {
        this.neighbors.putIfAbsent("TopRight", cells.get(idx - (colMax - 1))); 
        this.neighbors.putIfAbsent("TopLeft", new Wall());
      }
      else if (rightCol) {
        this.neighbors.putIfAbsent("TopLeft", cells.get(idx - (colMax + 1)));
        this.neighbors.putIfAbsent("TopRight", new Wall()); 

      }
      else if (!rightCol && !leftCol) {
        this.neighbors.putIfAbsent("TopRight", cells.get(idx - (colMax - 1)));  
        this.neighbors.putIfAbsent("TopLeft", cells.get(idx - (colMax + 1)));
      }
      this.neighbors.put("BottomLeft", new Wall());
      this.neighbors.put("Bottom", new Wall());
      this.neighbors.put("BottomRight", new Wall());
    }
    else {
      this.neighbors.put("Bottom", cells.get(idx + colMax));
    }
    if (!leftCol && !topRow && !rightCol && !bottomRow) {
      this.neighbors.putIfAbsent("TopRight", cells.get(idx - (colMax - 1)));  
      this.neighbors.putIfAbsent("BottomRight", cells.get(idx + (colMax + 1))); 
      this.neighbors.putIfAbsent("BottomLeft", cells.get(idx + (colMax - 1)));
      this.neighbors.putIfAbsent("TopLeft", cells.get(idx - (colMax + 1)));
    }
  }

  //Returns whether this Cell is a Mine
  public boolean mineStatus() {
    return this.isMine;
  }

  //Returns a WorldImage representation of this Cell based on its fields
  WorldImage drawCell() {
    if (this.isRevealed) {
      if (this.isMine) {
        return new OverlayImage(
            new CircleImage(6, OutlineMode.SOLID, Color.red),
            new RectangleImage(Cell.BLOCK_SIZE, Cell.BLOCK_SIZE, OutlineMode.SOLID, Color.gray));
      }
      else {
        return new OverlayImage(
            new TextImage(Integer.toString(this.neighborMines), 12, Color.green),
            new RectangleImage(Cell.BLOCK_SIZE, Cell.BLOCK_SIZE, OutlineMode.SOLID, Color.gray));
      }
    }
    else {
      if (isFlagged) {
        return new OverlayImage(
            new TriangleImage(
                new Posn(0,0), new Posn(-5, 10), new Posn(5, 10), OutlineMode.SOLID, Color.orange),
            new OverlayImage(
                new RectangleImage(
                    BLOCK_SIZE - 1, BLOCK_SIZE - 1, OutlineMode.SOLID, Color.DARK_GRAY),
                new RectangleImage(BLOCK_SIZE, BLOCK_SIZE, OutlineMode.SOLID, Color.white)));
      }
      return new OverlayImage(
          new RectangleImage(BLOCK_SIZE - 1, BLOCK_SIZE - 1, OutlineMode.SOLID, Color.DARK_GRAY),
          new RectangleImage(Cell.BLOCK_SIZE, Cell.BLOCK_SIZE, OutlineMode.SOLID, Color.white));
    }
  }

  //If the tile is not already flagged or revealed, then reveal cell(s)
  void handleLeftClick() {
    if (!isFlagged && !isRevealed) {
      this.floodFill();
    }
  }
  
  //Toggle whether this cell is flagged
  void handleRightClick() {
    this.isFlagged = !this.isFlagged;
  }

  //Reveals this tile and any adjacent tiles that have no mines as neighbors
  public void floodFill() {
    if (this.neighborMines == 0 && !isRevealed) {
      this.isRevealed = true;
      
      NeighborIterator map = new NeighborIterator(this.neighbors);
      IGamePiece lookingAt = new Wall();

      for (int i = 0; i < 8; i++) {
        lookingAt = map.next();
        lookingAt.floodFill();
      }
    }
    else {
      this.isRevealed = true;
    }
  }
  
}



//Represents a Wall, or the end of a row or column, of Minesweeper
class Wall implements IGamePiece {

  //A Wall cannot be a mine, so nothing is done
  public void makeMine() {
    //Nothing is done in this function because no wall can ever be made a mine
    //they are simply boundaries for the neighbors
  }

  //A Wall is never a mine, so its mineStatus is always false
  public boolean mineStatus() {
    return false;
  }

  //A Wall cannot be revealed, so empty function
  public void floodFill() {
    //Does nothing because a Wall is never concealed or revealed
  }
  
}

class ExamplesGamePiece {
  Cell cell1;
  Wall wall1;

  MineWorld world2;
  ArrayList<Cell> emptyCells;

  void reset() {
    this.cell1 = new Cell();
    this.wall1 = new Wall();
    this.world2 = new MineWorld(4, 4, new Random(4));
    this.emptyCells = this.world2.cells;
  }

  void testCountNeighbors(Tester t) {
    reset();
    this.world2.assignCells();
    this.world2.makeMines(4);
    t.checkExpect(world2.cells.get(0).countNeighborMines(), 1);
    reset();
  }

  void testMakeMine(Tester t) {
    reset();
    t.checkExpect(this.cell1.isMine, false);
    this.cell1.makeMine();
    t.checkExpect(this.cell1.isMine, true);
    reset();
  }

  void testMineStatus(Tester t) {
    reset();
    t.checkExpect(this.cell1.mineStatus(), false);
    t.checkExpect(this.wall1.mineStatus(), false);
    this.cell1.makeMine();
    this.wall1.makeMine();
    t.checkExpect(this.cell1.mineStatus(), true);
    t.checkExpect(this.wall1.mineStatus(), false);
  }

  void testAssignToCell(Tester t) {
    reset();
    t.checkExpect(this.cell1.neighbors, new HashMap<String, IGamePiece>());
    this.cell1.assignToCell(0, this.emptyCells, 4, 4);
    t.checkExpect(this.cell1.neighbors.get("BottomRight"), this.emptyCells.get(5));
    t.checkExpect(this.cell1.neighbors.get("TopLeft"), new Wall());
    reset();
  }

  void testDrawCell(Tester t) {
    reset();
    this.cell1.isRevealed = true;
    t.checkExpect(this.cell1.drawCell(),
        new OverlayImage(
            new TextImage(Integer.toString(0), 12, Color.green),
            new RectangleImage(Cell.BLOCK_SIZE, Cell.BLOCK_SIZE,
                OutlineMode.SOLID, Color.gray)));
    
    this.cell1.isMine = true;

    t.checkExpect(this.cell1.drawCell(), new OverlayImage(
        new CircleImage(6, OutlineMode.SOLID, Color.red),
        new RectangleImage(Cell.BLOCK_SIZE, Cell.BLOCK_SIZE, OutlineMode.SOLID, Color.gray)));
    
    this.cell1.isRevealed = false;
    
    this.cell1.isFlagged = true;
    
    t.checkExpect(this.cell1.drawCell(),
        new OverlayImage(
            new TriangleImage(
                new Posn(0,0),
                new Posn(Cell.BLOCK_SIZE / -4, Cell.BLOCK_SIZE / 2),
                new Posn(Cell.BLOCK_SIZE / 4, Cell.BLOCK_SIZE / 2),
                OutlineMode.SOLID, Color.orange),
            new OverlayImage(
                new RectangleImage(
                    Cell.BLOCK_SIZE - 1, Cell.BLOCK_SIZE - 1, OutlineMode.SOLID, Color.DARK_GRAY),
                new RectangleImage(
                    Cell.BLOCK_SIZE, Cell.BLOCK_SIZE, OutlineMode.SOLID, Color.white))));
    
    this.cell1.isFlagged = false;

    t.checkExpect(this.cell1.drawCell(),
        new OverlayImage(
            new RectangleImage(Cell.BLOCK_SIZE - 1, Cell.BLOCK_SIZE - 1,
                OutlineMode.SOLID, Color.DARK_GRAY),
            new RectangleImage(Cell.BLOCK_SIZE, Cell.BLOCK_SIZE, OutlineMode.SOLID, Color.white)));
    reset();
  }
  
  void testHandleLeftClick(Tester t) {
    reset();
    this.cell1.isFlagged = true;
    this.cell1.handleLeftClick();
    t.checkExpect(this.cell1.isRevealed, false);
    this.cell1.isFlagged = false;
    
    this.world2.assignCells();
    this.world2.cells.get(0).handleLeftClick();
    t.checkExpect(this.world2.cells.get(0).isRevealed, true);
    
    NeighborIterator map1 = new NeighborIterator(this.world2.cells.get(0).neighbors);
    IGamePiece lookingAt1 = new Wall();
    for (int i = 0; i < 8; i++) {
      lookingAt1 = map1.next();
      if (lookingAt1 instanceof Cell) {
        Cell cell = (Cell)lookingAt1;
        t.checkExpect(cell.isRevealed, true);
      }
    }
    reset();
  }
  
  void testHandleRightClick(Tester t) {
    reset();
    t.checkExpect(this.cell1.isFlagged, false);
    this.cell1.handleRightClick();
    t.checkExpect(this.cell1.isFlagged, true);
    this.cell1.handleRightClick();
    t.checkExpect(this.cell1.isFlagged, false);
    reset();
  }
  
  void testFloodFill(Tester t) {
    reset();
    this.cell1.isRevealed = true;
    this.cell1.floodFill();
    t.checkExpect(this.cell1.isRevealed, true);
    this.cell1.isRevealed = false;
    
    this.world2.assignCells();
    this.world2.cells.get(0).floodFill();
    t.checkExpect(this.world2.cells.get(0).isRevealed, true);
    
    NeighborIterator map1 = new NeighborIterator(this.world2.cells.get(0).neighbors);
    IGamePiece lookingAt1 = new Wall();
    for (int i = 0; i < 8; i++) {
      lookingAt1 = map1.next();
      if (lookingAt1 instanceof Cell) {
        Cell cell = (Cell)lookingAt1;
        t.checkExpect(cell.isRevealed, true);
      }
    }
    reset();
  }

}