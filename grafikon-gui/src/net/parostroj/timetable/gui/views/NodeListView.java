/*
 * NodesListView.java
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
import net.parostroj.timetable.actions.NodeSort;
import net.parostroj.timetable.gui.ApplicationModel;
import net.parostroj.timetable.gui.ApplicationModelEvent;
import net.parostroj.timetable.gui.ApplicationModelEventType;
import net.parostroj.timetable.gui.ApplicationModelListener;
import net.parostroj.timetable.gui.dialogs.EditNodeDialog;
import net.parostroj.timetable.model.Node;
import net.parostroj.timetable.model.NodeTrack;
import net.parostroj.timetable.model.NodeType;
import net.parostroj.timetable.utils.ResourceLoader;

/**
 * List of nodes and an editing functions.
 *
 * @author jub
 */
public class NodeListView extends javax.swing.JPanel implements ApplicationModelListener, ListSelectionListener {
    
    private ApplicationModel model;
    
    private EditNodeDialog editDialog;
    
    /** Creates new form NodesListView */
    public NodeListView() {
        initComponents();
        
        editDialog = new EditNodeDialog((Frame)this.getTopLevelAncestor());
    }
    
    public void setModel(ApplicationModel model) {
        this.model = model;
        model.addListener(this);
        nodeList.addListSelectionListener(this);
        this.updateNodeList();
    }

    @Override
    public void modelChanged(ApplicationModelEvent event) {
        switch (event.getType()) {
        case SET_DIAGRAM_CHANGED:
            this.updateNodeList();
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
    
    private void updateNodeList() {
        DefaultListModel listModel = new DefaultListModel();
        if (model.getDiagram() != null) {
            Collection<Node> nodes = model.getDiagram().getNet().getNodes();
            NodeSort sort = new NodeSort(NodeSort.Type.ASC);
            List<Node> sorted = sort.sort(nodes);
            for (Node n : sorted) {
                listModel.addElement(n);
            }
        }
            
        nodeList.setModel(listModel);
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
        nodeList = new javax.swing.JList();
        newButton = new javax.swing.JButton();
        editButton = new javax.swing.JButton();
        deleteButton = new javax.swing.JButton();
        javax.swing.JLabel jLabel1 = new javax.swing.JLabel();

        nodeList.setSelectionMode(javax.swing.ListSelectionModel.SINGLE_SELECTION);
        scrollPane.setViewportView(nodeList);

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

        jLabel1.setText(ResourceLoader.getString("nl.title")); // NOI18N

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
        if (nodeList.getSelectedIndex() != -1) {
            Node n = (Node)nodeList.getSelectedValue();
            if (!n.isEmpty() || !model.getDiagram().getNet().getLinesOf(n).isEmpty()) {
                JOptionPane.showMessageDialog(this, ResourceLoader.getString("nl.error.notempty"),ResourceLoader.getString("nl.error.title"),JOptionPane.ERROR_MESSAGE);
            } else {
                model.getDiagram().getNet().removeNode(n);
                this.updateNodeList();
            }
        }
        
    }//GEN-LAST:event_deleteButtonActionPerformed

    private void editButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_editButtonActionPerformed
        if (model.getDiagram() != null) {
            editDialog.setLocationRelativeTo(this);
            editDialog.setNode((Node)nodeList.getSelectedValue());
            editDialog.setVisible(true);
            if (editDialog.isModified()) {
                nodeList.repaint();
                // updated node
                model.fireEvent(new ApplicationModelEvent(ApplicationModelEventType.MODIFIED_NODE,model,(Node)nodeList.getSelectedValue()));
            }
        }
    }//GEN-LAST:event_editButtonActionPerformed

    private void newButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_newButtonActionPerformed
        // add new ...
        if (model.getDiagram() != null) {
            String result = JOptionPane.showInputDialog(this, ResourceLoader.getString("nl.name"));
            // do not create if empty or cancel selected
            if (result == null || result.equals(""))
                return;
            Node n = new Node(UUID.randomUUID().toString(), NodeType.STATION, result, result);
            NodeTrack track = new NodeTrack(UUID.randomUUID().toString(), "1");
            n.addTrack(track);
            model.getDiagram().getNet().addNode(n);
            this.updateNodeList();
            model.fireEvent(new ApplicationModelEvent(ApplicationModelEventType.NEW_NODE,model,n));
        }
    }//GEN-LAST:event_newButtonActionPerformed
    
    
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton deleteButton;
    private javax.swing.JButton editButton;
    private javax.swing.JButton newButton;
    private javax.swing.JList nodeList;
    private javax.swing.JScrollPane scrollPane;
    // End of variables declaration//GEN-END:variables
    
}