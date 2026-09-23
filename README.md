# SHUN-CHA's Minecraft Tools

このリポジトリには、複数のMinecraft MOD/プラグイン/ツールのソースコードをまとめています。

**jarは「ダウンロード」からどうぞ。**

## Mods / Plugins / Tools

- [HeadFirework](./headfirework/)([ダウンロード](https://github.com/SHUN-CHACHA/minecraft-tools/releases)) — Fabric MOD版。火薬+染料+プレイヤーヘッドから専用の花火の星を作り、打ち上げると爆発の瞬間にそのプレイヤーの顔が浮かび上がる演出を追加します。シングルプレイヤーでもマルチサーバーでも動作し、マルチサーバーの場合はクライアント側の操作が必要なければサーバー側だけでも動作します。
  - バージョン: 正式版 v1.1.4(Minecraft 26.2) / 最新β v1.2.0-beta.2(Minecraft 26.3対応)
  - 使用説明書: [日本語](./headfirework/docs/HeadFirework_使用説明書_JP.md) / [English](./headfirework/docs/HeadFirework_User_Guide_EN.md)
  - クラフトレシピ集: [日本語](./headfirework/docs/HeadFirework_クラフトレシピ集.md) / [English](./headfirework/docs/HeadFirework_Crafting_Recipes_EN.md)
- [HeadFirework Paper Plugin](./headfirework-paper/)([ダウンロード](https://github.com/SHUN-CHACHA/minecraft-tools/releases)) — Paperサーバー用プラグイン版。同じ演出をサーバー側だけの導入で実現できます(参加者側の導入は不要)。
  - バージョン: 正式版 v1.1.0(Minecraft 26.2) / 最新β v1.1.1-beta.1(Minecraft 26.3向け、ビルド確認のみ・PaperがまだMinecraft 26.3の正式ビルドを提供していないため実機未テスト)
  - 使用説明書: [日本語](./headfirework-paper/docs/HeadFireworkPaper_使用説明書_JP.md) / [English](./headfirework-paper/docs/HeadFireworkPaper_User_Guide_EN.md)
  - クラフトレシピ集: [日本語](./headfirework-paper/docs/HeadFireworkPaper_クラフトレシピ集.md) / [English](./headfirework-paper/docs/HeadFireworkPaper_Crafting_Recipes_EN.md)
- [OreHighlighter](./orehighlighter/)([ダウンロード](https://github.com/SHUN-CHACHA/minecraft-tools/releases)) — 鉱石視認性向上ツール。リソースパックを自動生成するPyQt6製デスクトップアプリ(Windows用exe配布)。ブロックごとにレインボー/枠線/点滅などのエフェクトを個別設定でき、体力・満腹度HUDアイコンの編集も可能です。
  - バージョン: v2.1.4
  - 使用マニュアル: [こちら](./orehighlighter/使用マニュアル.html)
* [Everyone's Favorite Place (EFP)](https://github.com/SHUN-CHACHA/minecraft-tools/blob/main/efp)([ダウンロード](https://github.com/SHUN-CHACHA/minecraft-tools/releases)) — マルチサーバー参加者の拠点・設備座標を記録し、公開設定に応じて全員で共有できるPaperプラグイン。ディメンションごとに座標を管理し、座標マップ形式のGUIで一覧表示できます。
   * バージョン: v1.0.0(Minecraft 26.2対応)
   * 使用説明書: [日本語](https://github.com/SHUN-CHACHA/minecraft-tools/blob/main/efp/%E4%BD%BF%E7%94%A8%E8%AA%AC%E6%98%8E%E6%9B%B8.md)
- [Gamepad Lite](./gamepadlite/)([ダウンロード](https://github.com/SHUN-CHACHA/minecraft-tools/releases)) — Fabric MOD版。Minecraft 26.3で採用されたSDL3経由でXInput系ゲームコントローラーを認識し、移動・視点操作・ジャンプ/ダッシュ/スニーク・攻撃/使用・ホットバー切替に対応します。インベントリ等の画面ではLB/RBでスロットを選択しBで決定、Aで画面を閉じられます。ControlifyなどMinecraft 26.3に正式対応した既存コントローラーMODが登場するまでのつなぎとして、Modrinthには公開せずGitHub Releasesのみで配布しています。
  - バージョン: v26.3.1(Minecraft 26.3対応)
  - 対応コントローラー: ELECOM JC-U4113S(XBOXモード)で動作確認済み
- [MaLiLib IME](./malilibime/)([ダウンロード](https://github.com/SHUN-CHACHA/minecraft-tools/releases)) — Fabric MOD版。Litematica・MiniHUD・TweakerooなどMaLiLibを使うMODの入力欄で、日本語入力(IME)を使えるようにするアドオンMODです。Minecraft 26.1以降のバニラのIME対応がMaLiLibの独自画面では働かず、「日本語モードに切り替わらない」「変換中の文字が表示されない」状態になる問題を補います。MaLiLibへのビルド依存はなく、バニラの入力欄の動作は変えません(クライアント専用)。
  - バージョン: v1.1.0(Minecraft 26.2 / 26.3対応。1つのjarで両方に対応)
  - 前提MOD: Fabric Loader / MaLiLib(Litematica等のMaLiLib系MODと併用)
  - 動作確認: Windows。26.3はLitematica 0.29.0 / MaLiLib 0.30.1 / MiniHUD 0.41.1、26.2はLitematica 0.28.7 / MaLiLib 0.29.6 / MiniHUD 0.40.7
  - 使用説明書: [日本語](./malilibime/使用説明書.md)