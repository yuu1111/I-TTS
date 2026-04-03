package dev.felnull.itts.core.dict;

import com.google.common.collect.ImmutableMap;
import org.apache.commons.lang3.ArrayUtils;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Unmodifiable;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 単位辞書
 *
 * @author MORIMORI0317
 */
public class UnitDictionary implements Dictionary {

    /**
     * 英字とハイフンの正規表現
     */
    private static final Pattern ALPHABETS_REGEX = Pattern.compile("[a-zA-Z-]+");

    /**
     * 英字の正規表現
     */
    private static final Pattern ALPHABET_REGEX = Pattern.compile("[a-zA-Z]+");

    /**
     * 単位に置き換える文字のひとつ前の文字を表す正規表現
     */
    private static final Pattern UNIT_PREFIX_REGEX = createPrefixAndUnitPattern();

    /**
     * 数字の正規表現
     */
    private static final Pattern NUMBERS_REGEX = Pattern.compile("\\d+");

    /**
     * 通常の大きい接頭辞
     */
    private static final Prefix[] NORMAL_UP_PREFIX = {
            Prefix.KILO,
            Prefix.MEGA,
            Prefix.GIGA,
            Prefix.TERA,
            Prefix.PETA,
            Prefix.EXA,
            Prefix.ZETTA,
            Prefix.YOTTA,
            Prefix.RONNA,
            Prefix.QUETTA
    };

    /**
     * 通常の小さい接頭辞
     */
    private static final Prefix[] NORMAL_DOWN_PREFIX = {
            Prefix.CENTI,
            Prefix.MILLI,
            Prefix.MICRO,
            Prefix.NANO,
            Prefix.PICO,
            Prefix.FEMTO,
            Prefix.ATTO,
            Prefix.ZEPTO,
            Prefix.YOCTO,
            Prefix.RONTO,
            Prefix.QUECTO
    };

    /**
     * 通常の全ての接頭辞
     */
    private static final Prefix[] NORMAL_ALL_PREFIX = ArrayUtils.addAll(NORMAL_UP_PREFIX, NORMAL_DOWN_PREFIX);

    private static Pattern createPrefixAndUnitPattern() {
        StringBuilder lastUnits = new StringBuilder();
        StringBuilder preOrUnitMiddle = new StringBuilder();
        for (Unit unit : Unit.values()) {
            String lst = unit.word.substring(unit.word.length() - 1);
            lastUnits.append(lst.toLowerCase(Locale.ROOT)).append(lst.toUpperCase(Locale.ROOT));

            String middle = unit.word.substring(0, unit.word.length() - 1);
            preOrUnitMiddle.append(middle.toLowerCase(Locale.ROOT)).append(middle.toUpperCase(Locale.ROOT));
        }

        for (Prefix prefix : Prefix.values()) {
            if (prefix.word == null || prefix.read == null) {
                continue;
            }

            Matcher mc = ALPHABET_REGEX.matcher(prefix.word);
            if (mc.matches()) {
                preOrUnitMiddle.append(prefix.word.toLowerCase(Locale.ROOT)).append(prefix.word.toUpperCase(Locale.ROOT));
            } else {
                preOrUnitMiddle.append(prefix.word);
            }
        }

        return Pattern.compile("\\d+[" + preOrUnitMiddle + "]*[" + lastUnits + "]");
    }

    @Override
    public @NotNull String apply(@NotNull String text, long guildId) {
        return UNIT_PREFIX_REGEX.matcher(text).replaceAll(matchResult -> {
            String lst = null;
            if (matchResult.end() < text.length()) {
                lst = String.valueOf(text.charAt(matchResult.end()));
            }

            return replaceUnitAndPrefix(matchResult.group(), lst);
        });
    }

    @Override
    public boolean isBuiltIn() {
        return true;
    }

    @Override
    public @NotNull String getName() {
        return "単位辞書";
    }

    @Override
    public @NotNull String getId() {
        return "unit";
    }

    private String replaceUnitAndPrefix(String text, String after) {
        if (after != null && ALPHABETS_REGEX.matcher(after).matches()) {
            return text;
        }

        Matcher nmc = NUMBERS_REGEX.matcher(text);

        if (!nmc.find()) {
            return text;
        }

        String number = nmc.group();
        String only = text.substring(number.length());
        Unit unit = Unit.getEndUnit(only);

        if (unit == null) {
            return text;
        }

        String unitStr = only.substring(only.length() - unit.word.length());
        boolean big = Character.isUpperCase(unitStr.charAt(0));
        String prefixStr = only.substring(0, only.length() - unitStr.length());

        if (prefixStr.isEmpty()) {
            return number + unit.read;
        }

        Prefix prefix = Prefix.getPrefix(prefixStr, unit, big);

        if (prefix == null) {
            return text;
        }

        return number + prefix.read + unit.read;
    }

    @Override
    public @NotNull @Unmodifiable Map<String, String> getShowInfo(long guildId) {
        ImmutableMap.Builder<String, String> builder = new ImmutableMap.Builder<>();

        StringBuilder upsbw = new StringBuilder();
        StringBuilder upsbr = new StringBuilder();
        StringBuilder dosbw = new StringBuilder();
        StringBuilder dosbr = new StringBuilder();

        for (Prefix pre : Prefix.values()) {
            if (pre.getWord() == null || pre.getRead() == null) {
                continue;
            }

            if (pre.isUp()) {
                upsbw.append(pre.getWord()).append(",");
                upsbr.append(pre.getRead()).append(",");
            } else {
                dosbw.append(pre.getWord()).append(",");
                dosbr.append(pre.getRead()).append(",");
            }
        }

        upsbw.deleteCharAt(upsbw.length() - 1);
        upsbr.deleteCharAt(upsbr.length() - 1);
        dosbw.deleteCharAt(dosbw.length() - 1);
        dosbr.deleteCharAt(dosbr.length() - 1);

        builder.put(upsbw.toString(), upsbr.toString());
        builder.put(dosbw.toString(), dosbr.toString());

        StringBuilder unitsbw = new StringBuilder();
        StringBuilder unitsbr = new StringBuilder();

        for (Unit unit : Unit.values()) {
            unitsbw.append(unit.getWord()).append(",");
            unitsbr.append(unit.getRead()).append(",");
        }

        unitsbw.deleteCharAt(unitsbw.length() - 1);
        unitsbr.deleteCharAt(unitsbr.length() - 1);

        builder.put(unitsbw.toString(), unitsbr.toString());

        return builder.build();
    }

    @Override
    public int getDefaultPriority() {
        return 3;
    }

    /**
     * 単位の列挙型
     *
     * @author MORIMORI0317
     * @see <a href="https://www.mikipulley.co.jp/JP/Services/Tech_data/tech01.html">参考サイト</a>
     */
    private enum Unit {
        /**
         * グラム
         */
        GRAM("g", "ぐらむ", NORMAL_ALL_PREFIX),

        /**
         * バイト
         */
        BYTE("b", "ばいと", NORMAL_UP_PREFIX),

        /**
         * メートル
         */
        METER("m", "めーとる", NORMAL_ALL_PREFIX),

        /**
         * 秒
         */
        SECOND("s", "秒", NORMAL_DOWN_PREFIX),

        /**
         * アンペア
         */
        AMPERE("a", "あんぺあ", NORMAL_ALL_PREFIX),

        /**
         * モル
         */
        MOLE("mol", "もる", NORMAL_ALL_PREFIX),

        /**
         * カンデラ
         */
        CANDELA("cd", "かんでら", NORMAL_ALL_PREFIX),

        /**
         * ラジアン
         */
        RADIAN("rad", "らじあん", NORMAL_ALL_PREFIX),

        /**
         * ステラジアン
         */
        STERADIAN("sr", "すてらじあん", NORMAL_ALL_PREFIX),

        /**
         * へるつ
         */
        HERTZ("Hz", "へるつ", ArrayUtils.addAll(NORMAL_ALL_PREFIX, Prefix.DECA, Prefix.HECTO)),

        /**
         * ニュートン
         */
        NEWTON("n", "にゅーとん", NORMAL_ALL_PREFIX),

        /**
         * パスカル
         */
        PASCAL("Pa", "ぱすかる", NORMAL_ALL_PREFIX),

        /**
         * ジュール
         */
        JOULE("J", "じゅーる", NORMAL_ALL_PREFIX),

        /**
         * ワット
         */
        WATT("W", "わっと", NORMAL_ALL_PREFIX),

        /**
         * クーロン
         */
        COULOMB("C", "くーろん", NORMAL_ALL_PREFIX),

        /**
         * ボルト
         */
        VOLT("V", "ぼると", NORMAL_ALL_PREFIX),

        /**
         * ファラド
         */
        FARAD("F", "ふぁらど", NORMAL_ALL_PREFIX),

        /**
         * オーム
         */
        OHM("Ω", "おーむ", NORMAL_ALL_PREFIX),

        /**
         * ジーメンス
         */
        SIEMENS("S", "じーめんす", NORMAL_ALL_PREFIX),

        /**
         * ウェーバ
         */
        WEBER("Wb", "うぇーば", NORMAL_ALL_PREFIX),

        /**
         * テスラ
         */
        TESLA("T", "てすら", NORMAL_ALL_PREFIX),

        /**
         * ヘンリー
         */
        HENRY("H", "へんりー", NORMAL_ALL_PREFIX),

        /**
         * 度
         */
        CELSIUS("°C", "ど"),

        /**
         * ルーメン
         */
        LUMEN("lm", "るーめん", NORMAL_ALL_PREFIX),

        /**
         * ルクス
         */
        LUX("lx", "るくす", NORMAL_ALL_PREFIX),

        /**
         * ベクレル
         */
        BECQUEREL("Bq", "べくれる", NORMAL_ALL_PREFIX),

        /**
         * グレイ
         */
        GRAY("Gy", "ぐれい", NORMAL_ALL_PREFIX),

        /**
         * シーベルト
         */
        SIEVERT("Sv", "しーべると", NORMAL_ALL_PREFIX),

        /**
         * カタール
         */
        KATAL("kat", "かたーる", NORMAL_ALL_PREFIX),

        /**
         * ケルビン
         */
        KELVIN("k", "けるびん"),

        /**
         * トン
         */
        TONS("t", "とん"),

        /**
         * リットル
         */
        LITER("l", "りっとる", ArrayUtils.addAll(NORMAL_ALL_PREFIX, Prefix.DECA, Prefix.DECI));

        /**
         * 置き換える文字
         */
        private final String word;

        /**
         * 読み
         */
        private final String read;

        /**
         * 単位の接頭辞
         */
        private final UnitDictionary.Prefix[] prefixes;

        /**
         * 置き換え対象正規表現
         */
        private final Pattern pattern;

        Unit(String word, String read, Prefix... prefixes) {
            this.word = word;
            this.read = read;
            this.prefixes = prefixes;
            this.pattern = Pattern.compile(word);
        }

        /**
         * テキストの末尾に一致する単位を取得する
         *
         * @param text テキスト
         * @return 一致した単位、見つからなければnull
         */
        public static Unit getEndUnit(String text) {
            List<Unit> maches = new ArrayList<>();

            for (Unit unit : values()) {
                if (unit.isEndMache(text)) {
                    maches.add(unit);
                }
            }

            if (maches.isEmpty()) {
                return null;
            }

            if (maches.size() == 1) {
                return maches.get(0);
            }

            for (Unit unit : maches) {
                if (unit.word.equals(text)) {
                    return unit;
                }
            }

            return maches.get(0);
        }

        /**
         * 置き換え文字を取得
         *
         * @return 置き換え文字
         */
        public String getWord() {
            return word;
        }

        /**
         * 読みを取得
         *
         * @return 読み
         */
        public String getRead() {
            return read;
        }

        /**
         * 使用可能な接頭辞を取得
         *
         * @return 接頭辞の配列
         */
        public UnitDictionary.Prefix[] getPrefixes() {
            return prefixes;
        }

        /**
         * テキストの末尾がこの単位に一致するか判定する
         *
         * @param text テキスト
         * @return 一致すればtrue
         */
        public boolean isEndMache(String text) {
            if (findPrefix(text)) {
                return true;
            }

            text = ALPHABET_REGEX.matcher(text).replaceAll(n -> n.group().toLowerCase(Locale.ROOT));

            if (findPrefix(text)) {
                return true;
            }

            text = ALPHABET_REGEX.matcher(text).replaceAll(n -> n.group().toUpperCase(Locale.ROOT));

            return findPrefix(text);
        }

        private boolean findPrefix(String text) {
            Matcher m = pattern.matcher(text);
            int lstm = -1;
            while (m.find()) {
                lstm = m.end();
            }
            return lstm == text.length();
        }
    }

    /**
     * 単位の接頭辞
     *
     * @author MORIMORI0317
     * @see <a href="https://unit.aist.go.jp/nmij/info/SI_prefixes/index.html">参考サイト</a>
     */
    private enum Prefix {
        /**
         * デシ
         */
        DECI("d", "でし"),

        /**
         * センチ
         */
        CENTI("c", "せんち"),

        /**
         * ミリ
         */
        MILLI("m", "みり"),

        /**
         * マイクロ
         */
        MICRO("μ", "まいくろ"),

        /**
         * ナノ
         */
        NANO("n", "なの"),

        /**
         * ピコ
         */
        PICO("p", "ぴこ"),

        /**
         * フェムト
         */
        FEMTO("f", "ふぇむと"),

        /**
         * アト
         */
        ATTO("a", "あと"),

        /**
         * セプト
         */
        ZEPTO("z", "せぷと"),

        /**
         * ヨクト
         */
        YOCTO("y", "よくと"),

        /**
         * ロント
         */
        RONTO("r", "ろんと"),

        /**
         * クエクト
         */
        QUECTO("q", "くえくと"),

        /**
         * デカ
         */
        DECA("da", "でか", true),

        /**
         * ヘクト
         */
        HECTO("h", "へくと", true),

        /**
         * キロ
         */
        KILO("k", "きろ", true),

        /**
         * メガ
         */
        MEGA("m", "めが", true),

        /**
         * ギガ
         */
        GIGA("g", "ぎが", true),

        /**
         * テラ
         */
        TERA("t", "てら", true),

        /**
         * ペタ
         */
        PETA("p", "ぺた", true),

        /**
         * エクサ
         */
        EXA("e", "えくさ", true),

        /**
         * ヨタ
         */
        ZETTA("y", "よた", true),

        /**
         * ゼタ
         */
        YOTTA("z", "ぜた", true),

        /**
         * ロナ
         */
        RONNA("r", "ろな", true),

        /**
         * クエタ
         */
        QUETTA("q", "くえた", true);

        /**
         * 置き換える文字
         */
        private final String word;

        /**
         * 読み
         */
        private final String read;

        /**
         * 大きいかどうか
         */
        private final boolean up;

        Prefix(String word, String read, boolean up) {
            this.word = word;
            this.read = read;
            this.up = up;
        }

        Prefix(String word, String read) {
            this(word, read, false);
        }

        /**
         * テキストと単位から適切な接頭辞を取得する
         *
         * @param text テキスト
         * @param unit 単位
         * @param big  大文字かどうか
         * @return 接頭辞、見つからなければnull
         */
        public static Prefix getPrefix(String text, UnitDictionary.Unit unit, boolean big) {
            List<Prefix> prefixes = new ArrayList<>();
            for (Prefix prefix : unit.getPrefixes()) {
                if (prefix.isMache(text)) {
                    prefixes.add(prefix);
                }
            }

            if (prefixes.isEmpty()) {
                return null;
            }

            if (prefixes.size() == 1) {
                return prefixes.get(0);
            }

            boolean pbig = Character.isUpperCase(text.charAt(0));

            Prefix up = prefixes.stream().filter(Prefix::isUp).findFirst().orElseThrow();
            Prefix don = prefixes.stream().filter(n -> !n.isUp()).findFirst().orElseThrow();

            if (!big && pbig) {
                return up;
            }

            return don;
        }

        /**
         * 読みを取得
         *
         * @return 読み
         */
        public String getRead() {
            return read;
        }

        /**
         * 置き換え文字を取得
         *
         * @return 置き換え文字
         */
        public String getWord() {
            return word;
        }

        /**
         * 大きい接頭辞かどうか
         *
         * @return 大きい接頭辞ならtrue
         */
        public boolean isUp() {
            return up;
        }

        /**
         * テキストがこの接頭辞に一致するか判定する
         *
         * @param text テキスト
         * @return 一致すればtrue
         */
        public boolean isMache(String text) {
            return word.equalsIgnoreCase(text);
        }
    }
}
