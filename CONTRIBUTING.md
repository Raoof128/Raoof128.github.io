# Contributing to QR-SHIELD

Thank you for your interest in contributing to QR-SHIELD! 🛡️

## Getting Started

### Prerequisites

- JDK 17+
- Android Studio Hedgehog (2023.1.1) or later
- Xcode 15+ (for iOS development)
- Kotlin 2.0.0

### Setting Up Development Environment

1. Fork the repository
2. Clone your fork:
   ```bash
   git clone https://github.com/YOUR_USERNAME/qrshield.git
   cd qrshield
   ```
3. Open in Android Studio or IntelliJ IDEA
4. Sync Gradle and wait for dependencies to download

## Ways to Contribute

### 🐛 Bug Reports

- Use the GitHub Issues template
- Include device/platform info
- Provide steps to reproduce
- Attach screenshots if UI-related

### ✨ Feature Requests

- Check existing issues first
- Describe the use case clearly
- Consider security implications

### 🔧 Pull Requests

1. Create a feature branch: `git checkout -b feature/your-feature`
2. Make your changes following our code style
3. Write/update tests
4. Run `./gradlew allTests` and ensure all pass
5. Submit PR with clear description

## Code Style

- Follow [Kotlin Coding Conventions](https://kotlinlang.org/docs/coding-conventions.html)
- Use meaningful variable names
- Document public APIs with KDoc
- Keep functions focused and small

## Testing

- Write unit tests for new logic
- Test edge cases
- Ensure cross-platform compatibility

## Security

- Never commit API keys or secrets
- Report security vulnerabilities privately
- Follow secure coding practices

## License

By contributing, you agree that your contributions will be licensed under the Apache License 2.0.

## Questions?

- Open a GitHub Discussion
- Email: contributors@qrshield.dev

Thank you for helping make QR-SHIELD better! 💜
