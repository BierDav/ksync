package at.quickme.ksync

import com.google.devtools.ksp.processing.CodeGenerator
import com.google.devtools.ksp.processing.KSPLogger
import com.google.devtools.ksp.symbol.KSAnnotation
import com.google.devtools.ksp.symbol.KSClassDeclaration
import com.google.devtools.ksp.symbol.KSDeclaration
import com.google.devtools.ksp.symbol.KSFunctionDeclaration

class RepositoryProcessor(
    options: Map<String, String>,
    logger: KSPLogger,
    codeGenerator: CodeGenerator
) :
    io.github.smyrgeorge.sqlx4k.processor.RepositoryProcessor(
        options,
        logger,
        codeGenerator
    ) {

    override fun additionalImports(): String = "import at.quickme.ksync.EventHook\n"

    override fun additionalAfterCrudHook(
        repo: KSClassDeclaration,
        fnName: String,
        dependentTablesArg: String,
        fnParams: String
    ) =
        "hook?.publish { EventHook(listOf($dependentTablesArg), result, context, ${repo.simpleName()}Events.${fnName}($fnParams)) }"


    private fun KSDeclaration.qualifiedName(): String? = qualifiedName?.asString()
    private fun KSClassDeclaration.simpleName(): String = simpleName.asString()
    private fun KSFunctionDeclaration.simpleName(): String = simpleName.asString()
    private fun KSClassDeclaration.qualifiedName(): String? = qualifiedName?.asString()
    private fun KSAnnotation.qualifiedName(): String? = annotationType.resolve().declaration.qualifiedName?.asString()

}