package net.glasslauncher.mods.glassinventorytweaks.mixin;

import net.glasslauncher.mods.glassinventorytweaks.impl.VanillaClickImpl;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.ingame.HandledScreen;
import org.lwjgl.input.Mouse;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Screen.class)
public class ScreenMixin {

    @Inject(method = "onMouseEvent", at = @At(value = "HEAD", target = "Lnet/minecraft/client/gui/screen/Screen;onMouseEvent()V"), cancellable = true)
    private void test(CallbackInfo ci) {
        if ((Object) this instanceof HandledScreen handledScreen) {
            float wheel = Mouse.getDWheel();
            if (wheel != 0 && VanillaClickImpl.handleScroll(handledScreen, wheel)) {
                ci.cancel();
            }
        }
    }
}
