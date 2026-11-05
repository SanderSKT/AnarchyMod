package net.blockhost.anarchymod.gui;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import net.blockhost.anarchymod.Domains;
import net.fabricmc.loader.api.FabricLoader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.Reader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

/** Client-only config. No HTTP requests, network telemetry, or scheduled refresh. */
public final class CustomDomains {
    private static final Logger LOGGER = LoggerFactory.getLogger("AnarchyMod");
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static final Path CONFIG = FabricLoader.getInstance().getConfigDir().resolve("anarchymod.json");
    private static final int MAX_ENTRIES = 256;
    private static final Set<String> entries = new LinkedHashSet<>();
    private static String error = "";

    private CustomDomains() {
    }

    public static synchronized void load() {
        entries.clear();
        error = "";
        if (Files.exists(CONFIG)) {
            try {
                if (Files.size(CONFIG) > 65536) {
                    throw new IOException("Config exceeds 64 KiB");
                }
                try (Reader reader = Files.newBufferedReader(CONFIG, StandardCharsets.UTF_8)) {
                    JsonElement root = JsonParser.parseReader(reader);
                    if (!root.isJsonObject()) {
                        throw new IOException("Expected a JSON object");
                    }
                    JsonObject object = root.getAsJsonObject();
                    JsonElement list = object.get("customDomains");
                    if (list != null) {
                        if (!list.isJsonArray()) {
                            throw new IOException("customDomains must be a JSON array");
                        }
                        JsonArray arr = list.getAsJsonArray();
                        if (arr.size() > MAX_ENTRIES) {
                            throw new IOException("Too many custom domains");
                        }
                        for (JsonElement element : arr) {
                            if (!element.isJsonPrimitive() || !element.getAsJsonPrimitive().isString()) {
                                throw new IOException("Domain entries must be strings");
                            }
                            String domain = Domains.canonicalPattern(element.getAsString());
                            if (domain != null && !Domains.isBuiltIn(domain)) {
                                entries.add(domain);
                            }
                        }
                    }
                }
            } catch (IOException | RuntimeException ex) {
                // Do not rewrite a malformed config; user can recover it by hand.
                entries.clear();
                error = "Could not read config; check logs.";
                LOGGER.error("Failed reading {}", CONFIG, ex);
            }
        }
        publish();
    }

    public static synchronized List<String> list() {
        List<String> sorted = new ArrayList<>(entries);
        Collections.sort(sorted);
        return sorted;
    }

    public static synchronized String lastError() {
        return error;
    }

    public static synchronized String add(String raw) {
        String domain = Domains.canonicalPattern(raw);
        if (domain == null) {
            return "Invalid address. Example: example.org or *.example.org";
        }
        if (Domains.isBuiltIn(domain)) {
            return "This server is already in the built-in list.";
        }
        if (entries.contains(domain)) {
            return "Already added.";
        }
        if (entries.size() >= MAX_ENTRIES) {
            return "Limit reached (256 custom addresses).";
        }

        entries.add(domain);
        try {
            save();
            publish();
            return "Added: " + domain;
        } catch (IOException ex) {
            entries.remove(domain);
            LOGGER.error("Could not save {}", CONFIG, ex);
            return "Could not save config; check logs.";
        }
    }

    public static synchronized String remove(String raw) {
        if (!entries.remove(raw)) {
            return "Address not found.";
        }
        try {
            save();
            publish();
            return "Removed: " + raw;
        } catch (IOException ex) {
            entries.add(raw);
            LOGGER.error("Could not save {}", CONFIG, ex);
            return "Could not save config; check logs.";
        }
    }

    private static void publish() {
        Domains.setCustomDomains(entries);
    }

    private static void save() throws IOException {
        Files.createDirectories(CONFIG.getParent());
        JsonObject config = new JsonObject();
        JsonArray domains = new JsonArray();
        for (String domain : list()) {
            domains.add(domain);
        }
        config.add("customDomains", domains);

        Path temp = CONFIG.resolveSibling(CONFIG.getFileName() + ".tmp");
        Files.writeString(temp, GSON.toJson(config) + System.lineSeparator(), StandardCharsets.UTF_8);
        try {
            Files.move(temp, CONFIG, StandardCopyOption.ATOMIC_MOVE, StandardCopyOption.REPLACE_EXISTING);
        } catch (java.nio.file.AtomicMoveNotSupportedException ex) {
            Files.move(temp, CONFIG, StandardCopyOption.REPLACE_EXISTING);
        }
    }
}
