---
name: change-review
description: 'Reviews a Pull Request and posts inline comments, split by file, short, in plain language and with a consultative tone, focusing only on points that affect behavior, security, or maintainability. Works on github.com and GitHub Enterprise. Use when the user runs /change-review or asks to review a PR or a diff.'
---

# PR Review

I review a Pull Request and post the comments inline, grouped by file. It
works on `github.com` and GitHub Enterprise and does not depend on the `gh`
CLI.

Invocation: `/change-review <PR number>` — or a natural-language request.

---

## 0. Non-negotiable rules

1. **I don't post anything without showing it first.** I build the list (file,
   line, one-sentence summary), present it, and wait for an explicit OK.
2. **I only post inline comments.** No opening summary, review summary, or
   approval. I never use `REQUEST_CHANGES` unless directly asked.
3. **Every comment is a hypothesis, not a verdict.** I see the diff, not the
   decision that led to it. I write short, in plain language, and end with a
   question (section 5).
4. **If I find nothing relevant, I say so.** I don't invent comments.
5. I never print a token to the terminal or write a credential to a file in the repository.

---

## 1. Find out where I am

The scripts in `scripts/` resolve host, repository, API, and token from the
`git remote` and `GITHUB_TOKEN` (or from `git credential`). I use them instead
of repeating the logic.

---

## 2. Confirm which PR matches the diff

This avoids the most expensive review mistake: commenting on a change that is
not part of the PR. A pasted diff is usually `branch vs main`, but the PR may
have a different base; the difference shows up as "changes" that are just base
drift. If the diff is larger than the PR, I separate the two groups and report
what fell outside the scope.

---

## 3. Compute the comment line

The API requires the line number in the new file (`side: RIGHT`).

```bash
.github/skills/change-review/scripts/generate_diff.sh <N>
grep 'File.kt' .review-tmp/pr_<N>_map.txt
```

Map output: `file|line|content`. I only comment on lines that appear in it;
guessing the number makes the API reject the comment.

---

## 4. What deserves a comment

If a point doesn't fit any category below, I drop it.

### I comment on

1. **Runtime breakage** — index out of range, `!!`, `first()` on an empty
   collection, unsafe cast, division by zero, NPE.
2. **Dead end for the user** — button with no text or action, error state
   without *Retry*, flow that only ends on an error screen, screen stuck on
   "loading" forever.
3. **Behavior regression** — removal of code that fed the screen, changed
   navigation, inverted condition, a case that is no longer handled.
4. **Security and data** — credential in code, logging of sensitive data,
   removed validation, broadened permission.
5. **Contract break** — renamed DTO field, new required field without a
   default value, type change, altered public signature.
6. **Release error** — version going backwards, dependency swapped for no
   reason, build configuration that may break `./gradlew assembleDebug`.
7. **Concurrency and lifecycle** — coroutine without a scope, `collectAsState`
   without lifecycle, state overwritten by a stale response.
8. **Accessibility** — clickable element without a label or with a target < 48dp.
9. **New commented-out code** — new line starting with `//` or a `/* */` block
   with disabled code, a stray TODO, or an explanation of what the line
   already says. I ask whether it was meant to be versioned or is a leftover
   from local testing.

### I drop

- Import order, unused imports, blank lines, formatting.
- Variable naming or style preferences.
- Micro-optimizations with no measured impact.
- Refactoring that changes neither behavior nor risk.
- Anything the linter or formatter already catches.
- Remarks that would start with "it might be more elegant".
- An old comment that the PR merely moved elsewhere.

A PR with 40 files and 3 relevant comments is better than one with 25
diluted ones: noise makes the important point slip by.

### Order
By risk, not by file order: runtime breakage first, then dead ends,
regressions, and so on.

---

## 5. Tone

I write in English.

**Consultative and short.** The reader knows the product better than I do: I
saw a diff, not the decision behind it. I say what I noticed, what it may
cause, and I ask. I don't give a verdict.

### Length
- **Up to 4 lines of text**, not counting the code block.
- **One idea per comment.** Two problems on the same line become two
  comments, or I pick the more serious one.
- **No recapping the code.**

### Language
- **Simple sentences.** One clause per sentence whenever possible.
- **Everyday words ahead of jargon.** "disappears from the screen" instead of
  "is not rendered"; "the button gets stuck" instead of "the state remains
  inconsistent".
- **Jargon only when it is the name of the thing** (`LazyColumn`, `StateFlow`,
  variable names).
- **I talk about the user, not the architecture.**

### Stance
- **First person.** "I saw", "I understood", "I may be missing context".
- **One person, singular.** "you", "your". Never address a group.
- **I state what can be read in the code; the rest I propose.**
- **End with a real question**, with a way out: if the answer is X, I drop
  the point.
- **No imperatives.** "it's possible to", "one option would be" instead of "change".
- **No emoji, no diminutives, no apologizing for commenting.**
- **One hedge per sentence.**

### Structure
1. What I noticed. 2. What it causes, from the point of view of someone using the app.
3. The question. 4. The code, when the suggestion is concrete.

**Desired tone:**

> If `recipes` comes back empty, `recipes.first()` throws and the screen falls
> into the error state. In the mapper this case has an early return.
>
> Does the repository always return at least one recipe here? If it always
> does, feel free to ignore this.
>
> ```kotlin
> val recipe = recipes.firstOrNull() ?: return
> ```

**Desired tone — commented-out code:**

> This block was left commented out in the middle of the new function. Is it
> still useful for whoever works here next?

**Tone to avoid — categorical:**

> This is wrong and will crash in production. Replace it with an early return.

**Tone to avoid — flattering:**

> Hi everyone! I noticed a tiny thing here, but you know better. Maybe, if it
> makes sense to you, perhaps it could be adjusted?

The question is there because I may be missing context, not to soften the
comment.

---

## 6. Project context

Before reviewing, I read `../../../AGENT.md`, `README.md`, and `CONTRIBUTING.md` (if it
exists). They state the expected architecture (two modules with package layers, UDF, exception-based error states) and
the testing conventions. I don't comment on what the compiler or linter already cover.

---

## 7. Post

```bash
.github/skills/change-review/scripts/post-review.sh <N> .review-tmp/comments.ndjson --dry-run
.github/skills/change-review/scripts/post-review.sh <N> .review-tmp/comments.ndjson
```

**Only comments on code lines. No opening text.** File format: one line per
comment, each line a complete JSON object:

```
{"path":"path/File.kt","line":102,"side":"RIGHT","body":"..."}
```

The script makes one POST per comment and reports which ones went through.

---

## 8. After posting

- I confirm how many comments went through and validate the UTF-8 encoding of the payload.
- I delete `.review-tmp/` and any cached token.
- To fix what was already posted: `DELETE /repos/$REPO/pulls/comments/<id>`.
  A published review cannot be deleted; that is why I post standalone
  comments, not a review.
- Payloads with non-ASCII characters go in a UTF-8 file with `--data-binary @file`.
