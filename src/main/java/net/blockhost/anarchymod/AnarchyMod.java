package net.blockhost.anarchymod;

import net.fabricmc.api.ClientModInitializer;

/** No startup side effects: server checks are handled by BlockedServersMixin. */
public class AnarchyMod implements ClientModInitializer {
    @Override
    public void onInitializeClient() {
        //? if 1.21.11 {
        net.blockhost.anarchymod.gui.CustomDomains.load();
        //? }
        // No remote requests, analytics or server-list modification.
    }
}
