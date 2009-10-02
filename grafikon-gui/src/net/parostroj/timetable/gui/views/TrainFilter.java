package net.parostroj.timetable.gui.views;

import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import net.parostroj.timetable.model.Train;
import net.parostroj.timetable.model.TrainType;
import net.parostroj.timetable.model.TrainTypeCategory;

/**
 * Class for filtering train according to some criteria.
 *
 * @author jub
 */
public abstract class TrainFilter {

    public static enum PredefinedType {
        FREIGHT, PASSENGER;
    }

    private static final TrainTypeCategory C_FREIGHT = TrainTypeCategory.fromString("freight");
    private static final TrainTypeCategory C_PASSENGER = TrainTypeCategory.fromString("passenger");

    public static TrainFilter getTrainFilter(PredefinedType type) {
        switch (type) {
            case FREIGHT:
                return new TrainFilter() {
                    @Override
                    public boolean filter(Train train) {
                        return train.getType().getCategory().equals(C_FREIGHT);
                    }
                };
            case PASSENGER:
                return new TrainFilter() {
                    @Override
                    public boolean filter(Train train) {
                        return train.getType().getCategory().equals(C_PASSENGER);
                    }
                };
            default:
                throw new IllegalArgumentException("Unexpected type: " + type);
        }
    }

    public static TrainFilter getTrainFilter(final Set<TrainType> types) {
        return new TrainFilter() {
            @Override
            public boolean filter(Train train) {
                return types.contains(train.getType());
            }
        };
    }

    public List<Train> filter(List<Train> trains) {
        List<Train> result = new LinkedList<Train>();
        for (Train train : trains) {
            if (this.filter(train)) {
                result.add(train);
            }
        }
        return result;
    }

    public abstract boolean filter(Train train);
}
