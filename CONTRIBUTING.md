# Contributing Guidelines

Versioning:  The project follows [SemVer](https://semver.org/) versioning.  Currently the project is still quite new and no release is being made past 0.0.1 until usage of the API begins to stabilize at least a little bit.  Nothing to worry about this early.

Commit message format:  [module] lowercase description of what happened

Optionally, you can elaborate more in the commit message with a full break between the title and description.

The `[module]` could have a / to designate a sub-functionality or area it focuses on.  For example, `[module/dispatch]`

Some commits must touch all shipped implementations of Dispatcher ( if you update AbstractDispatcher for example ).  The `[module]` would still be `[core]` as that is where the change is being made requiring the subclasses to implement.  Do not break these commits up as we could not fall back to one where `core` and another module are out-of-sync ( compile errors ).

## What to work on?

All work should be decided on *before* you begin working.  Either through DMs on Discord ( Bosco#8564 ) or through Issues in this repo.  Do not just whip up a PR for a feature and expect it to be accepted with open arms.

## How does merging work?

All code must be rebased against current master and able to be fast-forward merged on-top of master.  There are no merge commits, and master is a completely linear history where every single commit is able to be built, tested, and used.

If you have WIP or incomplete commits, squash them.  If you have commits doing too many things that aren't necessary, split them into separate PRs ( probably with a different issue ).  Keep a commit as small as it needs to be to satisfy an issue.

## Questions

For any questions, contact me on GitHub ( BoscoJared ) or Discord ( Bosco#8564 ).  Happy coding!
