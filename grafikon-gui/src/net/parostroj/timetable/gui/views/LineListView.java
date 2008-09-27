/*
 * LineListView.java
 *
 * Created on 28. září 2007, 15:03
 */

package net.parostroj.timetable.gui.views;

import java.awt.Frame;
import java.util.Collection;
import java.util.List;
import java.util.UUID;
import javax.swing.DefaultListModel;
import javax.swing.JOptionPane;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import net.parostroj.timetable.gui.ApplicationModel;
import net.parostroj.timetable.gui.ApplicationModelEvent;
import net.parostroj.timetable.gui.ApplicationModelEventType;
import net.parostroj.timetable.gui.ApplicationModelListener;
import net.parostroj.timetable.gui.dialogs.CreateLineDialog;
import net.parostroj.timetable.gui.dialogs.EditLineDialog;
import net.parostroj.timetable.model.Line;
import net.parostroj.timetable.model.LineTrack;
import net.parostroj.timetable.model.Node;
import net.parostroj.timetable.model.Route;
import net.parostroj.timetable.utils.ResourceLoader;
import net.parostroj.timetable.utils.Tuple;

/**
 * List of lines and an editing functions.
 *
 * @author jub
 */
public class LineListView extends javax.swing.JPanel implements ApplicationModelListener, ListSelectionListener {
    
    private ApplicationModel model;
    
    private CreateLineDialog createLineDialog;
    
    private EditLineDialog editLineDialog;
    
    /** Creates new form NodesListView */
    public LineListView() {
        initComponents();
        
        createLineDialog = new CreateLineDialog((Frame)this.getTopLevelAncestor(), true);
        editLineDialog = new EditLineDialog((Frame)this.getTopLevelAncestor(), true);
    }
    
    public void setModel(ApplicationModel model) {
        this.model = model;
        model.addListener(this);
        lineList.addListSelectionListener(this);
        this.updateLineList();
        createLineDialog.setModel(model);
        editLineDialog.setModel(model);
    }

    @Override
    public void modelChanged(ApplicationModelEvent event) {
        switch (event.getType()) {
        case SET_DIAGRAM_CHANGED:
            this.updateLineList();
            break;
        default:
            // nothing
        }
    }

    @Override
    public void valueChanged(ListSelectionEvent e) {
        if (!e.getValueIsAdjusting()) {
            editButton.setEnabled(e.getFirstIndex() != -1);
            deleteButton.setEnabled(e.getFirstIndex() != -1);
        }
    }
    
    private void updateLineList() {
        DefaultListModel listModel = new DefaultListModel();
        if (model.getDiagram() != null) {
            Collection<Line> lines = model.getDiagram().getNet().getLines();
            for (Line l : lines) {
                listModel.addElement(l);
            }
        }
            
        lineList.setModel(listModel);
        deleteButton.setEnabled(false);
        editButton.setEnabled(false);
    }
    
    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        scrollPane = new javax.swing.JScrollPane();
        lineList = new javax.swing.JList();
        newButton = new javax.swing.JButton();
        editButton = new javax.swing.JButton();
        deleteButton = new javax.swing.JButton();
        javax.swing.JLabel jLabel1 = new javax.swing.JLabel();

        lineList.setSelectionMode(javax.swing.ListSelectionModel.SINGLE_SELECTION);
        scrollPane.setViewportView(lineList);

        newButton.setText(ResourceLoader.getString("button.new")); // NOI18N
        newButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                newButtonActionPerformed(evt);
            }
        });

        editButton.setText(ResourceLoader.getString("button.edit")); // NOI18N
        editButton.setEnabled(false);
        editButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                editButtonActionPerformed(evt);
            }
        });

        deleteButton.setText(ResourceLoader.getString("button.delete")); // NOI18N
        deleteButton.setEnabled(false);
        deleteButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                deleteButtonActionPerformed(evt);
            }
        });

        jLabel1.setText(ResourceLoader.getString("ne.title")); // NOI18N

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addComponent(newButton, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(editButton, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(deleteButton, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
            .addComponent(scrollPane, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, 229, Short.MAX_VALUE)
            .addComponent(jLabel1, javax.swing.GroupLayout.DEFAULT_SIZE, 229, Short.MAX_VALUE)
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addComponent(jLabel1)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(scrollPane, javax.swing.GroupLayout.DEFAULT_SIZE, 24, Short.MAX_VALUE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(newButton)
                    .addComponent(editButton)
                    .addComponent(deleteButton)))
        );
    }// </editor-fold>//GEN-END:initComponents

    private void deleteButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_deleteButtonActionPerformed
        if (lineList.getSelectedIndex() != -1) {
            Line line = (Line)lineList.getSelectedValue();
            if (!line.isEmpty() || checkRoutesForLine(line, model.getDiagram().getRoutes())) {
                JOptionPane.showMessageDialog(this, ResourceLoader.getString("nl.error.notempty"),ResourceLoader.getString("nl.error.title"),JOptionPane.ERROR_MESSAGE);
            } else {
                model.getDiagram().getNet().removeLine(line);
                this.updateLineList();
            }
        }
        
    }//GEN-LAST:event_deleteButtonActionPerformed

    private boolean checkRoutesForLine(Line line, List<Route> routes) {
        for (Route route : routes) {
            if (route.contains(line))
                return true;
        }
        return false;
    }
    
    private void editButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_editButtonActionPerformed
        editLineDialog.setLine((Line)lineList.getSelectedValue());
        editLineDialog.setLocationRelativeTo(this);
        editLineDialog.setVisible(true);
        if (editLineDialog.isModified()) {
            lineList.repaint();
            model.fireEvent(new ApplicationModelEvent(ApplicationModelEventType.MODIFIED_LINE, model, (Line)lineList.getSelectedValue()));
        }
    }//GEN-LAST:event_editButtonActionPerformed

    private void newButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_newButtonActionPerformed
        // add new ...
        if (model.getDiagram() != null) {
            createLineDialog.updateNodes();
            createLineDialog.setLocationRelativeTo(this);
            createLineDialog.setVisible(true);
            
            // test if there ok was selected
            if (createLineDialog.getSelectedNodes() == null)
                return;
            
            Tuple<Node> selected = createLineDialog.getSelectedNodes();
            // create new line
            Line l = new Line(UUID.randomUUID().toString(), 1000, selected.first, selected.second, Line.UNLIMITED_SPEED);
            LineTrack track = new LineTrack(UUID.randomUUID().toString(), "1");
            l.addTrack(track);
            model.getDiagram().getNet().addLine(selected.first, selected.second, l);
            
            model.fireEvent(new ApplicationModelEvent(ApplicationModelEventType.NEW_LINE, model, l));
            this.updateLineList();
        }
    }//GEN-LAST:event_newButtonActionPerformed
    
    
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton deleteButton;
    private javax.swing.JButton editButton;
    private javax.swing.JList lineList;
    private javax.swing.JButton newButton;
    private javax.swing.JScrollPane scrollPane;
    // End of variables declaration//GEN-END:variables
    
}
