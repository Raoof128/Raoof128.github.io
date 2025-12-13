# QR-SHIELD Web E2E Tests

End-to-end tests for the QR-SHIELD web application using [Playwright](https://playwright.dev/).

## Prerequisites

- Node.js 18+ 
- npm 9+

## Setup

```bash
# Install dependencies
npm install

# Install browsers
npx playwright install
```

## Running Tests

```bash
# Run all tests (headless)
npm test

# Run tests with visible browser
npm run test:headed

# Run tests in debug mode
npm run test:debug

# Run tests with UI
npm run test:ui
```

## Test Suites

| Suite | Description |
|-------|-------------|
| `homepage.spec.ts` | Core URL analysis functionality |
| `accessibility.spec.ts` | WCAG 2.1 AA compliance tests |
| `performance.spec.ts` | Page load and interaction timing |
| `visual.spec.ts` | Visual regression tests with screenshots |

## Test Reports

After running tests, view the HTML report:

```bash
npm run test:report
```

## Configuration

See `playwright.config.ts` for:
- Browser targets (Chrome, Firefox, Safari, Mobile)
- Base URL configuration  
- Timeout settings
- CI/CD settings

## CI Integration

Tests automatically start the web dev server before running.
Set `CI=true` environment variable for CI mode.

## Writing New Tests

```typescript
import { test, expect } from '@playwright/test';

test('should do something', async ({ page }) => {
  await page.goto('/');
  await expect(page.locator('h1')).toBeVisible();
});
```

## Best Practices

1. **Use stable selectors**: Prefer `data-testid`, `role`, or semantic elements
2. **Wait for visibility**: Use `expect(element).toBeVisible()` 
3. **Keep tests independent**: Each test should be runnable in isolation
4. **Use page objects**: For complex pages, extract selectors into page objects
5. **Dismiss modals**: Use `dismissOnboarding(page)` helper in beforeEach

## Available Test IDs

The web app provides the following `data-testid` attributes:

| Selector | Element |
|----------|---------|
| `url-input` | Main URL input field |
| `analyze-button` | Analyze URL button |
| `logo` | QR-SHIELD logo |
| `result-card` | Analysis result container |
| `score-ring` | Risk score display |
| `verdict-pill` | Verdict badge (SAFE/SUSPICIOUS/MALICIOUS) |
| `risk-factors` | Risk factors list |
| `scan-another-button` | Reset/scan another button |
| `share-button` | Share result button |
| `report-button` | Report phishing button |

## Handling Onboarding Modal

First-time users see an onboarding modal. Tests should dismiss it:

```typescript
async function dismissOnboarding(page: Page) {
    const skipBtn = page.locator('#skipOnboarding');
    const modal = page.locator('#onboardingModal');
    
    if (await modal.isVisible({ timeout: 2000 }).catch(() => false)) {
        if (await skipBtn.isVisible({ timeout: 1000 }).catch(() => false)) {
            await skipBtn.click();
            await page.waitForTimeout(500);
        }
    }
}
```

## License

Apache-2.0
