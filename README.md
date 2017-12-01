# プロジェクトのインポート
1. 画面右上の<font color="green"> Clone or Download</font> → https...をコピー(右側のボタンで一発)
2. Eclipseのパッケージエクスプローラー(左画面)を右クリック → [インポート] → [Git] → [Gitからプロジェクト] → [URIの複製]
3. URIに1.でコピーしたアドレスを貼り付け(おそらく自動で入力される) 下側のメールアドレスとパスワードはGitHubのアカウントのものを入れる
4. masterにチェックが入っているのを確認して[次へ]
5. [ディレクトリ]をプロジェクトを保存したいディレクトリに変更 → [次へ]
6. [既存プロジェクトのインポート]にチェックを入れて[次へ] → [完了]

以上の操作で，プロジェクトpbl22-robot-05がコピーされる．

# ブランチの作成
コピーしたプロジェクトをそのまま編集すると，誰が編集したか分かりにくく，またコミットするたびに他の人の編集とマージしなければならない．
このため，各々のブランチを作成し，そこで編集した成果物をmasterにマージすることとする．以下でブランチの作成方法を説明する．

1. プロジェクトpbl22-robot-05を右クリック
2. [チーム] → [切り替え] → [新規ブランチ]
3. ブランチ名を好きな名前に(自分の名前がよい) → [完了]

これにより，以降の作業がmasterと分離される．各自の作業内容は完成次第masterとマージする．

# データ構造の説明
データ構造RobotData,RobotDataListの説明はJavadocで記述した．
(プロジェクトのディレクトリ)/doc/index.htmlにHTML変換したものがある．

# コミットとプッシュ
まず，以下の作業を行う．
1. Eclipse画面上の[ウィンドウ] → [設定]
2. [チーム] → [Git] → [コミット]
3. [コミット・ダイアログの代わりにコミットするためのステージング・ビューを表示]のチェックを外す → [適用して閉じる]

書いたコードはコミットすることで初めてローカルリポジトリに反映される．また変更したローカルリポジトリをプッシュすることでリモートリポジトリ(ここではこのリポジトリ)に反映される．
以下でコミットとプッシュの方法を説明する．なおコミットはある時点まで作成できたらそのたびに行うことが望ましいが，今回は規模が小さいため自分の作業が完了するまでコミットしなくてもよい．

1. プロジェクトを右クリック
2. [チーム] → [コミット]
3. コミット・メッセージを入力する．これは，このコミットで何が行われたかを説明するためのメッセージ．本来は非常に重要なものだが，今回は適当でよい．
4. [コミットとプッシュ]

成功したというダイアログが出ればコミットおよびプッシュ完了．