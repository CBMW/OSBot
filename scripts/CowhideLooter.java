package scripts;

import states.CowhideLooterStates;


import utils.Walker;

import org.osbot.rs07.api.ui.World;
import org.osbot.rs07.api.model.GroundItem;
import org.osbot.rs07.api.map.Area;
import org.osbot.rs07.api.map.Position;
import org.osbot.rs07.script.Script;
import org.osbot.rs07.script.ScriptManifest;
import org.osbot.rs07.utility.ConditionalSleep;

import java.awt.*;
import java.util.List;
import java.util.Random;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;


@ScriptManifest(name = "Cowhide Looter", author = "Dubai", version = 2.0, info = "Loots cowhides in Lumbridge", logo = "")
public class CowhideLooter extends Script {

    public enum State {
        WALKING_TO_COWS, LOOTING, CHANGING_AREA, HOPPING_WORLDS, WALKING_TO_BANK, DEPOSITING
    }

    private CowhideLooterStates stateHandler; // State handler
    public final Walker walker = new Walker(this);

    public int cowhidesLooted = 0;
    public long startTime;

    // Areas
    public final Area area1 = new Area(new Position(3253, 3255, 0), new Position(3265, 3268, 0));
    public final Area area2 = new Area(new Position(3254, 3269, 0), new Position(3265, 3295, 0));
    public final Area lumbridgeBank = new Area(new Position(3207, 3217, 2), new Position(3210, 3220, 2));

    @Override
    public void onStart() {
        log("Cowhide Looter script started.");
        startTime = System.currentTimeMillis();
        stateHandler = new CowhideLooterStates(this); // Initialize state handler
        determineStartState();
    }

    @Override
    public int onLoop() throws InterruptedException {
        return stateHandler.handleState(); // Delegate state handling
    }

    @Override
    public void onExit() {
        log("Cowhide Looter script ended.");
    }

    @Override
    public void onPaint(Graphics2D g) {
        g.setColor(Color.WHITE);
        g.setFont(new Font("Arial", Font.PLAIN, 12));
        long runtime = System.currentTimeMillis() - startTime;
        g.drawString("Cowhides looted: " + cowhidesLooted, 10, 30);
        g.drawString("Runtime: " + formatTime(runtime), 10, 45);
    }

    public void determineStartState() {
        if (getInventory().isFull()) {
            stateHandler.setCurrentState(State.WALKING_TO_BANK);
        } else if (area1.contains(myPlayer()) || area2.contains(myPlayer())) {
            stateHandler.setCurrentState(State.LOOTING);
        } else {
            stateHandler.setCurrentState(State.WALKING_TO_COWS);
        }
    }

    public void walkToCows() {
        walker.walkToArea(area1, "Walking to cow area");
        stateHandler.setCurrentState(State.LOOTING);
    }

    public void lootCowhides() throws InterruptedException {
        GroundItem cowhide = getGroundItems().closest("Cowhide");
        if (cowhide != null && cowhide.exists()) {
            if (cowhide.interact("Take")) {
                new ConditionalSleep(5000) {
                    @Override
                    public boolean condition() {
                        return !cowhide.exists();
                    }
                }.sleep();
                cowhidesLooted++;
            }
        } else {
            stateHandler.setCurrentState(State.CHANGING_AREA);
        }

        if (getInventory().isFull()) {
            stateHandler.setCurrentState(State.WALKING_TO_BANK);
        }
    }

    public void changeArea() {
        walker.walkToArea(area1.contains(myPlayer()) ? area2 : area1, "Changing cow area");
        stateHandler.setCurrentState(State.LOOTING);
    }

    public void hopWorlds() throws InterruptedException {
        int currentWorld = getWorlds().getCurrentWorld();
        Random random = new Random();
    
        // Dynamically fetch all available worlds
        List<World> availableWorlds = getWorlds().getAvailableWorlds(true).stream()
                .filter(world -> !world.isPvpWorld()) // Exclude PvP worlds
                .filter(world -> !world.isMembers()) // Exclude members-only worlds
                .filter(world -> world.getId() != currentWorld) // Exclude the current world
                .filter(world -> !world.isFull()) // Exclude full worlds
                .collect(Collectors.toList());
    
        if (!availableWorlds.isEmpty()) {
            // Select a random world from the filtered list
            World randomWorld = availableWorlds.get(random.nextInt(availableWorlds.size()));
    
            // Attempt to hop to the selected world
            if (getWorlds().hop(randomWorld.getId())) {
                new ConditionalSleep(10000) {
                    @Override
                    public boolean condition() {
                        return getWorlds().getCurrentWorld() == randomWorld.getId();
                    }
                }.sleep();
                log("Hopped to world " + randomWorld.getId());
            }
        } else {
            log("No available F2P worlds to hop to.");
        }
    
        stateHandler.setCurrentState(State.WALKING_TO_COWS); // Update the state
    }
    
    public void walkToBank() {
        walker.walkToArea(lumbridgeBank, "Walking to Lumbridge bank");
        stateHandler.setCurrentState(State.DEPOSITING);
    }

    public void depositCowhides() throws InterruptedException {
        if (getBank().isOpen()) {
            getBank().depositAll("Cowhide");
            new ConditionalSleep(5000) {
                @Override
                public boolean condition() {
                    return !getInventory().contains("Cowhide");
                }
            }.sleep();
            stateHandler.setCurrentState(State.WALKING_TO_COWS);
        } else {
            getBank().open();
            new ConditionalSleep(5000) {
                @Override
                public boolean condition() {
                    return getBank().isOpen();
                }
            }.sleep();
        }
    }

    public String formatTime(long ms) {
        long sec = TimeUnit.MILLISECONDS.toSeconds(ms) % 60;
        long min = TimeUnit.MILLISECONDS.toMinutes(ms) % 60;
        long hr = TimeUnit.MILLISECONDS.toHours(ms) % 24;
        return String.format("%02d:%02d:%02d", hr, min, sec);
    }
}
