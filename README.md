# okay-tax

Take-home pay in Poland for 2026: an employment contract (UoP) and a sole trader's B2B (the scale, the linear tax,
ryczałt; full, preferential or no ZUS), side by side, and the year's return (PIT-37, PIT-36, PIT-36L, PIT-28:
jointly, a single parent, the children's relief). Every number comes from the statutes and is kept with their
own words.

**Live:** https://okay-effects.github.io/okay-tax/

A calculator, not tax advice.

- `core` — the engine (Scala 3, JVM and Scala.js, no dependencies): `PayTheory.y2026`, `Pay`, `Jdg`.
- `app` — the page on [okay-ui](https://github.com/okay-effects/okay-ui).
- `sources/2026` — the statutes' texts the quotes are checked against.

```
sbt test          # the laws and the quotes
sbt web           # the page in app/target/web
sbt publishSite   # the page to gh-pages
```

See `specs/tax.md`. Built on [okay-effects](https://github.com/okay-effects).
