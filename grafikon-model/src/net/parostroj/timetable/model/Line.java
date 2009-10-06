package net.parostroj.timetable.model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import net.parostroj.timetable.model.events.LineEvent;
import net.parostroj.timetable.model.events.LineListener;

/**
 * Track between two route points.
 *
 * @author jub
 */
public class Line implements RouteSegment, AttributesHolder {

    /** ID. */
    private final String id;
    /** Length in mm. */
    private int length;
    /** List of node tracks. */
    private List<LineTrack> tracks;
    /** Top speed for the track. */
    private int topSpeed = UNLIMITED_SPEED;
    /** Unlimited spedd. */
    public static final int UNLIMITED_SPEED = -1;
    /** No speed constant. */
    public static final int NO_SPEED = UNLIMITED_SPEED;
    /** Attributes. */
    private Attributes attributes;
    /** Starting point. */
    private Node from;
    /** Ending point. */
    private Node to;
    private GTListenerSupport<LineListener, LineEvent> listenerSupport;

    /**
     * creates track with specified length.
     *
     * @param id id
     * @param length length of the track in milimeters
     * @param from starting point
     * @param to end point
     */
    public Line(String id, int length, Node from, Node to) {
        tracks = new ArrayList<LineTrack>();
        attributes = new Attributes();
        this.length = length;
        this.from = from;
        this.to = to;
        this.id = id;
        this.listenerSupport = new GTListenerSupport<LineListener, LineEvent>(new GTEventSender<LineListener, LineEvent>() {

            @Override
            public void fireEvent(LineListener listener, LineEvent event) {
                listener.lineChanged(event);
            }
        });
    }

    /**
     * creates track.
     * 
     * @param id id
     * @param length length of the track in milimeters
     * @param from starting point
     * @param to end point
     * @param topSpeed top speed
     */
    public Line(String id, int length, Node from, Node to, int topSpeed) {
        this(id, length, from, to);
        this.topSpeed = topSpeed;
    }

    public Attributes getAttributes() {
        return attributes;
    }

    public void setAttributes(Attributes attributes) {
        this.attributes = attributes;
    }

    /**
     * @return id of the line
     */
    @Override
    public String getId() {
        return id;
    }

    /**
     * @return track length
     */
    public int getLength() {
        return length;
    }

    /**
     * @param length length to be set
     */
    public void setLength(int length) {
        this.length = length;
        this.listenerSupport.fireEvent(new LineEvent(this, "length"));
    }

    public TimeInterval createTimeInterval(String intervalId, Train train, int start, TrainDiagram diagram, TimeIntervalDirection direction, int speed, int fromSpeed, int toSpeed) {
        int computedTime = this.computeRunningTime(train, speed, diagram, fromSpeed, toSpeed);
        int end = start + computedTime;

        LineTrack selectedTrack = null;
        TimeInterval interval = new TimeInterval(null, train, this, start, end, speed, direction, null);

        // check which track is free for adding
        for (LineTrack lineTrack : tracks) {
            TimeIntervalResult result = lineTrack.testTimeInterval(interval);
            if (result.getStatus() == TimeIntervalResult.Status.OK) {
                selectedTrack = lineTrack;
                break;
            }
        }

        if (selectedTrack == null) {
            // set first one
            selectedTrack = tracks.get(0);
        }

        return new TimeInterval(intervalId, train, this, start, end, speed, direction, selectedTrack);
    }

    /**
     * @return tracks
     */
    @Override
    public List<LineTrack> getTracks() {
        return Collections.unmodifiableList(tracks);
    }

    public void addTrack(LineTrack track) {
        track.line = this;
        tracks.add(track);
        this.listenerSupport.fireEvent(new LineEvent(this, LineEvent.Type.TRACK_ADDED));
    }

    public void addTrack(LineTrack track, int position) {
        track.line = this;
        tracks.add(position, track);
        this.listenerSupport.fireEvent(new LineEvent(this, LineEvent.Type.TRACK_ADDED));
    }

    public void removeTrack(LineTrack track) {
        track.line = null;
        tracks.remove(track);
        this.listenerSupport.fireEvent(new LineEvent(this, LineEvent.Type.TRACK_REMOVED));
    }

    public void removeAllTracks() {
        for (LineTrack track : tracks) {
            track.line = null;
        }
        tracks.clear();
        this.listenerSupport.fireEvent(new LineEvent(this, LineEvent.Type.TRACK_REMOVED));
    }

    /**
     * @return top speed
     */
    public int getTopSpeed() {
        return topSpeed;
    }

    /**
     * @param topSpeed top speed to be set
     */
    public void setTopSpeed(int topSpeed) {
        if (topSpeed == 0 || topSpeed < -1) {
            throw new IllegalArgumentException("Top speed should be positive number.");
        }
        this.topSpeed = topSpeed;
        this.listenerSupport.fireEvent(new LineEvent(this, "topSpeed"));
    }

    public int computeSpeed(Train train, int prefferedSpeed) {
        int speed;
        if (prefferedSpeed != NO_SPEED) {
            speed = Math.min(prefferedSpeed, train.getTopSpeed());
        } else {
            // apply max train speed
            speed = train.getTopSpeed();
        }

        // apply track speed limit
        if (this.topSpeed != UNLIMITED_SPEED) {
            speed = Math.min(speed, this.topSpeed);
        }
        return speed;
    }

    /**
     * computes running time for this track.
     *
     * @param train train
     * @param speed speed
     * @param diagram train diagram
     * @param fromSpeed from speed
     * @param toSpeed to speed
     * @return pair running time and speed
     */
    public int computeRunningTime(Train train, int speed, TrainDiagram diagram, int fromSpeed, int toSpeed) {
        Scale scale = (Scale) diagram.getAttribute("scale");
        double timeScale = (Double) diagram.getAttribute("time.scale");

        // compute time for the train
        int time = (int) Math.floor((((double) length) * scale.getRatio() * timeScale * 3.6) / (speed * 1000));

        // apply speeding and braking penalties
        PenaltyTable penaltyTable = diagram.getPenaltyTable();
        // braking penalty
        if (toSpeed < speed) {
            int penalty1 = adjustByRatio(penaltyTable.getRowForSpeedAndCategory(train.getType().getCategory(), speed).getDeceleration(), timeScale);
            int penalty2 = adjustByRatio(penaltyTable.getRowForSpeedAndCategory(train.getType().getCategory(), toSpeed).getDeceleration(), timeScale);
            time = time + penalty1 - penalty2;
        }
        // speeding penalty
        if (fromSpeed < speed) {
            int penalty1 = adjustByRatio(penaltyTable.getRowForSpeedAndCategory(train.getType().getCategory(), fromSpeed).getAcceleration(), timeScale);
            int penalty2 = adjustByRatio(penaltyTable.getRowForSpeedAndCategory(train.getType().getCategory(), speed).getAcceleration(), timeScale);
            time = time + penalty2 - penalty1;
        }

        // rounding to minutes (1 - 19 .. down, 20 - 59 .. up)
        time = ((time + 40) / 60) * 60;

        return time;
    }

    private static final double ADJUST_RATIO = 0.18d;
    private int adjustByRatio(int penalty, double timeScale) {
        return (int) Math.round(penalty * ADJUST_RATIO * timeScale);
    }

    @Override
    public void removeTimeInterval(TimeInterval interval) {
        interval.getTrack().removeTimeInterval(interval);
        this.listenerSupport.fireEvent(new LineEvent(this, LineEvent.Type.TIME_INTERVAL_REMOVED));
    }

    @Override
    public String toString() {
        return from.getAbbr() + "-" + to.getAbbr() + " <" + length + "," + topSpeed + ">";
    }

    @Override
    public void addTimeInterval(TimeInterval interval) {
        interval.getTrack().addTimeInterval(interval);
        this.listenerSupport.fireEvent(new LineEvent(this, LineEvent.Type.TIME_INTERVAL_ADDED));
    }

    @Override
    public Line asLine() {
        return this;
    }

    @Override
    public Node asNode() {
        return null;
    }

    public Node getFrom() {
        return from;
    }

    public Node getTo() {
        return to;
    }

    public Node getFrom(TimeIntervalDirection direction) {
        return (direction == TimeIntervalDirection.FORWARD) ? from : to;
    }

    public Node getTo(TimeIntervalDirection direction) {
        return (direction == TimeIntervalDirection.FORWARD) ? to : from;
    }

    @Override
    public boolean isEmpty() {
        for (LineTrack track : tracks) {
            if (!track.isEmpty()) {
                return false;
            }
        }
        return true;
    }

    /**
     * returns line track with specified number.
     * 
     * @param number number
     * @return line track
     */
    public LineTrack getLineTrackByNumber(String number) {
        for (LineTrack track : tracks) {
            if (track.getNumber().equals(number)) {
                return track;
            }
        }
        return null;
    }

    @Override
    public Object getAttribute(String key) {
        return attributes.get(key);
    }

    @Override
    public void setAttribute(String key, Object value) {
        attributes.put(key, value);
        this.listenerSupport.fireEvent(new LineEvent(this, key));
    }

    @Override
    public Object removeAttribute(String key) {
        Object returnValue = attributes.remove(key);
        this.listenerSupport.fireEvent(new LineEvent(this, key));
        return returnValue;
    }

    @Override
    public LineTrack findTrackById(String id) {
        for (LineTrack track : getTracks()) {
            if (track.getId().equals(id)) {
                return track;
            }
        }
        return null;
    }
    
    public void addListener(LineListener listener) {
        this.listenerSupport.addListener(listener);
    }
    
    public void removeListener(LineListener listener) {
        this.listenerSupport.removeListener(listener);
    }
    
    void fireTrackAttributeChanged(String attributeName, LineTrack track) {
        this.listenerSupport.fireEvent(new LineEvent(this, attributeName, track));
    }
}
