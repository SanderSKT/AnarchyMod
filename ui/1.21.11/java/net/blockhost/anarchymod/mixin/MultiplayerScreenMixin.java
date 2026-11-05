package net.blockhost.anarchymod.mixin;

import net.blockhost.anarchymod.gui.BlacklistScreen;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.multiplayer.JoinMultiplayerScreen;
import net.minecraft.network.chat.Component;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/** Button is added only after the vanilla Multiplayer screen creates its widgets. */
@Mixin(JoinMultiplayerScreen.class)
public abstract class MultiplayerScreenMixin extends Screen {
    protected MultiplayerScreenMixin(Component title) {
        super(title);
    }

    @Inject(method = "init", at = @At("TAIL"))
    private void anarchymod$addBlacklistButton(CallbackInfo ci) {
        // Vanilla controls occupy the bottom-center of this screen. The top
        // bar is used by ViaFabricPlus and other UI mods, so stay beside the
        // second vanilla button row, to its right, within the screen bounds.
        int buttonWidth = 132;
        int buttonX = Math.max(8, Math.min(this.width - buttonWidth - 8,
                this.width / 2 + 170));
        int buttonY = Math.max(8, this.height - 27);
        this.addRenderableWidget(Button.builder(Component.literal("Blacklist Manager"),
                button -> this.minecraft.setScreen(new BlacklistScreen(this)))
                .bounds(buttonX, buttonY, buttonWidth, 20).build());
    }
}
