package dev.felnull.itts.core.audio.loader;

import com.google.common.hash.HashCode;
import com.sedmelluq.discord.lavaplayer.player.AudioLoadResultHandler;
import com.sedmelluq.discord.lavaplayer.tools.FriendlyException;
import com.sedmelluq.discord.lavaplayer.track.AudioPlaylist;
import com.sedmelluq.discord.lavaplayer.track.AudioTrack;
import dev.felnull.itts.core.ITTSRuntimeUse;
import dev.felnull.itts.core.audio.VoiceAudioManager;
import dev.felnull.itts.core.cache.CacheUseEntry;
import dev.felnull.itts.core.cache.StreamOpener;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.atomic.AtomicReference;

/**
 * キャッシュを取る音声の読み込み
 *
 * @author MORIMORI0317
 */
public class CachedVoiceTrackLoader implements VoiceTrackLoader, ITTSRuntimeUse {

    /**
     * 音声識別用ハッシュ
     */
    private final HashCode hash;

    /**
     * 音声のストリーム取得用オープナー
     */
    private final StreamOpener streamOpener;

    /**
     * キャッシュのエントリ
     */
    private final AtomicReference<CacheUseEntry> cacheEntry = new AtomicReference<>();

    /**
     * コンストラクタ
     *
     * @param hash         音声識別用ハッシュ
     * @param streamOpener 音声のストリーム取得用オープナー
     */
    public CachedVoiceTrackLoader(HashCode hash, StreamOpener streamOpener) {
        this.hash = hash;
        this.streamOpener = streamOpener;
    }

    @Override
    public CompletableFuture<AudioTrack> load() {
        return getCacheManager().loadOrRestore(hash, streamOpener)
                .thenApplyAsync(this::loadTrack, getAsyncExecutor());
    }

    private AudioTrack loadTrack(CacheUseEntry cacheUseEntry) {
        cacheEntry.set(cacheUseEntry);
        String path = cacheUseEntry.file().getAbsolutePath();
        VoiceAudioManager vam = getVoiceAudioManager();
        AtomicReference<AudioTrack> retTrack = new AtomicReference<>();
        AtomicReference<Exception> error = new AtomicReference<>();

        try {
            vam.getAudioPlayerManager().loadItem(path, new AudioLoadResultHandler() {
                @Override
                public void trackLoaded(AudioTrack track) {
                    retTrack.set(track);
                }

                @Override
                public void playlistLoaded(AudioPlaylist playlist) {
                    error.set(new RuntimeException("Unexpected playlist loaded: " + path));
                }

                @Override
                public void noMatches() {
                    error.set(new RuntimeException("No audio track found: " + path));
                }

                @Override
                public void loadFailed(FriendlyException exception) {
                    error.set(exception);
                }
            }).get();
        } catch (InterruptedException | ExecutionException e) {
            throw new RuntimeException(e);
        }

        if (error.get() != null) {
            throw new RuntimeException("Failed to load track: " + path, error.get());
        }

        AudioTrack ret = retTrack.get();
        if (ret == null) {
            throw new RuntimeException("Failed to load track: " + path);
        }

        return ret;
    }

    @Override
    public void dispose() {
        CacheUseEntry ce = cacheEntry.get();
        if (ce != null) {
            ce.useLock().unlock();
        }
    }
}
