# Contributing to QR-SHIELD

Thank you for your interest in contributing to QR-SHIELD! 🛡️

We welcome contributions of all kinds—bug reports, feature requests, documentation improvements, and code contributions.

---

## 📋 Table of Contents

- [Getting Started](#getting-started)
- [Ways to Contribute](#ways-to-contribute)
- [Code Style](#code-style)
- [Testing](#testing)
- [Pull Request Process](#pull-request-process)
- [Security](#security)
- [Questions](#questions)

---

## Getting Started

### Prerequisites

| Requirement | Minimum Version | Purpose |
|-------------|-----------------|---------|
| JDK | 17+ | Build toolchain |
| Android Studio | Hedgehog (2023.1.1) | Android development |
| Xcode | 15+ | iOS development |
| Kotlin | 1.9.22+ | Language |
| Gradle | 8.0+ | Build system |

### Setting Up Development Environment

1. **Fork the repository**

2. **Clone your fork:**
   ```bash
   git clone https://github.com/YOUR_USERNAME/Raoof128.github.io.git
   cd Raoof128.github.io
   ```

3. **Open in Android Studio or IntelliJ IDEA**

4. **Sync Gradle and wait for dependencies to download**

5. **Verify the build:**
   ```bash
   ./gradlew build
   ```

---

## Ways to Contribute

### 🐛 Bug Reports

Found a bug? Please help us fix it:

1. **Check existing issues first** to avoid duplicates
2. **Use the GitHub Issues template**
3. **Include:**
   - Device/platform info (Android version, iOS version, etc.)
   - Steps to reproduce
   - Expected vs. actual behavior
   - Screenshots/videos if UI-related
   - Error logs if applicable

### ✨ Feature Requests

Have an idea? We'd love to hear it:

1. **Check existing issues** to see if it's already requested
2. **Describe the use case clearly**
3. **Consider security implications**
4. **Provide examples** of how it would work

### 📝 Documentation

Help improve our docs:

- Fix typos and grammatical errors
- Add missing documentation
- Improve code examples
- Translate to other languages

### 🔧 Code Contributions

Want to contribute code? Great!

1. **Start with "good first issue"** labels if you're new
2. **Discuss major changes** in an issue first
3. **Follow our code style** guidelines
4. **Write tests** for new functionality
5. **Update documentation** as needed

---

## Code Style

### Kotlin Guidelines

We follow [Kotlin Coding Conventions](https://kotlinlang.org/docs/coding-conventions.html):

```kotlin
// ✅ Good
class PhishingEngine(
    private val heuristicsEngine: HeuristicsEngine,
    private val brandDetector: BrandDetector
) {
    /**
     * Analyze a URL for phishing indicators.
     *
     * @param url The URL to analyze
     * @return RiskAssessment with score and verdict
     */
    fun analyze(url: String): RiskAssessment {
        // Implementation
    }
}

// ❌ Bad
class phishing_engine(private val h: HeuristicsEngine) {
    fun a(u: String): RiskAssessment { /* */ }
}
```

### Key Points

- **Meaningful names**: `calculateRiskScore()` not `calc()`
- **KDoc comments**: Document public APIs
- **Null safety**: Use `?` and `?.let` appropriately
- **Coroutines**: Use `suspend` functions for async operations
- **Immutability**: Prefer `val` over `var`

### Formatting

- Run `./gradlew ktlintFormat` before committing
- Use 4-space indentation
- Maximum line length: 120 characters

---

## Testing

### Running Tests

```bash
# Run all tests
./gradlew allTests

# Run common module tests
./gradlew :common:desktopTest

# Run with coverage
./gradlew :common:koverXmlReport

# Run specific test
./gradlew :common:desktopTest --tests "com.qrshield.engine.HeuristicsEngineTest"
```

### Writing Tests

```kotlin
class MyFeatureTest {
    
    @Test
    fun `should return SAFE for legitimate URL`() {
        val engine = PhishingEngine()
        
        val result = engine.analyze("https://www.google.com")
        
        assertEquals(Verdict.SAFE, result.verdict)
        assertTrue(result.score < 30)
    }
    
    @Test
    fun `should detect brand impersonation`() {
        val engine = PhishingEngine()
        
        val result = engine.analyze("https://paypa1-secure.tk/login")
        
        assertEquals(Verdict.MALICIOUS, result.verdict)
        assertTrue(result.flags.any { it.contains("Brand") })
    }
}
```

### Test Coverage

We aim for **70%+ coverage** on critical paths:

- ✅ PhishingEngine orchestration
- ✅ HeuristicsEngine rules
- ✅ BrandDetector matching
- ✅ TldScorer classification
- ✅ UrlAnalyzer parsing

---

## Pull Request Process

### Before Submitting

1. **Create a feature branch:**
   ```bash
   git checkout -b feature/your-feature
   # or
   git checkout -b fix/bug-description
   ```

2. **Make your changes** following code style guidelines

3. **Write/update tests**

4. **Run all tests:**
   ```bash
   ./gradlew allTests
   ```

5. **Update documentation** if needed

### Submitting

1. **Push your branch:**
   ```bash
   git push origin feature/your-feature
   ```

2. **Open a Pull Request** with:
   - Clear title (e.g., "feat: Add homograph detection for Latin lookalikes")
   - Description of changes
   - Link to related issue (if any)
   - Screenshots for UI changes

### Review Process

1. **Automated checks** must pass (CI, tests, lint)
2. **Code review** by a maintainer
3. **Address feedback** if requested
4. **Squash and merge** once approved

---

## Security

### ⚠️ Important

- **Never commit secrets** or API keys
- **Report vulnerabilities privately** to security@qrshield.dev
- **Follow secure coding practices**

See [SECURITY.md](SECURITY.md) for our full security policy.

---

## Questions?

- 💬 **GitHub Discussions**: General questions and ideas
- 🐛 **GitHub Issues**: Bug reports and feature requests
- 📧 **Email**: contributors@qrshield.dev

---

## License

By contributing, you agree that your contributions will be licensed under the **Apache License 2.0**.

---

Thank you for helping make QR-SHIELD better! 💜

*Together, we're protecting users from QRishing attacks.*
