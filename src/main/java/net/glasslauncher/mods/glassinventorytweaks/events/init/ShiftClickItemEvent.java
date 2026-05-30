package net.glasslauncher.mods.glassinventorytweaks.events.init;

import lombok.RequiredArgsConstructor;
import net.mine_diver.unsafeevents.Event;
import net.minecraft.client.gui.screen.ingame.HandledScreen;
import net.minecraft.screen.slot.Slot;

@RequiredArgsConstructor
public class ShiftClickItemEvent<T extends HandledScreen> extends Event {
    public final T screenHandler;
    public final Slot slot;
}
