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

package com.qrshield.analysis

import com.qrshield.core.UrlAnalyzer
import com.qrshield.core.RiskScorer
import com.qrshield.core.VerdictEngine

// =============================================================================
// QR-SHIELD Analysis Package
// =============================================================================
// 
// This package provides URL analysis utilities and parsers.
// These are lower-level components used by orchestrators.
//
// Package Organization:
// - com.qrshield.orchestration/ - Main entry points (PhishingOrchestrator)
// - com.qrshield.analysis/      - URL analysis utilities (this package)
// - com.qrshield.core/          - Constants, error handling, main engines
// - com.qrshield.engine/        - Detection engines (Heuristics, Brand, TLD)
// - com.qrshield.ml/            - Machine learning models
// - com.qrshield.model/         - Data models (RiskAssessment, Verdict)
// - com.qrshield.security/      - Input validation, sanitization
// =============================================================================

/**
 * Type alias for URL analysis functionality.
 * 
 * Provides URL parsing, component extraction, and security validation.
 * 
 * @see UrlAnalyzer
 */
typealias UrlAnalysisUtility = UrlAnalyzer

/**
 * Type alias for risk scoring utility.
 * 
 * @see RiskScorer
 */
typealias RiskScoringUtility = RiskScorer

/**
 * Type alias for verdict determination.
 * 
 * @see VerdictEngine
 */
typealias VerdictDeterminer = VerdictEngine
