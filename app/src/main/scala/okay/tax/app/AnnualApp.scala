package okay.tax.app

import okay.business.model.{Button, Field, Form, Part, Screen, Ui}
import okay.business.scala3.*
import okay.tax.{Annual, Books, Income, PayTheory, Pit11, Return}
import okay.tax.Form as TaxForm

/** what the year's return was asked with: the PIT-11s' sums, a business's year, a spouse's PIT-11, the children */
final case class Filed(revenue: Option[BigDecimal], costs: Option[BigDecimal], social: Option[BigDecimal],
    health: Option[BigDecimal], advances: Option[BigDecimal], form: Option[TaxForm], bRevenue: Option[BigDecimal],
    bCosts: Option[BigDecimal], bSocial: Option[BigDecimal], bHealth: Option[BigDecimal],
    bAdvances: Option[BigDecimal], sRevenue: Option[BigDecimal], sCosts: Option[BigDecimal],
    sSocial: Option[BigDecimal], sHealth: Option[BigDecimal], sAdvances: Option[BigDecimal], children: Option[Long],
    singleParent: Boolean):
  private def z(x: Option[BigDecimal]) = x.getOrElse(BigDecimal(0))
  def jobs: Vector[Pit11] = revenue.map(r => Pit11(r, z(costs), z(social), z(health), z(advances))).toVector
  def business: Option[(Books, TaxForm)] = form.map(f => Books(z(bRevenue), z(bCosts), z(bSocial), z(bHealth),
    z(bAdvances)) -> f)
  def spouse: Option[Income] = sRevenue.map(r => Income(Vector(Pit11(r, z(sCosts), z(sSocial), z(sHealth),
    z(sAdvances)))))

object Filed:
  /** a year of UoP at `gross` a month, as its PIT-11 would say it */
  def of(gross: BigDecimal): Filed =
    val p = Pit11.of(PayTheory.y2026, gross)
    Filed(Some(p.revenue), Some(p.costs), Some(p.social), Some(p.health), Some(p.advances), None, None, None, None,
      None, None, None, None, None, None, None, None, false)

/**
 * THE YEAR'S RETURN (specs/tax.md §4): the PIT-11s, a business, a spouse, the children in; each return out line by
 * line (PIT-37 or PIT-36 on the scale, PIT-36L, PIT-28) with what is to pay or paid back. A calculator, not tax advice
 */
object AnnualApp:
  private def form(w: Words, a: AnnualWords): Form[Filed] =
    def money(label: String) = Field.money(label, "PLN").optional
    val lump = Vector("12% (IT)" -> "0.12", "8,5%" -> "0.085", "14%" -> "0.14", "15%" -> "0.15", "17%" -> "0.17")
      .map((l, r) => s"${w.lump} $l" -> Option[TaxForm](TaxForm.Lump(BigDecimal(r))))
    val forms = (a.none -> Option.empty[TaxForm]) +: (w.scale -> Option[TaxForm](TaxForm.Scale)) +:
      (w.linear -> Option[TaxForm](TaxForm.Linear)) +: lump
    Form((money(a.revenue), money(a.costs), money(a.social), money(a.health), money(a.advances),
      Field.choice(a.form, forms*), money(a.bRevenue), money(a.bCosts), money(a.bSocial), money(a.bHealth),
      money(a.bAdvances), money(a.spouse + a.revenue), money(a.spouse + a.costs), money(a.spouse + a.social),
      money(a.spouse + a.health), money(a.spouse + a.advances), Field.whole(a.children).optional,
      Field.flag(a.singleParent))).as[Filed]

  /** the form, then the returns; `back` the calculator */
  def main[R[_]](w: Words, last: Option[Filed], back: => Unit ! R)(using In[Ui, R]): Unit ! R =
    val a = AnnualWords.of(w.lang)
    val f = last.fold(form(w, a))(form(w, a).filledWith)
    show(Screen[Option[Filed]](a.title)(Part.text(a.lead), f, Part.info(w.notAdvice))(
      Button.submit(a.compute, f)(Some(_)), Button(a.calculator)(None))).flatMap {
      case None => back
      case Some(filed) if filed.jobs.isEmpty && filed.business.isEmpty && filed.spouse.isEmpty =>
        show(Screen[Unit](a.title)(Part.problem(a.nothing))(Button(w.back)(()))).flatMap(_ =>
          main[R](w, Some(filed), back))
      case Some(filed) => result[R](w, a, filed, back)
    }

  private def returned(a: AnnualWords, r: Return): Part =
    val due = r.due
    val said = if due > 0 then Part.warning(s"${a.toPay}: ${App.zl(due)}")
      else if due < 0 then Part.info(s"${a.back}: ${App.zl(-due)}") else Part.info(a.even)
    Part.section(r.form)(said, Part.details(r.lines.filterNot((k, v) => k.startsWith("children") && v == 0)
      .map((k, v) => a.line(k) -> App.zl(v))*))

  def result[R[_]](w: Words, a: AnnualWords, filed: Filed, back: => Unit ! R)(using In[Ui, R]): Unit ! R =
    val returns = Annual.year(PayTheory.y2026, filed.jobs, filed.business, filed.spouse,
      filed.children.fold(0)(_.toInt), filed.singleParent)
    val total = returns.map(_.due).sum
    val sum = if returns.size < 2 then Vector.empty
      else Vector(if total > 0 then Part.warning(s"${a.toPay}: ${App.zl(total)}")
        else if total < 0 then Part.info(s"${a.back}: ${App.zl(-total)}") else Part.info(a.even))
    val parts = (sum ++ returns.map(returned(a, _))) :+ Part.text(a.law) :+ Part.info(w.notAdvice)
    show(Screen[Boolean](a.result)(parts*)(Button(a.change)(true), Button(a.calculator)(false))).flatMap { change =>
      if change then main[R](w, Some(filed), back) else back }
