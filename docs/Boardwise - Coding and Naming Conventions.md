# Boardwise: Coding & Naming Conventions

*Works On My Machine™ — the single source of truth for how our code looks*

**Why this exists:** the client called our code "ugly and inconsistent." That's almost always a cross-cutting problem: branches named five different ways, files cased three different ways, commit messages that just say "fix," API routes that mix verb styles. This document fixes that. Everyone follows it, no exceptions, no personal preferences over the wire.

**The golden rule:** each language uses its own standard idiom internally, but everything that crosses a boundary (JSON, routes, branches, commits) follows one shared convention. We're not inventing anything new here, just writing down the standard each tool already expects and enforcing it automatically.

## 0. The one decision that fixes most of it: camelCase over the wire

All JSON request/response bodies and all field names use camelCase, everywhere, regardless of which backend serves it.

We already do this in most of the API (`totalEdits`, `rulebookId`, `editorId`, `versionAfter`, `committedAt`). This just makes it a rule so it doesn't drift.

| Layer | Internal idiom | Over the wire (JSON) |
|---|---|---|
| Vue / Nuxt (JS/TS) | camelCase | camelCase (native) |
| Spring Boot (Java) | camelCase | camelCase (native Jackson default) |
| FastAPI (Python) | snake_case internally | serialised to camelCase (see §6) |
| MongoDB documents | — | stored as camelCase to match the API |

Python people: keep your variables snake_case in your code, that's correct Python. You only convert at the API edge using Pydantic aliases (config shown in §6). Don't write Python in camelCase, and don't emit snake_case JSON.

## 1. Branch naming

We're keeping our established Demo 1 structure (it works and the team knows it) and only tightening the short-lived branch names, plus adding issue numbers for traceability.

Long-lived branches (never deleted): `main`, `dev`, `frontend-dev`, `backend-dev`, `integration`.

Promotion flow: `fe|be|int/<type>/*` → its stream branch (`frontend-dev` / `backend-dev` / `integration`) → `dev` → `main`. Nothing reaches `main` without passing through `dev`.

```
main ← demo-ready / production only
└── dev ← staging: all three streams converge & get sanity-checked here
    ├── frontend-dev └── fe/<type>/*
    ├── backend-dev  └── be/<type>/*
    └── integration  └── int/<type>/*
```

Short-lived branches follow this exact pattern:

```
<stream>/<type>/<issue-number>-<short-kebab-description>
```

- `<stream>` = `fe` (frontend), `be` (backend), or `int` (integration)
- `<type>` = `feature`, `bug`, `task`, `chore`, or `docs`
- issue number links the branch to its GitHub issue, which feeds the "issues closed" metric and makes PRs traceable

| Pattern | Example |
|---|---|
| `fe/feature/<n>-<desc>` | `fe/feature/42-listing-card` |
| `be/feature/<n>-<desc>` | `be/feature/41-create-listing-endpoint` |
| `int/feature/<n>-<desc>` | `int/feature/43-listing-end-to-end` |
| `be/bug/<n>-<desc>` | `be/bug/57-listing-price-validation` |
| `fe/task/<n>-<desc>` | `fe/task/60-extract-base-button` |
| `int/chore/<n>-<desc>` | `int/chore/12-add-ci-pipeline` |
| `hotfix/<desc>` | `hotfix/login-token-expiry` (branched directly from `main`, the one exception) |

Rules:
- Branch from the right parent: `fe/*` off `frontend-dev`, `be/*` off `backend-dev`, `int/*` off `integration`.
- Delete the short-lived branch after its PR merges.
- One slice = one branch = one PR. Don't pile three features into one branch.

**Sequencing note (this is what actually fixes Demo 1):** the structure already has a place for integration, the `integration` and `int/*` branches exist. In Demo 1, integration didn't collapse because there was nowhere to put it. It collapsed because it got scheduled last. For every vertical slice, the paired `fe/*` and `be/*` branches need to converge through `integration` → `dev` in the same sprint as the slice, not batched at the end. It wasn't the branch structure that failed us, it was the order we built things in.

## 2. Commit messages — Conventional Commits

Every commit message follows the Conventional Commits standard. It keeps history readable, looks professional to the client and mentor, and pairs with our branch types.

```
<type>(<optional scope>): <short summary in imperative mood>

<optional body — what & why, not how>

<optional footer — e.g. Closes #42>
```

| Type | Meaning |
|---|---|
| feat | A new feature / slice |
| fix | A bug fix |
| docs | Documentation only |
| style | Formatting, no code-meaning change (whitespace, semicolons) |
| refactor | Code change that neither fixes a bug nor adds a feature |
| test | Adding or fixing tests |
| chore | Build process, tooling, dependencies |

Good examples:

```
feat(marketplace): add create-listing endpoint and form
fix(auth): invalidate JWT on logout
docs(vault): document edit-history API contract
test(user): add unit tests for friend-request acceptance
```

Rules:
- Summary in the imperative ("add," not "added" or "adds"), lowercase, no full stop, 72 characters or fewer.
- Always lowercase, words separated by hyphens (kebab-case). Never spaces, camelCase, or underscores in the description.
- One logical change per commit. Don't bundle a feature, a refactor, and a formatting pass together.
- Reference the issue in the footer: `Closes #42`.
- This is also what makes the "2 commits/day" metric mean something: each commit is a real, described step, not padding.

## 3. Pull Request format

PRs are where the reviewer (and client) actually read your work. Make them clean.

PR title: same format as a commit summary, e.g. `feat(marketplace): create-listing slice (FR2.2)`

PR description template (pin this as `.github/pull_request_template.md`):

```
## What
One-line summary of the slice.

## Why
The requirement / issue this satisfies (link it).

## How
Brief notes on the approach — only what a reviewer needs.

## Checklist (Definition of Done)
- [ ] Backend + frontend integrated and working together
- [ ] Runs on the deployed environment, not just localhost
- [ ] Unit tests pass, build is green
- [ ] All acceptance criteria (Given/When/Then) met
- [ ] Wiki updated if an API contract / setup step changed

Closes #<issue>
```

Rules:
- PR targets the matching stream branch (`fe/*` → `frontend-dev`, `be/*` → `backend-dev`, `int/*` → `integration`), which then promotes to `dev`, then `main`. Never PR straight to `main` (except `hotfix/`).
- At least one teammate reviews before merge (this is our "To Review" column / Magic Column).
- Keep PRs small, one slice per PR. A 40-file PR can't be reviewed properly.

## 4. File & folder naming (per layer)

Folder structure already exists in the repo (see Repo Setup); these rules govern new files so they stay consistent.

**Frontend (Vue / Nuxt)**

| Thing | Convention | Example |
|---|---|---|
| Vue component files | PascalCase.vue | `ListingCard.vue`, `BaseButton.vue` |
| Base/shared components | `Base` prefix | `BaseButton.vue`, `BaseModal.vue` |
| Pages (Nuxt routes) | kebab-case.vue (Nuxt routing convention) | `pages/marketplace/create.vue` |
| Composables | `useXxx.ts`, camelCase with `use` prefix | `useAuth.ts`, `useListings.ts` |
| Services / API clients | camelCase.ts | `listingService.ts` |
| Stores | camelCase.ts | `userStore.ts` |

**Backend (Spring Boot / Java)**

| Thing | Convention | Example |
|---|---|---|
| Classes | PascalCase | `ListingController`, `RulebookService` |
| Packages | all lowercase, no underscores | `com.boardwise.backend.marketplace` |
| Controllers | `<Domain>Controller` | `MarketplaceController` |
| Services | `<Domain>Service` | `VaultService` |
| Repositories | `<Entity>Repository` | `ListingRepository` |
| DTOs | `<Purpose>Request` / `<Purpose>Response` | `CreateListingRequest`, `ListingResponse` |
| Test files | `<ClassName>Test` | `RulebookServiceTest` |

**AI Gateway (FastAPI / Python)**

| Thing | Convention | Example |
|---|---|---|
| Modules / files | snake_case.py | `pdf_pipeline.py`, `rag_service.py` |
| Classes | PascalCase | `IngestionPipeline` |
| Functions / variables | snake_case | `extract_text`, `chunk_size` |
| Constants | UPPER_SNAKE_CASE | `MAX_PDF_SIZE_MB` |

## 5. Variable, function & class naming (per language)

Each language keeps its own standard, we don't force one casing across all three. This is correct, idiomatic, and what the linters expect anyway.

| Construct | Java (Spring) | JS/TS (Vue/Nuxt) | Python (FastAPI) |
|---|---|---|---|
| Variable | camelCase | camelCase | snake_case |
| Function / method | camelCase | camelCase | snake_case |
| Class | PascalCase | PascalCase | PascalCase |
| Constant | UPPER_SNAKE_CASE | UPPER_SNAKE_CASE | UPPER_SNAKE_CASE |
| Boolean | prefix `is`/`has`/`can` | prefix `is`/`has`/`can` | prefix `is_`/`has_`/`can_` |
| Private (Python) | — | — | leading underscore `_internal` |

Shared rules across all languages:
- Names describe what something is, not what type it is: `activeListings`, not `listingArray`.
- No abbreviations unless universally known (`id`, `url`, `http` are fine; `lst`, `usr`, `tmp` are not).
- No single letters except loop counters (`i`, `j`) and lambdas.
- Booleans read as a yes/no question: `isActive`, `hasLock`, `canEdit`.

## 6. API & JSON conventions

### Route structure

```
/api/<domain>/<resource>[/<id>][/<sub-resource>]
```

- The HTTP method is the verb. Never put verbs (`create`, `update`, `delete`, `get`) in the path.
- Resources are plural nouns: `/listings`, `/rulebooks`, `/groups`.
- Use path params for identity (`/listings/{listingId}`), query params for filtering/pagination (`?page=0&size=20`).
- camelCase for all query params and JSON fields.

Correct (most of our routes already are):

```
POST /api/auth/register
GET  /api/marketplace/listings
GET  /api/marketplace/listings/{listingId}
POST /api/marketplace/listings
GET  /api/vault/rulebooks/{id}/history
```

Fix these two — they break the rule (verb in path, order scrambled):

```
PATCH  /api/marketplace/update/listings/{listingId}  →  PATCH  /api/marketplace/listings/{listingId}
DELETE /api/delete/marketplace/listings/{listingId}   →  DELETE /api/marketplace/listings/{listingId}
```

These two are a big part of what makes the API look inconsistent. The HTTP method already tells you it's an update or a delete, so the path just needs to name the resource.

### HTTP status codes (use them honestly)

| Code | When |
|---|---|
| 200 OK | Successful GET/PATCH/DELETE with a body |
| 201 Created | Successful POST that creates a resource |
| 202 Accepted | Async accepted (e.g. PDF ingestion) |
| 400 Bad Request | Malformed input / failed validation |
| 401 Unauthorized | Missing/invalid JWT |
| 403 Forbidden | Valid JWT but not allowed (e.g. editing someone else's listing) |
| 404 Not Found | Resource doesn't exist |
| 409 Conflict | E.g. lock already held, email already registered |

### FastAPI → camelCase (the one bit of config Python needs)

```python
from pydantic import BaseModel, ConfigDict
from pydantic.alias_generators import to_camel

class CamelModel(BaseModel):
    model_config = ConfigDict(
        alias_generator=to_camel,
        populate_by_name=True,
    )

# All request/response models inherit from CamelModel:
class RulebookResponse(CamelModel):
    rulebook_id: str      # Python sees snake_case
    version_after: int    # JSON emits "versionAfter"
    committed_at: str     # JSON emits "committedAt"
```

## 7. MongoDB conventions

- Collection names: camelCase, plural — `users`, `listings`, `rulebooks`, `editEvents`.
- Field names: camelCase, matching the API exactly (`rulebookId`, `versionAfter`).
- The Mongo `_id` ObjectId stays `_id` internally but is exposed as `id` (camelCase) in API responses. Never leak `_id` to the frontend.
- Reference fields end in `Id`: `editorId`, `rulebookId`, `ownerId`.

## 8. Text conventions (comments, docstrings, docs)

**Code comments**
- Comment why, not what. The code already says what it does.
  - Bad: `// increment i`
  - Good: `// retry up to 3x because R2 occasionally 503s on cold start`
- No commented-out code in a merged PR, delete it, Git remembers.
- No `// TODO` without an issue number: `// TODO(#88): handle expired lock`.

**Docstrings / method docs**
- Java: Javadoc on public service/controller methods, one-line summary plus `@param`/`@return`.
- Python: triple-quoted docstring on public functions, one-line summary, then Args/Returns if non-obvious.
- JS/TS: JSDoc on exported functions/composables where the signature isn't self-explanatory.

**Wiki / documentation writing style**
- Title case for page titles, sentence case for headings within a page.
- Lead with the answer, keep paragraphs short.
- Every API contract page documents: endpoint, method, auth, request body, response body, status codes.
- Use fenced code blocks with a language tag (```json, ```java).
- This is also where the "3 wiki edits/week" metric gets met, through actual documentation rather than busywork.

## 9. Automate it

The rules above only work if linters and formatters enforce them, so nobody has to keep it all in their head. Add these per layer and wire them into CI:

| Layer | Formatter | Linter | Notes |
|---|---|---|---|
| Vue / Nuxt | Prettier | ESLint (eslint-plugin-vue) | Add `.prettierrc` + `.eslintrc`. Run on pre-commit. |
| Spring Boot | Spotless (or google-java-format) | Checkstyle | Add a `checkstyle.xml`; fail the build on violations. |
| FastAPI | Black | Ruff | Black formats, Ruff lints + sorts imports. `pyproject.toml`. |
| All | EditorConfig | — | One `.editorconfig` at repo root: UTF-8, LF line endings, 2-space JS / 4-space Java/Python, final newline. |
| Commits | commitlint + husky | — | Rejects commits that don't follow Conventional Commits. |

Set this up once, in a `chore/` branch this sprint. After that, inconsistent code can't be merged, the CI check goes red (this is also where the "50% build pass" metric ties in). Once it's automated, the client's complaint stops being something five people have to remember and becomes something the build itself enforces.

## 10. The short version (pin in Discord)

1. camelCase over the wire — all JSON, all routes, every service. Python stays snake_case internally only.
2. Branches: `stream/type/issue-description` (`fe`/`be`/`int` + `feature`/`bug`/`task`...), kebab-case, off the right stream. Flow: branch → stream → dev → main.
3. Commits: Conventional Commits — `feat(scope): imperative summary`.
4. No verbs in API paths — the HTTP method is the verb. Resources are plural nouns.
5. Each language keeps its own idiom internally; linters enforce it automatically.
6. One slice = one branch = one PR, reviewed before merge.

---

*Living document, update at retro when the team agrees on a change. Last updated 25 May 2026.*
