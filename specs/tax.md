# okay-tax — take-home pay and taxes in Poland, from the statutes

A calculator, not tax advice (doradztwo podatkowe is a regulated profession in Poland). Every number it uses is a
value stated in a statute or an official notice, kept with the statute's own words.

## 1. The theory of a year as data

`PayTheory.y2026`: each value (`Stated(value, source, quote)`) — the PIT scale (12% / 32%, threshold 120 000,
reducing amount 3600), the employee's costs (250 a month), the linear tax (19%), ryczałt rates, ZUS bases and
contributions (full, preferential, ulga na start), the health contribution (9%, 4.9% linear with its deduction
limit, the three ryczałt tiers, the minimum), the annual cap on pension and disability contributions.

The texts are in `sources/2026/` (from the Sejm ELI API, the ZUS notice, M.P. 2025 poz. 1274). `QuotesSuite` checks
that every quote is in its source, word for word: a value with no quote in a statute is not used.

## 2. The calculations

- `Pay.employment(t, gross)`: an employment contract (UoP), month by month: the cap on contributions, the threshold
  crossed in the year, the reducing amount 300 a month.
- `Jdg.year(t, revenue, costs, form, zus, sickness)`: a sole trader (JDG) on the scale, the linear tax or ryczałt,
  with full, preferential or no social contributions (ulga na start), voluntary sickness insurance or not.
- `Jdg.compare`: the forms side by side.

The laws (`PaySuite`): the ZUS tables, the health tiers, the minimum wage's net (3605.85), each form's worked
examples, and the pay laws over drawn inputs (more gross never less take-home; parts add up to the gross).

## 3. The page

okay-ui screens in the browser (Scala.js), in Polish (the default), English, Russian and Ukrainian. The form takes a
UoP gross and/or a B2B revenue (or a day rate and days), a ryczałt rate, costs, sickness insurance. The result is
every option sorted by the year's take-home, the first month of the best four part by part, and where every number
comes from (the statute's words).

## 4. The year's return

`Annual` (`core/.../Annual.scala`): an employer's year as its PIT-11 says it (`Pit11`), a JDG's as its books do
(`Books`); the returns of the year (`Annual.year`):

- PIT-37 (employment) or PIT-36 (with a JDG on the scale): the base is the income less each person's social
  contributions (art. 26 ust. 1 pkt 2); jointly with a spouse (art. 6 ust. 2) or as a single parent with a child
  (art. 6 ust. 4c) twice the tax of half; the children's relief (art. 27f: 92.67 a month the first two, 166.67 the
  third, 225 each next; one child within 112 000 married or single parent, 56 000 otherwise), the part the tax
  cannot take paid back up to the contributions (ust. 8–10).
- PIT-36L: 19% of the income less the social contributions and the health ones up to 14 100.
- PIT-28: the rate of the revenue less the social contributions and half the health ones.
- Bases and taxes rounded to full złoty (Ordynacja podatkowa art. 63 § 1, `sources/2026/op-2026.txt`).

The laws (`AnnualSuite`): the scale's own figures, the relief's amounts and limits, worked returns; the advances
the engine withholds come to the return within 12 zł (UoP and every JDG form and ZUS); filing jointly never costs
more than apart; the relief never raises the tax. Not yet: losses carried over, other reliefs (internet, donations,
IKZE), several ryczałt rates in one year, a child for part of the year.

The page: the return's own screen, and from the calculator's result the return at that pay (its PIT-11 filled in).

## 5. The questions about the rules

`Rules` (`core/.../Rules.scala`): 24 rules of the statutes as data — how people ask about each (phrases in Polish,
English, Russian, Ukrainian; the first its title), what it says in each language, and where it stands (`Cite`: the
source, the article, the words verbatim). The scale and the reducing amount, the employee's costs, filing jointly
and as a single parent and when neither is open (art. 6 ust. 8), the children's relief and its paid-back part, the
linear tax (19%, choosing it, its health contribution), ryczałt (rates, the 2 000 000 euro limit, its health
contribution), the health contribution on the scale, ZUS 2026, the exemptions of art. 21 ust. 1 pkt 148 and 152–154
and their shared cap, the internet, donations, thermal modernisation, the deadline, rounding.

`Rules.find(question)`: the question's words to stems (diacritics off, the first five letters, the words of asking
itself left out), each weighed by how few rules have it, twice in a rule's asks as in what it says.

The laws: every quote in its source (`QuotesSuite`, a line end's hyphen joined), every rule in four languages, each
way a rule is asked finds it first, questions in people's own words find the right rule (`RulesSuite`).

The page: "Pytania o przepisy" — a question or a rule from the list; each rule said and quoted. The year's return
says, with the rule, why a spouse files apart (`Return.notes`).

## 6. Next

1. A reader that understands the question (okay-desk's frames: the rules are the choices) beside the words' search,
   measured against it on held-out questions.
2. More of the year: losses carried over, IKZE, a child for part of the year, several ryczałt rates.

### 6.1 The measure (2026-11-26)

`data/heldout.tsv`: 192 questions (24 rules × 4 languages × 2) written before any measure, never used to tune the asks
or a reader. The word search (`HeldoutSuite`, its floor): the right rule first 134 (pl 29, en 29, ru 39, uk 37 of
48), among the first three 171. It misses what names no rule's words ("światłowód", "1234,49 zł", "I am 24 and
employed") and parts joint filing from the rule that closes it.
