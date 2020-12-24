package mindustry.game.griefprevention;

import java.time.Instant;
import java.util.LinkedList;
import java.util.List;

import arc.util.Nullable;
import mindustry.content.Blocks;
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

    // mindustry.Vars.ui.chatfrag.addMessage(String.format("%s %s %s", player ==
    // null ? "null" : player.name(), action.name(), block), null);
    lastAction = new TileAction(player, action, block);
    // mindustry.Vars.ui.scriptfrag.addMessage(String.format("%s",
    // block.localizedName));
    actionList.add(lastAction);
    if (actionList.size() > 5) {
      actionList.removeFirst();
    }
  }

  public TileAction getlastAction() {
    return lastAction;
  }

  @Override
  public String toString() {
    if (actionList == null) {
      return "";
    }
    StringBuilder sb = new StringBuilder();
    for (TileAction ta : actionList) {

      if(sb.length() > 0) {
        sb.append("\n");
      }
      sb.append(ta.toString());
    }
    return sb.toString();
  }

  public String toSimpleString() {
    if (actionList == null) {
      return "";
    }
    StringBuilder sb = new StringBuilder();
    for (TileAction ta : actionList) {
      if(sb.length() > 0) {
        sb.append("\n");
      }
      sb.append(ta.toSimpleString());
    }
    return sb.toString();
  }

  class TileAction {

    @Nullable
    Player player;
    ActionType action;
    @Nullable
    Block block;
    Instant time;

    TileAction(Player player, ActionType action, Block block) {
      this.player = player;
      this.action = action;
      this.block = block;
      time = Instant.now();
    }

    @Override
    public String toString() {
      return (player == null ? "null" : player.name) + " | " + action.name() + " | "
          + (block == null ? "" : block.name);
    }
    public String toSimpleString() {
      return (player == null ? "null" : player.name.length() > 30 ? player.name.substring(0, 29) : player.name) + " | " + action.simpleName() + " | "
          + (block == null ? "" : block.emoji());
    }
  }

  public enum ActionType {
    breakBlock("break"), placeBlock("place"), rotate, configure("config"), withdrawItem("withdraw"), depositItem("deposit"), 
    control, command, pickupBlock("pickup"), dropBlock("drop");
    String simpleName;

    ActionType(String simpleName) {
      this.simpleName = simpleName;
    }

    ActionType() {
      this.simpleName = this.name();
    }

    String simpleName() {
      return simpleName;
    }
  }
}
