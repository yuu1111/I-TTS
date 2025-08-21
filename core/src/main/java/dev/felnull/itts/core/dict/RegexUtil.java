package dev.felnull.itts.core.dict;

import java.util.*;
import java.util.function.Function;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 正規表現関係
 *
 * @author shiro8613
 */
public class RegexUtil {

    /** オプション保持用のリスト */
    private final List<RegexOption> optionList = new ArrayList<>();
    
    /** URL検出用パターン */
    private static final Pattern URL_PATTERN = Pattern.compile(
        "(?:https?|ftp)://[^\\s<>\"{}|\\\\^`\u3000-\u303f\uff00-\uffef]+" +  // 日本語記号と全角記号を除外
        "(?<![.,;:!?\u3001\u3002\u300d\u300f）])"  // 末尾の句読点を除外（括弧は許可）
    );

    /** 保護された文字列（URLなど）と通常テキストを分離 */
    private List<TextSegment> extractProtectedSegments(String text) {
        List<TextSegment> segments = new ArrayList<>();
        Matcher urlMatcher = URL_PATTERN.matcher(text);
        int lastEnd = 0;
        
        while (urlMatcher.find()) {
            // URL前のテキスト
            if (lastEnd < urlMatcher.start()) {
                segments.add(new TextSegment(text.substring(lastEnd, urlMatcher.start()), false));
            }
            // URL自体（保護対象）
            segments.add(new TextSegment(urlMatcher.group(), true));
            lastEnd = urlMatcher.end();
        }
        
        // 最後の部分
        if (lastEnd < text.length()) {
            segments.add(new TextSegment(text.substring(lastEnd), false));
        }
        
        return segments;
    }

    /** 日本語と英語をわけます */
    private List<String> splitJapaneseEnglish(String text) {
        String[] dividedText = text.split("");
        List<String> createdText = new ArrayList<>();
        StringBuilder tmpText = new StringBuilder();
        boolean en = false;

        for (String txt :dividedText) {
            if (txt.matches("[A-Za-z0-9:/#$%&.,-?_]+")) {
                if (!en) {
                    createdText.add(tmpText.toString());
                    tmpText.setLength(0);
                }
                en = true;
                tmpText.append(txt);
            } else {
                if (en) {
                    createdText.add(tmpText.toString());
                    tmpText.setLength(0);
                    en = false;
                }
                tmpText.append(txt);
            }
        }

        if (!tmpText.isEmpty()) {
            createdText.add(tmpText.toString());
        }

        return createdText;
    }

    /**
     * オプション追加
     *
     * @param priority 優先度
     * @param replacedText 置き換えテキスト
     * @param testFunction 置き換え判定用関数
     * @return this このクラスが返ってきます
     */
    public RegexUtil addOption(int priority, String replacedText, Function<String, Boolean> testFunction) {
        this.optionList.add(new RegexOption(priority, replacedText, testFunction));
        return this;
    }

    /**
     * テキストを置き換える
     *
     * @param text 置き換え対象のテキスト
     * @return 置き換え済みテキスト
     */
    public String replaceText(String text) {
        // URLを含むセグメントに分割
        List<TextSegment> segments = extractProtectedSegments(text);
        StringBuilder result = new StringBuilder();
        
        for (TextSegment segment : segments) {
            if (segment.isProtected) {
                // URLは全体を一つの単位として処理
                boolean replaced = false;
                for (RegexOption ops : optionList.stream().sorted(Comparator.comparingInt(o -> o.priority)).toList()) {
                    if (ops.testFunction.apply(segment.text)) {
                        result.append(ops.replacedText);
                        replaced = true;
                        break;
                    }
                }
                if (!replaced) {
                    result.append(segment.text);
                }
            } else {
                // 通常のテキストは従来通り分割して処理
                List<String> texts = splitJapaneseEnglish(segment.text);
                for (String txt : texts) {
                    boolean replaced = false;
                    for (RegexOption ops : optionList.stream().sorted(Comparator.comparingInt(o -> o.priority)).toList()) {
                        if (ops.testFunction.apply(txt)) {
                            result.append(ops.replacedText);
                            replaced = true;
                            break;
                        }
                    }
                    if (!replaced) {
                        result.append(txt);
                    }
                }
            }
        }
        
        return result.toString();
    }


    /** テキストセグメント（保護フラグ付き） */
    private static class TextSegment {
        final String text;
        final boolean isProtected;
        
        TextSegment(String text, boolean isProtected) {
            this.text = text;
            this.isProtected = isProtected;
        }
    }

    /** オプション定義用クラス
     *
     * @author shiro8613
     * */
    public static class RegexOption {

        /** プライオリティ */
        public int priority;

        /** 判定関数 */
        public Function<String, Boolean> testFunction;

        /**　置き換えテキスト*/
        public String replacedText;

        /**
         * コンストラクタ
         *
         * @param priority 優先度
         * @param replacedText 置き換えテキスト
         * @param testFunction 置き換え判定用関数
         */
        public RegexOption(int priority, String replacedText, Function<String, Boolean> testFunction) {
            this.priority = priority;
            this.replacedText = replacedText;
            this.testFunction = testFunction;
        }
    }

}
