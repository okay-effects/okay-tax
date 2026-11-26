package okay.tax

/** THE YEAR'S RETURNS AS LAWS (specs/tax.md §4): worked examples of the statute, and the laws over many inputs */
class AnnualSuite extends munit.FunSuite:
  private val t = PayTheory.y2026
  private def job(base: BigDecimal, advances: BigDecimal = 0) = Pit11(base, 0, 0, 0, advances)

  test("the scale: 100 000 → 8 400; 120 000 → 10 800; 150 000 → 20 400"):
    assertEquals(Annual.scale(t, Income(Vector(job(100000))))("tax"), BigDecimal(8400))
    assertEquals(Annual.scale(t, Income(Vector(job(120000))))("tax"), BigDecimal(10800))
    assertEquals(Annual.scale(t, Income(Vector(job(150000))))("tax"), BigDecimal(20400))
    assertEquals(Annual.scale(t, Income(Vector(job(30000))))("tax"), BigDecimal(0))

  test("spouses jointly: 200 000 and nothing → twice the tax of 100 000 = 16 800 (alone 36 400)"):
    val alone = Annual.scale(t, Income(Vector(job(200000))))
    val joint = Annual.scale(t, Income(Vector(job(200000))), Some(Income()))
    assertEquals(alone("tax"), BigDecimal(36400))
    assertEquals(joint("tax"), BigDecimal(16800))
    // a single parent the same, with a child; without one the halves are not open
    assertEquals(Annual.scale(t, Income(Vector(job(200000))), children = 1, singleParent = true)("tax"),
      BigDecimal(16800))
    assertEquals(Annual.scale(t, Income(Vector(job(200000))), singleParent = true)("tax"), BigDecimal(36400))

  test("the children's relief: 1 112.04 a child for two, 2 000.04 the third, 2 700 each next"):
    assertEquals(Annual.childRelief(t, 2, married = true, singleParent = false, 500000), BigDecimal("2224.08"))
    assertEquals(Annual.childRelief(t, 3, married = true, singleParent = false, 0), BigDecimal("4224.12"))
    assertEquals(Annual.childRelief(t, 4, married = true, singleParent = false, 0), BigDecimal("6924.12"))
    // one child: within 112 000 married or a single parent, 56 000 otherwise
    assertEquals(Annual.childRelief(t, 1, married = true, singleParent = false, 112000), BigDecimal("1112.04"))
    assertEquals(Annual.childRelief(t, 1, married = true, singleParent = false, 112001), BigDecimal(0))
    assertEquals(Annual.childRelief(t, 1, married = false, singleParent = false, 56001), BigDecimal(0))
    assertEquals(Annual.childRelief(t, 1, married = false, singleParent = true, 100000), BigDecimal("1112.04"))

  test("the relief the tax could not take is paid back, up to the contributions (art. 27f ust. 8–9)"):
    // 40 000 a year: base 34 000, tax 480; three children 4 224.12; the rest, 3 744.12, back (contributions 9 000)
    val r = Annual.scale(t, Income(Vector(Pit11(40000, 0, 6000, 3000, 1200))), children = 3)
    assertEquals(r("base"), BigDecimal(34000))
    assertEquals(r("tax"), BigDecimal(480))
    assertEquals(r("children"), BigDecimal(480))
    assertEquals(r("children.back"), BigDecimal("3744.12"))
    assertEquals(r.due, BigDecimal("-4944.12"))
    // and never more than the contributions
    val small = Annual.scale(t, Income(Vector(Pit11(40000, 0, 1000, 500, 0))), children = 3)
    assertEquals(small("children.back"), BigDecimal(1500))

  test("PIT-36L: 19% after the social contributions and the health ones up to 14 100"):
    val r = Annual.linear(t, Books(300000, 50000, 20000, 20000, 40000))
    assertEquals(r("health.deducted"), BigDecimal(14100))
    assertEquals(r("base"), BigDecimal(215900))
    assertEquals(r("tax"), BigDecimal(41021))
    assertEquals(r.due, BigDecimal(1021))

  test("PIT-28: 12% after the social contributions and half the health ones"):
    val r = Annual.lump(t, Books(240000, 0, 19798, 9966.96, 0), BigDecimal("0.12"))
    assertEquals(r("health.deducted"), BigDecimal("4983.48"))
    assertEquals(r("base"), BigDecimal(215219))
    assertEquals(r("tax"), BigDecimal(25826))

  test("the forms: employment alone PIT-37; with a JDG on the scale PIT-36; linear and ryczałt their own"):
    val b = Books(100000, 0, 0, 0, 0)
    assertEquals(Annual.year(t, Vector(job(1)), None).map(_.form), Vector("PIT-37"))
    assertEquals(Annual.year(t, Vector(job(1)), Some(b -> Form.Scale)).map(_.form), Vector("PIT-36"))
    assertEquals(Annual.year(t, Vector(job(1)), Some(b -> Form.Linear)).map(_.form), Vector("PIT-37", "PIT-36L"))
    assertEquals(Annual.year(t, Vector.empty, Some(b -> Form.Lump(0.12))).map(_.form), Vector("PIT-28"))

  test("the laws: a year's advances withheld as the engine withholds them come to its return, within a few złoty"):
    val grosses = (0 to 60).map(i => BigDecimal(4806) + BigDecimal(i) * 750)
    for g <- grosses do
      val r = Annual.scale(t, Income(Vector(Pit11.of(t, g))))
      assert(r.due.abs <= 12, s"UoP $g: due ${r.due}")
    for g <- grosses; f <- Seq(Form.Scale, Form.Linear, Form.Lump(BigDecimal("0.12")));
        z <- Seq(Zus.Start, Zus.Preferential, Zus.Full) do
      val b = Books.of(t, g, 0, f, z, sickness = false)
      val r = f match
        case Form.Scale => Annual.scale(t, Income(business = Some(b)))
        case Form.Linear => Annual.linear(t, b)
        case Form.Lump(rate) => Annual.lump(t, b, rate)
      assert(r.due.abs <= 12, s"JDG $g $f $z: due ${r.due}")

  test("the laws: filing jointly never costs more than alone, and the relief never raises the tax"):
    for a <- 0 to 300000 by 15000; b <- 0 to 300000 by 25000 do
      val x = Income(Vector(job(a))); val y = Income(Vector(job(b)))
      val joint = Annual.scale(t, x, Some(y))("tax")
      val apart = Annual.scale(t, x)("tax") + Annual.scale(t, y)("tax")
      assert(joint <= apart, s"$a + $b: joint $joint, apart $apart")
      assert(Annual.scale(t, x, children = 2)("tax.due") <= Annual.scale(t, x)("tax.due"))

  test("no halves beside the linear tax or ryczałt (art. 6 ust. 8): the spouse files apart, the return says why"):
    val b = Books(100000, 0, 0, 0, 0)
    for f <- Seq(Form.Linear, Form.Lump(BigDecimal("0.12"))) do
      val rs = Annual.year(t, Vector(job(200000)), Some(b -> f), Some(Income()))
      assertEquals(rs.head("tax"), BigDecimal(36400))
      assertEquals(rs.head.notes, Vector("halves.excluded"))
      val single = Annual.year(t, Vector(job(200000)), Some(b -> f), children = 1, singleParent = true)
      assertEquals(single.head("tax"), BigDecimal(36400))
    // on the scale the halves stand
    assertEquals(Annual.year(t, Vector(job(200000)), Some(Books(0, 0, 0, 0, 0) -> Form.Scale),
      Some(Income())).head("tax"), BigDecimal(16800))
