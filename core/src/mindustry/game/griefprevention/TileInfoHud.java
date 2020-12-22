
package mindustry.game.griefprevention;

import arc.Core;
import arc.math.geom.Vec2;
import arc.scene.Element;
import arc.scene.event.Touchable;
import arc.scene.style.Style;
import arc.scene.ui.layout.Table;
import mindustry.core.World;
import mindustry.game.griefprevention.TileInfo.TileAction;
import mindustry.gen.Tex;
import mindustry.ui.Styles;
import mindustry.world.Tile;

import static mindustry.Vars.*;

public class TileInfoHud extends Table {
  private Tile lastTile = null;
  private String lastOutput = "No data";

  public TileInfoHud() {
    background(Tex.clear);
    label(this::hudInfo).style(Styles.outlineLabel);

  }

  public String hudInfo() {
    Tile tile = getCursorTile();

    if (tile == lastTile)
      return lastOutput;

    if (tile == null)
      return lastOutput;


    if (tileInfoManagement.getOrCreateTileInfo(tile).actionList == null || tileInfoManagement.getOrCreateTileInfo(tile).actionList.size() == 0) {
      return lastOutput = formatTile(tile);
    }

    StringBuilder sb = new StringBuilder();
    sb.append(formatTile(tile));
    sb.append("\n");
    for (TileAction ta : tileInfoManagement.getOrCreateTileInfo(tile).actionList) {
      sb.append(ta.toString());
      sb.append("\n");
    }
    lastOutput = sb.toString();
    return lastOutput;
  }

  public Tile getCursorTile() {
    Vec2 vec = Core.input.mouseWorld(Core.input.mouseX(), Core.input.mouseY());
    return world.tile(World.toTile(vec.x), World.toTile(vec.y));
  }

  public String formatTile(Tile tile) {
    if (tile == null) return "(none)";
    return "(" + tile.x + ", " + tile.y + ")";
}

}
