package net.glasslauncher.mods.glassinventorytweaks.mixin;

import net.glasslauncher.mods.glassinventorytweaks.impl.ClickImpl;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.ingame.HandledScreen;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.screen.slot.Slot;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(HandledScreen.class)
public abstract class HandledScreenMixin extends Screen {

    @Shadow
    protected abstract Slot getSlotAt(int x, int y);

    @Shadow
    public ScreenHandler handler;

    @SuppressWarnings("NameDoesntMatchTargetClass")
    @Inject(method = "mouseClicked", at = @At(value = "HEAD", target = "Lnet/minecraft/screen/ScreenHandler;quickMove(I)Lnet/minecraft/item/ItemStack;"), cancellable = true)
    protected void mouseClicked(int mouseX, int mouseY, int button, CallbackInfo ci) {
        if (ClickImpl.click(mouseX, mouseY, button, (HandledScreen) (Object) this)) {
            ci.cancel();
        }
    }
}
