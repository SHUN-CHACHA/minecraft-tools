# SHUN-CHA's Minecraft Tools

このリポジトリには、複数のMinecraft MOD/プラグイン/ツールのソースコードをまとめています。

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