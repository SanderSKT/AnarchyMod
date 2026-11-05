package net.blockhost.anarchymod;

import net.fabricmc.api.ClientModInitializer;

/** No startup side effects: server checks are handled by BlockedServersMixin. */
public class AnarchyMod implements ClientModInitializer {
    @Override
    public void onInitializeClient() {
        // Deliberately empty: no server-list edits, analytics, or remote requests.
    }
}
