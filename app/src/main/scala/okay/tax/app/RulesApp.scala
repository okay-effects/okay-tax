package okay.tax.app

import okay.business.model.{Button, Column, Field, Form, Part, Screen, Table, Ui}
import okay.business.scala3.*
import okay.tax.{Rule, Rules}

/** the words of the questions about the rules */
final case class RulesWords(open: String, title: String, lead: String, question: String, ask: String, all: String,
    rule: String, show: String, none: String, again: String, calculator: String, found: String)

object RulesWords:
  val pl = RulesWords("Pytania o przepisy", "Pytania o przepisy: co mówi ustawa",
    "Zapytaj własnymi słowami albo wybierz przepis z listy. Odpowiedź to przepis opisany prosto i jego " +
      "dosłowne brzmienie z ustawy, z numerem artykułu.",
    "Twoje pytanie", "Zapytaj", "Wszystkie przepisy", "Przepis", "Pokaż", "Nie znalazłem przepisu do tego pytania. " +
      "Spróbuj innych słów albo wybierz z listy.", "Inne pytanie", "Kalkulator", "Przepisy do tego pytania")
  val en = RulesWords("Questions about the rules", "Questions about the rules: what the statute says",
    "Ask in your own words or choose a rule from the list. The answer is the rule said plainly and its exact words " +
      "in the statute, with the article.",
    "Your question", "Ask", "All the rules", "Rule", "Show", "No rule found for this question. Try other words or " +
      "choose from the list.", "Another question", "Calculator", "The rules for this question")
  val ru = RulesWords("Вопросы о правилах", "Вопросы о правилах: что говорит закон",
    "Спросите своими словами или выберите правило из списка. Ответ — правило простыми словами и его дословный " +
      "текст из закона (по-польски), с номером статьи.",
    "Ваш вопрос", "Спросить", "Все правила", "Правило", "Показать", "Не нашёл правила для этого вопроса. " +
      "Попробуйте другие слова или выберите из списка.", "Другой вопрос", "Калькулятор", "Правила по этому вопросу")
  val uk = RulesWords("Питання про правила", "Питання про правила: що каже закон",
    "Запитайте своїми словами або виберіть правило зі списку. Відповідь — правило простими словами і його " +
      "дослівний текст із закону (польською), з номером статті.",
    "Ваше питання", "Запитати", "Усі правила", "Правило", "Показати", "Не знайшов правила для цього питання. " +
      "Спробуйте інші слова або виберіть зі списку.", "Інше питання", "Калькулятор", "Правила з цього питання")
  def of(lang: String): RulesWords = lang match
    case "en" => en
    case "ru" => ru
    case "uk" => uk
    case _ => pl

/**
 * THE QUESTIONS ABOUT THE RULES (specs/tax.md §5): a question in one's own words, or a rule chosen from the list;
 * each rule found said plainly in the page's language and quoted from the statute, word for word, with its article
 */
object RulesApp:
  /** a rule shown: what it says, and where it stands */
  def shown(lang: String, r: Rule): Part =
    Part.section(r.title(lang))(Part.text(r.said(lang)), Part.details(r.cites.map(c => c.where -> s"«${c.quote}»")*))

  def main[R[_]](w: Words, back: => Unit ! R)(using In[Ui, R]): Unit ! R =
    val rw = RulesWords.of(w.lang)
    val f = Form(Tuple1(Field.text(rw.question)))
    val list = Table(Rules.all.toList)(Column.text(rw.rule)(_.title(w.lang))).keyedBy(_.id)
    show(Screen[Option[Either[String, Rule]]](rw.title)(Part.text(rw.lead), f, Part.section(rw.all)(list))(
      Button.submit(rw.ask, f)(q => Some(Left(q._1))), Button.select(rw.show, list)(r => Some(Right(r))),
      Button(rw.calculator)(None))).flatMap {
      case None => back
      case Some(Left(q)) => answer[R](w, rw, Rules.find(q), back)
      case Some(Right(r)) => answer[R](w, rw, Vector(r), back)
    }

  private def answer[R[_]](w: Words, rw: RulesWords, found: Vector[Rule], back: => Unit ! R)(
      using In[Ui, R]): Unit ! R =
    val parts = if found.isEmpty then Vector(Part.warning(rw.none)) else found.map(shown(w.lang, _))
    show(Screen[Boolean](if found.isEmpty then rw.title else rw.found)((parts :+ Part.info(w.notAdvice))*)(
      Button(rw.again)(true), Button(rw.calculator)(false))).flatMap(again => if again then main[R](w, back) else back)
