package uk.ac.ebi.biostd.exporter.utils;

import lombok.experimental.UtilityClass;

@UtilityClass
public class JsonUtil {

    public String unWrapJsonObject(String objectJson) {
        return objectJson.replaceAll("[{}]", "");
    }
}
