/*
 * TrainsPane.java
 *
 * Created on 3. září 2007, 10:22
 */
package net.parostroj.timetable.gui.panes;

import java.awt.Color;
import java.util.Collections;
import java.util.Set;
import net.parostroj.timetable.gui.AppPreferences;
import net.parostroj.timetable.gui.ApplicationModel;
import net.parostroj.timetable.gui.ApplicationModelEvent;
import net.parostroj.timetable.gui.ApplicationModelEventType;
import net.parostroj.timetable.gui.StorableGuiData;
import net.parostroj.timetable.gui.views.HighlightedTrains;
import net.parostroj.timetable.gui.views.TrainSelector;
import net.parostroj.timetable.model.TimeInterval;
import net.parostroj.timetable.model.Train;

/**
 * Trains pane.
 * 
 * @author jub
 */
public class TrainsPane extends javax.swing.JPanel implements StorableGuiData {
    
    private ApplicationModel model;
    
    /** Creates new form TrainsPane */
    public TrainsPane() {
        initComponents();
        
        scrollPane.getViewport().addChangeListener(graphicalTimetableView);
        scrollPane.getHorizontalScrollBar().setBlockIncrement(1000);
        scrollPane.getHorizontalScrollBar().setUnitIncrement(100);
    }
    
    /**
     * sets model.
     * 
     * @param model application model
     */
    public void setModel(final ApplicationModel model) {
        this.model = model;
        trainListView.setModel(model);
        trainView.setModel(model);
        graphicalTimetableView.setModel(model);
        
        HighlightedTrains ht = new HighlightedTrains() {
            
            Set<Train> set = Collections.emptySet();

            @Override
            public boolean isHighlighedInterval(TimeInterval interval) {
                return set.contains(interval.getTrain());
            }

            @Override
            public void modelChanged(ApplicationModelEvent event) {
                if (event.getType() == ApplicationModelEventType.SELECTED_TRAIN_CHANGED) {
                    set = Collections.singleton(model.getSelectedTrain());
                    graphicalTimetableView.repaint();
                }
            }

            @Override
            public Color getColor() {
                return Color.GREEN;
            }
        };
        model.addListener(ht);
        graphicalTimetableView.setHTrains(ht);

        graphicalTimetableView.setTrainSelector(new TrainSelector() {
            
            private TimeInterval selectedTimeInterval;
            
            @Override
            public void selectTrainInterval(TimeInterval interval) {
                // set selected train
                Train selected = null;
                if (interval != null)
                    selected = interval.getTrain();
                model.setSelectedTrain(selected);
                selectedTimeInterval = interval;
            }

            @Override
            public TimeInterval getSelectedTrainInterval() {
                return selectedTimeInterval;
            }
        });
    }
    
    @Override
    public void loadFromPreferences(AppPreferences prefs) {
        Integer dividerLoc = prefs.getInt("trains.divider");
        if (dividerLoc != null)
            splitPane.setDividerLocation(dividerLoc.intValue());
        trainView.loadFromPreferences(prefs);
    }
    
    @Override
    public void saveToPreferences(AppPreferences prefs) {
        prefs.setInt("trains.divider", splitPane.getDividerLocation());
        trainView.saveToPreferences(prefs);
    }
    
    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        splitPane = new javax.swing.JSplitPane();
        scrollPane = new javax.swing.JScrollPane();
        graphicalTimetableView = new net.parostroj.timetable.gui.views.GraphicalTimetableView();
        panel = new javax.swing.JPanel();
        trainListView = new net.parostroj.timetable.gui.views.TrainListView();
        trainView = new net.parostroj.timetable.gui.views.TrainView();

        splitPane.setDividerLocation(350);
        splitPane.setOrientation(javax.swing.JSplitPane.VERTICAL_SPLIT);

        javax.swing.GroupLayout graphicalTimetableViewLayout = new javax.swing.GroupLayout(graphicalTimetableView);
        graphicalTimetableView.setLayout(graphicalTimetableViewLayout);
        graphicalTimetableViewLayout.setHorizontalGroup(
            graphicalTimetableViewLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 3000, Short.MAX_VALUE)
        );
        graphicalTimetableViewLayout.setVerticalGroup(
            graphicalTimetableViewLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 172, Short.MAX_VALUE)
        );

        scrollPane.setViewportView(graphicalTimetableView);

        splitPane.setBottomComponent(scrollPane);

        javax.swing.GroupLayout panelLayout = new javax.swing.GroupLayout(panel);
        panel.setLayout(panelLayout);
        panelLayout.setHorizontalGroup(
            panelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(panelLayout.createSequentialGroup()
                .addComponent(trainListView, javax.swing.GroupLayout.PREFERRED_SIZE, 224, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(trainView, javax.swing.GroupLayout.DEFAULT_SIZE, 433, Short.MAX_VALUE))
        );
        panelLayout.setVerticalGroup(
            panelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(trainView, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, 349, Short.MAX_VALUE)
            .addComponent(trainListView, javax.swing.GroupLayout.DEFAULT_SIZE, 349, Short.MAX_VALUE)
        );

        splitPane.setLeftComponent(panel);

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(splitPane, javax.swing.GroupLayout.DEFAULT_SIZE, 665, Short.MAX_VALUE)
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(splitPane, javax.swing.GroupLayout.DEFAULT_SIZE, 548, Short.MAX_VALUE)
        );
    }// </editor-fold>//GEN-END:initComponents
    
    
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private net.parostroj.timetable.gui.views.GraphicalTimetableView graphicalTimetableView;
    private javax.swing.JPanel panel;
    private javax.swing.JScrollPane scrollPane;
    private javax.swing.JSplitPane splitPane;
    private net.parostroj.timetable.gui.views.TrainListView trainListView;
    private net.parostroj.timetable.gui.views.TrainView trainView;
    // End of variables declaration//GEN-END:variables
    
}
