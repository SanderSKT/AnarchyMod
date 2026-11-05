package net.blockhost.anarchymod.mixin;

import net.blockhost.anarchymod.gui.BlacklistScreen;
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
                Button.builder(Component.literal("Blacklist"),
                        button -> this.minecraft.setScreen(new BlacklistScreen(this)))
                        .bounds(0, 0, 76, 20).build());
        this.anarchymod$positionButton();
    }

    @Inject(method = "repositionElements", at = @At("TAIL"))
    private void anarchymod$afterLayout(CallbackInfo ci) {
        this.anarchymod$positionButton();
    }

    @Inject(method = "tick", at = @At("TAIL"))
    private void anarchymod$keepButtonAnchored(CallbackInfo ci) {
        this.anarchymod$positionButton();
    }

    @Unique
    private void anarchymod$positionButton() {
        if (this.anarchymod$blacklistButton == null) {
            return;
        }
        int desiredY = this.height - 27;
        int vanillaLeft = Integer.MAX_VALUE;
        // Find Edit/Delete/Refresh in the bottom vanilla row. This also adapts
        // to layouts that have been moved by other client-side mods.
        for (var child : this.children()) {
            if (child instanceof Button button && button != this.anarchymod$blacklistButton
                    && Math.abs(button.getY() - desiredY) <= 4
                    && button.getX() >= this.width / 2 - 180
                    && button.getX() < this.width / 2 - 75) {
                vanillaLeft = Math.min(vanillaLeft, button.getX());
            }
        }
        if (vanillaLeft == Integer.MAX_VALUE) {
            vanillaLeft = this.width / 2 - 154;
        }
        final int buttonWidth = 76;
        int x = vanillaLeft - buttonWidth - 4;
        int y = desiredY;
        if (x < 8) {
            // Narrow screens: move above the vanilla controls rather than
            // obstructing their hitboxes.
            x = Math.max(8, (this.width - buttonWidth) / 2);
            y = Math.max(8, this.height - 79);
        }
        this.anarchymod$blacklistButton.setPosition(x, Math.max(8, y));
    }
}
