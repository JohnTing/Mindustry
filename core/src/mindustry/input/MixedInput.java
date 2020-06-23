package mindustry.input;

import arc.*;
import arc.graphics.Color;
import arc.graphics.g2d.*;
import arc.input.KeyCode;
import arc.math.*;

import arc.util.*;

import mindustry.core.GameState.*;
import mindustry.entities.Units;
import mindustry.entities.traits.TargetTrait;
import mindustry.entities.type.TileEntity;
import mindustry.entities.type.Unit;
import mindustry.game.griefprevention.Auto.CamMode;
import mindustry.graphics.*;
import mindustry.world.*;
import static mindustry.Vars.*;

import mindustry.entities.traits.BuilderTrait.BuildRequest;

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
    
    boolean tryTileTapped(Tile tile) {
      tile = tile.link();

        boolean consumed = false;

        //check if tapped block is configurable
        if(tile.block().configurable && tile.interactable(player.getTeam())){
            consumed = true;
        }else if(!frag.config.hasConfigMouse()){ //make sure a configuration fragment isn't on the cursor
            //then, if it's shown and the current block 'agrees' to hide, hide it.
            if(frag.config.isShown() && frag.config.getSelectedTile().block().onConfigureTileTapped(frag.config.getSelectedTile(), tile)){
                consumed = true;
            }
            if(frag.config.isShown()){
                consumed = true;
            }
        }
        //consume tap event if necessary
        if(tile.interactable(player.getTeam()) && tile.block().consumesTap){
            consumed = true;
        }else if(tile.interactable(player.getTeam()) && tile.block().synthetic() && !consumed){
            if(tile.block().hasItems && tile.entity.items.total() > 0){
                consumed = true;
            }
        }
        return consumed;
    }

    public boolean checkShooting() {
      if(Core.input.keyTap(Binding.select) && !Core.scene.hasMouse()){
        
        //int cursorX = tileX(Core.input.mouseX());
        //int cursorY = tileY(Core.input.mouseY());
        //BuildRequest req = getRequest(cursorX, cursorY);
        Tile selected = tileAt(Core.input.mouseX(), Core.input.mouseY());

        if(Core.input.keyTap(Binding.break_block)){
        }else if(!selectRequests.isEmpty()){
        }else if(isPlacing()){
        // }else if(req != null && !req.breaking && mode == none && !req.initialized){
        //}else if(req != null && !req.breaking && !req.initialized){
        //}else if(req != null && req.breaking){
        }else if(selected != null){
            //only begin shooting if there's no cursor event
            if(!tryTileTapped(selected) && !tryTapPlayer(Core.input.mouseWorld().x, Core.input.mouseWorld().y) && (player.buildQueue().size == 0 || !player.isBuilding) && !droppingItem &&
            !tryBeginMine(selected) && player.getMineTile() == null && !Core.scene.hasKeyboard()){
              return true;
            }
        }else if(!Core.scene.hasKeyboard()){ //if it's out of bounds, shooting is just fine
          return true;
        }
      }
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
            Draw.color(Color.toFloatBits(1f, 0, 0, 0.9f));
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
