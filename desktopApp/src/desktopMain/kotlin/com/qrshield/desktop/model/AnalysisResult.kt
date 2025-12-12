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

package com.qrshield.desktop.model

import com.qrshield.model.Verdict

/**
 * Represents the result of a URL phishing analysis.
 * 
 * @property url The analyzed URL
 * @property score The risk score (0-100)
 * @property verdict The verdict (SAFE, SUSPICIOUS, MALICIOUS, UNKNOWN)
 * @property flags List of detected risk indicators
 * @property timestamp When the analysis was performed
 * 
 * @author QR-SHIELD Team
 * @since 1.0.0
 */
data class AnalysisResult(
    val url: String,
    val score: Int,
    val verdict: Verdict,
    val flags: List<String>,
    val timestamp: Long = System.currentTimeMillis()
)
