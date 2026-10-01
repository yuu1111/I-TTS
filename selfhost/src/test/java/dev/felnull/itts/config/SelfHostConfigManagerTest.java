package dev.felnull.itts.config;

import blue.endless.jankson.Jankson;
import blue.endless.jankson.JsonObject;
import dev.felnull.itts.utils.Json5Utils;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.*;

/**
 * 設定バージョン2から3への移行を検証する
 */
class SelfHostConfigManagerTest {
    /**
     * テスト用ディレクトリ
     */
    @TempDir
    private Path directory;

    @Test
    void addsDisabledDefaultsAndPreservesExistingValuesAndComments() throws Exception {
        Path file = directory.resolve("config.json5");
        Files.writeString(file, """
                {
                    config_version: 2,
                    // BOTトークンのコメント
                    bot_token: "test-token",
                    voicevox: {enable: false, api_url: ["http://localhost:50021"]},
                    // 独自設定も保持
                    custom_setting: {value: 42},
                }
                """);
        Jankson jankson = Jankson.builder().build();
        String original = Files.readString(file);
        SelfHostConfigManager manager = new SelfHostConfigManager(directory);
        assertFalse(manager.loadConfig().getOpenAiTtsConfig().isEnable());
        try (java.util.stream.Stream<Path> backups = Files.list(directory.resolve("old_config"))) {
            Path backup = backups.findFirst().orElseThrow();
            assertTrue(backup.getFileName().toString().startsWith("config_v2_"));
            assertEquals(original, Files.readString(backup));
        }

        JsonObject saved = jankson.load(file.toFile());
        assertEquals("test-token", saved.get(String.class, "bot_token"));
        assertEquals(3, saved.getInt("config_version", 0));
        assertEquals(42, saved.getObject("custom_setting").getInt("value", 0));
        assertFalse(saved.getObject("voicevox").getBoolean("enable", true));
        assertEquals("http://localhost:50021", Json5Utils.getStringListOfJsonArray(saved.getObject("voicevox"), "api_url").getFirst());
        String content = Files.readString(file);
        assertTrue(content.contains("BOTトークンのコメント"));
        assertTrue(content.contains("独自設定も保持"));
        assertTrue(content.contains("APIベースURL"));
        assertEquals(new OpenAiTtsConfigImpl(), ConfigImpl.LOADER.load(saved).getOpenAiTtsConfig());

        manager.loadConfig();
        assertEquals(content, Files.readString(file));
        try (java.util.stream.Stream<Path> files = Files.list(directory)) {
            assertEquals(2, files.count());
        }
    }

    @Test
    void preservesExistingOpenAiTtsSettingsDuringMigration() throws Exception {
        Path file = directory.resolve("config.json5");
        String content = """
                {config_version: 2, openai_tts: {
                    // 設定済み
                    enable: true,
                    base_url: "http://localhost:8000/v1",
                    api_key: "test-key",
                    model: "custom-model",
                    voices: ["custom-voice"],
                }}
                """;
        Files.writeString(file, content);
        JsonObject original = Jankson.builder().build().load(file.toFile());
        OpenAiTtsConfigImpl settings = OpenAiTtsConfigImpl.fromJson(original.getObject("openai_tts"));
        assertEquals(settings, new SelfHostConfigManager(directory).loadConfig().getOpenAiTtsConfig());
        JsonObject saved = Jankson.builder().build().load(file.toFile());
        assertEquals(3, saved.getInt("config_version", 0));
        assertEquals(settings, ConfigImpl.LOADER.load(saved).getOpenAiTtsConfig());
        assertTrue(Files.readString(file).contains("設定済み"));
    }

    @Test
    void doesNotReplaceExplicitNullSetting() throws Exception {
        Path file = directory.resolve("config.json5");
        String content = "{config_version: 2, openai_tts: null}";
        Files.writeString(file, content);
        JsonObject original = Jankson.builder().build().load(file.toFile());
        assertTrue(SelfHostConfigManager.migrateV2Config(file, original));
        JsonObject saved = Jankson.builder().build().load(file.toFile());
        assertEquals(3, saved.getInt("config_version", 0));
        assertEquals(original.get("openai_tts"), saved.get("openai_tts"));
    }

    @Test
    void createsVersion3ConfigOnFirstStartup() throws Exception {
        SelfHostConfigManager manager = new SelfHostConfigManager(directory);
        assertThrows(IllegalStateException.class, manager::loadConfig);
        JsonObject saved = Jankson.builder().build().load(directory.resolve("config.json5").toFile());
        assertEquals(3, saved.getInt("config_version", 0));
        assertEquals(new OpenAiTtsConfigImpl(), ConfigImpl.LOADER.load(saved).getOpenAiTtsConfig());
    }

    @Test
    void migratesLegacyVersion1ThroughVersion2ToVersion3() throws Exception {
        Path file = directory.resolve("config.json5");
        Files.writeString(file, "{config_version: 1, bot_token: 'legacy-token'}");
        assertEquals("legacy-token", new SelfHostConfigManager(directory).loadConfig().getBotToken());
        JsonObject saved = Jankson.builder().build().load(file.toFile());
        assertEquals(3, saved.getInt("config_version", 0));
        assertFalse(ConfigImpl.LOADER.load(saved).getOpenAiTtsConfig().isEnable());
    }

    @Test
    void rejectsFutureVersionWithoutChangingFile() throws Exception {
        Path file = directory.resolve("config.json5");
        String content = "{config_version: 4}";
        Files.writeString(file, content);
        assertThrows(IllegalStateException.class, new SelfHostConfigManager(directory)::loadConfig);
        assertEquals(content, Files.readString(file));
    }
}
