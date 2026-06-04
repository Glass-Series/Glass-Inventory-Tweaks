package net.glasslauncher.mods.glassinventorytweaks.impl;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import net.glasslauncher.mods.glassinventorytweaks.api.ShiftClickTreatSeparately;
import net.minecraft.inventory.Inventory;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.screen.slot.Slot;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public class SlotHelper {
    // Important when doing batshit huge move operations.
    private static final Cache<ScreenHandler, List<SlotData>> SLOT_CACHE = Caffeine.newBuilder().weakKeys().build();

    public static List<SlotData> getSlots(ScreenHandler handler) {
        return SLOT_CACHE.get(handler, SlotHelper::_getSlots);
    }

    private static List<SlotData> _getSlots(ScreenHandler handler) {
        List<SlotData> slotGroups = new ArrayList<>();
        int inventoryDataIndex = 0;
        int nextSplitPointIndex = 0;
        Inventory lastInv = null;

        //noinspection unchecked
        List<Slot> allSlots = handler.slots;

        ShiftClickTreatSeparately annotation = handler.getClass().getAnnotation(ShiftClickTreatSeparately.class);
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
