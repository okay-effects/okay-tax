# AGENTS.md — okay-tax

- Read `specs/tax.md` first; it is the contract.
- A value enters `PayTheory` only with its source and a verbatim quote found in `sources/` (`QuotesSuite`).
- A calculator, not tax advice: never word a result as a recommendation.
- Scala 3, `-Werror`, lines at most 120 characters. Scripts are scala-cli, never Python.
- The page: Polish by default, also English, Russian, Ukrainian (`app/.../Words.scala`), the footer "Built on
  okay-effects … Need more?".
- `sbt test` before a commit; commit and push each change; `sbt publishSite` after a change to the page.
- One version (0.1.0) while unused: republish it, never a new number.
