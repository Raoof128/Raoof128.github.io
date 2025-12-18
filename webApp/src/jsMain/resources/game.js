/**
 * QR-SHIELD Beat the Bot Game Controller
 * 
 * Handles game logic, scoring, round progression,
 * and integration with the PhishingEngine.
 * 
 * @author QR-SHIELD Team
 * @version 2.4.1
 */

// =============================================================================
// CONFIGURATION
// =============================================================================

const GameConfig = {
    version: '2.4.1',
    totalRounds: 10,
    pointsCorrect: 100,
    pointsWrong: -25,
    streakBonus: 25,
    botBasePoints: 100,
    storageKey: 'qrshield_game_stats',
};

// =============================================================================
// GAME STATE
// =============================================================================

const GameState = {
    currentRound: 1,
    sessionId: generateSessionId(),
    playerScore: 0,
    botScore: 0,
    playerStreak: 0,
    bestStreak: 0,
    correctAnswers: 0,
    roundStartTime: null,
    challenges: [],
    currentChallenge: null,
    isGameOver: false,
};

// =============================================================================
// CHALLENGE DATABASE
// =============================================================================

const CHALLENGES = [
    {
        url: 'https://au-post-tracking.verify-deliveries.net/login',
        message: '"AusPost: Your parcel failed delivery due to incorrect address details. Please update immediately via the link above to avoid return."',
        sender: '+61 491 570 010',
        isPhishing: true,
        hint: 'Look closely at the domain structure. Legitimate organisations rarely use hyphens in the main domain name or generic TLDs like <code>.net</code> for core services.',
        reasons: [
            'Domain uses suspicious hyphenated structure (verify-deliveries.net)',
            'SMS from unknown number impersonating AusPost',
            'Domain age < 30 days (recently registered)',
        ],
        educational: 'Official postal services use their primary domain (auspost.com.au) for all tracking. Verify by visiting the official website directly.',
    },
    {
        url: 'https://www.github.com/login',
        message: 'Sign in to your GitHub account to access your repositories.',
        sender: 'GitHub',
        isPhishing: false,
        hint: 'Check the domain carefully. This appears to be a well-known, trusted domain.',
        reasons: [
            'Certificate Issuer matches domain owner (DigiCert Inc)',
            'Domain age > 10 years (High trust)',
            'Top 100 Alexa Rank - whitelisted',
        ],
        educational: 'GitHub.com is a legitimate developer platform owned by Microsoft. Always verify the exact domain spelling.',
    },
    {
        url: 'https://secure-banking.c0mmonwealth.net/verify',
        message: '"Commonwealth Bank: Your account has been locked due to suspicious activity. Verify your identity immediately."',
        sender: '+61 400 123 456',
        isPhishing: true,
        hint: 'Look at the domain carefully. Is "c0mmonwealth" spelled correctly? The number 0 is often used to replace the letter O.',
        reasons: [
            'Homograph attack: "c0mmonwealth" uses zero instead of letter O',
            'Domain mimics legitimate bank (commbank.com.au)',
            'Urgency tactics in message',
        ],
        educational: 'Homograph attacks use similar-looking characters. Always check URLs character by character for suspicious substitutions.',
    },
    {
        url: 'https://www.atlassian.com/software/jira',
        message: 'Welcome to Jira - project tracking for agile teams.',
        sender: 'Atlassian',
        isPhishing: false,
        hint: 'This is a well-established software company domain.',
        reasons: [
            'Valid Extended Validation certificate',
            'Domain age > 15 years',
            'Alexa Top 1000 site',
        ],
        educational: 'Atlassian is a legitimate software company. Their primary domain atlassian.com is trustworthy.',
    },
    {
        url: 'http://signin.apple-id-verify.com/account',
        message: '"Your Apple ID was used to sign in on a new device. If this was not you, verify immediately."',
        sender: 'Apple Support',
        isPhishing: true,
        hint: 'Apple would never use a third-party domain for account verification. Check the actual domain name.',
        reasons: [
            'Domain "apple-id-verify.com" is not owned by Apple',
            'HTTP instead of HTTPS (no encryption)',
            'Subdomain "signin" used for deception',
        ],
        educational: 'Apple only uses apple.com for all account-related activities. Be wary of domains containing brand names but with different extensions.',
    },
    {
        url: 'https://mail.google.com/mail/u/0/',
        message: 'You have 3 unread emails in your inbox.',
        sender: 'Gmail',
        isPhishing: false,
        hint: 'This is a Google subdomain - check if it\'s the legitimate Gmail service.',
        reasons: [
            'Subdomain of google.com (trusted)',
            'Valid Google certificate',
            'Standard Gmail URL structure',
        ],
        educational: 'Google services use subdomains of google.com. mail.google.com is the legitimate Gmail address.',
    },
    {
        url: 'https://www.paypa1-secure.com/login',
        message: '"Your PayPal account has been limited. Please update your information."',
        sender: 'PayPal Security',
        isPhishing: true,
        hint: 'Look at the domain name carefully. Is "paypa1" spelled correctly?',
        reasons: [
            'Homograph attack: "paypa1" uses number 1 instead of letter L',
            'Not the official PayPal domain (paypal.com)',
            'Domain registered in the last 24 hours',
        ],
        educational: 'The number 1 and lowercase L look identical in many fonts. Always verify paypal.com directly.',
    },
    {
        url: 'https://linkedin.com/in/john-smith',
        message: 'View John Smith\'s professional profile on LinkedIn.',
        sender: 'LinkedIn',
        isPhishing: false,
        hint: 'This is the official LinkedIn domain for professional networking.',
        reasons: [
            'Official LinkedIn domain',
            'Standard profile URL structure',
            'Microsoft-owned platform',
        ],
        educational: 'LinkedIn uses linkedin.com as their primary domain. Profile URLs follow the /in/username format.',
    },
    {
        url: 'https://bit.ly/3x8K9mZ',
        message: '"You won a $500 gift card! Click to claim your prize."',
        sender: '+1 555 123 4567',
        isPhishing: true,
        hint: 'Short URLs hide the actual destination. Prize claims from unknown numbers are almost always scams.',
        reasons: [
            'Shortened URL hides true destination',
            'Unsolicited prize notification',
            'Unknown sender number',
        ],
        educational: 'Never trust shortened URLs from unknown sources. Use URL expander tools to see the real destination before clicking.',
    },
    {
        url: 'https://docs.google.com/document/d/1abc123',
        message: 'Sarah shared a document with you: Q4 Report.docx',
        sender: 'Google Docs',
        isPhishing: false,
        hint: 'Check if this is a legitimate Google subdomain for document sharing.',
        reasons: [
            'Subdomain of google.com',
            'Standard Google Docs URL format',
            'Valid Google certificate',
        ],
        educational: 'Google Docs uses docs.google.com. Always verify the sender knows you before opening shared documents.',
    },
];

// =============================================================================
// DOM ELEMENTS
// =============================================================================

const elements = {
    // Session
    sessionId: null,
    endSessionBtn: null,

    // Progress
    currentRound: null,
    totalRounds: null,
    progressFill: null,

    // Challenge
    challengeUrl: null,
    challengeMessage: null,
    hintText: null,
    btnPhishing: null,
    btnLegitimate: null,

    // Scoreboard
    playerScore: null,
    botScore: null,
    playerBar: null,
    botBar: null,
    playerStreak: null,
    playerAccuracy: null,

    // Analysis
    analysisRound: null,
    resultBadge: null,
    resultTitle: null,
    resultDesc: null,
    botDecision: null,
    reasoningList: null,
    educationalNote: null,

    // Modals
    resultModal: null,
    modalIcon: null,
    modalTitle: null,
    modalMessage: null,
    modalPoints: null,
    modalTime: null,
    nextRoundBtn: null,

    gameOverModal: null,
    gameOverTitle: null,
    gameOverMessage: null,
    finalPlayerScore: null,
    finalBotScore: null,
    finalAccuracy: null,
    finalBestStreak: null,
    playAgainBtn: null,

    // Toast
    toast: null,
    toastMessage: null,
};

// =============================================================================
// INITIALIZATION
// =============================================================================

document.addEventListener('DOMContentLoaded', () => {
    console.log('[QR-SHIELD Game] Initializing v' + GameConfig.version);

    cacheElements();
    setupEventListeners();
    initializeGame();

    console.log('[QR-SHIELD Game] Ready');
});

function cacheElements() {
    elements.sessionId = document.getElementById('sessionId');
    elements.endSessionBtn = document.getElementById('endSessionBtn');
    elements.currentRound = document.getElementById('currentRound');
    elements.totalRounds = document.getElementById('totalRounds');
    elements.progressFill = document.getElementById('progressFill');
    elements.challengeUrl = document.getElementById('challengeUrl');
    elements.challengeMessage = document.getElementById('challengeMessage');
    elements.hintText = document.getElementById('hintText');
    elements.btnPhishing = document.getElementById('btnPhishing');
    elements.btnLegitimate = document.getElementById('btnLegitimate');
    elements.playerScore = document.getElementById('playerScore');
    elements.botScore = document.getElementById('botScore');
    elements.playerBar = document.getElementById('playerBar');
    elements.botBar = document.getElementById('botBar');
    elements.playerStreak = document.getElementById('playerStreak');
    elements.playerAccuracy = document.getElementById('playerAccuracy');
    elements.analysisRound = document.getElementById('analysisRound');
    elements.resultBadge = document.getElementById('resultBadge');
    elements.resultTitle = document.getElementById('resultTitle');
    elements.resultDesc = document.getElementById('resultDesc');
    elements.botDecision = document.getElementById('botDecision');
    elements.reasoningList = document.getElementById('reasoningList');
    elements.educationalNote = document.getElementById('educationalNote');
    elements.resultModal = document.getElementById('resultModal');
    elements.modalIcon = document.getElementById('modalIcon');
    elements.modalTitle = document.getElementById('modalTitle');
    elements.modalMessage = document.getElementById('modalMessage');
    elements.modalPoints = document.getElementById('modalPoints');
    elements.modalTime = document.getElementById('modalTime');
    elements.nextRoundBtn = document.getElementById('nextRoundBtn');
    elements.gameOverModal = document.getElementById('gameOverModal');
    elements.gameOverTitle = document.getElementById('gameOverTitle');
    elements.gameOverMessage = document.getElementById('gameOverMessage');
    elements.finalPlayerScore = document.getElementById('finalPlayerScore');
    elements.finalBotScore = document.getElementById('finalBotScore');
    elements.finalAccuracy = document.getElementById('finalAccuracy');
    elements.finalBestStreak = document.getElementById('finalBestStreak');
    elements.playAgainBtn = document.getElementById('playAgainBtn');
    elements.toast = document.getElementById('toast');
    elements.toastMessage = document.getElementById('toastMessage');
}

function setupEventListeners() {
    elements.btnPhishing?.addEventListener('click', () => handleDecision(true));
    elements.btnLegitimate?.addEventListener('click', () => handleDecision(false));
    elements.nextRoundBtn?.addEventListener('click', nextRound);
    elements.playAgainBtn?.addEventListener('click', resetGame);
    elements.endSessionBtn?.addEventListener('click', endSession);

    // Keyboard shortcuts
    document.addEventListener('keydown', (e) => {
        if (GameState.isGameOver) return;

        if (e.key === 'p' || e.key === 'P' || e.key === '1') {
            handleDecision(true);
        } else if (e.key === 'l' || e.key === 'L' || e.key === '2') {
            handleDecision(false);
        } else if (e.key === 'Enter' && !elements.resultModal?.classList.contains('hidden')) {
            nextRound();
        }
    });
}

// =============================================================================
// GAME LOGIC
// =============================================================================

function initializeGame() {
    // Shuffle challenges
    GameState.challenges = shuffleArray([...CHALLENGES]).slice(0, GameConfig.totalRounds);

    // Update session ID display
    if (elements.sessionId) {
        elements.sessionId.textContent = GameState.sessionId;
    }

    // Update total rounds
    if (elements.totalRounds) {
        elements.totalRounds.textContent = GameConfig.totalRounds;
    }

    // Load first round
    loadRound();
}

function loadRound() {
    if (GameState.currentRound > GameConfig.totalRounds) {
        endGame();
        return;
    }

    GameState.currentChallenge = GameState.challenges[GameState.currentRound - 1];
    GameState.roundStartTime = Date.now();

    // Update UI
    updateRoundDisplay();
    updateChallengeDisplay();
    updateScoreDisplay();
}

function handleDecision(isPhishing) {
    if (!GameState.currentChallenge || GameState.isGameOver) return;

    const responseTime = (Date.now() - GameState.roundStartTime) / 1000;
    const isCorrect = isPhishing === GameState.currentChallenge.isPhishing;

    // Calculate points
    let points = 0;
    if (isCorrect) {
        points = GameConfig.pointsCorrect;
        GameState.playerStreak++;
        GameState.correctAnswers++;

        // Streak bonus
        if (GameState.playerStreak >= 3) {
            points += GameConfig.streakBonus * (GameState.playerStreak - 2);
        }

        // Update best streak
        if (GameState.playerStreak > GameState.bestStreak) {
            GameState.bestStreak = GameState.playerStreak;
        }
    } else {
        points = GameConfig.pointsWrong;
        GameState.playerStreak = 0;
    }

    GameState.playerScore = Math.max(0, GameState.playerScore + points);

    // Bot always gets it right
    GameState.botScore += GameConfig.botBasePoints;

    // Update analysis panel
    updateAnalysisDisplay(isCorrect);

    // Show result modal
    showResultModal(isCorrect, points, responseTime);

    // Update score display
    updateScoreDisplay();
}

function nextRound() {
    // Hide result modal
    elements.resultModal?.classList.add('hidden');

    // Advance round
    GameState.currentRound++;

    // Load next round
    loadRound();
}

function endGame() {
    GameState.isGameOver = true;

    // Determine winner
    const playerWon = GameState.playerScore > GameState.botScore;
    const tied = GameState.playerScore === GameState.botScore;

    // Update game over modal
    if (elements.gameOverTitle) {
        if (playerWon) {
            elements.gameOverTitle.textContent = '🎉 You Win!';
        } else if (tied) {
            elements.gameOverTitle.textContent = "It's a Tie!";
        } else {
            elements.gameOverTitle.textContent = 'Game Over!';
        }
    }

    if (elements.gameOverMessage) {
        elements.gameOverMessage.textContent = playerWon
            ? 'Congratulations! You beat the bot!'
            : 'The bot was faster this time. Try again!';
    }

    if (elements.finalPlayerScore) {
        elements.finalPlayerScore.textContent = GameState.playerScore;
    }

    if (elements.finalBotScore) {
        elements.finalBotScore.textContent = GameState.botScore;
    }

    if (elements.finalAccuracy) {
        const accuracy = Math.round((GameState.correctAnswers / GameConfig.totalRounds) * 100);
        elements.finalAccuracy.textContent = `${accuracy}%`;
    }

    if (elements.finalBestStreak) {
        elements.finalBestStreak.textContent = GameState.bestStreak;
    }

    // Show modal
    elements.gameOverModal?.classList.remove('hidden');

    // Save stats
    saveGameStats();
}

function resetGame() {
    // Reset state
    GameState.currentRound = 1;
    GameState.sessionId = generateSessionId();
    GameState.playerScore = 0;
    GameState.botScore = 0;
    GameState.playerStreak = 0;
    GameState.bestStreak = 0;
    GameState.correctAnswers = 0;
    GameState.isGameOver = false;

    // Hide modals
    elements.gameOverModal?.classList.add('hidden');
    elements.resultModal?.classList.add('hidden');

    // Reinitialize
    initializeGame();
}

function endSession() {
    if (confirm('Are you sure you want to end this session? Progress will be lost.')) {
        window.location.href = 'dashboard.html';
    }
}

// =============================================================================
// UI UPDATES
// =============================================================================

function updateRoundDisplay() {
    if (elements.currentRound) {
        elements.currentRound.textContent = GameState.currentRound;
    }

    if (elements.progressFill) {
        const progress = (GameState.currentRound / GameConfig.totalRounds) * 100;
        elements.progressFill.style.width = `${progress}%`;
    }
}

function updateChallengeDisplay() {
    const challenge = GameState.currentChallenge;
    if (!challenge) return;

    if (elements.challengeUrl) {
        elements.challengeUrl.textContent = challenge.url;
    }

    if (elements.challengeMessage) {
        elements.challengeMessage.textContent = challenge.message;
    }

    if (elements.hintText) {
        elements.hintText.innerHTML = challenge.hint;
    }
}

function updateScoreDisplay() {
    if (elements.playerScore) {
        elements.playerScore.textContent = `${GameState.playerScore} pts`;
    }

    if (elements.botScore) {
        elements.botScore.textContent = `${GameState.botScore} pts`;
    }

    // Update bars
    const maxScore = Math.max(GameState.playerScore, GameState.botScore, 100);

    if (elements.playerBar) {
        const playerPercent = (GameState.playerScore / maxScore) * 100;
        elements.playerBar.style.width = `${playerPercent}%`;
    }

    if (elements.botBar) {
        const botPercent = (GameState.botScore / maxScore) * 100;
        elements.botBar.style.width = `${botPercent}%`;
    }

    if (elements.playerStreak) {
        elements.playerStreak.textContent = GameState.playerStreak;
    }

    if (elements.playerAccuracy) {
        const played = Math.max(1, GameState.currentRound - 1);
        const accuracy = Math.round((GameState.correctAnswers / played) * 100);
        elements.playerAccuracy.textContent = `${accuracy}%`;
    }
}

function updateAnalysisDisplay(isCorrect) {
    const challenge = GameState.currentChallenge;
    if (!challenge) return;

    if (elements.analysisRound) {
        elements.analysisRound.textContent = GameState.currentRound;
    }

    // Update result badge
    const resultIcon = elements.resultBadge?.querySelector('.result-icon');
    if (resultIcon) {
        resultIcon.className = `result-icon ${isCorrect ? 'success' : 'error'}`;
        resultIcon.innerHTML = `<span class="material-symbols-outlined">${isCorrect ? 'check' : 'close'}</span>`;
    }

    if (elements.resultTitle) {
        elements.resultTitle.textContent = isCorrect ? 'Correct Decision!' : 'Incorrect Decision';
    }

    if (elements.resultDesc) {
        elements.resultDesc.textContent = isCorrect
            ? `You correctly identified this as ${challenge.isPhishing ? 'phishing' : 'legitimate'}.`
            : `This was actually ${challenge.isPhishing ? 'a phishing attempt' : 'legitimate'}.`;
    }

    if (elements.botDecision) {
        elements.botDecision.textContent = challenge.isPhishing ? 'Phishing' : 'Safe';
    }

    if (elements.reasoningList) {
        elements.reasoningList.innerHTML = challenge.reasons.map(reason => `
            <li>
                <span class="material-symbols-outlined">${challenge.isPhishing ? 'warning' : 'verified'}</span>
                <span>${reason}</span>
            </li>
        `).join('');
    }

    if (elements.educationalNote) {
        elements.educationalNote.textContent = `"${challenge.educational}"`;
    }
}

function showResultModal(isCorrect, points, responseTime) {
    if (elements.modalIcon) {
        elements.modalIcon.className = `modal-icon ${isCorrect ? 'success' : 'error'}`;
        elements.modalIcon.innerHTML = `<span class="material-symbols-outlined">${isCorrect ? 'check_circle' : 'cancel'}</span>`;
    }

    if (elements.modalTitle) {
        elements.modalTitle.textContent = isCorrect ? 'Correct!' : 'Wrong!';
    }

    if (elements.modalMessage) {
        const challenge = GameState.currentChallenge;
        elements.modalMessage.textContent = isCorrect
            ? `You spotted it correctly as ${challenge.isPhishing ? 'phishing' : 'legitimate'}!`
            : `This was ${challenge.isPhishing ? 'a phishing attempt' : 'actually legitimate'}.`;
    }

    if (elements.modalPoints) {
        elements.modalPoints.textContent = points >= 0 ? `+${points}` : points;
        elements.modalPoints.style.color = points >= 0 ? '#10b981' : '#ef4444';
    }

    if (elements.modalTime) {
        elements.modalTime.textContent = `${responseTime.toFixed(1)}s`;
    }

    // Check if it's the last round
    if (elements.nextRoundBtn) {
        elements.nextRoundBtn.textContent = GameState.currentRound >= GameConfig.totalRounds
            ? 'See Results'
            : 'Next Round';
    }

    elements.resultModal?.classList.remove('hidden');
}

// =============================================================================
// HELPERS
// =============================================================================

function generateSessionId() {
    return Math.random().toString(36).substring(2, 6).toUpperCase();
}

function shuffleArray(array) {
    for (let i = array.length - 1; i > 0; i--) {
        const j = Math.floor(Math.random() * (i + 1));
        [array[i], array[j]] = [array[j], array[i]];
    }
    return array;
}

function saveGameStats() {
    try {
        const stats = JSON.parse(localStorage.getItem(GameConfig.storageKey) || '{}');
        stats.gamesPlayed = (stats.gamesPlayed || 0) + 1;
        stats.totalCorrect = (stats.totalCorrect || 0) + GameState.correctAnswers;
        stats.bestStreak = Math.max(stats.bestStreak || 0, GameState.bestStreak);
        stats.highScore = Math.max(stats.highScore || 0, GameState.playerScore);
        localStorage.setItem(GameConfig.storageKey, JSON.stringify(stats));
    } catch (e) {
        console.error('[Game] Failed to save stats:', e);
    }
}

function showToast(message, type = 'success') {
    if (!elements.toast || !elements.toastMessage) return;

    elements.toastMessage.textContent = message;
    elements.toast.classList.remove('hidden');
    elements.toast.classList.add('show');

    setTimeout(() => {
        elements.toast.classList.remove('show');
        setTimeout(() => elements.toast.classList.add('hidden'), 300);
    }, 3000);
}

// =============================================================================
// EXPORTS
// =============================================================================

if (typeof module !== 'undefined' && module.exports) {
    module.exports = {
        GameState,
        GameConfig,
        CHALLENGES,
        handleDecision,
        resetGame,
    };
}
