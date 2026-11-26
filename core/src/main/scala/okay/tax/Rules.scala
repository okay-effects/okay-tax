package okay.tax

/** where a rule stands: the source (as `PayTheory`'s), the article, and its words verbatim */
final case class Cite(source: String, where: String, quote: String)

/**
 * A RULE OF THE STATUTES, SAID: how people ask about it (`asks`, by language: phrases joined by ';', the first its
 * title), what it says (`says`, by language), and where it stands (`cites`, each quote checked in its source)
 */
final case class Rule(id: String, asks: Map[String, String], says: Map[String, String], cites: Vector[Cite]):
  def title(lang: String): String = asks.getOrElse(lang, asks("pl")).split(';').head.trim
  def said(lang: String): String = says.getOrElse(lang, says("pl"))

/**
 * THE QUESTIONS ABOUT THE RULES (specs/tax.md §5): a question answered by the rules it names, each with the statute's
 * own words. Found by the question's words (`find`: each word's stem weighed by how few rules have it); a reader that
 * understands the question may choose among the same rules later. Said in Polish, English, Russian and Ukrainian;
 * the law is Polish, so the quotes are.
 */
object Rules:
  private def r(id: String, pl: String, en: String, ru: String, uk: String)(spl: String, sen: String, sru: String,
      suk: String)(cites: Cite*) =
    Rule(id, Map("pl" -> pl, "en" -> en, "ru" -> ru, "uk" -> uk), Map("pl" -> spl, "en" -> sen, "ru" -> sru,
      "uk" -> suk), cites.toVector)
  private def pit(where: String, quote: String) = Cite("pit", s"art. $where ustawy o PIT", quote)
  private def lump(where: String, quote: String) = Cite("lump", s"art. $where ustawy o ryczałcie", quote)

  val all: Vector[Rule] = Vector(
    r("scale", "Skala podatkowa: 12% i 32%; progi podatkowe; drugi próg podatkowy; ile wynosi podatek na skali",
      "The tax scale: 12% and 32%; tax brackets; the second tax bracket; how much is the tax on the scale",
      "Шкала налога: 12% и 32%; налоговые пороги; второй налоговый порог; сколько налог по шкале",
      "Шкала податку: 12% і 32%; податкові пороги; другий податковий поріг; скільки податок за шкалою")(
      "Na skali podatek wynosi 12% podstawy do 120 000 zł, minus kwota zmniejszająca 3600 zł; od nadwyżki ponad " +
        "120 000 zł — 32% (10 800 zł plus 32% nadwyżki).",
      "On the scale the tax is 12% of the base up to 120 000 zł, less the reducing amount of 3 600 zł; above " +
        "120 000 zł it is 32% of the excess (10 800 zł plus 32% of the excess).",
      "По шкале налог — 12% базы до 120 000 zł минус уменьшающая сумма 3600 zł; сверх 120 000 zł — 32% с превышения " +
        "(10 800 zł плюс 32% превышения).",
      "За шкалою податок — 12% бази до 120 000 zł мінус зменшувальна сума 3600 zł; понад 120 000 zł — 32% з " +
        "перевищення (10 800 zł плюс 32% перевищення).")(
      pit("27 ust. 1", "12 % minus kwota zmniejszająca podatek 3600 zł"),
      pit("27 ust. 1", "10 800 zł + 32 % nadwyżki ponad 120 000 zł")),
    r("free", "Kwota wolna od podatku 30 000 zł; kwota zmniejszająca podatek; 300 zł miesięcznie PIT-2",
      "The tax-free amount of 30 000 zł; the reducing amount; 300 zł a month PIT-2",
      "Необлагаемая сумма 30 000 zł (kwota wolna); уменьшающая сумма; 300 zł в месяц PIT-2",
      "Неоподатковувана сума 30 000 zł (kwota wolna); зменшувальна сума; 300 zł на місяць PIT-2")(
      "Kwota zmniejszająca podatek to 3600 zł rocznie: przy 12% pierwsze 30 000 zł podstawy jest w praktyce wolne " +
        "od podatku. Pracodawca (po złożeniu PIT-2) zmniejsza każdą zaliczkę o 1/12 tej kwoty, czyli 300 zł.",
      "The reducing amount is 3 600 zł a year: at 12% the first 30 000 zł of the base is in effect free of tax. An " +
        "employer (given a PIT-2) lowers each monthly advance by 1/12 of it, 300 zł.",
      "Уменьшающая сумма — 3600 zł в год: при 12% первые 30 000 zł базы фактически без налога. Работодатель (если " +
        "подан PIT-2) уменьшает каждый аванс на 1/12 этой суммы, то есть на 300 zł.",
      "Зменшувальна сума — 3600 zł на рік: при 12% перші 30 000 zł бази фактично без податку. Роботодавець (якщо " +
        "подано PIT-2) зменшує кожен аванс на 1/12 цієї суми, тобто на 300 zł.")(
      pit("27 ust. 1", "12 % minus kwota zmniejszająca podatek 3600 zł"),
      pit("31b ust. 1", "płatnik pomniejsza zaliczki o kwotę stanowiącą niewięcej niż 1/12 kwoty zmniejszającej " +
        "podatek")),
    r("employee.costs", "Koszty uzyskania przychodu na etacie; 250 zł; 300 zł dojazd z innej miejscowości; kilka umów " +
      "o pracę 4500 zł",
      "Employee's deductible costs; 250 zł; 300 zł commuting from another town; several employment contracts 4500 zł",
      "Расходы на получение дохода по трудовому договору; 250 zł; 300 zł живу в другом городе; несколько трудовых " +
        "договоров 4500 zł",
      "Витрати на отримання доходу за трудовим договором; 250 zł; 300 zł живу в іншому місті; кілька трудових " +
        "договорів 4500 zł")(
      "Koszty pracownika: 250 zł miesięcznie (do 3000 zł rocznie) przy jednej umowie; przy kilku naraz łącznie do " +
        "4500 zł rocznie; 300 zł miesięcznie (do 3600 zł), gdy mieszkasz poza miejscowością zakładu pracy.",
      "An employee's costs: 250 zł a month (up to 3 000 zł a year) with one contract; with several at once up to " +
        "4 500 zł a year in all; 300 zł a month (up to 3 600 zł) when you live outside the town of the workplace.",
      "Расходы работника: 250 zł в месяц (до 3000 zł в год) при одном договоре; при нескольких сразу — всего до " +
        "4500 zł в год; 300 zł в месяц (до 3600 zł), если живёте не в том населённом пункте, где работа.",
      "Витрати працівника: 250 zł на місяць (до 3000 zł на рік) за одного договору; за кількох одночасно — разом " +
        "до 4500 zł на рік; 300 zł на місяць (до 3600 zł), якщо живете не в тому населеному пункті, де робота.")(
      pit("22 ust. 2 pkt 1", "wynoszą 250 zł miesięcznie, a za rok podatkowy łącznie niewięcej niż 3000 zł"),
      pit("22 ust. 2 pkt 2", "nie mogą przekroczyć łącznie 4500 zł za rok podatkowy"),
      pit("22 ust. 2 pkt 3", "wynoszą 300 zł miesięcznie, a za rok podatkowy łącznie niewięcej niż 3600 zł")),
    r("joint", "Wspólne rozliczenie z małżonkiem; rozliczenie z żoną; rozliczenie z mężem",
      "Filing jointly with a spouse; joint return with my wife; joint return with my husband",
      "Совместная декларация с супругом; рассчитаться вместе с женой; рассчитаться вместе с мужем",
      "Спільна декларація з подружжям; розрахуватися разом з дружиною; розрахуватися разом з чоловіком")(
      "Małżonkowie przez cały rok (albo od ślubu do końca roku) mogą na wspólny wniosek w zeznaniu rozliczyć się " +
        "łącznie: podatek to dwukrotność podatku od połowy ich łącznych dochodów. Każdy najpierw odlicza swoje.",
      "Spouses married the whole year (or from the wedding to its end) may file jointly, asked for in the return: " +
        "the tax is twice the tax of half their joint income. Each deducts their own first.",
      "Супруги весь год (или со свадьбы до конца года) могут по совместному заявлению в декларации считать налог " +
        "вместе: налог равен удвоенному налогу с половины их общего дохода. Каждый сначала вычитает своё.",
      "Подружжя весь рік (або від весілля до кінця року) може за спільною заявою в декларації рахувати податок " +
        "разом: податок дорівнює подвоєному податку з половини їхнього спільного доходу. Кожен спершу віднімає своє.")(
      pit("6 ust. 2", "na wspólny wniosek wyrażony w zeznaniu podatkowym, opodatkowani łącznie od sumy"),
      pit("6 ust. 2", "w podwójnej wysokości podatku obliczonego od połowy łącznych dochodów małżonków")),
    r("halves.excluded", "Wspólne rozliczenie a liniowy lub ryczałt; czy na ryczałcie mogę rozliczyć się razem z żoną; " +
      "liniowy wyklucza rozliczenie z małżonkiem",
      "Joint filing and the linear tax or ryczałt; can I file together with my wife on ryczałt; linear excludes filing with a spouse",
      "Совместная декларация и линейный налог или ryczałt; можно ли на ryczałt рассчитаться вместе с женой; " +
        "линейный исключает совместную декларацию",
      "Спільна декларація і лінійний податок або ryczałt; чи можна на ryczałt розрахуватися разом з дружиною; " +
        "лінійний виключає спільну декларацію")(
      "Wspólnego rozliczenia z małżonkiem ani jako samotny rodzic nie ma, jeśli choćby jeden z małżonków (albo " +
        "rodzic lub dziecko) stosuje podatek liniowy (art. 30c) albo ryczałt. Wtedy każdy rozlicza się osobno.",
      "There is no joint return, nor one as a single parent, if even one spouse (or the parent or the child) is on " +
        "the linear tax (art. 30c) or ryczałt. Each then files on their own.",
      "Совместной декларации и декларации одинокого родителя нет, если хотя бы один из супругов (или родитель, или " +
        "ребёнок) платит линейный налог (ст. 30c) или ryczałt. Тогда каждый подаёт отдельно.",
      "Спільної декларації і декларації самотнього батька чи матері немає, якщо хоча б один із подружжя (або " +
        "батько чи дитина) сплачує лінійний податок (ст. 30c) або ryczałt. Тоді кожен подає окремо.")(
      pit("6 ust. 8", "nie ma zastosowania, w przypadku gdy chociażby jeden z małżonków, osoba samotnie wychowująca " +
        "dziecko lub jej dziecko")),
    r("single.parent", "Samotny rodzic; samotnie wychowuję dziecko; rozliczenie z dzieckiem",
      "A single parent; I raise my child alone; filing with a child",
      "Одинокий родитель; воспитываю ребёнка один; декларация вместе с ребёнком",
      "Самотній батько чи мати; виховую дитину сам; декларація разом з дитиною")(
      "Rodzic samotnie wychowujący dziecko (małoletnie, albo do 25 lat uczące się, albo pobierające zasiłek " +
        "pielęgnacyjny) liczy podatek jak małżonkowie: dwukrotność podatku od połowy swoich dochodów.",
      "A parent raising a child alone (under 18, or up to 25 and in school, or on a care allowance) counts the tax as " +
        "spouses do: twice the tax of half their income.",
      "Родитель, один воспитывающий ребёнка (несовершеннолетнего, или до 25 лет учащегося, или с пособием по уходу), " +
        "считает налог как супруги: удвоенный налог с половины своего дохода.",
      "Батько чи мати, що сам виховує дитину (неповнолітню, або до 25 років, що навчається, або з допомогою на " +
        "догляд), рахує податок як подружжя: подвоєний податок з половини свого доходу.")(
      pit("6 ust. 4d", "podatek jest określany w podwójnej wysokości podatku obliczonego od połowy dochodów osoby " +
        "samotnie wychowującej dzieci")),
    r("children", "Ulga na dzieci; ulga prorodzinna; ile wynosi ulga na dziecko; limit dochodu na jedno dziecko",
      "The children's relief; the relief for a child; how much is the child relief; the income limit for one child",
      "Льгота на детей; налоговая льгота на ребёнка; сколько льгота на ребёнка; лимит дохода на одного ребёнка",
      "Пільга на дітей; податкова пільга на дитину; скільки пільга на дитину; ліміт доходу на одну дитину")(
      "Od podatku na skali odliczasz za każdy miesiąc: 92,67 zł na pierwsze i drugie dziecko, 166,67 zł na trzecie, " +
        "225 zł na czwarte i każde następne. Przy jednym dziecku tylko gdy dochody nie przekroczyły 112 000 zł " +
        "(małżonkowie razem, samotny rodzic) albo 56 000 zł (osoba bez małżonka).",
      "From the tax on the scale you deduct for each month: 92.67 zł for the first and the second child, 166.67 zł " +
        "for the third, 225 zł for the fourth and each next. With one child only if the income was at most " +
        "112 000 zł (spouses together, a single parent) or 56 000 zł (a person without a spouse).",
      "Из налога по шкале вычитаете за каждый месяц: 92,67 zł на первого и второго ребёнка, 166,67 zł на третьего, " +
        "225 zł на четвёртого и каждого следующего. При одном ребёнке — только если доход не превысил 112 000 zł " +
        "(супруги вместе, одинокий родитель) или 56 000 zł (человек без супруга).",
      "З податку за шкалою віднімаєте за кожен місяць: 92,67 zł на першу і другу дитину, 166,67 zł на третю, " +
        "225 zł на четверту й кожну наступну. З однією дитиною — лише якщо дохід не перевищив 112 000 zł " +
        "(подружжя разом, самотній батько чи мати) або 56 000 zł (людина без подружжя).")(
      pit("27f ust. 2", "jednego małoletniego dziecka – kwota 92,67 zł"),
      pit("27f ust. 2", "166,67 zł na trzecie dziecko"), pit("27f ust. 2", "225 zł na czwarte i każde kolejne dziecko"),
      pit("27f ust. 2", "nie przekroczyły w roku podatkowym kwoty 112 000 zł"),
      pit("27f ust. 2", "nie przekroczyły w roku podatkowym kwoty 56 000 zł")),
    r("children.back", "Zwrot niewykorzystanej ulgi na dzieci; za mały podatek na ulgę; zwrot ulgi do wysokości " +
      "składek",
      "The unused children's relief paid back; tax too small for the relief; the relief back up to the contributions",
      "Возврат неиспользованной льготы на детей; налог меньше льготы; возврат льготы до суммы взносов",
      "Повернення невикористаної пільги на дітей; податок менший за пільгу; повернення пільги до суми внесків")(
      "Gdy podatek jest mniejszy niż ulga na dzieci, różnicę dostajesz z powrotem — ale nie więcej niż suma " +
        "zapłaconych składek społecznych i zdrowotnych (małżonków razem).",
      "When the tax is less than the children's relief, the difference is paid back — but no more than the social " +
        "and health contributions paid (the spouses' together).",
      "Если налог меньше льготы на детей, разницу возвращают — но не больше суммы уплаченных социальных и " +
        "медицинских взносов (супругов вместе).",
      "Якщо податок менший за пільгу на дітей, різницю повертають — але не більше суми сплачених соціальних і " +
        "медичних внесків (подружжя разом).")(
      pit("27f ust. 8", "podatnikowi przysługuje kwota stanowiąca różnicę między kwotą przysługującego podatnikowi " +
        "odliczenia a kwotą odliczoną w zeznaniu podatkowym"),
      pit("27f ust. 9", "Kwota stanowiąca różnicę, o której mowa w ust. 8, nie może przekroczyć sumy")),
    r("linear", "Podatek liniowy 19%; jak wybrać liniowy; termin wyboru podatku liniowego; liniowy a ulga na dzieci",
      "The linear tax of 19%; how to choose the linear tax; the deadline to choose the linear tax; linear and the " +
        "children's relief",
      "Линейный налог 19%; как выбрать линейный; срок выбора линейного налога; линейный и льгота на детей",
      "Лінійний податок 19%; як обрати лінійний; строк вибору лінійного податку; лінійний і пільга на дітей")(
      "Podatek liniowy to 19% dochodu z działalności. Wybierasz go pisemnym oświadczeniem do 20. dnia miesiąca po " +
        "pierwszym przychodzie w roku (wybór obowiązuje też w następnych latach). Ulga na dzieci odlicza się tylko " +
        "od podatku na skali, więc nie od liniowego.",
      "The linear tax is 19% of the business income. You choose it by a written statement by the 20th of the month " +
        "after the year's first revenue (the choice stands in the next years too). The children's relief comes off " +
        "the tax on the scale only, so not off the linear tax.",
      "Линейный налог — 19% дохода от фирмы. Выбирается письменным заявлением до 20-го числа месяца после первого " +
        "дохода в году (выбор действует и в следующие годы). Льгота на детей вычитается только из налога по шкале, " +
        "поэтому не из линейного.",
      "Лінійний податок — 19% доходу від фірми. Обирається письмовою заявою до 20-го числа місяця після першого " +
        "доходу в році (вибір діє й у наступні роки). Пільга на дітей віднімається лише від податку за шкалою, " +
        "тому не від лінійного.")(
      pit("30c ust. 1", "19 % podstawy obliczenia podatku"),
      pit("9a ust. 2", "do 20. dnia miesiąca następującego po miesiącu, w którym został osiągnięty pierwszy przychód " +
        "z tego tytułu w roku podatkowym"),
      pit("27f ust. 1", "Od podatku dochodowego obliczonego zgodnie z art. 27 podatnik ma prawo odliczyć")),
    r("linear.health", "Składka zdrowotna na liniowym 4,9%; odliczenie składki zdrowotnej na liniowym; limit 14 100 zł",
      "The health contribution on the linear tax 4.9%; deducting the health contribution on linear; the 14 100 zł limit",
      "Взнос на здоровье на линейном 4,9%; вычет взноса на здоровье на линейном; лимит 14 100 zł",
      "Внесок на здоров'я на лінійному 4,9%; вирахування внеску на здоров'я на лінійному; ліміт 14 100 zł")(
      "Na liniowym składka zdrowotna to 4,9% dochodu (nie mniej niż minimum). Zapłacone składki odliczasz od " +
        "dochodu, w 2026 r. łącznie do 14 100 zł.",
      "On the linear tax the health contribution is 4.9% of the income (no less than the minimum). The contributions " +
        "paid come off the income, in 2026 up to 14 100 zł in all.",
      "На линейном взнос на здоровье — 4,9% дохода (не меньше минимума). Уплаченные взносы вычитаются из дохода, " +
        "в 2026 г. всего до 14 100 zł.",
      "На лінійному внесок на здоров'я — 4,9% доходу (не менше мінімуму). Сплачені внески віднімаються від доходу, " +
        "у 2026 р. разом до 14 100 zł.")(
      Cite("health", "art. 79a ust. 1 ustawy o świadczeniach opieki zdrowotnej", "4,9 % podstawy wymiaru składki"),
      Cite("mp", "M.P. 2025 poz. 1274", "w roku 2026 nie może przekroczyć 14 100 zł")),
    r("lump.rates", "Stawki ryczałtu; ryczałt 12% dla IT; ryczałt 17% wolne zawody; limit ryczałtu 2 mln euro",
      "Ryczałt rates; ryczałt 12% for IT; ryczałt 17% liberal professions; the ryczałt limit of 2 million euro",
      "Ставки ryczałt; ryczałt 12% для IT; ryczałt 17% свободные профессии; лимит ryczałt 2 млн евро",
      "Ставки ryczałt; ryczałt 12% для IT; ryczałt 17% вільні професії; ліміт ryczałt 2 млн євро")(
      "Ryczałt liczy się od przychodu, bez kosztów. Stawka zależy od działalności: m.in. 17% wolne zawody, 15%, 12% " +
        "usługi IT (programowanie, doradztwo informatyczne), 8,5% wiele innych usług. Ryczałt wolno płacić, jeśli " +
        "przychód w poprzednim roku nie przekroczył 2 000 000 euro (albo gdy działalność się zaczyna).",
      "Ryczałt is counted from the revenue, with no costs. The rate depends on the activity: among others 17% the " +
        "liberal professions, 15%, 12% IT services (programming, IT consulting), 8.5% many other services. It may be " +
        "paid if the previous year's revenue was at most 2 000 000 euro (or when the business starts).",
      "Ryczałt считается с выручки, без расходов. Ставка зависит от деятельности: в том числе 17% свободные " +
        "профессии, 15%, 12% IT-услуги (программирование, IT-консалтинг), 8,5% многие другие услуги. Платить " +
        "ryczałt можно, если выручка в прошлом году не превысила 2 000 000 евро (или если деятельность начинается).",
      "Ryczałt рахується з виручки, без витрат. Ставка залежить від діяльності: зокрема 17% вільні професії, 15%, " +
        "12% IT-послуги (програмування, IT-консалтинг), 8,5% багато інших послуг. Сплачувати ryczałt можна, якщо " +
        "виручка в минулому році не перевищила 2 000 000 євро (або якщо діяльність починається).")(
      lump("12 ust. 1 pkt 1", "17 % przychodów osiąganych w zakresie wolnych zawodów"),
      lump("12 ust. 1 pkt 2b", "2b) 12 % przychodów ze świadczenia usług"),
      lump("6 ust. 4", "w wysokości nieprzekraczającej 2 000 000 euro")),
    r("lump.health", "Składka zdrowotna na ryczałcie; 498,35 830,58 1495,04 zł; odliczenie połowy składki zdrowotnej",
      "The health contribution on ryczałt; 498.35 830.58 1495.04 zł; deducting half the health contribution",
      "Взнос на здоровье на ryczałt; 498,35 830,58 1495,04 zł; вычет половины взноса на здоровье",
      "Внесок на здоров'я на ryczałt; 498,35 830,58 1495,04 zł; вирахування половини внеску на здоров'я")(
      "Na ryczałcie składka zdrowotna jest stała, zależnie od przychodu w roku: 498,35 zł miesięcznie do 60 000 zł, " +
        "830,58 zł do 300 000 zł, 1495,04 zł powyżej (2026). Połowę zapłaconych składek odliczasz od przychodu.",
      "On ryczałt the health contribution is fixed by the year's revenue: 498.35 zł a month up to 60 000 zł, 830.58 zł " +
        "up to 300 000 zł, 1 495.04 zł above (2026). Half the contributions paid comes off the revenue.",
      "На ryczałt взнос на здоровье фиксированный по годовой выручке: 498,35 zł в месяц до 60 000 zł, 830,58 zł до " +
        "300 000 zł, 1495,04 zł выше (2026). Половина уплаченных взносов вычитается из выручки.",
      "На ryczałt внесок на здоров'я фіксований за річною виручкою: 498,35 zł на місяць до 60 000 zł, 830,58 zł до " +
        "300 000 zł, 1495,04 zł вище (2026). Половина сплачених внесків віднімається від виручки.")(
      Cite("zus", "ZUS, składki 2026", "Składka na ubezpieczenie zdrowotne wynosi 498,35 zł"),
      Cite("zus", "ZUS, składki 2026", "Składka na ubezpieczenie zdrowotne wynosi 830,58 zł"),
      Cite("zus", "ZUS, składki 2026", "Składka na ubezpieczenie zdrowotne wynosi 1 495,04"),
      lump("11 ust. 1a", "mogą być pomniejszone o 50 % składek na ubezpieczenie zdrowotne")),
    r("scale.health", "Składka zdrowotna na skali 9%; minimalna składka zdrowotna; składka zdrowotna na etacie",
      "The health contribution on the scale 9%; the minimum health contribution; the health contribution of an employee",
      "Взнос на здоровье на шкале 9%; минимальный взнос на здоровье; взнос на здоровье по трудовому договору",
      "Внесок на здоров'я за шкалою 9%; мінімальний внесок на здоров'я; внесок на здоров'я за трудовим договором")(
      "Składka zdrowotna to 9% podstawy: na etacie — wynagrodzenia po składkach społecznych, w firmie na skali — " +
        "dochodu. W firmie nie mniej niż 432,54 zł miesięcznie (od lutego 2026 r.).",
      "The health contribution is 9% of its base: for an employee the pay less the social contributions, for a " +
        "business on the scale its income. A business pays at least 432.54 zł a month (from February 2026).",
      "Взнос на здоровье — 9% базы: по трудовому договору — зарплаты после социальных взносов, у фирмы на шкале — " +
        "дохода. Фирма платит не меньше 432,54 zł в месяц (с февраля 2026 г.).",
      "Внесок на здоров'я — 9% бази: за трудовим договором — зарплати після соціальних внесків, у фірми за шкалою — " +
        "доходу. Фірма сплачує не менше 432,54 zł на місяць (з лютого 2026 р.).")(
      Cite("health", "art. 79 ustawy o świadczeniach opieki zdrowotnej",
        "Składka na ubezpieczenie zdrowotne wynosi 9 % podstawy wymiaru składki"),
      Cite("zus", "ZUS, składki 2026", "432,54 zł (za miesiące od lutego 2026 r. do stycznia 2027 r.")),
    r("zus", "Składki ZUS w 2026; pełny ZUS 5652 zł; preferencyjny ZUS 1441,80 zł; limit 30-krotności 282 600 zł",
      "ZUS contributions in 2026; full ZUS 5652 zł; preferential ZUS 1441.80 zł; the yearly cap 282 600 zł",
      "Взносы ZUS в 2026; полный ZUS 5652 zł; льготный ZUS 1441,80 zł; годовой лимит 282 600 zł",
      "Внески ZUS у 2026; повний ZUS 5652 zł; пільговий ZUS 1441,80 zł; річний ліміт 282 600 zł")(
      "W 2026 r. pełne składki firmy liczy się od 5652 zł (z chorobowym i Funduszem Pracy 1926,76 zł miesięcznie), " +
        "preferencyjne od 1441,80 zł (456,18 zł z chorobowym). Emerytalne i rentowe płaci się do podstawy 282 600 zł " +
        "w roku.",
      "In 2026 a business's full contributions are counted from 5 652 zł (with sickness and the Labour Fund " +
        "1 926.76 zł a month), the preferential ones from 1 441.80 zł (456.18 zł with sickness). Pension and " +
        "disability are paid up to a base of 282 600 zł a year.",
      "В 2026 г. полные взносы фирмы считаются от 5652 zł (с больничным и Фондом труда 1926,76 zł в месяц), " +
        "льготные — от 1441,80 zł (456,18 zł с больничным). Пенсионный и рентный платятся до базы 282 600 zł в год.",
      "У 2026 р. повні внески фірми рахуються від 5652 zł (з лікарняним і Фондом праці 1926,76 zł на місяць), " +
        "пільгові — від 1441,80 zł (456,18 zł з лікарняним). Пенсійний і рентний сплачуються до бази 282 600 zł " +
        "на рік.")(
      Cite("zus", "ZUS, składki 2026", "5 652 zł [ 1 ]"), Cite("zus", "ZUS, składki 2026", "1441,80 zł [ 4 ]"),
      Cite("zus", "ZUS, składki 2026", "Roczna podstawa wymiaru na ubezpieczenia emerytalne i rentowe w 2026 roku " +
        "może wynosić maksymalnie 282 600 zł")),
    r("young", "Ulga dla młodych do 26 lat; zerowy PIT dla młodych; zwolnienie do 85 528 zł",
      "The relief for the young under 26; zero PIT for the young; exempt up to 85 528 zł",
      "Льгота для молодых до 26 лет; нулевой PIT для молодёжи; освобождение до 85 528 zł",
      "Пільга для молоді до 26 років; нульовий PIT для молоді; звільнення до 85 528 zł")(
      "Do ukończenia 26 lat przychody z umowy o pracę, zlecenia, praktyk i staży oraz zasiłek macierzyński są " +
        "zwolnione z PIT do 85 528 zł w roku. Nie dotyczy to działalności gospodarczej.",
      "Until 26, revenue from an employment contract, a contract of mandate, apprenticeships and the maternity " +
        "allowance is free of PIT up to 85 528 zł a year. It does not cover a business.",
      "До 26 лет доходы по трудовому договору, договору поручения, практикам и стажировкам, а также декретное " +
        "пособие освобождены от PIT до 85 528 zł в год. На фирму (JDG) это не распространяется.",
      "До 26 років доходи за трудовим договором, договором доручення, практиками й стажуваннями, а також " +
        "декретна допомога звільнені від PIT до 85 528 zł на рік. На фірму (JDG) це не поширюється.")(
      pit("21 ust. 1 pkt 148", "otrzymane przez podatnika do ukończenia 26. roku życia, do wysokości " +
        "nieprzekraczającej w roku podatkowym kwoty 85 528 zł")),
    r("return.relief", "Ulga na powrót; przeprowadzka do Polski; zwolnienie przez cztery lata po powrocie",
      "The relief for coming back; moving to Poland; exempt for four years after moving",
      "Льгота на возвращение (ulga na powrót); переезд в Польшу; освобождение на четыре года после переезда",
      "Пільга на повернення (ulga na powrót); переїзд до Польщі; звільнення на чотири роки після переїзду")(
      "Kto przeniósł miejsce zamieszkania do Polski, ma zwolnione przychody (praca, zlecenie, działalność na skali, " +
        "liniowym albo ryczałcie) do 85 528 zł rocznie przez cztery kolejne lata — od roku przeprowadzki albo " +
        "następnego. Są dodatkowe warunki (m.in. wcześniejsze lata za granicą).",
      "Who moved their residence to Poland has their revenue (employment, mandate, a business on the scale, linear " +
        "or ryczałt) exempt up to 85 528 zł a year for four years in a row — from the year of the move or the next. " +
        "There are further conditions (among them earlier years abroad).",
      "Кто перенёс место жительства в Польшу, имеет освобождённые доходы (работа, поручение, фирма на шкале, " +
        "линейном или ryczałt) до 85 528 zł в год четыре года подряд — с года переезда или следующего. Есть " +
        "дополнительные условия (в том числе предыдущие годы за границей).",
      "Хто переніс місце проживання до Польщі, має звільнені доходи (робота, доручення, фірма за шкалою, лінійним " +
        "або ryczałt) до 85 528 zł на рік чотири роки поспіль — з року переїзду або наступного. Є додаткові умови " +
        "(зокрема попередні роки за кордоном).")(
      pit("21 ust. 1 pkt 152", "przychody podatnika, który przeniósł miejsce zamieszkania na terytorium " +
        "Rzeczypospolitej Polskiej, do wysokości nieprzekraczającej w roku podatkowym kwoty 85 528 zł"),
      pit("21 ust. 1 pkt 152", "w czterech kolejno po sobie następujących latach podatkowych")),
    r("family4", "Ulga dla rodzin 4+; czworo dzieci zwolnienie; zerowy PIT dla dużych rodzin",
      "The relief for families of four children or more; four children exemption; zero PIT for large families",
      "Льгота для семей 4+; четверо детей освобождение; нулевой PIT для многодетных",
      "Пільга для родин 4+; четверо дітей звільнення; нульовий PIT для багатодітних")(
      "Rodzic co najmniej czworga dzieci ma zwolnione przychody z pracy, zlecenia, działalności (skala, liniowy, " +
        "ryczałt) i zasiłku macierzyńskiego do 85 528 zł w roku.",
      "A parent of at least four children has their revenue from employment, mandate, a business (scale, linear, " +
        "ryczałt) and the maternity allowance exempt up to 85 528 zł a year.",
      "Родитель как минимум четырёх детей имеет освобождённые доходы от работы, поручения, фирмы (шкала, линейный, " +
        "ryczałt) и декретного пособия до 85 528 zł в год.",
      "Батько чи мати щонайменше чотирьох дітей має звільнені доходи від роботи, доручення, фірми (шкала, " +
        "лінійний, ryczałt) і декретної допомоги до 85 528 zł на рік.")(
      pit("21 ust. 1 pkt 153", "co najmniej czworga dzieci")),
    r("senior", "Ulga dla pracujących seniorów; praca po 60 lub 65 lat; zwolnienie emeryta, który nie pobiera emerytury",
      "The relief for working seniors; working after 60 or 65; exempt if not drawing the pension",
      "Льгота для работающих пенсионеров; работа после 60 или 65 лет; освобождение, если не получаете пенсию",
      "Пільга для працюючих пенсіонерів; робота після 60 або 65 років; звільнення, якщо не отримуєте пенсію")(
      "Kobieta po 60. i mężczyzna po 65. roku życia, którzy nie pobierają emerytury, mają zwolnione przychody z " +
        "pracy, zlecenia i działalności do 85 528 zł w roku (pod warunkiem podlegania ubezpieczeniom społecznym).",
      "A woman over 60 or a man over 65 not drawing a pension has their revenue from employment, mandate and a " +
        "business exempt up to 85 528 zł a year (if insured under the social insurance).",
      "Женщина после 60 и мужчина после 65 лет, не получающие пенсию, имеют освобождённые доходы от работы, " +
        "поручения и фирмы до 85 528 zł в год (при условии социального страхования).",
      "Жінка після 60 і чоловік після 65 років, які не отримують пенсію, мають звільнені доходи від роботи, " +
        "доручення і фірми до 85 528 zł на рік (за умови соціального страхування).")(
      pit("21 ust. 1 pkt 154", "po ukończeniu 60. roku życia w przypadku kobiety i 65. roku życia w przypadku " +
        "mężczyzny")),
    r("exempt.cap", "Łączny limit zwolnień 85 528 zł; ulga dla młodych i ulga na powrót razem; kilka ulg naraz",
      "The joint cap on the exemptions 85 528 zł; the young and the coming back reliefs together; several reliefs at once",
      "Общий лимит освобождений 85 528 zł; льгота для молодых и на возвращение вместе; несколько льгот сразу",
      "Спільний ліміт звільнень 85 528 zł; пільга для молоді і на повернення разом; кілька пільг одночасно")(
      "Ulga dla młodych, na powrót, dla rodzin 4+ i dla seniorów mają jeden wspólny limit: razem najwyżej 85 528 zł " +
        "zwolnionego przychodu w roku.",
      "The reliefs for the young, for coming back, for families of four and for seniors share one cap: at most " +
        "85 528 zł of exempt revenue a year in all.",
      "Льготы для молодых, на возвращение, для семей 4+ и для пенсионеров имеют один общий лимит: всего не больше " +
        "85 528 zł освобождённого дохода в год.",
      "Пільги для молоді, на повернення, для родин 4+ і для пенсіонерів мають один спільний ліміт: разом не " +
        "більше 85 528 zł звільненого доходу на рік.")(
      pit("21 ust. 44", "Suma przychodów zwolnionych od podatku na podstawie ust. 1 pkt 148 i 152–154 nie może " +
        "w roku podatkowym przekroczyć kwoty 85 528 zł")),
    r("loss", "Strata z lat ubiegłych; odliczenie straty z działalności; strata 50% przez pięć lat",
      "A loss from past years; deducting a business loss; a loss over five years, half a year",
      "Убыток прошлых лет; вычет убытка фирмы; убыток по 50% в течение пяти лет",
      "Збиток минулих років; вирахування збитку фірми; збиток по 50% протягом п'яти років")(
      "Stratę z działalności odliczasz od dochodu z tej samej działalności w kolejnych pięciu latach: najwyżej " +
        "połowę straty w roku, albo raz do 5 000 000 zł (resztę dalej po połowie). Na ryczałcie strat się nie " +
        "odlicza.",
      "A business loss comes off the income of the same business over the next five years: at most half of the loss " +
        "a year, or once up to 5 000 000 zł (the rest then half a year). Ryczałt has no losses to deduct.",
      "Убыток фирмы вычитается из дохода той же фирмы в следующие пять лет: не больше половины убытка в год, или " +
        "один раз до 5 000 000 zł (остаток дальше по половине). На ryczałt убытки не вычитаются.",
      "Збиток фірми віднімається від доходу тієї ж фірми в наступні п'ять років: не більше половини збитку на рік, " +
        "або один раз до 5 000 000 zł (решта далі по половині). На ryczałt збитки не віднімаються.")(
      pit("9 ust. 3", "w najbliższych kolejno po sobie następujących pięciu latach podatkowych"),
      pit("9 ust. 3", "nie może przekroczyć 50 % wysokości tej straty"),
      pit("9 ust. 3", "o kwotę nieprzekraczającą 5 000 000 zł")),
    r("internet","Ulga internetowa; odliczenie za internet 760 zł",
      "The internet relief; deducting internet costs 760 zł",
      "Льгота на интернет; вычет за интернет 760 zł",
      "Пільга на інтернет; вирахування за інтернет 760 zł")(
      "Wydatki na internet odliczasz od dochodu, najwyżej 760 zł w roku.",
      "Internet costs come off the income, at most 760 zł a year.",
      "Расходы на интернет вычитаются из дохода, не больше 760 zł в год.",
      "Витрати на інтернет віднімаються від доходу, не більше 760 zł на рік.")(
      pit("26 ust. 1 pkt 6a", "wydatków ponoszonych przez podatnika z tytułu użytkowania sieci Internet, w wysokości " +
        "nieprzekraczającej w roku podatkowym kwoty 760 zł")),
    r("donations", "Odliczenie darowizn; darowizna na cele pożytku publicznego; limit 6% dochodu",
      "Deducting donations; a donation for public benefit; the limit of 6% of income",
      "Вычет пожертвований; пожертвование на общественно полезные цели; лимит 6% дохода",
      "Вирахування пожертв; пожертва на суспільно корисні цілі; ліміт 6% доходу")(
      "Darowizny na cele pożytku publicznego i religijne odliczasz od dochodu, najwyżej 6% dochodu.",
      "Donations for public benefit and religious purposes come off the income, at most 6% of it.",
      "Пожертвования на общественно полезные и религиозные цели вычитаются из дохода, не больше 6% дохода.",
      "Пожертви на суспільно корисні й релігійні цілі віднімаються від доходу, не більше 6% доходу.")(
      pit("26 ust. 1 pkt 9", "niewięcej jednak niż kwoty stanowiącej 6 % dochodu")),
    r("thermo", "Ulga termomodernizacyjna; ocieplenie domu odliczenie; 53 000 zł",
      "The thermal modernisation relief; insulating a house deduction; 53 000 zł",
      "Льгота на термомодернизацию; утепление дома вычет; 53 000 zł",
      "Пільга на термомодернізацію; утеплення будинку вирахування; 53 000 zł")(
      "Właściciel domu jednorodzinnego odlicza wydatki na termomodernizację, łącznie najwyżej 53 000 zł.",
      "The owner of a single-family house deducts the costs of its thermal modernisation, at most 53 000 zł in all.",
      "Владелец частного дома вычитает расходы на термомодернизацию, всего не больше 53 000 zł.",
      "Власник приватного будинку віднімає витрати на термомодернізацію, разом не більше 53 000 zł.")(
      pit("26h ust. 2", "Kwota odliczenia nie może przekroczyć 53 000 zł")),
    r("deadline", "Termin złożenia zeznania rocznego; do kiedy PIT; 30 kwietnia",
      "The deadline for the annual return; when is PIT due; 30 April",
      "Срок подачи годовой декларации; до какого числа PIT; 30 апреля",
      "Строк подання річної декларації; до якого числа PIT; 30 квітня")(
      "Zeznanie za rok składa się od 15 lutego do 30 kwietnia następnego roku; złożone wcześniej uznaje się za " +
        "złożone 15 lutego.",
      "The return for a year is filed from 15 February to 30 April of the next year; one filed earlier counts as " +
        "filed on 15 February.",
      "Декларацию за год подают с 15 февраля по 30 апреля следующего года; поданная раньше считается поданной " +
        "15 февраля.",
      "Декларацію за рік подають з 15 лютого до 30 квітня наступного року; подана раніше вважається поданою " +
        "15 лютого.")(
      pit("45 ust. 1", "w terminie od dnia 15 lutego do dnia 30 kwietnia roku następującego po roku podatkowym")),
    r("rounding", "Zaokrąglanie podatku do pełnych złotych; zaokrąglenie podstawy opodatkowania",
      "Rounding the tax to full złoty; rounding the tax base",
      "Округление налога до целых злотых; округление налоговой базы",
      "Округлення податку до цілих злотих; округлення податкової бази")(
      "Podstawę i podatek zaokrągla się do pełnych złotych: końcówki poniżej 50 groszy pomija się, od 50 groszy " +
        "zaokrągla w górę.",
      "The base and the tax are rounded to full złoty: under 50 grosze dropped, from 50 grosze up.",
      "Базу и налог округляют до целых злотых: меньше 50 грошей отбрасывают, от 50 грошей — вверх.",
      "Базу й податок округлюють до цілих злотих: менше 50 грошів відкидають, від 50 грошів — угору.")(
      Cite("op", "art. 63 § 1 Ordynacji podatkowej", "zaokrągla się do pełnych złotych w ten sposób, że")))

  // THE FINDING: words to stems (diacritics off, the first five letters), each stem weighed by how few rules have it
  private val folded = "ąćęłńóśźż".zip("acelnoszz").toMap
  /** the words of asking itself — "how much", "can I", "ile", "сколько" — naming no rule */
  private val asking = Set("how", "much", "what", "where", "which", "can", "the", "and", "for", "with", "does",
    "ile", "jak", "czy", "jest", "moge", "mozna", "dla", "jesli", "mam", "wynosi", "сколько", "как", "можно",
    "для", "если", "скільки", "як", "можна", "якщо")
  def stems(text: String): Vector[String] =
    text.toLowerCase.map(c => folded.getOrElse(c, c)).map(c => if c.isLetterOrDigit then c else ' ').split(' ')
      .toVector
      .filter(w => (w.length >= 3 || w.forall(_.isDigit) && w.nonEmpty) && !asking(w)).map(_.take(5))
  private val bags: Vector[(Rule, Set[String], Set[String])] =
    all.map(r => (r, r.asks.values.flatMap(stems).toSet, r.says.values.flatMap(stems).toSet))
  private val idf: Map[String, Double] =
    bags.flatMap((_, a, s) => a ++ s).groupBy(identity).view.mapValues(n => math.log(1.0 + all.size.toDouble / n.size))
      .toMap

  /** the rules a question names, best first: a stem in a rule's asks weighs twice one in what it says */
  def find(question: String, n: Int = 3): Vector[Rule] =
    val q = stems(question).toSet
    bags.map((r, a, s) => r -> q.toVector.map(w => idf.getOrElse(w, 0.0) * (if a(w) then 2 else if s(w) then 1
      else 0)).sum).filter(_._2 > 0).sortBy(-_._2).take(n).map(_._1)
