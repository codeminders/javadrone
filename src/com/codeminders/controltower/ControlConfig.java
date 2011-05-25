/*
 * ControlConfig.java
 *
 * Created on 25.05.2011, 18:06:24
 */
package com.codeminders.controltower;

import com.codeminders.ardrone.ARDrone.Animation;
import com.codeminders.ardrone.ARDrone.LED;
import com.codeminders.controltower.config.AssignableControl;
import com.codeminders.controltower.config.AssignableControl.COMMAND;
import com.codeminders.controltower.config.AssignableControl.CONTROL_KEY;
import com.codeminders.controltower.config.ControlMap;
import java.util.LinkedList;
import java.util.List;

/**
 *
 * @author normenhansen
 */
@SuppressWarnings("serial")
public class ControlConfig extends javax.swing.JDialog {

    private ControlMap map;
    private List<AssignableControl> list;
    private int index = 0;

    /** Creates new form ControlConfig */
    public ControlConfig(java.awt.Frame parent, boolean modal, ControlMap map) {
        super(parent, modal);
        this.map = map;
        initComponents();
        initButtonList();
        initCommandList();
        buttonList.setSelectedIndex(0);
    }

    private void initButtonList() {
        CONTROL_KEY[] keys = CONTROL_KEY.values();
        String[] str = new String[keys.length];
        for (int i = 0; i < keys.length; i++) {
            CONTROL_KEY key = keys[i];
            str[i] = key.name();
        }
        buttonList.setListData(str);
    }

    private void initCommandList() {
        commandList.removeAllItems();
        COMMAND[] keys = COMMAND.values();
        commandList.addItem("None");
        for (int i = 0; i < keys.length; i++) {
            COMMAND key = keys[i];
            commandList.addItem(key.name());
        }
    }

    private void setAnimMode() {
        valueList.removeAllItems();
        Animation[] keys = Animation.values();
        for (int i = 0; i < keys.length; i++) {
            Animation key = keys[i];
            valueList.addItem(key.name());
        }
        valueList.setEnabled(true);
        durationSpinner.setEnabled(true);
        frequencySpinner.setEnabled(false);
    }

    private void setLEDMode() {
        valueList.removeAllItems();
        LED[] keys = LED.values();
        for (int i = 0; i < keys.length; i++) {
            LED key = keys[i];
            valueList.addItem(key.name());
        }
        valueList.setEnabled(true);
        durationSpinner.setEnabled(true);
        frequencySpinner.setEnabled(true);
    }

    private void setDefaultMode() {
        valueList.removeAllItems();
        valueList.setEnabled(false);
        durationSpinner.setEnabled(false);
        frequencySpinner.setEnabled(false);
    }

    private void switchMode() {
        if (commandList.getSelectedItem() == null || "None".equals((String) commandList.getSelectedItem())) {
            setDefaultMode();
            return;
        }
        String str = (String) commandList.getSelectedItem();
        if (str == null) {
            return;
        }
        COMMAND command = COMMAND.valueOf(str);
        if (command == COMMAND.PLAY_ANIMATION) {
            setAnimMode();
        } else if (command == COMMAND.PLAY_LED) {
            setLEDMode();
        } else {
            setDefaultMode();
        }
    }

    private void updateButtonSelection() {
        String controlKey = (String) buttonList.getSelectedValue();
        if (controlKey == null) {
            resetView();
            return;
        }
        list = map.getControls(CONTROL_KEY.valueOf(controlKey));
        jLabel4.setText((index + 1) + "");
        if (list == null) {
            list = new LinkedList<AssignableControl>();
            commandList.setSelectedIndex(0);
            setDefaultMode();
            return;
        }
        if (list.size() <= index) {
            commandList.setSelectedIndex(0);
            setDefaultMode();
            return;
        }
        AssignableControl control = list.get(index);
        commandList.setSelectedItem(control.getCommand().name());
        if (control.getCommand() == COMMAND.PLAY_ANIMATION) {
            setAnimMode();
            valueList.setSelectedItem(control.getAnim().name());
        } else if (control.getCommand() == COMMAND.PLAY_LED) {
            setLEDMode();
            valueList.setSelectedItem(control.getLed().name());
        } else {
            setDefaultMode();
        }
        durationSpinner.setValue(control.getDuration());
        frequencySpinner.setValue(control.getFrequency());
        delaySpinner.setValue(control.getDelay());
    }

    private void storeButtonCommands() {
        AssignableControl control = null;
        String controlString = (String) buttonList.getSelectedValue();
        String commandString = (String) commandList.getSelectedItem();
        if (controlString == null) {
            return;
        }
        CONTROL_KEY key = CONTROL_KEY.valueOf(controlString);
        if (key == null) {
            return;
        }
        if ("None".equals(commandString)) {
            if (list.size() > index) {
                list.remove(index);
            }
            return;
        }
        COMMAND command = COMMAND.valueOf((String) commandList.getSelectedItem());
        if (command == null) {
            return;
        }
        if (command == COMMAND.PLAY_ANIMATION) {
            Animation anim = Animation.valueOf((String) valueList.getSelectedItem());
            control = new AssignableControl(key, anim, (Integer) delaySpinner.getValue(), (Integer) durationSpinner.getValue());

        } else if (command == COMMAND.PLAY_LED) {
            LED led = LED.valueOf((String) valueList.getSelectedItem());
            float freq = frequencySpinner.getValue() instanceof Float ? (Float) frequencySpinner.getValue() : (Integer) frequencySpinner.getValue();
            control = new AssignableControl(key, led, (Integer) delaySpinner.getValue(), freq, (Integer) durationSpinner.getValue());
        } else {
            control = new AssignableControl(key, command, (Integer) delaySpinner.getValue());
        }
        list.add(index, control);
        if (list.size() > index + 1) {
            list.remove(index + 1);
        }
        map.setControls(key, list);
    }

    private void resetView() {
        setDefaultMode();
        index = 0;
        jLabel4.setText("1");
        commandList.setSelectedIndex(0);
        durationSpinner.setValue(0);
        frequencySpinner.setValue(0);
        delaySpinner.setValue(0);
    }

    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jScrollPane1 = new javax.swing.JScrollPane();
        buttonList = new javax.swing.JList();
        commandList = new javax.swing.JComboBox();
        valueList = new javax.swing.JComboBox();
        durationSpinner = new javax.swing.JSpinner();
        jLabel1 = new javax.swing.JLabel();
        jLabel2 = new javax.swing.JLabel();
        frequencySpinner = new javax.swing.JSpinner();
        jLabel3 = new javax.swing.JLabel();
        jToolBar1 = new javax.swing.JToolBar();
        jButton4 = new javax.swing.JButton();
        jPanel1 = new javax.swing.JPanel();
        jLabel4 = new javax.swing.JLabel();
        jPanel2 = new javax.swing.JPanel();
        jButton3 = new javax.swing.JButton();
        jSeparator3 = new javax.swing.JToolBar.Separator();
        storeButton = new javax.swing.JButton();
        jLabel5 = new javax.swing.JLabel();
        jSeparator2 = new javax.swing.JSeparator();
        jLabel6 = new javax.swing.JLabel();
        delaySpinner = new javax.swing.JSpinner();

        setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);
        setTitle("Controller Mapping");

        buttonList.setModel(new javax.swing.AbstractListModel() {
            String[] strings = { "Item 1", "Item 2", "Item 3", "Item 4", "Item 5" };
            public int getSize() { return strings.length; }
            public Object getElementAt(int i) { return strings[i]; }
        });
        buttonList.addListSelectionListener(new javax.swing.event.ListSelectionListener() {
            public void valueChanged(javax.swing.event.ListSelectionEvent evt) {
                listSelection(evt);
            }
        });
        jScrollPane1.setViewportView(buttonList);

        commandList.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "Item 1", "Item 2", "Item 3", "Item 4" }));
        commandList.addItemListener(new java.awt.event.ItemListener() {
            public void itemStateChanged(java.awt.event.ItemEvent evt) {
                updateCommandSelection(evt);
            }
        });

        valueList.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "Item 1", "Item 2", "Item 3", "Item 4" }));

        durationSpinner.addChangeListener(new javax.swing.event.ChangeListener() {
            public void stateChanged(javax.swing.event.ChangeEvent evt) {
                storeCommandz(evt);
            }
        });

        jLabel1.setText("Duration");

        jLabel2.setText("Frequency");

        frequencySpinner.setModel(new javax.swing.SpinnerNumberModel(Float.valueOf(0.0f), null, null, Float.valueOf(0.1f)));
        frequencySpinner.addChangeListener(new javax.swing.event.ChangeListener() {
            public void stateChanged(javax.swing.event.ChangeEvent evt) {
                storeCommandz(evt);
            }
        });

        jLabel3.setText("Button");

        jToolBar1.setFloatable(false);
        jToolBar1.setRollover(true);

        jButton4.setText("prev");
        jButton4.setFocusable(false);
        jButton4.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        jButton4.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        jButton4.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton4ActionPerformed(evt);
            }
        });
        jToolBar1.add(jButton4);

        javax.swing.GroupLayout jPanel1Layout = new javax.swing.GroupLayout(jPanel1);
        jPanel1.setLayout(jPanel1Layout);
        jPanel1Layout.setHorizontalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 35, Short.MAX_VALUE)
        );
        jPanel1Layout.setVerticalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 21, Short.MAX_VALUE)
        );

        jToolBar1.add(jPanel1);

        jLabel4.setText("1");
        jToolBar1.add(jLabel4);

        javax.swing.GroupLayout jPanel2Layout = new javax.swing.GroupLayout(jPanel2);
        jPanel2.setLayout(jPanel2Layout);
        jPanel2Layout.setHorizontalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 35, Short.MAX_VALUE)
        );
        jPanel2Layout.setVerticalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 21, Short.MAX_VALUE)
        );

        jToolBar1.add(jPanel2);

        jButton3.setText("next");
        jButton3.setFocusable(false);
        jButton3.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        jButton3.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        jButton3.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton3ActionPerformed(evt);
            }
        });
        jToolBar1.add(jButton3);
        jToolBar1.add(jSeparator3);

        storeButton.setText("store");
        storeButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                storeButtonActionPerformed(evt);
            }
        });
        jToolBar1.add(storeButton);

        jLabel5.setText("Commands");

        jLabel6.setText("Delay (ms)");

        delaySpinner.setModel(new javax.swing.SpinnerNumberModel(Integer.valueOf(0), null, null, Integer.valueOf(100)));
        delaySpinner.addChangeListener(new javax.swing.event.ChangeListener() {
            public void stateChanged(javax.swing.event.ChangeEvent evt) {
                storeCommandz(evt);
            }
        });

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jLabel3)
                    .addComponent(jScrollPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 145, Short.MAX_VALUE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(jLabel5, javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(valueList, javax.swing.GroupLayout.Alignment.LEADING, 0, 209, Short.MAX_VALUE)
                    .addComponent(commandList, javax.swing.GroupLayout.Alignment.LEADING, 0, 209, Short.MAX_VALUE)
                    .addGroup(javax.swing.GroupLayout.Alignment.LEADING, layout.createSequentialGroup()
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(jLabel1)
                            .addComponent(jLabel2)
                            .addComponent(jLabel6))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                            .addComponent(durationSpinner, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, 133, Short.MAX_VALUE)
                            .addComponent(frequencySpinner, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, 133, Short.MAX_VALUE)
                            .addComponent(delaySpinner, javax.swing.GroupLayout.DEFAULT_SIZE, 133, Short.MAX_VALUE)))
                    .addComponent(jSeparator2, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, 209, Short.MAX_VALUE)
                    .addComponent(jToolBar1, javax.swing.GroupLayout.DEFAULT_SIZE, 209, Short.MAX_VALUE))
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel5)
                    .addComponent(jLabel3))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(commandList, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(valueList, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                            .addComponent(jLabel1)
                            .addComponent(durationSpinner, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                            .addComponent(jLabel2)
                            .addComponent(frequencySpinner, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                            .addComponent(delaySpinner, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(jLabel6))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addComponent(jSeparator2, javax.swing.GroupLayout.PREFERRED_SIZE, 10, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jToolBar1, javax.swing.GroupLayout.PREFERRED_SIZE, 25, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addComponent(jScrollPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 227, Short.MAX_VALUE))
                .addContainerGap())
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void listSelection(javax.swing.event.ListSelectionEvent evt) {//GEN-FIRST:event_listSelection
        index = 0;
        updateButtonSelection();
    }//GEN-LAST:event_listSelection

    private void updateCommandSelection(java.awt.event.ItemEvent evt) {//GEN-FIRST:event_updateCommandSelection
        switchMode();
    }//GEN-LAST:event_updateCommandSelection

    private void storeCommandz(javax.swing.event.ChangeEvent evt) {//GEN-FIRST:event_storeCommandz
//        storeButtonCommands();
    }//GEN-LAST:event_storeCommandz

    private void storeButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_storeButtonActionPerformed
        storeButtonCommands();
    }//GEN-LAST:event_storeButtonActionPerformed

    private void jButton4ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton4ActionPerformed
        if (index > 0) {
            index--;
            updateButtonSelection();
        }
    }//GEN-LAST:event_jButton4ActionPerformed

    private void jButton3ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton3ActionPerformed
        if (index < list.size()) {
            index++;
            updateButtonSelection();
        }
    }//GEN-LAST:event_jButton3ActionPerformed
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JList buttonList;
    private javax.swing.JComboBox commandList;
    private javax.swing.JSpinner delaySpinner;
    private javax.swing.JSpinner durationSpinner;
    private javax.swing.JSpinner frequencySpinner;
    private javax.swing.JButton jButton3;
    private javax.swing.JButton jButton4;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JLabel jLabel4;
    private javax.swing.JLabel jLabel5;
    private javax.swing.JLabel jLabel6;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JPanel jPanel2;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JSeparator jSeparator2;
    private javax.swing.JToolBar.Separator jSeparator3;
    private javax.swing.JToolBar jToolBar1;
    private javax.swing.JButton storeButton;
    private javax.swing.JComboBox valueList;
    // End of variables declaration//GEN-END:variables
}
