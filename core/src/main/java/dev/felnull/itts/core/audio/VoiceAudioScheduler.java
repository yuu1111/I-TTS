package dev.felnull.itts.core.audio;

import com.sedmelluq.discord.lavaplayer.player.AudioPlayer;
import com.sedmelluq.discord.lavaplayer.player.event.AudioEventAdapter;
import com.sedmelluq.discord.lavaplayer.track.AudioTrack;
import com.sedmelluq.discord.lavaplayer.track.AudioTrackEndReason;
import dev.felnull.itts.core.ITTSRuntimeUse;
import dev.felnull.itts.core.audio.loader.VoiceTrackLoader;
import dev.felnull.itts.core.tts.saidtext.SaidText;
import dev.felnull.itts.core.util.TTSUtils;
import dev.felnull.itts.core.voice.Voice;
import net.dv8tion.jda.api.managers.AudioManager;

import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicReference;

/**
 * オーディオ再生のスケジュール
 *
 * @author MORIMORI0317
 */
public class VoiceAudioScheduler extends AudioEventAdapter implements ITTSRuntimeUse {

    /**
     * 現在再生中のテキストと終了時処理
     *
     * @param loadedSaidText 読み込み済み読み上げテキスト
     * @param playEndRun     再生終了後の処理
     */
    private record PlayingEntry(LoadedSaidText loadedSaidText, Runnable playEndRun) {
    }

    /**
     * オーディオマネージャー
     */
    private final AudioManager audioManager;

    /**
     * ボイスオーディオマネージャー
     */
    private final VoiceAudioManager voiceAudioManager;

    /**
     * オーディオプレイヤー
     */
    private final AudioPlayer audioPlayer;

    /**
     * 現在の読み込み済み読み上げテキスト
     */
    private final AtomicReference<PlayingEntry> currentLoaded = new AtomicReference<>();

    /**
     * サーバーID
     */
    private final long guildId;

    /**
     * コンストラクタ
     *
     * @param audioManager      オーディオマネージャー
     * @param voiceAudioManager ボイスオーディオマネージャー
     * @param guildId           サーバーID
     */
    public VoiceAudioScheduler(AudioManager audioManager, VoiceAudioManager voiceAudioManager, long guildId) {
        this.audioManager = audioManager;
        this.voiceAudioManager = voiceAudioManager;
        this.audioPlayer = voiceAudioManager.getAudioPlayerManager().createPlayer();
        this.guildId = guildId;
        this.audioPlayer.addListener(this);
        this.audioManager.setSendingHandler(new VoiceAudioHandler(audioPlayer));
    }

    /**
     * 破棄
     */
    public void dispose() {
        stop();
        this.audioManager.setSendingHandler(null);
    }

    /**
     * 読み込みを開始
     *
     * @param saidText 読み上げテキスト
     * @return 読み込み済み読み上げテキストの非同期読み込みCompletableFuture
     */
    public CompletableFuture<LoadedSaidText> load(SaidText saidText) {
        CompletableFuture<String> textCf = saidText.getText();
        CompletableFuture<Voice> voiceCf = saidText.getVoice();

        return textCf.thenCombineAsync(voiceCf, (text, voice) -> {
                    String sayText = getDictionaryManager().applyDict(text, guildId);
                    Objects.requireNonNull(voice, "Voice is null");
                    return new SayTextWithVoice(TTSUtils.roundText(voice, guildId, sayText, false), voice);
                }, getAsyncExecutor())
                .thenComposeAsync(sayTextVoice -> {
                    VoiceTrackLoader vtl = sayTextVoice.voice().createVoiceTrackLoader(sayTextVoice.sayText());
                    return vtl.load().thenApply(r -> new LoadedSaidText(saidText, r, vtl::dispose));
                }, getAsyncExecutor());
    }

    /**
     * 再生を一時停止
     */
    public void stop() {
        PlayingEntry old = currentLoaded.getAndSet(null);
        if (old != null) {
            old.loadedSaidText().dispose();
        }
        audioPlayer.stopTrack();
    }

    /**
     * 再生を開始
     *
     * @param loadedSaidText 読み込み済み読み上げテキスト
     * @param playEndRun     再生終了後の処理
     */
    public void play(LoadedSaidText loadedSaidText, Runnable playEndRun) {
        currentLoaded.set(new PlayingEntry(loadedSaidText, playEndRun));
        audioPlayer.playTrack(loadedSaidText.getTrack());
    }

    @Override
    public void onTrackEnd(AudioPlayer player, AudioTrack track, AudioTrackEndReason endReason) {
        PlayingEntry old = currentLoaded.getAndSet(null);
        if (old != null) {
            old.loadedSaidText().setAlreadyUsed(true);
            old.playEndRun().run();
        }
    }

    /**
     * 辞書適用済みテキストと音声の組
     *
     * @param sayText 読み上げテキスト
     * @param voice   音声
     */
    private record SayTextWithVoice(String sayText, Voice voice) {
    }
}
