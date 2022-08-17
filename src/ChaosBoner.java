import com.epicbot.api.i.a.v.A;
import com.epicbot.api.shared.APIContext;
import com.epicbot.api.shared.GameType;
import com.epicbot.api.shared.entity.ItemWidget;
import com.epicbot.api.shared.entity.SceneObject;
import com.epicbot.api.shared.model.Area;
import com.epicbot.api.shared.script.LoopScript;
import com.epicbot.api.shared.script.ScriptManifest;
import com.epicbot.api.shared.util.paint.frame.PaintFrame;
import com.epicbot.api.shared.util.time.Time;

import java.awt.*;

@ScriptManifest(name = "ChaosBoner", gameType = GameType.OS)
public class ChaosBoner extends LoopScript {

    @Override
    public boolean onStart(String... strings) {
        //check for bones and burning amulet
        return true;
    }

    private Area ALTER = new Area(2948, 3819, 2949, 3822);
    private Area LAVA_MAZE = new Area(3022, 3832, 3036, 3846);
    private Area LAVA_MAZE_SOUTH = new Area(3017, 3820, 3030, 3832);
    private Area CHAOS_FANATIC = new Area(2975, 3841, 2984, 3851);
    private Area LUMBRIDGE = new Area(3226, 3211, 3217, 3277);

    private String currentLocation = "";

    public String updateLocation(){
        if(LUMBRIDGE.contains(getAPIContext().localPlayer().getLocation())){
            currentLocation = "LUMBRIDGE";
        } else if (LAVA_MAZE.contains(getAPIContext().localPlayer().getLocation())){
            currentLocation = "LAVA_MAZE";
        } else if (LAVA_MAZE_SOUTH.contains(getAPIContext().localPlayer().getLocation())){
            currentLocation = "LAVA_MAZE_SOUTH";
        } else if (ALTER.contains(getAPIContext().localPlayer().getLocation())){
            currentLocation = "ALTER";
        } else if (CHAOS_FANATIC.contains(getAPIContext().localPlayer().getLocation())){
            currentLocation = "CHAOS_FANATIC";
        } else {
            currentLocation = "TRAVELLING";
        }
        return currentLocation;
    }

    private boolean doesNeedsBones(){
        System.out.println("Checking if needs bones");
        if(getAPIContext().inventory().contains(526) && getAPIContext().inventory().contains(setBurningAmmyId())){
            if(getAPIContext().inventory().getCount(526) == 2){
                return false;
            } else if (getAPIContext().inventory().getCount(526) != 2){
                return true;
            }
        }
        return true;
    }

    private void bankForBones(){
        getAPIContext().bank().open();
        if(!getAPIContext().inventory().contains(21166, 21169, 21171,21173, 21175)){
            if(getAPIContext().bank().contains(21166)){
                getAPIContext().bank().withdraw(1, 21166);
            } else if(getAPIContext().bank().contains(21169)){
                getAPIContext().bank().withdraw(1, 21169);
            } else if(getAPIContext().bank().contains(21171)){
                getAPIContext().bank().withdraw(1, 21171);
            } else if(getAPIContext().bank().contains(21173)){
                getAPIContext().bank().withdraw(1, 21173);
            } else if(getAPIContext().bank().contains(21175)){
                getAPIContext().bank().withdraw(1, 21175);
            }
            getAPIContext().bank().withdraw(2, 526);
        } else {
            getAPIContext().bank().withdraw(2, 526);
        }

        if(getAPIContext().bank().isOpen()){
            getAPIContext().bank().close();
        }
    }

    private void useBones(){
        SceneObject alter = getAPIContext().objects().query().id(411).results().nearest();
        System.out.println("Using Bones on alter");
        getAPIContext().inventory().getItem(526).interact("Use");
        alter.click();
    }

    private int setBurningAmmyId(){
        return getAPIContext().inventory().getItemAt(0).getId();
    }

    private void travelToAlter(){
        if(currentLocation != "ALTER" && !doesNeedsBones()){
            ItemWidget amulet = getAPIContext().inventory().getItem(setBurningAmmyId());
            amulet.interact("Rub");
            Time.sleep(5000, () -> getAPIContext().dialogues().isDialogueOpen());
            getAPIContext().dialogues().selectOption(3);
            Time.sleep(5000, () -> getAPIContext().dialogues().hasOption("Okay, teleport to level 41 Wilderness."));
            getAPIContext().dialogues().selectOption(1);
            System.out.println("Sleeping till in wilderness");
            Time.sleep(10000, () -> getAPIContext().localPlayer().isInWilderness());
            System.out.println("Walking South");
            getAPIContext().webWalking().walkTo(LAVA_MAZE_SOUTH.getRandomTile());
            System.out.println("Arrived South");
            Time.sleep(10000, () -> !getAPIContext().localPlayer().isMoving());
            System.out.println("Walking to ALTER");
            getAPIContext().webWalking().walkTo(ALTER.getRandomTile());
        } else {
            return;
        }
    }

    private void suicide(){
        System.out.println("An hero");
        getAPIContext().webWalking().walkTo(CHAOS_FANATIC.getRandomTile());
    }

    @Override
    protected int loop() {
        updateLocation();
        if(currentLocation == "LUMBRIDGE" && doesNeedsBones()){
            bankForBones();
        } else if (currentLocation == "LUMBRIDGE" && !doesNeedsBones()) {
            travelToAlter();
        }

        if(currentLocation == "ALTER" && getAPIContext().inventory().contains(526)){
            useBones();
            System.out.println("Sleeping till all bones gone or level up");
            Time.sleep(999999, () -> !getAPIContext().inventory().contains(526) || getAPIContext().dialogues().isDialogueOpen());
        } else if(currentLocation == "ALTER" && !getAPIContext().inventory().contains(526)){
            suicide();
        }

        return 600;
    }

    @Override
    protected void onPaint(Graphics2D g, APIContext ctx) {
        PaintFrame frame = new PaintFrame("ChaosBoner");
        frame.addLine("Title", "Value");
        frame.draw(g, 0, 170, ctx);
    }
}