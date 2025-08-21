package dev.felnull.itts.core.dict;

public class DebugAbbreviationTest {
    public static void main(String[] args) {
        AbbreviationDictionary dict = new AbbreviationDictionary();
        long guildId = 123456789L;
        
        // テストケース
        String[] testCases = {
            "日本語の記事: https://ja.wikipedia.org/wiki/日本語 を参照",
            "チャンネルはこちら https://youtube.com/@ユーザー名/videos です",
            "詳細は https://ja.wikipedia.org/wiki/Discord_(ソフトウェア) を見てください",
            "地理情報: https://ja.wikipedia.org/wiki/日本#地理 について",
            "中文页面: https://zh.wikipedia.org/wiki/中文 を見る",
            "한국어 페이지: https://ko.wikipedia.org/wiki/한국어 확인"
        };
        
        for (String test : testCases) {
            String result = dict.apply(test, guildId);
            System.out.println("入力: " + test);
            System.out.println("結果: " + result);
            System.out.println();
        }
    }
}