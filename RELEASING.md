# Releasing

`okay-tax-core` (JVM and Scala.js, no dependencies) is a Maven repository on GitHub Pages: the `gh-pages` branch,
its `maven/` directory beside the page, served at https://okay-effects.github.io/okay-tax/maven.

```scala-sbt
resolvers += "okay-tax" at "https://okay-effects.github.io/okay-tax/maven"
libraryDependencies += "io.github.okay-effects" %%% "okay-tax-core" % "0.1.0"
```

## Making one

While nobody but us uses it, a change is REPUBLISHED under 0.1.0 and the tag moved; a new number only when someone
else depends on it.

1. `sbt test` green; committed; `git tag -f v0.1.0`.
2. `rm -rf target/pages-maven && sbt publish`.
3. Onto `gh-pages`, under `maven/` (`sbt publishSite` keeps `maven/`):

   ```sh
   git worktree add /tmp/okay-tax-pages gh-pages
   mkdir -p /tmp/okay-tax-pages/maven && rsync -ac target/pages-maven/ /tmp/okay-tax-pages/maven/
   git -C /tmp/okay-tax-pages add -A && git -C /tmp/okay-tax-pages commit -m "release 0.1.0"
   git -C /tmp/okay-tax-pages push origin gh-pages && git worktree remove /tmp/okay-tax-pages
   git push -f origin v0.1.0
   ```
