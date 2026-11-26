package okay.tax

/** the theory's and the rules' quotes against the statutes kept in sources/2026, verbatim (the JVM reads the files) */
class QuotesSuite extends munit.FunSuite:
  private val dir = java.nio.file.Path.of("sources/2026")
  private val files = Map("pit" -> "pit-2026.txt", "lump" -> "ryczalt-2025.txt", "sus" -> "sus-2026.txt",
    "health" -> "health.txt", "zus" -> "zus-2026.txt", "mp" -> "mp-2025-1274.txt", "op" -> "op-2026.txt")
  private def norm(s: String) = s.replace("​", "").replace("­", "").replaceAll("\\s+", " ")
  /** each source as printed (whitespace one space) and with the words a line's end split joined again */
  private lazy val texts: Map[String, (String, String)] = files.view.mapValues { f =>
    val raw = java.nio.file.Files.readString(dir.resolve(f))
    (norm(raw), norm(raw.replaceAll("(\\p{L})-[ \\t]*\\r?\\n[ \\t]*(\\p{L})", "$1$2")))
  }.toMap
  private def found(source: String, quote: String): Boolean =
    val (printed, joined) = texts(source)
    printed.contains(norm(quote)) || joined.contains(norm(quote))

  test("every value's quote is in its source, verbatim (the sources in sources/2026)"):
    assert(files.values.forall(f => java.nio.file.Files.exists(dir.resolve(f))), "the sources are missing")
    for (k, st) <- PayTheory.y2026 do assert(found(st.source, st.quote), s"$k: «${st.quote}» not in ${st.source}")

  test("every rule's quote is in its source, verbatim"):
    for r <- Rules.all; c <- r.cites do assert(found(c.source, c.quote), s"${r.id}: «${c.quote}» not in ${c.source}")
