/*
 * StatusBar.java
 *
 * Created on 4. září 2007, 16:54
 */

package net.parostroj.timetable.gui;

import net.parostroj.timetable.utils.ResourceLoader;

/**
 * Status bar for the application.
 *
 * @author jub
 */
public class StatusBar extends javax.swing.JPanel implements ApplicationModelListener {
    
    /** Creates new form StatusBar */
    public StatusBar() {
        initComponents();
    }

    public void modelChanged(ApplicationModelEvent event) {
        switch (event.getType()) {
            case SET_DIAGRAM_CHANGED:
                if (event.getModel().getDiagram() != null)
                    left.setText(ResourceLoader.getString("status.bar.trains") + " " + event.getModel().getDiagram().getTrains().size());
                else
                    left.setText("");
                break;
            default:
                // nothings
        }
    }
    
    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc=" Generated Code ">//GEN-BEGIN:initComponents
    private void initComponents() {

        left = new javax.swing.JTextField();
        center = new javax.swing.JTextField();
        right = new javax.swing.JTextField();

        left.setEditable(false);

        center.setEditable(false);

        right.setEditable(false);

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addComponent(left, javax.swing.GroupLayout.PREFERRED_SIZE, 143, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(0, 0, 0)
                .addComponent(center, javax.swing.GroupLayout.DEFAULT_SIZE, 391, Short.MAX_VALUE)
                .addGap(0, 0, 0)
                .addComponent(right, javax.swing.GroupLayout.PREFERRED_SIZE, 147, javax.swing.GroupLayout.PREFERRED_SIZE))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(center)
            .addComponent(left)
            .addComponent(right)
        );
    }// </editor-fold>//GEN-END:initComponents
    
    
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JTextField center;
    private javax.swing.JTextField left;
    private javax.swing.JTextField right;
    // End of variables declaration//GEN-END:variables
    
}
