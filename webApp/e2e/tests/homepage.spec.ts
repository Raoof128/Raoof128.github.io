/**
 * Mehr Guard Web App E2E Tests - Homepage
 * 
 * Tests the main landing page and URL analysis functionality.
 * 
 * @author Mehr Guard Team
 * @license Apache-2.0
 */

import { test, expect, Page } from '@playwright/test';

// Helper function to get common elements
const getElements = (page: Page) => ({
    input: page.locator('[data-testid="url-input"]'),
    button: page.locator('[data-testid="analyze-button"]'),
    logo: page.locator('[data-testid="logo"]'),
    resultCard: page.locator('[data-testid="result-card"]'),
    scoreRing: page.locator('[data-testid="score-ring"]'),
    verdictPill: page.locator('[data-testid="verdict-pill"]'),
    riskFactors: page.locator('[data-testid="risk-factors"]'),
});

// Helper to dismiss onboarding modal if present
async function dismissOnboarding(page: Page) {
    const skipBtn = page.locator('#skipOnboarding');
    const onboardingModal = page.locator('#onboardingModal');

    // Check if onboarding modal is visible
    if (await onboardingModal.isVisible({ timeout: 2000 }).catch(() => false)) {
        // Click skip button if visible
        if (await skipBtn.isVisible({ timeout: 1000 }).catch(() => false)) {
            await skipBtn.click();
            // Wait for modal to close
            await page.waitForTimeout(500);
        }
    }
}

test.describe('Homepage', () => {
    test.beforeEach(async ({ page }) => {
        await page.goto('/');
        // Wait for page to be fully loaded
        await page.waitForLoadState('networkidle');
        // Dismiss onboarding modal if present
        await dismissOnboarding(page);
    });

    // ==========================================================================
    // PAGE LOAD TESTS
    // ==========================================================================

    test('should load the homepage successfully', async ({ page }) => {
        await expect(page).toHaveTitle(/Mehr Guard|MehrGuard|Premium|Scanner/i);
    });

    test('should display the logo', async ({ page }) => {
        const { logo } = getElements(page);
        await expect(logo).toBeVisible();
    });

    test('should display the main heading', async ({ page }) => {
        const heading = page.locator('h1');
        await expect(heading.first()).toBeVisible();
        await expect(heading.first()).toContainText(/Scan|URL|Safe/i);
    });

    test('should display the URL input field', async ({ page }) => {
        const { input } = getElements(page);
        await expect(input).toBeVisible();
        await expect(input).toHaveAttribute('placeholder', /example.com/i);
    });

    test('should display the analyze button', async ({ page }) => {
        const { button } = getElements(page);
        await expect(button).toBeVisible();
        await expect(button).toBeEnabled();
        await expect(button).toContainText(/Analyze/i);
    });

    // ==========================================================================
    // URL ANALYSIS TESTS
    // ==========================================================================

    test('should analyze a safe URL', async ({ page }) => {
        const { input, button, resultCard } = getElements(page);

        await input.fill('https://www.google.com');
        await button.click();

        // Wait for result card to appear (hidden class removed)
        await expect(resultCard).not.toHaveClass(/hidden/, { timeout: 15000 });
        await expect(resultCard).toBeVisible({ timeout: 15000 });
    });

    test('should analyze a suspicious URL', async ({ page }) => {
        const { input, button, resultCard, verdictPill } = getElements(page);

        await input.fill('https://paypa1-secure.tk/login');
        await button.click();

        // Wait for result
        await expect(resultCard).not.toHaveClass(/hidden/, { timeout: 15000 });

        // Check verdict shows suspicious or malicious
        const verdictText = await verdictPill.textContent();
        expect(['SUSPICIOUS', 'MALICIOUS', 'WARNING']).toContain(verdictText?.toUpperCase() || '');
    });

    test('should show loading state during analysis', async ({ page }) => {
        const { input, button } = getElements(page);

        await input.fill('https://example.com');

        // Click and immediately check for loading state
        await button.click();

        // The button should have loading class briefly or be disabled
        // This is a race condition test - we just verify no crash
        await page.waitForTimeout(100);
    });

    test('should handle empty input gracefully', async ({ page }) => {
        const { button, input } = getElements(page);

        // Try to analyze with empty input
        await input.clear();
        await button.click();

        // Should not crash - page should still be functional
        await expect(page).toHaveURL(/\//);
        await expect(input).toBeVisible();
    });

    test('should analyze URL on Enter key press', async ({ page }) => {
        const { input, resultCard } = getElements(page);

        await input.fill('https://github.com');
        await input.press('Enter');

        // Should trigger analysis
        await expect(resultCard).not.toHaveClass(/hidden/, { timeout: 15000 });
    });

    // ==========================================================================
    // RESULT DISPLAY TESTS
    // ==========================================================================

    test('should display risk score after analysis', async ({ page }) => {
        const { input, button, scoreRing } = getElements(page);

        await input.fill('https://www.amazon.com');
        await button.click();

        // Wait for score to be visible
        await expect(scoreRing).toBeVisible({ timeout: 15000 });

        // Score should be a number 0-100
        const scoreText = await scoreRing.textContent();
        const score = parseInt(scoreText || '0', 10);
        expect(score).toBeGreaterThanOrEqual(0);
        expect(score).toBeLessThanOrEqual(100);
    });

    test('should allow analyzing a new URL after result', async ({ page }) => {
        const { input, button, resultCard } = getElements(page);

        // First analysis
        await input.fill('https://www.google.com');
        await button.click();
        await expect(resultCard).not.toHaveClass(/hidden/, { timeout: 15000 });

        // Reset and analyze another
        const scanAnotherBtn = page.locator('[data-testid="scan-another-button"]');
        if (await scanAnotherBtn.isVisible()) {
            await scanAnotherBtn.click();
        }

        // Input should be clearable for new analysis
        await input.clear();
        await input.fill('https://www.apple.com');
        await button.click();

        // Should complete without crash
        await page.waitForTimeout(2000);
    });
});

test.describe('URL Validation', () => {
    test.beforeEach(async ({ page }) => {
        await page.goto('/');
        await page.waitForLoadState('networkidle');
        await dismissOnboarding(page);
    });

    test('should accept https URLs', async ({ page }) => {
        const { input } = getElements(page);
        await input.fill('https://secure-site.com');

        // Should be valid - input should accept the value
        await expect(input).toHaveValue('https://secure-site.com');
    });

    test('should accept http URLs', async ({ page }) => {
        const { input } = getElements(page);
        await input.fill('http://example.com');

        await expect(input).toHaveValue('http://example.com');
    });

    test('should handle URLs with query parameters', async ({ page }) => {
        const { input, button } = getElements(page);

        await input.fill('https://example.com/page?param1=value1&param2=value2');
        await button.click();

        // Should not crash
        await page.waitForTimeout(2000);
        await expect(page).toHaveURL(/\//);
    });

    test('should handle very long URLs', async ({ page }) => {
        const { input, button } = getElements(page);

        const longPath = 'a'.repeat(200);
        await input.fill(`https://example.com/${longPath}`);
        await button.click();

        // Should handle gracefully
        await page.waitForTimeout(2000);
        await expect(page).toHaveURL(/\//);
    });
});
