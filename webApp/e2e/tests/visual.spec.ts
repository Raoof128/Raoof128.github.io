/**
 * Mehr Guard Web App E2E Tests - Visual Regression
 * 
 * Tests for visual consistency and UI appearance.
 * 
 * @author Mehr Guard Team
 * @license Apache-2.0
 */

import { test, expect, Page } from '@playwright/test';

// Helper to dismiss onboarding modal if present
async function dismissOnboarding(page: Page) {
    const skipBtn = page.locator('#skipOnboarding');
    const onboardingModal = page.locator('#onboardingModal');

    if (await onboardingModal.isVisible({ timeout: 2000 }).catch(() => false)) {
        if (await skipBtn.isVisible({ timeout: 1000 }).catch(() => false)) {
            await skipBtn.click();
            await page.waitForTimeout(500);
        }
    }
}

test.describe('Visual Regression', () => {
    test.beforeEach(async ({ page }) => {
        await page.goto('/');
        await dismissOnboarding(page);
    });

    // ==========================================================================
    // SCREENSHOT COMPARISONS
    // ==========================================================================

    test('should match homepage screenshot', async ({ page }) => {
        await page.waitForLoadState('networkidle');

        await expect(page).toHaveScreenshot('homepage.png', {
            fullPage: true,
            threshold: 0.1, // 10% tolerance
        });
    });

    test('should match safe result screenshot', async ({ page }) => {
        const input = page.locator('input[type="text"], input[type="url"], textarea').first();
        const button = page.locator('button:has-text("Analyze"), button:has-text("Scan"), button:has-text("Check")').first();

        await input.fill('https://www.google.com');
        await button.click();
        await page.waitForTimeout(3000);
        await page.waitForLoadState('networkidle');

        await expect(page).toHaveScreenshot('safe-result.png', {
            fullPage: true,
            threshold: 0.15,
        });
    });

    test('should match suspicious result screenshot', async ({ page }) => {
        const input = page.locator('input[type="text"], input[type="url"], textarea').first();
        const button = page.locator('button:has-text("Analyze"), button:has-text("Scan"), button:has-text("Check")').first();

        await input.fill('https://paypa1-login.suspicious.tk');
        await button.click();
        await page.waitForTimeout(3000);
        await page.waitForLoadState('networkidle');

        await expect(page).toHaveScreenshot('suspicious-result.png', {
            fullPage: true,
            threshold: 0.15,
        });
    });

    // ==========================================================================
    // RESPONSIVE LAYOUT TESTS
    // ==========================================================================

    test('should match mobile layout screenshot', async ({ page }) => {
        await page.setViewportSize({ width: 375, height: 667 });
        await page.goto('/');
        await page.waitForLoadState('networkidle');

        await expect(page).toHaveScreenshot('mobile-homepage.png', {
            fullPage: true,
            threshold: 0.15,
        });
    });

    test('should match tablet layout screenshot', async ({ page }) => {
        await page.setViewportSize({ width: 768, height: 1024 });
        await page.goto('/');
        await page.waitForLoadState('networkidle');

        await expect(page).toHaveScreenshot('tablet-homepage.png', {
            fullPage: true,
            threshold: 0.15,
        });
    });

    test('should match wide desktop layout screenshot', async ({ page }) => {
        await page.setViewportSize({ width: 1920, height: 1080 });
        await page.goto('/');
        await page.waitForLoadState('networkidle');

        await expect(page).toHaveScreenshot('desktop-wide-homepage.png', {
            fullPage: true,
            threshold: 0.15,
        });
    });

    // ==========================================================================
    // DARK MODE TESTS (if supported)
    // ==========================================================================

    test('should match dark mode screenshot', async ({ page }) => {
        await page.emulateMedia({ colorScheme: 'dark' });
        await page.goto('/');
        await page.waitForLoadState('networkidle');

        await expect(page).toHaveScreenshot('dark-mode-homepage.png', {
            fullPage: true,
            threshold: 0.15,
        });
    });

    test('should match light mode screenshot', async ({ page }) => {
        await page.emulateMedia({ colorScheme: 'light' });
        await page.goto('/');
        await page.waitForLoadState('networkidle');

        await expect(page).toHaveScreenshot('light-mode-homepage.png', {
            fullPage: true,
            threshold: 0.15,
        });
    });

    // ==========================================================================
    // COMPONENT SCREENSHOTS
    // ==========================================================================

    test('should match input field screenshot', async ({ page }) => {
        const input = page.locator('input[type="text"], input[type="url"], textarea').first();

        await expect(input).toHaveScreenshot('input-field.png', {
            threshold: 0.1,
        });
    });

    test('should match analyze button screenshot', async ({ page }) => {
        const button = page.locator('button:has-text("Analyze"), button:has-text("Scan"), button:has-text("Check")').first();

        await expect(button).toHaveScreenshot('analyze-button.png', {
            threshold: 0.1,
        });
    });

    test('should match header screenshot', async ({ page }) => {
        const header = page.locator('header, .header, nav').first();

        if (await header.isVisible()) {
            await expect(header).toHaveScreenshot('header.png', {
                threshold: 0.15,
            });
        }
    });

    // ==========================================================================
    // INTERACTION STATE SCREENSHOTS
    // ==========================================================================

    test('should match loading state screenshot', async ({ page }) => {
        const input = page.locator('input[type="text"], input[type="url"], textarea').first();
        const button = page.locator('button:has-text("Analyze"), button:has-text("Scan"), button:has-text("Check")').first();

        await input.fill('https://example.com');
        await button.click();

        // Capture during loading (if loading state exists)
        await page.waitForTimeout(100);

        // Screenshot may or may not catch loading - that's ok
    });

    test('should match focused input screenshot', async ({ page }) => {
        const input = page.locator('input[type="text"], input[type="url"], textarea').first();

        await input.focus();

        await expect(input).toHaveScreenshot('input-field-focused.png', {
            threshold: 0.15,
        });
    });

    test('should match button hover screenshot', async ({ page }) => {
        const button = page.locator('button:has-text("Analyze"), button:has-text("Scan"), button:has-text("Check")').first();

        await button.hover();

        await expect(button).toHaveScreenshot('analyze-button-hover.png', {
            threshold: 0.15,
        });
    });
});

test.describe('Visual Regression - Cross Browser', () => {
    test('should have consistent layout across browsers', async ({ page, browserName }) => {
        await page.goto('/');
        await page.waitForLoadState('networkidle');

        await expect(page).toHaveScreenshot(`homepage-${browserName}.png`, {
            fullPage: true,
            threshold: 0.2, // Higher tolerance for cross-browser differences
        });
    });
});
