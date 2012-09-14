package com.codeminders.ardrone;

import java.io.IOException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import com.codeminders.ardrone.AssignableControl.ControllerButton;


public class ControlMap {

    private HashMap<ControllerButton, List<AssignableControl>> map = new HashMap<ControllerButton, List<AssignableControl>>();
    ScheduledThreadPoolExecutor exec = new ScheduledThreadPoolExecutor(1);

    public ControlMap() {
        loadMap();
    }

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

    private void loadMap() {
        map.put(ControllerButton.PS, new LinkedList<AssignableControl>());
        map.get(ControllerButton.PS).add(new AssignableControl(ControllerButton.PS, AssignableControl.Command.RESET, 0));

        map.put(ControllerButton.START, new LinkedList<AssignableControl>());
        map.get(ControllerButton.START).add(new AssignableControl(ControllerButton.START, AssignableControl.Command.TAKEOFF, 0));

        map.put(ControllerButton.SELECT, new LinkedList<AssignableControl>());
        map.get(ControllerButton.SELECT).add(new AssignableControl(ControllerButton.SELECT, AssignableControl.Command.LAND, 0));
        
        map.put(ControllerButton.TRIANGLE, new LinkedList<AssignableControl>());
        map.get(ControllerButton.TRIANGLE).add(new AssignableControl(ControllerButton.TRIANGLE, AssignableControl.Command.VIDEO_CYCLE, 0));
    }
}