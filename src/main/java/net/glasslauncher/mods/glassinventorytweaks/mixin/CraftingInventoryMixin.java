package net.glasslauncher.mods.glassinventorytweaks.mixin;

import net.glasslauncher.mods.glassinventorytweaks.api.ShouldNotShiftClick;
import net.minecraft.inventory.CraftingInventory;
import org.spongepowered.asm.mixin.Mixin;

@ShouldNotShiftClick
@Mixin(CraftingInventory.class)
public class CraftingInventoryMixin {
}
