/*
 * EngineCyclesPane.java
 *
 * Created on 12. září 2007, 13:33
 */
package net.parostroj.timetable.gui.panes;

import java.awt.Color;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import net.parostroj.timetable.gui.ApplicationModel;
import net.parostroj.timetable.gui.ApplicationModelEvent;
import net.parostroj.timetable.gui.views.GraphicalTimetableView.TrainColors;
import net.parostroj.timetable.gui.views.*;
import net.parostroj.timetable.model.Train;
import net.parostroj.timetable.model.TrainsCycle;
import net.parostroj.timetable.model.TrainsCycleItem;

/**
 * Pane for manipulating engine cycles.
 *
 * @author jub
 */
public class TrainsCyclesPane extends javax.swing.JPanel {

    private TCDelegate delegate;

    private class HighligterAndSelector implements HighlightedTrains, TrainSelector, TrainColorChooser {

        private TrainColorChooser chooserDelegate;
        private TrainSelector selectorDelegate;

        public HighligterAndSelector(TrainColorChooser chooser, TrainSelector selector) {
            this.chooserDelegate = chooser;
            this.selectorDelegate = selector;
        }
        private Set<Train> set = Collections.emptySet();
        private TrainsCycle last;

        @Override
        public Set<Train> getHighlighedTrains() {
            return Collections.singleton(selectorDelegate.getSelectedTrain());
        }

        @Override
        public void modelChanged(ApplicationModelEvent event) {
            if (delegate.transformEventType(event.getType()) == TCDelegate.Action.SELECTED_CHANGED) {
                last = delegate.getSelectedCycle(event.getModel());
                set = this.createSet(last);
                graphicalTimetableView.repaint();
            } else if (delegate.transformEventType(event.getType()) == TCDelegate.Action.MODIFIED_CYCLE) {
                if (event.getObject() == last) {
                    set = this.createSet(last);
                    graphicalTimetableView.repaint();
                }
            }
        }

        private Set<Train> createSet(TrainsCycle cycle) {
            if (cycle == null) {
                return Collections.emptySet();
            }
            Set<Train> result = new HashSet<Train>();
            for (TrainsCycleItem item : cycle) {
                result.add(item.getTrain());
            }
            return result;
        }

        @Override
        public Color getColor() {
            return Color.GREEN;
        }

        @Override
        public void selectTrain(Train train) {
            selectorDelegate.selectTrain(train);
            graphicalTimetableView.repaint();
        }

        @Override
        public Train getSelectedTrain() {
            return selectorDelegate.getSelectedTrain();
        }

        @Override
        public Color getColor(Train train) {
            if (set.contains(train))
                return Color.RED;
            return chooserDelegate.getColor(train);
        }
    }

    /** Creates new form EngineCyclesPane */
    public TrainsCyclesPane() {
        initComponents();
        scrollPane.getHorizontalScrollBar().setBlockIncrement(1000);
        scrollPane.getHorizontalScrollBar().setUnitIncrement(100);
    }

    public void setModel(ApplicationModel model, final TCDelegate delegate, TrainColorChooser chooser) {
        this.delegate = delegate;
        HighligterAndSelector hts = new HighligterAndSelector(chooser, eCTrainListView);
        eCListView.setModel(model, delegate);
        eCDetailsView.setModel(model, delegate);
        eCTrainListView.setModel(model, delegate);
        graphicalTimetableView.setModel(model);
        graphicalTimetableView.setTrainColors(TrainColors.BY_COLOR_CHOOSER, hts);
        model.addListener(hts);
        graphicalTimetableView.setHTrains(hts);
        graphicalTimetableView.setTrainSelector(hts);
    }

    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        eCListView = new net.parostroj.timetable.gui.views.TCListView();
        eCDetailsView = new net.parostroj.timetable.gui.views.TCDetailsView2();
        eCTrainListView = new net.parostroj.timetable.gui.views.TCTrainListView();
        scrollPane = new javax.swing.JScrollPane();
        graphicalTimetableView = new net.parostroj.timetable.gui.views.GraphicalTimetableView();

        javax.swing.GroupLayout graphicalTimetableViewLayout = new javax.swing.GroupLayout(graphicalTimetableView);
        graphicalTimetableView.setLayout(graphicalTimetableViewLayout);
        graphicalTimetableViewLayout.setHorizontalGroup(
            graphicalTimetableViewLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 4000, Short.MAX_VALUE)
        );
        graphicalTimetableViewLayout.setVerticalGroup(
            graphicalTimetableViewLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 137, Short.MAX_VALUE)
        );

        scrollPane.setViewportView(graphicalTimetableView);

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addComponent(eCDetailsView, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(eCListView, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(eCTrainListView, javax.swing.GroupLayout.DEFAULT_SIZE, 414, Short.MAX_VALUE)
                .addContainerGap())
            .addComponent(scrollPane, javax.swing.GroupLayout.DEFAULT_SIZE, 600, Short.MAX_VALUE)
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(eCDetailsView, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(eCListView, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addComponent(eCTrainListView, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(scrollPane, javax.swing.GroupLayout.DEFAULT_SIZE, 157, Short.MAX_VALUE))
        );
    }// </editor-fold>//GEN-END:initComponents
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private net.parostroj.timetable.gui.views.TCDetailsView2 eCDetailsView;
    private net.parostroj.timetable.gui.views.TCListView eCListView;
    private net.parostroj.timetable.gui.views.TCTrainListView eCTrainListView;
    private net.parostroj.timetable.gui.views.GraphicalTimetableView graphicalTimetableView;
    private javax.swing.JScrollPane scrollPane;
    // End of variables declaration//GEN-END:variables
}
