import org.osbot.rs07.api.model.RS2Object;
import org.osbot.rs07.api.ui.Skill;
import org.osbot.rs07.script.Script;
import org.osbot.rs07.script.ScriptManifest;
import org.osbot.rs07.api.map.Area;
import java.awt.*;
import java.util.List;
import java.util.Random;

@ScriptManifest(name = "Oak Banker", author = "Dubai", version = 0.1, info = "Banks oak logs", logo = "")
public class oak_banker extends Script {

    private final Area treeArea = new Area(2946, 3396, 2959, 3412);
    private final Area bankArea = new Area(2944, 3368, 2946, 3373);
    private final Random random = new Random();
    private int antiBanCounter = 0;
    private long startTime;
    private int logsChopped = 0; // Counter for logs chopped
    private int previousLogCount = 0; // Variable to keep track of the previous inventory count
    private int initialLevel; // Variable to store the initial level

    @Override
    public void onStart() {
        log("Script started.");
        startTime = System.currentTimeMillis();
        previousLogCount = (int) getInventory().getAmount("Oak logs"); // Initialize the previous log count
        initialLevel = getSkills().getStatic(Skill.WOODCUTTING); // Store the initial level
    }

    @Override
    public void onExit() {
        log("Script ended.");
    }

    @Override
    public int onLoop() {
        int currentLogCount = (int) getInventory().getAmount("Oak logs"); // Get the current log count
        if (currentLogCount > previousLogCount) {
            logsChopped += (currentLogCount - previousLogCount); // Increment the counter based on the difference
            previousLogCount = currentLogCount; // Update the previous log count
        }

        if (inventory.isFull()) {
            log("Inventory is full. Walking to the bank...");
            if (!bankArea.contains(myPlayer())) {
                log("Player is not in the bank area. Walking to the bank...");
                getWalking().webWalk(bankArea);
            } else {
                log("Player is in the bank area.");
                if (!getBank().isOpen()) {
                    log("Bank is not open. Attempting to open a random bank booth...");
                    try {
                        openRandomBankBooth();
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                } else {
                    log("Bank is open. Depositing items...");
                    if (getBank().depositAllExcept("Bronze axe","Steel axe")) {
                        previousLogCount = 0; // Reset the previous log count after banking
                        try {
                            sleep(random(200, 300));
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                        getBank().close();
                        log("Deposited logs. Walking back to the tree area...");
                        getWalking().webWalk(treeArea);
                    } else {
                        log("Failed to deposit items. Retrying...");
                    }
                }
            }
        } else {
            log("Inventory is not full. Walking to the tree area...");
            if (!treeArea.contains(myPlayer())) {
                log("Player is not in the tree area. Walking to the tree area...");
                getWalking().webWalk(treeArea);
                addRandomDelay(); // Add random delay
            } else {
                log("Player is in the tree area.");
                RS2Object tree = objects.closest(t -> t != null && (t.getName().equals("Oak tree") || t.getName().equals("Oak Tree")) && treeArea.contains(t));
                if (tree != null && tree.isVisible() && !myPlayer().isAnimating()) {
                    log("Chopping tree...");
                    tree.interact("Chop down");
                    addRandomDelay();
                    performAntiBanActions();
                } else {
                    log("No tree found or player is busy.");
                }
            }
        }

        // Check if the bank is open and handle accordingly
        if (getBank().isOpen()) {
            log("Bank is open. Depositing items...");
            if (getBank().depositAllExcept("Axe")) {
                previousLogCount = 0; // Reset the previous log count after banking
                try {
                    sleep(random(200, 300));
                } catch (InterruptedException e) {
                    e.printStackTrace(); // Handle the exception as needed
                }
                getBank().close();
                log("Deposited logs. Walking back to the tree area...");
                getWalking().webWalk(treeArea);
            } else {
                log("Failed to deposit items. Closing bank and retrying...");
                getBank().close();
            }
        }

        return 5000; // The amount of time in milliseconds before the loop starts over (5 seconds)
    }

    private void openRandomBankBooth() throws InterruptedException {
        List<RS2Object> bankBooths = getObjects().filter(obj -> obj != null && obj.getName().equals("Bank booth"));
        if (!bankBooths.isEmpty()) {
            RS2Object randomBankBooth = bankBooths.get(random.nextInt(bankBooths.size()));
            randomBankBooth.interact("Bank");
            sleep(random(200, 600)); // Wait for the bank to open
        } else {
            log("No bank booths found.");
        }
    }

    private void performAntiBanActions() {
        antiBanCounter++;
        if (antiBanCounter % 2 == 0) { // Perform anti-ban random mouse movement every 2nd time
            log("Performing anti-ban actions...");
            int x = random.nextInt(500);
            int y = random.nextInt(500);
            getMouse().move(x, y);
            try {
                sleep(random(200, 500));
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    private void addRandomDelay() { //Random delay for antiban purposes
        try {
            sleep(random(1000, 15000)); // ADJUST ANTI BAN TIMER HERE
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onPaint(Graphics2D g) {
        long runTime = System.currentTimeMillis() - startTime;
        int currentLevel = getSkills().getStatic(Skill.WOODCUTTING); // Get the current level
        int levelsGained = currentLevel - initialLevel; // Calculate the levels gained

        g.setColor(Color.WHITE);
        g.drawString("Runtime: " + formatTime(runTime), 10, 30);
        g.drawString("Logs Chopped: " + logsChopped, 10, 70); // Display the logs chopped counter
        g.drawString("Levels Gained: " + levelsGained, 10, 50); // Display the levels gained
    }

    private String formatTime(long time) {
        long seconds = (time / 1000) % 60;
        long minutes = (time / (1000 * 60)) % 60;
        long hours = (time / (1000 * 60 * 60)) % 24;
        return String.format("%02d:%02d:%02d", hours, minutes, seconds);
    }
}
