package okay.tax.app

import okay.business.model.{Button, Column, Field, Form, Part, Screen, Table, Ui}
import okay.business.scala3.*
import okay.tax.{Jdg, Pay, PayTheory, Zus}
import okay.tax.Form as TaxForm

/** what was asked: an employment contract at a gross and / or a JDG's revenue (a month, or a day rate and days) */
final case class Asked(gross: Option[BigDecimal], revenue: Option[BigDecimal], dayRate: Option[BigDecimal],
    days: Option[Long], lumpRate: BigDecimal, costs: Option[BigDecimal], sickness: Boolean):
  def b2b: Option[BigDecimal] = revenue.orElse(for r <- dayRate; d <- days yield r * d)

/**
 * THE CALCULATOR (specs/tax.md): the form, then every way the money may be earned side by side — UoP at the gross, a
 * JDG on the scale, the linear tax and ryczałt with each ZUS — the year's take-home and its month, each part of the
 * first month, and where every number comes from (the statute's words). A calculator, not tax advice
 */
object App:
  def zl(x: BigDecimal): String =
    val s = x.setScale(2, BigDecimal.RoundingMode.HALF_UP).toString
    val (whole, frac) = s.span(_ != '.')
    val neg = whole.startsWith("-")
    val digits = whole.stripPrefix("-")
    val grouped = digits.reverse.grouped(3).mkString(" ").reverse
    s"${if neg then "−" else ""}$grouped${frac.replace('.', ',')} zł"

  private def form(w: Words): Form[Asked] =
    Form((Field.money(w.gross, "PLN").optional, Field.money(w.revenue, "PLN").optional,
      Field.money(w.dayRate, "PLN").optional, Field.whole(w.days).optional,
      Field.choice(w.lumpRate, ("12% (IT)", BigDecimal("0.12")), ("8,5%", BigDecimal("0.085")),
        ("14%", BigDecimal("0.14")), ("15%", BigDecimal("0.15")), ("17%", BigDecimal("0.17"))),
      Field.money(w.costs, "PLN").optional, Field.flag(w.sickness)))
      .as[Asked]

  def main[R[_]](w: Words, last: Option[Asked] = None)(using In[Ui, R]): Unit ! R =
    val f = last.fold(form(w))(a => form(w).filledWith(a))
    // Left(true): the year's return; Left(false): the questions about the rules
    show(Screen[Either[Boolean, Asked]](w.title)(Part.text(w.lead), f, Part.info(w.notAdvice))(
      Button.submit(w.compute, f)(Right(_)), Button(AnnualWords.of(w.lang).open)(Left(true)),
      Button(RulesWords.of(w.lang).open)(Left(false)))).flatMap {
      case Left(true) => AnnualApp.main[R](w, None, main[R](w, last))
      case Left(false) => RulesApp.main[R](w, main[R](w, last))
      case Right(a) if a.gross.isEmpty && a.b2b.isEmpty =>
        show(Screen[Unit](w.title)(Part.problem(w.nothing))(Button(w.back)(()))).flatMap(_ => main[R](w, Some(a)))
      case Right(a) => result[R](w, a)
    }

  /** one way of earning: its name and its year */
  private final case class Way(name: String, year: Pay.Year)

  def result[R[_]](w: Words, a: Asked)(using In[Ui, R]): Unit ! R =
    val t = PayTheory.y2026
    val costs = a.costs.getOrElse(BigDecimal(0))
    val uop = a.gross.map(g => Way(s"UoP — ${zl(g)} ${w.grossShort}", Pay.employment(t, g))).toVector
    val forms = Vector(w.scale -> TaxForm.Scale, w.linear -> TaxForm.Linear,
      s"${w.lump} ${(a.lumpRate * 100).bigDecimal.stripTrailingZeros.toPlainString}%" -> TaxForm.Lump(a.lumpRate))
    val zuses = Vector(w.zusFull -> Zus.Full, w.zusPreferential -> Zus.Preferential, w.zusStart -> Zus.Start)
    val b2b = a.b2b.toVector.flatMap(r => for (fn, f) <- forms; (zn, z) <- zuses
      yield Way(s"B2B — $fn, $zn", Jdg.year(t, r, costs, f, z, a.sickness)))
    val ways = (uop ++ b2b).sortBy(-_.year.net)
    val table = Table(ways.toList)(Column.text(w.way)(_.name), Column.text(w.month)(x => zl(x.year.averageNet)),
      Column.text(w.year)(x => zl(x.year.net)))
    // the first month of each way, part by part
    def parts(x: Way): Part =
      val m = x.year.months.head
      Part.section(x.name)(Part.details(w.income -> zl(m.gross), w.social -> zl(m.social), w.health -> zl(m.health),
        w.tax -> zl(x.year.months.map(_.tax).sum / 12), w.other -> zl(m.other), w.net -> zl(x.year.averageNet)))
    val sources = Part.section(w.sources)((Part.text(w.sourcesLead) +: t.toVector.sortBy(_._1).map((k, s) =>
      Part.details(k -> s"${s.value} — «${s.quote}» (${w.source(s.source)})")))*)
    val shown = (Part.text(a.b2b.fold("")(r => s"${w.b2bMonthly}: ${zl(r)}")) +: Part.info(w.notAdvice) +: table +:
      ways.take(4).map(parts)) :+ sources
    // the year's return at this pay: the PIT-11 the engine's withholding makes, filled in
    val annual = a.gross.toList.map(g =>
      Button[Either[Boolean, Filed]](AnnualWords.of(w.lang).fromCalculator)(Right(Filed.of(g))))
    show(Screen[Either[Boolean, Filed]](w.result)(shown*)(
      (Button[Either[Boolean, Filed]](w.change)(Left(true)) :: Button[Either[Boolean, Filed]](w.again)(Left(false)) ::
        annual)*)).flatMap {
      case Left(change) => main[R](w, Option.when(change)(a))
      case Right(filed) => AnnualApp.main[R](w, Some(filed), result[R](w, a)) }
