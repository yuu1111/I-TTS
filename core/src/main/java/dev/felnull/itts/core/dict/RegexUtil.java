package dev.felnull.itts.core.dict;

import java.util.*;
import java.util.function.Function;

/**
 * 正規表現関係
 *
 * @author shiro8613
 */
public class RegexUtil {

    /**
     * オプション保持用のリスト
     */
    private final List<RegexOption> optionList = new ArrayList<>();

    /**
     * ソート済みオプションのキャッシュ
     */
    private List<RegexOption> sortedOptions;

    /**
     * 日本語と英語をわける
     */
    private List<String> splitJapaneseEnglish(String text) {
        String[] dividedText = text.split("");
        List<String> createdText = new ArrayList<>();
        StringBuilder tmpText = new StringBuilder();
        boolean en = false;

        for (String txt : dividedText) {
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
     * @param priority     優先度
     * @param replacedText 置き換えテキスト
     * @param testFunction 置き換え判定用関数
     * @return このインスタンス
     */
    public RegexUtil addOption(int priority, String replacedText, Function<String, Boolean> testFunction) {
        this.optionList.add(new RegexOption(priority, replacedText, testFunction));
        this.sortedOptions = null;
        return this;
    }

    /**
     * ソート済みオプションリストを取得
     *
     * @return ソート済みオプションリスト
     */
    private List<RegexOption> getSortedOptions() {
        if (sortedOptions == null) {
            sortedOptions = optionList.stream()
                    .sorted(Comparator.comparingInt(RegexOption::priority))
                    .toList();
        }
        return sortedOptions;
    }

    /**
     * テキストを置き換える
     *
     * @param text 置き換え対象のテキスト
     * @return 置き換え済みテキスト
     */
    public String replaceText(String text) {
        List<String> texts = splitJapaneseEnglish(text);
        List<RegexOption> sorted = getSortedOptions();
        List<String> createdText = new ArrayList<>();

        LOOP:
        for (String txt : texts) {
            for (RegexOption ops : sorted) {
                if (ops.testFunction().apply(txt)) {
                    createdText.add(ops.replacedText());
                    continue LOOP;
                }
            }
            createdText.add(txt);
        }

        return String.join("", createdText);
    }

    /**
     * オプション定義
     *
     * @param priority     優先度
     * @param replacedText 置き換えテキスト
     * @param testFunction 置き換え判定用関数
     * @author shiro8613
     */
    private record RegexOption(int priority, String replacedText, Function<String, Boolean> testFunction) {
    }
}
