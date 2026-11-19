package wtf.reversed.toolbox.util;

import java.io.*;
import java.nio.file.*;
import java.util.*;
import java.util.regex.*;

/**
 * Locates games installed through Steam.
 * <p>
 * Reads Steam's {@code libraryfolders.vdf} to find all library folders, then reads the
 * {@code appmanifest_*.acf} files in each library to find the installed games.
 */
public final class SteamApps {
    private final Map<Integer, Path> games = new TreeMap<>();

    private SteamApps() {
    }

    /**
     * Finds all games installed through Steam on this machine.
     *
     * @return a map of app ID to install directory
     * @throws IOException if a library folder can't be listed
     */
    public static Map<Integer, Path> findAllGames() throws IOException {
        var steam = new SteamApps();
        steam.findAll();
        return steam.games;
    }

    private void findAll() throws IOException {
        for (var folderPath : findLibraryFolders()) {
            findAllGames(folderPath);
        }
    }

    private List<Path> findLibraryFolders() {
        var libraryFoldersPath = getSteamPath().resolve("config/libraryfolders.vdf");
        return VdfParser.tryParse(libraryFoldersPath)
            .map(this::findLibraryFolderPaths)
            .orElse(List.of());
    }

    private Path getSteamPath() {
        var userHome = System.getProperty("user.home");
        if (userHome == null) {
            throw new IllegalStateException("Unable to determine user home directory");
        }
        return switch (Platform.OS.current()) {
            case WINDOWS -> Path.of("C:\\Program Files (x86)\\Steam");
            case LINUX -> Path.of(userHome, ".steam/steam");
            case MACOS -> Path.of(userHome, "Library/Application Support/Steam");
        };
    }

    private List<Path> findLibraryFolderPaths(Map<String, Object> root) {
        if (!(root.get("libraryfolders") instanceof Map<?, ?> folders)) {
            return List.of();
        }

        var result = new ArrayList<Path>();
        for (var folder : folders.values()) {
            if (folder instanceof Map<?, ?> map && map.get("path") instanceof String path) {
                var steamapps = Path.of(path, "steamapps");
                if (Files.exists(steamapps)) {
                    result.add(steamapps);
                }
            }
        }
        return result;
    }

    private void findAllGames(Path folderPath) throws IOException {
        try (var stream = Files.newDirectoryStream(folderPath, "appmanifest_*.acf")) {
            for (var manifestPath : stream) {
                findInstallDir(folderPath, manifestPath);
            }
        }
    }

    private void findInstallDir(Path folderPath, Path manifestPath) {
        var manifest = VdfParser.tryParse(manifestPath).orElse(Map.of());
        if (manifest.get("AppState") instanceof Map<?, ?> appState
            && appState.get("appid") instanceof String appId
            && appState.get("installdir") instanceof String installDir
        ) {
            var path = folderPath.resolve("common", installDir);
            if (Files.exists(path)) {
                games.put(Integer.parseInt(appId), path);
            }
        }
    }

    private static final class VdfParser {
        private static final Pattern LEXER = Pattern.compile("(\\s+)|\"((?:[^\\\\\"]+|\\\\.)*+)\"|([{}])");

        private static Optional<Map<String, Object>> tryParse(Path path) {
            try {
                var source = Files.readString(path);
                return Optional.of(parse(source));
            } catch (Exception e) {
                return Optional.empty();
            }
        }

        private static Map<String, Object> parse(String source) {
            var matcher = LEXER.matcher(source);
            var stack = new ArrayDeque<Map<String, Object>>();
            stack.push(new LinkedHashMap<>());

            var key = (String) null;
            while (matcher.lookingAt()) {
                if (matcher.group(2) != null) {
                    if (key == null) {
                        key = matcher.group(2);
                    } else {
                        var value = matcher.group(2).replace("\\\\", "\\");
                        stack.peek().put(key, value);
                        key = null;
                    }
                } else if (matcher.group(3) != null) {
                    if (matcher.group(3).equals("{")) {
                        var map = new LinkedHashMap<String, Object>();
                        stack.peek().put(key, map);
                        key = null;
                        stack.push(map);
                    } else /*if (matcher.group(3).equals("}"))*/ {
                        stack.pop();
                    }
                }
                matcher.region(matcher.end(), source.length());
            }
            if (matcher.regionStart() != source.length() || stack.size() != 1) {
                throw new IllegalArgumentException("Invalid input at " + matcher.regionStart());
            }
            return stack.pop();
        }
    }
}
