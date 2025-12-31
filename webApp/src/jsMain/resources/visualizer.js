/**
 * Mehr Guard Brain Visualizer
 * 
 * Canvas-based neural network visualization for the Beat The Bot game.
 * Matches the CommonBrainVisualizer.kt implementation in Android/Desktop.
 * 
 * Features:
 * - 80 node neural network grid
 * - Signal-to-cluster mapping (deterministic hashing)
 * - Pulsing red nodes when signals detected
 * - Ripple effect on active clusters
 * - Explanation badges below the visual
 * - Accessible text description
 * 
 * @author Mehr Guard Team
 * @version 1.0.0
 */

// =============================================================================
// CONFIGURATION
// =============================================================================

const VisualizerConfig = {
    nodeCount: 80,
    canvasHeight: 200,
    pulseSpeed: 2000, // ms per full cycle
    baseNodeRadius: 3,
    activeNodeRadius: 6,
    rippleMultiplier: 2.5,
    colors: {
        safe: '#3b82f6',        // Blue - matches Material primary
        danger: '#ef4444',      // Red - matches Material error
        inactive: 'rgba(107, 114, 128, 0.3)',
        connection: 'rgba(107, 114, 128, 0.1)',
        activeConnection: 'rgba(239, 68, 68, 0.3)',
    }
};

// =============================================================================
// BRAIN NODES
// =============================================================================

let brainNodes = [];
let animationFrameId = null;
let phaseOffset = 0;

/**
 * Generate brain nodes with stable positions (fixed seed).
 * Uses rejection sampling for circular distribution.
 */
function generateBrainNodes(count, seed = 12345) {
    const nodes = [];
    let rng = seed;

    // Simple seeded random for reproducibility
    const random = () => {
        rng = (rng * 1103515245 + 12345) & 0x7fffffff;
        return rng / 0x7fffffff;
    };

    for (let i = 0; i < count; i++) {
        let x, y;
        // Rejection sampling for circular distribution
        do {
            x = random() * 2 - 1;
            y = random() * 2 - 1;
        } while (x * x + y * y > 1);

        nodes.push({
            x: x,
            y: y,
            phaseOffset: random() * Math.PI * 2
        });
    }

    return nodes;
}

/**
 * Map signal strings to node cluster indices.
 * Deterministic: same signals always light same nodes.
 */
function mapSignalsToNodes(signals, nodeCount) {
    if (!signals || signals.length === 0) return new Set();

    const activeNodes = new Set();
    const seed = signals.join('').split('').reduce((a, c) => a + c.charCodeAt(0), 0);
    let rng = seed;

    const random = (min, max) => {
        rng = (rng * 1103515245 + 12345) & 0x7fffffff;
        return min + Math.floor((rng / 0x7fffffff) * (max - min));
    };

    signals.forEach(signal => {
        // Hash signal to center node
        const signalHash = signal.split('').reduce((a, c) => a + c.charCodeAt(0), 0);
        const centerIndex = Math.abs(signalHash) % nodeCount;
        activeNodes.add(centerIndex);

        // Add 8 nearby nodes
        for (let i = 0; i < 8; i++) {
            const neighbor = Math.max(0, Math.min(nodeCount - 1,
                centerIndex + random(-10, 10)));
            activeNodes.add(neighbor);
        }
    });

    return activeNodes;
}

// =============================================================================
// CANVAS RENDERING
// =============================================================================

/**
 * Render the brain visualization on a canvas element.
 * 
 * @param {HTMLCanvasElement} canvas - Target canvas
 * @param {string[]} signals - Active detected signals
 */
function renderBrainCanvas(canvas, signals) {
    const ctx = canvas.getContext('2d');
    const width = canvas.width;
    const height = canvas.height;
    const centerX = width / 2;
    const centerY = height / 2;

    // Initialize nodes if needed
    if (brainNodes.length === 0) {
        brainNodes = generateBrainNodes(VisualizerConfig.nodeCount);
    }

    const activeIndices = mapSignalsToNodes(signals || [], brainNodes.length);
    const hasSignals = signals && signals.length > 0;

    // Clear canvas
    ctx.clearRect(0, 0, width, height);

    // Calculate pulse phase
    const time = performance.now();
    const pulsePhase = (time % VisualizerConfig.pulseSpeed) /
        VisualizerConfig.pulseSpeed * Math.PI * 2;

    // Draw connections first (background layer)
    brainNodes.forEach((node, index) => {
        if (index % 3 === 0) {
            const neighborIndex = (index + 3) % brainNodes.length;
            const neighbor = brainNodes[neighborIndex];

            const isActive = activeIndices.has(index) || activeIndices.has(neighborIndex);
            const color = isActive && hasSignals
                ? VisualizerConfig.colors.activeConnection
                : VisualizerConfig.colors.connection;

            const x1 = centerX + node.x * width * 0.4;
            const y1 = centerY + node.y * height * 0.4;
            const x2 = centerX + neighbor.x * width * 0.4;
            const y2 = centerY + neighbor.y * height * 0.4;

            ctx.beginPath();
            ctx.moveTo(x1, y1);
            ctx.lineTo(x2, y2);
            ctx.strokeStyle = color;
            ctx.lineWidth = 1;
            ctx.stroke();
        }
    });

    // Draw nodes
    brainNodes.forEach((node, index) => {
        const isActive = activeIndices.has(index);

        // Subtle floating animation
        const floatOffset = Math.sin(pulsePhase + node.phaseOffset) * 5;

        const x = centerX + node.x * width * 0.4;
        const y = centerY + node.y * height * 0.4 + floatOffset;

        // Determine color
        let color;
        if (!hasSignals) {
            color = VisualizerConfig.colors.safe;
            ctx.globalAlpha = 0.6;
        } else if (isActive) {
            color = VisualizerConfig.colors.danger;
            ctx.globalAlpha = 1.0;
        } else {
            color = VisualizerConfig.colors.inactive;
            ctx.globalAlpha = 1.0;
        }

        // Calculate radius with pulse
        let radius = isActive
            ? VisualizerConfig.activeNodeRadius
            : VisualizerConfig.baseNodeRadius;

        if (isActive) {
            const pulseScale = 1 + 0.3 * Math.sin(pulsePhase * 2 + node.phaseOffset);
            radius *= pulseScale;
        }

        // Draw node
        ctx.beginPath();
        ctx.arc(x, y, radius, 0, Math.PI * 2);
        ctx.fillStyle = color;
        ctx.fill();

        // Draw ripple for active nodes
        if (isActive) {
            const rippleRadius = radius * VisualizerConfig.rippleMultiplier;
            ctx.beginPath();
            ctx.arc(x, y, rippleRadius, 0, Math.PI * 2);
            ctx.fillStyle = color;
            ctx.globalAlpha = 0.2;
            ctx.fill();
        }

        ctx.globalAlpha = 1.0;
    });
}

/**
 * Start the animation loop for the brain visualizer.
 * 
 * @param {HTMLCanvasElement} canvas - Target canvas
 * @param {function} getSignals - Function that returns current signals array
 */
function startBrainAnimation(canvas, getSignals) {
    if (animationFrameId) {
        cancelAnimationFrame(animationFrameId);
    }

    function animate() {
        renderBrainCanvas(canvas, getSignals());
        animationFrameId = requestAnimationFrame(animate);
    }

    animate();
}

/**
 * Stop the brain animation.
 */
function stopBrainAnimation() {
    if (animationFrameId) {
        cancelAnimationFrame(animationFrameId);
        animationFrameId = null;
    }
}

// =============================================================================
// BADGE RENDERING
// =============================================================================

/**
 * Create explanation badges for detected signals.
 * 
 * @param {HTMLElement} container - Container element for badges
 * @param {string[]} signals - Active detected signals
 */
function renderSignalBadges(container, signals) {
    if (!container) return;

    container.innerHTML = '';

    if (!signals || signals.length === 0) {
        container.style.display = 'none';
        return;
    }

    container.style.display = 'flex';

    signals.forEach(signal => {
        const badge = document.createElement('span');
        badge.className = 'signal-badge';
        badge.textContent = signal.replace(/_/g, ' ');
        container.appendChild(badge);
    });
}

/**
 * Update the accessible description element.
 * 
 * @param {HTMLElement} element - Description element
 * @param {string[]} signals - Active detected signals
 */
function updateAccessibleDescription(element, signals) {
    if (!element) return;

    if (!signals || signals.length === 0) {
        element.textContent = 'AI Neural Net: No threats detected. Brain pattern is calm and blue.';
    } else {
        element.textContent = `AI Neural Net: Active alert. Detected signals: ${signals.join(', ')}. Brain pattern is pulsing red.`;
    }
}

// =============================================================================
// SIGNAL MAPPING
// =============================================================================

/**
 * Convert challenge reasons to signal strings.
 * Maps Web's text-based reasons to the signal format used by KMP.
 * 
 * @param {string[]} reasons - Array of reason strings from challenge data
 * @returns {string[]} Array of normalized signal strings
 */
function reasonsToSignals(reasons) {
    if (!reasons || reasons.length === 0) return [];

    const signalMap = {
        'domain': 'SUSPICIOUS_DOMAIN',
        'subdomain': 'SUBDOMAIN_ABUSE',
        'hyphen': 'SUSPICIOUS_DOMAIN',
        'typosquatting': 'TYPOSQUATTING',
        'homograph': 'HOMOGRAPH_ATTACK',
        'http': 'INSECURE_PROTOCOL',
        'shortened': 'URL_SHORTENER',
        'shortened url': 'URL_SHORTENER',
        '.tk': 'RISKY_TLD',
        '.ml': 'RISKY_TLD',
        '.ga': 'RISKY_TLD',
        'tld': 'RISKY_TLD',
        'urgency': 'URGENCY_TACTIC',
        'brand': 'BRAND_IMPERSONATION',
        'certificate': 'CERTIFICATE_ISSUE',
        'age': 'NEW_DOMAIN',
        'recently registered': 'NEW_DOMAIN',
    };

    const signals = new Set();

    reasons.forEach(reason => {
        const lowerReason = reason.toLowerCase();

        // Check each keyword
        for (const [keyword, signal] of Object.entries(signalMap)) {
            if (lowerReason.includes(keyword)) {
                signals.add(signal);
            }
        }

        // If no match, create a generic signal from the reason
        if (signals.size === 0) {
            signals.add(reason.toUpperCase().replace(/[^A-Z0-9]+/g, '_').slice(0, 30));
        }
    });

    return Array.from(signals);
}

// =============================================================================
// EXPORTS
// =============================================================================

if (typeof window !== 'undefined') {
    window.BrainVisualizer = {
        render: renderBrainCanvas,
        start: startBrainAnimation,
        stop: stopBrainAnimation,
        renderBadges: renderSignalBadges,
        updateDescription: updateAccessibleDescription,
        reasonsToSignals: reasonsToSignals,
    };
}

if (typeof module !== 'undefined' && module.exports) {
    module.exports = {
        renderBrainCanvas,
        startBrainAnimation,
        stopBrainAnimation,
        renderSignalBadges,
        updateAccessibleDescription,
        reasonsToSignals,
    };
}
