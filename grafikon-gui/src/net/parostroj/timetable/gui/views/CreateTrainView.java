/*
 * CreateTrainView.java
 *
 * Created on 26. srpen 2007, 12:53
 */

package net.parostroj.timetable.gui.views;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JOptionPane;
import net.parostroj.timetable.actions.NodeSort;

import net.parostroj.timetable.gui.ApplicationModel;
import net.parostroj.timetable.gui.commands.CommandException;
import net.parostroj.timetable.gui.commands.CreateTrainCommand;
import net.parostroj.timetable.gui.dialogs.ThroughNodesDialog;
import net.parostroj.timetable.model.Node;
import net.parostroj.timetable.model.NodeType;
import net.parostroj.timetable.model.TrainType;
import net.parostroj.timetable.utils.ResourceLoader;
import net.parostroj.timetable.utils.TimeConverter;

/**
 * View for dialog with creating of the train.
 *
 * @author jub
 */
public class CreateTrainView extends javax.swing.JPanel {
    
    private static final Logger LOG = Logger.getLogger(CreateTrainView.class.getName());

    private ApplicationModel model;
    
    private ThroughNodesDialog tnDialog;
    
    private List<Node> throughNodes;
    
    /** 
     * Creates new form CreateTrainView 
     */
    public CreateTrainView() {
        initComponents();
        tnDialog = new ThroughNodesDialog(null, true);
    }

    public void setModel(ApplicationModel model) {
        this.model = model;
        tnDialog.setModel(model);
        this.updateView();
    }
    
    private void updateView() {
        DefaultComboBoxModel fromModel = new DefaultComboBoxModel();
        DefaultComboBoxModel toModel = new DefaultComboBoxModel();
        
        Collection<Node> v = model.getDiagram().getNet().getNodes();
        NodeSort sort = new NodeSort(NodeSort.Type.ASC);
        List<Node> list = sort.sort(v);
        
        for (Node node : list) {
            if (node.getType() != NodeType.SIGNAL) {
                fromModel.addElement(node);
                toModel.addElement(node);
            }
        }
        
        fromComboBox.setModel(fromModel);
        toComboBox.setModel(toModel);
        
        // model for train types
        typeComboBox.setModel(new DefaultComboBoxModel(model.getDiagram().getTrainTypes().toArray()));
        
        // reset through nodes
        throughNodes = new ArrayList<Node>();
        throughTextField.setText(throughNodes.toString());
    }
    
    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        javax.swing.JLabel jLabel1 = new javax.swing.JLabel();
        fromComboBox = new javax.swing.JComboBox();
        javax.swing.JLabel jLabel2 = new javax.swing.JLabel();
        toComboBox = new javax.swing.JComboBox();
        speedTextField = new javax.swing.JTextField();
        javax.swing.JLabel jLabel3 = new javax.swing.JLabel();
        stopTextField = new javax.swing.JTextField();
        javax.swing.JLabel jLabel4 = new javax.swing.JLabel();
        cancelButton = new javax.swing.JButton();
        okButton = new javax.swing.JButton();
        javax.swing.JLabel jLabel5 = new javax.swing.JLabel();
        nameTextField = new javax.swing.JTextField();
        javax.swing.JLabel jLabel6 = new javax.swing.JLabel();
        typeComboBox = new javax.swing.JComboBox();
        javax.swing.JLabel jLabel7 = new javax.swing.JLabel();
        startTimeTextField = new javax.swing.JTextField();
        commentTextField = new javax.swing.JTextField();
        javax.swing.JLabel jLabel8 = new javax.swing.JLabel();
        dieselCheckBox = new javax.swing.JCheckBox();
        electricCheckBox = new javax.swing.JCheckBox();
        javax.swing.JLabel jLabel9 = new javax.swing.JLabel();
        throughButton = new javax.swing.JButton();
        throughTextField = new javax.swing.JTextField();

        jLabel1.setText(ResourceLoader.getString("from.node")); // NOI18N

        jLabel2.setText(ResourceLoader.getString("to.node")); // NOI18N

        jLabel3.setText(ResourceLoader.getString("create.train.speed")); // NOI18N

        jLabel4.setText(ResourceLoader.getString("create.train.stop")); // NOI18N

        cancelButton.setText(ResourceLoader.getString("button.cancel")); // NOI18N
        cancelButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                cancelButtonActionPerformed(evt);
            }
        });

        okButton.setText(ResourceLoader.getString("button.ok")); // NOI18N
        okButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                okButtonActionPerformed(evt);
            }
        });

        jLabel5.setText(ResourceLoader.getString("create.train.number")); // NOI18N

        jLabel6.setText(ResourceLoader.getString("create.train.type")); // NOI18N

        jLabel7.setText(ResourceLoader.getString("create.train.starttime")); // NOI18N

        jLabel8.setText(ResourceLoader.getString("create.train.description")); // NOI18N

        dieselCheckBox.setText(ResourceLoader.getString("create.train.diesel")); // NOI18N
        dieselCheckBox.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 0, 0));
        dieselCheckBox.setMargin(new java.awt.Insets(0, 0, 0, 0));

        electricCheckBox.setText(ResourceLoader.getString("create.train.electric")); // NOI18N
        electricCheckBox.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 0, 0));
        electricCheckBox.setMargin(new java.awt.Insets(0, 0, 0, 0));

        jLabel9.setText(ResourceLoader.getString("create.train.through")); // NOI18N

        throughButton.setText(ResourceLoader.getString("create.train.throughbutton")); // NOI18N
        throughButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                throughButtonActionPerformed(evt);
            }
        });

        throughTextField.setEditable(false);

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                        .addComponent(okButton)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(cancelButton))
                    .addGroup(layout.createSequentialGroup()
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                                .addComponent(jLabel8, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                .addComponent(jLabel6, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                .addComponent(jLabel5, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, 48, Short.MAX_VALUE)
                                .addComponent(jLabel1, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, 48, Short.MAX_VALUE)
                                .addComponent(jLabel4, javax.swing.GroupLayout.Alignment.TRAILING, 0, 0, Short.MAX_VALUE)
                                .addComponent(jLabel3, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, 48, Short.MAX_VALUE)
                                .addComponent(jLabel2, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, 48, Short.MAX_VALUE))
                            .addComponent(jLabel9))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(fromComboBox, 0, 235, Short.MAX_VALUE)
                            .addComponent(toComboBox, 0, 235, Short.MAX_VALUE)
                            .addGroup(layout.createSequentialGroup()
                                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING, false)
                                    .addComponent(stopTextField, javax.swing.GroupLayout.Alignment.LEADING)
                                    .addComponent(speedTextField, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, 69, Short.MAX_VALUE))
                                .addGap(18, 18, 18)
                                .addComponent(jLabel7)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(startTimeTextField, javax.swing.GroupLayout.PREFERRED_SIZE, 81, javax.swing.GroupLayout.PREFERRED_SIZE))
                            .addComponent(nameTextField, javax.swing.GroupLayout.DEFAULT_SIZE, 235, Short.MAX_VALUE)
                            .addComponent(typeComboBox, 0, 235, Short.MAX_VALUE)
                            .addComponent(commentTextField, javax.swing.GroupLayout.DEFAULT_SIZE, 235, Short.MAX_VALUE)
                            .addGroup(layout.createSequentialGroup()
                                .addComponent(dieselCheckBox)
                                .addGap(18, 18, 18)
                                .addComponent(electricCheckBox))
                            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                                .addComponent(throughTextField, javax.swing.GroupLayout.DEFAULT_SIZE, 174, Short.MAX_VALUE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(throughButton)))))
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel6)
                    .addComponent(typeComboBox, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(dieselCheckBox)
                    .addComponent(electricCheckBox))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel5)
                    .addComponent(nameTextField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(commentTextField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel8))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(fromComboBox, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel1))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(toComboBox, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel2))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel9)
                    .addComponent(throughButton)
                    .addComponent(throughTextField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(speedTextField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel3)
                    .addComponent(jLabel7)
                    .addComponent(startTimeTextField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(stopTextField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel4))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(cancelButton)
                    .addComponent(okButton))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
    }// </editor-fold>//GEN-END:initComponents

private void okButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_okButtonActionPerformed
    try {
        // test needed values
        try {
            Integer.valueOf(speedTextField.getText());
        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(this.getParent(), ResourceLoader.getString("create.train.trainspeedmissing"),
                    ResourceLoader.getString("create.train.error"), JOptionPane.ERROR_MESSAGE);
            return;
        }
        
        if (nameTextField.getText() == null || nameTextField.getText().trim().equals("")) {
            JOptionPane.showMessageDialog(this.getParent(), ResourceLoader.getString("create.train.trainnamemissing"),
                    ResourceLoader.getString("create.train.error"), JOptionPane.ERROR_MESSAGE);
            return;
        }
        
        if (fromComboBox.getSelectedItem() == null || toComboBox.getSelectedItem() == null) {
            JOptionPane.showMessageDialog(this.getParent(), "",
                    ResourceLoader.getString("create.train.error"), JOptionPane.ERROR_MESSAGE);
            return;
        }
        
        // create command ...
        CreateTrainCommand createCommand = new CreateTrainCommand(
                nameTextField.getText(),
                (TrainType)typeComboBox.getSelectedItem(),
                Integer.valueOf(speedTextField.getText()),
                (Node)fromComboBox.getSelectedItem(),
                (Node)toComboBox.getSelectedItem(),
                throughNodes,
                TimeConverter.convertFromTextToInt(startTimeTextField.getText()),
                (stopTextField.getText().equals("") ? 0 : Integer.valueOf(stopTextField.getText()) * 60),
                commentTextField.getText(),
                dieselCheckBox.isSelected(),
                electricCheckBox.isSelected());
        // execute command
        model.applyCommand(createCommand);
        // hide dialog
        this.closeDialog();
    } catch (CommandException e) {
        LOG.log(Level.WARNING, "Error executing create train command.", e);
        JOptionPane.showMessageDialog(this.getParent(), ResourceLoader.getString("create.train.createtrainerror"),
                    ResourceLoader.getString("create.train.error"), JOptionPane.ERROR_MESSAGE);
    }
}//GEN-LAST:event_okButtonActionPerformed

private void cancelButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_cancelButtonActionPerformed
    // hide dialog
    this.closeDialog();
}//GEN-LAST:event_cancelButtonActionPerformed

private void throughButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_throughButtonActionPerformed
    // show through dialog
    tnDialog.setNodes(throughNodes);
    tnDialog.setLocationRelativeTo(this);
    tnDialog.setVisible(true);
    throughNodes = tnDialog.getNodes();
    throughTextField.setText(throughNodes.toString());
}//GEN-LAST:event_throughButtonActionPerformed
    
    private void closeDialog() {
        this.getTopLevelAncestor().setVisible(false);
    }
    
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton cancelButton;
    private javax.swing.JTextField commentTextField;
    private javax.swing.JCheckBox dieselCheckBox;
    private javax.swing.JCheckBox electricCheckBox;
    private javax.swing.JComboBox fromComboBox;
    private javax.swing.JTextField nameTextField;
    private javax.swing.JButton okButton;
    private javax.swing.JTextField speedTextField;
    private javax.swing.JTextField startTimeTextField;
    private javax.swing.JTextField stopTextField;
    private javax.swing.JButton throughButton;
    private javax.swing.JTextField throughTextField;
    private javax.swing.JComboBox toComboBox;
    private javax.swing.JComboBox typeComboBox;
    // End of variables declaration//GEN-END:variables
    
}