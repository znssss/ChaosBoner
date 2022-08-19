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
    private Area LAVA_MAZE_SOUTH = new Area(3021, 3823, 3016, 3818);
    private Area CHAOS_FANATIC = new Area(2978, 3844, 2985, 3851);
    private Area LUMBRIDGE = new Area(3217, 3210, 3226, 3228);

    private String currentLocation = "";
    private String prevLocation = "";

    private int boneId = 536;
    private int bonesPerInv = 27;

    private int loopSpeed = 600;

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
        if(getAPIContext().inventory().contains(boneId) && getAPIContext().inventory().contains(setBurningAmmyId())){
            if(getAPIContext().inventory().getCount(boneId) == bonesPerInv){
                return false;
            } else if (getAPIContext().inventory().getCount(boneId) != bonesPerInv){
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
        }
        getAPIContext().bank().withdraw(bonesPerInv, boneId);
        getAPIContext().bank().close();
    }

    private void useBones(){
        SceneObject alter = getAPIContext().objects().query().id(411).results().nearest();
        System.out.println("Using Bones on alter");
        getAPIContext().inventory().getItem(boneId).interact("Use");
        alter.click();
        System.out.println("Sleeping till all bones gone or level up");
        Time.sleep(20000, () -> !getAPIContext().inventory().contains(boneId) || getAPIContext().dialogues().isDialogueOpen());
    }

    private int setBurningAmmyId(){
        return getAPIContext().inventory().getItemAt(0).getId();
    }

    private void travelToWilderness(){
        ItemWidget amulet = getAPIContext().inventory().getItem(setBurningAmmyId());
        if(currentLocation == "LUMBRIDGE" && !doesNeedsBones() && !getAPIContext().dialogues().isDialogueOpen()) {
            System.out.println("Rubbing ammy");
            amulet.interact("Rub");
        } else if(getAPIContext().dialogues().isDialogueOpen() && getAPIContext().dialogues().hasOptionContaining("Lava")){
            System.out.println("Selecting Lava Maze");
            getAPIContext().dialogues().selectOption(3);
        } else if(getAPIContext().dialogues().isDialogueOpen() && getAPIContext().dialogues().hasOptionContaining("Okay")){
            System.out.println("Selecting Okay");
            getAPIContext().dialogues().selectOption(1);
        }
    }

    private void travelSouth(){
        System.out.println("Arrived at Lava Maze, running south");
        getAPIContext().webWalking().walkTo(LAVA_MAZE_SOUTH.getRandomTile());
    }

    private void travelToAlter(){
        System.out.println("Walking to ALTER");
        getAPIContext().webWalking().walkTo(ALTER.getRandomTile());
    }

    private void suicide(){
        System.out.println("You are an hero " + getAPIContext().client().getUsername());
        getAPIContext().webWalking().walkTo(CHAOS_FANATIC.getRandomTile());
    }

    private void recoverBadWebWalk(){
        System.out.println("Recovering from bad web walk");
        if(prevLocation == "LUMBRIDGE"){
            travelToAlter();
        } else if(prevLocation == "LAVA_MAZE" && getAPIContext().inventory().contains(boneId)){
            travelToAlter();
        } else if(prevLocation == "LAVA_MAZE" && !getAPIContext().inventory().contains(boneId)) {
            suicide();
        }
        else if(prevLocation == "ALTER" && getAPIContext().inventory().contains(boneId)){
            travelToAlter();
        } else if(prevLocation == "ALTER" && !getAPIContext().inventory().contains(boneId)){
            suicide();
        }
    }

    @Override
    protected int loop() {
        System.out.println("Current loc: " + currentLocation);
        System.out.println("Prev loc: " + prevLocation);
        updateLocation();
        if(currentLocation == "LUMBRIDGE" && doesNeedsBones()){
            prevLocation = "ALTER";
            bankForBones();
        } else if (currentLocation == "LUMBRIDGE" && !doesNeedsBones() && !getAPIContext().localPlayer().isAnimating()) {
            prevLocation = "CHAOS_FANATIC";
            getAPIContext().bank().close();
            travelToWilderness();
        } else if(getAPIContext().localPlayer().isInWilderness() && currentLocation == "LAVA_MAZE"){
            prevLocation = "LUMBRIDGE";
            travelSouth();
        } else if(getAPIContext().localPlayer().isInWilderness() && currentLocation == "LAVA_MAZE_SOUTH"){
            prevLocation = "LAVA_MAZE";
            travelToAlter();
        } else if(currentLocation == "ALTER" && getAPIContext().inventory().contains(boneId)){
            prevLocation = "LAVA_MAZE_SOUTH";
//            loopSpeed = 50;
            useBones();
        } else if(currentLocation == "ALTER" && !getAPIContext().inventory().contains(boneId)){
//            loopSpeed = 600;
            suicide();
        } else if(currentLocation == "CHAOS_FANATIC"){
            prevLocation = "ALTER";
        } else if(currentLocation == "TRAVELLING" && !getAPIContext().localPlayer().isAnimating()){
            recoverBadWebWalk();
        }
        return loopSpeed;
    }

    @Override
    protected void onPaint(Graphics2D g, APIContext ctx) {
        PaintFrame frame = new PaintFrame("ChaosBoner");
        frame.addLine("Title", "Value");
        frame.draw(g, 0, 170, ctx);
    }
}