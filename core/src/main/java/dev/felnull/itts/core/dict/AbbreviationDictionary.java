package dev.felnull.itts.core.dict;

import com.google.common.collect.ImmutableMap;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Unmodifiable;

import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 省略辞書
 *
 * @author MORIMORI0317
 */
public class AbbreviationDictionary implements Dictionary {

    /**
     * インラインコードの正規表現（バッククォート1つ）
     */
    private static final Pattern INLINE_CODE_REGEX = Pattern.compile("`[^`\n]+`");
    
    /**
     * コードブロックの正規表現（非貪欲マッチング）
     */
    private static final Pattern CODE_BLOCK_REGEX = Pattern.compile("```[\\s\\S]*?```");

    /**
     * 正規表現関係
     */
    private final RegexUtil regexUtil = new RegexUtil()
            .addOption(2, "ドメインショウリャク", s -> {
                Pattern pattern = Pattern.compile("^([a-zA-Z0-9][a-zA-Z0-9-]*[a-zA-Z0-9]*\\.)+[a-zA-Z]{2,}$");
                Matcher matcher = pattern.matcher(s);
                return matcher.find();
            })
            .addOption(1, "アイピーブイフォーショウリャク", s -> {
                Pattern pattern = Pattern.compile("^((25[0-5]|2[0-4][0-9]|1[0-9][0-9]|[1-9]?[0-9])\\.){3}(25[0-5]|2[0-4][0-9]|1[0-9][0-9]|[1-9]?[0-9])$");
                Matcher matcher = pattern.matcher(s);
                return matcher.matches();
            })
            .addOption(1, "アイピーブイロクショウリャク", s -> {
                Pattern pattern = Pattern.compile("(([0-9a-fA-F]{1,4}:){7}[0-9a-fA-F]{1,4}|"
                        + "([0-9a-fA-F]{1,4}:){1,7}:|"
                        + "([0-9a-fA-F]{1,4}:){1,6}:[0-9a-fA-F]{1,4}|"
                        + "([0-9a-fA-F]{1,4}:){1,5}(:[0-9a-fA-F]{1,4}){1,2}|"
                        + "([0-9a-fA-F]{1,4}:){1,4}(:[0-9a-fA-F]{1,4}){1,3}|"
                        + "([0-9a-fA-F]{1,4}:){1,3}(:[0-9a-fA-F]{1,4}){1,4}|"
                        + "([0-9a-fA-F]{1,4}:){1,2}(:[0-9a-fA-F]{1,4}){1,5}|"
                        + "[0-9a-fA-F]{1,4}:((:[0-9a-fA-F]{1,4}){1,6})|"
                        + ":((:[0-9a-fA-F]{1,4}){1,7}|:)|fe80:(:[0-9a-fA-F]{0,4}){0,4}%[0-9a-zA-Z]+|"
                        + "::(ffff(:0{1,4})?:)?((25[0-5]|(2[0-4]|1?[0-9])?[0-9])\\.){3}(25[0-5]|"
                        + "(2[0-4]|1?[0-9])?[0-9])|([0-9a-fA-F]{1,4}:){1,4}:((25[0-5]|"
                        + "(2[0-4]|1?[0-9])?[0-9])\\.){3}(25[0-5]|(2[0-4]|1?[0-9])?[0-9]))");
                Matcher matcher = pattern.matcher(s);
                return matcher.matches();
            });


    /** URLパターン（優先度最高で検出） */
    private static final Pattern URL_PATTERN = Pattern.compile(
        "(?:https?|ftp)://[^\\s\u3000<>\"{}|\\\\^`]+" +
        "(?<![.,;:!?\u3001\u3002\u300d\u300f）])"
    );

    @Override
    public @NotNull String apply(@NotNull String text, long guildId) {
        // インラインコードを保護（変換しない）
        Map<String, String> inlineCodePlaceholders = new HashMap<>();
        Matcher inlineMatcher = INLINE_CODE_REGEX.matcher(text);
        int inlineIndex = 0;
        StringBuffer sb = new StringBuffer();
        while (inlineMatcher.find()) {
            String placeholder = "__INLINE_CODE_" + inlineIndex + "__";
            inlineCodePlaceholders.put(placeholder, inlineMatcher.group());
            inlineMatcher.appendReplacement(sb, placeholder);
            inlineIndex++;
        }
        inlineMatcher.appendTail(sb);
        text = sb.toString();
        
        // コードブロックを置換
        text = CODE_BLOCK_REGEX.matcher(text).replaceAll("コードブロックショウリャク");
        
        // URLを先に検出して置換（RegexUtilで分割される前に処理）
        text = URL_PATTERN.matcher(text).replaceAll("ユーアルエルショウリャク");
        
        // 他の変換を実行
        text = regexUtil.replaceText(text);
        
        // インラインコードを元に戻す
        for (Map.Entry<String, String> entry : inlineCodePlaceholders.entrySet()) {
            text = text.replace(entry.getKey(), entry.getValue());
        }
        
        return text;
    }

    @Override
    public boolean isBuiltIn() {
        return true;
    }

    @Override
    public @NotNull String getName() {
        return "省略辞書";
    }

    @Override
    public @NotNull String getId() {
        return "abbreviation";
    }

    @Override
    public @NotNull @Unmodifiable Map<String, String> getShowInfo(long guildId) {
        return ImmutableMap.of("https://...", "URL省略", "``` コードブロック ```", "コードブロック省略");
    }

    @Override
    public int getDefaultPriority() {
        return 1;
    }
}
