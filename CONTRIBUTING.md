# Contributing

## Branching Model

This project follows GitFlow.

- `main`: stable branch for release-ready code
- `develop`: integration branch for ongoing development
- `feature/*`: feature branches created from `develop`
- `docs/*`: documentation branches created from `develop`
- `release/*`: release preparation branches created from `develop`
- `hotfix/*`: urgent fix branches created from `main`

## Workflow

Start new development work from `develop`.

```powershell
git switch develop
git pull
git switch -c feature/example-feature
```

Use `docs/*` instead of `feature/*` when the change is only documentation.

```powershell
git switch -c docs/project-governance
```

## Pull Requests

- Merge `feature/*` into `develop`.
- Merge `docs/*` into `develop`.
- Merge `release/*` into `main`, then merge `main` back into `develop`.
- Merge `hotfix/*` into `main`, then merge `main` back into `develop`.

Pull requests should pass the GitHub Actions build before merging.

## Releases

Prepare releases from `develop`.

```powershell
git switch develop
git pull
git switch -c release/1.0.0
```

After the release branch is ready, merge it into `main`, tag the release, and merge `main` back into `develop`.

```powershell
git switch main
git pull
git tag v1.0.0
git push origin v1.0.0

git switch develop
git merge main
git push
```

## Commit Messages

Use short, imperative commit messages.

Examples:

- `Add Create dependency`
- `Document GitFlow workflow`
- `Fix generated resource filtering`
