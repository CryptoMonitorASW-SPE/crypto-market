package hexagonal

import com.lemonappdev.konsist.api.Konsist
import com.lemonappdev.konsist.api.architecture.KoArchitectureCreator.assertArchitecture
import com.lemonappdev.konsist.api.architecture.Layer
import kotlin.test.Test

class DependencyRuleTest {
    @Test
    fun `clean architecture layers have correct dependencies`() {
        Konsist
            .scopeFromProject()
            .assertArchitecture {
                // Define layers
                val domain = Layer("Domain", "it.unibo.domain..")
                val application = Layer("Application", "it.unibo.application..")
                val infrastructureAdapter = Layer("Infrastructure Adapter", "it.unibo.infrastructure.adapter..")
                val main = Layer("Main", "it.unibo..")

                domain.dependsOnNothing()
                application.dependsOn(domain)
                infrastructureAdapter.dependsOn(domain)
                main.dependsOn(application, infrastructureAdapter, domain)
            }
    }
}
