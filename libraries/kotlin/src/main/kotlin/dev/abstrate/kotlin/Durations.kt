package dev.abstrate.kotlin

import java.time.Duration

operator fun Duration.times(multiplicand: Int): Duration =
    multipliedBy(multiplicand.toLong())

operator fun Duration.times(multiplicand: Long): Duration =
    multipliedBy(multiplicand)
