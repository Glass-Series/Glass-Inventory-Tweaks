package net.glasslauncher.mods.glassinventorytweaks.impl;

import net.fabricmc.api.EnvType;
import net.fabricmc.loader.api.FabricLoader;
import net.glasslauncher.mods.glassinventorytweaks.api.ShouldNotShiftClick;
import net.glasslauncher.mods.glassinventorytweaks.api.event.ShiftClickItemEvent;
import net.mine_diver.unsafeevents.Event;
import net.minecraft.client.Minecraft;
import net.minecraft.item.ItemStack;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.screen.slot.Slot;
import net.modificationstation.stationapi.api.StationAPI;
import net.modificationstation.stationapi.api.network.packet.PacketHelper;

import java.util.List;

public class ModdedClickImpl {

    public static boolean run(ClickType type, int[] clickedSlots, ScreenHandler handler) {
        PacketHelper.send(new InventoryTweaksPacket(handler, type, clickedSlots));
        return true;
    }

    public static boolean shiftClick(Slot shiftClickedSlot, ScreenHandler handler) {
        if (shiftClickedSlot == null || shiftClickedSlot.getStack() == null) {
            return false;
        }

        Event event = new ShiftClickItemEvent<>(handler, shiftClickedSlot);
        StationAPI.EVENT_BUS.post(event);
        if (shiftClickedSlot.getStack() == null) {
            return true; // Someone emptied the slot, off we go
        }

        List<SlotData> slotGroups = SlotHelper.getSlots(handler);
        SlotData originalInv = slotGroups.stream().filter(e -> e.slots.contains(shiftClickedSlot)).findFirst().orElseThrow();

        return shiftClickStack(shiftClickedSlot, handler, originalInv);
    }

    public static boolean shiftClickStack(Slot shiftClickedSlot, ScreenHandler handler, SlotData originalInv) {
        ItemStack stackToMove = shiftClickedSlot.getStack();

        List<SlotData> slotGroups = SlotHelper.getSlots(handler);

        for (SlotData group : slotGroups) {
            if (group == originalInv) {
                continue;
            }

            for (Slot potentialSlot : group.slots) {
                if (potentialSlot == shiftClickedSlot || potentialSlot.inventory.getClass().isAnnotationPresent(ShouldNotShiftClick.class) || potentialSlot.getClass().isAnnotationPresent(ShouldNotShiftClick.class)) {
                    continue;
                }
                ItemStack mergeStack = potentialSlot.getStack();

                if (potentialSlot.canInsert(stackToMove) && (mergeStack == null || (mergeStack.isItemEqual(stackToMove) && mergeStack.count < potentialSlot.getMaxItemCount() && mergeStack.count < mergeStack.getItem().getMaxCount()))) {

                    int maxCount;
                    if (mergeStack != null) {
                        maxCount = Math.min(mergeStack.count, potentialSlot.getMaxItemCount()) - mergeStack.count;
                    } else {
                        maxCount = potentialSlot.getMaxItemCount();
                    }

                    ItemStack toAdd = shiftClickedSlot.takeStack(maxCount);
                    if (mergeStack != null) {
                        mergeStack.count += toAdd.count;
                    } else {
                        potentialSlot.setStack(toAdd);
                    }
                    if (stackToMove.count < 1) {
                        return true; // stack empty, stop the search
                    }
                }
            }
        }
        return true;
    }
}
