package net.parostroj.timetable.model.ls.impl3;

import net.parostroj.timetable.model.TrainType;
import java.awt.Color;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;
import net.parostroj.timetable.model.SpeedingBrakingType;
import net.parostroj.timetable.model.TrainsData;
import net.parostroj.timetable.utils.Conversions;

/**
 * Train type storage class.
 *
 * @author jub
 */
@XmlRootElement(name = "train_type")
@XmlType(propOrder = {"id", "abbr", "desc", "color", "braking", "platform", "trainNameTemplate", "trainCompleteNameTemplate"})
public class LSTrainType {

    private String id;
    private String abbr;
    private String desc;
    private String color;
    private String braking;
    private boolean platform;
    private String trainNameTemplate;
    private String trainCompleteNameTemplate;

    public LSTrainType() {
    }

    public LSTrainType(TrainType type) {
        this.id = type.getId();
        this.abbr = type.getAbbr();
        this.desc = type.getDesc();
        this.platform = type.isPlatform();
        Color c = type.getColor();
        this.color = Conversions.convertColorToText(c);
        this.braking = type.getSbType().toString();
        this.trainNameTemplate = type.getTrainNameTemplate();
        this.trainCompleteNameTemplate = type.getTrainCompleteNameTemplate();
    }

    public String getAbbr() {
        return abbr;
    }

    public void setAbbr(String abbr) {
        this.abbr = abbr;
    }

    public String getBraking() {
        return braking;
    }

    public void setBraking(String braking) {
        this.braking = braking;
    }

    public String getColor() {
        return color;
    }

    public void setColor(String color) {
        this.color = color;
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

    public boolean isPlatform() {
        return platform;
    }

    public void setPlatform(boolean platform) {
        this.platform = platform;
    }

    @XmlElement(name = "name_template")
    public String getTrainNameTemplate() {
        return trainNameTemplate;
    }

    public void setTrainNameTemplate(String trainNameTemplate) {
        this.trainNameTemplate = trainNameTemplate;
    }

    @XmlElement(name = "complete_name_template")
    public String getTrainCompleteNameTemplate() {
        return trainCompleteNameTemplate;
    }

    public void setTrainCompleteNameTemplate(String trainCompleteNameTemplate) {
        this.trainCompleteNameTemplate = trainCompleteNameTemplate;
    }
    
    public TrainType createTrainType(TrainsData data) {
        TrainType type = new TrainType(id);
        type.setAbbr(abbr);
        type.setColor(Conversions.convertTextToColor(color));
        type.setDesc(desc);
        type.setPlatform(platform);
        type.setSbType(SpeedingBrakingType.fromString(braking));
        type.setTrainCompleteNameTemplate(trainCompleteNameTemplate);
        type.setTrainNameTemplate(trainNameTemplate);
        type.setTrainsData(data);
        return type;
    }
}