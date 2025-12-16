package com.qrshield.architecture

import com.lemonappdev.konsist.api.Konsist
import com.lemonappdev.konsist.api.ext.list.withNameEndingWith
import com.lemonappdev.konsist.api.verify.assertFalse
import com.lemonappdev.konsist.api.verify.assertTrue
import kotlin.test.Test

class KonsistTest {
    @Test
    fun `classes in core package should not import ui package`() {
        Konsist.scopeFromProject()
            .classes()
            .filter { it.resideInPackage("com.qrshield.core..") }
            .assertTrue {
                !it.hasImport { import -> import.name.startsWith("com.qrshield.ui") }
            }
    }

    @Test
    fun `view models should have ViewModel suffix`() {
        Konsist.scopeFromProject()
            .classes()
            .filter { it.hasParentWithName("ViewModel") } // Assuming a base ViewModel class or androidx
            .assertTrue { it.name.endsWith("ViewModel") }
    }

    @Test
    fun `domain model classes should be data classes`() {
        Konsist.scopeFromProject()
            .classes()
            .filter { it.resideInPackage("com.qrshield.model..") }
            .filter { !it.name.endsWith("s") } // Exclude utility objects/containers if any
            .assertTrue { it.hasDataModifier || it.hasSealedModifier || it.name.contains("result", ignoreCase = true) }
    }

    @Test
    fun `use cases should reside in usecase package`() {
         // If we had use cases. We might not have them yet.
         // Let's check for "Engine" suffix classes residing in engine or core package
         Konsist.scopeFromProject()
             .classes()
             .withNameEndingWith("Engine")
             .assertTrue { 
                 it.resideInPackage("com.qrshield.core..") || 
                 it.resideInPackage("com.qrshield.engine..") 
             }
    }
}
