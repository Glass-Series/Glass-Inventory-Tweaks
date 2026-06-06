package net.glasslauncher.mods.glassinventorytweaks.impl;

import net.minecraft.item.ItemStack;
import net.minecraft.screen.slot.Slot;

public class StackUtil {

    public static boolean canMerge(Slot potentialSlot, ItemStack stackToMove) {
        ItemStack mergeStack = potentialSlot.getStack();
        return potentialSlot.canInsert(stackToMove) && (mergeStack == null || (mergeStack.isItemEqual(stackToMove) && mergeStack.count < potentialSlot.getMaxItemCount() && mergeStack.count < mergeStack.getItem().getMaxCount()));
    }

    public static boolean canMerge(ItemStack mergeStack, ItemStack stackToMove) {
        return mergeStack == null || (mergeStack.isItemEqual(stackToMove) && mergeStack.count < mergeStack.getItem().getMaxCount());
    }
}
