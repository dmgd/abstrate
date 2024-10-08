package dev.abstrate.jackson

import com.fasterxml.jackson.annotation.JsonInclude.Include.NON_NULL
import com.fasterxml.jackson.annotation.JsonTypeInfo
import com.fasterxml.jackson.core.JsonParser.Feature.USE_FAST_BIG_NUMBER_PARSER
import com.fasterxml.jackson.core.JsonParser.Feature.USE_FAST_DOUBLE_PARSER
import com.fasterxml.jackson.databind.DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.SerializationFeature.WRITE_DATES_AS_TIMESTAMPS
import com.fasterxml.jackson.databind.SerializationFeature.WRITE_DURATIONS_AS_TIMESTAMPS
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule
import com.fasterxml.jackson.module.kotlin.KotlinFeature.UseJavaDurationConversion
import com.fasterxml.jackson.module.kotlin.KotlinModule

fun standardObjectMapper(
    configureKotlin: KotlinModule.Builder.() -> Unit = {},
    configureJavaTime: JavaTimeModule.() -> Unit = {},
    sealedTypeInfo: JsonTypeInfo = JsonTypeInfo(use = JsonTypeInfo.Id.SIMPLE_NAME, property = "type"),
) =
    ObjectMapper()
        .apply {
            registerModule(
                KotlinModule.Builder()
                    .enable(UseJavaDurationConversion)
                    .apply(configureKotlin)
                    .build()
            )
            registerModule(JavaTimeModule().apply(configureJavaTime))
            registerModule(SealedTypes(sealedTypeInfo))
            setSerializationInclusion(NON_NULL)
            configure(FAIL_ON_UNKNOWN_PROPERTIES, false)
            configure(USE_FAST_BIG_NUMBER_PARSER, true)
            configure(USE_FAST_DOUBLE_PARSER, true)
            configure(WRITE_DATES_AS_TIMESTAMPS, false)
            configure(WRITE_DURATIONS_AS_TIMESTAMPS, false)
        }
