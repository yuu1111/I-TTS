package dev.felnull.itts.core.dict;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Disabled;

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
    @DisplayName("言語指定付きコードブロックの省略")
    void testCodeBlockWithLanguage() {
        String input = "Javaコード:\n```java\npublic class Main {\n    public static void main(String[] args) {\n        System.out.println(\"Hello\");\n    }\n}\n```\n終了";
        String result = dictionary.apply(input, TEST_GUILD_ID);
        assertEquals("Javaコード:\nコードブロックショウリャク\n終了", result);
    }

    @Test
    @DisplayName("複数のコードブロックの省略")
    void testMultipleCodeBlocks() {
        String input = "最初のコード:\n```\ncode1\n```\n次のコード:\n```\ncode2\n```\n完了";
        String result = dictionary.apply(input, TEST_GUILD_ID);
        assertEquals("最初のコード:\nコードブロックショウリャク\n次のコード:\nコードブロックショウリャク\n完了", result);
    }

    @Test
    @DisplayName("コードブロック内にURLが含まれる場合")
    void testCodeBlockWithUrl() {
        String input = "例:\n```\n// URLを含むコメント\n// https://example.com\nString url = \"https://api.example.com\";\n```\n説明";
        String result = dictionary.apply(input, TEST_GUILD_ID);
        assertEquals("例:\nコードブロックショウリャク\n説明", result);
    }

    @Test
    @DisplayName("インラインコード（バッククォート1つ）は対象外")
    void testInlineCodeNotAffected() {
        String input = "インラインコード `System.out.println()` は変換されない";
        String result = dictionary.apply(input, TEST_GUILD_ID);
        assertEquals("インラインコード `System.out.println()` は変換されない", result);
    }

    @Test
    @DisplayName("コードブロックの後にURLがある場合")
    void testCodeBlockFollowedByUrl() {
        String input = "コード:\n```\ncode\n```\n参考: https://example.com";
        String result = dictionary.apply(input, TEST_GUILD_ID);
        assertEquals("コード:\nコードブロックショウリャク\n参考: ユーアルエルショウリャク", result);
    }

    @Test
    @DisplayName("空のコードブロック")
    void testEmptyCodeBlock() {
        String input = "空のブロック:\n```\n```\n終了";
        String result = dictionary.apply(input, TEST_GUILD_ID);
        assertEquals("空のブロック:\nコードブロックショウリャク\n終了", result);
    }

    @Test
    @DisplayName("ネストされたバッククォート（異常ケース）")
    void testNestedBackticks() {
        String input = "異常:\n```\n```内部```\n```\n終了";
        String result = dictionary.apply(input, TEST_GUILD_ID);
        // 最初のマッチで全体が置換される
        assertEquals("異常:\nコードブロックショウリャク\n終了", result);
    }

    @Test
    @DisplayName("コードブロック内に日本語が含まれる場合")
    void testCodeBlockWithJapanese() {
        String input = "日本語コメント:\n```python\n# 日本語のコメント\nprint(\"こんにちは\")\n```\n以上";
        String result = dictionary.apply(input, TEST_GUILD_ID);
        assertEquals("日本語コメント:\nコードブロックショウリャク\n以上", result);
    }

    @Test
    @DisplayName("Markdown形式のコードブロック（4スペースインデント）は対象外")
    void testMarkdownIndentedCode() {
        String input = "Markdownコード:\n    code line 1\n    code line 2\n終了";
        String result = dictionary.apply(input, TEST_GUILD_ID);
        // 4スペースインデントは対象外
        assertEquals("Markdownコード:\n    code line 1\n    code line 2\n終了", result);
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

    @Test
    @DisplayName("クエリパラメータを含むURL")
    void testUrlWithQueryParameters() {
        String input = "検索結果: https://www.google.com/search?q=日本語&hl=ja&lr=lang_ja を見る";
        String result = dictionary.apply(input, TEST_GUILD_ID);
        assertEquals("検索結果: ユーアルエルショウリャク を見る", result);
    }

    @Test
    @DisplayName("アンカー（#）を含む複雑なURL")
    void testComplexUrlWithAnchor() {
        String input = "セクション: https://example.com/page?param=value#見出し について";
        String result = dictionary.apply(input, TEST_GUILD_ID);
        assertEquals("セクション: ユーアルエルショウリャク について", result);
    }

    @Test
    @DisplayName("ポート番号を含むURL")
    void testUrlWithPort() {
        String input = "開発サーバー: http://localhost:8080/api/test にアクセス";
        String result = dictionary.apply(input, TEST_GUILD_ID);
        assertEquals("開発サーバー: ユーアルエルショウリャク にアクセス", result);
    }

    @Test
    @DisplayName("URLエンコードされた日本語を含むURL")
    void testUrlEncodedJapanese() {
        String input = "エンコード済み: https://example.com/search?q=%E6%97%A5%E6%9C%AC%E8%AA%9E です";
        String result = dictionary.apply(input, TEST_GUILD_ID);
        assertEquals("エンコード済み: ユーアルエルショウリャク です", result);
    }

    @Test
    @DisplayName("絵文字を含むテキスト内のURL")
    void testUrlWithEmoji() {
        String input = "楽しい😊 https://example.com/fun 🎉サイト";
        String result = dictionary.apply(input, TEST_GUILD_ID);
        assertEquals("楽しい😊 ユーアルエルショウリャク 🎉サイト", result);
    }

    @Test
    @DisplayName("連続するURLの処理")
    void testConsecutiveUrls() {
        String input = "サイト1:https://example1.com サイト2:https://example2.com";
        String result = dictionary.apply(input, TEST_GUILD_ID);
        assertEquals("サイト1:ユーアルエルショウリャク サイト2:ユーアルエルショウリャク", result);
    }

    @Test
    @DisplayName("改行を含むテキスト内のURL")
    void testUrlWithNewlines() {
        String input = "一行目\nhttps://example.com\n三行目";
        String result = dictionary.apply(input, TEST_GUILD_ID);
        assertEquals("一行目\nユーアルエルショウリャク\n三行目", result);
    }

    @Test
    @DisplayName("タブ文字を含むテキスト内のURL")
    void testUrlWithTabs() {
        String input = "項目\thttps://example.com\t説明";
        String result = dictionary.apply(input, TEST_GUILD_ID);
        assertEquals("項目\tユーアルエルショウリャク\t説明", result);
    }

    @Test
    @DisplayName("全角スペースで区切られたURL")
    void testUrlWithFullWidthSpace() {
        String input = "サイト　https://example.com　です";
        String result = dictionary.apply(input, TEST_GUILD_ID);
        assertEquals("サイト　ユーアルエルショウリャク　です", result);
    }

    @Test
    @DisplayName("アンダースコアを含むURL")
    void testUrlWithUnderscore() {
        String input = "API: https://api.example.com/user_profile/get_data を呼び出す";
        String result = dictionary.apply(input, TEST_GUILD_ID);
        assertEquals("API: ユーアルエルショウリャク を呼び出す", result);
    }

    @Test
    @DisplayName("チルダを含むURL")
    void testUrlWithTilde() {
        String input = "ユーザーページ: https://example.com/~username/profile を表示";
        String result = dictionary.apply(input, TEST_GUILD_ID);
        assertEquals("ユーザーページ: ユーアルエルショウリャク を表示", result);
    }

    @Test
    @DisplayName("プラス記号を含むURL")
    void testUrlWithPlus() {
        String input = "検索: https://example.com/search?q=C%2B%2B+programming を実行";
        String result = dictionary.apply(input, TEST_GUILD_ID);
        assertEquals("検索: ユーアルエルショウリャク を実行", result);
    }

    @Test
    @DisplayName("感嘆符で終わるURL（除外されるべき）")
    void testUrlEndingWithExclamation() {
        String input = "すごいサイト https://example.com! を発見";
        String result = dictionary.apply(input, TEST_GUILD_ID);
        assertEquals("すごいサイト ユーアルエルショウリャク! を発見", result);
    }

    @Test
    @DisplayName("疑問符で終わるURL（除外されるべき）")
    void testUrlEndingWithQuestion() {
        String input = "このサイト https://example.com? 知ってる";
        String result = dictionary.apply(input, TEST_GUILD_ID);
        assertEquals("このサイト ユーアルエルショウリャク? 知ってる", result);
    }

    @Test
    @DisplayName("角括弧内のURL")
    void testUrlInSquareBrackets() {
        String input = "参考: [https://example.com] を確認";
        String result = dictionary.apply(input, TEST_GUILD_ID);
        assertEquals("参考: [ユーアルエルショウリャク] を確認", result);
    }

    @Test
    @DisplayName("引用符内のURL")
    void testUrlInQuotes() {
        String input = "URL「https://example.com」を開く";
        String result = dictionary.apply(input, TEST_GUILD_ID);
        assertEquals("URL「ユーアルエルショウリャク」を開く", result);
    }

    @Test
    @DisplayName("アラビア文字を含むURL")
    void testArabicUrl() {
        String input = "アラビア語: https://ar.wikipedia.org/wiki/العربية を参照";
        String result = dictionary.apply(input, TEST_GUILD_ID);
        assertEquals("アラビア語: ユーアルエルショウリャク を参照", result);
    }

    @Test
    @DisplayName("ロシア文字を含むURL")
    void testCyrillicUrl() {
        String input = "ロシア語: https://ru.wikipedia.org/wiki/Русский_язык です";
        String result = dictionary.apply(input, TEST_GUILD_ID);
        assertEquals("ロシア語: ユーアルエルショウリャク です", result);
    }

    @Test
    @DisplayName("HTTPSとHTTPが混在するテキスト")
    void testMixedHttpHttps() {
        String input = "安全: https://secure.com 非安全: http://insecure.com";
        String result = dictionary.apply(input, TEST_GUILD_ID);
        assertEquals("安全: ユーアルエルショウリャク 非安全: ユーアルエルショウリャク", result);
    }

    @Test
    @DisplayName("URLの直後に日本語が続く（スペースなし）")
    void testUrlDirectlyFollowedByJapanese() {
        String input = "サイトhttps://example.comです";
        String result = dictionary.apply(input, TEST_GUILD_ID);
        assertEquals("サイトユーアルエルショウリャクです", result);
    }

    @Test
    @DisplayName("セミコロンで終わるURL（除外されるべき）")
    void testUrlEndingWithSemicolon() {
        String input = "リンク: https://example.com; 次の項目";
        String result = dictionary.apply(input, TEST_GUILD_ID);
        assertEquals("リンク: ユーアルエルショウリャク; 次の項目", result);
    }

    @Test
    @DisplayName("コロンで終わるURL（除外されるべき）")
    void testUrlEndingWithColon() {
        String input = "以下のURL https://example.com: について";
        String result = dictionary.apply(input, TEST_GUILD_ID);
        assertEquals("以下のURL ユーアルエルショウリャク: について", result);
    }

    @Test
    @DisplayName("複雑なパスを持つGitHubのURL")
    void testComplexGitHubUrl() {
        String input = "リポジトリ: https://github.com/user-name/repo_name.git/blob/main/src/file.java#L123 を確認";
        String result = dictionary.apply(input, TEST_GUILD_ID);
        assertEquals("リポジトリ: ユーアルエルショウリャク を確認", result);
    }

    @Test
    @DisplayName("Base64エンコードされたデータURL（対象外のはず）")
    void testDataUrl() {
        String input = "画像: data:image/png;base64,iVBORw0KGgoAAAANSUhEUgAAAAUA を表示";
        String result = dictionary.apply(input, TEST_GUILD_ID);
        // data:スキームは対象外なので変換されない
        assertEquals("画像: data:image/png;base64,iVBORw0KGgoAAAANSUhEUgAAAAUA を表示", result);
    }

    @Test
    @Disabled("メールアドレスとドメインの区別は妥協")
    @DisplayName("mailtoリンク（対象外のはず）")
    void testMailtoLink() {
        String input = "連絡先: mailto:test@example.com にメール";
        String result = dictionary.apply(input, TEST_GUILD_ID);
        // mailtoスキームは対象外なので変換されない
        assertEquals("連絡先: mailto:test@example.com にメール", result);
    }

    @Test
    @DisplayName("javascriptスキーム（対象外のはず）")
    void testJavascriptScheme() {
        String input = "実行: javascript:alert('test') をクリック";
        String result = dictionary.apply(input, TEST_GUILD_ID);
        // javascriptスキームは対象外なので変換されない
        assertEquals("実行: javascript:alert('test') をクリック", result);
    }

    @Test
    @DisplayName("fileスキーム（対象外のはず）")
    void testFileScheme() {
        String input = "ローカル: file:///C:/Users/test/document.pdf を開く";
        String result = dictionary.apply(input, TEST_GUILD_ID);
        // fileスキームは対象外なので変換されない
        assertEquals("ローカル: file:///C:/Users/test/document.pdf を開く", result);
    }

    // === URLが含まれない記号テスト ===
    
    @Test
    @DisplayName("URLを含まない記号だらけの文章")
    void testSymbolsWithoutUrl() {
        String input = "記号テスト: !@#$%^&*()_+-=[]{}\\|;':\",./<>?～！＠＃＄％＾＆＊（）";
        String result = dictionary.apply(input, TEST_GUILD_ID);
        // URLが含まれないので変換されない
        assertEquals("記号テスト: !@#$%^&*()_+-=[]{}\\|;':\",./<>?～！＠＃＄％＾＆＊（）", result);
    }

    @Test
    @DisplayName("プログラミング記号を含む文章（URLなし）")
    void testProgrammingSymbolsWithoutUrl() {
        String input = "コード例: if (x > 0 && y < 10) { return x * y; } // コメント";
        String result = dictionary.apply(input, TEST_GUILD_ID);
        // URLが含まれないので変換されない
        assertEquals("コード例: if (x > 0 && y < 10) { return x * y; } // コメント", result);
    }

    @Test
    @DisplayName("数式記号を含む文章（URLなし）")
    void testMathSymbolsWithoutUrl() {
        String input = "数式: a² + b² = c² および x ≈ 3.14159 × 10⁵";
        String result = dictionary.apply(input, TEST_GUILD_ID);
        // URLが含まれないので変換されない
        assertEquals("数式: a² + b² = c² および x ≈ 3.14159 × 10⁵", result);
    }

    @Test
    @DisplayName("顔文字を含む文章（URLなし）")
    void testEmoticonWithoutUrl() {
        String input = "楽しい (^_^) / (T_T) 悲しい ＼(^o^)／ わーい";
        String result = dictionary.apply(input, TEST_GUILD_ID);
        // URLが含まれないので変換されない
        assertEquals("楽しい (^_^) / (T_T) 悲しい ＼(^o^)／ わーい", result);
    }

    @Test
    @Disabled("メールアドレスとドメインの区別は妥協")
    @DisplayName("メールアドレス風の文字列（@記号）（URLなし）")
    void testEmailLikeStringWithoutUrl() {
        String input = "連絡先: user@example.com または admin@localhost まで";
        String result = dictionary.apply(input, TEST_GUILD_ID);
        // URLではないので変換されない
        assertEquals("連絡先: user@example.com または admin@localhost まで", result);
    }

    // === URLと記号が混在するテスト ===
    
    @Test
    @DisplayName("URLと記号が混在する文章")
    void testUrlWithMixedSymbols() {
        String input = "参考資料: https://example.com (※重要!) & https://test.com [必読]";
        String result = dictionary.apply(input, TEST_GUILD_ID);
        assertEquals("参考資料: ユーアルエルショウリャク (※重要!) & ユーアルエルショウリャク [必読]", result);
    }

    @Test
    @DisplayName("URLとプログラミング記号が混在")
    void testUrlWithProgrammingSymbols() {
        String input = "API呼び出し: fetch('https://api.example.com') || axios.get('https://backup.com');";
        String result = dictionary.apply(input, TEST_GUILD_ID);
        assertEquals("API呼び出し: fetch('ユーアルエルショウリャク') || axios.get('ユーアルエルショウリャク');", result);
    }

    @Test
    @DisplayName("URLと数式記号が混在")
    void testUrlWithMathSymbols() {
        String input = "詳細: https://math.example.com/formula → x² + y² = z² を参照";
        String result = dictionary.apply(input, TEST_GUILD_ID);
        assertEquals("詳細: ユーアルエルショウリャク → x² + y² = z² を参照", result);
    }

    @Test
    @DisplayName("URLと顔文字が混在")
    void testUrlWithEmoticon() {
        String input = "見て！(^o^)/ https://funny.com ＼(^o^)／ 面白い！";
        String result = dictionary.apply(input, TEST_GUILD_ID);
        assertEquals("見て！(^o^)/ ユーアルエルショウリャク ＼(^o^)／ 面白い！", result);
    }

    @Test
    @Disabled("メールアドレスとドメインの区別は妥協")
    @DisplayName("URLとメールアドレスが混在")
    void testUrlWithEmail() {
        String input = "サイト: https://example.com 連絡: admin@example.com";
        String result = dictionary.apply(input, TEST_GUILD_ID);
        assertEquals("サイト: ユーアルエルショウリャク 連絡: admin@example.com", result);
    }

    @Test
    @DisplayName("複雑な記号環境でのURL")
    void testUrlInComplexSymbolEnvironment() {
        String input = "<<<https://example.com>>> | {https://test.com} & [https://link.com]";
        String result = dictionary.apply(input, TEST_GUILD_ID);
        assertEquals("<<<ユーアルエルショウリャク>>> | {ユーアルエルショウリャク} & [ユーアルエルショウリャク]", result);
    }

    @Test
    @DisplayName("矢印記号とURL")
    void testUrlWithArrows() {
        String input = "手順: https://step1.com → https://step2.com ⇒ https://step3.com";
        String result = dictionary.apply(input, TEST_GUILD_ID);
        assertEquals("手順: ユーアルエルショウリャク → ユーアルエルショウリャク ⇒ ユーアルエルショウリャク", result);
    }

    @Test
    @DisplayName("引用符の種類とURL")
    void testUrlWithVariousQuotes() {
        String input = "\"https://example1.com\" 'https://example2.com' `https://example3.com` \u201chttps://example4.com\u201d";
        String result = dictionary.apply(input, TEST_GUILD_ID);
        assertEquals("\"ユーアルエルショウリャク\" 'ユーアルエルショウリャク' `ユーアルエルショウリャク` \u201cユーアルエルショウリャク\u201d", result);
    }

    @Test
    @DisplayName("パイプ記号とURL")
    void testUrlWithPipe() {
        String input = "選択: https://option1.com | https://option2.com || https://option3.com";
        String result = dictionary.apply(input, TEST_GUILD_ID);
        assertEquals("選択: ユーアルエルショウリャク | ユーアルエルショウリャク || ユーアルエルショウリャク", result);
    }

    @Test
    @DisplayName("特殊な括弧とURL")
    void testUrlWithSpecialBrackets() {
        String input = "｛https://example1.com｝【https://example2.com】〔https://example3.com〕";
        String result = dictionary.apply(input, TEST_GUILD_ID);
        assertEquals("｛ユーアルエルショウリャク｝【ユーアルエルショウリャク】〔ユーアルエルショウリャク〕", result);
    }

    @Test
    @DisplayName("通貨記号とURL")
    void testUrlWithCurrencySymbols() {
        String input = "価格: $100 詳細: https://shop.com/product ¥1000 €50";
        String result = dictionary.apply(input, TEST_GUILD_ID);
        assertEquals("価格: $100 詳細: ユーアルエルショウリャク ¥1000 €50", result);
    }

    @Test
    @DisplayName("パーセント記号とURL")
    void testUrlWithPercent() {
        String input = "割引: 50% オフ！ https://sale.com/50%off 詳細を確認";
        String result = dictionary.apply(input, TEST_GUILD_ID);
        assertEquals("割引: 50% オフ！ ユーアルエルショウリャク 詳細を確認", result);
    }

    @Test
    @DisplayName("アスタリスクとURL")
    void testUrlWithAsterisk() {
        String input = "***重要*** https://important.com ***必読***";
        String result = dictionary.apply(input, TEST_GUILD_ID);
        assertEquals("***重要*** ユーアルエルショウリャク ***必読***", result);
    }

    @Test
    @DisplayName("スラッシュ記号の混在")
    void testUrlWithSlashes() {
        String input = "比較: A/B または https://example.com/path/to/page // コメント";
        String result = dictionary.apply(input, TEST_GUILD_ID);
        assertEquals("比較: A/B または ユーアルエルショウリャク // コメント", result);
    }

    @Test
    @DisplayName("バックスラッシュとURL")
    void testUrlWithBackslash() {
        String input = "パス: C:\\Users\\test と URL: https://example.com/path";
        String result = dictionary.apply(input, TEST_GUILD_ID);
        assertEquals("パス: C:\\Users\\test と URL: ユーアルエルショウリャク", result);
    }
}