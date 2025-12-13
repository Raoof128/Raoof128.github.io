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

## License

Apache-2.0
