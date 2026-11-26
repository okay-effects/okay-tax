package okay.tax.app

/** the words of the year's deductions (specs/tax.md §7), in Polish, English, Russian and Ukrainian */
final case class MoreWords(childMonths: String, lossAmount: String, lossBefore: String, internet: String,
    thermo: String, donations: String, lines: Map[String, String])

object MoreWords:
  def of(lang: String): MoreWords = lang match
    case "en" => MoreWords("A child for part of the year: its months", "Business: a past year's loss",
      "Business: of it deducted before", "The internet paid in the year", "Thermal modernisation paid",
      "Donations made", Map("loss" -> "Past loss deducted", "internet" -> "Internet deducted",
        "thermo" -> "Thermal modernisation deducted", "donations" -> "Donations deducted"))
    case "ru" => MoreWords("Ребёнок часть года: месяцев", "Фирма: убыток прошлого года",
      "Фирма: из него уже вычтено", "Интернет, оплачено за год", "Термомодернизация, оплачено",
      "Пожертвования", Map("loss" -> "Вычтен убыток прошлых лет", "internet" -> "Вычтен интернет",
        "thermo" -> "Вычтена термомодернизация", "donations" -> "Вычтены пожертвования"))
    case "uk" => MoreWords("Дитина частину року: місяців", "Фірма: збиток минулого року",
      "Фірма: з нього вже вирахувано", "Інтернет, сплачено за рік", "Термомодернізація, сплачено",
      "Пожертви", Map("loss" -> "Вирахувано збиток минулих років", "internet" -> "Вирахувано інтернет",
        "thermo" -> "Вирахувано термомодернізацію", "donations" -> "Вирахувано пожертви"))
    case _ => MoreWords("Dziecko przez część roku: miesiące", "Działalność: strata z lat ubiegłych",
      "Działalność: z niej już odliczono", "Internet zapłacony w roku", "Termomodernizacja zapłacona",
      "Darowizny", Map("loss" -> "Odliczona strata z lat ubiegłych", "internet" -> "Odliczony internet",
        "thermo" -> "Odliczona termomodernizacja", "donations" -> "Odliczone darowizny"))
