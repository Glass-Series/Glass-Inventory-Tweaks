package net.glasslauncher.mods.glassinventorytweaks.impl;

import net.glasslauncher.mods.glassinventorytweaks.api.ShiftClickTreatSeparately;
import net.glasslauncher.mods.glassinventorytweaks.api.ShouldNotShiftClick;
import net.glasslauncher.mods.glassinventorytweaks.events.init.ShiftClickItemEvent;
import net.mine_diver.unsafeevents.Event;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screen.ingame.HandledScreen;
import net.minecraft.inventory.Inventory;
import net.minecraft.item.ItemStack;
import net.minecraft.screen.slot.Slot;
import net.modificationstation.stationapi.api.StationAPI;
import org.lwjgl.input.Keyboard;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public class ClickImpl {

    public static boolean shiftClick(Slot shiftClickedSlot, HandledScreen handledScreen) {
        if (shiftClickedSlot == null || shiftClickedSlot.getStack() == null) {
            return false;
        }

        SlotData originalInv = null;

        Event event = new ShiftClickItemEvent<>(handledScreen, shiftClickedSlot);
        StationAPI.EVENT_BUS.post(event);
        if (shiftClickedSlot.getStack() == null) {
            return true; // Someone emptied the slot, off we go
        }
        boolean stashedCursorStack = Minecraft.INSTANCE.player.inventory.getCursorStack() != null;

        ItemStack stackToMove = shiftClickedSlot.getStack();

        List<SlotData> slotGroups = new ArrayList<>();
        int inventoryDataIndex = 0;
        int nextSplitPointIndex = 0;
        Inventory lastInv = null;

        if (handledScreen.handler.slots.isEmpty()) {
            return true;
        }
        //noinspection unchecked
        List<Slot> allSlots = handledScreen.handler.slots;

        ShiftClickTreatSeparately annotation = handledScreen.getClass().getAnnotation(ShiftClickTreatSeparately.class);
        int[] splitPoints = annotation != null ? annotation.value() : null;

        for (Slot slot : allSlots) {
            Inventory currentInv = slot.inventory;

            if (currentInv != lastInv) {
                if (!slotGroups.isEmpty()) {
                    if (slotGroups.get(inventoryDataIndex).slots.isEmpty()) {
                        slotGroups.remove(inventoryDataIndex);
                    }
                    else {
                        inventoryDataIndex++;
                    }
                }

                while (slotGroups.size() <= inventoryDataIndex) {
                    slotGroups.add(new SlotData(currentInv, inventoryDataIndex, annotation == null || annotation.sectionPreference().length <= inventoryDataIndex ? inventoryDataIndex : annotation.sectionPreference()[inventoryDataIndex]));
                }
            }

            if (splitPoints != null && nextSplitPointIndex < splitPoints.length) {
                int slotIndex = slot.id;

                if (slotIndex == splitPoints[nextSplitPointIndex]) {
                    slotGroups.get(inventoryDataIndex).slots.add(slot);

                    nextSplitPointIndex++;
                    inventoryDataIndex++;
                    while (slotGroups.size() <= inventoryDataIndex) {
                        slotGroups.add(new SlotData(currentInv, inventoryDataIndex, annotation == null || annotation.sectionPreference().length <= inventoryDataIndex ? inventoryDataIndex : annotation.sectionPreference()[inventoryDataIndex]));
                    }

                    lastInv = currentInv;
                    continue;
                }
            }

            slotGroups.get(inventoryDataIndex).slots.add(slot);

            if (slot == shiftClickedSlot) {
                originalInv = slotGroups.get(inventoryDataIndex);
            }

            lastInv = currentInv;
        }

        slotGroups.sort(Comparator.comparingInt(e -> -e.priority));

        Minecraft.INSTANCE.interactionManager.clickSlot(handledScreen.handler.syncId, shiftClickedSlot.id, 0, false, Minecraft.INSTANCE.player);

        for (SlotData group : slotGroups) {
            if (originalInv != null && group == originalInv) {
                continue;
            }

            for (Slot potentialSlot : group.slots) {
                if (potentialSlot.inventory.getClass().isAnnotationPresent(ShouldNotShiftClick.class) || potentialSlot.getClass().isAnnotationPresent(ShouldNotShiftClick.class)) {
                    continue;
                }
                ItemStack mergeStack = potentialSlot.getStack();

                if (potentialSlot.canInsert(stackToMove) && (mergeStack == null || (mergeStack.isItemEqual(stackToMove) && mergeStack.count < shiftClickedSlot.getMaxItemCount() && mergeStack.count < mergeStack.getItem().getMaxCount()))) {

                    Minecraft.INSTANCE.interactionManager.clickSlot(handledScreen.handler.syncId, potentialSlot.id, 0, false, Minecraft.INSTANCE.player);

                    if (Minecraft.INSTANCE.player.inventory.getCursorStack() == null) {
                        if (stashedCursorStack) {
                            Minecraft.INSTANCE.interactionManager.clickSlot(handledScreen.handler.syncId, shiftClickedSlot.id, 0, false, Minecraft.INSTANCE.player);
                        }
                        return true;
                    }
                }
            }
        }
        // Put the stack back
        if (Minecraft.INSTANCE.player.inventory.getCursorStack() != null) {
            Minecraft.INSTANCE.interactionManager.clickSlot(handledScreen.handler.syncId, shiftClickedSlot.id, 0, false, Minecraft.INSTANCE.player);
        }
        else if (stashedCursorStack) {
            Minecraft.INSTANCE.interactionManager.clickSlot(handledScreen.handler.syncId, shiftClickedSlot.id, 0, false, Minecraft.INSTANCE.player);
        }
        return true;
    }
}
