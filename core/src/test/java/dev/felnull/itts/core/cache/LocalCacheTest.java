package dev.felnull.itts.core.cache;

import com.google.common.hash.HashCode;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * LocalCacheのテスト
 */
class LocalCacheTest {

    @TempDir
    Path tempDir;

    /**
     * dispose()はファイルが存在する場合、正常に削除すべき。
     * バグ: 現在のコードは削除成功時に例外をスローする。
     */
    @Test
    void dispose_shouldDeleteFileWithoutException() throws IOException {
        File testFile = tempDir.resolve("test-cache-file").toFile();
        Files.createFile(testFile.toPath());
        assertTrue(testFile.exists(), "テストファイルが作成されていること");

        HashCode hashCode = HashCode.fromInt(12345);
        LocalCache localCache = new LocalCache(hashCode, testFile);

        assertDoesNotThrow(() -> localCache.dispose(),
                "dispose()はファイル削除成功時に例外をスローすべきではない");
        assertFalse(testFile.exists(), "dispose()後にファイルが削除されていること");
    }

    /**
     * dispose()はファイルが存在しない場合も例外をスローしない。
     */
    @Test
    void dispose_shouldNotThrowWhenFileDoesNotExist() {
        File nonExistentFile = tempDir.resolve("non-existent-file").toFile();
        assertFalse(nonExistentFile.exists(), "ファイルが存在しないこと");

        HashCode hashCode = HashCode.fromInt(12345);
        LocalCache localCache = new LocalCache(hashCode, nonExistentFile);

        assertDoesNotThrow(() -> localCache.dispose(),
                "dispose()は存在しないファイルに対して例外をスローすべきではない");
    }
}
