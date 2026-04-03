package dev.felnull.itts.core.dict;

import org.jetbrains.annotations.NotNull;

import java.util.Map;
import java.util.function.Function;
import java.util.regex.Pattern;

/**
 * 正規表現置き換え辞書のベース
 *
 * @author MORIMORI0317
 */
public abstract class RegexReplaceBaseDictionary implements Dictionary {
    /**
     * 置き換える正規表現と置き換えファンクションのMAP
     *
     * @param guildId サーバーID
     * @return 正規表現と置き換えファンクションのMAP
     */
    @NotNull
    protected abstract Map<Pattern, Function<String, String>> getReplaces(long guildId);

    @Override
    public @NotNull String apply(@NotNull String text, long guildId) {
        Map<Pattern, Function<String, String>> replaces = getReplaces(guildId);
        String result = text;
        for (Map.Entry<Pattern, Function<String, String>> entry : replaces.entrySet()) {
            result = entry.getKey().matcher(result).replaceAll(res -> entry.getValue().apply(res.group()));
        }
        return result;
    }
}
