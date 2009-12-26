package net.parostroj.timetable.gui.dialogs;

import java.awt.Frame;
import java.awt.Rectangle;
import java.util.LinkedList;
import java.util.List;
import java.util.StringTokenizer;
import javax.swing.JPanel;
import net.parostroj.timetable.gui.AppPreferences;
import net.parostroj.timetable.gui.StorableGuiData;
import net.parostroj.timetable.gui.utils.GuiUtils;
import net.parostroj.timetable.utils.ResourceLoader;

/**
 * Floating dialog window.
 *
 * @author jub
 */
public class FloatingDialog extends javax.swing.JDialog implements StorableGuiData {

    private String storageKeyPrefix;

    /** Creates new form FloatingDialog */
    public FloatingDialog(java.awt.Frame parent, boolean modal) {
        super(parent, modal);
        initComponents();
    }

    public FloatingDialog(Frame parent, JPanel panel, String titleKey, String storageKeyPrefix) {
        this(parent, false);
        if (titleKey != null)
            this.setTitle(ResourceLoader.getString(titleKey));
        getContentPane().add(panel, java.awt.BorderLayout.CENTER);
        
        this.storageKeyPrefix = storageKeyPrefix;

        // update layout
        pack();
    }

    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        pack();
    }// </editor-fold>//GEN-END:initComponents

    protected String createStorageKey(String keySuffix) {
        return new StringBuilder(storageKeyPrefix).append('.').append(keySuffix).toString();
    }

    @Override
    public void saveToPreferences(AppPreferences prefs) {
        prefs.removeWithPrefix(storageKeyPrefix);
        prefs.setString(this.createStorageKey("position"), GuiUtils.getPosition(this));
        prefs.setBoolean(this.createStorageKey("visible"), this.isVisible());
    }

    @Override
    public void loadFromPreferences(AppPreferences prefs) {
        // set position
        String positionStr = prefs.getString(this.createStorageKey("position"));
        GuiUtils.setPosition(positionStr, this);
        // set visibility
        if (Boolean.TRUE.equals(prefs.getBoolean(this.createStorageKey("visible"))))
                this.setVisible(true);
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    // End of variables declaration//GEN-END:variables

}