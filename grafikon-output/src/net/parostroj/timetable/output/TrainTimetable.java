/*
 * TrainTimetable.java
 * 
 * Created on 8.9.2007, 16:30:05
 */
package net.parostroj.timetable.output;

import java.io.IOException;
import java.io.Writer;
import java.util.*;
import net.parostroj.timetable.model.*;
import net.parostroj.timetable.utils.Pair;
import net.parostroj.timetable.utils.TimeConverter;

/**
 * Time table for train.
 * 
 * @author jub
 */
public class TrainTimetable {
    
    private Train train;
    
    private int length;
    
    private int allRunningTime;
    
    private int allStopTime;
    
    private boolean d3;
    
    private TrainTimetablesListTemplates templates;
    
    private List<Pair<String, String>> comments;

    public TrainTimetable(Train train, TrainTimetablesListTemplates templates) {
        this.train = train;
        this.templates = templates;
        d3 = train.oneLineHasAttribute("line.type", "D3");
        
        this.computeLength();
    }
    
    private void computeLength() {
        length = templates.getTimetableFooterHeight() + templates.getTimetableHeaderHeight();
        length = length + templates.getTimetableLineHeight() * ((train.getTimeIntervalList().size() / 2) + 1);
        
        // TODO include number of engine cycles to computation ...
        
        // compute comments
        boolean lineEnd = false;
        boolean occupied = false;
        boolean shunt = false;
        int commentsCount = 0;
        for (TimeInterval interval : train.getTimeIntervalList()) {
            if (interval.getOwner() instanceof Line)
                continue;
            if ((interval.getType() == TimeIntervalType.NODE_STOP || interval.getType() == TimeIntervalType.NODE_END)
                    && Boolean.TRUE.equals(interval.getTrack().getAttribute("line.end")))
                lineEnd = true;
            if (Boolean.TRUE.equals(interval.getAttribute("occupied")))
                occupied = true;
            if (Boolean.TRUE.equals(interval.getAttribute("shunt")))
                shunt = true;
            if (Boolean.TRUE.equals(interval.getAttribute("comment.shown")))
                commentsCount++;
        }
        
        if (lineEnd)
            length += templates.getTimetableCommentHeight();
        if (occupied)
            length += templates.getTimetableCommentHeight();
        if (shunt)
            length += templates.getTimetableCommentHeight();
        length += commentsCount * templates.getTimetableCommentHeight();
    }

    public int getLength() {
        return length;
    }
    
    public void writeTo(Writer writer, Page page) throws IOException {
        comments = new LinkedList<Pair<String, String>>();
        
        Formatter f = new Formatter(writer);
        String wString = (train.getAttribute("weight.info") == null || train.getAttribute("weight.info").equals("")) ? "" : (". " + TrainTimetablesListTemplates.getString("norm.load") + " " + train.getAttribute("weight.info") + " " + TrainTimetablesListTemplates.getString("tons"));
        String trainLine = train.getCompleteName();
        String engineCycleDesc = this.createCommentWithEngineCycles();
        String rString = this.createRouteInfo();
        String headerTemplate = d3 ? templates.getTimetableHeaderD3() : templates.getTimetableHeader();
        f.format(headerTemplate, trainLine, engineCycleDesc, wString, rString);
        this.writeLines(f);
        String allTimeStr = null;
        int allTime = allRunningTime + allStopTime;
        if (TimeConverter.getHours(allTime) == 0) {
            allTimeStr = TimeConverter.convertAllMinutesToText(allTime) + " " + TrainTimetablesListTemplates.getString("minutes");
        } else {
            allTimeStr = TimeConverter.convertHoursAndMinutesToText(allTime,
                    TrainTimetablesListTemplates.getString("hours"),
                    TrainTimetablesListTemplates.getString("minutes"));
        }
        
        String footerTemplate = d3 ? templates.getTimetableFooterD3() : templates.getTimetableFooter();
        String summaryTemplate = d3 ? templates.getTimetableSummaryD3() : templates.getTimetableSummary();
        String commentTemplate = d3 ? templates.getTimetableCommentD3() : templates.getTimetableComment();
        f.format(summaryTemplate,TimeConverter.convertAllMinutesToText(allRunningTime), 
                TimeConverter.convertAllMinutesToText(allStopTime), allTimeStr,
                TrainTimetablesListTemplates.getString("total.train.time"));
        
        // process comments
        for (Pair<String, String> comment : comments) {
            f.format(commentTemplate, comment.first, "= " + comment.second);
        }
        
        writer.write(footerTemplate);
    }
    
    private String createRouteInfo() {
        if (train.getAttribute("route.info") == null || train.getAttribute("route.info").equals(""))
            return "";
        String result = (String)train.getAttribute("route.info");
        result = result.replace("$1", "<b>" + train.getStartNode().getName() + "</b>");
        result = result.replace("$2", "<b>" + train.getEndNode().getName() + "</b>");
        result = result.replace("$" , "<b>" + train.getStartNode().getName() + "</b>&mdash;<b>" + train.getEndNode().getName() + "</b>");
        result = result.replace("-","&mdash;");
        result = "<br><span style=\"font-size: 3mm;\">" + result + "</span>";
        return result;
    }
    
    private void writeLines(Formatter f) {
        String lineTemplate = d3 ? templates.getTimetableLineD3() : templates.getTimetableLine();
        Iterator<TimeInterval> i = train.getTimeIntervalList().iterator();
        String lastComment = "";
        allRunningTime = 0;
        allStopTime = 0;
        int lastSpeed = 0;
        String speedStr = null;
        int lastRunningTime = 0;
        String stopTime = null;
        int lastHourFrom = -1;
        int lastHoutTo = -1;
        NodeTrack lastStraight = null;
        while(i.hasNext()) {
            TimeInterval nodeInterval = i.next();
            Node node = (Node)nodeInterval.getOwner();
            if (node.getType() == NodeType.SIGNAL) {
                TimeInterval ii = i.next();
                lastRunningTime += ii.getLength();
                allRunningTime += ii.getLength();
                continue;
            }
            if (nodeInterval.getType() == TimeIntervalType.NODE_STOP) {
                stopTime = TimeConverter.convertAllMinutesToText(nodeInterval.getLength());
                allStopTime += nodeInterval.getLength();
            } else {
                stopTime = "&nbsp;";
            }
            boolean controlStation  = Boolean.TRUE.equals(node.getAttribute("control.station")) && d3;
            TimeInterval lineInterval = i.hasNext() ? i.next() : null;
            speedStr = "&nbsp;";
            if (lineInterval != null) {
                if (lineInterval.getSpeed() != lastSpeed) {
                    speedStr = Integer.toString(lineInterval.getSpeed());
                }
                lastSpeed = lineInterval.getSpeed();
            }
            
            String fromTime = "&nbsp;";
            String toTime = "&nbsp;";
            if (nodeInterval.getType() != TimeIntervalType.NODE_START && nodeInterval.getType() != TimeIntervalType.NODE_THROUGH) {
                int hour = TimeConverter.getHours(nodeInterval.getStart());
                if (lastHourFrom != hour || nodeInterval.getType() == TimeIntervalType.NODE_END) {
                    fromTime = TimeConverter.convertFromIntToText(nodeInterval.getStart(), " ");
                    lastHourFrom = hour;
                } else {
                    fromTime = TimeConverter.convertMinutesToText(nodeInterval.getStart());
                }
            }
            if (nodeInterval.getType() != TimeIntervalType.NODE_END) {
                int hour = TimeConverter.getHours(nodeInterval.getEnd());
                if (lastHoutTo != hour) {
                    toTime = TimeConverter.convertFromIntToText(nodeInterval.getEnd(), " ");
                    lastHoutTo = hour;
                } else {
                    toTime = TimeConverter.convertMinutesToText(nodeInterval.getEnd());
                }   
            }
            
            String stopName = TransformUtil.transformStation(node);
            if (nodeInterval.getType() == TimeIntervalType.NODE_START || nodeInterval.getType() == TimeIntervalType.NODE_END || 
                    node.getType() == NodeType.STATION_BRANCH) {
                stopName = "<b>" + stopName + "</b>";
            }
            
            String column8 = "&nbsp;";
            boolean trapezoid = false;
            if (d3 && Boolean.TRUE.equals(node.getAttribute("trapezoid.sign"))) {
                String c8 = this.getTrapezoidTrains(nodeInterval, node);
                if (c8 != null) {
                    column8 = c8;
                    trapezoid = true;
                }
            }
            
            String note = "";
            if ("new.signals".equals(node.getAttribute("interlocking.plant"))) {
                note = "<img src=\"signal.gif\" style=\"height: 3.5mm\">";
            } else if (lastStraight != null && lastStraight != nodeInterval.getTrack()) {
                note = "&#8594;";
            }
            
            if (trapezoid) {
                note += "<img src=\"trapezoid_sign.gif\" style=\"height: 3.5mm; vertical-align: middle\">";
            }
            
            // add control station sign
            if (controlStation) {
                stopName += " <img src=\"control_station.gif\" style=\"height: 2.5mm; vertical-align: baseline\">";
            }
            
            // check comment line end
            if (nodeInterval.getType() == TimeIntervalType.NODE_STOP || nodeInterval.getType() == TimeIntervalType.NODE_END) {
                if (Boolean.TRUE.equals(nodeInterval.getTrack().getAttribute("line.end"))) {
                    note += "&Delta;";
                    this.addComment(new Pair<String, String>("&Delta;",TrainTimetablesListTemplates.getString("entry.line.end")));
                }
            }
            
            // check occupied track
            if (Boolean.TRUE.equals(nodeInterval.getAttribute("occupied"))) {
                note += "&Omicron;";
                this.addComment(new Pair<String, String>("&Omicron;", TrainTimetablesListTemplates.getString("entry.occupied")));
            }

            // check shunt
            if (Boolean.TRUE.equals(nodeInterval.getAttribute("shunt"))) {
                note += "&loz;";
                this.addComment(new Pair<String, String>("&loz;", TrainTimetablesListTemplates.getString("entry.shunt")));
            }
            
            // check shunt
            if (Boolean.TRUE.equals(nodeInterval.getAttribute("comment.shown"))) {
                lastComment = lastComment + "*";
                note += lastComment;
                this.addComment(new Pair<String, String>(lastComment, nodeInterval.getComment()));
            }

            if (lineInterval != null)
                lastStraight = lineInterval.getToStraightTrack();
            
            String column2a = "&nbsp;";
            if (d3 && !controlStation && nodeInterval.getOwner().asNode().getTracks().size() > 1)
                column2a = nodeInterval.getTrack().getNumber();
            
            if (note.equals(""))
                note = "&nbsp;";

            f.format(lineTemplate,stopName,note,this.convertLastRunningTime(lastRunningTime),fromTime,stopTime,toTime,speedStr,column8,column2a);
            if (lineInterval != null) {
                lastRunningTime = lineInterval.getLength();
                allRunningTime += lineInterval.getLength();
            }
        }
    }
    
    private String convertLastRunningTime(int time) {
        if (time != 0)
            return TimeConverter.convertAllMinutesToText(time);
        else
            return "&nbsp;";
    }
    
    private void addComment(Pair<String, String> comment) {
        // check if comment already exists
        for (Pair<String, String> item : comments) {
            if (item.first.equals(comment.first))
                return;
        }
        comments.add(comment);
    }
    
    private String getTrapezoidTrains(TimeInterval interval, Node node) {
        Set<TimeInterval> over = node.getOverlappingTimeIntervals(interval);
        // filter out ...
        for (Iterator<TimeInterval> i = over.iterator(); i.hasNext();) {
            TimeInterval checked = i.next();
            if (interval.getStart() < checked.getStart())
                i.remove();
        }
        if (over.size() == 0)
            return null;
        else {
            StringBuilder result = new StringBuilder();
            for (TimeInterval item : over) {
                if (result.length() != 0)
                    result.append(", ");
                result.append(item.getTrain().getName());
            }
            return result.toString();
        }
    }
    
    private String createCommentWithEngineCycles() {
        if (train.getCycles(TrainsCycleType.ENGINE_CYCLE).isEmpty()) {
            return "-";
        } else if (train.getCycles(TrainsCycleType.ENGINE_CYCLE).size() == 1) {
            TrainsCycleItem item = train.getCycles(TrainsCycleType.ENGINE_CYCLE).iterator().next();
            return TrainTimetablesListTemplates.getString(((Boolean)train.getAttribute("diesel")) ? "diesel.unit" : "engine") + " " + item.getCycle().getDescription();
        } else {
            StringBuilder desc = new StringBuilder();
            for (TrainsCycleItem item : train.getCycles(TrainsCycleType.ENGINE_CYCLE)) {
                if (desc.length() != 0)
                    desc.append("<br>");
                desc.append(TrainTimetablesListTemplates.getString(((Boolean)train.getAttribute("diesel")) ? "diesel.unit" : "engine") + " " + item.getCycle().getDescription());
                desc.append(" ");
                desc.append(item.getFromNode().getName());
                desc.append("-");
                desc.append(item.getToNode().getName());
            }
            return desc.toString();
        }
    }
}