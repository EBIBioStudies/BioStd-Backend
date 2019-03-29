package uk.ac.ebi.biostd.exporter.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.ArrayList;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.apache.commons.lang3.StringUtils;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class Attribute {

    private String name;
    private String value;

    @JsonProperty("valqual")
    @JsonInclude(Include.NON_EMPTY)
    private String valueQualifierString;

    @JsonIgnore
    private Boolean reference;

    @JsonIgnore
    public List<Valqual> getValquals() {
        if (StringUtils.isNotBlank(valueQualifierString)) {
            List<Valqual> valquals = new ArrayList<>();
            String[] values = valueQualifierString.split(";");
            for (String value : values) {
                String[] single = value.split("=");
                Valqual valqual = single.length > 1 ? new Valqual(single[0], single[1]) : new Valqual(single[0], "");

                valquals.add(valqual);
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
