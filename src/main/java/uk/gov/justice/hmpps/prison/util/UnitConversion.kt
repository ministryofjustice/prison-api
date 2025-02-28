package uk.gov.justice.hmpps.prison.util

import kotlin.math.roundToInt

// Length
fun centimetresToFeetAndInches(cm: Int): Pair<Int, Int> {
  val inchesTotal = (cm / 2.54).roundToInt()
  val feet = inchesTotal / 12
  val inches = inchesTotal % 12

  return Pair(feet, inches)
}

fun feetAndInchesToCentimetres(feet: Int, inches: Int): Int {
  val totalInches = (feet * 12) + inches
  return (totalInches * 2.54).roundToInt()
}

// Weight
fun kilogramsToPounds(kg: Int): Int = (kg * 2.20462).roundToInt()

fun kilogramsToStoneAndPounds(kg: Int): Pair<Int, Int> {
  val totalPounds = (kg * 2.20462).roundToInt()
  val stone = totalPounds / 14
  val pounds = totalPounds % 14

  return Pair(stone, pounds)
}

fun stoneAndPoundsToKilograms(stone: Int, pounds: Int): Int {
  val totalPounds = (stone * 14) + pounds
  return (totalPounds / 2.20462).roundToInt()
}
