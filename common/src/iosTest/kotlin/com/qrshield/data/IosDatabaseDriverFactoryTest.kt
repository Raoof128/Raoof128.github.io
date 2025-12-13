/*
 * Copyright 2025-2026 QR-SHIELD Contributors
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.qrshield.data

import kotlin.test.Test
import kotlin.test.assertNotNull
import kotlin.test.assertTrue
import kotlin.test.assertFailsWith

/**
 * iOS-specific unit tests for DatabaseDriverFactory.
 *
 * These tests verify that the iOS platform correctly implements
 * the expect/actual pattern for SQLDelight database access.
 *
 * @author QR-SHIELD Security Team
 * @since 1.0.0
 */
class IosDatabaseDriverFactoryTest {

    @Test
    fun `factory can be instantiated`() {
        val factory = DatabaseDriverFactory()
        assertNotNull(factory, "DatabaseDriverFactory should be instantiable on iOS")
    }

    @Test
    fun `factory creates SqlDriver`() {
        val factory = DatabaseDriverFactory()
        val driver = factory.createDriver()

        assertNotNull(driver, "createDriver should return a non-null SqlDriver")
    }

    @Test
    fun `driver can execute simple query`() {
        val factory = DatabaseDriverFactory()
        val driver = factory.createDriver()

        // Execute a simple query to verify driver works
        driver.execute(null, "SELECT 1", 0)

        // If we get here without exception, the driver works
        assertTrue(true, "Driver should execute simple query without error")
    }
}
