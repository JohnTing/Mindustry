package mindustry.ui.dialogs;

import arc.*;
import arc.graphics.g2d.*;
import arc.scene.style.*;
import arc.scene.ui.*;
import arc.scene.ui.layout.*;
import arc.util.*;
import mindustry.*;
import mindustry.game.*;
import mindustry.gen.*;
import mindustry.graphics.*;
import mindustry.maps.*;
import mindustry.ui.*;

public class CustomGameDialog extends BaseDialog{
    private MapPlayDialog dialog = new MapPlayDialog();
    private String search = "";
    private TextField searchField;

    public CustomGameDialog(){
        super("@customgame");
        addCloseButton();
        shown(this::setup);
        onResize(this::setup);
    }

    void setup(){
        clearChildren();
        add(titleTable).growX().row();
        stack(cont, buttons).grow();
        buttons.bottom();
        cont.clear();

        Runnable[] rebuildPane = {null};
        cont.top();
        cont.table(s -> {
            s.left();
            s.image(Icon.zoom);
            searchField = s.field(search, res -> {
                search = res.toLowerCase();
                rebuildPane[0].run();
            }).growX().get();
        }).fillX().padBottom(4);
        cont.row();


        Table maps = new Table();
        maps.marginRight(14);
        maps.marginBottom(55f);
        ScrollPane pane = new ScrollPane(maps);
        int maxwidth = Math.max((int)(Core.graphics.getWidth() / Scl.scl(210)), 1);
        float images = 146f;

        rebuildPane[0] = ()-> {
        maps.clear();
        maps.marginRight(14);
        maps.marginBottom(55f);
        
        pane.setFadeScrollBars(false);       

        int i = 0;
        maps.defaults().width(170).fillY().top().pad(4f);
        for(Map map : Vars.maps.all()){

            if(!search.isEmpty() && !map.name().toLowerCase().contains(search)) {
                continue;
            }

            if(i % maxwidth == 0){
                maps.row();
            }

            ImageButton image = new ImageButton(new TextureRegion(map.safeTexture()), Styles.cleari);
            image.margin(5);
            image.top();

            Image img = image.getImage();
            img.remove();

            image.row();
            image.table(t -> {
                t.left();
                for(Gamemode mode : Gamemode.all){
                    TextureRegionDrawable icon = Vars.ui.getIcon("mode" + Strings.capitalize(mode.name()) + "Small");
                    if(mode.valid(map) && Core.atlas.isFound(icon.getRegion())){
                        t.image(icon).size(16f).pad(4f);
                    }
                }
            }).left();
            image.row();
            image.add(map.name()).pad(1f).growX().wrap().left().get().setEllipsis(true);
            image.row();
            image.image(Tex.whiteui, Pal.gray).growX().pad(3).height(4f);
            image.row();
            image.add(img).size(images);

            BorderImage border = new BorderImage(map.safeTexture(), 3f);
            border.setScaling(Scaling.fit);
            image.replaceImage(border);

            image.clicked(() -> dialog.show(map));

            maps.add(image);

            i++;
        }

        if(Vars.maps.all().size == 0){
            maps.add("@maps.none").pad(50);
        }
        };
        rebuildPane[0].run();

        cont.add(pane).uniformX();
    }
}
