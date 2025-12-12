/*
 * Copyright 2024 QR-SHIELD Contributors
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

package com.qrshield.orchestration

import com.qrshield.core.PhishingEngine
import com.qrshield.model.RiskAssessment

// =============================================================================
// QR-SHIELD Orchestration Package
// =============================================================================
// 
// This package provides the main entry points for phishing detection.
// It contains orchestrators that coordinate multiple analysis engines.
//
// Primary Classes:
// - PhishingOrchestrator: Main entry point for URL analysis
//   Coordinates HeuristicsEngine, BrandDetector, TldScorer, ML Model
//
// Architecture:
// - orchestration/ - Coordinates analysis (this package)
// - engine/ - Detection algorithms (HeuristicsEngine, BrandDetector, etc.)
// - core/ - URL parsing, constants, error handling
// - ml/ - Machine learning models and feature extraction
// ============================================================================

/**
 * Type alias for the main phishing analysis orchestrator.
 * 
 * This provides a cleaner name that reflects the orchestrator pattern.
 * The underlying implementation is [PhishingEngine] from the core package.
 * 
 * ## Example
 * ```kotlin
 * val orchestrator = PhishingOrchestrator()
 * val result = orchestrator.analyze("https://example.com")
 * ```
 * 
 * @see PhishingEngine
 */
typealias PhishingOrchestrator = PhishingEngine

/**
 * Type alias for the analysis result.
 * 
 * Provides semantic clarity that this is an assessment from the orchestrator.
 * 
 * @see RiskAssessment
 */
typealias AnalysisResult = RiskAssessment
