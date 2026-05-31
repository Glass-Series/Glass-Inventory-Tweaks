package net.glasslauncher.mods.glassinventorytweaks.mixin;

import net.glasslauncher.mods.glassinventorytweaks.impl.ClickImpl;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.ingame.HandledScreen;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.screen.slot.Slot;
import org.lwjgl.input.Keyboard;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(HandledScreen.class)
public abstract class HandledScreenMixin extends Screen {

    @Unique boolean mouseDown = false;

    @Shadow
    protected abstract Slot getSlotAt(int x, int y);

    @Shadow
    public ScreenHandler handler;

    @SuppressWarnings("NameDoesntMatchTargetClass")
    @Inject(method = "mouseClicked", at = @At(value = "HEAD"), cancellable = true)
    protected void mouseClicked(int mouseX, int mouseY, int button, CallbackInfo ci) {
        mouseDown = true;
        if (button != 0 || !Keyboard.isKeyDown(Keyboard.KEY_LSHIFT)) {
            return;
        }

        if (ClickImpl.shiftClick(getSlotAt(mouseX, mouseY), (HandledScreen) (Object) this)) {
            ci.cancel();
        }
    }

    @Inject(method = "mouseReleased", at = @At(value = "HEAD"))
    private void mouseReleased(int mouseX, int mouseY, int button, CallbackInfo ci) {
        if (button == 0) {
            mouseDown = false;
        }
    }

    @Inject(method = "render", at = @At("HEAD"))
    private void drag(int mouseX, int mouseY, float delta, CallbackInfo ci) {
        if (mouseDown && Keyboard.isKeyDown(Keyboard.KEY_LSHIFT)) {
            ClickImpl.shiftClick(getSlotAt(mouseX, mouseY), (HandledScreen) (Object) this);
        }
    }
}
