package net.glasslauncher.mods.glassinventorytweaks.impl;

import net.glasslauncher.mods.glassinventorytweaks.api.ShouldNotShiftClick;
import net.glasslauncher.mods.glassinventorytweaks.api.event.ShiftClickItemEvent;
import net.mine_diver.unsafeevents.Event;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screen.ingame.HandledScreen;
import net.minecraft.item.ItemStack;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.screen.slot.Slot;
import net.modificationstation.stationapi.api.StationAPI;
import org.lwjgl.input.Mouse;

import java.util.List;
import java.util.Set;

public class VanillaClickImpl {

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
        boolean stashedCursorStack = Minecraft.INSTANCE.player.inventory.getCursorStack() != null;

        Minecraft.INSTANCE.interactionManager.clickSlot(handler.syncId, shiftClickedSlot.id, 0, false, Minecraft.INSTANCE.player);

        boolean result = shiftClickCursorStack(handler, originalInv);

        // Put the stack back
        if (Minecraft.INSTANCE.player.inventory.getCursorStack() != null || stashedCursorStack) {
            Minecraft.INSTANCE.interactionManager.clickSlot(handler.syncId, shiftClickedSlot.id, 0, false, Minecraft.INSTANCE.player);
        }
        return result;
    }

    public static boolean shiftClickCursorStack(ScreenHandler handler, SlotData originalInv) {
        ItemStack stackToMove = Minecraft.INSTANCE.player.inventory.getCursorStack();

        if (handler.slots.isEmpty() || stackToMove == null) {
            return false;
        }

        List<SlotData> slotGroups = SlotHelper.getSlots(handler);

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

                    Minecraft.INSTANCE.interactionManager.clickSlot(handler.syncId, potentialSlot.id, 0, false, Minecraft.INSTANCE.player);

                    if (Minecraft.INSTANCE.player.inventory.getCursorStack() == null) {
                        return true;
                    }
                }
            }
        }
        return true;
    }

    public static boolean handleScroll(HandledScreen handledScreen, float wheel) {
        if (Minecraft.INSTANCE.player.inventory.getCursorStack() != null) {
            return false;
        }

        int mouseX = Mouse.getEventX() * handledScreen.width / Minecraft.INSTANCE.displayWidth;
        int mouseY = handledScreen.height - Mouse.getEventY() * handledScreen.height / Minecraft.INSTANCE.displayHeight - 1;
        Slot shiftClickedSlot = handledScreen.getSlotAt(mouseX, mouseY);

        if (shiftClickedSlot == null) {
            return false;
        }

        if (Minecraft.INSTANCE.player.inventory.getCursorStack() == null) { // Sorry pal, minecraft code limitations
            SlotData originalInv = SlotHelper.getSlots(handledScreen.handler).stream().filter(e -> e.slots.contains(shiftClickedSlot)).findFirst().orElseThrow();
            Slot holdMyStackASec = null;
            for (Slot potentialEmptySlot : (List<Slot>) handledScreen.handler.slots) {
                if (shiftClickedSlot.inventory == Minecraft.INSTANCE.player.inventory && SlotHelper.getSlots(handledScreen.handler).stream().filter(e -> e.slots.contains(potentialEmptySlot)).findFirst().orElseThrow() == originalInv && potentialEmptySlot.getStack() == null) {
                    holdMyStackASec = potentialEmptySlot;
                    break;
                }
            }

            if (holdMyStackASec == null) {
                return false;
            }

            Minecraft.INSTANCE.interactionManager.clickSlot(handledScreen.handler.syncId, shiftClickedSlot.id, 0, false, Minecraft.INSTANCE.player);
            Minecraft.INSTANCE.interactionManager.clickSlot(handledScreen.handler.syncId, holdMyStackASec.id, 1, false, Minecraft.INSTANCE.player);
            Minecraft.INSTANCE.interactionManager.clickSlot(handledScreen.handler.syncId, shiftClickedSlot.id, 0, false, Minecraft.INSTANCE.player);
            Minecraft.INSTANCE.interactionManager.clickSlot(handledScreen.handler.syncId, holdMyStackASec.id, 0, false, Minecraft.INSTANCE.player);
            VanillaClickImpl.shiftClickCursorStack(handledScreen.handler, originalInv);
        }
        return true;
    }

    public static boolean handleSingleSpread(Set<Slot> hoveredSlots, ScreenHandler handler) {
        ItemStack cursorStack = Minecraft.INSTANCE.player.inventory.getCursorStack();
        if (cursorStack == null) {
            return false;
        }
        for (Slot slot : hoveredSlots) {
            if (StackUtil.canMerge(slot, cursorStack)) {
                Minecraft.INSTANCE.interactionManager.clickSlot(handler.syncId, slot.id, 1, false, Minecraft.INSTANCE.player);
            }
        }
        return true;
    }
}
