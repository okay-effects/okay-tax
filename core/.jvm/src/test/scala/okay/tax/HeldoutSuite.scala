package okay.tax

import scala.jdk.CollectionConverters.*

/** THE HELD-OUT MEASURE (specs/tax.md §6): the word search on questions written before it was measured, never tuned
 * on; first right and right among three, by language. A floor, not a target: the numbers are in the spec */
class HeldoutSuite extends munit.FunSuite:
  test("the word search on the held-out questions"):
    val rows = java.nio.file.Files.readAllLines(java.nio.file.Path.of("data/heldout.tsv")).asScala.toVector
      .filterNot(l => l.startsWith("#") || l.isBlank).map(_.split('\t')).map(a => (a(0), a(1), a(2)))
    val ids = Rules.all.map(_.id).toSet
    assert(rows.forall(r => ids(r._1)), rows.filterNot(r => ids(r._1)).mkString)
    val got = rows.map((id, lang, q) => (id, lang, q, Rules.find(q).map(_.id)))
    for lang <- Vector("pl", "en", "ru", "uk") do
      val mine = got.filter(_._2 == lang)
      println(f"  $lang: first ${mine.count(g => g._4.headOption.contains(g._1))}%3d, among three " +
        f"${mine.count(g => g._4.contains(g._1))}%3d, none ${mine.count(_._4.isEmpty)}%3d of ${mine.size}")
    val first = got.count(g => g._4.headOption.contains(g._1)); val three = got.count(g => g._4.contains(g._1))
    println(s"  all: first $first, among three $three of ${got.size}")
    got.filterNot(g => g._4.headOption.contains(g._1)).foreach(g => println(s"    [${g._2}] ${g._3} → ${g._4} (${g._1})"))
    assert(first >= 134 && three >= 171, s"below the measure of 2026-11-26: first $first, among three $three")
