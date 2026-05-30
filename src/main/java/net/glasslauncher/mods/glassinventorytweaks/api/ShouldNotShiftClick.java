package net.glasslauncher.mods.glassinventorytweaks.api;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Annotate an inventory with this (works with mixins!) to make sure an inventory is NEVER shift clicked into.
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface ShouldNotShiftClick {
}
