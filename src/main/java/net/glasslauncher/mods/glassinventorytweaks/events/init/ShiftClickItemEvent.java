package net.glasslauncher.mods.glassinventorytweaks.events.init;

import lombok.RequiredArgsConstructor;
import net.mine_diver.unsafeevents.Event;
import net.minecraft.client.gui.screen.ingame.HandledScreen;
import net.minecraft.screen.slot.Slot;

/**
 * Called when an item is shift-clicked in the inventory.
 * If the slot's item stack is null, the default shift+click handling is canceled.
 * NOTE: THE SLOT CONTENTS MAY ALREADY BE NULL FROM ANOTHER EVENT!
 */
@RequiredArgsConstructor
public class ShiftClickItemEvent<T extends HandledScreen> extends Event {
    public final T screenHandler;
    public final Slot slot;
}
