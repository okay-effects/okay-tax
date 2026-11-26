package okay.tax

/** the theory's quotes against the statutes kept in sources/2026, verbatim (the JVM reads the files) */
class QuotesSuite extends munit.FunSuite:
  private val t = PayTheory.y2026
  test("every quote is in its source, verbatim (the sources in sources/2026)"):
    val dir = java.nio.file.Path.of("sources/2026")
    val files = Map("pit" -> "pit-2026.txt", "lump" -> "ryczalt-2025.txt", "sus" -> "sus-2026.txt",
      "health" -> "health.txt", "zus" -> "zus-2026.txt", "mp" -> "mp-2025-1274.txt",
      "op" -> "op-2026.txt")
    assert(files.values.forall(f => java.nio.file.Files.exists(dir.resolve(f))), "the sources are missing")
    val texts = files.view.mapValues(f => java.nio.file.Files.readString(dir.resolve(f))
      .replace("\u200b", "").replace("\u00ad", "").replaceAll("\\s+", " ")).toMap
    for (k, st) <- t do
      assert(texts(st.source).contains(st.quote.replaceAll("\\s+", " ")), s"$k: «${st.quote}» not in ${st.source}")
