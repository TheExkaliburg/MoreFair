

placeholder for an actual guide how to contribute to this project


but this is a good start and should be the least: https://gist.github.com/Chaser324/ce0505fbed06b947d962

## Dependency updates and security checks

Install the [hosted Renovate GitHub App](https://github.com/apps/renovate) for this
repository and enable GitHub's dependency graph and Dependabot alerts. The app
uses `.github/renovate.json`; no Renovate token or self-hosted workflow is needed.
Do not also enable Dependabot version-update PRs for the same dependencies.

Renovate checks npm/pnpm, Maven, GitHub Actions, Dockerfiles and Compose files.
Regular updates are scheduled for Monday 04:00-06:59 UTC, major updates require
Dependency Dashboard approval, and auto-merge is disabled. Related Tiptap, Pinia
and Vue i18n packages are grouped. The deprecated `spark/` module is excluded.
Security-update PRs are not restricted to the regular update window.

CodeQL and ESLint run on pushes, pull requests and weekly schedules. ESLint uses
the locked frontend dependencies and reports lint findings through SARIF;
tool/configuration failures fail the job. Fork pull requests run linting without
attempting to upload SARIF with a read-only token.

The Dependency Security workflow rejects new high/critical advisories visible
to GitHub's dependency review, including development dependencies. On trusted
pushes and weekly/manual runs it submits the resolved backend Maven graph,
including transitives, without building the deprecated Spark module. GitHub's
dependency graph and alerts must be enabled for these integrations.

Weekly/manual frontend audits use pnpm 12 directly, independently of GitHub's
pnpm lockfile support. They include all development dependencies because this
project also declares browser/runtime packages there. Existing high/critical
findings fail that audit too; remaining Nuxt/Content and Tiptap findings are not
silenced while their migrations are pending. To run the same audit locally:

```sh
cd frontend
pnpm audit --audit-level=high --registry=https://registry.npmjs.org/
```
