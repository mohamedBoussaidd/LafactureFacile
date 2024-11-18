package Mboussaid.laFactureFacile.Models;

import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Builder
public class GetDate {
    public static final ZoneId ZONE_ID = ZoneId.of("Europe/Paris");

    private Instant date;

    public static Instant getNowToInstant() {
        System.out.println(ZonedDateTime.now(ZONE_ID));
        return ZonedDateTime.now(ZONE_ID).toInstant();
    }
    public static ZonedDateTime getNow() {
        System.out.println(ZonedDateTime.now(ZONE_ID));
        return ZonedDateTime.now(ZONE_ID);
    }
}
