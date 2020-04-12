package kilanny.muslimalarm.data;

import java.util.ArrayList;

import kilanny.muslimalarm.R;

public final class Tune {
    public final int rawResId, nameResId, id;
    public boolean selected, playing;

    private Tune(int id, int rawResId, int nameResId) {
        this.id = id;
        this.rawResId = rawResId;
        this.nameResId = nameResId;
    }

    public static Tune findTuneOrDefault(int tune, int defaultTuneIdx) {
        Tune[] tunes = getTunes(0);
        for (Tune value : tunes) {
            if (value.rawResId == tune)
                return value;
        }
        for (Tune value : tunes) {
            if (value.id == tune)
                return value;
        }
        return tunes[defaultTuneIdx];
    }

    /**
     * @param mode 0 -> all. 1 -> Short Azan. 2 -> Long Azan, 3 -> All azan, 4 -> All non azan
     */
    public static Tune[] getTunes(int mode) {
        final int[] sounds = {
                R.raw.air_raid_alarm, R.raw.alarm, R.raw.alarm_1, R.raw.alarm_clock,
                R.raw.azan_final, R.raw.azan_haya, R.raw.azan_makkah, R.raw.azan_otaiby,
                R.raw.alarm_clock_new_s5, R.raw.alarm_loud, R.raw.alarm_ring,
                R.raw.azan_qatami, R.raw.azan_quite, R.raw.azan_salat_khair, R.raw.azan_salati,
                R.raw.alarm_rooster, R.raw.alarm_vs_turbo, R.raw.alarms, R.raw.animal_cow,
                R.raw.azan_full_abdulbasit, R.raw.azan_full_deedat, R.raw.azan_full_egy,
                R.raw.animal_horse, R.raw.animal_sounds_cats, R.raw.car_alaram_2009,
                R.raw.azan_full_egy2, R.raw.azan_full_egy3,
                R.raw.clock_alarm_samsung, R.raw.craw, R.raw.extreme_alarm, R.raw.loud_alarm,
                R.raw.loud_alarm_clock, R.raw.loud_alarm_sound, R.raw.loud_alarm_tone,
                R.raw.loud_continuous_beep, R.raw.loud_snoring, R.raw.monkey_sound,
                R.raw.msg_foundx20, R.raw.old_alarm_clock_best, R.raw.puma_roar,
                R.raw.real_alarm_tone, R.raw.ttwu7, R.raw.warning, R.raw.wolf
        };
        final int[] ids = {
                1, 2, 3, 4,
                32, 33, 34, 35,
                5, 6, 7,
                36, 37, 38, 39,
                8, 9, 10, 11,
                40, 41, 42,
                12, 13, 14,
                43, 44,
                15, 16, 17, 18,
                19, 20, 21,
                22, 23, 24,
                25, 26, 27,
                28, 29, 30, 31
        };
        final int[] names = {
                R.string.nuclear_alert, R.string.hazard_warning, R.string.car_guard, R.string.old_peep_alarm,
                R.string.azan_final, R.string.azan_haya, R.string.azan_makkah, R.string.azan_otaiby,
                R.string.hazard_traffic, R.string.annoying_surprise, R.string.loud_tune,
                R.string.azan_qatami, R.string.azan_quite, R.string.azan_salat_khair, R.string.azan_salati,
                R.string.rooster, R.string.ambulance_vehicle, R.string.police, R.string.cow,
                R.string.azan_full_abdulbasit, R.string.azan_full_deedat, R.string.azan_full_egy,
                R.string.horse, R.string.birds, R.string.traffic_trouble,
                R.string.azan_full_egy2, R.string.azan_full_egy3,
                R.string.shocks, R.string.crow, R.string.extreme_alarm, R.string.warning_siren,
                R.string.annoying_tune_fixed, R.string.upward_annoying_tune, R.string.bubbles,
                R.string.constant_wave, R.string.high_snoring, R.string.monkey,
                R.string.metal_gear, R.string.old_alarm, R.string.tiger,
                R.string.drops, R.string.phone_alert, R.string.bubbles_fast, R.string.wolf
        };
        ArrayList<Tune> tunes = new ArrayList<>();
        for (int i = 0; i < sounds.length; ++i) {
            if (mode == 1) {
                if (ids[i] < 32 || ids[i] > 39) continue;
            } else if (mode == 2) {
                if (ids[i] < 40) continue;
            } else if (mode == 3) {
                if (ids[i] < 32) continue;
            } else if (mode == 4) {
                if (ids[i] >= 32) continue;
            }
            tunes.add(new Tune(ids[i], sounds[i], names[i]));
        }
        return tunes.toArray(new Tune[0]);
    }
}
