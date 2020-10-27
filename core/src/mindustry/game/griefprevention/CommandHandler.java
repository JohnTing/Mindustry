package mindustry.game.griefprevention;

import arc.Core;
import arc.math.geom.Vec2;
import arc.func.Cons;
import arc.util.Log;
import mindustry.gen.Call;
import mindustry.net.Packets.AdminAction;
import mindustry.type.Item;
import mindustry.world.Block;
import mindustry.world.Tile;

import static mindustry.Vars.*;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map.Entry;

// introducing the worst command system known to mankind
public class CommandHandler {
    public static class CommandContext {
        public List<String> args;

        public CommandContext(List<String> args) {
            this.args = args;
        }
    }

    public HashMap<String, Cons<CommandContext>> commands = new HashMap<>();

    public CommandHandler() {

    }

}
