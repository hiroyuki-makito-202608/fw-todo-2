# DB設計書

## 1. 概要

- データベース: MySQL
- テーブル数: 1
- テーブル名: `todos`
- 1行で、やること1件を表す。

## 2. テーブル定義

### `todos`

| カラム | 型 | NULL | 初期値・制約 | 内容 |
|---|---|---|---|---|
| `id` | `BIGINT` | 不可 | 主キー、自動採番 | 1件を識別する番号 |
| `title` | `VARCHAR(255)` | 不可 | なし | やること |
| `detail` | `VARCHAR(255)` | 可 | なし | メモ |
| `category` | `VARCHAR(255)` | 不可 | なし | ジャンル。デザイン、マーケティング、プログラミング、資格、就職活動のいずれか |
| `priority` | `INT` | 不可 | `2` | 優先度。`1` は高、`2` は中、`3` は低 |
| `due_date` | `DATE` | 可 | なし | 期限 |
| `completed` | `BOOLEAN` | 不可 | `0` | 完了状態。`1` は完了、`0` は未完了 |
| `created_at` | `DATETIME` | 不可 | `CURRENT_TIMESTAMP` | 登録日時。MySQLが自動設定する |
| `updated_at` | `DATETIME` | 不可 | `CURRENT_TIMESTAMP`、更新時に自動更新 | 最終更新日時。MySQLが自動設定・更新する |

`NULL` は値が未設定であることを表す。可の場合は値を空にできる。

### 主キー・自動設定

- `id` は主キー（各行を区別するための一意な番号）であり、自動で1ずつ増える。
- `created_at` は登録時にMySQLが現在日時を設定する。
- `updated_at` は登録時に現在日時を設定し、その行が更新されるたびにMySQLが現在日時へ更新する。
- `priority` を指定せず登録した場合は `2`（中）とする。
- `completed` を指定せず登録した場合は `0`（未完了）とする。MySQLの`BOOLEAN`では`0`が未完了、`1`が完了を表す。

## 3. 要件とカラムの対応

| 要件 | 対応するカラム | 対応内容 |
|---|---|---|
| やることを登録・編集・一覧表示する | `title` | やることの文字列を保存する。最大255文字、必須 |
| やることの名前で検索する | `title` | 入力した文字列を含むかどうかの検索に使う |
| メモを登録・編集する | `detail` | メモを保存する。最大255文字、任意 |
| ジャンルを選択・表示・検索する | `category` | 指定された5つのジャンルのいずれかを保存する。必須 |
| 優先度を登録・編集・表示する | `priority` | `1`（高）、`2`（中）、`3`（低）を保存する。必須、初期値は`2` |
| 期限を登録・編集・表示し、並び替える | `due_date` | 期限を保存する。任意。期限順の並び替えに使う |
| 完了状態を編集・表示する | `completed` | 完了なら`1`、未完了なら`0`を保存する。初期値は`0` |
| 1件を識別し、編集・削除の対象にする | `id` | 各行を識別する自動採番の番号 |
| 登録日時を記録する | `created_at` | 登録時に自動設定する |
| 最終更新日時を記録する | `updated_at` | 登録時と更新時に自動設定する |

## 4. 申し送り事項：操作記録

登録・編集・削除の操作時には、操作の種類と対象の`id`を記録し、`title`と`detail`の内容は記録しないことが要件である。

このアプリで使用するテーブルは要件どおり`todos`の1つだけとするため、操作記録用のテーブルは追加しない。操作記録の保存先と実装方法は未決定であり、後続作業への申し送り事項とする。

## 5. DDL

DDL（テーブルを作成するSQL文）は次のとおり。

```sql
CREATE TABLE todos (
    id BIGINT NOT NULL AUTO_INCREMENT,
    title VARCHAR(255) NOT NULL,
    detail VARCHAR(255) NULL,
    category VARCHAR(255) NOT NULL,
    priority INT NOT NULL DEFAULT 2,
    due_date DATE NULL,
    completed BOOLEAN NOT NULL DEFAULT 0,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (id)
);
```

## 6. 設計上の補足

- ジャンルは画面で指定された5種類から選ぶ。元資料にデータベース側で種類を制限する制約の指定はないため、このDDLでは追加の制約を設けない。
- 優先度の選択肢や文字数などの入力チェックは、画面・アプリ側の要件として扱う。
- 画面の配置やテンプレートの設計は、このDB設計書の対象外とする。
