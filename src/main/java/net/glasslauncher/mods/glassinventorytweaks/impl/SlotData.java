package net.glasslauncher.mods.glassinventorytweaks.impl;


import net.minecraft.inventory.Inventory;
import net.minecraft.screen.slot.Slot;

import java.util.ArrayList;
import java.util.List;

public class SlotData {
    public final Inventory inventory;
    public final int inventoryIndex;
    public final int priority;
    public final List<Slot> slots = new ArrayList<>();

    public SlotData(Inventory inventory, int inventoryIndex, int priority) {
        this.inventory = inventory;
        this.inventoryIndex = inventoryIndex;
        this.priority = priority;
    }
}
