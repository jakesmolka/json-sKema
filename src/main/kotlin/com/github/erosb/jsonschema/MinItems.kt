package com.github.erosb.jsonschema

data class MinItemsSchema(val minItems: Number, override val location: SourceLocation) : Schema(location) {
    override fun <P> accept(visitor: SchemaVisitor<P>): P? = visitor.visitMinItemsSchema(this)
}
internal val minItemsLoader: KeywordLoader = { containingObject: IJsonObj, keywordValue: IJsonValue, location: SourceLocation ->
    MinItemsSchema(keywordValue.requireNumber().value, location)
}

data class MinItemsValidationFailure(
    override val schema: MinItemsSchema,
    override val instance: IJsonArray<*>
) : ValidationFailure("expected minimum items: ${schema.minItems}, found only ${instance.length()}", schema, instance, Keyword.MIN_ITEMS)
