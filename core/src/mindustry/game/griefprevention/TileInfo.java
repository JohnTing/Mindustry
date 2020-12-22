package mindustry.game.griefprevention;

import java.time.Instant;
import java.util.LinkedList;
import java.util.List;

import arc.util.Nullable;
import mindustry.gen.Building;
import mindustry.gen.Player;
import mindustry.type.Item;
import mindustry.world.Block;
import mindustry.world.Tile;

public class TileInfo implements Cloneable {

  LinkedList<TileAction> actionList = new LinkedList<TileAction>();
  TileAction lastAction = null;
  TileInfo() {
  }
  
  public void add(Player player, ActionType action, Block block) {
    
    if (lastAction != null) { 
      if (lastAction.player == player && lastAction.action == action && lastAction.block == block) {
        return;
      }
    }
    
    // mindustry.Vars.ui.chatfrag.addMessage(String.format("%s %s %s", player == null ? "null" : player.name(), action.name(), block), null);
    lastAction = new TileAction(player, action, block);
    // mindustry.Vars.ui.scriptfrag.addMessage(String.format("%s", block.localizedName));
    actionList.add(lastAction);
    if (actionList.size() > 5) {
      actionList.removeFirst();
    }
  } 
  public TileAction getlastAction() {
    return lastAction;
  }


  /*
  public void modifyLastOne(Player player, ActionType action, Block block) {

    if (actionList.size() == 0) {
      add(player, action, block);
      return;
    }

    TileAction ta = actionList.getLast();
    if (player != null) {
      ta.player = player;
    }
    if (action != null) {
      ta.action = action;
    }
    if (block != null) {
      ta.block = block;
    }
  }
  */

  /*
  public TileInfo clone() {
    try {
        return (TileInfo)super.clone();
    } catch (CloneNotSupportedException e) {
        throw new RuntimeException("literally not possible");
    }
  }*/
  @Override
  public String toString() {
    if(actionList == null) {
      return "";
    }
    boolean first = true;
    StringBuilder sb = new StringBuilder();
    for (TileAction ta : actionList) {

      if (first) {
        first = false;
      } else {
        sb.append("\n");
      }

      sb.append(ta.toString());
    }

    return sb.toString();
  }

  class TileAction {

    @Nullable Player player;
    ActionType action;
    @Nullable Block block;
    Instant time;
    TileAction(Player player, ActionType action, Block block) {
      this.player = player;
      this.action = action;
      this.block = block;
      time = Instant.now();
    }

    @Override
    public String toString() {

      // Instant.now().


        return (player == null ? "null" : player.name) + " | " + action.name() + " | " + (block == null ? "" : block.name);
      }
  }
  public enum ActionType {
    breakBlock, placeBlock, rotate, configure, withdrawItem, depositItem, control, command, pickupBlock, dropBlock
  }
}
