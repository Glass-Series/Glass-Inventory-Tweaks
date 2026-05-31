package net.glasslauncher.mods.glassinventorytweaks.api;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/**
 * Splits the associated ScreenHandler's slots at the given index, and treats the two halves as separate inventories.
 */
@Retention(RetentionPolicy.RUNTIME)
public @interface ShiftClickTreatSeparately {
    /**
     * If you want to further divide the inventory, you can specify slot indexes to split by.
     */
    int[] value() default {-1};

    /**
     * This is an array of priorities relative to the inventory section index.
     * Higher means it'll be shift+clicked first.
     */
    int[] sectionPreference() default {};
}
