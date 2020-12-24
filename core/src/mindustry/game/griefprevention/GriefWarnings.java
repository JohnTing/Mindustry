package mindustry.game.griefprevention;

import arc.Core;
import arc.Events;
import arc.graphics.Color;
import arc.math.Mathf;
import arc.util.Log;
import mindustry.Vars;
import mindustry.content.Blocks;
import mindustry.content.Items;
import mindustry.content.Liquids;
import mindustry.game.EventType;
import mindustry.game.Team;
import mindustry.game.EventType.BuildSelectEvent;
import mindustry.game.EventType.DepositEvent;
import mindustry.game.EventType.ResetEvent;
import mindustry.game.EventType.TileChangeEvent;
import mindustry.game.EventType.WithdrawEvent;
import mindustry.game.griefprevention.TileInfo.TileAction;
import mindustry.gen.Builderc;
import mindustry.gen.Building;
import mindustry.gen.Call;
import mindustry.gen.Player;
import mindustry.gen.Unit;
import mindustry.mod.Plugin;
import mindustry.net.Administration.TraceInfo;
import mindustry.net.Packets.AdminAction;
import mindustry.type.Item;
import mindustry.world.Block;
import mindustry.world.Tile;
import mindustry.world.blocks.distribution.Sorter;
import mindustry.world.blocks.power.ItemLiquidGenerator;
import mindustry.world.blocks.power.NuclearReactor;
import mindustry.world.blocks.power.PowerGraph;
import mindustry.world.blocks.power.PowerNode;
import mindustry.world.blocks.sandbox.ItemSource;
import mindustry.world.blocks.sandbox.LiquidSource;
import mindustry.world.blocks.storage.StorageBlock;
import mindustry.world.blocks.storage.Unloader;

import static mindustry.Vars.*;
import static mindustry.Vars.player;

import java.time.Instant;
import java.util.HashMap;
import java.util.Map;
import java.util.WeakHashMap;

public class GriefWarnings {

  public GriefWarnings() {
    Events.on(DepositEvent.class, this::handleDeposit);
    Events.on(BuildSelectEvent.class, this::handleBuildSelectEvent);
  }
  
  Map<Tile, Player> powerSplit = new  HashMap<Tile, Player>();

  private Instant lastWarningTime = Instant.now();

  public boolean sendMessage(String message, int cooldown) {

    if (!Instant.now().isAfter(lastWarningTime.plusSeconds(cooldown))) {
      // ui.chatfrag.addMessage("skip", null);
      return false;
    }
    lastWarningTime = Instant.now();

    ui.chatfrag.addMessage(message, null);
    return true;
  }

  public boolean sendMessage(String message) {
    return sendMessage(message, 1);
  }

  public void handleBuildSelectEvent(BuildSelectEvent event) {

    Team team = event.team;
    Unit builder = event.builder;
    Tile tile = event.tile;
    boolean breaking = event.breaking;

    if (!breaking && team == player.team() && builder.getPlayer() != null && builder.buildPlan() != null) {

      Player player = builder.getPlayer();
      float progress = builder.buildPlan().progress;

      float coreDistance = getDistanceToCore(player.unit(), tile);
      Block cblock = builder.buildPlan().block;

      // persistent warnings that keep showing
      if (coreDistance < 30 && cblock instanceof NuclearReactor) {
        String message = "[scarlet]WARNING[] " + formatPlayer(player) + " is building a reactor at " + formatTile(tile)
            + " [stat]" + Math.round(coreDistance) + "[] blocks from core. [stat]" + Math.round(progress * 100) + "%";
        sendMessage(message, 5 + 20*(int)(1-progress));
      }
      /*
      else if (coreDistance < 10 && cblock instanceof ItemLiquidGenerator) {
        String message = "[scarlet]WARNING[] " + formatPlayer(player) + " is building a generator at "
            + formatTile(tile) + " [stat]" + Math.round(coreDistance) + "[] blocks from core. [stat]"
            + Math.round(progress * 100) + "%";
        sendMessage(message, 2);
      }*/

      if (cblock instanceof NuclearReactor) {
        String message = "[lightgray]Notice[] " + formatPlayer(player) + " is building a reactor at " + formatTile(tile)
            + "[stat]" + Math.round(progress * 100) + "%";
        sendMessage(message, 10 + 40*(int)(1-progress));
      }
    }
  }

  public void handlePowerGraphSplit(Tile tile, Player player) {
    sendMessage("[lightgray]Notice[] Power split by " + formatPlayer(player)+ " at " + formatTile(tile));
  }

  public void handleDeposit(DepositEvent event) {
    Player targetPlayer = event.player;

    Tile tile = event.tile.tile();
    Item item = event.item;
    int amount = event.amount;
    if (targetPlayer == null)
      return;

    if (item.equals(Items.thorium) && tile.block() instanceof NuclearReactor) {
      String message = "[scarlet]WARNING[] " + targetPlayer.name + "[white] ([stat]#" + targetPlayer.id
          + "[]) transfers [accent]" + amount + "[] thorium to a reactor. " + formatTile(tile);
      sendMessage(message);
    } else if (item.explosiveness > 0.5f) {
      Block block = tile.block();
      if (block instanceof ItemLiquidGenerator) {
        String message = "[scarlet]WARNING[] " + formatPlayer(targetPlayer) + " transfers [accent]" + amount
            + "[] blast to a generator. " + formatTile(tile);
        sendMessage(message);
      } else if (block instanceof StorageBlock) {
        String message = "[scarlet]WARNING[] " + formatPlayer(targetPlayer) + " transfers [accent]" + amount
            + "[] blast to a Container. " + formatTile(tile);
        sendMessage(message);
      }
    }
  }

  public void handleThoriumReactorheat(Tile tile, float heat) {
    if (heat > 0.15f && tile.interactable(player.team())) {
      sendMessage(
          "[scarlet]WARNING[] Thorium reactor at " + formatTile(tile) + " is overheating! Heat: [accent]" + heat);
    }
  }

  public float getDistanceToCore(Unit unit, float x, float y) {
    if (unit == null) {
      return Integer.MAX_VALUE;
    }
    if (unit.closestCore() == null) {
      return Integer.MAX_VALUE;
    }
    Tile nearestCore = unit.closestCore().tile();
    if (nearestCore == null) {
      return Integer.MAX_VALUE;
    }
    return Mathf.dst(x, y, nearestCore.x, nearestCore.y);
  }

  public float getDistanceToCore(Unit unit, Tile tile) {
    if (tile == null) {
      return Integer.MAX_VALUE;
    }
    return getDistanceToCore(unit, tile.x, tile.y);
  }

  public float getDistanceToCore(Unit unit) {
    return getDistanceToCore(unit, unit.x, unit.y);
  }

  public String formatPlayer(Player target) {
    String playerString;
    if (target != null) {
      playerString = target.name;
    } else {
      playerString = "[lightgray]unknown[]";
    }
    return playerString;
  }

  public String formatTile(Tile tile) {
    if (tile == null)
      return "(none)";
    return "(" + tile.x + ", " + tile.y + ")";
  }

}
