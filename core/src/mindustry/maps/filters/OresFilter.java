package mindustry.maps.filters;

import arc.math.Mathf;
import arc.util.*;
import mindustry.content.*;
import mindustry.world.*;

import static mindustry.maps.filters.FilterOption.*;

public class OresFilter extends GenerateFilter{
    public float scl = 23, threshold = 0.81f, octaves = 2f, falloff = 0.3f;

    float[] oreRatios = new float[6];
    Block[] ores = {Blocks.oreCopper, Blocks.oreLead, Blocks.oreCoal, Blocks.oreTitanium, Blocks.oreThorium, Blocks.oreScrap};
    
    float distSum = 0;
    public Block ore = Blocks.oreCopper, target = Blocks.air;

    @Override
    public FilterOption[] options(){
        return Structs.arr(
        new SliderOption("scale", () -> scl, f -> scl = f, 1f, 500f),
        new SliderOption("threshold", () -> threshold, f -> threshold = f, 0f, 1f),
        new SliderOption("octaves", () -> octaves, f -> octaves = f, 1f, 10f),
        new SliderOption("falloff", () -> falloff, f -> falloff = f, 0f, 1f),
        new SliderOption("copper", () -> oreRatios[0], f -> oreRatios[0] = f, 0f, 1f),
        new SliderOption("lead", () -> oreRatios[1], f -> oreRatios[1] = f, 0f, 1f),
        new SliderOption("coal", () -> oreRatios[2], f -> oreRatios[2] = f, 0f, 1f),
        new SliderOption("titanium", () -> oreRatios[3], f -> oreRatios[3] = f, 0f, 1f),
        new SliderOption("thorium", () -> oreRatios[4], f -> oreRatios[4] = f, 0f, 1f),
        new SliderOption("scrap", () -> oreRatios[5], f -> oreRatios[5] = f, 0f, 1f),

        new BlockOption("target", () -> target, b -> target = b, oresFloorsOptional)
        );
    }

    @Override
    public void apply(){
        float noise = noise(in.x, in.y, scl, 1f, octaves, falloff);
        if(distSum <= 0) {
            for(float a : oreRatios) {
                distSum += a;
            }
            if(distSum <= 0) {
                return;
            }
        }
        float rand = Mathf.random();
        float ratio = 1.0f / distSum;

        double tempDist = 0;
        for (int i = 0; i < oreRatios.length;i++) {
            tempDist += oreRatios[i];
            if (rand / ratio <= tempDist) {
                ore = ores[i];
            }
        }



        if(noise > threshold && in.overlay != Blocks.spawn && (target == Blocks.air || in.floor == target || in.overlay == target) && in.floor.asFloor().hasSurface()){

            in.overlay = ore;
        }
    }
}
