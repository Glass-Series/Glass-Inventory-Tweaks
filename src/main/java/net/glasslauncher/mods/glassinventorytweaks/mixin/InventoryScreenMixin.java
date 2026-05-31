package net.glasslauncher.mods.glassinventorytweaks.mixin;

import net.glasslauncher.mods.glassinventorytweaks.api.ShiftClickTreatSeparately;
import net.minecraft.client.gui.screen.ingame.InventoryScreen;
import org.spongepowered.asm.mixin.Mixin;

@ShiftClickTreatSeparately(
        value = {
                8, // armor/inventory
                35,// inventory/hotbar
                46 // hotbar/accessoryapi
        },
        sectionPreference = {
                0, // craftoutput
                0, // craftinput
                4, // armor
                1, // main
                2, // hotbar
                3  // accessoryapi (pray to god this never changes before I add a proper api for this)
        }
)
@Mixin(InventoryScreen.class)
public class InventoryScreenMixin {
}
