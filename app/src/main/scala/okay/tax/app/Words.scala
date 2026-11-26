package okay.tax.app

/** the page's words in Polish, English, Russian and Ukrainian */
final case class Words(lang: String, title: String, lead: String, gross: String, revenue: String, dayRate: String,
    days: String, lumpRate: String, costs: String, sickness: String, compute: String, notAdvice: String,
    nothing: String, back: String, grossShort: String, scale: String, linear: String, lump: String, zusFull: String,
    zusPreferential: String, zusStart: String, way: String, month: String, year: String, income: String,
    social: String, health: String, tax: String, other: String, net: String, sources: String, sourcesLead: String,
    b2bMonthly: String, result: String, change: String, again: String, sourceNames: Map[String, String]):
  def source(key: String): String = sourceNames.getOrElse(key, key)

object Words:
  private val names = Map(
    "pit" -> "ustawa o PIT, Dz.U. 2026 poz. 592", "lump" -> "ustawa o ryczałcie, Dz.U. 2025 poz. 843",
    "sus" -> "ustawa o systemie ubezpieczeń społecznych, Dz.U. 2026 poz. 1474",
    "health" -> "ustawa o świadczeniach opieki zdrowotnej, Dz.U. 2025 poz. 1461",
    "zus" -> "ZUS: wysokość składek w 2026 r.", "mp" -> "M.P. 2025 poz. 1274")
  val pl = Words("pl", "Ile na rękę? UoP i B2B w 2026", "Wpisz pensję brutto na umowie o pracę i/lub przychód z " +
    "B2B (miesięcznie albo stawkę dzienną i liczbę dni). Policzymy wszystkie warianty: skalę, liniowy, ryczałt, " +
    "każdy ZUS — rok i średni miesiąc.", "UoP: brutto miesięcznie", "B2B: przychód miesięcznie",
    "B2B: stawka dzienna", "B2B: dni w miesiącu", "Stawka ryczałtu", "Koszty firmy miesięcznie",
    "Dobrowolne chorobowe", "Policz", "To kalkulator, nie doradztwo podatkowe: liczby wynikają wprost z przepisów " +
      "(źródła poniżej), decyzja należy do Ciebie.", "Wpisz brutto UoP albo przychód B2B.", "Wróć", "brutto",
    "skala", "liniowy", "ryczałt", "pełny ZUS", "preferencyjny ZUS", "ulga na start", "Wariant",
    "Na rękę / miesiąc", "Na rękę / rok", "Brutto / przychód", "Składki społeczne", "Składka zdrowotna",
    "Podatek (średnio)", "Inne (FP, koszty)", "Na rękę", "Skąd te liczby",
    "Każda wartość z cytatem z przepisu, z którego pochodzi:", "B2B miesięcznie", "Wynik", "Zmień", "Od nowa",
    names)
  val en = Words("en", "Take-home pay in Poland: UoP and B2B, 2026", "Enter a gross salary on an employment " +
    "contract and / or a B2B revenue (a month, or a day rate and days). Every option is computed: the scale, the " +
    "linear tax, ryczałt, each ZUS — the year and the average month.", "UoP: gross a month", "B2B: revenue a month",
    "B2B: day rate", "B2B: days a month", "Ryczałt rate", "Business costs a month", "Voluntary sickness insurance",
    "Compute", "A calculator, not tax advice: the numbers follow from the statutes (sources below); the decision " +
      "is yours.", "Enter a UoP gross or a B2B revenue.", "Back", "gross", "scale", "linear", "ryczałt",
    "full ZUS", "preferential ZUS", "ulga na start", "Option", "Take-home / month", "Take-home / year",
    "Gross / revenue", "Social contributions", "Health contribution", "Tax (average)", "Other (FP, costs)",
    "Take-home", "Where the numbers come from", "Every value with its quote from the statute it comes from:",
    "B2B a month", "Result", "Change", "Start over", names)
  val ru = Words("ru", "Сколько на руки? UoP и B2B в Польше, 2026", "Введите зарплату брутто по трудовому договору " +
    "и/или выручку B2B (в месяц или дневную ставку и число дней). Посчитаем все варианты: шкалу, линейный налог, " +
    "ryczałt, каждый ZUS — за год и в среднем за месяц.", "UoP: брутто в месяц", "B2B: выручка в месяц",
    "B2B: ставка в день", "B2B: дней в месяце", "Ставка ryczałt", "Расходы фирмы в месяц", "Добровольный больничный",
    "Посчитать", "Это калькулятор, а не налоговая консультация: числа следуют прямо из законов (источники ниже), " +
      "решение за вами.", "Введите брутто UoP или выручку B2B.", "Назад", "брутто", "шкала", "линейный",
    "ryczałt", "полный ZUS", "льготный ZUS", "ulga na start", "Вариант", "На руки / месяц", "На руки / год",
    "Брутто / выручка", "Социальные взносы", "Взнос на здоровье", "Налог (в среднем)", "Прочее (FP, расходы)",
    "На руки", "Откуда эти числа", "Каждое значение с цитатой из закона, откуда оно взято:", "B2B в месяц",
    "Результат", "Изменить", "Сначала", names)
  val uk = Words("uk", "Скільки на руки? UoP і B2B у Польщі, 2026", "Введіть зарплату брутто за трудовим договором " +
    "та/або дохід B2B (на місяць або денну ставку й кількість днів). Порахуємо всі варіанти: шкалу, лінійний " +
    "податок, ryczałt, кожен ZUS — за рік і в середньому за місяць.", "UoP: брутто на місяць",
    "B2B: дохід на місяць", "B2B: ставка на день", "B2B: днів на місяць", "Ставка ryczałt", "Витрати фірми на місяць",
    "Добровільний лікарняний", "Порахувати", "Це калькулятор, а не податкова консультація: числа випливають прямо " +
      "із законів (джерела нижче), рішення за вами.", "Введіть брутто UoP або дохід B2B.", "Назад", "брутто",
    "шкала", "лінійний", "ryczałt", "повний ZUS", "пільговий ZUS", "ulga na start", "Варіант", "На руки / місяць",
    "На руки / рік", "Брутто / дохід", "Соціальні внески", "Внесок на здоров'я", "Податок (у середньому)",
    "Інше (FP, витрати)", "На руки", "Звідки ці числа", "Кожне значення з цитатою із закону, звідки воно взяте:",
    "B2B на місяць", "Результат", "Змінити", "Спочатку", names)
  def of(lang: String): Words = lang.take(2).toLowerCase match
    case "en" => en
    case "ru" => ru
    case "uk" => uk
    case _ => pl
