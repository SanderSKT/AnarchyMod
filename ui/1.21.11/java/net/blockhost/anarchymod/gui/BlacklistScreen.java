package net.blockhost.anarchymod.gui;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

import java.util.List;

/** Simple paginated editor with immediate persistence and no game restart. */
public final class BlacklistScreen extends Screen {
    private final Screen parent;
    private final boolean subdomains;
    private final int requestedPage;
    private final String status;
    private final String draft;
    private EditBox address;
    private int page;
    private int pageSize;
    private int panelLeft;
    private int panelTop;
    private int panelWidth;
    private int panelHeight;
    private List<String> visibleEntries;

    public BlacklistScreen(Screen parent) {
        this(parent, true, 0, CustomDomains.lastError(), "");
    }

    private BlacklistScreen(Screen parent, boolean subdomains, int page, String status, String draft) {
        super(Component.literal("AnarchyMod - Blacklist Manager"));
        this.parent = parent;
        this.subdomains = subdomains;
        this.requestedPage = page;
        this.status = status;
        this.draft = draft;
    }

    @Override
    protected void init() {
        this.panelWidth = Math.min(324, this.width - 16);
        this.panelHeight = Math.min(296, this.height - 16);
        this.panelLeft = (this.width - panelWidth) / 2;
        this.panelTop = (this.height - panelHeight) / 2;
        int x = panelLeft + 16;
        int contentWidth = panelWidth - 32;
        this.address = this.addRenderableWidget(new EditBox(
                this.font, x, panelTop + 60, contentWidth - 84, 20, Component.literal("Server address")));
        this.address.setMaxLength(253);
        this.address.setHint(Component.literal("example.org or *.example.org"));
        this.address.setValue(this.draft);
        this.addRenderableWidget(Button.builder(Component.literal("Add"), b -> addAddress())
                .bounds(x + contentWidth - 76, panelTop + 60, 76, 20).build());
        this.addRenderableWidget(Button.builder(
                Component.literal("Include subdomains: " + (subdomains ? "ON" : "OFF")),
                b -> show(!subdomains, this.requestedPage, "", this.address.getValue()))
                .bounds(x, panelTop + 88, Math.min(190, contentWidth), 20).build());

        this.visibleEntries = CustomDomains.list();
        this.pageSize = Math.max(1, Math.min(5, (panelHeight - 224) / 24));
        int pages = Math.max(1, (visibleEntries.size() + pageSize - 1) / pageSize);
        this.page = Math.max(0, Math.min(requestedPage, pages - 1));
        int start = page * pageSize;
        for (int i = 0; i < pageSize && start + i < visibleEntries.size(); i++) {
            final String entry = visibleEntries.get(start + i);
            this.addRenderableWidget(Button.builder(Component.literal("Remove"),
                    b -> show(subdomains, page, CustomDomains.remove(entry), this.address.getValue()))
                    .bounds(x + contentWidth - 70, panelTop + 144 + 24 * i, 70, 20).build());
        }
        Button previous = this.addRenderableWidget(Button.builder(Component.literal("< Prev"),
                b -> show(subdomains, page - 1, status, this.address.getValue()))
                .bounds(x, panelTop + panelHeight - 55, 70, 20).build());
        previous.active = page > 0;
        Button next = this.addRenderableWidget(Button.builder(Component.literal("Next >"),
                b -> show(subdomains, page + 1, status, this.address.getValue()))
                .bounds(x + contentWidth - 70, panelTop + panelHeight - 55, 70, 20).build());
        next.active = page + 1 < pages;
        this.addRenderableWidget(Button.builder(Component.literal("Done"), b -> onClose())
                .bounds(this.width / 2 - 75, panelTop + panelHeight - 28, 150, 20).build());
    }

    private void addAddress() {
        String value = this.address.getValue().trim();
        if (subdomains && !value.startsWith("*.")) {
            value = "*." + value;
        }
        String result = CustomDomains.add(value);
        show(subdomains, page, result, "");
    }

    private void show(boolean subdomains, int page, String status, String draft) {
        if (this.minecraft != null) {
            this.minecraft.setScreen(new BlacklistScreen(parent, subdomains, page, status, draft));
        }
    }

    @Override
    public void onClose() {
        if (this.minecraft != null) {
            this.minecraft.setScreen(this.parent);
        }
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float delta) {
        // Do NOT invoke Screen.renderBackground here: the normal screen render
        // pipeline (and other mods) can already apply the blur pass once.
        // A solid overlay needs no extra blur and avoids IllegalStateException:
        // "Can only blur once per frame".
        graphics.fill(0, 0, this.width, this.height, 0xF0121821);
        int right = panelLeft + panelWidth;
        int bottom = panelTop + panelHeight;
        graphics.fill(panelLeft, panelTop, right, bottom, 0xFF1C232E);
        graphics.fill(panelLeft, panelTop, right, panelTop + 1, 0xFF51627A);
        graphics.fill(panelLeft, bottom - 1, right, bottom, 0xFF51627A);
        super.render(graphics, mouseX, mouseY, delta);
        int x = panelLeft + 16;
        graphics.drawCenteredString(this.font, this.title, this.width / 2,
                panelTop + 12, 0xFFFFFFFF);
        graphics.drawString(this.font, "Server hostname (no http://):",
                x, panelTop + 45, 0xFFBBBBBB);
        graphics.drawString(this.font, "Custom exceptions (" + visibleEntries.size() + "):",
                x, panelTop + 125, 0xFFAAAAAA);
        graphics.fill(x, panelTop + 137, panelLeft + panelWidth - 16,
                panelTop + 138, 0xFF394758);

        for (int i = 0; i < pageSize && page * pageSize + i < visibleEntries.size(); i++) {
            String entry = visibleEntries.get(page * pageSize + i);
            String caption = entry.length() > 31 ? entry.substring(0, 28) + "..." : entry;
            graphics.drawString(this.font, caption, x + 4, panelTop + 150 + 24 * i, 0xFFE5E5E5);
        }
        if (visibleEntries.isEmpty()) {
            graphics.drawString(this.font, "No custom addresses yet.",
                    x + 4, panelTop + 150, 0xFFAAAAAA);
            graphics.drawString(this.font, "Built-in exceptions remain active.",
                    x + 4, panelTop + 164, 0xFFAAAAAA);
        }
        if (!status.isBlank()) {
            graphics.drawString(this.font, status.length() > 56 ? status.substring(0, 53) + "..." : status,
                    x, panelTop + panelHeight - 70, 0xFFFFD070);
        }
        int pages = Math.max(1, (visibleEntries.size() + pageSize - 1) / pageSize);
        graphics.drawCenteredString(this.font, "Page " + (page + 1) + "/" + pages,
                this.width / 2, panelTop + panelHeight - 49, 0xFFAAAAAA);
    }
}
