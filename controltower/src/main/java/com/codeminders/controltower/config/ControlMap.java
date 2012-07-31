package com.codeminders.controltower.config;

import com.codeminders.ardrone.ARDrone;
import com.codeminders.controltower.config.AssignableControl.ControllerButton;
import java.io.IOException;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.prefs.BackingStoreException;
import java.util.prefs.Preferences;

import org.apache.log4j.Logger;

/**
 * This class stores the button mapping and manages
 * button command sending to the drone
 * @author normenhansen
 */
public class ControlMap {

    private Preferences prefs;
    private HashMap<ControllerButton, List<AssignableControl>> map = new HashMap<ControllerButton, List<AssignableControl>>();
    ScheduledThreadPoolExecutor exec = new ScheduledThreadPoolExecutor(1);

    public ControlMap() {
        prefs = Preferences.userNodeForPackage(this.getClass());
        loadMap();
    }

    /**
     * Called from the update loop in ControlTower when a button is pressed
     * @param drone
     * @param button
     * @throws IOException 
     */
    public synchronized void sendCommand(ARDrone drone, ControllerButton button) throws IOException {
        List<AssignableControl> commands = map.get(button);
        if (commands == null) {
            return;
        }
        for (Iterator<AssignableControl> it = commands.iterator(); it.hasNext();) {
            final AssignableControl assignableCommand = it.next();
            if (assignableCommand.getDelay() > 0) {
                delayCommand(assignableCommand, drone);
            } else {
                assignableCommand.sendToDrone(drone);
            }
        }
    }

    /**
     * Executes a command for a drone with its assigned delay using an Executor
     * @param command
     * @param drone 
     */
    private void delayCommand(final AssignableControl command, final ARDrone drone) {
        exec.schedule(new Callable<Object>() {

            @Override
            public Object call() throws Exception {
                command.sendToDrone(drone);
                return null;
            }
        }, command.getDelay(), TimeUnit.MILLISECONDS);
    }

    /**
     * Gets the list of control commands that are assigned to a certain button
     * @param button
     */
    public List<AssignableControl> getControls(ControllerButton button) {
        return map.get(button);
    }

    /**
     * Sets the list of control commands for a certain button
     * @param button
     * @param controls 
     */
    public synchronized void setControls(ControllerButton button, List<AssignableControl> controls) {
        map.put(button, controls);
        storeMap();
    }

    /**
     * Loads the mapping from the java preferences
     */
    private void loadMap() {
        boolean found = false;
        try {
            String[] keys = prefs.keys();
            for (int i = 0; i < keys.length; i++) {
                String string = keys[i];
                if (string.startsWith("CONTROL_MAPPING")) {
                    String mapping = prefs.get(string, "");
                    AssignableControl command = new AssignableControl(mapping);
                    List<AssignableControl> commands = map.get(command.getButton());
                    if (commands == null) {
                        commands = new LinkedList<AssignableControl>();
                        map.put(command.getButton(), commands);
                    }
                    commands.add(0, command);
                    Logger.getLogger(ControlMap.class.getName()).debug("Load command:{0} "+command.getPrefString());
                    found = true;
                }
            }
        } catch (BackingStoreException ex) {
            Logger.getLogger(ControlMap.class.getName()).error("{0}", ex);
        }
        if (!found) {
            Logger.getLogger(ControlMap.class.getName()).debug("Load default button map");
            createDefaultMapping();
        }
    }

    /**
     * Creates the default mapping in case no settings are stored yet
     */
    private void createDefaultMapping() {
        map.put(ControllerButton.PS, new LinkedList<AssignableControl>());
        map.get(ControllerButton.PS).add(new AssignableControl(ControllerButton.PS, AssignableControl.Command.RESET, 0));

        map.put(ControllerButton.START, new LinkedList<AssignableControl>());
        map.get(ControllerButton.START).add(new AssignableControl(ControllerButton.START, AssignableControl.Command.TAKEOFF, 0));

        map.put(ControllerButton.SELECT, new LinkedList<AssignableControl>());
        map.get(ControllerButton.SELECT).add(new AssignableControl(ControllerButton.SELECT, AssignableControl.Command.LAND, 0));
        
        map.put(ControllerButton.TRIANGLE, new LinkedList<AssignableControl>());
        map.get(ControllerButton.TRIANGLE).add(new AssignableControl(ControllerButton.TRIANGLE, AssignableControl.Command.VIDEO_CYCLE, 0));
        
        map.put(ControllerButton.SQUARE, new LinkedList<AssignableControl>());
        map.get(ControllerButton.SQUARE).add(new AssignableControl(ControllerButton.SQUARE, AssignableControl.Command.TAKE_SNAPSHOT, 0));

        map.put(ControllerButton.CIRCLE, new LinkedList<AssignableControl>());
        map.get(ControllerButton.CIRCLE).add(new AssignableControl(ControllerButton.CIRCLE, AssignableControl.Command.RECORD_VIDEO, 0));

    }

    /**
     * Stores the mapping in the java preferences
     */
    private void storeMap() {
        clearMap();
        int i = 0;
        Collection<List<AssignableControl>> list = map.values();
        for (Iterator<List<AssignableControl>> it = list.iterator(); it.hasNext();) {
            List<AssignableControl> list1 = it.next();
            for (Iterator<AssignableControl> it1 = list1.iterator(); it1.hasNext();) {
                AssignableControl assignableCommand = it1.next();
                prefs.put("CONTROL_MAPPING_" + i++, assignableCommand.getPrefString());
            }
        }
        try {
            prefs.flush();
        } catch (BackingStoreException ex) {
            Logger.getLogger(ControlMap.class.getName()).error("{0}", ex);
        }
    }

    /**
     * Clears the used preferences from all mappings
     */
    private void clearMap() {
        String[] keys;
        try {
            keys = prefs.keys();
            for (int i = 0; i < keys.length; i++) {
                String string = keys[i];
                if (string.startsWith("CONTROL_MAPPING")) {
                    prefs.remove(string);
                }
            }
        } catch (BackingStoreException ex) {
            Logger.getLogger(ControlMap.class.getName()).error("{0}", ex);
        }
    }
}