package mindustry.game.griefprevention;

import arc.Core;
import arc.math.geom.Vec2;
import arc.scene.Element;
import arc.scene.event.Touchable;
import arc.scene.style.Style;
import arc.scene.ui.layout.Table;
import mindustry.gen.Tex;
import mindustry.ui.Styles;
import mindustry.world.Tile;

import static mindustry.Vars.*;

import java.util.List;


public class TileInfoHud extends Table {
    private Tile lastTile = null;
    private String lastOutput = "No data";

    public TileInfoHud() {
        touchable(Touchable.disabled);
        //background(Tex.pane);
        background(Tex.clear);
        //label(this::hudInfo);
        label(this::hudInfo).style(Styles.outlineLabel);

    }

    private void touchable(Touchable disabled) {
    }

    public String hudInfo() {
        Tile tile = getCursorTile();
        if (tile == lastTile) return lastOutput;
        if (tile == null) return lastOutput = "No data";
        return lastOutput = String.join("\n", tileInfo(tile));
    }

    public Tile getCursorTile() {
      Vec2 vec = Core.input.mouseWorld(Core.input.mouseX(), Core.input.mouseY());
      return world.tile(world.toTile(vec.x), world.toTile(vec.y));
  }

  public List<String> tileInfo(Tile tile) {
    return null;
}



}
