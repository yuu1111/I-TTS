# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## プロジェクト概要

I-TTS（Integration TTS）は、Discord向けの多機能テキスト読み上げBOTです。VOICEVOX、COEIROINK、SHAREVOX、VoiceTextなどの音声合成エンジンに対応しています。

## 必須コマンド

### ビルド
```bash
# 実行可能JARファイルのビルド
./gradlew shadowJar

# 通常のビルド（テスト含む）
./gradlew build

# クリーンビルド
./gradlew clean build
```

### テスト実行
```bash
# 全テスト実行
./gradlew test

# 特定モジュールのテスト
./gradlew :core:test
./gradlew :selfhost:test
```

### コード品質チェック
```bash
# Checkstyleによるコード規約チェック
./gradlew checkstyle

# 特定モジュールのCheckstyle
./gradlew :core:checkstyle
./gradlew :selfhost:checkstyle
```

### 開発時の実行
```bash
# IDEからの実行（IntelliJ IDEA推奨）
# メインクラス: dev.felnull.itts.Main
# VM引数: -Xms1G -Xmx1G
# 作業ディレクトリ: $ProjectDir$/.run

# コマンドラインからの実行
java -jar build/libs/itts-selfhost-*.jar
```

## アーキテクチャ概要

### モジュール構成

#### core モジュール
BOTの中核機能を実装。主要コンポーネント：

- **ITTSRuntime**: アプリケーション全体の管理・初期化
- **Bot**: Discord JDAとの統合、イベント処理
- **VoiceManager**: 音声タイプの管理
- **TTSManager**: テキスト読み上げ処理の管理
- **ConfigManager**: 設定ファイルの管理
- **SaveDataManager**: サーバー・ユーザーデータの永続化
- **DictionaryManager**: 読み上げ辞書の管理
- **CacheManager**: 音声データのキャッシュ管理

#### selfhost モジュール
セルフホスト版の実装。ShadowJarで実行可能JARを生成：

- **Main**: エントリーポイント
- **SelfHostITTSRuntimeContext**: セルフホスト用のランタイムコンテキスト
- **ConfigImpl系**: 設定ファイルの具体的な実装
- **SaveData系**: データ永続化の具体的な実装

### 音声合成エンジン統合

#### VOICEVOX系（VOICEVOX、COEIROINK、SHAREVOX）
- **VoicevoxManager**: エンジン管理とAPI通信
- **VoicevoxBalancer**: 複数エンジンURL間の負荷分散
- **VoicevoxSpeaker/Style**: 話者とスタイルの管理
- HTTP APIを通じて外部エンジンと通信（ユーザーが事前起動必要）

#### VoiceText
- **VoiceTextManager**: VoiceText Web API統合
- **VoiceTextSpeaker**: 話者管理
- APIキーによる認証が必要

### Discord コマンド実装

`core/src/main/java/dev/felnull/itts/core/discord/command/` 配下：
- スラッシュコマンドは`BaseCommand`を継承
- 各コマンドクラスで権限、引数、実行ロジックを定義
- 主要コマンド：JoinCommand、LeaveCommand、VoiceCommand、ConfigCommand、DictCommand

### 設定管理

初回起動時の動作：
1. 実行ディレクトリに`config.json5`を生成
2. BOTトークンが未設定のため停止
3. ユーザーが設定編集後、再起動で正常動作開始

設定ファイル形式：JSON5（コメント対応）
- グローバル設定：`config.json5`
- サーバー別設定：SaveDataManagerで管理

### データフロー

1. **Discord イベント受信**: JDA → Bot → DCEventListener
2. **コマンド処理**: BaseCommand実装クラスで処理
3. **テキスト読み上げ**: 
   - TTSManager → VoiceManager → 音声エンジン（Voicevox/VoiceText）
   - 音声データ取得 → VoiceAudioManager → LavaPlayerで再生
4. **キャッシュ**: CacheManagerでメモリキャッシュ管理

### 重要な設計パターン

- **マネージャーパターン**: 各機能をManagerクラスで管理
- **Factory/Builder**: 複雑なオブジェクト生成に使用
- **非同期処理**: CompletableFutureを多用
- **ExecutorService**: 用途別のスレッドプール管理
  - asyncWorkerExecutor: 一般的な非同期処理
  - httpWorkerExecutor: HTTP通信専用
  - heavyWorkerExecutor: 重い処理用（固定スレッド数）

### 依存関係の注意点

- **Java 17必須**: 言語機能とライブラリ互換性
- **JDA 5.2.1**: Discord API統合
- **LavaPlayer Fork 1.4.3**: 音声再生
- **Kuromoji**: 日本語形態素解析（読み仮名変換）

### デバッグとトラブルシューティング

- ログ設定：`selfhost/src/main/resources/log4j2.xml`
- 実行時VM引数で調整：`-Xms1G -Xmx1G`（メモリ不足時は増加）
- VOICEVOX系エンジン接続エラー：エンジンが起動しているか確認
- VoiceText APIエラー：APIキーとレート制限確認

### コード規約

Checkstyle設定（`config/checkstyle/checkstyle.xml`）：
- 文字コード：UTF-8
- 最大行長：170文字（警告レベル）
- タブ文字禁止
- package-info.javaにJavadoc必須
- FelNull開発用規約準拠

## Windows開発環境の注意事項

### 基本原則
- **PowerShell**: `powershell`ではなく`pwsh`（PowerShell Core）を使用し、常に`-Encoding UTF8`を指定
- **パス処理**: ハードコーディングされたパス区切り文字（`\`や`/`）を避け、Javaの`Path`や`File.separator`を使用
- **エンコーディング**: すべてのファイルをUTF-8（BOMなし）で統一、`StandardCharsets.UTF_8`を明示的に指定
- **ビルドツール**: Gradleラッパー（`gradlew.bat`）を使用してクロスプラットフォーム対応

### Git設定（必須）
```bash
git config core.autocrlf true      # 改行コード自動変換
git config core.quotepath false    # 日本語ファイル名対応
```

### Windows固有のGradleコマンド
```bash
# Windows環境でのビルド
gradlew.bat shadowJar

# Windows環境でのテスト
gradlew.bat test

# Windows環境でのCheckstyle
gradlew.bat checkstyle
```

### よくある問題への対処

| 症状 | 対処法 |
|------|--------|
| 日本語の文字化け | PowerShell Coreと`-Encoding UTF8`を使用、JavaのVMオプションに`-Dfile.encoding=UTF-8`を追加 |
| パスが見つからない | `Paths.get()`や`File.separator`を使用、絶対パスは`Path.toAbsolutePath()`で取得 |
| Gradleビルドエラー | `gradlew.bat`を使用、`JAVA_HOME`環境変数が正しく設定されているか確認 |
| ファイルパス長エラー | Windows Long Path Supportを有効化（グループポリシーで設定） |
| VOICEVOX接続エラー | Windows Defenderやファイアウォールでポートがブロックされていないか確認 |
| 文字コード問題 | IntelliJ IDEAのFile Encodings設定をUTF-8に統一 |

### 重要な注意点
- Windows環境では常にクロスプラットフォーム対応を意識してコードを書く
- ファイル操作時は必ず`StandardCharsets.UTF_8`でエンコーディングを明示的に指定
- パス操作は`java.nio.file.Path`と`java.nio.file.Paths`を使用
- 外部プロセス実行時は`ProcessBuilder`を使用してプラットフォーム差異を吸収
- JSON5ファイル（`config.json5`）の編集時はBOM無しUTF-8対応エディタを使用