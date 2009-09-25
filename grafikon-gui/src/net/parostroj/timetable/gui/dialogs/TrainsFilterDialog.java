/*
 * TrainsFilterDialog.java
 *
 * Created on 26.11.2008, 13:26:04
 */
package net.parostroj.timetable.gui.dialogs;

import java.awt.GridLayout;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import javax.swing.JCheckBox;
import net.parostroj.timetable.model.TrainDiagram;
import net.parostroj.timetable.model.TrainType;
import net.parostroj.timetable.utils.Pair;
import net.parostroj.timetable.utils.ResourceLoader;

/**
 * Dialog for filtering trains.
 * 
 * @author jub
 */
public class TrainsFilterDialog extends javax.swing.JDialog {

    private List<Pair<TrainType,JCheckBox>> typesList;
    private Set<TrainType> selectedTypes;

    /** Creates new form TrainsFilterDialog */
    public TrainsFilterDialog(java.awt.Frame parent, boolean modal) {
        super(parent, modal);
        initComponents();
    }

    public void setTrainTypes(TrainDiagram diagram, Set<TrainType> types) {
        if (diagram == null)
            return;
        typesPanel.removeAll();
        typesPanel.setLayout(new GridLayout(diagram.getTrainTypes().size(), 1));
        typesList = new LinkedList<Pair<TrainType, JCheckBox>>();
        for (TrainType type : diagram.getTrainTypes()) {
            JCheckBox checkBox = new JCheckBox(type.getAbbr() + " (" + type.getDesc() + ")");
            typesList.add(new Pair<TrainType, JCheckBox>(type, checkBox));
            checkBox.setSelected(types.contains(type));
            typesPanel.add(checkBox);
        }
        this.pack();
    }

    public Set<TrainType> getSelectedTypes() {
        return selectedTypes;
    }

    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        typesPanel = new javax.swing.JPanel();
        okPanel = new javax.swing.JPanel();
        okButton = new javax.swing.JButton();

        setDefaultCloseOperation(javax.swing.WindowConstants.DO_NOTHING_ON_CLOSE);

        typesPanel.setLayout(new java.awt.GridLayout());
        getContentPane().add(typesPanel, java.awt.BorderLayout.CENTER);

        okPanel.setLayout(new java.awt.FlowLayout(java.awt.FlowLayout.RIGHT));

        okButton.setText(ResourceLoader.getString("button.ok")); // NOI18N
        okButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                okButtonActionPerformed(evt);
            }
        });
        okPanel.add(okButton);

        getContentPane().add(okPanel, java.awt.BorderLayout.SOUTH);

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void okButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_okButtonActionPerformed
        selectedTypes = new HashSet<TrainType>();
        for (Pair<TrainType, JCheckBox> pair : typesList) {
            if (pair.second.isSelected())
                selectedTypes.add(pair.first);
        }
        this.setVisible(false);
    }//GEN-LAST:event_okButtonActionPerformed

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton okButton;
    private javax.swing.JPanel okPanel;
    private javax.swing.JPanel typesPanel;
    // End of variables declaration//GEN-END:variables

}
