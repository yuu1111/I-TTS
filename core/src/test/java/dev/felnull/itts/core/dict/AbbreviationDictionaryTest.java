package dev.felnull.itts.core.dict;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;

import static org.junit.jupiter.api.Assertions.*;

/**
 * AbbreviationDictionary のテストクラス
 * 特にURL正規表現のUnicode文字対応をテスト
 */
public class AbbreviationDictionaryTest {

    private AbbreviationDictionary dictionary;
    private static final long TEST_GUILD_ID = 123456789L;

    @BeforeEach
    void setUp() {
        dictionary = new AbbreviationDictionary();
    }

    @Test
    @DisplayName("基本的なURLの省略")
    void testBasicUrlAbbreviation() {
        String input = "このサイト https://example.com をご覧ください";
        String result = dictionary.apply(input, TEST_GUILD_ID);
        assertEquals("このサイト ユーアルエルショウリャク をご覧ください", result);
    }

    @Test
    @DisplayName("日本語を含むURLの省略")
    void testJapaneseUrlAbbreviation() {
        String input = "日本語の記事: https://ja.wikipedia.org/wiki/日本語 を参照";
        String result = dictionary.apply(input, TEST_GUILD_ID);
        assertEquals("日本語の記事: ユーアルエルショウリャク を参照", result);
    }

    @Test
    @DisplayName("@記号を含むYouTubeのURLの省略")
    void testYoutubeAtSymbolUrlAbbreviation() {
        String input = "チャンネルはこちら https://youtube.com/@ユーザー名/videos です";
        String result = dictionary.apply(input, TEST_GUILD_ID);
        assertEquals("チャンネルはこちら ユーアルエルショウリャク です", result);
    }

    @Test
    @DisplayName("括弧を含むWikipediaのURLの省略")
    void testWikipediaParenthesesUrlAbbreviation() {
        String input = "詳細は https://ja.wikipedia.org/wiki/Discord_(ソフトウェア) を見てください";
        String result = dictionary.apply(input, TEST_GUILD_ID);
        assertEquals("詳細は ユーアルエルショウリャク を見てください", result);
    }

    @Test
    @DisplayName("フラグメント識別子を含むURLの省略")
    void testFragmentUrlAbbreviation() {
        String input = "地理情報: https://ja.wikipedia.org/wiki/日本#地理 について";
        String result = dictionary.apply(input, TEST_GUILD_ID);
        assertEquals("地理情報: ユーアルエルショウリャク について", result);
    }

    @Test
    @DisplayName("句読点で終わるURLの正しい処理")
    void testUrlEndingWithPunctuation() {
        // URLの後に句読点がある場合、句読点はURLに含まれない
        String input = "詳細は https://example.com をご覧ください。";
        String result = dictionary.apply(input, TEST_GUILD_ID);
        assertEquals("詳細は ユーアルエルショウリャク をご覧ください。", result);
        
        String input2 = "サイト（https://example.com）を確認";
        String result2 = dictionary.apply(input2, TEST_GUILD_ID);
        assertEquals("サイト（ユーアルエルショウリャク）を確認", result2);
    }

    @Test
    @DisplayName("複数のURLを含むテキストの省略")
    void testMultipleUrls() {
        String input = "サイト1: https://example.com と サイト2: https://ja.wikipedia.org/wiki/日本語 を参照";
        String result = dictionary.apply(input, TEST_GUILD_ID);
        assertEquals("サイト1: ユーアルエルショウリャク と サイト2: ユーアルエルショウリャク を参照", result);
    }

    @Test
    @DisplayName("FTPスキームのURLの省略")
    void testFtpUrlAbbreviation() {
        String input = "FTPサーバー: ftp://example.com/files にアクセス";
        String result = dictionary.apply(input, TEST_GUILD_ID);
        assertEquals("FTPサーバー: ユーアルエルショウリャク にアクセス", result);
    }

    @Test
    @DisplayName("中国語を含むURLの省略")
    void testChineseUrlAbbreviation() {
        String input = "中文页面: https://zh.wikipedia.org/wiki/中文 を見る";
        String result = dictionary.apply(input, TEST_GUILD_ID);
        assertEquals("中文页面: ユーアルエルショウリャク を見る", result);
    }

    @Test
    @DisplayName("韓国語を含むURLの省略")
    void testKoreanUrlAbbreviation() {
        String input = "한국어 페이지: https://ko.wikipedia.org/wiki/한국어 확인";
        String result = dictionary.apply(input, TEST_GUILD_ID);
        assertEquals("한국어 페이지: ユーアルエルショウリャク 확인", result);
    }

    @Test
    @DisplayName("コードブロックの省略")
    void testCodeBlockAbbreviation() {
        String input = "コードは以下です:\n```\nSystem.out.println(\"Hello\");\n```\n以上です";
        String result = dictionary.apply(input, TEST_GUILD_ID);
        assertEquals("コードは以下です:\nコードブロックショウリャク\n以上です", result);
    }

    @Test
    @DisplayName("ドメインのみの省略")
    void testDomainAbbreviation() {
        String input = "example.com にアクセス";
        String result = dictionary.apply(input, TEST_GUILD_ID);
        assertEquals("ドメインショウリャク にアクセス", result);
    }

    @Test
    @DisplayName("IPv4アドレスの省略")
    void testIpv4Abbreviation() {
        String input = "サーバーのIPは 192.168.1.1 です";
        String result = dictionary.apply(input, TEST_GUILD_ID);
        assertEquals("サーバーのIPは アイピーブイフォーショウリャク です", result);
    }

    @Test
    @DisplayName("IPv6アドレスの省略")
    void testIpv6Abbreviation() {
        String input = "IPv6アドレス: 2001:db8::1 を使用";
        String result = dictionary.apply(input, TEST_GUILD_ID);
        assertEquals("IPv6アドレス: アイピーブイロクショウリャク を使用", result);
    }
}