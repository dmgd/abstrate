package uk.co.abstrate.json

import com.fasterxml.jackson.annotation.JsonTypeInfo
import com.fasterxml.jackson.databind.JavaType
import com.fasterxml.jackson.databind.cfg.MapperConfig
import com.fasterxml.jackson.databind.introspect.AnnotatedClass
import com.fasterxml.jackson.databind.introspect.NopAnnotationIntrospector
import com.fasterxml.jackson.databind.jsontype.TypeResolverBuilder
import com.fasterxml.jackson.databind.jsontype.impl.StdTypeResolverBuilder
import com.fasterxml.jackson.databind.module.SimpleModule
import kotlin.reflect.full.allSuperclasses

class SealedTypes private constructor(private val sealedClassTypeResolvers: SealedClassTypeResolvers) : SimpleModule() {

    constructor(typeInfo: JsonTypeInfo) : this(SealedClassTypeResolvers(typeInfo))

    override fun setupModule(context: SetupContext) {
        context.appendAnnotationIntrospector(sealedClassTypeResolvers)
    }
}

internal class SealedClassTypeResolvers(
    private val typeInfo: JsonTypeInfo,
) : NopAnnotationIntrospector() {

    override fun findTypeResolver(config: MapperConfig<*>, ac: AnnotatedClass, baseType: JavaType): TypeResolverBuilder<*>? {
        val kotlinClass = baseType.rawClass.kotlin
        val sealed = kotlinClass.takeIf { it.isSealed } ?: kotlinClass.allSuperclasses.firstOrNull { it.isSealed }
        return if (sealed == null) {
            null
        } else {
            StdTypeResolverBuilder().init(JsonTypeInfo.Value.from(typeInfo), null)
        }
    }
}
