package net.parostroj.timetable.model;

/**
 * Track in the station.
 *
 * @author jub
 */
public class NodeTrack extends Track {

    /** Platform. */
    private boolean platform;

    /**
     * Constructor.
     * 
     * @param id id
     */
    public NodeTrack(String id) {
        super(id);
    }

    /**
     * creates instance with specified track number.
     *
     * @param id id
     * @param number track number
     */
    public NodeTrack(String id, String number) {
        super(id, number);
    }

    @Override
    public String toString() {
        return super.toString();
    }

    /**
     * @return the platform
     */
    public boolean isPlatform() {
        return platform;
    }

    /**
     * @param platform the platform to set
     */
    public void setPlatform(boolean platform) {
        this.platform = platform;
    }
}