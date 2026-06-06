package net.glasslauncher.mods.glassinventorytweaks.impl;

import net.fabricmc.api.EnvType;
import net.fabricmc.loader.api.FabricLoader;
import net.glasslauncher.mods.glassinventorytweaks.api.ShouldNotShiftClick;
import net.glasslauncher.mods.glassinventorytweaks.api.event.ShiftClickItemEvent;
import net.mine_diver.unsafeevents.Event;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.network.NetworkHandler;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.screen.slot.Slot;
import net.minecraft.server.network.ServerPlayNetworkHandler;
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
        return shiftClick(shiftClickedSlot, handler, shiftClickedSlot.getStack().count);
    }

    public static boolean shiftClick(Slot shiftClickedSlot, ScreenHandler handler, int amount) {
        if (shiftClickedSlot == null || shiftClickedSlot.getStack() == null) {
            return false;
        }

        Event event = new ShiftClickItemEvent<>(handler, shiftClickedSlot);
        StationAPI.EVENT_BUS.post(event);
        ItemStack stack = shiftClickedSlot.getStack();
        if (stack == null) {
            return true; // Someone emptied the slot, off we go
        }

        List<SlotData> slotGroups = SlotHelper.getSlots(handler);
        SlotData originalInv = slotGroups.stream().filter(e -> e.slots.contains(shiftClickedSlot)).findFirst().orElseThrow();

        return shiftClickStack(shiftClickedSlot, handler, originalInv, amount);
    }

    public static boolean shiftClickStack(Slot shiftClickedSlot, ScreenHandler handler, SlotData originalInv, int amount) {
        ItemStack stackToMove = shiftClickedSlot.getStack();
        boolean shouldReMerge = false;
        if (amount == 0) {
            return false;
        }
        if (amount != stackToMove.count) {
            stackToMove = stackToMove.copy();
            stackToMove.count = Math.min(amount, stackToMove.count);
            shouldReMerge = true;
        }

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

                if (StackUtil.canMerge(potentialSlot, stackToMove)) {

                    int maxCount;
                    if (mergeStack != null) {
                        maxCount = Math.min(mergeStack.getMaxCount(), potentialSlot.getMaxItemCount()) - mergeStack.count;
                    } else {
                        maxCount = potentialSlot.getMaxItemCount();
                    }

                    ItemStack toAdd = shiftClickedSlot.takeStack(maxCount);
                    if (toAdd == null) {
                        return true;
                    }
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
        if (shouldReMerge) {
            shiftClickedSlot.getStack().count += stackToMove.count;
        }
        return true;
    }

    public static boolean handleHotbar(int[] slots, ScreenHandler handler, PlayerEntity player) {
        Slot slot = handler.getSlot(slots[1]);
        if (slot == null) {
            return false;
        }

        ItemStack existingHotbarItem = player.inventory.main[slots[0]];

        if (!slot.canInsert(existingHotbarItem)) {
            return false;
        }

        ItemStack stack = slot.getStack();
        player.inventory.main[slots[0]] = stack;
        slot.setStack(existingHotbarItem);
        return true;
    }

    public static boolean spreadSingle(ItemStack cursorStack, int[] slots, ScreenHandler handler, PlayerEntity player) {
        if (cursorStack == null) {
            return false;
        }

        for (int slotId : slots) {
            Slot slot = handler.getSlot(slotId);
            if (StackUtil.canMerge(slot, cursorStack)) {
                if (cursorStack.count <= 0) {
                    player.inventory.setCursorStack(null);
                    return true;
                }
                if (slot.getStack() != null) {
                    slot.getStack().count++;
                }
                else {
                    slot.setStack(cursorStack.copy());
                    slot.getStack().count = 1;
                }
                cursorStack.count--;
            }
        }

        if (cursorStack.count <= 0) {
            player.inventory.setCursorStack(null);
            return true;
        }

        return false;
    }

    public static boolean spreadEven(ItemStack cursorStack, int[] slots, ScreenHandler handler, PlayerEntity player) {
        if (cursorStack == null) {
            return false;
        }
        int divisionCount = (int) Math.floor(cursorStack.count / (float) slots.length);
        if (divisionCount == 0) {
            return false;
        }
        int diff;
        for (int slotId : slots) {
            if (cursorStack.count <= 0) {
                player.inventory.setCursorStack(null);
                return true;
            }

            if (cursorStack.count < divisionCount) { // Can't properly distribute, return time
                return true;
            }

            Slot slot = handler.getSlot(slotId);
            if (StackUtil.canMerge(slot, cursorStack)) {
                if (slot.hasStack()) {
                    diff = Math.min(divisionCount, Math.min(slot.getMaxItemCount(), cursorStack.getMaxCount()) - slot.getStack().count);
                    slot.getStack().count += diff;
                }
                else {
                    diff = divisionCount;
                    ItemStack newStack = cursorStack.copy();
                    newStack.count = divisionCount;
                    slot.setStack(newStack);
                }

                cursorStack.count -= diff;
            }
        }

        if (cursorStack.count <= 0) {
            player.inventory.setCursorStack(null);
            return true;
        }
        return true;
    }
}
