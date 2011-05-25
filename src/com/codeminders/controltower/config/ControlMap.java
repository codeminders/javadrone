package com.codeminders.controltower.config;

import com.codeminders.ardrone.ARDrone;
import com.codeminders.controltower.config.AssignableControl.CONTROL_KEY;
import java.io.IOException;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.prefs.BackingStoreException;
import java.util.prefs.Preferences;

/**
 *
 * @author normenhansen
 */
public class ControlMap {

    private Preferences prefs;
    private HashMap<CONTROL_KEY, List<AssignableControl>> map = new HashMap<CONTROL_KEY, List<AssignableControl>>();
    ScheduledThreadPoolExecutor exec = new ScheduledThreadPoolExecutor(1);

    public ControlMap() {
        prefs = Preferences.userNodeForPackage(this.getClass());
        loadMap();
    }

    public synchronized void sendCommand(ARDrone drone, CONTROL_KEY key) throws IOException {
        List<AssignableControl> commands = map.get(key);
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

    private void delayCommand(final AssignableControl command, final ARDrone drone) {
        exec.schedule(new Callable<Object>() {

            @Override
            public Object call() throws Exception {
                command.sendToDrone(drone);
                return null;
            }
        }, command.getDelay(), TimeUnit.MILLISECONDS);
    }

    public List<AssignableControl> getControls(CONTROL_KEY key) {
        return map.get(key);
    }

    public synchronized void setControls(CONTROL_KEY key, List<AssignableControl> controls) {
        map.put(key, controls);
        storeMap();
    }

    private void loadMap() {
        boolean found = false;
        try {
            String[] keys = prefs.keys();
            for (int i = 0; i < keys.length; i++) {
                String string = keys[i];
                if (string.startsWith("CONTROL_MAPPING")) {
                    String mapping = prefs.get(string, "");
                    AssignableControl command = new AssignableControl(mapping);
                    List<AssignableControl> commands = map.get(command.getKey());
                    if (commands == null) {
                        commands = new LinkedList<AssignableControl>();
                        map.put(command.getKey(), commands);
                    }
                    commands.add(command);
                    Logger.getLogger(ControlMap.class.getName()).log(Level.FINE, "Load command:{0}", command.getPrefString());
                    found = true;
                }
            }
        } catch (BackingStoreException ex) {
            Logger.getLogger(ControlMap.class.getName()).log(Level.SEVERE, "{0}", ex);
        }
        if (!found) {
            Logger.getLogger(ControlMap.class.getName()).log(Level.FINE, "Load default button map");
            createDefaultMapping();
        }
    }

    private void createDefaultMapping() {
        map.put(CONTROL_KEY.PS, new LinkedList<AssignableControl>());
        map.get(CONTROL_KEY.PS).add(new AssignableControl(CONTROL_KEY.PS, AssignableControl.COMMAND.RESET, 0));
        map.put(CONTROL_KEY.START, new LinkedList<AssignableControl>());
        map.get(CONTROL_KEY.START).add(new AssignableControl(CONTROL_KEY.START, AssignableControl.COMMAND.TAKEOFF, 0));
        map.put(CONTROL_KEY.SELECT, new LinkedList<AssignableControl>());
        map.get(CONTROL_KEY.SELECT).add(new AssignableControl(CONTROL_KEY.SELECT, AssignableControl.COMMAND.LAND, 0));
        map.put(CONTROL_KEY.TRIANGLE, new LinkedList<AssignableControl>());
        map.get(CONTROL_KEY.TRIANGLE).add(new AssignableControl(CONTROL_KEY.TRIANGLE, AssignableControl.COMMAND.VIDEO_CYCLE, 0));
    }

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
            Logger.getLogger(ControlMap.class.getName()).log(Level.SEVERE, "{0}", ex);
        }
    }

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
            Logger.getLogger(ControlMap.class.getName()).log(Level.SEVERE, "{0}", ex);
        }
    }
}
