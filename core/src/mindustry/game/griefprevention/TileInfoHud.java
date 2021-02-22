
package mindustry.game.griefprevention;

import arc.Core;
import arc.math.geom.Vec2;
import arc.scene.Element;
import arc.scene.event.Touchable;
import arc.scene.style.Style;
import arc.scene.ui.layout.Table;
import mindustry.Vars;
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
    invalidate();
    if (mobile) {
      label(this::hudInfo).style(Styles.outlineLabel).fontScale(0.7f);
    } else {
      label(this::hudInfo).style(Styles.outlineLabel);
    }
  }

  public String hudInfo() {


    Tile tile = getCursorTile();

    if (tile == lastTile)
      return lastOutput;

    if (tile == null)
      return lastOutput;


    if (tileInfoManagement.getOrCreateTileInfo(tile).actionList == null || tileInfoManagement.getOrCreateTileInfo(tile).actionList.size() == 0) {
      return lastOutput = "no data";
    }
    if (Vars.mobile) {
      lastOutput = tileInfoManagement.getOrCreateTileInfo(tile).toSimpleString();
    } else {
      lastOutput = tileInfoManagement.getOrCreateTileInfo(tile).toString();
    }
   
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
