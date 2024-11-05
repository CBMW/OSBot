import org.osbot.rs07.api.Skills;
import org.osbot.rs07.api.map.Area;
import org.osbot.rs07.api.map.Position;
import org.osbot.rs07.api.model.GroundItem;
import org.osbot.rs07.api.model.NPC;
import org.osbot.rs07.api.ui.Skill;
import org.osbot.rs07.script.Script;
import org.osbot.rs07.script.ScriptManifest;
import org.osbot.rs07.utility.ConditionalSleep;
import javax.swing.*;
import java.awt.*;
import java.time.Duration;
import java.time.Instant;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import antiban.basicAB;

@ScriptManifest(name = "Fighter", author = "Dubai", version = 0.9, info = "Fights mobs", logo = "")
public class Fighter extends Script {
    private final Object lock = new Object();
    public boolean guiDone = false;
    private Instant startTime;
    private NPC currentTargetNPC;
    private basicAB antiBan;
    private static Random random = new Random();
    private boolean isLooting;

    public void setHopWorlds(List<Integer> hopWorlds) {
        this.hopWorlds = hopWorlds;
    }

    public void setSelectedBankArea(Area bankArea) {
        this.bankArea = bankArea;
    }

    public Object getLock() {
        return lock;
    }

    public void guiDone() {
        synchronized (lock) {
            guiDone = true;
            lock.notify();
        }
    }

    private enum State {
        WALK_TO_COWS, KILL_COWS, PICKUP_COWHIDES, WALK_TO_BANK, BANK, HANDLE_BANK_INTERFACE, HOP_WORLD, ENABLE_RUN
    }

    private State currentState;
    private final Area defaultBankArea = new Area(3009, 3355, 3017, 3358);
    private Area bankArea = defaultBankArea;
    private Map<String, Area> npcAreas = new HashMap<>();
    private boolean enableEating;
    private String selectedFood;
    private int eatBelowHealth;
    private int withdrawFoodAmount;
    private boolean hopForPlayers;
    private List<Integer> hopWorlds;
    private boolean enableRun;
    private String selectedNPC;
    private boolean enableLooting;
    private List<String> itemsToLoot;

    @Override
    public void onStart() {
        antiBan = new basicAB(this);
        antiBan.setAntiBanChance(0.1);
        startTime = Instant.now();

        // Populate NPC areas
        npcAreas.put("Cow", new Area(3022, 3298, 3042, 3312));
        npcAreas.put("Monk", new Area(3200, 3400, 3210, 3410));
        npcAreas.put("Guard", new Area(3000, 3400, 3010, 3410));
        npcAreas.put("Chicken", new Area(3026, 3282, 3037, 3289));

        // Show GUI
        SwingUtilities.invokeLater(() -> new FighterGUI(this).showGUI());

        // Wait for GUI to be done
        synchronized (lock) {
            while (!guiDone) {
                try {
                    lock.wait();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }

        currentState = State.WALK_TO_COWS; // Set initial state
    }

    @Override
    public int onLoop() throws InterruptedException {
        if (random.nextInt(10) < 1) { // Example condition (1 in 10 chance)
            antiBan.performAntiBan();
        }

        if (!guiDone) {
            return 500; // Delay if GUI isn't finished
        }

        log("Current State: " + currentState);  // Debugging statement

        switch (currentState) {
            case WALK_TO_COWS:
                walkToCows();
                break;
            case KILL_COWS:
                killCows();
                break;
            case PICKUP_COWHIDES:
                pickupCowhides();
                break;
            case WALK_TO_BANK:
                walkToBank();
                break;
            case BANK:
                handleBanking();
                break;
            case HANDLE_BANK_INTERFACE:
                handleBankInterface();
                break;
            case HOP_WORLD:
                hopWorld();
                break;
            case ENABLE_RUN:
                enableRunMode();
                break;
        }

        return 1000; // Adjust delay as needed
    }

    private void walkToCows() {
        if (!npcAreas.get(selectedNPC).contains(myPosition())) {
            log("Walking to NPC area...");
            getWalking().webWalk(npcAreas.get(selectedNPC));
        } else {
            currentState = State.KILL_COWS;
        }
    }

    private void killCows() throws InterruptedException {
        // Ensure that the current target is null or not alive, then find a new target
        if (currentTargetNPC == null || !currentTargetNPC.exists() || currentTargetNPC.getHealthPercent() == 0) {
            log("Searching for a new target...");

            // Search for the closest attackable NPC within the defined area
            currentTargetNPC = getNpcs().closest(npc ->
                    npc.getName().equals(selectedNPC) &&
                            npcAreas.get(selectedNPC).contains(npc) &&
                            npc.isAttackable() &&
                            !npc.isUnderAttack()
            );

            if (currentTargetNPC != null) {
                log("New target found: " + currentTargetNPC.getName() + " (ID: " + currentTargetNPC.getId() + ")");
                if (currentTargetNPC.interact("Attack")) {
                    log("Attacking NPC...");
                    new ConditionalSleep(5000) {
                        @Override
                        public boolean condition() {
                            return getCombat().isFighting();
                        }
                    }.sleep();
                } else {
                    log("Failed to interact with NPC. Trying again...");
                    currentTargetNPC = null; // Reset target to reattempt finding and attacking
                }
            } else {
                log("No valid targets found. Waiting...");
                sleep(random(1000, 2000)); // Avoid busy-waiting and give time for new NPCs to spawn
            }
        } else {
            log("Currently engaged with an NPC. Waiting for the combat to finish...");
            sleep(random(1000, 2000)); // Wait for combat to finish
        }

        // Ensure that the state remains consistent, only switching states when required
        if (currentTargetNPC == null || !currentTargetNPC.isAttackable()) {
            currentState = State.PICKUP_COWHIDES; // Switch to looting if no valid NPCs are available
        }
    }

    private void pickupCowhides() {
        if (enableLooting) {
            // Ensure the bot only loots after a fight and while not fighting
            if (!getCombat().isFighting() && currentTargetNPC != null && !currentTargetNPC.isAttackable()) {
                GroundItem lootItem = getGroundItems().closest(item -> itemsToLoot.contains(item.getName()) && npcAreas.get(selectedNPC).contains(item));
                if (lootItem != null) {
                    log("Looting item: " + lootItem.getName());
                    lootItem.interact("Take");
                    new ConditionalSleep(5000) {
                        @Override
                        public boolean condition() {
                            // Check if the lootItem is still on the ground
                            return getGroundItems().closest(lootItem.getName()) == null;
                        }
                    }.sleep();
                } else {
                    log("No more items to loot. Switching back to combat.");
                    currentState = State.KILL_COWS;  // Transition back to killing cows
                }
            } else if (getCombat().isFighting()) {
                log("In combat, waiting to finish before looting.");
                // Stay in combat state
            } else {
                log("No valid loot or combat, switching to attack.");
                currentState = State.KILL_COWS;  // Transition back to killing cows
            }
        } else {
            currentState = State.KILL_COWS;  // If looting is not enabled, switch back to combat
        }
    }

    private void walkToBank() {
        if (!bankArea.contains(myPosition())) {
            log("Walking to bank...");
            getWalking().webWalk(bankArea);
        } else {
            currentState = State.BANK;
        }
    }

    private void handleBanking() {if (
            enableEating && getSkills().getDynamic(Skill.forId(3)) <= eatBelowHealth) {
            if (getInventory().contains(selectedFood)) {
                log("Eating food...");
                getInventory().getItem(selectedFood).interact("Eat");
                new ConditionalSleep(5000) {
                    @Override
                    public boolean condition() {
                        return getSkills().getDynamic(Skill.forId(3)) > eatBelowHealth;
                    }
                }.sleep();
            } else {
                log("Food not found in inventory. Banking for food...");
                currentState = State.WALK_TO_BANK;
            }
        } else if (getInventory().isFull()) {
            currentState = State.BANK;
        } else {
            currentState = State.PICKUP_COWHIDES;
        }
    }

    private void handleBankInterface() throws InterruptedException {
        if (getBank().isOpen()) {
            if (enableEating && !getInventory().contains(selectedFood)) {
                log("Withdrawing food...");
                getBank().withdraw(selectedFood, withdrawFoodAmount);
                new ConditionalSleep(5000) {
                    @Override
                    public boolean condition() {
                        return getInventory().contains(selectedFood);
                    }
                }.sleep();
            }

            if (getInventory().isFull()) {
                log("Depositing items...");
                getBank().depositAll();
                new ConditionalSleep(5000) {
                    @Override
                    public boolean condition() {
                        return getInventory().isEmpty();
                    }
                }.sleep();
            }
        } else {
            log("Opening bank...");
            getBank().open();
        }
    }

    private void hopWorld() throws InterruptedException {
        if (hopWorlds != null && !hopWorlds.isEmpty()) {
            int randomWorld = hopWorlds.get(random.nextInt(hopWorlds.size()));
            log("Hopping to world: " + randomWorld);

            // Open the world hopper interface (by clicking the logout tab if necessary)
            getTabs().open(org.osbot.rs07.api.ui.Tab.LOGOUT);
            sleep(random(1000, 2000)); // Wait for the interface to open

            // This might differ based on your OSBot version; ensure that the widget IDs and actions are correct
            if (getWidgets().getWidgetContainingText("World Hopping") != null) {
                getWidgets().getWidgetContainingText("World Hopping").interact("World Hopping");
                sleep(random(1000, 2000)); // Wait for the world hopper to open

                // Select the world from the list
                // Find the specific widget or button to select the world; this is a placeholder
                getWidgets().getWidgetContainingText(Integer.toString(randomWorld)).interact("Select World");
                sleep(random(1000, 2000)); // Wait for the world to switch
            } else {
                log("World Hopper widget not found.");
            }

            // Optionally verify if the world has been changed
            new ConditionalSleep(10000) {
                @Override
                public boolean condition() {
                    return getWorlds().getCurrentWorld() == randomWorld;
                }
            }.sleep();
        } else {
            log("No worlds to hop.");
        }
    }



    private void enableRunMode() {
        if (!getSettings().isRunning()) {
            log("Enabling run mode...");
            getSettings().setRunning(true);
        }
        currentState = State.KILL_COWS;
    }

    @Override
    public void onPaint(Graphics2D g) {
        super.onPaint(g);
        long elapsed = Duration.between(startTime, Instant.now()).toMillis();
        int hours = (int) (elapsed / 3600000);
        int minutes = (int) ((elapsed % 3600000) / 60000);
        int seconds = (int) ((elapsed % 60000) / 1000);

        // Draw a semi-transparent background for the text
        g.setColor(new Color(0, 0, 0, 150)); // Black with 150 alpha
        g.fillRect(10, 30, 200, 80); // Adjust size and position as needed
        g.setColor(Color.WHITE);
        g.drawString(String.format("Runtime: %02d:%02d:%02d", hours, minutes, seconds), 20, 50);
        g.drawString("State: " + currentState, 20, 70);
        g.drawString("Target NPC: " + (currentTargetNPC != null ? currentTargetNPC.getName() : "None"), 20, 90);
        g.drawString("Looting: " + (isLooting ? "Yes" : "No"), 20, 110);

        if (enableRun) {
            g.drawString("Run Enabled: Yes", 20, 130);
        } else {
            g.drawString("Run Enabled: No", 20, 130);
        }
    }

    public static int random(int min, int max) {
        return random.nextInt(max - min + 1) + min;
    }
    public void setEnableEating(boolean enableEating) {
        this.enableEating = enableEating;
    }

    public void setSelectedFood(String selectedFood) {
        this.selectedFood = selectedFood;
    }

    public void setEatBelowHealth(int eatBelowHealth) {
        this.eatBelowHealth = eatBelowHealth;
    }

    public void setWithdrawFoodAmount(int withdrawFoodAmount) {
        this.withdrawFoodAmount = withdrawFoodAmount;
    }

    public void setHopForPlayers(boolean hopForPlayers) {
        this.hopForPlayers = hopForPlayers;
    }

    public void setEnableRun(boolean enableRun) {
        this.enableRun = enableRun;
    }

    public void setSelectedNPC(String selectedNPC) {
        this.selectedNPC = selectedNPC;
    }

    public void setEnableLooting(boolean enableLooting) {
        this.enableLooting = enableLooting;
    }

    public void setItemsToLoot(List<String> itemsToLoot) {
        this.itemsToLoot = itemsToLoot;
    }

}
