package net.glasslauncher.mods.glassinventorytweaks.mixin;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import it.unimi.dsi.fastutil.ints.Int2IntMap;
import it.unimi.dsi.fastutil.ints.Int2IntOpenHashMap;
import net.glasslauncher.mods.glassinventorytweaks.impl.*;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.ingame.HandledScreen;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.screen.slot.Slot;
import org.lwjgl.input.Keyboard;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

import static net.glasslauncher.mods.glassinventorytweaks.impl.ClickType.*;

@Mixin(HandledScreen.class)
public abstract class HandledScreenMixin extends Screen {
    @Unique
    int mouseDown = -1;
    @Unique
    Set<Slot> hoveredSlots = new HashSet<>();
    @Unique
    ItemStack previewStack;
    @Unique
    Slot dragStartSlot;
    @Unique
    boolean skip = false;
    @Unique
    int mouseX;
    @Unique
    int mouseY;

    @Shadow
    protected abstract Slot getSlotAt(int x, int y);

    @Shadow
    public ScreenHandler handler;

    @Inject(method = "keyPressed", at = @At("HEAD"), cancellable = true)
    private void hotKeys(char chr, int keyCode, CallbackInfo ci) {
        if (keyCode < Keyboard.KEY_1 || keyCode > Keyboard.KEY_9) {
            return;
        }

        Slot slot = getSlotAt(mouseX, mouseY);
        if (slot == null) {
            return;
        }

        if (!slot.canInsert(Minecraft.INSTANCE.player.inventory.main[keyCode - 2])) {
            return;
        }

        if (ModdedClickImpl.run(ClickType.HOTBAR, new int[]{keyCode - 2, slot.id}, handler)) {
            ci.cancel();
        }
    }

    @SuppressWarnings("NameDoesntMatchTargetClass")
    @Inject(method = "mouseClicked", at = @At(value = "HEAD"), cancellable = true)
    protected void mouseClicked(int mouseX, int mouseY, int button, CallbackInfo ci) {
        if (skip) {
            return;
        }
        if (mouseDown != -1) {
            ci.cancel(); // Already doing something.
            return;
        }

        Slot slot = getSlotAt(mouseX, mouseY);

        if ((button == 1 || button == 0) && minecraft.player.inventory.getCursorStack() != null) {
            mouseDown = button;
            dragStartSlot = slot;
            ci.cancel(); // TODO: Uhhh, held item shift click is broken fix this shit dipshit
            return;
        }
        if (button != 0 || !Keyboard.isKeyDown(Keyboard.KEY_LSHIFT)) {
            return;
        }
        mouseDown = button;
        if (slot == null) {
            return;
        }

        if (GlassInventoryTweaks.runningWithMod ? ModdedClickImpl.run(SHIFT_CLICK, new int[]{slot.id}, handler) : VanillaClickImpl.shiftClick(slot, handler)) {
            ci.cancel();
        }
    }

    @Inject(method = "mouseReleased", at = @At(value = "HEAD"))
    private void mouseReleased(int mouseX, int mouseY, int button, CallbackInfo ci) {
        if (button == mouseDown) {
            switch (mouseDown) {
                case 0 -> {
                    if (hoveredSlots.isEmpty()) {
                        skip = true;
                        mouseClicked(mouseX, mouseY, button);
                        skip = false;
                    }
                    if (GlassInventoryTweaks.runningWithMod) {
                        ModdedClickImpl.run(EVEN_SPREAD, hoveredSlots.stream().mapToInt(e -> e.id).toArray(), handler);
                    }
                    hoveredSlots.forEach(e -> ((HighlightableSlot) e).setHighlighted(false));
                    hoveredSlots.clear();
                }
                case 1 -> {
                    if (hoveredSlots.isEmpty()) {
                        skip = true;
                        mouseClicked(mouseX, mouseY, button);
                        skip = false;
                    }
                    if (GlassInventoryTweaks.runningWithMod) {
                        ModdedClickImpl.run(SPREAD_ONE, hoveredSlots.stream().mapToInt(e -> e.id).toArray(), handler);
                    } else {
//                        VanillaClickImpl.handleSingleSpread();
                    }
                    hoveredSlots.forEach(e -> ((HighlightableSlot) e).setHighlighted(false));
                    hoveredSlots.clear();
                }
            }

            mouseDown = -1;
            dragStartSlot = null;
        }
    }

    @Inject(method = "render", at = @At("HEAD"))
    private void drag(int mouseX, int mouseY, float delta, CallbackInfo ci) {
        this.mouseX = mouseX;
        this.mouseY = mouseY;

        Slot slot = getSlotAt(mouseX, mouseY);
        if (slot == null) {
            return;
        }
        switch (mouseDown) {
            case 0 -> {
                if (minecraft.player.inventory.getCursorStack() == null && Keyboard.isKeyDown(Keyboard.KEY_LSHIFT)) {
                    if (GlassInventoryTweaks.runningWithMod) {
                        ModdedClickImpl.run(SHIFT_CLICK, new int[]{slot.id}, handler);
                        return;
                    }
                    VanillaClickImpl.shiftClick(slot, handler);
                }
                else if (minecraft.player.inventory.getCursorStack() != null && hoveredSlots.size() < minecraft.player.inventory.getCursorStack().count && slot != dragStartSlot) {
                    if (StackUtil.canMerge(slot, minecraft.player.inventory.getCursorStack())) {
                else if (GlassInventoryTweaks.runningWithMod && minecraft.player.inventory.getCursorStack() != null && hoveredSlots.size() < minecraft.player.inventory.getCursorStack().count && slot != dragStartSlot) {
                        if (dragStartSlot != null) {
                            hoveredSlots.add(dragStartSlot);
                            ((HighlightableSlot) dragStartSlot).setHighlighted(true);
                            dragStartSlot = null;
                        }
                        hoveredSlots.add(slot);
                        ((HighlightableSlot) slot).setHighlighted(true);
                    }
                }
            }
            case 1 -> {
                if (minecraft.player.inventory.getCursorStack() != null && hoveredSlots.size() < minecraft.player.inventory.getCursorStack().count && slot != dragStartSlot) {
                    if (StackUtil.canMerge(slot, minecraft.player.inventory.getCursorStack())) {
                        if (dragStartSlot != null) {
                            hoveredSlots.add(dragStartSlot);
                            ((HighlightableSlot) dragStartSlot).setHighlighted(true);
                            dragStartSlot = null;
                        }
                        hoveredSlots.add(slot);
                        ((HighlightableSlot) slot).setHighlighted(true);
                    }
                }
            }
        }
    }

    @WrapOperation(method = "drawSlot", at = @At(value = "INVOKE", target = "Lnet/minecraft/screen/slot/Slot;getStack()Lnet/minecraft/item/ItemStack;"))
    private ItemStack drawDragPreview(Slot instance, Operation<ItemStack> original) {
        if (!hoveredSlots.contains(instance)) {
            return original.call(instance);
        }

        if (!instance.hasStack()) {
            return previewStack;
        }
        ItemStack stack = instance.getStack().copy();
        stack.count += previewStack.count;
        return stack;
    }

    @WrapOperation(method = "render", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/screen/ingame/HandledScreen;isPointOverSlot(Lnet/minecraft/screen/slot/Slot;II)Z"))
    private boolean drawHighlight(HandledScreen instance, Slot slot, int x, int y, Operation<Boolean> original) {
        if (hoveredSlots.contains(slot)) {
            return true;
        }
        return original.call(instance, slot, x, y);
    }

    @Inject(method = "render", at = @At("HEAD"))
    private void makePreviewStack(int mouseX, int mouseY, float delta, CallbackInfo ci) {
        previewStack = null;
        if (hoveredSlots.isEmpty()) {
            return;
        }

        ItemStack cursorStack = minecraft.player.inventory.getCursorStack();
        if (cursorStack == null) {
            return;
        }
        int count;
        switch (mouseDown) {
            case 0:
                count = (int) Math.floor(cursorStack.count / (float) hoveredSlots.size());
                break;
            case 1:
                count = 1;
                break;
            default:
                return;
        }

        previewStack = cursorStack.copy();
        previewStack.count = count;
    }

    @Inject(method = "render", at = @At("RETURN"))
    private void wipePreviewStack(int mouseX, int mouseY, float delta, CallbackInfo ci) {
        previewStack = null;
    }
}
