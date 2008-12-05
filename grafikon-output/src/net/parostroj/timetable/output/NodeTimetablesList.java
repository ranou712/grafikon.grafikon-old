/*
 * NodeTimetablesList.java
 * 
 * Created on 11.9.2007, 19:33:20
 */
package net.parostroj.timetable.output;

import java.io.IOException;
import java.io.Writer;
import java.util.Collection;
import java.util.Formatter;
import java.util.List;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import net.parostroj.timetable.actions.NodeSort;
import net.parostroj.timetable.actions.TrainsHelper;
import net.parostroj.timetable.model.*;
import net.parostroj.timetable.utils.*;

/**
 * List of timetables for nodes.
 * 
 * @author jub
 */
public class NodeTimetablesList {
    
    private static final Logger LOG = Logger.getLogger(NodeTimetablesList.class.getName());

    private List<Node> nodes;
    private NodeTimetablesListTemplates templates;
    private TrainDiagram diagram;

    public NodeTimetablesList(Collection<Node> aNodes, TrainDiagram diagram) {
        NodeSort s = new NodeSort(NodeSort.Type.ASC);
        nodes = s.sortWithoutSignals(aNodes);
        templates = new NodeTimetablesListTemplates();
        this.diagram = diagram;
    }

    public void writeTo(Writer writer) throws IOException {
        Formatter f = new Formatter(writer);
        writer.write(String.format(templates.getHtmlHeader(),templates.getString("node.timetable")));
        for (Node node : nodes) {
            f.format(templates.getTimetableHeader(), node.getName(),
                templates.getString("column.train"),
                templates.getString("column.from"),
                templates.getString("column.arrival"),
                templates.getString("column.track"),
                templates.getString("column.departure"),
                templates.getString("column.to"),
                templates.getString("column.notes")
              );
            for (TimeInterval i : this.collectIntervals(node)) {
                this.writeLine(f, writer, i);
            }
            writer.write(templates.getTimetableFooter());
        }
        writer.write(templates.getHtmlFooter());
    }
    
    private void writeLine(Formatter f, Writer writer, TimeInterval i) throws IOException {
        TimeInterval from = i.getTrain().getIntervalBefore(i);
        TimeInterval to = i.getTrain().getIntervalAfter(i);
        
        String fromNodeName = TransformUtil.getFromAbbr(i);
        String toNodeName = TransformUtil.getToAbbr(i);
        
        String fromTime = (from == null && !i.getType().isTechnological()) ? "&nbsp;" : TimeConverter.convertFromIntToText(i.getStart());
        String toTime = (to == null && !i.getType().isTechnological()) ? "&nbsp;" : TimeConverter.convertFromIntToText(i.getEnd());
        
        String comment = this.generateComment(i);
        
        f.format(templates.getTimetableLine(), i.getTrain().getName(),fromNodeName,fromTime,i.getTrack().getNumber(),toTime,toNodeName,comment);
    }
    
    private TimeIntervalList collectIntervals(Node node) {
        TimeIntervalList list = new TimeIntervalList();
        for (NodeTrack track : node.getTracks()) {
            for (TimeInterval i : track.getTimeIntervalList()) {
                list.addIntervalByStartTime(i);
            }
        }
        return list;
    }
    
    private String generateComment(TimeInterval interval) {
        // technological time handle differently
        if (interval.getType().isTechnological())
            return templates.getString("technological.time");

        StringBuilder comment = new StringBuilder();
        this.generateCommentWithWeight(interval, comment);
        this.generateCommentForEngineCycle(interval, comment);
        this.generateCommentForTrainUnitCycle(interval, comment);
        // comment itself
        String commentStr = (String)interval.getAttribute("comment");
        if (commentStr != null && !commentStr.equals("")) {
            this.appendDelimiter(comment);
            comment.append(commentStr);
        }
        if (Boolean.TRUE.equals(interval.getAttribute("occupied"))) {
            this.appendDelimiter(comment);
            comment.append(TrainTimetablesListTemplates.getString("entry.occupied"));
        }
        if (comment.length() == 0)
            return "&nbsp;";
        else
            return comment.toString();
    }
    
    private void generateCommentForEngineCycle(TimeInterval interval, StringBuilder comment) {
        Train train = interval.getTrain();
        for (TrainsCycleItem item : train.getCycles(TrainsCycleType.ENGINE_CYCLE)) {
            if (item.getToInterval() == interval) {
                // end
                TrainsCycleItem itemNext = item.getCycle().getNextItem(item);
                if (itemNext != null) {
                    this.appendDelimiter(comment);
                    comment.append(templates.getString("engine.to"));
                    comment.append(' ').append(itemNext.getTrain().getName());
                    comment.append(" (").append(TimeConverter.convertFromIntToText(itemNext.getStartTime()));
                    comment.append(')');
                }
            }
            if (item.getFromInterval() == interval) {
                // start
                this.appendDelimiter(comment);
                comment.append(templates.getString("engine")).append(": ");
                comment.append(item.getCycle().getName()).append(" (");
                comment.append(TransformUtil.getEngineCycleDescription(item.getCycle())).append(')');
            }
        }
    }
    
    private void generateCommentForTrainUnitCycle(TimeInterval interval, StringBuilder comment) {
        Train train = interval.getTrain();
        for (TrainsCycleItem item : train.getCycles(TrainsCycleType.TRAIN_UNIT_CYCLE)) {
            // end
            if (item.getToInterval() == interval) {
                TrainsCycleItem itemNext = item.getCycle().getNextItem(item);
                if (itemNext != null) {
                    this.appendDelimiter(comment);
                    comment.append(templates.getString("train.unit")).append(": ");
                    comment.append(item.getCycle().getName()).append(" (");
                    comment.append(item.getCycle().getDescription()).append(") ");
                    comment.append(templates.getString("move.to"));
                    comment.append(' ').append(itemNext.getTrain().getName());
                    comment.append(" (").append(TimeConverter.convertFromIntToText(itemNext.getStartTime()));
                    comment.append(')');
                }
            }
            // start
            if (item.getFromInterval() == interval) {
                this.appendDelimiter(comment);
                comment.append(templates.getString("train.unit")).append(": ");
                comment.append(item.getCycle().getName()).append(" (");
                comment.append(item.getCycle().getDescription()).append(')');
            }
        }
    }

    private static Pattern NUMBER = Pattern.compile("\\d+");

    private void generateCommentWithWeight(TimeInterval interval, StringBuilder comment) {
        Train train = interval.getTrain();
        if (interval.getTrain().getIntervalAfter(interval) != null) {
            Integer weight = TrainsHelper.getNextWeight(interval.getOwnerAsNode(), train);
            if (weight != null) {
                // new style of weight information
                this.appendDelimiter(comment);
                comment.append('[');
                comment.append(weight);
                comment.append("t]");
            } else {
                // check old style comment
                String weightStr = (String) train.getAttribute("weight.info");
                if (weightStr != null && !weightStr.trim().equals("")) {
                    this.appendDelimiter(comment);
                    comment.append('[');
                    comment.append(weightStr);
                    comment.append("t]");
                }
                // try to convert weight string to number
                if (weightStr != null) {
                    try {
                        Matcher matcher = NUMBER.matcher(weightStr);
                        if (matcher.find()) {
                            String number = matcher.group(0);
                            weight = Integer.valueOf(number);
                        }
                    } catch (NumberFormatException e) {
                        LOG.fine("Cannot convert weight to number: " + weightStr);
                    }
                }
            }
            // if weight is computed the convert to length
            Integer length = TrainsHelper.convertWeightToLengt(train, diagram, weight);
            if (length != null) {
                comment.append("[");
                comment.append(length);
                String lengthUnit = null;
                if (Boolean.TRUE.equals(diagram.getAttribute("station.length.in.axles"))) {
                    lengthUnit = " " + TrainTimetablesListTemplates.getString("length.axles");
                } else {
                    lengthUnit = (String)diagram.getAttribute("station.length.unit");
                }
                if (lengthUnit == null)
                    lengthUnit = "";
                comment.append(lengthUnit).append("]");
            }
        }
    }

    private void appendDelimiter(StringBuilder str) {
        if (str.length() > 0)
            str.append(", ");
    }
}
