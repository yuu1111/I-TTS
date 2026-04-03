package dev.felnull.itts.core.dict;

import dev.felnull.itts.core.ITTSRuntimeUse;
import dev.felnull.itts.core.savedata.legacy.LegacyDictData;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Unmodifiable;

import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * ユーザー定義辞書データに基づく正規表現辞書の共通基底クラス
 */
public abstract class CustomDictBaseDictionary extends RegexReplaceBaseDictionary implements ITTSRuntimeUse {

    /**
     * 辞書データを取得する
     *
     * @param guildId サーバーID
     * @return 辞書データのリスト
     */
    @NotNull
    protected abstract List<LegacyDictData> fetchData(long guildId);

    /**
     * ログ出力用の辞書名を取得する
     *
     * @return 辞書名
     */
    @NotNull
    protected abstract String getDictLogName();

    @Override
    public boolean isBuiltIn() {
        return false;
    }

    @Override
    public @NotNull @Unmodifiable Map<String, String> getShowInfo(long guildId) {
        return fetchData(guildId).stream()
                .collect(Collectors.toMap(LegacyDictData::getTarget, LegacyDictData::getRead));
    }

    @Override
    protected @NotNull Map<Pattern, Function<String, String>> getReplaces(long guildId) {
        return fetchData(guildId).stream()
                .flatMap(n -> {
                    try {
                        Pattern pattern = Pattern.compile(n.getTarget());
                        return Stream.of(new PatternRead(pattern, n.getRead()));
                    } catch (PatternSyntaxException e) {
                        getITTSLogger().warn("Invalid regex pattern in {} dict: {}", getDictLogName(), n.getTarget());
                        return Stream.empty();
                    }
                })
                .collect(Collectors.toMap(PatternRead::pattern, pr -> ignored -> pr.read()));
    }

    /**
     * パターンと読みの組
     *
     * @param pattern 正規表現パターン
     * @param read    読み
     */
    private record PatternRead(Pattern pattern, String read) {
    }
}
