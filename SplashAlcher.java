package scripts;

import org.osbot.rs07.api.ui.Spells;
import org.osbot.rs07.api.ui.Skill;
import org.osbot.rs07.api.model.NPC;
import org.osbot.rs07.script.Script;
import org.osbot.rs07.script.ScriptManifest;

import java.awt.*;
import javax.swing.*;

@ScriptManifest(name = "Splash Alcher", author = "Dubai", version = 1, info = "Splashes/High Alchemy", logo = "")
public class SplashAlcher extends Script {

    private String alchItem;  // Item to high alch, set in RockCrabsGUI
    private long startTime;   // For tracking runtime
    private int startMagicLevel;  // Initial magic level for tracking gains
    private int startMagicXP;  // Initial magic XP
    private State currentState;  // Current state of the script
    private NPC grizzlyBear;  // Reference to the Grizzly bear

    private enum State {
        CAST_ALCH, SELECT_ITEM, ATTACK_BEAR, WAIT_FOR_ANIMATION
    }

    @Override
    public void onStart() {
        alchItem = JOptionPane.showInputDialog("Enter the item name to high alch:");
        startTime = System.currentTimeMillis();
        startMagicLevel = getSkills().getDynamic(Skill.MAGIC);
        startMagicXP = getSkills().getExperience(Skill.MAGIC);
        currentState = State.CAST_ALCH;  // Initial state
        log("Starting Scripts.SplashAlcher with alch item: " + alchItem);
    }

    @Override
    public void onExit() {
        log("Stopping Scripts.SplashAlcher.");
    }

    @Override
    public int onLoop() throws InterruptedException {
        switch (currentState) {
            case CAST_ALCH:
                if (getMagic().canCast(Spells.NormalSpells.HIGH_LEVEL_ALCHEMY)) {
                    getMagic().castSpell(Spells.NormalSpells.HIGH_LEVEL_ALCHEMY);
                    sleep(random(400, 600));
                    currentState = State.SELECT_ITEM;
                }
                break;

            case SELECT_ITEM:
                if (getInventory().contains(alchItem)) {
                    getInventory().interact("Cast", alchItem);
                    sleep(random(400, 600));
                    currentState = State.ATTACK_BEAR;
                }
                break;

            case ATTACK_BEAR:
                if (!getCombat().isFighting()) {
                    attackGrizzlyBear();
                    sleep(random(400, 600));
                    currentState = State.WAIT_FOR_ANIMATION;
                }
                break;

            case WAIT_FOR_ANIMATION:
                if (!myPlayer().isAnimating()) {
                    sleep(random(1000, 1500));  // Wait for animation to finish
                    currentState = State.CAST_ALCH;  // Reset to cast alch again
                }
                break;
        }
        return random(200, 300);
    }

    private void attackGrizzlyBear() throws InterruptedException {
        // Target and attack the Grizzly bear
        if (grizzlyBear == null || !grizzlyBear.exists()) {
            grizzlyBear = getNpcs().closest("Grizzly bear");
        }

        if (grizzlyBear != null && !getCombat().isFighting()) {
            if (grizzlyBear.isVisible()) {
                grizzlyBear.interact("Attack");
                sleep(random(600, 800));
            } else {
                getCamera().toEntity(grizzlyBear);  // Rotate camera to the bear if not visible
                sleep(random(400, 600));
            }
        }
    }

    @Override
    public void onPaint(Graphics2D g) {
        // Calculate runtime and experience
        long runTime = System.currentTimeMillis() - startTime;
        int currentMagicXP = getSkills().getExperience(Skill.MAGIC);
        int magicXPGained = currentMagicXP - startMagicXP;
        int magicLevelGained = getSkills().getDynamic(Skill.MAGIC) - startMagicLevel;
        double xpPerHour = (magicXPGained * 3600000D) / runTime;  // XP per hour formula

        // Improved on-paint design
        g.setColor(new Color(0, 0, 0, 150));  // Semi-transparent background
        g.fillRoundRect(10, 10, 250, 120, 10, 10);  // Rounded box

        g.setColor(Color.WHITE);
        g.setFont(new Font("Arial", Font.BOLD, 14));
        g.drawString("Scripts.SplashAlcher by Dubai", 20, 30);

        g.setFont(new Font("Arial", Font.PLAIN, 12));
        g.drawString("Runtime: " + formatTime(runTime), 20, 50);
        g.drawString("Magic Levels Gained: " + magicLevelGained, 20, 65);
        g.drawString("Magic XP Gained: " + magicXPGained, 20, 80);
        g.drawString("XP per Hour: " + String.format("%.2f", xpPerHour), 20, 95);
        g.drawString("Current State: " + currentState.name(), 20, 110);
    }

    private String formatTime(long millis) {
        long seconds = millis / 1000;
        long minutes = seconds / 60;
        long hours = minutes / 60;
        return String.format("%02d:%02d:%02d", hours, minutes % 60, seconds % 60);
    }
}
