package net.parostroj.timetable.model.ls.impl3;

import java.util.LinkedList;
import java.util.List;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.bind.annotation.XmlElements;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;
import net.parostroj.timetable.actions.TrainIntervalsBuilder;
import net.parostroj.timetable.model.Line;
import net.parostroj.timetable.model.LineTrack;
import net.parostroj.timetable.model.Node;
import net.parostroj.timetable.model.NodeTrack;
import net.parostroj.timetable.model.TimeInterval;
import net.parostroj.timetable.model.Train;
import net.parostroj.timetable.model.TrainDiagram;

/**
 * Class for storing trains.
 * 
 * @author jub
 */
@XmlRootElement(name = "train")
@XmlType(propOrder = {"id", "number", "desc", "type", "topSpeed", "start", "attributes", "route"})
public class LSTrain {

    private String id;
    private String number;
    private String desc;
    private String type;
    private int topSpeed;
    private int start;
    private LSAttributes attributes;
    private List<Object> route;

    public LSTrain() {
    }

    public LSTrain(Train train) {
        this.id = train.getId();
        this.number = train.getNumber();
        this.desc = train.getDescription();
        this.type = train.getType().getId();
        this.topSpeed = train.getTopSpeed();
        this.start = train.getStartTime();
        this.attributes = new LSAttributes(train.getAttributes());

        // create route parts ...
        route = new LinkedList<Object>();
        for (TimeInterval interval : train.getTimeIntervalList()) {
            Object part = null;
            if (interval.getOwner() instanceof Line) {
                part = new LSTrainRoutePartLine(interval);
            } else {
                part = new LSTrainRoutePartNode(interval);
            }
            route.add(part);
        }
    }

    public LSAttributes getAttributes() {
        return attributes;
    }

    public void setAttributes(LSAttributes attributes) {
        this.attributes = attributes;
    }

    public String getDesc() {
        return desc;
    }

    public void setDesc(String desc) {
        this.desc = desc;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getNumber() {
        return number;
    }

    public void setNumber(String number) {
        this.number = number;
    }

    public int getTopSpeed() {
        return topSpeed;
    }

    public void setTopSpeed(int topSpeed) {
        this.topSpeed = topSpeed;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    @XmlElementWrapper
    @XmlElements({
        @XmlElement(name = "node", type = LSTrainRoutePartNode.class),
        @XmlElement(name = "line", type = LSTrainRoutePartLine.class)
    })
    public List<Object> getRoute() {
        return route;
    }

    public void setRoute(List<Object> route) {
        this.route = route;
    }

    public int getStart() {
        return start;
    }

    public void setStart(int start) {
        this.start = start;
    }
    
    public Train createTrain(TrainDiagram diagram) {
        Train train = new Train(id, number, diagram.getTrainTypeById(type));
        train.setAttributes(attributes.createAttributes());
        train.setDescription(desc);
        train.setTopSpeed(topSpeed);
        // build time interval list
        TrainIntervalsBuilder builder = new TrainIntervalsBuilder(diagram, train, start);
        for (Object routePart : getRoute()) {
            if (routePart instanceof LSTrainRoutePartNode) {
                LSTrainRoutePartNode nodePart = (LSTrainRoutePartNode)routePart;
                Node node = diagram.getNet().getNodeById(nodePart.getNodeId());
                NodeTrack nodeTrack = node.findTrackById(nodePart.getTrackId());
                builder.addNode(node, nodeTrack, nodePart.getStop());
            } else {
                LSTrainRoutePartLine linePart = (LSTrainRoutePartLine)routePart;
                Line line = diagram.getNet().getLineById(linePart.getLineId());
                LineTrack lineTrack = line.findTrackById(linePart.getTrackId());
                builder.addLine(line, lineTrack, linePart.getSpeed());
            }
        }
        builder.finish();
        return train;
    }
}