package edu.whimc.observations.utils;

import edu.whimc.observations.Observations;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public final class ObservationContentValidator {

    public enum Failure {
        EMPTY,
        PROFANITY,
        NONSENSE
    }

    public static final class Result {
        private final Failure failure;

        private Result(Failure failure) {
            this.failure = failure;
        }

        public boolean isAccepted() {
            return failure == null;
        }

        public Failure getFailure() {
            return failure;
        }
    }

    /** More than 5 identical characters in a row (6+). */
    private static final Pattern REPEATED_CHAR = Pattern.compile("(.)\\1{5,}");
    private static final Pattern WORD_BOUNDARY = Pattern.compile("\\b([a-z0-9']+)\\b");
    private static final Set<String> BLOCKED_WORDS = loadBlockedWords();
    private static final Map<String, Pattern> SEPARATED_WORD_PATTERNS = buildSeparatedPatterns();

    private ObservationContentValidator() {
    }

    public static Result validate(String raw) {
        String text = ChatColor.stripColor(raw == null ? "" : raw).trim();
        if (text.isEmpty()) {
            return new Result(Failure.EMPTY);
        }

        // Check spam on the real text before any repeat-collapsing for word matching.
        if (isNonsense(text)) {
            return new Result(Failure.NONSENSE);
        }

        if (containsBlockedWord(text)) {
            return new Result(Failure.PROFANITY);
        }
        return new Result(null);
    }

    public static void sendRejection(Player player, Result result) {
        if (result.getFailure() == Failure.PROFANITY) {
            Utils.msg(player,
                    "&cThat observation contains language that isn't allowed on this server.",
                    "&7Please rephrase your observation and try again.");
            return;
        }
        if (result.getFailure() == Failure.NONSENSE) {
            Utils.msg(player,
                    "&cThat doesn't look like a real observation.",
                    "&7Please write a meaningful sentence and try again.");
            return;
        }
        Utils.msg(player, "&cYour observation can't be empty.");
    }

    public static void notifyOps(Observations plugin, Player player, String attemptedText) {
        String censored = censor(attemptedText);
        String alert = "&c[Observations] &f" + player.getName()
                + " &7tried to submit a blocked observation: &8\"" + censored + "&8\"";
        String plain = ChatColor.stripColor(Utils.color(alert));

        plugin.getLogger().warning(plain);
        for (Player online : Bukkit.getOnlinePlayers()) {
            if (online.isOp()) {
                Utils.msg(online, alert);
            }
        }
    }

    private static boolean containsBlockedWord(String text) {
        // @ is used both as 'a' (l@ser) and 'u' (f@ck); check both readings.
        for (String normalized : normalizeVariants(text)) {
            if (matchesBlockedOnNormalized(normalized)) {
                return true;
            }
        }
        return false;
    }

    private static boolean matchesBlockedOnNormalized(String normalized) {
        Matcher matcher = WORD_BOUNDARY.matcher(normalized);
        while (matcher.find()) {
            if (BLOCKED_WORDS.contains(matcher.group(1))) {
                return true;
            }
        }

        // Compact letter stream catches words glued to filler (xxfuckxx) for longer terms.
        String compact = normalized.replaceAll("[^a-z]", "");
        for (String blocked : BLOCKED_WORDS) {
            if (blocked.length() >= 5 && compact.contains(blocked)) {
                return true;
            }
        }

        // f.u.c.k / f u c k / f_u_c_k — requires real separators so "hello" ≠ "hell".
        for (Pattern separated : SEPARATED_WORD_PATTERNS.values()) {
            if (separated.matcher(normalized).find()) {
                return true;
            }
        }
        return false;
    }

    private static boolean isNonsense(String text) {
        String lower = text.toLowerCase();
        if (REPEATED_CHAR.matcher(lower).find()) {
            return true;
        }

        // Truncate long runs to 5 so diversity checks still see key-hold spam remnants
        // without letting collapse-to-one hide the problem.
        String truncated = truncateRepeatingCharacters(lower, 5);
        String lettersOnly = truncated.replaceAll("[^a-z]", "");
        if (lettersOnly.length() < 8) {
            return false;
        }

        long unique = lettersOnly.chars().distinct().count();
        if (unique <= 2) {
            return true;
        }

        return (double) unique / lettersOnly.length() < 0.2;
    }

    /** Caps consecutive identical characters at {@code max}. Longer runs are truncated. */
    private static String truncateRepeatingCharacters(String text, int max) {
        if (text == null || text.isEmpty() || max < 1) {
            return text == null ? "" : text;
        }
        StringBuilder out = new StringBuilder(text.length());
        char prev = 0;
        int run = 0;
        for (int i = 0; i < text.length(); i++) {
            char c = text.charAt(i);
            if (i > 0 && c == prev) {
                run++;
                if (run > max) {
                    continue;
                }
            } else {
                prev = c;
                run = 1;
            }
            out.append(c);
        }
        return out.toString();
    }

    private static List<String> normalizeVariants(String text) {
        List<String> variants = new ArrayList<>(2);
        variants.add(normalize(text, 'a'));
        String asU = normalize(text, 'u');
        if (!asU.equals(variants.get(0))) {
            variants.add(asU);
        }
        return variants;
    }

    private static String normalize(String text, char atReplacement) {
        String normalized = text.toLowerCase();
        normalized = normalized.replace('@', atReplacement);
        normalized = normalized.replace('4', 'a');
        normalized = normalized.replace('3', 'e');
        normalized = normalized.replace('1', 'i').replace('!', 'i');
        normalized = normalized.replace('0', 'o');
        normalized = normalized.replace('$', 's').replace('5', 's');
        normalized = normalized.replace('7', 't').replace('+', 't');
        normalized = normalized.replace('9', 'g').replace('8', 'b');
        normalized = normalized.replace("ph", "f");
        // Turn separators into spaces so word boundaries still work; keep letters/digits.
        normalized = normalized.replaceAll("[^a-z0-9'\\s]", " ");
        // Collapse stretched letters for matching (fuuuuck → fuck).
        normalized = normalized.replaceAll("(.)\\1{2,}", "$1");
        normalized = normalized.replaceAll("\\s+", " ").trim();
        return normalized;
    }

    private static String censor(String text) {
        String stripped = ChatColor.stripColor(text);
        if (stripped.length() <= 3) {
            return "***";
        }
        return stripped.substring(0, 1) + "***" + stripped.substring(stripped.length() - 1);
    }

    private static Map<String, Pattern> buildSeparatedPatterns() {
        Map<String, Pattern> patterns = new HashMap<>();
        for (String word : BLOCKED_WORDS) {
            if (word.length() < 3) {
                continue;
            }
            StringBuilder regex = new StringBuilder();
            for (int i = 0; i < word.length(); i++) {
                if (i > 0) {
                    // At least one non-alphanumeric between letters (spaces, punctuation, symbols).
                    regex.append("[^a-z0-9]+");
                }
                regex.append(Pattern.quote(String.valueOf(word.charAt(i))));
            }
            patterns.put(word, Pattern.compile(regex.toString()));
        }
        return patterns;
    }

    private static Set<String> loadBlockedWords() {
        Set<String> words = new HashSet<>();
        List<String> lines = new ArrayList<>();

        try (InputStream stream = ObservationContentValidator.class.getClassLoader()
                .getResourceAsStream("blocked-words.txt")) {
            if (stream != null) {
                try (BufferedReader reader = new BufferedReader(
                        new InputStreamReader(stream, StandardCharsets.UTF_8))) {
                    String line;
                    while ((line = reader.readLine()) != null) {
                        lines.add(line);
                    }
                }
            }
        } catch (Exception e) {
            Bukkit.getLogger().warning("[Observations] Could not load blocked-words.txt");
        }

        for (String line : lines) {
            String trimmed = line.trim().toLowerCase();
            if (trimmed.isEmpty() || trimmed.startsWith("#")) {
                continue;
            }
            words.add(trimmed);
        }
        return words;
    }
}
