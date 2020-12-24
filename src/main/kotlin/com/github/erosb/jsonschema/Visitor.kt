package com.github.erosb.jsonschema

import java.lang.RuntimeException

interface Visitor<P> {
    fun visitCompositeSchema(schema: CompositeSchema): P? = visitChildren(schema)
    fun visitTrueSchema(schema: TrueSchema): P? = visitChildren(schema)
    fun visitFalseSchema(schema: FalseSchema): P? = visitChildren(schema)
    fun visitMinLengthSchema(schema: MinLengthSchema): P? = visitChildren(schema)
    fun visitMaxLengthSchema(schema: MaxLengthSchema): P? = visitChildren(schema)
    fun visitAllOfSchema(schema: AllOfSchema): P? = visitChildren(schema)
    fun visitReferenceSchema(schema: ReferenceSchema): P? = visitChildren(schema)
    fun visitAdditionalPropertiesSchema(schema: AdditionalPropertiesSchema): P? = visitChildren(schema)
    fun identity(): P? = null
    fun accumulate(previous: P?, current: P?): P? = current ?: previous
    fun visitChildren(parent: Schema): P? {
        var product: P? = identity()
        for (subschema in parent.subschemas()) {
            val current = subschema.accept(this)
            product = accumulate(product, current);
        }
        return product
    }
}

internal class SchemaNotFoundException(expectedKey: String, actualKey: String) : RuntimeException("expected key: $expectedKey, but found: $actualKey")

internal class TraversingVisitor<P : Schema>(vararg keys: String) : Visitor<P> {

    private val remainingKeys = keys.asList().toMutableList()

    private fun consume(schema: Schema, key: String, cb: () -> P?): P? {
        if (remainingKeys[0] == key) {
            remainingKeys.removeAt(0);
            if (remainingKeys.isEmpty()) {
                return schema as P
            }
            return cb()
        }
        throw SchemaNotFoundException("additionalProperties", remainingKeys[0]);
    }

    override fun visitAdditionalPropertiesSchema(schema: AdditionalPropertiesSchema): P? = consume(schema, "additionalProperties")
    { super.visitAdditionalPropertiesSchema(schema) }

    override fun visitReferenceSchema(schema: ReferenceSchema): P? = consume(schema, "\$ref")
    { super.visitReferenceSchema(schema) }
}
