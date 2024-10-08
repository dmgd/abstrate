@file:Suppress("ConvertObjectToDataObject")

package dev.abstrate.jackson

import org.junit.jupiter.api.Assertions.assertArrayEquals
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertInstanceOf
import org.junit.jupiter.api.Assertions.fail
import org.junit.jupiter.api.Test
import java.math.BigDecimal
import java.math.BigInteger
import java.time.Duration as JavaDuration
import java.time.Instant
import java.util.UUID
import kotlin.time.Duration.Companion.seconds
import kotlin.time.Duration as KotlinDuration

val aStandardObjectMapper = standardObjectMapper()

class StandardObjectMapperTests {

    @Test
    fun values() {
        roundTrip(true, "true")
        roundTrip(ByteArray(3) { it.toByte() })
        roundTrip(56.toByte(), "56")
        roundTrip(4567.toShort(), "4567")
        roundTrip(3456789, "3456789")
        roundTrip(ExampleLong(1234567890L), "1234567890")
        roundTrip(BigInteger("12345678901234567890"), "12345678901234567890")
        roundTrip(12.34f, "12.34")
        roundTrip(1234.5678, "1234.5678")
        roundTrip(BigDecimal("12345678901234567890.1234567890123456789"), "12345678901234567890.1234567890123456789")
        roundTrip("testing", "\"testing\"")
        roundTrip(UUID(1, 7), "\"00000000-0000-0001-0000-000000000007\"")
        roundTrip(listOf(1, 2, 3), "[1,2,3]")
        roundTrip(setOf(1, 2, 3), "[1,2,3]")
        roundTrip(mapOf("a" to 1, "b" to true, "c" to listOf(1, 2, 3)), "{\"a\":1,\"b\":true,\"c\":[1,2,3]}")
        roundTrip(Instant.ofEpochMilli(7), "\"1970-01-01T00:00:00.007Z\"")
        roundTrip(JavaDuration.ofSeconds(7), "\"PT7S\"")
        roundTrip(7.seconds, "\"PT7S\"")
    }

    @Test
    fun `value types`() {
        roundTrip(ExampleBoolean(true), "true")
        roundTrip(ExampleByteArray(ByteArray(3) { it.toByte() }))
        roundTrip(ExampleByte(56), "56")
        roundTrip(ExampleShort(4567), "4567")
        roundTrip(ExampleInt(3456789), "3456789")
        roundTrip(ExampleLong(1234567890), "1234567890")
        roundTrip(ExampleBigInteger(BigInteger("12345678901234567890")), "12345678901234567890")
        roundTrip(ExampleFloat(12.34f), "12.34")
        roundTrip(ExampleDouble(1234.5678), "1234.5678")
        roundTrip(ExampleBigDecimal(BigDecimal("12345678901234567890.1234567890123456789")), "12345678901234567890.1234567890123456789")
        roundTrip(ExampleString("testing"), "\"testing\"")
        roundTrip(ExampleUuid(UUID(1, 7)), "\"00000000-0000-0001-0000-000000000007\"")
        roundTrip(ExampleList(listOf(1, 2, 3)), "[1,2,3]")
        roundTrip(ExampleSet(setOf(1, 2, 3)), "[1,2,3]")
        // doesn't work currently, in jackson 2.17.2: roundTrip(ExampleMap(mapOf("a" to 1, "b" to true, "c" to listOf(1, 2, 3))), "{\"a\":1,\"b\":true,\"c\":[1,2,3]}")
        roundTrip(ExampleInstant(Instant.ofEpochMilli(7)), "\"1970-01-01T00:00:00.007Z\"")
        roundTrip(ExampleJavaDuration(JavaDuration.ofSeconds(7)), "\"PT7S\"")
        // doesn't work currently, in jackson 2.17.2: roundTrip(ExampleKotlinDuration(2.seconds), "\"PT7S\"")
    }

    @Test
    fun `sealed interface`() {
        roundTrip<ExampleSealedInterface>(ExampleSealedInterface.DataObject)
        roundTrip<ExampleSealedInterface>(ExampleSealedInterface.Object)
        roundTrip<ExampleSealedInterface>(ExampleSealedInterface.DataClass(1))
        roundTrip<ExampleSealedInterface>(ExampleSealedInterface.Class(7))
    }

    @Test
    fun `sealed class`() {
        roundTrip<ExampleSealedClass>(ExampleSealedClass.DataObject)
        roundTrip<ExampleSealedClass>(ExampleSealedClass.Object)
        roundTrip<ExampleSealedClass>(ExampleSealedClass.DataClass(1))
        roundTrip<ExampleSealedClass>(ExampleSealedClass.Class(7))
    }

    @Test
    fun `sealed class with value`() {
        roundTrip<ExampleSealedClassWithValue>(ExampleSealedClassWithValue.DataObject)
        roundTrip<ExampleSealedClassWithValue>(ExampleSealedClassWithValue.Object)
        roundTrip<ExampleSealedClassWithValue>(ExampleSealedClassWithValue.DataClass(1, 4))
        roundTrip<ExampleSealedClassWithValue>(ExampleSealedClassWithValue.Class(7, 10))
    }

    private inline fun <reified T : Any> roundTrip(value: T, expectedFormat: String? = null) {
        val json =
            try {
                aStandardObjectMapper.writeValueAsString(value)
            } catch (e: Exception) {
                fail("Failed to serialise $value", e)
            }
        if (expectedFormat != null) {
            assertEquals(expectedFormat, json)
        }
        val result =
            try {
                aStandardObjectMapper.readValue(json, T::class.java)
            } catch (e: Exception) {
                fail("Failed to deserialise '$json' (from $value)", e)
            }
        when (value) {
            is ExampleByteArray -> {
                assertInstanceOf(ExampleByteArray::class.java, result)
                assertArrayEquals(value.value, (result as ExampleByteArray).value)
            }

            is ByteArray ->
                assertArrayEquals(value, result as? ByteArray)

            else ->
                assertEquals(value, result)
        }
    }

    @JvmInline
    value class ExampleBoolean(val value: Boolean)

    @JvmInline
    value class ExampleByteArray(val value: ByteArray)

    @JvmInline
    value class ExampleByte(val value: Byte)

    @JvmInline
    value class ExampleShort(val value: Short)

    @JvmInline
    value class ExampleInt(val value: Int)

    @JvmInline
    value class ExampleLong(val value: Long)

    @JvmInline
    value class ExampleBigInteger(val value: BigInteger)

    @JvmInline
    value class ExampleFloat(val value: Float)

    @JvmInline
    value class ExampleDouble(val value: Double)

    @JvmInline
    value class ExampleBigDecimal(val value: BigDecimal)

    @JvmInline
    value class ExampleString(val value: String)

    @JvmInline
    value class ExampleUuid(val value: UUID)

    @JvmInline
    value class ExampleList<T>(val value: List<T>)

    @JvmInline
    value class ExampleSet<T>(val value: Set<T>)

    @JvmInline
    value class ExampleMap<K, V>(val value: Map<K, V>)

    @JvmInline
    value class ExampleInstant(val value: Instant)

    @JvmInline
    value class ExampleJavaDuration(val value: JavaDuration)

    @JvmInline
    value class ExampleKotlinDuration(val value: KotlinDuration)

    sealed interface ExampleSealedInterface {
        data object DataObject : ExampleSealedInterface
        object Object : ExampleSealedInterface {

            override fun equals(other: Any?) = other is Object
        }

        data class DataClass(val value: Int) : ExampleSealedInterface
        class Class(val value: Int) : ExampleSealedInterface {

            override fun equals(other: Any?) = other is Class && value == other.value
            override fun hashCode() = value
        }
    }

    sealed class ExampleSealedClass {
        data object DataObject : ExampleSealedClass()
        object Object : ExampleSealedClass() {

            override fun equals(other: Any?) = other is Object
        }

        data class DataClass(val value: Int) : ExampleSealedClass()
        class Class(val value: Int) : ExampleSealedClass() {

            override fun equals(other: Any?) = other is Class && value == other.value
            override fun hashCode() = value
        }
    }

    sealed class ExampleSealedClassWithValue(open val value: Int) {
        data object DataObject : ExampleSealedClassWithValue(1)
        object Object : ExampleSealedClassWithValue(7) {

            override fun equals(other: Any?) = other is Object
        }

        data class DataClass(val x: Int, override val value: Int) : ExampleSealedClassWithValue(value)
        class Class(val x: Int, value: Int) : ExampleSealedClassWithValue(value) {

            override fun equals(other: Any?) = other is Class && value == other.value
            override fun hashCode() = value
        }
    }

    @Suppress("unused")
    sealed interface ExampleOpenClassSealedInterface {

        open class OpenClass : ExampleOpenClassSealedInterface
    }
}
