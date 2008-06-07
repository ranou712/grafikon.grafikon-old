/*
 * EditNodeDialog.java
 *
 * Created on 28. září 2007, 16:49
 */
package net.parostroj.timetable.gui.dialogs;

import java.awt.event.ItemEvent;
import java.util.LinkedList;
import java.util.List;
import java.util.UUID;
import java.util.logging.Logger;
import javax.swing.DefaultListModel;
import javax.swing.JOptionPane;
import javax.swing.ListModel;
import net.parostroj.timetable.gui.views.NodeTypeWrapper;
import net.parostroj.timetable.model.Node;
import net.parostroj.timetable.model.NodeTrack;
import net.parostroj.timetable.model.NodeType;
import net.parostroj.timetable.utils.ResourceLoader;

/**
 * Edit dialog for node.
 *
 * @author jub
 */
public class EditNodeDialog extends javax.swing.JDialog {

    private static final Logger LOG = Logger.getLogger(EditNodeDialog.class.getName());
    private Node node;
    private List<NodeTrack> removed;
    private boolean modified;

    /** Creates new form EditNodeDialog */
    public EditNodeDialog(java.awt.Frame parent) {
        super(parent);
        initComponents();

        // fill combo box
        for (NodeType type : NodeType.values()) {
            typeComboBox.addItem(NodeTypeWrapper.getWrapper(type));
        }
    }

    public boolean isModified() {
        return modified;
    }

    public void setModified(boolean modified) {
        this.modified = modified;
    }

    public void setNode(Node node) {
        this.node = node;
        this.modified = false;
        this.updateValues();
    }

    private void updateSelectedTrack(NodeTrack track) {
        boolean enabled = track != null;
        lineEndCheckBox.setEnabled(enabled);
        platformCheckBox.setEnabled(enabled);
        if (enabled) {
            platformCheckBox.setSelected(track.isPlatform());
            lineEndCheckBox.setSelected(Boolean.TRUE.equals(track.getAttribute("line.end")));
        }
    }

    private void updateValues() {
        removed = new LinkedList<NodeTrack>();
        nameTextField.setText(node.getName());
        abbrTextField.setText(node.getAbbr());
        typeComboBox.setSelectedItem(NodeTypeWrapper.getWrapper(node.getType()));
        signalsCheckBox.setSelected("new.signals".equals(node.getAttribute("interlocking.plant")));
        controlCheckBox.setSelected(Boolean.TRUE.equals(node.getAttribute("control.station")));
        trapezoidCheckBox.setSelected(Boolean.TRUE.equals(node.getAttribute("trapezoid.sign")));

        // clear checkboxes for tracks
        platformCheckBox.setSelected(false);
        lineEndCheckBox.setSelected(false);

        // get node tracks
        DefaultListModel listModel = new DefaultListModel();
        for (NodeTrack track : node.getTracks()) {
            listModel.addElement(track);
        }
        nodeTrackList.setModel(listModel);
    }

    private void writeValuesBack() {
        this.modified = true;
        if (!"".equals(nameTextField.getText())) {
            node.setName(nameTextField.getText());
        }
        if (!"".equals(abbrTextField.getText())) {
            node.setAbbr(abbrTextField.getText());
        }
        if (signalsCheckBox.isSelected()) {
            node.setAttribute("interlocking.plant", "new.signals");
        } else {
            node.removeAttribute("interlocking.plant");
        }
        node.setType(((NodeTypeWrapper) typeComboBox.getSelectedItem()).getType());
        node.setAttribute("control.station", controlCheckBox.isSelected());
        node.setAttribute("trapezoid.sign", trapezoidCheckBox.isSelected());

        // wipe out all previous tracks
        node.removeAllTracks();

        // write node tracks back
        ListModel m = nodeTrackList.getModel();
        for (int i = 0; i < m.getSize(); i++) {
            NodeTrack t = (NodeTrack) m.getElementAt(i);
            node.addTrack(t);
        }
    }

    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        javax.swing.JLabel jLabel1 = new javax.swing.JLabel();
        nameTextField = new javax.swing.JTextField();
        javax.swing.JLabel jLabel2 = new javax.swing.JLabel();
        abbrTextField = new javax.swing.JTextField();
        javax.swing.JLabel jLabel3 = new javax.swing.JLabel();
        typeComboBox = new javax.swing.JComboBox();
        signalsCheckBox = new javax.swing.JCheckBox();
        controlCheckBox = new javax.swing.JCheckBox();
        scrollPane = new javax.swing.JScrollPane();
        nodeTrackList = new javax.swing.JList();
        newNodeTrackButton = new javax.swing.JButton();
        renameNodeTrackButton = new javax.swing.JButton();
        deleteNodeTrackButton = new javax.swing.JButton();
        okButton = new javax.swing.JButton();
        cancelButton = new javax.swing.JButton();
        trapezoidCheckBox = new javax.swing.JCheckBox();
        platformCheckBox = new javax.swing.JCheckBox();
        lineEndCheckBox = new javax.swing.JCheckBox();

        setDefaultCloseOperation(javax.swing.WindowConstants.DO_NOTHING_ON_CLOSE);
        setModal(true);
        setResizable(false);

        jLabel1.setText(ResourceLoader.getString("ne.name")); // NOI18N

        jLabel2.setText(ResourceLoader.getString("ne.abbr")); // NOI18N

        jLabel3.setText(ResourceLoader.getString("ne.type")); // NOI18N

        typeComboBox.addItemListener(new java.awt.event.ItemListener() {
            public void itemStateChanged(java.awt.event.ItemEvent evt) {
                typeComboBoxItemStateChanged(evt);
            }
        });

        signalsCheckBox.setText(ResourceLoader.getString("ne.new.signals")); // NOI18N

        controlCheckBox.setText(ResourceLoader.getString("ne.control.station")); // NOI18N

        nodeTrackList.setSelectionMode(javax.swing.ListSelectionModel.SINGLE_SELECTION);
        nodeTrackList.addListSelectionListener(new javax.swing.event.ListSelectionListener() {
            public void valueChanged(javax.swing.event.ListSelectionEvent evt) {
                nodeTrackListValueChanged(evt);
            }
        });
        scrollPane.setViewportView(nodeTrackList);

        newNodeTrackButton.setText(ResourceLoader.getString("button.new")); // NOI18N
        newNodeTrackButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                newNodeTrackButtonActionPerformed(evt);
            }
        });

        renameNodeTrackButton.setText(ResourceLoader.getString("button.rename")); // NOI18N
        renameNodeTrackButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                renameNodeTrackButtonActionPerformed(evt);
            }
        });

        deleteNodeTrackButton.setText(ResourceLoader.getString("button.delete")); // NOI18N
        deleteNodeTrackButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                deleteNodeTrackButtonActionPerformed(evt);
            }
        });

        okButton.setText(ResourceLoader.getString("button.ok")); // NOI18N
        okButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                okButtonActionPerformed(evt);
            }
        });

        cancelButton.setText(ResourceLoader.getString("button.cancel")); // NOI18N
        cancelButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                cancelButtonActionPerformed(evt);
            }
        });

        trapezoidCheckBox.setText(ResourceLoader.getString("ne.trapezoid.table")); // NOI18N

        platformCheckBox.setText(ResourceLoader.getString("ne.platform")); // NOI18N
        platformCheckBox.setEnabled(false);
        platformCheckBox.addItemListener(new java.awt.event.ItemListener() {
            public void itemStateChanged(java.awt.event.ItemEvent evt) {
                platformCheckBoxItemStateChanged(evt);
            }
        });

        lineEndCheckBox.setText(ResourceLoader.getString("ne.line.end")); // NOI18N
        lineEndCheckBox.setEnabled(false);
        lineEndCheckBox.addItemListener(new java.awt.event.ItemListener() {
            public void itemStateChanged(java.awt.event.ItemEvent evt) {
                lineEndCheckBoxItemStateChanged(evt);
            }
        });

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(platformCheckBox)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(lineEndCheckBox))
                    .addComponent(scrollPane, javax.swing.GroupLayout.DEFAULT_SIZE, 259, Short.MAX_VALUE)
                    .addComponent(trapezoidCheckBox)
                    .addGroup(layout.createSequentialGroup()
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                            .addComponent(jLabel3, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(jLabel2, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(jLabel1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(abbrTextField, javax.swing.GroupLayout.DEFAULT_SIZE, 215, Short.MAX_VALUE)
                            .addComponent(nameTextField, javax.swing.GroupLayout.DEFAULT_SIZE, 215, Short.MAX_VALUE)
                            .addComponent(typeComboBox, 0, 215, Short.MAX_VALUE)))
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(newNodeTrackButton, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(renameNodeTrackButton, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(deleteNodeTrackButton, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                        .addComponent(okButton)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(cancelButton))
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(signalsCheckBox)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(controlCheckBox)))
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(nameTextField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel1))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel2)
                    .addComponent(abbrTextField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel3)
                    .addComponent(typeComboBox, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(signalsCheckBox)
                    .addComponent(controlCheckBox))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(trapezoidCheckBox)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(scrollPane, javax.swing.GroupLayout.PREFERRED_SIZE, 109, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(platformCheckBox)
                    .addComponent(lineEndCheckBox))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(newNodeTrackButton)
                    .addComponent(renameNodeTrackButton)
                    .addComponent(deleteNodeTrackButton))
                .addGap(18, 18, 18)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(cancelButton)
                    .addComponent(okButton))
                .addContainerGap())
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void okButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_okButtonActionPerformed
        this.writeValuesBack();
        this.setVisible(false);
    }//GEN-LAST:event_okButtonActionPerformed

    private void cancelButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_cancelButtonActionPerformed
        this.setVisible(false);
    }//GEN-LAST:event_cancelButtonActionPerformed

    private void newNodeTrackButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_newNodeTrackButtonActionPerformed
        String name = JOptionPane.showInputDialog(this, "");
        NodeTrack track = new NodeTrack(UUID.randomUUID().toString(), name);
        track.setPlatform(true);
        ((DefaultListModel) nodeTrackList.getModel()).addElement(track);
    }//GEN-LAST:event_newNodeTrackButtonActionPerformed

    private void renameNodeTrackButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_renameNodeTrackButtonActionPerformed
        if (!nodeTrackList.isSelectionEmpty()) {
            NodeTrack track = (NodeTrack) nodeTrackList.getSelectedValue();
            String name = JOptionPane.showInputDialog(this, "", track.getNumber());
            if (name != null && !name.equals("")) {
                track.setNumber(name);
            }
            nodeTrackList.repaint();
        }
    }//GEN-LAST:event_renameNodeTrackButtonActionPerformed

    private void deleteNodeTrackButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_deleteNodeTrackButtonActionPerformed
        // removing
        if (!nodeTrackList.isSelectionEmpty()) {
            NodeTrack track = (NodeTrack) nodeTrackList.getSelectedValue();
            // test node track
            if (!track.isEmpty()) {
                JOptionPane.showMessageDialog(this, ResourceLoader.getString("nl.error.notempty"), null, JOptionPane.ERROR_MESSAGE);
            } else {
                ((DefaultListModel) nodeTrackList.getModel()).removeElement(track);
                // add to removed
                removed.add(track);
            }
        }
    }//GEN-LAST:event_deleteNodeTrackButtonActionPerformed
    private NodeTypeWrapper lastSelectedType;

    private void typeComboBoxItemStateChanged(java.awt.event.ItemEvent evt) {//GEN-FIRST:event_typeComboBoxItemStateChanged
        if (evt.getStateChange() == ItemEvent.SELECTED) {
            // selected ... (SIGNAL is allowed only if there is only one track)
            if (((NodeTypeWrapper) typeComboBox.getSelectedItem()).getType() == NodeType.SIGNAL) {
                if (nodeTrackList.getModel().getSize() != 1) {
                    typeComboBox.setSelectedItem(lastSelectedType);
                }
            }

            boolean signal = (((NodeTypeWrapper) typeComboBox.getSelectedItem()).getType() == NodeType.SIGNAL);
            newNodeTrackButton.setEnabled(!signal);
            renameNodeTrackButton.setEnabled(!signal);
            deleteNodeTrackButton.setEnabled(!signal);
        } else if (evt.getStateChange() == ItemEvent.DESELECTED) {
            lastSelectedType = (NodeTypeWrapper) evt.getItem();
        }
    }//GEN-LAST:event_typeComboBoxItemStateChanged

    private void nodeTrackListValueChanged(javax.swing.event.ListSelectionEvent evt) {//GEN-FIRST:event_nodeTrackListValueChanged
        if (!evt.getValueIsAdjusting()) {
            NodeTrack selected = (NodeTrack) nodeTrackList.getSelectedValue();
            this.updateSelectedTrack(selected);
        }
    }//GEN-LAST:event_nodeTrackListValueChanged

    private void platformCheckBoxItemStateChanged(java.awt.event.ItemEvent evt) {//GEN-FIRST:event_platformCheckBoxItemStateChanged
        if (evt.getStateChange() == ItemEvent.SELECTED) {
            NodeTrack selected = (NodeTrack) nodeTrackList.getSelectedValue();
            if (selected != null) {
                selected.setPlatform(platformCheckBox.isSelected());
            }
        }
    }//GEN-LAST:event_platformCheckBoxItemStateChanged

    private void lineEndCheckBoxItemStateChanged(java.awt.event.ItemEvent evt) {//GEN-FIRST:event_lineEndCheckBoxItemStateChanged
        if (evt.getStateChange() == ItemEvent.SELECTED) {
            NodeTrack selected = (NodeTrack) nodeTrackList.getSelectedValue();
            if (selected != null) {
                selected.setAttribute("line.end", lineEndCheckBox.isSelected());
            }
        }
    }//GEN-LAST:event_lineEndCheckBoxItemStateChanged
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JTextField abbrTextField;
    private javax.swing.JButton cancelButton;
    private javax.swing.JCheckBox controlCheckBox;
    private javax.swing.JButton deleteNodeTrackButton;
    private javax.swing.JCheckBox lineEndCheckBox;
    private javax.swing.JTextField nameTextField;
    private javax.swing.JButton newNodeTrackButton;
    private javax.swing.JList nodeTrackList;
    private javax.swing.JButton okButton;
    private javax.swing.JCheckBox platformCheckBox;
    private javax.swing.JButton renameNodeTrackButton;
    private javax.swing.JScrollPane scrollPane;
    private javax.swing.JCheckBox signalsCheckBox;
    private javax.swing.JCheckBox trapezoidCheckBox;
    private javax.swing.JComboBox typeComboBox;
    // End of variables declaration//GEN-END:variables
}