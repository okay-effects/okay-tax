package okay.tax

import scala.math.BigDecimal.RoundingMode

/** ONE EMPLOYER'S YEAR, as its PIT-11 says it: the revenue, the costs, the contributions withheld, the advances */
final case class Pit11(revenue: BigDecimal, costs: BigDecimal, social: BigDecimal, health: BigDecimal,
    advances: BigDecimal)

object Pit11:
  /** the PIT-11 of a year of UoP at `gross` a month, as `Pay.employment` withholds it */
  def of(t: Pay.Theory, gross: BigDecimal): Pit11 =
    val y = Pay.employment(t, gross)
    Pit11(gross * 12, t("pit.employee.costs").value * 12, y.months.map(_.social).sum, y.months.map(_.health).sum,
      y.months.map(_.tax).sum)

/** A JDG'S YEAR, as its books say it: the revenue, the costs, the contributions paid, the advances paid */
final case class Books(revenue: BigDecimal, costs: BigDecimal, social: BigDecimal, health: BigDecimal,
    advances: BigDecimal)

object Books:
  /** the books of a year of `Jdg.year`: the labour fund among the costs (on the scale and the linear tax) */
  def of(t: Pay.Theory, revenue: BigDecimal, costs: BigDecimal, form: Form, zus: Zus, sickness: Boolean): Books =
    val y = Jdg.year(t, revenue, costs, form, zus, sickness)
    Books(revenue * 12, y.months.map(_.other).sum, y.months.map(_.social).sum, y.months.map(_.health).sum,
      y.months.map(_.tax).sum)

/** what one person earned in a year on the scale: employers' PIT-11s, and a JDG's books if it is on the scale */
final case class Income(jobs: Vector[Pit11] = Vector.empty, business: Option[Books] = None):
  /** each source's income (a source's loss is not set against another's in the year) */
  def income: BigDecimal = jobs.map(j => (j.revenue - j.costs).max(0)).sum +
    business.fold(BigDecimal(0))(b => (b.revenue - b.costs).max(0))
  def revenue: BigDecimal = jobs.map(_.revenue).sum + business.fold(BigDecimal(0))(_.revenue)
  def costs: BigDecimal = jobs.map(_.costs).sum + business.fold(BigDecimal(0))(_.costs)
  def social: BigDecimal = jobs.map(_.social).sum + business.fold(BigDecimal(0))(_.social)
  def health: BigDecimal = jobs.map(_.health).sum + business.fold(BigDecimal(0))(_.health)
  def advances: BigDecimal = jobs.map(_.advances).sum + business.fold(BigDecimal(0))(_.advances)
  /** the income less the social contributions, each person's own (art. 26 ust. 1 pkt 2) */
  def base: BigDecimal = (income - social).max(0)

/**
 * A RETURN: the form's name and its lines in order — a key the page words, and its value. `due` more than 0 is to
 * pay, less than 0 is paid back
 */
final case class Return(form: String, lines: Vector[(String, BigDecimal)], notes: Vector[String] = Vector.empty):
  def apply(key: String): BigDecimal = lines.find(_._1 == key).fold(BigDecimal(0))(_._2)
  def due: BigDecimal = apply("due")

/**
 * THE YEAR'S RETURNS (specs/tax.md §4): PIT-37 (employment alone on the scale), PIT-36 (with a JDG on the scale),
 * PIT-36L (a JDG on the linear tax), PIT-28 (ryczałt). The bases and the taxes rounded to full złoty (Ordynacja
 * podatkowa art. 63 § 1); the advances paid set against the tax.
 */
object Annual:
  import Pay.Theory
  private def zloty(x: BigDecimal): BigDecimal = x.setScale(0, RoundingMode.HALF_UP)
  private def grosz(x: BigDecimal): BigDecimal = x.setScale(2, RoundingMode.HALF_UP)

  /** the scale of art. 27 ust. 1 on a base, unrounded */
  def scaleTax(t: Theory, base: BigDecimal): BigDecimal =
    def v(k: String) = t(k).value
    val low = base.min(v("pit.threshold"))
    (low * v("pit.rate1") + (base - low).max(0) * v("pit.rate2") - v("pit.reducing")).max(0)

  /** the children's relief of a year (art. 27f ust. 2): 92.67 a month the first and the second child, 166.67 the
   * third, 225 each next; one child only within the income limit (`limitIncome`: art. 27f ust. 2a) */
  def childRelief(t: Theory, children: Int, married: Boolean, singleParent: Boolean,
      limitIncome: BigDecimal): BigDecimal =
    def v(k: String) = t(k).value
    val limit = if married || singleParent then v("child.limit.married") else v("child.limit.single")
    if children <= 0 || children == 1 && limitIncome > limit then 0
    else (1 to children).map(i => if i <= 2 then v("child.month") else if i == 3 then v("child.third.month")
      else v("child.fourth.month")).sum * 12

  /**
   * ON THE SCALE: PIT-37, or PIT-36 when a JDG on the scale is among the incomes. With `spouse` the spouses' joint
   * return (art. 6 ust. 2: twice the tax of half their bases); `singleParent` the same on one's own (art. 6 ust. 4c).
   * The children's relief from the tax, the part the tax could not take paid back up to the contributions (art. 27f
   * ust. 8–10)
   */
  def scale(t: Theory, own: Income, spouse: Option[Income] = None, children: Int = 0,
      singleParent: Boolean = false): Return =
    val all = own +: spouse.toVector
    val base = zloty(all.map(_.base).sum)
    val halved = spouse.nonEmpty || singleParent && children > 0
    val tax = zloty(if halved then scaleTax(t, base / 2) * 2 else scaleTax(t, base))
    val relief = childRelief(t, children, spouse.nonEmpty, singleParent, all.map(_.base).sum)
    val taken = relief.min(tax)
    val back = (relief - taken).min(all.map(i => i.social + i.health).sum)
    val taxDue = zloty(tax - taken)
    val advances = all.map(_.advances).sum
    Return(if all.exists(_.business.nonEmpty) then "PIT-36" else "PIT-37", Vector(
      "revenue" -> all.map(_.revenue).sum, "costs" -> all.map(_.costs).sum, "income" -> all.map(_.income).sum,
      "social" -> all.map(_.social).sum, "base" -> base, "tax" -> tax, "children" -> taken, "tax.due" -> taxDue,
      "advances" -> advances, "children.back" -> back, "due" -> (taxDue - advances - back)))

  /** PIT-36L: 19% of the income less the social contributions and the health ones up to the year's limit */
  def linear(t: Theory, b: Books): Return =
    def v(k: String) = t(k).value
    val income = (b.revenue - b.costs).max(0)
    val health = b.health.min(v("health.linear.limit"))
    val base = zloty((income - b.social - health).max(0))
    val tax = zloty(base * v("pit.linear"))
    Return("PIT-36L", Vector("revenue" -> b.revenue, "costs" -> b.costs, "income" -> income, "social" -> b.social,
      "health.deducted" -> health, "base" -> base, "tax" -> tax, "tax.due" -> tax, "advances" -> b.advances,
      "due" -> (tax - b.advances)))

  /** PIT-28: `rate` of the revenue less the social contributions and half the health ones */
  def lump(t: Theory, b: Books, rate: BigDecimal): Return =
    val health = grosz(b.health * t("lump.health.deducted").value)
    val base = zloty((b.revenue - b.social - health).max(0))
    val tax = zloty(base * rate)
    Return("PIT-28", Vector("revenue" -> b.revenue, "social" -> b.social, "health.deducted" -> health,
      "base" -> base, "tax" -> tax, "tax.due" -> tax, "advances" -> b.advances, "due" -> (tax - b.advances)))

  /** the returns of a year: employment and a JDG on the scale in one; a JDG on the linear tax or ryczałt in its own */
  def year(t: Theory, jobs: Vector[Pit11], business: Option[(Books, Form)], spouse: Option[Income] = None,
      children: Int = 0, singleParent: Boolean = false): Vector[Return] =
    val onScale = business.collect { case (b, Form.Scale) => b }
    val own = Income(jobs, onScale)
    // NO HALVES BESIDE THE LINEAR TAX OR RYCZAŁT (art. 6 ust. 8): jointly or as a single parent only when neither
    // applies; the spouse then files apart, and the return says why
    val excluded = business.exists(_._2 != Form.Scale) && (spouse.nonEmpty || singleParent)
    val (withSpouse, single) = if excluded then (None, false) else (spouse, singleParent)
    val scaled = Option.when(jobs.nonEmpty || onScale.nonEmpty || withSpouse.nonEmpty)(
      scale(t, own, withSpouse, children, single).copy(notes = if excluded then Vector("halves.excluded")
        else Vector.empty))
    scaled.toVector ++ business.collect {
      case (b, Form.Linear) => linear(t, b)
      case (b, Form.Lump(rate)) => lump(t, b, rate) }
