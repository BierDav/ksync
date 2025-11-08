package io.github.smyrgeorge.sqlx4k.processor

import com.google.devtools.ksp.processing.Resolver
import com.google.devtools.ksp.symbol.KSAnnotation
import com.google.devtools.ksp.symbol.KSClassDeclaration
import com.google.devtools.ksp.symbol.KSValueArgument
import com.google.devtools.ksp.symbol.KSVisitorVoid
import io.github.smyrgeorge.sqlx4k.annotation.Table

class TableLookup(resolver: Resolver) {
    val lookup = mutableMapOf<String, KSClassDeclaration>()

    init {
        val symbols = resolver
            .getSymbolsWithAnnotation(TypeNames.TABLE_ANNOTATION)
            .filterIsInstance<KSClassDeclaration>()

        symbols.forEach { it.accept(Visitor(), Unit) }
    }

    inner class Visitor : KSVisitorVoid() {
        override fun visitClassDeclaration(classDeclaration: KSClassDeclaration, data: Unit) {
            // Getting the @Table annotation object.
            val table: KSAnnotation = classDeclaration.annotations.first {
                it.qualifiedName() == TypeNames.TABLE_ANNOTATION
            }

            // Getting the 'name' argument object from the @Table.
            val nameArgument: KSValueArgument = table.arguments
                .first { arg -> arg.name?.asString() == Table::name.name }

            // Getting the value of the 'name' argument.
            val tableName = nameArgument.value as String

            lookup[tableName] = classDeclaration
        }
    }

    private fun KSAnnotation.qualifiedName(): String? = annotationType.resolve().declaration.qualifiedName?.asString()

    operator fun get(tableName: String): KSClassDeclaration? = lookup[tableName]
}