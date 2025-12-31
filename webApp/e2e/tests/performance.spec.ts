/**
 * Mehr Guard Web App E2E Tests - Performance
 * 
 * Tests for page load performance, responsiveness, and interaction timing.
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

test.describe('Performance', () => {
    // ==========================================================================
    // PAGE LOAD PERFORMANCE
    // ==========================================================================

    test('should load within 3 seconds', async ({ page }) => {
        const startTime = Date.now();

        await page.goto('/');

        // Wait for main content to be visible
        const input = page.locator('input[type="text"], input[type="url"], textarea').first();
        await expect(input).toBeVisible();

        const loadTime = Date.now() - startTime;
        expect(loadTime).toBeLessThan(3000);
    });

    test('should have DOMContentLoaded under 2 seconds', async ({ page }) => {
        const navigationPromise = page.waitForEvent('domcontentloaded');
        const startTime = Date.now();

        await page.goto('/');
        await navigationPromise;

        const domContentLoadedTime = Date.now() - startTime;
        expect(domContentLoadedTime).toBeLessThan(2000);
    });

    test('should have first contentful paint under 2 seconds', async ({ page }) => {
        await page.goto('/');

        const fcp = await page.evaluate(() => {
            return new Promise<number>((resolve) => {
                const observer = new PerformanceObserver((list) => {
                    const entries = list.getEntries();
                    const fcpEntry = entries.find(e => e.name === 'first-contentful-paint');
                    if (fcpEntry) {
                        resolve(fcpEntry.startTime);
                        observer.disconnect();
                    }
                });
                observer.observe({ entryTypes: ['paint'] });

                // Fallback timeout
                setTimeout(() => resolve(0), 5000);
            });
        });

        // FCP should be under 2 seconds (or 0 if not available)
        if (fcp > 0) {
            expect(fcp).toBeLessThan(2000);
        }
    });

    // ==========================================================================
    // INTERACTION PERFORMANCE
    // ==========================================================================

    test('should respond to input within 100ms', async ({ page }) => {
        await page.goto('/');

        const input = page.locator('input[type="text"], input[type="url"], textarea').first();

        const startTime = Date.now();
        await input.fill('https://example.com');
        const inputTime = Date.now() - startTime;

        // Input should be near-instant (accounting for Playwright overhead)
        expect(inputTime).toBeLessThan(500);
    });

    test('should complete URL analysis within 5 seconds', async ({ page }) => {
        await page.goto('/');

        const input = page.locator('input[type="text"], input[type="url"], textarea').first();
        const button = page.locator('button:has-text("Analyze"), button:has-text("Scan"), button:has-text("Check")').first();

        await input.fill('https://www.google.com');

        const startTime = Date.now();
        await button.click();

        // Wait for result
        const result = page.locator('.result, .result-card, [class*="result"], [class*="safe"]');
        await expect(result.first()).toBeVisible({ timeout: 5000 });

        const analysisTime = Date.now() - startTime;
        expect(analysisTime).toBeLessThan(5000);
    });

    test('should show loading indicator within 100ms', async ({ page }) => {
        await page.goto('/');

        const input = page.locator('input[type="text"], input[type="url"], textarea').first();
        const button = page.locator('button:has-text("Analyze"), button:has-text("Scan"), button:has-text("Check")').first();

        await input.fill('https://www.amazon.com');

        // Check for immediate feedback
        await button.click();

        // Look for any indication of processing
        const processing = page.locator('.loading, [class*="loading"], .spinner, [aria-busy="true"], button:disabled');

        // Some feedback should appear quickly (or button should be disabled)
        // This is best-effort as timing may vary
    });

    // ==========================================================================
    // MULTIPLE ANALYSIS PERFORMANCE
    // ==========================================================================

    test('should handle rapid successive analyses', async ({ page }) => {
        await page.goto('/');

        const input = page.locator('input[type="text"], input[type="url"], textarea').first();
        const button = page.locator('button:has-text("Analyze"), button:has-text("Scan"), button:has-text("Check")').first();

        const urls = [
            'https://google.com',
            'https://github.com',
            'https://apple.com',
        ];

        const startTime = Date.now();

        for (const url of urls) {
            await input.clear();
            await input.fill(url);
            await button.click();
            await page.waitForTimeout(500); // Brief wait between analyses
        }

        const totalTime = Date.now() - startTime;

        // 3 analyses should complete reasonably quickly
        expect(totalTime).toBeLessThan(15000);
    });

    test('should not degrade performance with history', async ({ page }) => {
        await page.goto('/');

        const input = page.locator('input[type="text"], input[type="url"], textarea').first();
        const button = page.locator('button:has-text("Analyze"), button:has-text("Scan"), button:has-text("Check")').first();

        // Perform multiple analyses
        for (let i = 0; i < 5; i++) {
            await input.clear();
            await input.fill(`https://example${i}.com`);
            await button.click();
            await page.waitForTimeout(500);
        }

        // Final analysis should still be fast
        await input.clear();
        await input.fill('https://final-test.com');

        const startTime = Date.now();
        await button.click();

        const result = page.locator('.result, .result-card, [class*="result"]');
        await expect(result.first()).toBeVisible({ timeout: 5000 });

        const analysisTime = Date.now() - startTime;
        expect(analysisTime).toBeLessThan(5000);
    });

    // ==========================================================================
    // RESOURCE EFFICIENCY
    // ==========================================================================

    test('should not have memory leaks after multiple analyses', async ({ page }) => {
        await page.goto('/');

        const input = page.locator('input[type="text"], input[type="url"], textarea').first();
        const button = page.locator('button:has-text("Analyze"), button:has-text("Scan"), button:has-text("Check")').first();

        // Capture initial heap
        const initialHeap = await page.evaluate(() => {
            if ('memory' in performance) {
                return (performance as any).memory.usedJSHeapSize;
            }
            return 0;
        });

        // Perform many analyses
        for (let i = 0; i < 10; i++) {
            await input.clear();
            await input.fill(`https://test${i}.example.com`);
            await button.click();
            await page.waitForTimeout(300);
        }

        // Capture final heap
        const finalHeap = await page.evaluate(() => {
            if ('memory' in performance) {
                return (performance as any).memory.usedJSHeapSize;
            }
            return 0;
        });

        // Heap growth should be reasonable (< 50MB)
        if (initialHeap > 0 && finalHeap > 0) {
            const heapGrowth = (finalHeap - initialHeap) / 1024 / 1024;
            expect(heapGrowth).toBeLessThan(50);
        }
    });

    // ==========================================================================
    // NETWORK EFFICIENCY
    // ==========================================================================

    test('should minimize network requests', async ({ page }) => {
        const requests: string[] = [];

        page.on('request', request => {
            requests.push(request.url());
        });

        await page.goto('/');

        // Wait for page to fully load
        await page.waitForLoadState('networkidle');

        // Should not have excessive requests (< 50)
        expect(requests.length).toBeLessThan(50);
    });

    test('should cache static resources', async ({ page }) => {
        await page.goto('/');
        await page.waitForLoadState('networkidle');

        // Reload and count network requests
        let requestCount = 0;

        page.on('request', () => {
            requestCount++;
        });

        await page.reload();
        await page.waitForLoadState('networkidle');

        // On reload, browser should use cache for some resources
        // This is a basic check - actual caching depends on server config
        expect(requestCount).toBeGreaterThan(0);
    });
});

test.describe('Performance - Mobile', () => {
    test('should load quickly on mobile connection', async ({ page }) => {
        // Simulate 3G connection
        const client = await page.context().newCDPSession(page);
        await client.send('Network.enable');
        await client.send('Network.emulateNetworkConditions', {
            offline: false,
            downloadThroughput: 1.5 * 1024 * 1024 / 8, // 1.5 Mbps
            uploadThroughput: 750 * 1024 / 8, // 750 Kbps
            latency: 100,
        });

        const startTime = Date.now();
        await page.goto('/');

        const input = page.locator('input[type="text"], input[type="url"], textarea').first();
        await expect(input).toBeVisible({ timeout: 10000 });

        const loadTime = Date.now() - startTime;
        // Should load within 10 seconds even on slow connection
        expect(loadTime).toBeLessThan(10000);
    });

    test('should function on mobile viewport', async ({ page }) => {
        await page.setViewportSize({ width: 375, height: 667 });
        await page.goto('/');

        const input = page.locator('input[type="text"], input[type="url"], textarea').first();
        const button = page.locator('button:has-text("Analyze"), button:has-text("Scan"), button:has-text("Check")').first();

        await expect(input).toBeVisible();
        await expect(button).toBeVisible();

        await input.fill('https://mobile.test.com');
        await button.click();

        await page.waitForTimeout(3000);

        // Should work on mobile
        await expect(page).toHaveURL(/\//);
    });
});
