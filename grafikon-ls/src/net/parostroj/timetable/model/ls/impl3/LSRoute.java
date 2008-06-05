package net.parostroj.timetable.model.ls.impl3;

import java.util.LinkedList;
import java.util.List;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;
import net.parostroj.timetable.model.Net;
import net.parostroj.timetable.model.Route;
import net.parostroj.timetable.model.RouteSegment;

/**
 * Storage for route.
 * 
 * @author jub
 */
@XmlRootElement(name = "route")
@XmlType(propOrder = {"id", "name", "netPart", "segments"})
public class LSRoute {

    private String id;
    private String name;
    private boolean netPart;
    private List<String> segments;

    public LSRoute(Route route) {
        this.id = route.getId();
        this.name = route.getName();
        this.netPart = route.isNetPart();
        this.segments = new LinkedList<String>();
        for (RouteSegment segment : route.getSegments()) {
            this.segments.add(segment.getId());
        }
    }

    public LSRoute() {
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    @XmlElement(name = "net_part")
    public boolean isNetPart() {
        return netPart;
    }

    public void setNetPart(boolean netPart) {
        this.netPart = netPart;
    }

    @XmlElementWrapper
    @XmlElement(name = "segment")
    public List<String> getSegments() {
        return segments;
    }

    public void setSegments(List<String> segments) {
        this.segments = segments;
    }
    
    public Route createRoute(Net net) {
        Route route = new Route(id);
        route.setName(name);
        route.setNetPart(netPart);
        // create segments
        boolean node = true;
        for (String segment : getSegments()) {
            if (node)
                route.getSegments().add(net.getNodeById(segment));
            else
                route.getSegments().add(net.getLineById(segment));
            node = !node;
        }
        return route;
    }
}
