package net.glasslauncher.mods.glassinventorytweaks.api;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/**
 * Splits the associated ScreenHandler's slots at the given index, and treats the two halves as separate inventories.
 */
@Retention(RetentionPolicy.RUNTIME)
public @interface ShiftClickTreatSeparately {
    int[] value() default {-1};
    int[] sectionPreference() default {};
}
