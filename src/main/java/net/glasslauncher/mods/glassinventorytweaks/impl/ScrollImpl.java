package net.glasslauncher.mods.glassinventorytweaks.impl;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screen.ingame.HandledScreen;
import net.minecraft.screen.slot.Slot;
import org.lwjgl.input.Mouse;

import java.util.List;

public class ScrollImpl {

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
            SlotData originalInv = ClickImpl.getSlots(handledScreen).stream().filter(e -> e.slots.contains(shiftClickedSlot)).findFirst().orElseThrow();
            Slot holdMyStackASec = null;
            for (Slot potentialEmptySlot : (List<Slot>) handledScreen.handler.slots) {
                if (shiftClickedSlot.inventory == Minecraft.INSTANCE.player.inventory && ClickImpl.getSlots(handledScreen).stream().filter(e -> e.slots.contains(potentialEmptySlot)).findFirst().orElseThrow() == originalInv && potentialEmptySlot.getStack() == null) {
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
            ClickImpl.shiftClickCursorStack(handledScreen, originalInv);
        }
        return true;
    }
}
