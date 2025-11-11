package at.quickme.ksync

import com.google.devtools.ksp.processing.SymbolProcessor
import com.google.devtools.ksp.processing.SymbolProcessorEnvironment
import com.google.devtools.ksp.processing.SymbolProcessorProvider

class RepositoryProcessorProvider : SymbolProcessorProvider {
    override fun create(environment: SymbolProcessorEnvironment): SymbolProcessor {
        return RepositoryProcessor(
            options = environment.options,
            logger = environment.logger,
            codeGenerator = environment.codeGenerator
        )
    }
}
