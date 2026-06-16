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
import java.util.HashSet;
import java.util.List;
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

    private static final Pattern REPEATED_CHAR = Pattern.compile("(.)\\1{7,}");
    private static final Pattern WORD_BOUNDARY = Pattern.compile("\\b([a-z0-9']+)\\b");
    private static final Set<String> BLOCKED_WORDS = loadBlockedWords();

    private ObservationContentValidator() {
    }

    public static Result validate(String raw) {
        String text = ChatColor.stripColor(raw == null ? "" : raw).trim();
        if (text.isEmpty()) {
            return new Result(Failure.EMPTY);
        }

        String normalized = normalize(text);
        if (containsBlockedWord(normalized)) {
            return new Result(Failure.PROFANITY);
        }
        if (isNonsense(normalized)) {
            return new Result(Failure.NONSENSE);
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

    private static boolean containsBlockedWord(String normalized) {
        Matcher matcher = WORD_BOUNDARY.matcher(normalized);
        while (matcher.find()) {
            if (BLOCKED_WORDS.contains(matcher.group(1))) {
                return true;
            }
        }
        return false;
    }

    private static boolean isNonsense(String text) {
        if (REPEATED_CHAR.matcher(text).find()) {
            return true;
        }

        String lettersOnly = text.replaceAll("[^a-z]", "");
        if (lettersOnly.length() < 8) {
            return false;
        }

        long unique = lettersOnly.chars().distinct().count();
        if (unique <= 2) {
            return true;
        }

        return (double) unique / lettersOnly.length() < 0.2;
    }

    private static String normalize(String text) {
        String normalized = text.toLowerCase();
        normalized = normalized.replace('@', 'a').replace('4', 'a');
        normalized = normalized.replace('3', 'e');
        normalized = normalized.replace('1', 'i').replace('!', 'i');
        normalized = normalized.replace('0', 'o');
        normalized = normalized.replace('$', 's').replace('5', 's');
        normalized = normalized.replaceAll("[^a-z0-9'\\s]", " ");
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
