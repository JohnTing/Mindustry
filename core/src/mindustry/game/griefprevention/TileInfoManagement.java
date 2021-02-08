package mindustry.game.griefprevention;

import java.time.Instant;
import java.util.LinkedList;
import java.util.WeakHashMap;

import arc.Events;
import arc.struct.IntSet;
import arc.struct.Queue;
import arc.struct.Seq;
import mindustry.game.EventType.BlockBuildBeginEvent;
import mindustry.game.EventType.BlockBuildEndEvent;
import mindustry.game.EventType.BuildSelectEvent;
import mindustry.game.EventType.CommandIssueEvent;
import mindustry.game.EventType.ConfigEvent;
import mindustry.game.EventType.DepositEvent;
import mindustry.game.EventType.PickupEvent;
import mindustry.game.EventType.TapEvent;
import mindustry.game.EventType.WithdrawEvent;
import mindustry.game.griefprevention.TileInfo.ActionType;
import mindustry.gen.Building;
import mindustry.gen.Call;
import mindustry.gen.Player;
import mindustry.world.Block;
import mindustry.world.Tile;
import mindustry.world.blocks.ConstructBlock.ConstructBuild;
import mindustry.world.blocks.power.PowerGraph;
import mindustry.world.blocks.power.PowerNode;
import mindustry.Vars;

public class TileInfoManagement {

    public WeakHashMap<Tile, TileInfo> tileInfos = new WeakHashMap<Tile, TileInfo>();

    public TileInfo getTileInfo(Tile tile) {
        return getOrCreateTileInfo(tile);
    }

    public TileInfoManagement() {
        Events.on(ConfigEvent.class, this::handleConfigEvent);
        Events.on(DepositEvent.class, this::handleDepositEvent);
        Events.on(WithdrawEvent.class, this::handleWithdrawEvent);
        Events.on(PickupEvent.class, this::handlePickupEvent);
        // Events.on(BlockBuildBeginEvent.class, this::handleBlockBuildBeginEvent);
        // Events.on(BuildSelectEvent.class, this::handleBuildSelectEvent);
        // Events.on(BlockBuildEndEvent.class, this::handleBlockBuildEndEvent);
        // Events.on(TapEvent.class, this::handleTapEvent);
    }

    void handleConfigEvent(ConfigEvent e) {
        Player player = e.player;
        Building building = e.tile;
        addInfo(player, TileInfo.ActionType.configure, building.tile(), building.block());
    }

    void handleDepositEvent(DepositEvent e) {
        Player player = e.player;
        Building building = e.tile;
        addInfo(player, TileInfo.ActionType.depositItem, building.tile(), building.block());
    }

    void handleWithdrawEvent(WithdrawEvent e) {
        Player player = e.player;
        Building building = e.tile;
        addInfo(player, TileInfo.ActionType.withdrawItem, building.tile(), building.block());
    }

    void handlePickupEvent(PickupEvent e) {
        if (!e.carrier.isPlayer() || e.build == null) {
            return;
        }
        Player player = e.carrier.getPlayer();
        Building building = e.build;
        addInfo(player, TileInfo.ActionType.pickupBlock, building.tile(), building.block());
    }

    void handleBlockBuildBeginEvent(BlockBuildBeginEvent e) {

        if (e.unit == null || !e.unit.isPlayer()) {
            return;
        }
        Tile tile = e.tile;
        Player player = e.unit.getPlayer();

        Block block = null;

        if (tile.build != null) {
            if (tile.build instanceof ConstructBuild) {
                ConstructBuild cbuild = (ConstructBuild) (tile.build);
                /*
                 * if (tileInfos.get(tile) == null && cbuild.previous != null) {
                 * getOrCreateTileInfo(tile).add(null, TileInfo.ActionType.placeBlock,
                 * cbuild.previous); mindustry.Vars.ui.scriptfrag.addMessage("0"); }
                 */

                if (cbuild.cblock != null) {
                    block = cbuild.cblock;
                } else {
                    block = cbuild.block;
                }
            } else {
                block = tile.build.block();
            }
        } else if (tile.block() != null) {
            block = tile.block();
        } else if (e.unit.buildPlan() != null) {
            block = e.unit.buildPlan().block;
        }

        if (e.breaking) {
            addInfo(player, TileInfo.ActionType.breakBlock, tile, block);
        } else {
            addInfo(player, TileInfo.ActionType.placeBlock, tile, block);
        }
    }

    void handleBlockBuildEndEvent(BlockBuildEndEvent e) {
        /*
         * if (e.unit == null || !e.unit.isPlayer()) { return; } Player player =
         * e.unit.getPlayer(); Tile tile = e.tile;
         * 
         * if (e.breaking) { addInfo(player, TileInfo.ActionType.breakBlock, tile,
         * null); } else { addInfo(player, TileInfo.ActionType.placeBlock, tile,
         * player.builder().buildPlan().block); }
         */
    }

    void handleBuildSelectEvent(BuildSelectEvent e) {
        /*
         * if (!e.builder.isPlayer()) { return; } Player player = e.builder.getPlayer();
         * Tile tile = e.tile; if (e.breaking) { addInfo(player,
         * TileInfo.ActionType.breakBlock, tile, null); } else { addInfo(player,
         * TileInfo.ActionType.placeBlock, tile, player.builder().buildPlan().block); }
         */
    }

    public void lastAccessedChange() {
    }

    public void addInfo(Player player, ActionType action, Tile tile, Block block) {
        if(player == null || block == null) {
            return;
        }
        if (action == ActionType.breakBlock || action == ActionType.pickupBlock) {
            checkPowerSplit(tile.build, player);
        }

        if (tile != null) {
            tile.getLinkedTiles(linked -> {
                getOrCreateTileInfo(linked).add(player, action, block);

                // mindustry.Vars.ui.chatfrag.addMessage("" + tile.x + ", " + tile.y, null);
            });
        }
    }

    private Instant lastCheckPowerTime = Instant.now();
    private final Queue<Building> queue = new Queue<>();
    private final Seq<Building> outArray1 = new Seq<>();
    private final Seq<Building> outArray2 = new Seq<>();
    private final IntSet closedSet = new IntSet();

    public boolean checkPowerSplit(Building tile, Player player) {
        if (tile == null) {
            return false;
        }
        if (!(tile.block() instanceof PowerNode)) {
            return false;
        }
        if (lastCheckPowerTime.plusSeconds(1).isAfter(Instant.now())) {
            return false;
        }
        lastCheckPowerTime = Instant.now();

        closedSet.clear();
        PowerGraph thisgraph = tile.power().graph;

        int touch = 0;
        int splitNum = 0;
        int majorSplit = 0;

        for (Building other : tile.getPowerConnections(outArray1)) {
            if (other.power().graph != thisgraph)
                continue;
            if (closedSet.contains(other.pos())) {
                continue;
            }
            touch += 1;
            splitNum = 0;
            queue.clear();
            queue.addLast(other);
            while (queue.size > 0) {
                Building child = queue.removeFirst();
                for (Building next : child.getPowerConnections(outArray2)) {
                    if (next != tile && !closedSet.contains(next.pos())) {

                        queue.addLast(next);
                        closedSet.add(next.pos());
                        splitNum += 1;
                    }
                }
            }
            if (splitNum > 50) {
                majorSplit += 1;
            }
        }
        // mindustry.Vars.ui.scriptfrag.addMessage(String.format("touch = %d, majorSplit = %d", touch, majorSplit));
        if (touch > 1 && majorSplit >= 2) {
            Vars.griefWarnings.handlePowerGraphSplit(tile.tile(), player);
            return true;
        }
        return false;
    }

    public TileInfo getOrCreateTileInfo(Tile tile) {
        TileInfo tileinfo = tileInfos.get(tile);
        if (tileinfo == null) {
            tileinfo = new TileInfo();

            tileInfos.put(tile, tileinfo);
        }
        return tileinfo;
    }

}
