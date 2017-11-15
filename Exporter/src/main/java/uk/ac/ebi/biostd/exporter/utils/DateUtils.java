package uk.ac.ebi.biostd.exporter.utils;

import static java.util.concurrent.TimeUnit.HOURS;
import static java.util.concurrent.TimeUnit.MILLISECONDS;
import static java.util.concurrent.TimeUnit.MINUTES;

import java.time.Instant;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import lombok.experimental.UtilityClass;

@UtilityClass
public class DateUtils {

    public String getFromEpochSeconds(long seconds) {
        OffsetDateTime dateTime = Instant.ofEpochSecond(seconds).atOffset(ZoneOffset.UTC);

        return String.format("%d-%02d-%02d", dateTime.getYear(), dateTime.getMonthValue(), dateTime.getDayOfMonth());
    }

    public String getFromEpochMiliseconds(long milliseconds) {
        OffsetDateTime dateTime = Instant.ofEpochMilli(milliseconds).atOffset(ZoneOffset.UTC);

        return String.format("%d-%02d-%02d %d:%02d:%02d", dateTime.getYear(), dateTime.getMonthValue(),
                dateTime.getDayOfMonth(), dateTime.getHour(), dateTime.getMinute(), dateTime.getSecond());
    }

    public String getElapsedTime(long milliseconds) {
        long hours = MILLISECONDS.toHours(milliseconds);
        long minute = MILLISECONDS.toMinutes(milliseconds) - HOURS.toMinutes(hours);
        long second = MILLISECONDS.toSeconds(milliseconds) - HOURS.toSeconds(hours) - MINUTES.toSeconds(minute);

        return String.format("%02d-%02d-%02d", hours, minute, second);
    }
}
