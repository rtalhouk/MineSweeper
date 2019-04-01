import java.util.HashMap;
import java.util.Iterator;

import tester.Tester;

//Allows a Cell's neighbors to be iterated over
class NeighborIterator implements Iterator<IGamePiece> {
  HashMap<String, IGamePiece> map;
  String key = "Top";
  boolean completed = false;

  NeighborIterator(HashMap<String, IGamePiece> neighbors) {
    this.map = neighbors;
  }

  //If all keys have not been iterated over, the Iterator has a Next
  public boolean hasNext() {
    return !completed;
  }

  //Cycles through the keys of this Iterator's map
  public IGamePiece next() {
    if (this.key.equals("Top")) {
      this.key = "TopRight";
      return this.map.get("Top");
    }
    else if (this.key.equals("TopRight")) {
      this.key = "Right";
      return this.map.get("TopRight");
    }
    else if (this.key.equals("Right")) {
      this.key = "BottomRight";
      return this.map.get("Right");
    }
    else if (this.key.equals("BottomRight")) {
      this.key = "Bottom";
      return this.map.get("BottomRight");
    }
    else if (this.key.equals("Bottom")) {
      this.key = "BottomLeft";
      return this.map.get("Bottom");
    }
    else if (this.key.equals("BottomLeft")) {
      this.key = "Left";
      return this.map.get("BottomLeft");
    }
    else if (this.key.equals("Left")) {
      this.key = "TopLeft";
      return this.map.get("Left");
    }
    else if (this.key.equals("TopLeft")) {
      this.key = "";
      this.completed = true;
      return this.map.get("TopLeft");
    }
    else {
      throw new RuntimeException("Next not found");
    }
  }
}

class ExamplesNeighborIterator {
  NeighborIterator iterator1;
  HashMap<String, IGamePiece> map;
  
  void reset() {
    this.map = new HashMap<String, IGamePiece>();
    
    this.map.put("Top", new Wall());
    this.map.put("TopRight", new Cell());
    this.map.put("Right", new Wall());
    this.map.put("BottomRight", new Cell());
    this.map.put("Bottom", new Wall());
    this.map.put("BottomLeft", new Cell());
    this.map.put("Left", new Wall());
    this.map.put("TopLeft", new Cell());
    
    this.iterator1 = new NeighborIterator(this.map);
  }
  
  void testHasNext(Tester t) {
    reset();
    t.checkExpect(this.iterator1.hasNext(), true);
    this.iterator1.completed = true;
    t.checkExpect(this.iterator1.hasNext(), false);
    reset();
  }
  
  void testNext(Tester t) {
    reset();
    t.checkExpect(this.iterator1.next(), new Wall());
    t.checkExpect(this.iterator1.next(), new Cell());
    reset();
  }
}