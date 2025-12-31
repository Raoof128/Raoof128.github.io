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

package com.raouf.mehrguard.data

import app.cash.sqldelight.db.SqlDriver

/**
 * Expect declaration for platform-specific SqlDriver creation.
 *
 * Each platform (Android, iOS, Desktop, Web) provides its own
 * actual implementation of this class.
 *
 * @author QR-SHIELD Security Team
 * @since 1.0.0
 */
expect class DatabaseDriverFactory {
    fun createDriver(): SqlDriver
}
