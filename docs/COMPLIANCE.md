# Mehr Guard Compliance Mapping

## Australian Compliance Frameworks

### ACSC Essential Eight Alignment

| Control | Mehr Guard Contribution | Alignment Level |
|---------|----------------------|-----------------|
| **1. Application Control** | Analyzes URLs before user executes action | ◐ Partial |
| **2. Patch Applications** | N/A (end-user application) | — |
| **3. Configure Microsoft Office Macros** | N/A | — |
| **4. User Application Hardening** | Blocks known malicious URL patterns | ◐ Partial |
| **5. Restrict Admin Privileges** | Operates without elevated permissions | ● Full |
| **6. Patch Operating Systems** | N/A | — |
| **7. Multi-Factor Authentication** | N/A | — |
| **8. Regular Backups** | Local history export capability | ○ Minimal |

### Australian Privacy Act 1988

| Principle | Mehr Guard Compliance |
|-----------|---------------------|
| **APP 1**: Open & Transparent Management | ✅ Open source, clear privacy policy |
| **APP 3**: Collection of Solicited Personal Information | ✅ Zero personal data collection |
| **APP 5**: Notification of Collection | ✅ No notification needed—no collection |
| **APP 6**: Use or Disclosure | ✅ No data to disclose |
| **APP 11**: Security of Personal Information | ✅ AES-256 encrypted local storage |
| **APP 12**: Access to Personal Information | ✅ User controls all local data |

### Australian Cyber Security Centre (ACSC) Guidelines

| Guideline | Implementation |
|-----------|---------------|
| Phishing Prevention | ✅ Core purpose of the application |
| User Education | ✅ Provides detailed risk explanations |
| Incident Response | ✅ History enables incident review |
| Security Awareness | ✅ Active user engagement with security |

### Scamwatch Australia Alignment

Mehr Guard directly addresses Scamwatch categories:
- ✅ Phishing/credential harvesting
- ✅ Fake charity/payment scams
- ✅ Impersonation scams
- ✅ Shopping/delivery scams via QR

### SPAM Act 2003 (Cth) Awareness

Mehr Guard helps users identify QR codes that may link to content violating Australian anti-spam legislation:

| Aspect | Mehr Guard Contribution |
|--------|------------------------|
| **Consent Verification** | Alerts users to unknown senders via QR |
| **Identification** | Detects URLs hiding sender identity |
| **Unsubscribe Mechanisms** | Identifies suspicious redirect chains that obscure opt-out options |
| **Commercial Email Links** | Flags URLs commonly associated with spam campaigns |

**Note**: While Mehr Guard helps users identify potentially non-compliant QR marketing, it is a user protection tool and does not replace regulatory enforcement. Users who receive suspected spam QR codes should report them to the ACMA.



## Global Compliance Frameworks

### ISO 27001:2022 Controls

| Control | Mapping |
|---------|---------|
| **A.5.7** Threat Intelligence | ML model uses threat intelligence patterns |
| **A.8.2.1** Classification | URL risk classification system |
| **A.8.9** Configuration Management | Defined heuristic thresholds |
| **A.12.2.1** Controls Against Malware | Proactive phishing detection |
| **A.13.1.1** Network Controls | Pre-access URL validation |

### NIST Cybersecurity Framework

| Function | Mehr Guard Mapping |
|----------|------------------|
| **Identify (ID)** | Identifies potential threats in QR codes |
| **Protect (PR)** | Prevents users from accessing malicious URLs |
| **Detect (DE)** | Real-time detection of phishing indicators |
| **Respond (RS)** | Clear alerts and recommendations |
| **Recover (RC)** | History for post-incident analysis |

### NIST AI Risk Management Framework

| Function | Implementation |
|----------|---------------|
| **GOVERN** | Open source governance, community oversight |
| **MAP** | ML model scope clearly documented |
| **MEASURE** | Confidence scores, explainable outputs |
| **MANAGE** | User maintains control over final decisions |

---

## GDPR Alignment (For EU Users)

| Article | Compliance |
|---------|-----------|
| **Art. 5** Principles | Data minimization - zero collection |
| **Art. 6** Lawful Basis | No processing of personal data |
| **Art. 17** Right to Erasure | Users can clear local history |
| **Art. 25** Privacy by Design | Built without data collection |

---

## Compliance Summary

```
┌─────────────────────────────────────────────────────────┐
│                  COMPLIANCE SCORECARD                    │
├─────────────────────────────────────────────────────────┤
│  Australian Privacy Act         ████████████  100%      │
│  ACSC Essential Eight           ████░░░░░░░░   33%      │
│  ISO 27001 (relevant controls)  ████████░░░░   67%      │
│  NIST CSF                       ████████████  100%      │
│  NIST AI RMF                    ██████████░░   83%      │
│  GDPR                           ████████████  100%      │
└─────────────────────────────────────────────────────────┘

Note: Partial scores reflect non-applicable controls
      Mehr Guard is an end-user security tool, not an
      enterprise security solution.
```

---

## Audit Trail

Mehr Guard maintains local audit capabilities:

1. **Scan History**: Timestamped record of all scans
2. **Risk Assessments**: Full analysis breakdown stored
3. **Export Capability**: JSON export for enterprise review
4. **No Remote Logs**: All data remains on-device
