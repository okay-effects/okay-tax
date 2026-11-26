package okay.tax

/** THE RULES AS LAWS (specs/tax.md §5): said in every language, quoted, and found by the ways people ask */
class RulesSuite extends munit.FunSuite:
  test("every rule said in Polish, English, Russian and Ukrainian, asked in each, quoted at least once; ids unique"):
    for r <- Rules.all do
      assertEquals(r.asks.keySet, Set("pl", "en", "ru", "uk"), r.id)
      assertEquals(r.says.keySet, Set("pl", "en", "ru", "uk"), r.id)
      assert(r.cites.nonEmpty, r.id)
    assertEquals(Rules.all.map(_.id).distinct.size, Rules.all.size)

  test("each way a rule is asked finds it first"):
    val missed = for r <- Rules.all; (lang, asks) <- r.asks.toVector; ask <- asks.split(';').map(_.trim)
      if !Rules.find(ask).headOption.contains(r) yield s"[$lang] $ask → ${Rules.find(ask).map(_.id)} (not ${r.id})"
    assert(missed.isEmpty, missed.mkString("\n"))

  test("questions in people's own words"):
    def first(q: String) = Rules.find(q).headOption.map(_.id)
    assertEquals(first("czy mogę rozliczyć się z żoną jeśli mam ryczałt?"), Some("halves.excluded"))
    assertEquals(first("How much is the tax-free amount?"), Some("free"))
    assertEquals(first("до какого числа подавать декларацию?"), Some("deadline"))
    assertEquals(first("скільки пільга на дитину"), Some("children"))
    assertEquals(Rules.find("xyzzy"), Vector.empty)
