package uk.gov.justice.hmpps.prison.util

class NaturalOrderComparator : Comparator<SortAttribute> {
  private fun isDigit(ch: Char): Boolean {
    return ((ch.code >= 48) && (ch.code <= 57))
  }

  /**
   * Length of string is passed in for improved efficiency (only need to calculate it once)
   */
  private fun getChunk(s: String, strLen: Int, markerValue: Int): String {
    var marker = markerValue
    val chunk = StringBuilder()
    var c = s[marker]
    chunk.append(c)
    marker++
    if (isDigit(c)) {
      while (marker < strLen) {
        c = s[marker]
        if (!isDigit(c)) break
        chunk.append(c)
        marker++
      }
    } else {
      while (marker < strLen) {
        c = s[marker]
        if (isDigit(c)) break
        chunk.append(c)
        marker++
      }
    }
    return chunk.toString()
  }

  override fun compare(key1: SortAttribute?, key2: SortAttribute?): Int {
    if ((key1 == null) || (key2 == null)) {
      return 0
    }
    val s1 = key1.key
    val s2 = key2.key

    var thisMarker = 0
    var thatMarker = 0
    val s1Length = s1.length
    val s2Length = s2.length

    while (thisMarker < s1Length && thatMarker < s2Length) {
      val thisChunk = getChunk(s1, s1Length, thisMarker)
      thisMarker += thisChunk.length

      val thatChunk = getChunk(s2, s2Length, thatMarker)
      thatMarker += thatChunk.length

      // If both chunks contain numeric characters, sort them numerically
      var result: Int
      if (isDigit(thisChunk[0]) && isDigit(thatChunk[0])) {
        // Simple chunk comparison by length.
        val thisChunkLength = thisChunk.length
        result = thisChunkLength - thatChunk.length
        // If equal, the first different number counts
        if (result == 0) {
          for (i in 0 until thisChunkLength) {
            result = thisChunk[i].code - thatChunk[i].code
            if (result != 0) {
              return result
            }
          }
        }
      } else {
        result = thisChunk.compareTo(thatChunk)
      }

      if (result != 0) return result
    }

    return s1Length - s2Length
  }
}

interface SortAttribute {
  val key: String
}
