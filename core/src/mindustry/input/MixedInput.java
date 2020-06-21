package mindustry.input;

import arc.*;
import arc.Graphics.*;
import arc.Graphics.Cursor.*;
import arc.graphics.Color;
import arc.graphics.g2d.*;
import arc.input.KeyCode;
import arc.math.*;
import arc.scene.*;
import arc.scene.event.*;
import arc.scene.ui.*;
import arc.scene.ui.layout.*;
import arc.util.ArcAnnotate.*;
import arc.util.*;
import mindustry.*;
import mindustry.core.GameState.*;
import mindustry.entities.Units;
import mindustry.entities.traits.TargetTrait;
import mindustry.entities.traits.BuilderTrait.*;
import mindustry.entities.type.TileEntity;
import mindustry.entities.type.Unit;
import mindustry.game.EventType.*;
import mindustry.game.griefprevention.Auto.CamMode;
import mindustry.game.*;
import mindustry.gen.*;
import mindustry.graphics.*;
import mindustry.ui.*;
import mindustry.world.*;

import static arc.Core.scene;
import static mindustry.Vars.*;
import static mindustry.input.PlaceMode.*;

public class MixedInput extends DesktopInput{
    
    @Override
    public boolean tap(float x, float y, int count, KeyCode button) {
      if(state.is(State.menu)) return false;

        float worldx = Core.input.mouseWorld(x, y).x, worldy = Core.input.mouseWorld(x, y).y;

        //get tile on cursor
        Tile cursor = tileAt(x, y);

        //ignore off-screen taps
        if(cursor == null || Core.scene.hasMouse(x, y)) return false;

        checkTargets(worldx, worldy);
        return false;
    }
    
    void checkTargets(float x, float y){
      player.target = null;
      Unit unit = Units.closestEnemy(player.getTeam(), x, y, 20f, u -> !u.isDead());

      if(unit != null){
          player.setMineTile(null);
          player.target = unit;
      }else{
          Tile tile = world.ltileWorld(x, y);

          if(tile != null && tile.synthetic() && player.getTeam().isEnemy(tile.getTeam())){
              TileEntity entity = tile.entity;
              player.setMineTile(null);
              player.target = entity;
          }else if(tile != null && player.mech.canHeal && tile.entity != null && tile.getTeam() == player.getTeam() && tile.entity.damaged()){
              player.setMineTile(null);
              player.target = tile.entity;
          }
      }
    }

    private float crosshairScale;
    private TargetTrait lastTarget;
    @Override
    public void drawBottom(){
        Lines.stroke(1f);
        TargetTrait target = player.target;

        //draw targeting crosshair
        if(target != null && !state.isEditor()){
            if(target != lastTarget){
                crosshairScale = 0f;
                lastTarget = target;
            }

            crosshairScale = Mathf.lerpDelta(crosshairScale, 1f, 0.2f);

            Draw.color(Pal.remove);
            Lines.stroke(1f);

            float radius = Interpolation.swingIn.apply(crosshairScale);

            Lines.poly(target.getX(), target.getY(), 4, 7f * radius, Time.time() * 1.5f);
            Lines.spikes(target.getX(), target.getY(), 3f * radius, 6f * radius, 4, Time.time() * 1.5f);
        }
        Draw.color(Color.toFloatBits(1f, 0, 0, 0.3f));
        
        if (griefWarnings.auto.freecam != CamMode.None) {
          
          if (griefWarnings.auto.freecam != CamMode.Free) {
            Draw.color(Color.toFloatBits(1f, 1f, 0, 0.5f));
          }
          else if (Core.input.keyDown(Binding.freecam_slowmove)) {
            Draw.color(Color.toFloatBits(1f, 0, 0, 0.7f));
          }
          else {
            Draw.color(Color.toFloatBits(1f, 0, 0, 0.3f));
          }
            Lines.stroke(1f);
            float radius = Interpolation.swingIn.apply(1.1f);

            float spikesTime = Time.time();

            if(Core.settings.getBool("autotarget")) {
              Lines.poly(Core.camera.position.x, Core.camera.position.y, 4, 6f * radius, spikesTime * 1.5f);
            }
            else {
              Lines.circle(Core.camera.position.x, Core.camera.position.y, radius * 5f);
            }

            Lines.spikes(Core.camera.position.x, Core.camera.position.y, 4f * radius, 6f * radius, 4, spikesTime * 1.5f);
        }
        Draw.reset();
    }
}
