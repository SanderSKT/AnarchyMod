package net.blockhost.anarchymod;

import java.net.IDN;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Locale;
import java.util.Set;

/**
 * Offline-only local allowlist for the Mojang blocked-server check.
 * No remote domain fetch, telemetry, or disk/network I/O.
 */
public final class Domains {
    // Keep existing server compatibility while adding 9b9t. A '*.' entry also
    // matches the base host itself (see matchesNormalized below).
    private static final Set<String> DOMAINS = Collections.unmodifiableSet(new HashSet<>(Arrays.asList(
        "*.9b9t.com", "*.9b9t.org", "*.2b2t.com",
        "*.6b6t.org", "*.10b10t.org", "*.6b6t.cc", "*.6b6t.me",
        "*.7b7t.me", "*.8b8t.org", "*.8b8t.xyz", "*.alacity.net",
        "*.anarchypvp.pw", "*.l2x9.org", "*.simpleanarchy.org"
    )));

    private static volatile Set<String> customDomains = Collections.emptySet();

    private Domains() {
    }

    public static boolean contains(String input) {
        String domain = normalizeHost(input);
        if (domain == null) {
            return false;
        }

        for (String entry : DOMAINS) {
            if (matchesNormalized(domain, entry)) {
                return true;
            }
        }

        for (String entry : customDomains) {
            if (matchesNormalized(domain, entry)) {
                return true;
            }
        }

        return false;
    }

    public static boolean matches(String input, String pattern) {
        String domain = normalizeHost(input);
        String entry = normalizeEntry(pattern);
        return domain != null && entry != null && matchesNormalized(domain, entry);
    }

    private static boolean matchesNormalized(String domain, String entry) {
        if (!entry.startsWith("*.")) {
            return domain.equals(entry);
        }

        String base = entry.substring(2);
        return domain.equals(base) || domain.endsWith("." + base);
    }

    /**
     * Replaces all user-added exceptions atomically. Does not alter built-ins.
     * A volatile immutable snapshot keeps checks safe while a screen edits the list.
     */
    public static void setCustomDomains(java.util.Collection<String> patterns) {
        Set<String> cleaned = new HashSet<>();
        for (String raw : patterns) {
            String value = canonicalPattern(raw);
            if (value != null && !DOMAINS.contains(value)) {
                cleaned.add(value);
            }
        }
        customDomains = Collections.unmodifiableSet(cleaned);
    }

    public static boolean isBuiltIn(String canonicalPattern) {
        return DOMAINS.contains(canonicalPattern);
    }

    /** Canonicalized domain or *.domain; never accepts URLs or invalid ports. */
    public static String canonicalPattern(String input) {
        String canonical = normalizeEntry(input);
        if (canonical == null) {
            return null;
        }
        String host = canonical.startsWith("*.") ? canonical.substring(2) : canonical;
        // The client blacklist is hostname-based; a domain needs at least one dot.
        if (!host.contains(".") || host.contains(":")) {
            return null;
        }
        return canonical;
    }

    private static String normalizeEntry(String input) {
        if (input == null) {
            return null;
        }

        String trimmed = input.trim();
        boolean wildcard = trimmed.startsWith("*.");
        String host = normalizeHost(wildcard ? trimmed.substring(2) : trimmed);
        return host == null ? null : (wildcard ? "*." : "") + host;
    }

    static String normalizeHost(String input) {
        if (input == null) {
            return null;
        }

        String host = input.trim();
        if (host.startsWith("*.")) {
            host = host.substring(2);
        }
        if (host.isEmpty()) {
            return null;
        }

        if (host.startsWith("[")) {
            int closingBracket = host.indexOf(']');
            if (closingBracket < 0 || !isValidPortSuffix(host.substring(closingBracket + 1))) {
                return null;
            }
            host = host.substring(1, closingBracket);
        } else {
            int firstColon = host.indexOf(':');
            int lastColon = host.lastIndexOf(':');
            if (firstColon >= 0 && firstColon == lastColon) {
                if (!isValidPortSuffix(host.substring(firstColon))) {
                    return null;
                }
                host = host.substring(0, firstColon);
            }
        }

        while (host.endsWith(".")) {
            host = host.substring(0, host.length() - 1);
        }
        if (host.isEmpty()) {
            return null;
        }

        try {
            // IPv6 literals contain colons and are not valid IDNs, but preserving them is safe.
            if (host.indexOf(':') >= 0) {
                return host.toLowerCase(Locale.ROOT);
            }
            String ascii = IDN.toASCII(host, IDN.USE_STD3_ASCII_RULES).toLowerCase(Locale.ROOT);
            return ascii.isEmpty() || ascii.length() > 253 ? null : ascii;
        } catch (IllegalArgumentException ignored) {
            return null;
        }
    }

    private static boolean isValidPortSuffix(String suffix) {
        if (suffix.isEmpty()) {
            return true;
        }
        if (suffix.charAt(0) != ':' || suffix.length() == 1) {
            return false;
        }

        try {
            int port = Integer.parseInt(suffix.substring(1));
            return port >= 1 && port <= 65_535;
        } catch (NumberFormatException ignored) {
            return false;
        }
    }
}
