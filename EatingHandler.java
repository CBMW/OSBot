package utils;

import org.osbot.rs07.api.ui.Skill;
import org.osbot.rs07.script.Script;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.concurrent.atomic.AtomicBoolean;

public class EatingHandler implements Runnable {
    private final Script script;
    private final int eatBelowHP;
    private final String food;
    private final AtomicBoolean running;
    private Thread thread; // Track the thread for proper management

    public EatingHandler(Script script, int eatBelowHP, String food) {
        this.script = script;
        this.eatBelowHP = eatBelowHP;
        this.food = food;
        this.running = new AtomicBoolean(false);
    }

    @Override
    public void run() {
        script.log("EatingHandler thread is running.");
        while (running.get()) {
            try {
                if (!running.get()) {
                    script.log("Stopping EatingHandler.");
                    break;
                }
                checkHealthAndEat();
                // Sleep for 1 second between checks
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                script.log("EatingHandler thread interrupted.");
                stop(); // Ensure we stop the handler cleanly if interrupted
            } catch (Exception e) {
                logException(e);
            }
        }
    }

    private void checkHealthAndEat() throws InterruptedException {
        int currentHealth = script.getSkills().getDynamic(Skill.HITPOINTS);

        if (currentHealth == 0) {
            script.log("Player is dead. Waiting for respawn...");
            waitForRespawn();
        } else if (shouldEat(currentHealth)) {
            eatFood();
        }
    }

    private boolean shouldEat(int currentHealth) {
        return currentHealth > 0 && currentHealth <= eatBelowHP;
    }

    private boolean eatFood() {
        try {
            script.log("Attempting to eat food...");
            if (script.getInventory().interact("Eat", food)) {
                script.log("Successfully ate: " + food);
                script.sleep(600);  // Mimic eating time
                return true;
            } else {
                script.log("Failed to eat: " + food);
            }
        } catch (Exception e) {
            script.log("Error while eating: " + e.getMessage());
        }
        return false;
    }

    private void waitForRespawn() throws InterruptedException {
        new org.osbot.rs07.utility.ConditionalSleep(30000) {
            @Override
            public boolean condition() {
                return script.getSkills().getDynamic(Skill.HITPOINTS) > 0;
            }
        }.sleep();
        script.log("Player respawned. Resuming eating checks.");
    }

    // Start the EatingHandler thread
    public synchronized void start() {
        if (!running.get()) {
            running.set(true);
            thread = new Thread(this);
            thread.start();
            script.log("EatingHandler started.");
        }
    }

    // Stop the EatingHandler thread
    public synchronized void stop() {
        running.set(false);  // Set running to false to break the while loop
        if (thread != null && thread.isAlive()) {
            thread.interrupt();  // Interrupt the thread to stop if it's sleeping
        }
        script.log("EatingHandler stopped.");
    }

    private void logException(Exception e) {
        script.log("Exception in EatingHandler: " + e.getMessage());
        StringWriter writer = new StringWriter();
        PrintWriter printWriter = new PrintWriter(writer);
        e.printStackTrace(printWriter);
        printWriter.flush();
        script.log(writer.toString());
    }
}
