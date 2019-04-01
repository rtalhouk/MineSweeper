import java.awt.Color;
import java.util.ArrayList;
import java.util.Random;
import java.util.Arrays;
import java.util.HashMap;

import tester.*;
import javalib.impworld.*;
import javalib.worldimages.Posn;
import javalib.worldimages.TextImage;

//Represents the World State of the game Minesweeper
class MineWorld extends World {
  int rowMax;
  int colMax;
  Random rand;
  ArrayList<Cell> cells;

  MineWorld(int rowMax, int colMax, int numMines) {
    this.rowMax = rowMax;
    this.colMax = colMax;
    this.rand = new Random();
    this.cells = this.makeCells();
    this.assignCells();
    this.makeMines(numMines);
    
    for (Cell c : this.cells) {
      c.neighborMines = c.countNeighborMines();
    }
  }

  //THIS CONSTRUCTOR IS FOR TESTING PURPOSES ONLY
  MineWorld(int rowMax, int colMax, Random rand) {
    this.rowMax = rowMax;
    this.colMax = colMax;
    this.rand = rand;
    this.cells = this.makeCells(); 
  }

  //Turns the given number of cells in this MineWorld into Mines based on their index in
  //this MineWorld's ArrayList of Cells
  void makeMines(int numMines) {
    if (numMines > this.rowMax * this.colMax) {
      throw new IllegalArgumentException("Number of mines must be less than number of cells");
    }

    ArrayList<Integer> cellNums = new ArrayList<Integer>();
    for (int i = 0; i < this.rowMax * this.colMax; i++) {
      cellNums.add(i);
    }
    
    for (int i = 0; i < numMines; i++) {
      int newMine = cellNums.remove(this.rand.nextInt(cellNums.size()));
      this.cells.get(newMine).makeMine();
    }
  }

  //Creates a list of cells based on how many rows and columns this MineWorld is to have
  ArrayList<Cell> makeCells() {
    ArrayList<Cell> cells = new ArrayList<Cell>();
    for (int i = 0; i < this.rowMax * this.colMax; i++) {
      cells.add(new Cell());
    } 
    return cells;
  }

  //Assigns all the cells in this MineWorld to their correct neighbors
  void assignCells() {
    for (int i = 0; i < this.rowMax * this.colMax; i++) {
      this.cells.get(i).assignToCell(i, this.cells, rowMax, colMax);
    }
  }


  //Draws a WorldScene representation of this MineWorld by placing each cell on a canvas
  public WorldScene makeScene() {
    WorldScene scene = new WorldScene(this.colMax * 20, this.rowMax * 20);
    
    for (int i = 0; i < this.cells.size(); i++) {
      scene.placeImageXY(
          cells.get(i).drawCell(),
          ((i % colMax) * Cell.BLOCK_SIZE) + Cell.BLOCK_SIZE / 2,
          (Math.floorDiv(i, this.colMax) * Cell.BLOCK_SIZE) + Cell.BLOCK_SIZE / 2);
    }
    
    return scene;
  }
  
  //Handles everything to do with clicking, including revealing and flagging tiles
  public void onMouseClicked(Posn location, String button) {
    if (button.equals("LeftButton")) {
      this.getTileAt(location).handleLeftClick();
    }
    if (button.equals("RightButton")) {
      this.getTileAt(location).handleRightClick();
    }
  }
  
  //Returns the tile at a given row and column position on the board
  Cell getTileAt(Posn location) {
    Posn tileLoc =
        new Posn(
            Math.floorDiv(location.x, Cell.BLOCK_SIZE),
            Math.floorDiv(location.y, Cell.BLOCK_SIZE));
    return this.cells.get((tileLoc.y * colMax) + tileLoc.x);
  }
  
  //Checks if the user has won or lost the game at any moment
  public void onTick() {
    boolean youWin = true;
    for (Cell c : this.cells) {
      if (c.isMine && c.isRevealed) {
        this.endOfWorld("You Lose");
      }
      else if (!c.isMine && !c.isRevealed) {
        youWin = false;
      }
    }
    
    if (youWin) {
      this.endOfWorld("You Win");
    }
  }
  
  //Draws the final scene of the game when a user wins or loses
  public WorldScene lastScene(String msg) {
    if (msg.equals("You Win")) {
      WorldScene win = this.makeScene();
      win.placeImageXY(
          new TextImage(msg, 28, Color.red),
          this.colMax * (Cell.BLOCK_SIZE / 2),
          this.rowMax * (Cell.BLOCK_SIZE / 2));
      return win;
    }
    else {
      for (Cell c : this.cells) {
        if (c.isMine) {
          c.isRevealed = true;
        }
      }
      WorldScene lose = this.makeScene();
      lose.placeImageXY(
          new TextImage(msg, 28, Color.red),
          this.colMax * (Cell.BLOCK_SIZE / 2),
          this.rowMax * (Cell.BLOCK_SIZE / 2));
      return lose;
    }
  }

}


class ExamplesMineWorld {
  MineWorld world1;
  ArrayList<Cell> emptyCells;
  ArrayList<Cell> fullCells;

  void reset() {
    this.world1 = new MineWorld(5, 5, new Random(5));
    this.emptyCells = new ArrayList<Cell>(
        Arrays.asList(new Cell(), new Cell(), new Cell(), new Cell(), new Cell(),
            new Cell(), new Cell(), new Cell(), new Cell(), new Cell(),
            new Cell(), new Cell(), new Cell(), new Cell(), new Cell(),
            new Cell(), new Cell(), new Cell(), new Cell(), new Cell(),
            new Cell(), new Cell(), new Cell(), new Cell(), new Cell()));
  }

  void testMakeCells(Tester t) {
    reset();
    t.checkExpect(this.world1.makeCells(), this.emptyCells);
    reset();
  }
  
  void testAssignCells(Tester t) {
    reset();
    t.checkExpect(this.world1.cells, this.emptyCells);
    t.checkExpect(this.world1.cells.get(0).neighbors, new HashMap<String, IGamePiece>());
    this.world1.assignCells();
    t.checkExpect(this.world1.cells.get(0).neighbors.get("TopLeft"), new Wall());
    reset();
  }

  void testMakeMines(Tester t) {
    reset();
    t.checkException(
        new IllegalArgumentException("Number of mines must be less than number of cells"),
        this.world1, "makeMines", 27);
    this.world1.assignCells();
    this.world1.makeMines(5);
    t.checkExpect(this.world1.cells.get(1).isMine, false);
    t.checkExpect(this.world1.cells.get(4).isMine, true);
    t.checkExpect(this.world1.cells.get(21).isMine, true);
    t.checkExpect(this.world1.cells.get(22).isMine, true);
    t.checkExpect(this.world1.cells.get(11).isMine, true);
    t.checkExpect(this.world1.cells.get(12).isMine, true);
    t.checkExpect(this.world1.cells.get(2).isMine, false);
    t.checkExpect(this.world1.cells.get(17).isMine, false);
    reset();
  }
  
  void testOnMouseClicked(Tester t) {
    reset();
    this.world1.assignCells();
    this.world1.makeMines(5);
    this.world1.onMouseClicked(new Posn(1, 1), "LeftButton");
    t.checkExpect(this.world1.cells.get(0).isRevealed, true);
  }
  
  void testGetTileAt(Tester t) {
    reset();
    this.world1.assignCells();
    this.world1.makeMines(5);
    t.checkExpect(this.world1.getTileAt(new Posn(1, 1)), this.world1.cells.get(0));
    t.checkExpect(this.world1.getTileAt(new Posn(50, 1)), this.world1.cells.get(2));
  }
  
  void testLastScene(Tester t) {
    reset();
    this.world1.assignCells();
    this.world1.makeMines(5);
    WorldScene img = this.world1.makeScene();
    img.placeImageXY(
        new TextImage("You Win", 28, Color.red),
        this.world1.colMax * (20 / 2),
        this.world1.rowMax * (20 / 2));
    t.checkExpect(this.world1.lastScene("You Win"), img);
  }
  
  void testBigBang(Tester t) {
    reset();
    MineWorld mw = new MineWorld(16, 30, 99);
    int worldWidth = mw.colMax * Cell.BLOCK_SIZE;
    int worldHeight = mw.rowMax * Cell.BLOCK_SIZE;
    double tickRate = 1.0 / 28.0;
    mw.bigBang(worldWidth, worldHeight, tickRate);
  }
  
  
  

}