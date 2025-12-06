# Security Policy

## Reporting a Vulnerability

If you discover a security vulnerability in QR-SHIELD, please report it responsibly:

1. **DO NOT** open a public GitHub issue
2. Email security@qrshield.dev with:
   - Description of the vulnerability
   - Steps to reproduce
   - Potential impact
   - Suggested fix (if any)

We will respond within 48 hours and work with you to address the issue.

## Supported Versions

| Version | Supported          |
| ------- | ------------------ |
| 1.x.x   | ✅ Active support  |
| < 1.0   | ❌ Not supported   |

## Security Best Practices

### For Users

- Always download from official sources
- Keep the app updated
- Report suspicious behavior

### For Contributors

- Never commit secrets or API keys
- Use parameterized queries
- Validate all input
- Follow secure coding guidelines

## Known Security Considerations

1. **URL Analysis Limitations**: QR-SHIELD provides risk scoring but cannot guarantee 100% detection
2. **Network Requests**: Optional URL expansion requires network access
3. **Local Storage**: History is encrypted but physical device access could expose data

## Acknowledgments

We appreciate security researchers who help improve QR-SHIELD. Responsible disclosure will be acknowledged in our SECURITY_ACKNOWLEDGMENTS.md.
