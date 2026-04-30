# CLAUDE.md — Claude Code 必須ルール

このファイルはClaude Codeがセッション開始時に読み込む。**全ルールは必須**。例外はない。

---

## 1. イシューファースト — 必ずイシューを先に作る

実装・修正・リファクタリングを問わず、コードを書く前に必ずGitHubイシューを作成すること。

```bash
# 既存イシューを確認
gh issue list --state open

# イシューがなければ作成（必ずラベルを付ける）
gh issue create --title "feat: カードに期限日を追加" --label "feature"
```

発行されたイシュー番号を記録する。以降の作業はすべてこの番号に紐づく。

**イシュー番号なしでコードを書くことは禁止。**

---

## 2. ブランチ命名規則

イシュー作成後、すぐに `main` から作業ブランチを切る。

```
<type>/issue-<番号>-<短い説明>
```

| type | 用途 |
|------|------|
| `feature` | 新機能 |
| `fix` | バグ修正 |
| `chore` | リファクタリング・設定・依存関係 |
| `docs` | ドキュメントのみ |

**例:**
```
feature/issue-12-add-card-due-date
fix/issue-7-list-deletion-crash
chore/issue-3-update-gradle-wrapper
docs/issue-15-update-api-readme
```

```bash
git checkout main
git pull origin main
git checkout -b feature/issue-12-add-card-due-date
```

**イシュー番号を含まないブランチ名は禁止。**

---

## 3. コミットメッセージ形式

```
<type>: <要約（日本語可）>

<任意の本文>

Refs #<イシュー番号>
```

type: `feat` / `fix` / `docs` / `chore` / `refactor` / `test` / `style`

**例:**
```
feat: カードに期限日設定機能を追加

締め切り日をDatePickerで設定できるようにした。

Refs #12
```

- 1行目は72文字以内
- 作業ブランチ上の全コミットに `Refs #番号` を含める

---

## 4. mainへの直接pushは禁止

- `main` はGitHubのブランチ保護で直接pushが拒否される
- `.claude/settings.json` でも `git push origin main` はブロック済み
- 必ずfeatureブランチにpushする:

```bash
git push origin feature/issue-12-add-card-due-date
```

---

## 5. PR必須フロー

1. ブランチをoriginにpush
2. PRを作成（テンプレート `.github/PULL_REQUEST_TEMPLATE.md` を使用）:
   ```bash
   gh pr create --title "feat: カードに期限日設定機能を追加" --base main
   ```
3. PR本文に `Closes #<番号>` を含める（マージ時にイシューが自動クローズされる）
4. PRタイトルはコミット形式に合わせる: `<type>: <要約>`

---

## 6. 作業チェックリスト（毎タスク）

```
[ ] 1. gh issue list で既存イシューを確認
[ ] 2. gh issue create でイシューを作成、番号を記録
[ ] 3. git checkout main && git pull origin main
[ ] 4. git checkout -b <type>/issue-<番号>-<説明>
[ ] 5. 実装
[ ] 6. git add <特定ファイル>（git add -A は使わない）
[ ] 7. git commit（Refs #番号 を含める）
[ ] 8. git push origin <ブランチ名>
[ ] 9. gh pr create（Closes #番号 を含める）
[ ] 10. GitHub上でPRをマージ
```

---

## 7. 禁止事項

| 禁止コマンド | 理由 |
|---|---|
| `git push origin main` | ブランチ保護 + settings.json でブロック済み |
| `git push --force` / `git push -f` | settings.json でブロック済み |
| `git reset --hard` | settings.json でブロック済み |
| `git branch -D` | settings.json でブロック済み |
| イシューなしで実装開始 | このルールに違反 |
| `.env` やシークレットのコミット | セキュリティリスク |

---

## 8. プロジェクト構成

```
TaskManagement/
├── frontend/          # React 19 + Vite（ポート 5173）
│   └── src/
├── backend/           # Spring Boot 3.4 + Java 21 + PostgreSQL（ポート 8080）
│   └── src/
├── docker-compose.yml
└── docs/
```

- フロントエンド起動: `cd frontend && npm run dev`
- バックエンド起動: `cd backend && ./gradlew bootRun`
- DB起動: `docker compose up -d`
