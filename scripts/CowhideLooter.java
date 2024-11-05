package scripts;

import org.osbot.rs07.api.model.GroundItem;
import org.osbot.rs07.api.map.Area;
import org.osbot.rs07.api.map.Position;
import org.osbot.rs07.script.Script;
import org.osbot.rs07.script.ScriptManifest;
import org.osbot.rs07.utility.ConditionalSleep;
import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.util.Arrays;
import java.util.List;
import java.util.Random;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import utils.EnableRun;  // Importing the EnableRun class

@ScriptManifest(name = "Cowhide Looter", author = "Dubai", version = 1.0, info = "Loots cowhides in Lumbridge", logo = "")
public class CowhideLooter extends Script {

    private enum State {
        WALKING2COWS, LOOTING, CHANGING_AREA, HOPPING_WORLDS, WALKING2BANK, DEPOSITING
    }

    private State currentState;
    private int cowhidesLooted = 0;
    private long startTime;

    private Area area1 = new Area(new Position(3253, 3255, 0), new Position(3265, 3268, 0));
    private Area area2 = new Area(new Position(3254, 3269, 0), new Position(3265, 3295, 0));
    private Area lumbridgeBank = new Area(new Position(3207, 3217, 2), new Position(3210, 3220, 2));

    private List<Integer> f2pWorlds = Arrays.asList(
            301, 308, 316, 326, 335, 382, 383, 393, 394, 395, 396, 397, 398, 399
    );

    private EnableRun enableRun;  // Create instance of EnableRun class
    private Random random = new Random();  // Instantiate Random object

    @Override
    public void onStart() {
        log("Cowhide Looter script started.");
        currentState = State.WALKING2COWS;
        startTime = System.currentTimeMillis();
        enableRun = new EnableRun(this);  // Initialize EnableRun instance
    }

    @Override
    public int onLoop() throws InterruptedException {
        switch (currentState) {
            case WALKING2COWS:
                walkToCows();
                break;
            case LOOTING:
                lootCowhides();
                break;
            case CHANGING_AREA:
                changeArea();
                break;
            case HOPPING_WORLDS:
                hopWorlds();
                break;
            case WALKING2BANK:
                walkToBank();
                break;
            case DEPOSITING:
                depositCowhides();
                break;
        }
        return 1000;
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

    private void walkToCows() {
        enableRun.enableRunMode();  // Activate run only when walking to cows or bank
        if (!area1.contains(myPlayer()) && !area2.contains(myPlayer())) {
            getWalking().webWalk(area1);
        } else {
            log("Arrived at cow area.");
            currentState = State.LOOTING;
        }
    }

    private long lastCowhideSearchTime = 0;  // Variable to track time spent searching

    private void lootCowhides() throws InterruptedException {
        GroundItem cowhide = getGroundItems().closest("Cowhide");

        if (lastCowhideSearchTime == 0) {
            lastCowhideSearchTime = System.currentTimeMillis();
        }

        if (cowhide != null && cowhide.exists()) {
            lastCowhideSearchTime = 0;  // Reset the timer as cowhide was found
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
            // Check if no cowhides are found after 10 seconds
            if (System.currentTimeMillis() - lastCowhideSearchTime > 10000) {
                if (area1.contains(myPlayer()) || area2.contains(myPlayer())) {
                    currentState = State.CHANGING_AREA;
                } else {
                    currentState = State.HOPPING_WORLDS;
                }
                lastCowhideSearchTime = 0;
            }
        }

        if (getInventory().isFull()) {
            currentState = State.WALKING2BANK;
        }
    }

    private void changeArea() {
        enableRun.enableRunMode();  // Activate run when changing areas
        if (area1.contains(myPlayer())) {
            getWalking().webWalk(area2.getRandomPosition());
        } else {
            getWalking().webWalk(area1.getRandomPosition());
        }
        currentState = State.LOOTING;
    }

    private void hopWorlds() throws InterruptedException {
        int currentWorld = getWorlds().getCurrentWorld();
        List<Integer> availableWorlds = f2pWorlds.stream()
                .filter(world -> world != currentWorld)
                .collect(Collectors.toList());
        int randomWorld = availableWorlds.get(random.nextInt(availableWorlds.size()));
        if (getWorlds().hop(randomWorld)) {
            new ConditionalSleep(10000) {
                @Override
                public boolean condition() {
                    return getWorlds().getCurrentWorld() == randomWorld;
                }
            }.sleep();
        }
        currentState = State.WALKING2COWS;
    }

    private void walkToBank() {
        enableRun.enableRunMode();  // Activate run only when walking to cows or bank
        if (!lumbridgeBank.contains(myPlayer())) {
            getWalking().webWalk(lumbridgeBank);
        } else {
            log("Arrived at Lumbridge bank.");
            currentState = State.DEPOSITING;
        }
    }

    private void depositCowhides() throws InterruptedException {
        if (getBank().isOpen()) {
            getBank().depositAll("Cowhide");
            new ConditionalSleep(5000) {
                @Override
                public boolean condition() {
                    return !getInventory().contains("Cowhide");
                }
            }.sleep();
            currentState = State.WALKING2COWS;
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

    private String formatTime(long ms) {
        long sec = TimeUnit.MILLISECONDS.toSeconds(ms) % 60;
        long min = TimeUnit.MILLISECONDS.toMinutes(ms) % 60;
        long hr = TimeUnit.MILLISECONDS.toHours(ms) % 24;
        return String.format("%02d:%02d:%02d", hr, min, sec);
    }
}
