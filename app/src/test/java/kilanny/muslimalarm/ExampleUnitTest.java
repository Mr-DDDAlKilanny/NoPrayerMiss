package kilanny.muslimalarm;

import org.junit.Test;

import java.util.Map;

import kilanny.muslimalarm.util.PrayTime;

import static org.junit.Assert.*;

/**
 * Example local unit test, which will execute on the development machine (host).
 *
 * @see <a href="http://d.android.com/tools/testing">Testing documentation</a>
 */
public class ExampleUnitTest {
    @Test
    public void PrayTimeIsNotNan() {
        for (int calcMethod = 0; calcMethod < 7; ++calcMethod) {
            for (int asr = 0; asr < 2; ++asr) {
                for (int angle = 0; angle < 4; ++angle) {
                    for (double lng = -180; lng <= 180; lng += 0.1) {
                        for (double lat = -90; lat <= 90; lat += 0.1) {
                            Map<String, String> prayerTimes = PrayTime.getPrayerTimes(calcMethod,
                                    asr,
                                    angle,
                                    lat,
                                    lng,
                                    PrayTime.TIME_24,
                                    2020,
                                    4,
                                    12,
                                    3,
                                    0);
                            for (String value : prayerTimes.values()) {
                                assertNotEquals(value, PrayTime.InvalidTime);
                            }
                        }
                    }
                }
            }
        }
    }
}