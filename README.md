# hatakenote

家庭菜園・貸し農園向けの畑管理Androidアプリ

## 概要

区画ごとの作付け記録や作業履歴を管理し、連作障害の回避やリマインダー通知で効率的な畑仕事をサポートします。

## 主な機能

### 区画管理
- グリッド形式で畑全体のレイアウトを視覚的に表示
- 区画ごとの作物を色分けで識別（科別に色分け）

### 作付け・作業記録
- 作物、植えた日、収穫日、写真を記録
- 耕起・元肥・追肥などの作業履歴を管理
- 1区画に複数作物の混植にも対応

### 連作障害アラート
- 過去の作付け履歴をもとに相性の悪い作物を警告
- 科ごとの連作NG期間を考慮した提案

### リマインダー
- 追肥時期を自動計算して事前に通知

### カレンダー
- 月表示カレンダーで作業日を一覧
- 天気情報を表示（Open-Meteo API連携）

### AIアシスタント（Gemini連携）
- 育て方の質問・アドバイス
- 写真から病害虫を診断
- 連作障害を考慮した次の作物のおすすめ提案

## 動作環境

- Android 13 (API 33) 以上

## 開発環境

- Kotlin
- Gradle (Kotlin DSL)
- JDK 17 / 21

## セットアップ

以下のファイルは Git 管理外のため、clone 後に手動で配置してください。

### `local.properties`

```properties
GEMINI_API_KEY=...
FIREBASE_WEB_CLIENT_ID=...
```

### `app/google-services.json`

1. Firebase コンソールで HatakeNote プロジェクトを開く
2. 「プロジェクトの設定」→「マイアプリ」→ Android アプリ（`com.example.hatakenote`）を選択
3. `google-services.json` をダウンロードし、`app/google-services.json` に配置
4. デバッグビルドで Google ログインを使う場合は、`./gradlew :app:signingReport` で確認したデバッグ用 SHA-1 を同じアプリに登録

配置していないと `:app:processDebugGoogleServices` で `File google-services.json is missing` エラーになります。clone し直したときに困らないよう、リポジトリ外（パスワードマネージャー等）にも保管しておいてください。

## ビルド方法

JDK 17 または 21 を使用してください（JDK 24 以降では Gradle 8.13 が `Type T not present` で失敗します）。

```bash
./gradlew build
```

## ライセンス

TBD