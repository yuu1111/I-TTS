package dev.felnull.itts.core.cache;

import com.google.common.hash.HashCode;
import dev.felnull.fnjl.util.FNDataUtil;
import dev.felnull.itts.core.ITTSRuntimeUse;
import org.apache.commons.io.FileUtils;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Supplier;

/**
 * キャッシュの管理
 *
 * @author MORIMORI0317
 */
public class CacheManager implements ITTSRuntimeUse {

    /**
     * キャッシュ保存用フォルダー
     */
    private static final File LOCAL_CACHE_FOLDER = new File("./tmp");

    /**
     * 保存済みローカルキャッシュ
     */
    private final Map<HashCode, CompletableFuture<LocalCache>> localCaches = new ConcurrentHashMap<>();

    /**
     * グローバルキャッシュアクセスの取得
     */
    private final Supplier<GlobalCacheAccess> globalCacheAccessFactory;

    /**
     * コンストラクタ
     *
     * @param globalCacheAccessFactory グローバルキャッシュアクセスの取得用Supplier
     */
    public CacheManager(@Nullable Supplier<GlobalCacheAccess> globalCacheAccessFactory) {
        try {
            FileUtils.deleteDirectory(LOCAL_CACHE_FOLDER);
            FNDataUtil.wishMkdir(LOCAL_CACHE_FOLDER);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        this.globalCacheAccessFactory = globalCacheAccessFactory;
    }

    /**
     * キャッシュを読み込む、もしくは生成する
     *
     * @param key        キー
     * @param loadOpener ストリーム生成
     * @return キャッシュエントリのCompletableFuture
     */
    public CompletableFuture<CacheUseEntry> loadOrRestore(@NotNull HashCode key, @NotNull StreamOpener loadOpener) {
        return localCaches.computeIfAbsent(key, ky -> {
            CompletableFuture<LocalCache> future = createLocalCache(ky, loadOpener);
            future.exceptionally(ex -> {
                localCaches.remove(ky, future);
                return null;
            });
            return future;
        }).thenApplyAsync(LocalCache::restore, getAsyncExecutor());
    }

    /**
     * ローカルキャッシュを非同期で生成する
     *
     * @param key        キャッシュキー
     * @param loadOpener ストリーム生成
     * @return ローカルキャッシュのCompletableFuture
     */
    private CompletableFuture<LocalCache> createLocalCache(HashCode key, StreamOpener loadOpener) {
        File lcFile = getLocalCacheFile(key);

        CompletableFuture<File> cf;
        if (globalCacheAccessFactory != null) {
            cf = CompletableFuture.supplyAsync(() -> loadViaGlobalCache(key, loadOpener, lcFile), getAsyncExecutor());
        } else {
            cf = CompletableFuture.supplyAsync(() -> loadDirectly(loadOpener, lcFile), getAsyncExecutor());
        }

        return cf.thenApply(file -> new LocalCache(key, file));
    }

    /**
     * グローバルキャッシュ経由でデータを取得し、ローカルファイルに書き込む
     *
     * @param key        キャッシュキー
     * @param loadOpener ストリーム生成
     * @param lcFile     ローカルキャッシュファイル
     * @return 書き込み済みのローカルキャッシュファイル
     */
    private File loadViaGlobalCache(HashCode key, StreamOpener loadOpener, File lcFile) {
        try (GlobalCacheAccess gca = globalCacheAccessFactory.get()) {
            byte[] data = gca.get(key);

            if (data == null) {
                data = loadAndCacheWithLock(gca, key, loadOpener);
            }

            Files.write(lcFile.toPath(), data);
            return lcFile;
        } catch (Exception ex) {
            throw new RuntimeException(ex);
        }
    }

    /**
     * ロックを取得してダブルチェック後、データを読み込みグローバルキャッシュに保存する
     *
     * @param gca        グローバルキャッシュアクセス
     * @param key        キャッシュキー
     * @param loadOpener ストリーム生成
     * @return キャッシュデータ
     * @throws IOException          IO例外
     * @throws InterruptedException 割り込み例外
     */
    private byte[] loadAndCacheWithLock(GlobalCacheAccess gca, HashCode key, StreamOpener loadOpener)
            throws IOException, InterruptedException {
        gca.lock(key);
        try {
            byte[] data = gca.get(key);
            if (data == null) {
                try (BufferedInputStream in = new BufferedInputStream(loadOpener.openStream())) {
                    data = in.readAllBytes();
                }
                gca.set(key, data);
            }
            return data;
        } finally {
            gca.unlock(key);
        }
    }

    /**
     * ストリームからローカルファイルに直接書き込む
     *
     * @param loadOpener ストリーム生成
     * @param lcFile     ローカルキャッシュファイル
     * @return 書き込み済みのローカルキャッシュファイル
     */
    private File loadDirectly(StreamOpener loadOpener, File lcFile) {
        try (var in = loadOpener.openStream(); var out = new FileOutputStream(lcFile)) {
            FNDataUtil.inputToOutputBuff(in, out);
        } catch (IOException | InterruptedException e) {
            throw new RuntimeException(e);
        }
        return lcFile;
    }

    private File getLocalCacheFile(HashCode hashCode) {
        return new File(LOCAL_CACHE_FOLDER, hashCode.toString());
    }

    /**
     * キャッシュを破棄
     *
     * @param hashCode キャッシュのキー用ハッシュコード
     */
    protected void disposeCache(HashCode hashCode) {
        CompletableFuture<LocalCache> lc = localCaches.remove(hashCode);
        if (lc != null) {
            lc.thenAcceptAsync(LocalCache::dispose, getAsyncExecutor());
        }
    }
}
