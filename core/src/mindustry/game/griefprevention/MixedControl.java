package mindustry.game.griefprevention;

import mindustry.entities.Predict;
import mindustry.entities.Units;
import mindustry.entities.traits.TargetTrait;
import mindustry.entities.type.Player;
import mindustry.entities.type.SolidEntity;
import mindustry.entities.type.TileEntity;
import mindustry.game.Team;
import mindustry.input.Binding;
import mindustry.input.MixedInput;
import mindustry.type.Weapon;
import mindustry.world.Tile;
import mindustry.world.blocks.BuildBlock;
import arc.Core;
import arc.math.Angles;
import arc.math.Mathf;
import arc.math.geom.Geometry;
import arc.math.geom.Rect;
import arc.math.geom.Vec2;
import arc.scene.ui.TextField;
import arc.util.Time;


import static mindustry.Vars.*;

import java.lang.reflect.Field;

public class MixedControl {
  public Rect rect = new Rect();
  
  public boolean moved;
  public Vec2 movement;
  public Vec2 velocity;

  MixedControl() {
    try {
        Class<Player> playerClass = Player.class;
        Field playerMovementField = playerClass.getDeclaredField("movement");
        playerMovementField.setAccessible(true);
        movement = (Vec2)playerMovementField.get(player);
    } catch (NoSuchFieldException | IllegalAccessException ex) {
        throw new RuntimeException("reflective access failed on Player.movement");
    }
    try {
        Class<SolidEntity> solidEntityClass = SolidEntity.class;
        Field playerVelocityField = solidEntityClass.getDeclaredField("velocity");
        playerVelocityField.setAccessible(true);
        velocity = (Vec2)playerVelocityField.get(player);
    } catch (NoSuchFieldException | IllegalAccessException ex) {
        throw new RuntimeException("reflective access failed on SolidEntity.velocity");
    }
  }

  float dst(TargetTrait target) {
    return player.dst(target);
  }
  float dst(TargetTrait target, TargetTrait target2) {
    return target.dst(target2);
  }

  float dst(float target, float target2) {
   return player.dst(target, target2);
  }

  float angleTo(TargetTrait target) {
    return player.angleTo(target);
  }
  boolean isDead() {
    return player.isDead();
  }
  public void hitbox(Rect rect){
    rect.setSize(player.mech.hitsize).setCenter(player.x, player.y);
}

  public void updateVelocityStatus() {
    player.updateVelocityStatus();
  }
  public boolean isBuilding() {
    return player.isBuilding();
  }
  public Tile getMineTile() {
    return player.getMineTile();
  }

  public Weapon getWeapon(){
      return player.mech.weapon;
  }

  public void setMineTile(Tile tile){
    player.setMineTile(tile);
  }

  public boolean isShooting(){
    return player.isShooting();
  }

  public void keyboardMovement() {
    Tile tile = world.tileWorld(player.x, player.y);
    // boolean canMove = (!Core.scene.hasKeyboard() || ui.minimapfrag.shown()) && !griefWarnings.auto.movementOverride();
    boolean canMove = (!Core.scene.hasKeyboard() || ui.minimapfrag.shown()) && griefWarnings.auto.canMove();
    player.isBoosting = Core.input.keyDown(Binding.dash) && !player.mech.flying;

    //if player is in solid block
    if(tile != null && tile.solid()){
      player.isBoosting = true;
    }

    float speed = player.isBoosting && !player.mech.flying ? player.mech.boostSpeed : player.mech.speed;
    /*
    if(player.mech.flying){
        //prevent strafing backwards, have a penalty for doing so
        float penalty = 0.2f; //when going 180 degrees backwards, reduce speed to 0.2x
        speed *= Mathf.lerp(1f, penalty, Angles.angleDist(player.rotation, velocity.angle()) / 180f);
    }
    */
    movement.setZero();

    float xa = Core.input.axis(Binding.move_x);
    float ya = Core.input.axis(Binding.move_y);
    if(!(Core.scene.getKeyboardFocus() instanceof TextField)){
        movement.y += ya * speed;
        movement.x += xa * speed;
    }

    if(Core.input.keyDown(Binding.mouse_move)){
      movement.x += Mathf.clamp((Core.input.mouseX() - Core.graphics.getWidth() / 2f) * 0.005f, -1, 1) * speed;
      movement.y += Mathf.clamp((Core.input.mouseY() - Core.graphics.getHeight() / 2f) * 0.005f, -1, 1) * speed;
    }
    movement.limit(speed).scl(Time.delta());

    if(canMove){
      velocity.add(movement.x, movement.y);
  }
    float prex = player.x, prey = player.y;
    //updateVelocityStatus();
    moved = dst(prex, prey) > 0.001f;

    movement.limit(speed).scl(Time.delta());

  }
  boolean forcedShooting = false;
  public void keyboardShooting() {

    if (Core.input.keyTap(Binding.select) && !Core.scene.hasMouse() ) {
      if(control.input != null && control.input instanceof MixedInput) {
        if(control.input.canShoot() && ((MixedInput)control.input).checkShooting()) {
          forcedShooting = true;
        }
      }
    }
    
    if(forcedShooting) {
      Vec2 vec = Core.input.mouseWorld(control.input.getMouseX(), control.input.getMouseY());
      player.pointerX = vec.x;
      player.pointerY = vec.y;
    }
    player.isShooting = forcedShooting;


    if (Core.input.keyRelease(Binding.select)) {
      forcedShooting = false;
      player.isShooting = false;
      return;
    }
  }

  public void keyboardRotation() {
    boolean canMove = (!Core.scene.hasKeyboard() || ui.minimapfrag.shown()) && griefWarnings.auto.canMove();
    if(canMove){
      float baseLerp = player.mech.getRotationAlpha(player);
      if(!Core.input.keyDown(Binding.select) || !player.mech.turnCursor){
          if(!movement.isZero()){
            player.rotation = Mathf.slerpDelta(player.rotation, player.mech.flying ? velocity.angle() : movement.angle(), 0.13f * baseLerp);
          }
      }else if (Core.input.keyDown(Binding.select) && player.isShooting()){
          float angle = control.input.mouseAngle(player.x, player.y);
          player.rotation = Mathf.slerpDelta(player.rotation, angle, 0.1f * baseLerp);
      }
    }
  }

  public void touchRotation() {
    float baseLerp = player.mech.getRotationAlpha(player);
    if(isShooting()) {
      Vec2 vec = new Vec2(player.x, player.y);
      float angle = vec.angleTo(player.pointerX, player.pointerY);
      player.rotation = Mathf.slerpDelta(player.rotation, angle, 0.1f * baseLerp);
    }
    if(velocity.len() <= 0.2f && player.mech.flying){
      player.rotation += Mathf.sin(Time.time() + player.id * 99, 10f, 1f);
    }else if(player.target == null){
      player.rotation = Mathf.slerpDelta(player.rotation, velocity.angle(), velocity.len() / 10f);
    }
  }

  public void touchMovement() {
    float targetX = Core.camera.position.x, targetY = Core.camera.position.y;
    float attractDst = 15f;
    float speed = player.isBoosting && !player.mech.flying ? player.mech.boostSpeed : player.mech.speed;
    movement.set((targetX - player.x) / Time.delta(), (targetY - player.y) / Time.delta()).limit(speed);
    movement.setAngle(Mathf.slerp(movement.angle(), velocity.angle(), 0.05f));

    float dstRatio = dst(targetX, targetY) / attractDst; 
    if(dst(targetX, targetY) < attractDst){

      velocity.angleTo(targetX - player.x, targetX - player.x);
      velocity.scl(Mathf.clamp(dstRatio + 0.2f, 0f, 1f) * Time.delta());
      
      if ( dst(targetX, targetY) < 2f) {
        velocity.setZero();
      }
    }

    float expansion = 3f;

    hitbox(rect);
    rect.x -= expansion;
    rect.y -= expansion;
    rect.width += expansion * 2f;
    rect.height += expansion * 2f;

    player.isBoosting = collisions.overlapsTile(rect) || dst(targetX, targetY) > 85f;

    velocity.add(movement.scl(Time.delta()));
    
    

    float lx = player.x, ly = player.y;
    //updateVelocityStatus();
    moved = dst(lx, ly) > 0.001f;

    if(player.mech.flying){
        //hovering effect
        player.x += Mathf.sin(Time.time() + player.id * 999, 25f, 0.08f);
        player.y += Mathf.cos(Time.time() + player.id * 999, 25f, 0.08f);
    }
  }

  public void touchShooting() {
    if(!(!isBuilding() && getMineTile() == null)) {
      return;
    }

    if (player.target != null) {

      if(Units.invalidateTarget(player.target, player) &&
      !(player.target instanceof TileEntity && ((TileEntity)player.target).damaged() && player.target.isValid() && player.target.getTeam() == player.getTeam() && player.mech.canHeal && player.dst(player.target) + 4f < getWeapon().bullet.range() && !(((TileEntity)player.target).block instanceof BuildBlock))){
        player.target = null;
      }

      if(state.isEditor()){
        player.target = null;
      }
    }

    if(player.target == null){
      player.isShooting = false;
        if(Core.settings.getBool("autotarget")){
          player.target = Units.closestTarget(player.getTeam(), player.x, player.y, getWeapon().bullet.range(), u -> u.getTeam() != Team.derelict, u -> u.getTeam() != Team.derelict);

            if(player.mech.canHeal && player.target == null){
              player.target = Geometry.findClosest(player.x, player.y, indexer.getDamaged(Team.sharded));
                if(player.target != null && dst(player.target) > getWeapon().bullet.range()){
                  player.target = null;
                }else if(player.target != null){
                  player.target = ((Tile)player.target).entity;
                }
            }
            if(player.target != null){
                setMineTile(null);
            }
        }
    }else if(player.target.isValid() || (player.target instanceof TileEntity && ((TileEntity)player.target).damaged() && player.target.getTeam() == player.getTeam() &&
    player.mech.canHeal && dst(player.target) < getWeapon().bullet.range())){

        Vec2 intercept = Predict.intercept(player, player.target, getWeapon().bullet.speed);

        player.pointerX = intercept.x;
        player.pointerY = intercept.y;

        player.isShooting = true;
    }
  }
}
