package uk.ac.ebi.biostd.exporter.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.ArrayList;
import java.util.List;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import uk.ac.ebi.biostd.exporter.jobs.full.xml.BooleanAdapter;

@Data
@AllArgsConstructor
@NoArgsConstructor
@XmlRootElement(name = "attribute")
@XmlAccessorType(XmlAccessType.NONE)
public class Attribute {

    @XmlElement(name = "name")
    private String name;

    @XmlElement(name = "value")
    private String value;

    @JsonProperty("valqual")
    @JsonInclude(Include.NON_EMPTY)
    private String valueQualifierString;

    @JsonIgnore
    @XmlAttribute(name = "reference")
    @XmlJavaTypeAdapter(BooleanAdapter.class)
    private Boolean reference;

    @JsonIgnore
    @XmlElement(name = "valqual")
    public List<Valqual> getValquals() {
        if (valueQualifierString != null) {
            List<Valqual> valquals = new ArrayList<>();
            String[] values = valueQualifierString.split(";");
            for (String value : values) {
                String[] single = value.split("=");
                valquals.add(new Valqual(single[0], single[1]));
            }

            return valquals;
        }

        return null;
    }

    public Attribute(String name, String value) {
        this.name = name;
        this.value = value;
    }
}
