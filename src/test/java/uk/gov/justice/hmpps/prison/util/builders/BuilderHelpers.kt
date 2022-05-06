package uk.gov.justice.hmpps.prison.util

internal fun randomName(): String {
  // return random name between 3 and 10 characters long
  return (1..(3 + (Math.random() * 7).toInt())).map {
    ('a' + (Math.random() * 26).toInt())
  }.joinToString("")
}
