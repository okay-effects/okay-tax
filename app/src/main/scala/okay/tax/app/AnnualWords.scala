package okay.tax.app

/** the words of the year's return, in Polish, English, Russian and Ukrainian */
final case class AnnualWords(open: String, fromCalculator: String, title: String, lead: String, revenue: String,
    costs: String, social: String, health: String, advances: String, form: String, none: String, bRevenue: String,
    bCosts: String, bSocial: String, bHealth: String, bAdvances: String, spouse: String, children: String,
    singleParent: String, compute: String, nothing: String, result: String, toPay: String, back: String,
    even: String, calculator: String, change: String, law: String, lines: Map[String, String]):
  def line(key: String): String = lines.getOrElse(key, key)

object AnnualWords:
  val pl = AnnualWords(open = "Zeznanie roczne (PIT za 2026)", fromCalculator = "Zeznanie roczne z tą pensją",
    title = "Zeznanie roczne za 2026: PIT-37, PIT-36, PIT-36L, PIT-28",
    lead = "Wpisz sumy z PIT-11 wszystkich pracodawców, liczby z działalności za rok, a przy rozliczeniu wspólnym " +
      "— PIT-11 małżonka. Puste pola to zero. Ulga na dzieci liczona za cały rok.",
    revenue = "PIT-11: przychód", costs = "PIT-11: koszty uzyskania", social = "PIT-11: składki społeczne",
    health = "PIT-11: składka zdrowotna", advances = "PIT-11: zaliczki pobrane", form = "Działalność: forma",
    none = "brak działalności", bRevenue = "Działalność: przychód za rok", bCosts = "Działalność: koszty",
    bSocial = "Działalność: składki społeczne", bHealth = "Działalność: składki zdrowotne",
    bAdvances = "Działalność: zaliczki wpłacone", spouse = "Małżonek, wspólnie — ",
    children = "Dzieci małoletnie (cały rok)", singleParent = "Samotnie wychowuję dzieci", compute = "Policz PIT",
    nothing = "Wpisz przychód z PIT-11 albo z działalności.", result = "Zeznanie roczne", toPay = "Do zapłaty",
    back = "Zwrot", even = "Nic do zapłaty, nic do zwrotu", calculator = "Kalkulator", change = "Zmień",
    law = "Skala: art. 27; wspólnie: art. 6 ust. 2; samotny rodzic: art. 6 ust. 4c; ulga na dzieci: art. 27f; " +
      "liniowy: art. 30c (ustawa o PIT); ryczałt: art. 11 i 12 (ustawa o ryczałcie); zaokrąglenia: art. 63 " +
      "Ordynacji podatkowej.",
    lines = Map("revenue" -> "Przychód", "costs" -> "Koszty", "income" -> "Dochód", "social" -> "Składki społeczne",
      "health.deducted" -> "Składki zdrowotne odliczone", "base" -> "Podstawa obliczenia podatku",
      "tax" -> "Podatek", "children" -> "Ulga na dzieci odliczona", "tax.due" -> "Podatek należny",
      "advances" -> "Zaliczki", "children.back" -> "Zwrot niewykorzystanej ulgi na dzieci",
      "due" -> "Do zapłaty (minus: zwrot)"))
  val en = AnnualWords(open = "The year's return (PIT for 2026)", fromCalculator = "The year's return at this pay",
    title = "The return for 2026: PIT-37, PIT-36, PIT-36L, PIT-28",
    lead = "Enter the sums of all your employers' PIT-11s, your business's year, and for a joint return your " +
      "spouse's PIT-11. An empty field is zero. The children's relief is for the whole year.",
    revenue = "PIT-11: revenue", costs = "PIT-11: costs", social = "PIT-11: social contributions",
    health = "PIT-11: health contribution", advances = "PIT-11: advances withheld", form = "Business: tax form",
    none = "no business", bRevenue = "Business: the year's revenue", bCosts = "Business: costs",
    bSocial = "Business: social contributions", bHealth = "Business: health contributions",
    bAdvances = "Business: advances paid", spouse = "Spouse, jointly — ",
    children = "Children under 18 (the whole year)", singleParent = "I raise my children alone",
    compute = "Compute the PIT", nothing = "Enter a PIT-11 revenue or a business's.", result = "The year's return",
    toPay = "To pay", back = "Paid back", even = "Nothing to pay, nothing back", calculator = "Calculator",
    change = "Change",
    law = "The scale: art. 27; jointly: art. 6(2); a single parent: art. 6(4c); the children's relief: art. 27f; " +
      "linear: art. 30c (the PIT act); ryczałt: art. 11 and 12 (the ryczałt act); rounding: art. 63 of the Tax " +
      "Ordinance.",
    lines = Map("revenue" -> "Revenue", "costs" -> "Costs", "income" -> "Income", "social" -> "Social contributions",
      "health.deducted" -> "Health contributions deducted", "base" -> "Tax base", "tax" -> "Tax",
      "children" -> "Children's relief deducted", "tax.due" -> "Tax due", "advances" -> "Advances",
      "children.back" -> "Children's relief paid back", "due" -> "To pay (minus: paid back)"))
  val ru = AnnualWords(open = "Годовая декларация (PIT за 2026)",
    fromCalculator = "Годовая декларация с этой зарплатой",
    title = "Декларация за 2026: PIT-37, PIT-36, PIT-36L, PIT-28",
    lead = "Введите суммы из PIT-11 всех работодателей, цифры фирмы за год, а при совместной декларации — PIT-11 " +
      "супруга. Пустое поле — ноль. Льгота на детей считается за весь год.",
    revenue = "PIT-11: доход (przychód)", costs = "PIT-11: расходы", social = "PIT-11: социальные взносы",
    health = "PIT-11: взнос на здоровье", advances = "PIT-11: удержанные авансы", form = "Фирма: форма налога",
    none = "нет фирмы", bRevenue = "Фирма: выручка за год", bCosts = "Фирма: расходы",
    bSocial = "Фирма: социальные взносы", bHealth = "Фирма: взносы на здоровье",
    bAdvances = "Фирма: уплаченные авансы", spouse = "Супруг(а), совместно — ",
    children = "Несовершеннолетние дети (весь год)", singleParent = "Воспитываю детей один (одна)",
    compute = "Посчитать PIT", nothing = "Введите доход из PIT-11 или выручку фирмы.", result = "Годовая декларация",
    toPay = "К доплате", back = "К возврату", even = "Ни доплаты, ни возврата", calculator = "Калькулятор",
    change = "Изменить",
    law = "Шкала: ст. 27; совместно: ст. 6 п. 2; одинокий родитель: ст. 6 п. 4c; льгота на детей: ст. 27f; " +
      "линейный: ст. 30c (закон о PIT); ryczałt: ст. 11 и 12 (закон о ryczałt); округление: ст. 63 Налогового " +
      "кодекса (Ordynacja podatkowa).",
    lines = Map("revenue" -> "Доход (przychód)", "costs" -> "Расходы", "income" -> "Доход (dochód)",
      "social" -> "Социальные взносы", "health.deducted" -> "Взносы на здоровье вычтены",
      "base" -> "База налога", "tax" -> "Налог", "children" -> "Льгота на детей вычтена",
      "tax.due" -> "Налог к уплате", "advances" -> "Авансы", "children.back" -> "Возврат неиспользованной льготы",
      "due" -> "К доплате (минус: возврат)"))
  val uk = AnnualWords(open = "Річна декларація (PIT за 2026)",
    fromCalculator = "Річна декларація з цією зарплатою",
    title = "Декларація за 2026: PIT-37, PIT-36, PIT-36L, PIT-28",
    lead = "Введіть суми з PIT-11 усіх роботодавців, цифри фірми за рік, а для спільної декларації — PIT-11 " +
      "чоловіка чи дружини. Порожнє поле — нуль. Пільга на дітей рахується за весь рік.",
    revenue = "PIT-11: дохід (przychód)", costs = "PIT-11: витрати", social = "PIT-11: соціальні внески",
    health = "PIT-11: внесок на здоров'я", advances = "PIT-11: утримані аванси", form = "Фірма: форма податку",
    none = "немає фірми", bRevenue = "Фірма: виручка за рік", bCosts = "Фірма: витрати",
    bSocial = "Фірма: соціальні внески", bHealth = "Фірма: внески на здоров'я",
    bAdvances = "Фірма: сплачені аванси", spouse = "Чоловік / дружина, спільно — ",
    children = "Неповнолітні діти (весь рік)", singleParent = "Виховую дітей сам (сама)",
    compute = "Порахувати PIT", nothing = "Введіть дохід з PIT-11 або виручку фірми.", result = "Річна декларація",
    toPay = "До сплати", back = "До повернення", even = "Ні доплати, ні повернення", calculator = "Калькулятор",
    change = "Змінити",
    law = "Шкала: ст. 27; спільно: ст. 6 п. 2; самотній батько чи мати: ст. 6 п. 4c; пільга на дітей: ст. 27f; " +
      "лінійний: ст. 30c (закон про PIT); ryczałt: ст. 11 і 12 (закон про ryczałt); округлення: ст. 63 " +
      "Податкового кодексу (Ordynacja podatkowa).",
    lines = Map("revenue" -> "Дохід (przychód)", "costs" -> "Витрати", "income" -> "Дохід (dochód)",
      "social" -> "Соціальні внески", "health.deducted" -> "Внески на здоров'я вирахувано",
      "base" -> "База податку", "tax" -> "Податок", "children" -> "Пільгу на дітей вирахувано",
      "tax.due" -> "Податок до сплати", "advances" -> "Аванси", "children.back" -> "Повернення невикористаної пільги",
      "due" -> "До сплати (мінус: повернення)"))
  def of(lang: String): AnnualWords = lang match
    case "en" => en
    case "ru" => ru
    case "uk" => uk
    case _ => pl
