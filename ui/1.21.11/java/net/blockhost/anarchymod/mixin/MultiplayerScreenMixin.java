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
                        .bounds(0, 0, 68, 20).build());
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
        // A compact 68px button fits to the left of the vanilla row even
        // at the narrower GUI scale shown in the user's screenshot.
        // Never move into the server list: keep it anchored in the footer.
        final int buttonWidth = 68;
        int x = Math.max(4, vanillaLeft - buttonWidth - 4);
        this.anarchymod$blacklistButton.setPosition(x, Math.max(8, desiredY));
    }
}
