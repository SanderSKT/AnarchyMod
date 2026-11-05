package net.blockhost.anarchymod.mixin;

import net.blockhost.anarchymod.gui.BlacklistScreen;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.multiplayer.JoinMultiplayerScreen;
import net.minecraft.network.chat.Component;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/** The button follows the actual screen size, even if another UI mod reflows widgets. */
@Mixin(JoinMultiplayerScreen.class)
public abstract class MultiplayerScreenMixin extends Screen {
    @Unique
    private Button anarchymod$blacklistButton;

    protected MultiplayerScreenMixin(Component title) {
        super(title);
    }

    @Inject(method = "init", at = @At("TAIL"))
    private void anarchymod$addBlacklistButton(CallbackInfo ci) {
        this.anarchymod$blacklistButton = this.addRenderableWidget(
                Button.builder(Component.literal("Blacklist Manager"),
                        button -> this.minecraft.setScreen(new BlacklistScreen(this)))
                        .bounds(0, 0, 132, 20).build());
        this.anarchymod$positionButton();
    }

    @Inject(method = "render", at = @At("HEAD"))
    private void anarchymod$updateButtonPosition(GuiGraphics graphics, int mouseX, int mouseY,
                                                float delta, CallbackInfo ci) {
        this.anarchymod$positionButton();
    }

    @Unique
    private void anarchymod$positionButton() {
        if (this.anarchymod$blacklistButton == null) {
            return;
        }
        // Always place it beside, not on top of, the lower vanilla controls.
        // Recalculate on every render: other mods may resize/reflow the screen
        // after init(), so positioning only there can leave a floating button.
        int buttonWidth = 132;
        int desiredX = this.width / 2 + 170;
        int x = Math.max(8, Math.min(this.width - buttonWidth - 8, desiredX));
        int y = Math.max(8, this.height - 27);
        this.anarchymod$blacklistButton.setPosition(x, y);
    }
}
