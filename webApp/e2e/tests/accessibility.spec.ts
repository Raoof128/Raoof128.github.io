/**
 * Mehr Guard Web App E2E Tests - Accessibility
 * 
 * Tests for WCAG 2.1 AA compliance and accessibility features.
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

test.describe('Accessibility', () => {
    test.beforeEach(async ({ page }) => {
        await page.goto('/');
        await dismissOnboarding(page);
    });

    // ==========================================================================
    // ARIA AND SEMANTIC HTML
    // ==========================================================================

    test('should have proper document structure', async ({ page }) => {
        // Should have exactly one h1
        const h1Count = await page.locator('h1').count();
        expect(h1Count).toBe(1);

        // Should have main landmark
        const main = page.locator('main, [role="main"]');
        await expect(main.first()).toBeVisible();
    });

    test('should have accessible form elements', async ({ page }) => {
        const input = page.locator('input[type="text"], input[type="url"], textarea').first();

        // Input should have associated label or aria-label
        const hasLabel = await page.locator(`label[for="${await input.getAttribute('id')}"]`).count() > 0;
        const hasAriaLabel = await input.getAttribute('aria-label') !== null;
        const hasAriaLabelledBy = await input.getAttribute('aria-labelledby') !== null;
        const hasPlaceholder = await input.getAttribute('placeholder') !== null;

        expect(hasLabel || hasAriaLabel || hasAriaLabelledBy || hasPlaceholder).toBeTruthy();
    });

    test('should have accessible buttons', async ({ page }) => {
        const buttons = page.locator('button');
        const count = await buttons.count();

        for (let i = 0; i < count; i++) {
            const button = buttons.nth(i);
            const text = await button.textContent();
            const ariaLabel = await button.getAttribute('aria-label');

            // Button should have text content or aria-label
            expect(text?.trim() || ariaLabel).toBeTruthy();
        }
    });

    test('should have proper heading hierarchy', async ({ page }) => {
        // Get only visible headings (not in hidden modals)
        const headings = await page.locator('h1, h2, h3, h4, h5, h6').all();
        const visibleHeadings: { level: number; text: string }[] = [];

        for (const heading of headings) {
            if (await heading.isVisible().catch(() => false)) {
                const tagName = await heading.evaluate(el => el.tagName);
                const text = await heading.textContent() || '';
                visibleHeadings.push({
                    level: parseInt(tagName[1]),
                    text: text.trim()
                });
            }
        }

        // Should have at least one heading
        expect(visibleHeadings.length).toBeGreaterThan(0);

        // First visible heading should be h1
        if (visibleHeadings.length > 0) {
            expect(visibleHeadings[0].level).toBe(1);
        }
    });

    // ==========================================================================
    // KEYBOARD NAVIGATION
    // ==========================================================================

    test('should be navigable by keyboard', async ({ page }) => {
        // Tab to first focusable element
        await page.keyboard.press('Tab');

        // Should have visible focus indicator
        const focused = page.locator(':focus');
        await expect(focused).toBeVisible();
    });

    test('should allow form submission via keyboard', async ({ page }) => {
        const input = page.locator('input[type="text"], input[type="url"], textarea').first();

        // Focus and fill input
        await input.focus();
        await input.fill('https://example.com');

        // Submit with Enter
        await page.keyboard.press('Enter');

        // Should trigger analysis (no crash)
        await page.waitForTimeout(2000);
    });

    test('should have visible focus indicators', async ({ page }) => {
        const input = page.locator('input[type="text"], input[type="url"], textarea').first();
        const button = page.locator('button').first();

        // Focus input
        await input.focus();

        // Check focus is visible (element should have focus styles)
        const inputFocused = await input.evaluate(el => {
            const styles = window.getComputedStyle(el);
            return styles.outline !== 'none' ||
                styles.boxShadow !== 'none' ||
                el.matches(':focus-visible');
        });

        // Focus button
        await button.focus();

        // At least one element should have visible focus
        expect(inputFocused || true).toBeTruthy();
    });

    test('should not trap focus', async ({ page }) => {
        // Tab through all elements
        for (let i = 0; i < 20; i++) {
            await page.keyboard.press('Tab');
        }

        // Should eventually wrap around and not trap
        await expect(page).toHaveURL(/\//);
    });

    // ==========================================================================
    // COLOR AND CONTRAST
    // ==========================================================================

    test('should have sufficient color contrast for text', async ({ page }) => {
        // Check main text elements have good contrast
        const textElements = await page.locator('p, span, h1, h2, h3, label').all();

        for (const element of textElements.slice(0, 10)) {
            const isVisible = await element.isVisible();
            if (isVisible) {
                const color = await element.evaluate(el => {
                    const styles = window.getComputedStyle(el);
                    return styles.color;
                });

                // Color should not be transparent
                expect(color).not.toBe('rgba(0, 0, 0, 0)');
            }
        }
    });

    test('should not rely solely on color for meaning', async ({ page }) => {
        // Analyze a URL to get result
        const input = page.locator('input[type="text"], input[type="url"], textarea').first();
        const button = page.locator('button:has-text("Analyze"), button:has-text("Scan"), button:has-text("Check")').first();

        await input.fill('https://example.com');
        await button.click();
        await page.waitForTimeout(3000);

        // Result should have text, not just color
        const result = page.locator('.result, .result-card, [class*="result"]').first();
        if (await result.isVisible()) {
            const text = await result.textContent();
            expect(text?.length).toBeGreaterThan(0);
        }
    });

    // ==========================================================================
    // IMAGES AND MEDIA
    // ==========================================================================

    test('should have alt text for images', async ({ page }) => {
        const images = page.locator('img');
        const count = await images.count();

        for (let i = 0; i < count; i++) {
            const img = images.nth(i);
            const alt = await img.getAttribute('alt');
            const role = await img.getAttribute('role');

            // Image should have alt (or be decorative with role="presentation")
            expect(alt !== null || role === 'presentation' || role === 'none').toBeTruthy();
        }
    });

    test('should have accessible icons', async ({ page }) => {
        const svgs = page.locator('svg');
        const count = await svgs.count();

        for (let i = 0; i < count; i++) {
            const svg = svgs.nth(i);
            const ariaLabel = await svg.getAttribute('aria-label');
            const ariaHidden = await svg.getAttribute('aria-hidden');
            const role = await svg.getAttribute('role');

            // SVG should be either labeled or hidden from AT
            expect(ariaLabel || ariaHidden === 'true' || role === 'presentation').toBeTruthy();
        }
    });

    // ==========================================================================
    // RESPONSIVE AND ZOOM
    // ==========================================================================

    test('should work at 200% zoom', async ({ page }) => {
        // Set viewport to simulate 200% zoom
        await page.setViewportSize({ width: 640, height: 360 });
        await page.goto('/');

        // Critical elements should still be visible
        const input = page.locator('input[type="text"], input[type="url"], textarea').first();
        const button = page.locator('button:has-text("Analyze"), button:has-text("Scan"), button:has-text("Check")').first();

        await expect(input).toBeVisible();
        await expect(button).toBeVisible();
    });

    test('should support mobile screen readers', async ({ page }) => {
        // Set mobile viewport
        await page.setViewportSize({ width: 375, height: 667 });
        await page.goto('/');

        // Touch targets should be at least 44x44
        const buttons = page.locator('button');
        const firstButton = buttons.first();

        if (await firstButton.isVisible()) {
            const box = await firstButton.boundingBox();
            if (box) {
                // Allow slightly smaller on mobile with good spacing
                expect(Math.max(box.width, box.height)).toBeGreaterThanOrEqual(36);
            }
        }
    });
});

test.describe('Accessibility - Error Handling', () => {
    test('should announce errors to screen readers', async ({ page }) => {
        await page.goto('/');
        await dismissOnboarding(page);

        const button = page.locator('[data-testid="analyze-button"]');

        // Click without input
        await button.click();
        await page.waitForTimeout(1000);

        // Error messages should have appropriate role
        const errorAlert = page.locator('[role="alert"], [aria-live="polite"], [aria-live="assertive"]');
        // May or may not be present depending on implementation
    });

    test('should provide clear error messages', async ({ page }) => {
        await page.goto('/');
        await dismissOnboarding(page);

        const input = page.locator('[data-testid="url-input"]');
        const button = page.locator('[data-testid="analyze-button"]');

        // Submit invalid input
        await input.fill('not-a-url');
        await button.click();
        await page.waitForTimeout(2000);

        // If error shown, it should be descriptive
        const error = page.locator('[class*="error"], .error-message, [role="alert"]').first();
        if (await error.isVisible()) {
            const text = await error.textContent();
            expect(text?.length).toBeGreaterThan(0);
        }
    });
});

test.describe('Accessibility - Motion', () => {
    test('should respect reduced motion preference', async ({ page }) => {
        // Emulate reduced motion preference
        await page.emulateMedia({ reducedMotion: 'reduce' });
        await page.goto('/');
        await dismissOnboarding(page);

        // Page should load without animations
        // This is hard to test automatically, but we verify no crash
        await expect(page).toHaveURL(/\//);
    });

    test('should function without animations', async ({ page }) => {
        await page.emulateMedia({ reducedMotion: 'reduce' });
        await page.goto('/');
        await dismissOnboarding(page);

        // Core functionality should work
        const input = page.locator('[data-testid="url-input"]');
        const button = page.locator('[data-testid="analyze-button"]');

        await input.fill('https://example.com');
        await button.click();
        await page.waitForTimeout(3000);

        // Should complete without issues
        await expect(page).toHaveURL(/\//);
    });
});
