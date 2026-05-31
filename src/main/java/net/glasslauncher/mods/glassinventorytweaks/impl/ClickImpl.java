package net.glasslauncher.mods.glassinventorytweaks.impl;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
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

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public class ClickImpl {
    // Important when doing batshit huge move operations.
    private static final Cache<HandledScreen, List<SlotData>> SLOT_CACHE = Caffeine.newBuilder().weakKeys().build();

    public static boolean shiftClick(Slot shiftClickedSlot, HandledScreen handledScreen) {
        if (shiftClickedSlot == null || shiftClickedSlot.getStack() == null) {
            return false;
        }

        Event event = new ShiftClickItemEvent<>(handledScreen, shiftClickedSlot);
        StationAPI.EVENT_BUS.post(event);
        if (shiftClickedSlot.getStack() == null) {
            return true; // Someone emptied the slot, off we go
        }

        List<SlotData> slotGroups = getSlots(handledScreen);
        SlotData originalInv = slotGroups.stream().filter(e -> e.slots.contains(shiftClickedSlot)).findFirst().orElseThrow();
        boolean stashedCursorStack = Minecraft.INSTANCE.player.inventory.getCursorStack() != null;

        Minecraft.INSTANCE.interactionManager.clickSlot(handledScreen.handler.syncId, shiftClickedSlot.id, 0, false, Minecraft.INSTANCE.player);

        boolean result = shiftClickCursorStack(handledScreen, originalInv);

        // Put the stack back
        if (Minecraft.INSTANCE.player.inventory.getCursorStack() != null || stashedCursorStack) {
            Minecraft.INSTANCE.interactionManager.clickSlot(handledScreen.handler.syncId, shiftClickedSlot.id, 0, false, Minecraft.INSTANCE.player);
        }
        return result;
    }

    public static boolean shiftClickCursorStack(HandledScreen handledScreen, SlotData originalInv) {
        ItemStack stackToMove = Minecraft.INSTANCE.player.inventory.getCursorStack();

        if (handledScreen.handler.slots.isEmpty() || stackToMove == null) {
            return false;
        }

        List<SlotData> slotGroups = getSlots(handledScreen);

        for (SlotData group : slotGroups) {
            if (group == originalInv) {
                continue;
            }

            for (Slot potentialSlot : group.slots) {
                if (potentialSlot.inventory.getClass().isAnnotationPresent(ShouldNotShiftClick.class) || potentialSlot.getClass().isAnnotationPresent(ShouldNotShiftClick.class)) {
                    continue;
                }
                ItemStack mergeStack = potentialSlot.getStack();

                if (potentialSlot.canInsert(stackToMove) && (mergeStack == null || (mergeStack.isItemEqual(stackToMove) && mergeStack.count < potentialSlot.getMaxItemCount() && mergeStack.count < mergeStack.getItem().getMaxCount()))) {

                    Minecraft.INSTANCE.interactionManager.clickSlot(handledScreen.handler.syncId, potentialSlot.id, 0, false, Minecraft.INSTANCE.player);

                    if (Minecraft.INSTANCE.player.inventory.getCursorStack() == null) {
                        return true;
                    }
                }
            }
        }
        return true;
    }

    public static List<SlotData> getSlots(HandledScreen handledScreen) {
        return SLOT_CACHE.get(handledScreen, ClickImpl::_getSlots);
    }

    private static List<SlotData> _getSlots(HandledScreen handledScreen) {
        List<SlotData> slotGroups = new ArrayList<>();
        int inventoryDataIndex = 0;
        int nextSplitPointIndex = 0;
        Inventory lastInv = null;

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

            lastInv = currentInv;
        }

        slotGroups.sort(Comparator.comparingInt(e -> -e.priority));
        return slotGroups;
    }
}
