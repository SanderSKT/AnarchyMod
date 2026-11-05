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
        this.addRenderableWidget(Button.builder(Component.literal("Blacklist Manager"),
                button -> this.minecraft.setScreen(new BlacklistScreen(this)))
                .bounds(this.width - 138, 7, 130, 20).build());
    }
}
