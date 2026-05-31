package net.glasslauncher.mods.glassinventorytweaks.mixin;

import net.glasslauncher.mods.glassinventorytweaks.impl.HighlightableSlot;
import net.minecraft.screen.slot.Slot;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;

@Mixin(Slot.class)
public class SlotMixin implements HighlightableSlot {
    @Unique
    private boolean highlighted = false;

    @Override
    public boolean isHighlighted() {
        return highlighted;
    }

    @Override
    public void setHighlighted(boolean highlighted) {
        this.highlighted = highlighted;
    }
}
