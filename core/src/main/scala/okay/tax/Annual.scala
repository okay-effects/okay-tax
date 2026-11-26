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

/** a past year's loss of the business: its amount and what of it was deducted before */
final case class PastLoss(amount: BigDecimal, before: BigDecimal = 0)

/**
 * WHAT A PERSON DEDUCTS beyond the contributions (specs/tax.md §7): the business's past losses (art. 9 ust. 3, from
 * its own income), the internet, thermal modernisation and donations (art. 26, art. 26h)
 */
final case class Deductions(losses: Vector[PastLoss] = Vector.empty, internet: BigDecimal = 0,
    thermo: BigDecimal = 0, donations: BigDecimal = 0)

/** what one person earned in a year on the scale: employers' PIT-11s, and a JDG's books if it is on the scale */
final case class Income(jobs: Vector[Pit11] = Vector.empty, business: Option[Books] = None,
    deductions: Deductions = Deductions()):
  def businessIncome: BigDecimal = business.fold(BigDecimal(0))(b => (b.revenue - b.costs).max(0))
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
    childRelief(t, Vector.fill(children.max(0))(12), married, singleParent, limitIncome)

  /**
   * THE RELIEF MONTH BY MONTH (art. 27f ust. 2: "za każdy miesiąc kalendarzowy"): `months` each child's months in the
   * year; a month's amount by the number of children in it (the months taken together, so which months does not
   * change the sum). The one-child limit only when there never was more than one child (ust. 2b)
   */
  def childRelief(t: Theory, months: Vector[Int], married: Boolean, singleParent: Boolean,
      limitIncome: BigDecimal): BigDecimal =
    def v(k: String) = t(k).value
    def nth(i: Int) = if i <= 2 then v("child.month") else if i == 3 then v("child.third.month")
      else v("child.fourth.month")
    val limit = if married || singleParent then v("child.limit.married") else v("child.limit.single")
    val ms = months.map(_.max(0).min(12)).filter(_ > 0)
    if ms.isEmpty || ms.size == 1 && limitIncome > limit then 0
    else (1 to 12).map(m => (1 to ms.count(_ >= m)).map(nth).sum).sum

  /** what of past losses may come off this year (art. 9 ust. 3): a loss up to 5 000 000 not yet touched may come off
   * at once; else at most half of it a year. Losses older than five years are not given (the caller's to leave out) */
  def lossAllowed(t: Theory, losses: Vector[PastLoss]): BigDecimal =
    def v(k: String) = t(k).value
    losses.map { l =>
      val rest = (l.amount - l.before).max(0)
      if l.before == 0 && l.amount <= v("loss.once") then rest else rest.min(l.amount * v("loss.share")) }.sum

  /**
   * ONE PERSON'S DEDUCTIONS ON THE SCALE, in order, and the base they leave: the business's past loss from its own
   * income, the social contributions, the internet (up to 760), thermal modernisation (up to 53 000), donations (up
   * to 6% of the income less the contributions); none below zero
   */
  def deducted(t: Theory, i: Income): (Vector[(String, BigDecimal)], BigDecimal) =
    def v(k: String) = t(k).value
    val d = i.deductions
    val loss = lossAllowed(t, d.losses).min(i.businessIncome)
    val afterSocial = (i.income - loss - i.social).max(0)
    val internet = d.internet.max(0).min(v("deduct.internet")).min(afterSocial)
    val thermo = d.thermo.max(0).min(v("deduct.thermo")).min(afterSocial - internet)
    val donations = d.donations.max(0).min(grosz(afterSocial * v("deduct.donations")))
      .min(afterSocial - internet - thermo)
    (Vector("loss" -> loss, "internet" -> internet, "thermo" -> thermo, "donations" -> donations).filter(_._2 > 0),
      afterSocial - internet - thermo - donations)

  /**
   * ON THE SCALE: PIT-37, or PIT-36 when a JDG on the scale is among the incomes. With `spouse` the spouses' joint
   * return (art. 6 ust. 2: twice the tax of half their bases); `singleParent` the same on one's own (art. 6 ust. 4c).
   * The children's relief from the tax (`children` the whole year, `childMonths` each one's months of a part), the
   * part the tax could not take paid back up to the contributions (art. 27f ust. 8–10)
   */
  def scale(t: Theory, own: Income, spouse: Option[Income] = None, children: Int = 0,
      singleParent: Boolean = false, childMonths: Vector[Int] = Vector.empty): Return =
    val all = own +: spouse.toVector
    val each = all.map(deducted(t, _))
    val deductions = each.flatMap(_._1).groupMapReduce(_._1)(_._2)(_ + _)
    val base = zloty(each.map(_._2).sum)
    val months = Vector.fill(children.max(0))(12) ++ childMonths.filter(_ > 0)
    val halved = spouse.nonEmpty || singleParent && months.nonEmpty
    val tax = zloty(if halved then scaleTax(t, base / 2) * 2 else scaleTax(t, base))
    val relief = childRelief(t, months, spouse.nonEmpty, singleParent, all.map(_.base).sum)
    val taken = relief.min(tax)
    val back = (relief - taken).min(all.map(i => i.social + i.health).sum)
    val taxDue = zloty(tax - taken)
    val advances = all.map(_.advances).sum
    Return(if all.exists(_.business.nonEmpty) then "PIT-36" else "PIT-37", Vector(
      "revenue" -> all.map(_.revenue).sum, "costs" -> all.map(_.costs).sum, "income" -> all.map(_.income).sum,
      "social" -> all.map(_.social).sum) ++ Vector("loss", "internet", "thermo", "donations").flatMap(k =>
      deductions.get(k).map(k -> _)) ++ Vector("base" -> base, "tax" -> tax, "children" -> taken,
      "tax.due" -> taxDue, "advances" -> advances, "children.back" -> back, "due" -> (taxDue - advances - back)))

  /** PIT-36L: 19% of the income less a past loss, the social contributions, the health ones up to the year's limit
   * and thermal modernisation (art. 26h: from the base of art. 30c ust. 2 too) */
  def linear(t: Theory, b: Books, d: Deductions = Deductions()): Return =
    def v(k: String) = t(k).value
    val income = (b.revenue - b.costs).max(0)
    val loss = lossAllowed(t, d.losses).min(income)
    val health = b.health.min(v("health.linear.limit"))
    val before = (income - loss - b.social - health).max(0)
    val thermo = d.thermo.max(0).min(v("deduct.thermo")).min(before)
    val base = zloty(before - thermo)
    val tax = zloty(base * v("pit.linear"))
    Return("PIT-36L", Vector("revenue" -> b.revenue, "costs" -> b.costs, "income" -> income) ++
      Option.when(loss > 0)("loss" -> loss) ++ Vector("social" -> b.social, "health.deducted" -> health) ++
      Option.when(thermo > 0)("thermo" -> thermo) ++ Vector("base" -> base, "tax" -> tax, "tax.due" -> tax,
      "advances" -> b.advances, "due" -> (tax - b.advances)))

  /** PIT-28: `rate` of the revenue less the social contributions and half the health ones */
  def lump(t: Theory, b: Books, rate: BigDecimal): Return =
    val health = grosz(b.health * t("lump.health.deducted").value)
    val base = zloty((b.revenue - b.social - health).max(0))
    val tax = zloty(base * rate)
    Return("PIT-28", Vector("revenue" -> b.revenue, "social" -> b.social, "health.deducted" -> health,
      "base" -> base, "tax" -> tax, "tax.due" -> tax, "advances" -> b.advances, "due" -> (tax - b.advances)))

  /**
   * The returns of a year: employment and a JDG on the scale in one; a JDG on the linear tax or ryczałt in its own.
   * `deductions` the person's own: a past loss with the business it came from (on the scale or the linear tax; none
   * on ryczałt here), thermal modernisation with the linear return when there is one, the internet and donations on
   * the scale (art. 26)
   */
  def year(t: Theory, jobs: Vector[Pit11], business: Option[(Books, Form)], spouse: Option[Income] = None,
      children: Int = 0, singleParent: Boolean = false, deductions: Deductions = Deductions(),
      childMonths: Vector[Int] = Vector.empty): Vector[Return] =
    val onScale = business.collect { case (b, Form.Scale) => b }
    val linearly = business.exists(_._2 == Form.Linear)
    val onScaleDeducts = if linearly then deductions.copy(losses = Vector.empty, thermo = 0)
      else if onScale.isEmpty then deductions.copy(losses = Vector.empty) else deductions
    val own = Income(jobs, onScale, onScaleDeducts)
    // NO HALVES BESIDE THE LINEAR TAX OR RYCZAŁT (art. 6 ust. 8): jointly or as a single parent only when neither
    // applies; the spouse then files apart, and the return says why
    val excluded = business.exists(_._2 != Form.Scale) && (spouse.nonEmpty || singleParent)
    val (withSpouse, single) = if excluded then (None, false) else (spouse, singleParent)
    val scaled = Option.when(jobs.nonEmpty || onScale.nonEmpty || withSpouse.nonEmpty)(
      scale(t, own, withSpouse, children, single, childMonths).copy(notes =
        if excluded then Vector("halves.excluded") else Vector.empty))
    scaled.toVector ++ business.collect {
      case (b, Form.Linear) => linear(t, b, Deductions(deductions.losses, thermo = deductions.thermo))
      case (b, Form.Lump(rate)) => lump(t, b, rate) }
