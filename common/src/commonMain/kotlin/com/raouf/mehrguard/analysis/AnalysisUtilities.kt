/*
 * Copyright 2025-2026 Mehr Guard Contributors
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

package com.raouf.mehrguard.analysis

import com.raouf.mehrguard.core.UrlAnalyzer
import com.raouf.mehrguard.core.RiskScorer
import com.raouf.mehrguard.core.VerdictEngine

// =============================================================================
// Mehr Guard Analysis Package
// =============================================================================
//
// This package provides URL analysis utilities and parsers.
// These are lower-level components used by orchestrators.
//
// Package Organization:
// - com.raouf.mehrguard.orchestration/ - Main entry points (PhishingOrchestrator)
// - com.raouf.mehrguard.analysis/      - URL analysis utilities (this package)
// - com.raouf.mehrguard.core/          - Constants, error handling, main engines
// - com.raouf.mehrguard.engine/        - Detection engines (Heuristics, Brand, TLD)
// - com.raouf.mehrguard.ml/            - Machine learning models
// - com.raouf.mehrguard.model/         - Data models (RiskAssessment, Verdict)
// - com.raouf.mehrguard.security/      - Input validation, sanitization
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
