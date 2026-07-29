// ==========================================================================
// WEARFASHION - APP CONTROLLER
// ==========================================================================

// Global Application State
let state = {
    clothes: [],
    persons: [],
    history: [],
    reports: [],
    favorites: [],
    userGeneratedImages: [],
    selectedPersonId: null,
    selectedClothingId: null,
    lastGeneratedImageId: null,
    activeTab: 'landing', // landing by default
    activeWardrobeTab: 'clothing',
    theme: 'dark',
    isAuthenticated: !!localStorage.getItem('authToken'),
    authToken: localStorage.getItem('authToken') || null,
    userId: null,
    // Simulated state in LocalStorage
    notifications: [],
    reviews: [],
    profile: { name: 'Sophia Carter', gender: 'Female', age: 24, email: 'you@domain.com' },
    // Verification flow state
    tempRegisterData: null,
    otpTimerInterval: null
};

// API Endpoint Mappings (Spring Boot Backend)
const API = {
    clothing: '/api/clothing',
    person: '/api/person',
    generate: '/api/tryon/generate',
    history: '/api/history',
    reports: '/api/reports',
    favorite: '/api/favorite',
    generatedImages: '/api/generated-images',
    login: '/api/auth/login',
    register: '/api/auth/register'
};

// Parse login token
if (state.authToken && state.authToken.startsWith('USER-')) {
    state.userId = Number(state.authToken.split('-')[1]);
}

// Image Source Normalization
function normalizeImageSrc(imagePath) {
    if (!imagePath || typeof imagePath !== 'string') return '';
    const stripped = imagePath.trim();
    if (!stripped) return '';
    if (/^(data:|https?:\/\/)/i.test(stripped)) return stripped;
    
    let normalized = stripped.replace(/\\/g, '/');
    
    const tryonIdx = normalized.indexOf('outputs/tryon/');
    if (tryonIdx !== -1) {
        return '/' + normalized.substring(tryonIdx);
    }
    
    const garmentIdx = normalized.indexOf('outputs/garment/');
    if (garmentIdx !== -1) {
        return '/' + normalized.substring(garmentIdx);
    }

    const uploadsIdx = normalized.indexOf('uploads/');
    if (uploadsIdx !== -1) {
        return '/' + normalized.substring(uploadsIdx);
    }
    
    return normalized.startsWith('/') ? normalized : `/${normalized}`;
}

// DOM Init
document.addEventListener('DOMContentLoaded', () => {
    // Theme Initializer
    const savedTheme = localStorage.getItem('theme') || 'dark';
    setTheme(savedTheme);

    // Initial Database Mocks
    initMockDatabase();

    // Stats Animation on scroll/load
    initLandingStatsAnimation();

    // Check Auth State
    if (state.isAuthenticated) {
        state.activeTab = 'dashboard';
        updateAuthView();
        fetchData();
    } else {
        showAuthShell('login');
    }

    setupDragAndDrop();
    initOtpInputNavigation();
});

// Mock Local Database Initializer
function initMockDatabase() {
    // Seed Reviews
    const savedReviews = localStorage.getItem('reviews');
    if (!savedReviews) {
        const seedReviews = [
            {
                id: 1,
                name: "Marcus K.",
                rating: 5,
                title: "Incredible design pipeline",
                content: "The mapping of clothes onto custom models is incredibly precise. Saves our brand weeks of production overhead.",
                date: "2026-07-15"
            },
            {
                id: 2,
                name: "Liam P.",
                rating: 4,
                title: "AI styling reports are very useful",
                content: "Perfect for testing color matching. The ratings and occasion suggestions are highly accurate.",
                date: "2026-07-16"
            }
        ];
        localStorage.setItem('reviews', JSON.stringify(seedReviews));
        state.reviews = seedReviews;
    } else {
        state.reviews = JSON.parse(savedReviews);
    }

    // Seed Notifications
    const savedNotifications = localStorage.getItem('notifications');
    if (!savedNotifications) {
        const seedNotifications = [
            {
                id: 1,
                title: "Welcome to WearFashion AI Studio!",
                message: "Configure your model persons and clothing garments in the Wardrobe tab, then use the Try-On Studio to synthesize styles.",
                time: "Just now",
                unread: true
            }
        ];
        localStorage.setItem('notifications', JSON.stringify(seedNotifications));
        state.notifications = seedNotifications;
    } else {
        state.notifications = JSON.parse(savedNotifications);
    }
    updateNotificationsBadge();

    // User Profile
    const savedProfile = localStorage.getItem('userProfile');
    if (savedProfile) {
        state.profile = JSON.parse(savedProfile);
    }
}

// Landing Page Stats Animation
function initLandingStatsAnimation() {
    const stats = document.querySelectorAll('.stat-number');
    stats.forEach(stat => {
        const target = parseFloat(stat.getAttribute('data-target'));
        const isDecimal = target % 1 !== 0;
        let current = 0;
        const duration = 1500; // ms
        const stepTime = 30;
        const steps = duration / stepTime;
        const increment = target / steps;
        
        let stepCount = 0;
        const timer = setInterval(() => {
            current += increment;
            stepCount++;
            if (stepCount >= steps) {
                clearInterval(timer);
                stat.innerText = isDecimal ? target.toFixed(1) + '%' : target + (stat.innerText.includes('M') ? 'M+' : 'K+');
            } else {
                stat.innerText = isDecimal ? current.toFixed(1) + '%' : Math.floor(current) + (stat.innerText.includes('M') ? 'M+' : 'K+');
            }
        }, stepTime);
    });
}

// FAQ toggle utility
function toggleFaq(element) {
    const item = element.parentElement;
    item.classList.toggle('open');
}

// Info Modal Management
function openModal(pageType) {
    const modal = document.getElementById('info-modal');
    const title = document.getElementById('modal-title');
    const content = document.getElementById('modal-content');
    if (!modal || !title || !content) return;

    modal.style.display = 'flex';

    if (pageType === 'about') {
        title.innerText = 'About WearFashion Studio';
        content.innerHTML = `
            <p><strong>WearFashion</strong> is a state-of-the-art AI virtual fitting platform. By utilizing deep learning models, we enable designers and shoppers to synthesize fashion designs onto diverse model persons instantly.</p>
            <p style="margin-top:12px;">Our mission is to reduce physical clothing waste and sample patterns while providing customers with a premium, digital wardrobe try-on experience.</p>
        `;
    } else if (pageType === 'contact') {
        title.innerText = 'Contact AI Fashion Support';
        content.innerHTML = `
            <p>For support, API integrations, and enterprise licensing queries, feel free to get in touch:</p>
            <ul style="margin-top:12px; list-style-position: inside;">
                <li>Email: <strong>krups0628@gmail.com</strong></li>
                <li>Phone: <strong>+1 (800) 555-FASHION</strong></li>
                <li>Address: <strong>AI Fashion Hub, San Francisco, CA</strong></li>
            </ul>
        `;
    } else if (pageType === 'privacy') {
        title.innerText = 'Privacy Policy';
        content.innerHTML = `
            <p>Your privacy is important to us. All garment and portrait uploads are stored securely and remain restricted to your user session ID.</p>
            <p style="margin-top:12px;">We do not share your virtual modeling assets or history templates with third parties. Data is hosted locally or within encrypted server environments.</p>
        `;
    } else {
        title.innerText = 'Terms of Service';
        content.innerHTML = `
            <p>By using WearFashion virtual try-on tools, you agree to upload only ownership-permitted clothing assets and portraits.</p>
            <p style="margin-top:12px;">Usage of synthesized outputs for commercial branding requires an enterprise modeling plan. Abuse of synthesis queues will result in temporary IP limitations.</p>
        `;
    }
}

function closeModal() {
    const modal = document.getElementById('info-modal');
    if (modal) modal.style.display = 'none';
}

// Authentication shell navigations
function showAuthShell(panel = 'login') {
    document.getElementById('landing-page').style.display = 'none';
    document.getElementById('auth-shell').style.display = 'grid';
    showAuthPanel(panel);
}

function backToLanding() {
    document.getElementById('auth-shell').style.display = 'none';
    document.getElementById('landing-page').style.display = 'block';
    state.activeTab = 'landing';
}

// Update authentication visibility shells
function updateAuthView() {
    const landingPage = document.getElementById('landing-page');
    const authShell = document.getElementById('auth-shell');
    const appShell = document.getElementById('app-shell');

    if (state.isAuthenticated) {
        if (landingPage) landingPage.style.display = 'none';
        if (authShell) authShell.style.display = 'none';
        if (appShell) appShell.style.display = 'grid';
        switchTab(state.activeTab === 'landing' ? 'dashboard' : state.activeTab);
    } else {
        if (appShell) appShell.style.display = 'none';
        if (state.activeTab === 'landing') {
            if (landingPage) landingPage.style.display = 'block';
            if (authShell) authShell.style.display = 'none';
        } else {
            if (landingPage) landingPage.style.display = 'none';
            if (authShell) authShell.style.display = 'grid';
        }
    }
}

// Show specific auth card
function showAuthPanel(panel) {
    const cards = ['login-card', 'register-card', 'forgot-card', 'verify-card'];
    cards.forEach(id => {
        const card = document.getElementById(id);
        if (card) card.style.display = 'none';
    });

    if (panel === 'register') {
        document.getElementById('register-card').style.display = 'block';
    } else if (panel === 'forgot') {
        document.getElementById('forgot-card').style.display = 'block';
    } else if (panel === 'verify') {
        document.getElementById('verify-card').style.display = 'block';
    } else {
        document.getElementById('login-card').style.display = 'block';
    }
}

// Password show/hide toggle
function togglePasswordVisibility(inputId, button) {
    const input = document.getElementById(inputId);
    if (!input) return;
    if (input.type === 'password') {
        input.type = 'text';
        button.innerHTML = '<i class="fa-regular fa-eye-slash"></i>';
    } else {
        input.type = 'password';
        button.innerHTML = '<i class="fa-regular fa-eye"></i>';
    }
}

// Password strength indicator
function checkPasswordStrength(input) {
    const val = input.value;
    const bar = document.getElementById('password-strength-bar');
    const text = document.getElementById('password-strength-text');
    if (!bar || !text) return;

    if (!val) {
        bar.className = 'pw-strength-bar';
        text.innerText = 'Password strength';
        return;
    }

    let score = 0;
    if (val.length >= 8) score++;
    if (/[A-Z]/.test(val)) score++;
    if (/[0-9]/.test(val)) score++;
    if (/[^A-Za-z0-9]/.test(val)) score++;

    bar.className = 'pw-strength-bar';
    if (score <= 1) {
        bar.classList.add('weak');
        text.innerText = 'Weak password (Include symbols/numbers)';
        text.style.color = 'var(--danger-color)';
    } else if (score === 2 || score === 3) {
        bar.classList.add('medium');
        text.innerText = 'Medium strength';
        text.style.color = 'var(--warning-color)';
    } else {
        bar.classList.add('strong');
        text.innerText = 'Strong password';
        text.style.color = 'var(--success-color)';
    }
}

// Handle login POST
async function handleLogin(event) {
    event.preventDefault();
    try {
        const email = document.getElementById('login-email').value.trim();
        const password = document.getElementById('login-password').value;

        const res = await fetch(API.login, {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify({ email, password })
        });

        if (!res.ok) throw new Error(await readErrorMessage(res));
        const data = await res.json();
        
        localStorage.setItem('authToken', data.token || 'authenticated');
        state.authToken = data.token || 'authenticated';
        state.isAuthenticated = true;

        if (state.authToken.startsWith('USER-')) {
            state.userId = Number(state.authToken.split('-')[1]);
        }

        // Save profile email
        state.profile.email = email;
        localStorage.setItem('userProfile', JSON.stringify(state.profile));

        updateAuthView();
        showToast(data.message || 'Login successful.', 'success');
        fetchData();
    } catch (err) {
        console.error(err);
        showToast(err.message || 'Login failed.', 'error');
    }
}

// Handle registration POST (followed by mock OTP page)
async function handleRegister(event) {
    event.preventDefault();
    try {
        const name = document.getElementById('register-name').value.trim();
        const email = document.getElementById('register-email').value.trim();
        const password = document.getElementById('register-password').value;
        const gender = document.getElementById('register-gender').value;
        const age = Number(document.getElementById('register-age').value);

        state.tempRegisterData = { name, email, password, gender, age };

        const res = await fetch(API.register, {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify({ name, email, password, gender, age })
        });

        if (!res.ok) throw new Error(await readErrorMessage(res));
        const data = await res.json();

        // Register success -> display Email Verification screen with simulated OTP
        showToast(data.message || 'Registration successful. Verification OTP sent.', 'success');
        
        // Populate display email
        document.getElementById('verify-email-display').innerText = email;
        
        // Show verification panel
        showAuthPanel('verify');
        
        // Launch countdown timer (5 mins)
        startOtpCountdown();
        
        // Inform user to check their email or terminal
        setTimeout(() => {
            showToast('Please check your registered email inbox or backend terminal log for the verification OTP.', 'info');
        }, 1000);

    } catch (err) {
        console.error(err);
        showToast(err.message || 'Registration failed.', 'error');
    }
}

// OTP countdown timer
function startOtpCountdown() {
    clearInterval(state.otpTimerInterval);
    const timerDisplay = document.getElementById('otp-timer');
    const resendBtn = document.getElementById('btn-resend-otp');
    if (!timerDisplay || !resendBtn) return;

    resendBtn.disabled = true;
    let timeRemaining = 300; // 5 mins in seconds

    state.otpTimerInterval = setInterval(() => {
        const minutes = Math.floor(timeRemaining / 60);
        const seconds = timeRemaining % 60;
        timerDisplay.innerText = `${minutes.toString().padStart(2, '0')}:${seconds.toString().padStart(2, '0')}`;

        if (timeRemaining <= 0) {
            clearInterval(state.otpTimerInterval);
            timerDisplay.innerText = "Expired";
            resendBtn.disabled = false;
        }
        timeRemaining--;
    }, 1000);
}

// Resend OTP trigger
async function resendOtpCode() {
    try {
        const email = state.tempRegisterData ? state.tempRegisterData.email : '';
        if (!email) {
            showToast('No registration email found. Please register again.', 'error');
            return;
        }

        const res = await fetch(`/api/auth/resend-otp?email=${encodeURIComponent(email)}`, {
            method: 'POST'
        });

        if (!res.ok) throw new Error(await readErrorMessage(res));

        showToast('A new OTP has been sent successfully.', 'success');
        startOtpCountdown();
        setTimeout(() => {
            showToast('Please check your email inbox or backend terminal log for the new OTP.', 'info');
        }, 1000);
    } catch (err) {
        console.error(err);
        showToast(err.message || 'Failed to resend OTP code.', 'error');
    }
}

// OTP code inputs utility
function initOtpInputNavigation() {
    const digits = document.querySelectorAll('.otp-digit');
    digits.forEach((input, index) => {
        input.addEventListener('input', (e) => {
            if (e.target.value.length === 1 && index < digits.length - 1) {
                digits[index + 1].focus();
            }
            updateOtpCompleteValue();
        });
        input.addEventListener('keydown', (e) => {
            if (e.key === 'Backspace' && e.target.value.length === 0 && index > 0) {
                digits[index - 1].focus();
            }
        });
    });
}

function updateOtpCompleteValue() {
    const digits = document.querySelectorAll('.otp-digit');
    let code = '';
    digits.forEach(input => code += input.value);
    document.getElementById('otp-complete-value').value = code;
}

// Verify OTP Form submit
async function handleOtpVerification(event) {
    event.preventDefault();
    const codeEntered = document.getElementById('otp-complete-value').value;
    if (codeEntered.length < 6) {
        showToast('Please enter all 6 digits.', 'error');
        return;
    }

    try {
        const email = state.tempRegisterData ? state.tempRegisterData.email : '';
        const res = await fetch('/api/auth/verify-otp', {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify({ email: email, otp: codeEntered })
        });

        if (!res.ok) throw new Error(await readErrorMessage(res));
        const data = await res.json();

        showToast(data.message || 'Email verified successfully! You can log in now.', 'success');
        
        // Save temp details into local profile
        if (state.tempRegisterData) {
            state.profile.name = state.tempRegisterData.name;
            state.profile.age = state.tempRegisterData.age;
            state.profile.gender = state.tempRegisterData.gender;
            state.profile.email = state.tempRegisterData.email;
            localStorage.setItem('userProfile', JSON.stringify(state.profile));
        }

        // Reset verify screen
        clearInterval(state.otpTimerInterval);
        document.getElementById('verify-form').reset();
        showAuthPanel('login');
    } catch (err) {
        console.error(err);
        showToast(err.message || 'Invalid OTP code. Please try again.', 'error');
    }
}

// Handle Forgot password submission
function handleForgotPassword(event) {
    event.preventDefault();
    const email = document.getElementById('forgot-email').value.trim();
    showToast(`Simulation: Password reset link sent to ${email}`, 'success');
    document.getElementById('forgot-form').reset();
    showAuthPanel('login');
}

// Logout session
function logout() {
    localStorage.removeItem('authToken');
    state.authToken = null;
    state.isAuthenticated = false;
    state.userId = null;
    state.activeTab = 'landing';
    updateAuthView();
}

async function readErrorMessage(response) {
    try {
        const data = await response.json();
        return data.message || data.detail || 'Request failed.';
    } catch (err) {
        return 'Request failed.';
    }
}

// Theme settings
function setTheme(theme) {
    state.theme = theme;
    document.documentElement.setAttribute('data-theme', theme);
    localStorage.setItem('theme', theme);
    const icon = document.getElementById('theme-icon');
    if (icon) {
        icon.className = theme === 'light' ? 'fa-solid fa-moon' : 'fa-solid fa-sun';
    }
}

function toggleTheme() {
    setTheme(state.theme === 'dark' ? 'light' : 'dark');
}

// Tab Switching & Sidebar Nav
function switchTab(tabId, wardrobeSubTab = null) {
    if (!state.isAuthenticated && tabId !== 'landing') {
        showAuthShell('login');
        return;
    }
    state.activeTab = tabId;

    // Update nav active tags
    document.querySelectorAll('.nav-btn').forEach(btn => btn.classList.remove('active'));
    const activeBtn = document.getElementById(`nav-${tabId}`);
    if (activeBtn) activeBtn.classList.add('active');

    // Update active tab contents
    document.querySelectorAll('.tab-content').forEach(section => section.classList.remove('active'));
    const activeSection = document.getElementById(`tab-${tabId}`);
    if (activeSection) activeSection.classList.add('active');

    // Subtab redirections
    if (tabId === 'wardrobe' && wardrobeSubTab) {
        switchWardrobeTab(wardrobeSubTab);
    }

    // Refresh specific page data
    if (tabId === 'dashboard') {
        renderDashboardStats();
    } else if (tabId === 'favorites') {
        fetchFavorites();
    } else if (tabId === 'history') {
        fetchHistory();
    } else if (tabId === 'reports') {
        fetchReports();
    } else if (tabId === 'reviews') {
        renderReviewsFeed();
    } else if (tabId === 'profile') {
        renderProfileFields();
    } else if (tabId === 'notifications') {
        renderNotificationsFeed();
    }
}

// Switch Wardrobe manager subtab
function switchWardrobeTab(subTab) {
    state.activeWardrobeTab = subTab;
    document.querySelectorAll('.wardrobe-tab-btn').forEach(btn => btn.classList.remove('active'));
    const activeBtn = document.getElementById(`wardrobe-tab-${subTab}-btn`);
    if (activeBtn) activeBtn.classList.add('active');

    document.querySelectorAll('.wardrobe-content').forEach(content => content.classList.remove('active'));
    const activeContent = document.getElementById(`wardrobe-${subTab}`);
    if (activeContent) activeContent.classList.add('active');
}

// Fetch all initial data
async function fetchData() {
    showToast('Loading AI Wardrobe Database...', 'info');
    try {
        await Promise.all([
            fetchClothes(),
            fetchPersons(),
            fetchHistory(),
            fetchReports(),
            fetchFavorites(),
            fetchUserGeneratedImages()
        ]);
        renderDashboardStats();
    } catch (err) {
        console.error('Error fetching data:', err);
        showToast('Error loading active wardrobe.', 'error');
    }
}

// API: Clothes
async function fetchClothes() {
    try {
        const res = await fetch(API.clothing);
        if (!res.ok) throw new Error();
        state.clothes = await res.json();

        // Auto-select logic
        if (state.clothes.length === 1) {
            state.selectedClothingId = state.clothes[0].id;
        } else if (state.clothes.length > 0 && !state.selectedClothingId) {
            state.selectedClothingId = state.clothes[state.clothes.length - 1].id;
        }

        // Update counts
        const badge = document.getElementById('clothing-count');
        if (badge) badge.innerText = `${state.clothes.length} available`;

        renderStudioClothingSelection();
        renderWardrobeClothingList();
        updateGenerateButtonState();
    } catch (err) {
        console.error(err);
        showToast('Failed to fetch clothes.', 'error');
    }
}

// API: Models
async function fetchPersons() {
    try {
        const res = await fetch(API.person);
        if (!res.ok) throw new Error();
        state.persons = await res.json();

        // Auto-select logic
        if (state.persons.length === 1) {
            state.selectedPersonId = state.persons[0].id;
        } else if (state.persons.length > 0 && !state.selectedPersonId) {
            state.selectedPersonId = state.persons[state.persons.length - 1].id;
        }

        // Update counts
        const badge = document.getElementById('person-count');
        if (badge) badge.innerText = `${state.persons.length} available`;

        renderStudioPersonSelection();
        renderWardrobePersonList();
        updateGenerateButtonState();
    } catch (err) {
        console.error(err);
        showToast('Failed to fetch models.', 'error');
    }
}

// API: History (using user-specific path if ID is parsed)
async function fetchHistory() {
    try {
        let res;
        if (state.userId) {
            res = await fetch(`${API.history}/user/${state.userId}`);
        } else {
            res = await fetch(API.history);
        }
        if (!res.ok) throw new Error();
        state.history = await res.json();
        renderHistoryTimeline();
    } catch (err) {
        console.error(err);
        showToast('Failed to fetch history.', 'error');
    }
}

// API: styling Reports
async function fetchReports() {
    try {
        const res = await fetch(API.reports);
        if (!res.ok) throw new Error();
        state.reports = await res.json();
        renderReportsGrid();
    } catch (err) {
        console.error(err);
        showToast('Failed to fetch AI reports.', 'error');
    }
}

// API: User Generated Images (for Favorites mapping metadata)
async function fetchUserGeneratedImages() {
    if (!state.userId) return;
    try {
        const res = await fetch(`${API.generatedImages}/user/${state.userId}`);
        if (!res.ok) throw new Error();
        state.userGeneratedImages = await res.json();
    } catch (err) {
        console.error('Failed to fetch user generated images:', err);
    }
}

// API: Favorites
async function fetchFavorites() {
    try {
        const res = await fetch(API.favorite);
        if (!res.ok) throw new Error();
        state.favorites = await res.json();
        renderFavoritesGrid();
    } catch (err) {
        console.error('Failed to fetch favorites:', err);
    }
}

// Render Studio selectors
function renderStudioPersonSelection() {
    const grid = document.getElementById('studio-person-grid');
    if (!grid) return;

    grid.innerHTML = `
        <div class="embedded-upload-card" onclick="switchTab('wardrobe', 'person')">
            <i class="fa-solid fa-circle-plus"></i>
            <span>Add Model</span>
        </div>
    `;

    state.persons.forEach(person => {
        const isSelected = state.selectedPersonId === person.id;
        const card = document.createElement('div');
        card.className = `selectable-card ${isSelected ? 'selected' : ''}`;
        card.onclick = () => selectPerson(person.id);
        
        card.innerHTML = `
            <img src="${normalizeImageSrc(person.imagePath)}" alt="${person.personName || 'Model'}">
            <div class="card-meta">${person.personName || 'Model'} (Age: ${person.age})</div>
            <div class="selected-overlay"><i class="fa-solid fa-check"></i></div>
        `;
        grid.appendChild(card);
    });
}

function renderStudioClothingSelection() {
    const grid = document.getElementById('studio-clothing-grid');
    if (!grid) return;

    grid.innerHTML = `
        <div class="embedded-upload-card" onclick="switchTab('wardrobe', 'clothing')">
            <i class="fa-solid fa-circle-plus"></i>
            <span>Add Clothes</span>
        </div>
    `;

    state.clothes.forEach(item => {
        const isSelected = state.selectedClothingId === item.id;
        const card = document.createElement('div');
        card.className = `selectable-card ${isSelected ? 'selected' : ''}`;
        card.onclick = () => selectClothing(item.id);
        
        card.innerHTML = `
            <img src="${normalizeImageSrc(item.imagePath)}" alt="${item.clothName}">
            <div class="card-meta">${item.clothName} (${item.color})</div>
            <div class="selected-overlay"><i class="fa-solid fa-check"></i></div>
        `;
        grid.appendChild(card);
    });
}

function selectPerson(id) {
    state.selectedPersonId = state.selectedPersonId === id ? null : id;
    renderStudioPersonSelection();
    updateGenerateButtonState();
}

function selectClothing(id) {
    state.selectedClothingId = state.selectedClothingId === id ? null : id;
    renderStudioClothingSelection();
    updateGenerateButtonState();
}

function updateGenerateButtonState() {
    const btn = document.getElementById('btn-generate');
    if (btn) {
        btn.disabled = !(state.selectedPersonId && state.selectedClothingId);
    }
}

// Synthesis trigger
async function triggerTryOn() {
    if (!state.selectedPersonId || !state.selectedClothingId) {
        showToast('Please select both a model and clothing garment.', 'error');
        return;
    }

    const loader = document.getElementById('generator-loader');
    const resultImg = document.getElementById('synthesized-result');
    const placeholder = document.getElementById('generator-placeholder');
    const generateBtn = document.getElementById('btn-generate');
    const postActions = document.getElementById('post-actions');
    const progressBar = document.getElementById('generator-progress');
    const progressPercent = document.getElementById('loader-percent');

    if (!loader || !resultImg || !placeholder || !postActions || !progressBar || !progressPercent || !generateBtn) {
        showToast('Generator UI components missing.', 'error');
        return;
    }

    const clothingItem = state.clothes.find(c => c.id === state.selectedClothingId);
    const personItem = state.persons.find(p => p.id === state.selectedPersonId);
    
    if (!clothingItem || !personItem) {
        showToast('Selected model or clothing item not found.', 'error');
        return;
    }

    loader.style.display = 'flex';
    resultImg.style.display = 'none';
    placeholder.style.display = 'none';
    postActions.style.display = 'none';
    generateBtn.disabled = true;

    let progressVal = 5;
    progressBar.style.width = '5%';
    progressPercent.innerText = '5%';

    const progressInterval = setInterval(() => {
        if (progressVal < 90) {
            progressVal += 1;
            progressBar.style.width = `${progressVal}%`;
            progressPercent.innerText = `${progressVal}%`;
        }
    }, 2800);

    try {
        // Fetch binary image blobs from URLs
        showToast('Preparing image resources...', 'info');
        const [personBlob, clothingBlob] = await Promise.all([
            fetch(normalizeImageSrc(personItem.imagePath)).then(r => {
                if (!r.ok) throw new Error('Failed to retrieve model image.');
                return r.blob();
            }),
            fetch(normalizeImageSrc(clothingItem.imagePath)).then(r => {
                if (!r.ok) throw new Error('Failed to retrieve clothing image.');
                return r.blob();
            })
        ]);

        const formData = new FormData();
        formData.append('userId', state.userId || 1); // Fallback to 1 if guest
        formData.append('personId', state.selectedPersonId);
        formData.append('clothingId', state.selectedClothingId);
        formData.append('person', personBlob, 'person.png');
        formData.append('fabric', clothingBlob, 'fabric.png');
        formData.append('garmentType', clothingItem.clothType || 'Shirt');

        const response = await fetch(API.generate, {
            method: 'POST',
            body: formData
        });

        if (!response.ok) throw new Error(await readErrorMessage(response));
        const startData = await response.json();
        const generatedId = startData.id || startData.generatedImageId;

        if (!generatedId) {
            throw new Error('Generation job was not created.');
        }

        const finalData = await pollGenerationStatus(generatedId, (pollProgress) => {
            if (pollProgress > progressVal) {
                progressVal = pollProgress;
                progressBar.style.width = `${progressVal}%`;
                progressPercent.innerText = `${progressVal}%`;
            }
        });

        clearInterval(progressInterval);
        progressBar.style.width = '100%';
        progressPercent.innerText = '100%';

        setTimeout(() => {
            loader.style.display = 'none';
            const imageSrc = finalData.outputImage || finalData.outputImageUrl || finalData.outputImagePath || '';
            resultImg.src = normalizeImageSrc(imageSrc);
            resultImg.style.display = 'block';
            postActions.style.display = 'flex';
            generateBtn.disabled = false;
            state.lastGeneratedImageId = finalData.id;
            
            // Add notification alert
            addNotificationAlert(
                "Synthesis Successful",
                `Styled outfit synthesis completed for image ID #${finalData.id}. View AI Reports or save to History.`
            );
            showToast('Outfit styling synthesis complete!', 'success');
        }, 300);
    } catch (err) {
        clearInterval(progressInterval);
        loader.style.display = 'none';
        placeholder.style.display = 'flex';
        generateBtn.disabled = false;
        console.error(err);
        showToast('Error during synthesis: ' + (err.message || 'Check AI REST API.'), 'error');
    }
}

async function pollGenerationStatus(generatedId, onProgress) {
    const maxAttempts = 240;
    const delayMs = 3000;

    for (let attempt = 0; attempt < maxAttempts; attempt++) {
        // Poll the status endpoint under /api/tryon/status/{id}
        const res = await fetch(`/api/tryon/status/${generatedId}`);
        if (!res.ok) throw new Error(await readErrorMessage(res));

        const data = await res.json();
        const status = (data.status || '').toUpperCase();

        if (onProgress) {
            const estimatedProgress = Math.min(95, 10 + Math.floor((attempt / maxAttempts) * 85));
            onProgress(estimatedProgress);
        }

        if (status === 'SUCCESS' || status === 'COMPLETED') {
            return data;
        }
        if (status === 'FAILED') {
            throw new Error(data.message || 'Generation failed on AI server.');
        }

        await new Promise(resolve => setTimeout(resolve, delayMs));
    }
    throw new Error('Generation timed out. Check python AI services.');
}

// Save synthesized image to favorites
async function likeSynthesizedImage() {
    if (!state.lastGeneratedImageId) {
        showToast('No generated image output to like.', 'error');
        return;
    }

    try {
        const res = await fetch(`${API.favorite}/${state.lastGeneratedImageId}`, {
            method: 'POST'
        });
        if (!res.ok) throw new Error();
        showToast('Style liked and saved to Favorites!', 'success');
        
        // Add notification alert
        addNotificationAlert("Added to Favorites", `Style image ID #${state.lastGeneratedImageId} was added to your Liked Styles.`);
        
        await fetchFavorites();
        await fetchUserGeneratedImages();
    } catch (err) {
        console.error(err);
        showToast('Failed to save to favorites.', 'error');
    }
}

// Remove style from favorites
async function removeFavorite(favId) {
    if (!confirm('Are you sure you want to remove this style from favorites?')) return;
    try {
        const res = await fetch(`${API.favorite}/${favId}`, {
            method: 'DELETE'
        });
        if (!res.ok) throw new Error();
        showToast('Style removed from favorites.', 'success');
        await fetchFavorites();
    } catch (err) {
        console.error(err);
        showToast('Failed to remove from favorites.', 'error');
    }
}

// Render Favorites Grid
function renderFavoritesGrid() {
    const grid = document.getElementById('favorites-grid');
    if (!grid) return;

    if (state.favorites.length === 0) {
        grid.innerHTML = `
            <div class="empty-placeholder">
                <i class="fa-solid fa-heart-crack"></i>
                <h3>No Favorites Saved</h3>
                <p>Generate some styles in the Studio and click "Like Style" to build your favorites.</p>
            </div>
        `;
        return;
    }

    grid.innerHTML = '';
    state.favorites.forEach(fav => {
        // Find matching generated image details to locate clothingId
        const genImg = state.userGeneratedImages.find(img => img.id === fav.generatedImageId) || 
                       state.history.find(hist => hist.generatedImageId === fav.generatedImageId);
        
        let clothName = 'Custom Styled Outfit';
        let category = 'Styled';
        let color = 'Accent';

        if (genImg && genImg.clothingId) {
            const clothingItem = state.clothes.find(c => c.id === genImg.clothingId);
            if (clothingItem) {
                clothName = clothingItem.clothName;
                category = clothingItem.clothType;
                color = clothingItem.color;
            }
        }

        const card = document.createElement('div');
        card.className = 'favorite-card';
        card.setAttribute('data-category', category);
        card.setAttribute('data-color', color);
        card.setAttribute('data-name', clothName);

        card.innerHTML = `
            <div class="favorite-img-wrap">
                <img src="${normalizeImageSrc(fav.generatedImagePath)}" alt="${clothName}">
                <span class="favorite-badge-type">${category}</span>
            </div>
            <div class="favorite-details">
                <h4>${clothName}</h4>
                <p><i class="fa-solid fa-palette"></i> Color Accent: ${color}</p>
            </div>
            <div class="favorite-card-footer">
                <span>Img: #${fav.generatedImageId}</span>
                <button class="btn-remove-favorite" onclick="removeFavorite(${fav.id})">
                    <i class="fa-solid fa-heart-circle-xmark"></i> Remove
                </button>
            </div>
        `;
        grid.appendChild(card);
    });
}

// Filter Favorites search
function filterFavorites() {
    const searchVal = document.getElementById('favorite-search-input').value.toLowerCase();
    const categoryFilter = document.getElementById('favorite-filter-category').value;
    const sortBy = document.getElementById('favorite-sort-by').value;

    const cards = Array.from(document.querySelectorAll('.favorite-card'));
    
    // Sort logic
    if (sortBy === 'Oldest') {
        cards.reverse(); // Reverse grid layouts since they render in insertion order
    }

    cards.forEach(card => {
        const name = card.getAttribute('data-name').toLowerCase();
        const category = card.getAttribute('data-category');
        const color = card.getAttribute('data-color').toLowerCase();
        
        const matchesSearch = name.includes(searchVal) || color.includes(searchVal) || category.toLowerCase().includes(searchVal);
        const matchesCategory = categoryFilter === 'All' || category === categoryFilter;

        if (matchesSearch && matchesCategory) {
            card.style.display = 'block';
        } else {
            card.style.display = 'none';
        }
    });
}

// Save Try-on to History Log
async function saveToHistoryLog() {
    if (!state.lastGeneratedImageId) {
        showToast('No generated image output to save.', 'error');
        return;
    }

    try {
        // Redundant POST omitted because Spring Boot automatically saves generations to the history table.
        // We simply refresh the log records.
        showToast('Saved to history successfully!', 'success');
        
        addNotificationAlert("Saved to History", `Style ID #${state.lastGeneratedImageId} was saved to your History Log.`);
        
        fetchHistory(); // refresh logs
    } catch (err) {
        console.error(err);
        showToast('Failed to save to history.', 'error');
    }
}

// Generate AI styling report
async function generateStylingReport() {
    if (!state.lastGeneratedImageId) {
        showToast('No generated image to analyze.', 'error');
        return;
    }

    showToast('Consulting Fashion AI analytics...', 'info');
    try {
        const res = await fetch(`${API.reports}/generate?generatedImageId=${state.lastGeneratedImageId}`, {
            method: 'POST'
        });

        if (!res.ok) throw new Error();
        showToast('Styling report created successfully!', 'success');
        
        addNotificationAlert("Report Generated", `New AI Styling Report created for image ID #${state.lastGeneratedImageId}.`);
        
        await fetchReports();
        switchTab('reports');
    } catch (err) {
        console.error(err);
        showToast('Failed to generate styling report.', 'error');
    }
}

// Render Reports Grid
function renderReportsGrid() {
    const grid = document.getElementById('reports-grid');
    if (!grid) return;

    if (state.reports.length === 0) {
        grid.innerHTML = `
            <div class="empty-placeholder">
                <i class="fa-solid fa-clipboard-question"></i>
                <h3>No Styling Reports Yet</h3>
                <p>Synthesize try-on outfits first, then click "Get AI Report" in the Studio tab to see reports.</p>
            </div>
        `;
        return;
    }

    grid.innerHTML = '';
    state.reports.forEach(report => {
        const card = document.createElement('div');
        card.className = 'report-card';
        
        let starsHtml = '';
        for (let i = 1; i <= 5; i++) {
            starsHtml += `<i class="fa-${i <= report.rating ? 'solid' : 'regular'} fa-star"></i>`;
        }

        card.innerHTML = `
            <div class="report-img-wrapper">
                <img src="${normalizeImageSrc(report.outputImageUrl)}" alt="Styled outfit">
                <div class="rating-stars">
                    ${starsHtml}
                </div>
            </div>
            <div class="report-details">
                <div class="report-chips">
                    <span class="chip chip-occasion"><i class="fa-solid fa-compass"></i> Occasion: ${report.occasion}</span>
                    <span class="chip chip-color"><i class="fa-solid fa-palette"></i> Accents: ${report.colorSuggestion}</span>
                </div>
                <p class="report-description">${report.recommendation}</p>
            </div>
            <div class="report-card-footer">
                <span>Report ID: #${report.id}</span>
                <button class="delete-report-btn" onclick="deleteReport(${report.id})">
                    <i class="fa-solid fa-trash-can"></i> Delete
                </button>
            </div>
        `;
        grid.appendChild(card);
    });
}

// Delete Report
async function deleteReport(id) {
    if (!confirm('Are you sure you want to delete this styling report?')) return;
    try {
        const res = await fetch(`${API.reports}/${id}`, {
            method: 'DELETE'
        });
        if (!res.ok) throw new Error();
        showToast('Report deleted successfully.', 'success');
        fetchReports();
    } catch (err) {
        console.error(err);
        showToast('Failed to delete report.', 'error');
    }
}

// Render History Timeline
function renderHistoryTimeline() {
    const timeline = document.getElementById('history-timeline');
    const clearBtn = document.getElementById('btn-clear-history');
    if (!timeline) return;

    if (state.history.length === 0) {
        timeline.innerHTML = `
            <div class="empty-placeholder">
                <i class="fa-solid fa-timeline"></i>
                <h3>No Logs Recorded</h3>
                <p>History log is currently empty. Synthesize try-ons and save them to build your portfolio.</p>
            </div>
        `;
        if (clearBtn) clearBtn.style.display = 'none';
        return;
    }

    if (clearBtn) clearBtn.style.display = 'flex';
    timeline.innerHTML = '';
    
    const sortedHistory = [...state.history].sort((a,b) => b.historyId - a.historyId);

    sortedHistory.forEach(item => {
        const dateObj = new Date(item.createdAt);
        const formattedDate = dateObj.toLocaleDateString('en-US', { 
            weekday: 'long', year: 'numeric', month: 'short', day: 'numeric', hour: '2-digit', minute: '2-digit' 
        });

        const historyRow = document.createElement('div');
        historyRow.className = 'history-item';
        
        historyRow.innerHTML = `
            <div class="history-pics">
                <div class="history-thumb output">
                    <img src="${normalizeImageSrc(item.outputImage)}" alt="Result">
                </div>
            </div>
            <div class="history-meta">
                <h4>Virtual Try-On Generation (ID: #${item.generatedImageId})</h4>
                <p><i class="fa-solid fa-user"></i> Styled by: <strong>${item.userName}</strong></p>
                <p><i class="fa-solid fa-calendar-day"></i> Timestamp: ${formattedDate}</p>
            </div>
            <div>
                <button class="btn-secondary clear-btn" onclick="deleteHistoryItem(${item.historyId})">
                    <i class="fa-solid fa-trash-can"></i> Delete Log
                </button>
            </div>
        `;
        timeline.appendChild(historyRow);
    });
}

// Delete History Item
async function deleteHistoryItem(id) {
    if (!confirm('Remove this record from your history?')) return;
    try {
        const res = await fetch(`${API.history}/${id}`, {
            method: 'DELETE'
        });
        if (!res.ok) throw new Error();
        showToast('History record removed.', 'success');
        fetchHistory();
    } catch (err) {
        console.error(err);
        showToast('Failed to remove history record.', 'error');
    }
}

// Clear All History
async function clearAllHistory() {
    if (!confirm('WARNING: This will permanently delete ALL saved try-on logs. Continue?')) return;
    try {
        const res = await fetch(`${API.history}/clear`, {
            method: 'DELETE'
        });
        if (!res.ok) throw new Error();
        showToast('All history logs cleared.', 'success');
        fetchHistory();
    } catch (err) {
        console.error(err);
        showToast('Failed to clear history.', 'error');
    }
}

// Render Wardrobe: Clothing
function renderWardrobeClothingList() {
    const grid = document.getElementById('manager-clothing-grid');
    if (!grid) return;

    if (state.clothes.length === 0) {
        grid.innerHTML = `
            <div class="empty-placeholder">
                <i class="fa-solid fa-box-open"></i>
                <p>No clothing garments uploaded yet.</p>
            </div>
        `;
        return;
    }

    grid.innerHTML = '';
    state.clothes.forEach(item => {
        const card = document.createElement('div');
        card.className = 'manager-card';
        card.innerHTML = `
            <button class="delete-card-btn" onclick="deleteClothingItem(${item.id})" title="Delete Item">
                <i class="fa-solid fa-xmark"></i>
            </button>
            <img src="${normalizeImageSrc(item.imagePath)}" alt="${item.clothName}">
            <div class="manager-card-details">
                <h5>${item.clothName}</h5>
                <p>${item.clothType} • ${item.color}</p>
            </div>
        `;
        grid.appendChild(card);
    });
}

// Delete Clothing Item
async function deleteClothingItem(id) {
    if (!confirm('Are you sure you want to delete this clothing item?')) return;
    try {
        const res = await fetch(`${API.clothing}/${id}`, {
            method: 'DELETE'
        });
        if (!res.ok) throw new Error();
        showToast('Clothing deleted successfully.', 'success');
        
        if (state.selectedClothingId === id) {
            state.selectedClothingId = null;
            updateGenerateButtonState();
        }
        
        fetchClothes();
    } catch (err) {
        console.error(err);
        showToast('Failed to delete clothing.', 'error');
    }
}

// Render Wardrobe: Model Persons
function renderWardrobePersonList() {
    const grid = document.getElementById('manager-person-grid');
    if (!grid) return;

    if (state.persons.length === 0) {
        grid.innerHTML = `
            <div class="empty-placeholder">
                <i class="fa-solid fa-users-slash"></i>
                <p>No model persons created yet.</p>
            </div>
        `;
        return;
    }

    grid.innerHTML = '';
    state.persons.forEach(person => {
        const card = document.createElement('div');
        card.className = 'manager-card';
        card.innerHTML = `
            <button class="delete-card-btn" onclick="deletePersonItem(${person.id})" title="Delete Person">
                <i class="fa-solid fa-xmark"></i>
            </button>
            <img src="${normalizeImageSrc(person.imagePath)}" alt="${person.personName || 'Model'}">
            <div class="manager-card-details">
                <h5>${person.personName || 'Model Person'}</h5>
                <p>${person.gender} • Age: ${person.age}</p>
            </div>
        `;
        grid.appendChild(card);
    });
}

// Delete Model Person
async function deletePersonItem(id) {
    if (!confirm('Are you sure you want to delete this model person?')) return;
    try {
        const res = await fetch(`${API.person}/${id}`, {
            method: 'DELETE'
        });
        if (!res.ok) throw new Error();
        showToast('Model person deleted.', 'success');
        
        if (state.selectedPersonId === id) {
            state.selectedPersonId = null;
            updateGenerateButtonState();
        }
        
        fetchPersons();
    } catch (err) {
        console.error(err);
        showToast('Failed to delete model.', 'error');
    }
}

// Preview uploaded images
function previewFile(input, previewId) {
    const file = input.files[0];
    const previewContainer = document.getElementById(previewId);
    if (!file || !previewContainer) return;

    const img = previewContainer.querySelector('img');
    if (!img) return;

    const reader = new FileReader();
    reader.onload = (e) => {
        img.src = e.target.result;
        previewContainer.style.display = 'block';
    };
    reader.readAsDataURL(file);
}

// Upload Clothes
async function handleClothingUpload(e) {
    e.preventDefault();

    const name = document.getElementById('clothing-name').value;
    const type = document.getElementById('clothing-type').value;
    const color = document.getElementById('clothing-color').value;
    const fileInput = document.getElementById('clothing-file');
    
    if (fileInput.files.length === 0) {
        showToast('Please select a clothing image.', 'error');
        return;
    }

    const formData = new FormData();
    formData.append('clothName', name);
    formData.append('clothType', type);
    formData.append('color', color);
    formData.append('image', fileInput.files[0]);

    showToast('Uploading clothing garment...', 'info');
    try {
        const res = await fetch(`${API.clothing}/upload`, {
            method: 'POST',
            body: formData
        });

        if (!res.ok) throw new Error();
        showToast('Clothing uploaded successfully!', 'success');
        
        addNotificationAlert("Clothing Uploaded", `New clothing item "${name}" was uploaded to your wardrobe.`);
        
        document.getElementById('form-upload-clothing').reset();
        document.getElementById('clothing-preview').style.display = 'none';
        fetchClothes();
    } catch (err) {
        console.error(err);
        showToast('Failed to upload clothing. Try again.', 'error');
    }
}

// Upload Model Portrait
async function handlePersonUpload(e) {
    e.preventDefault();

    const gender = document.getElementById('person-gender').value;
    const age = document.getElementById('person-age').value;
    const fileInput = document.getElementById('person-file');
    
    if (fileInput.files.length === 0) {
        showToast('Please select a portrait image.', 'error');
        return;
    }

    const personName = `Model ${gender} #${state.persons.length + 1}`;

    const formData = new FormData();
    formData.append('personName', personName);
    formData.append('gender', gender);
    formData.append('age', age);
    formData.append('image', fileInput.files[0]);

    showToast('Adding model person...', 'info');
    try {
        const res = await fetch(`${API.person}/upload`, {
            method: 'POST',
            body: formData
        });

        if (!res.ok) throw new Error();
        showToast('Model person added successfully!', 'success');
        
        addNotificationAlert("Person Created", `New model person "${personName}" was added successfully.`);
        
        document.getElementById('form-upload-person').reset();
        document.getElementById('person-preview').style.display = 'none';
        fetchPersons();
    } catch (err) {
        console.error(err);
        showToast('Failed to upload model. Try again.', 'error');
    }
}

// Drag & Drop
function setupDragAndDrop() {
    document.querySelectorAll('.dropzone').forEach(zone => {
        const input = zone.querySelector('input[type="file"]');
        if (!input) return;
        
        zone.addEventListener('dragover', (e) => {
            e.preventDefault();
            zone.style.borderColor = 'var(--accent-color)';
            zone.style.background = 'var(--accent-soft)';
        });

        zone.addEventListener('dragleave', () => {
            zone.style.borderColor = 'var(--card-border)';
            zone.style.background = 'transparent';
        });

        zone.addEventListener('drop', (e) => {
            e.preventDefault();
            zone.style.borderColor = 'var(--card-border)';
            zone.style.background = 'transparent';
            
            const files = e.dataTransfer?.files;
            if (files && files.length > 0) {
                if (typeof DataTransfer !== 'undefined') {
                    const dataTransfer = new DataTransfer();
                    Array.from(files).forEach(file => dataTransfer.items.add(file));
                    input.files = dataTransfer.files;
                } else {
                    input.files = files;
                }

                const event = new Event('change', { bubbles: true });
                input.dispatchEvent(event);
            }
        });
    });
}

// Stats Counter Rendering on Dashboard
function renderDashboardStats() {
    const modelsCount = document.getElementById('dash-models-count');
    const clothesCount = document.getElementById('dash-clothes-count');
    const historyCount = document.getElementById('dash-history-count');
    const favoritesCount = document.getElementById('dash-favorites-count');
    const nameSpan = document.getElementById('dashboard-user-name');

    if (nameSpan) nameSpan.innerText = state.profile.name;

    // Trigger incremental count rendering
    animateStatsValue(modelsCount, state.persons.length);
    animateStatsValue(clothesCount, state.clothes.length);
    animateStatsValue(historyCount, state.history.length);
    animateStatsValue(favoritesCount, state.favorites.length);

    // Recent activity list populate
    const actList = document.getElementById('dashboard-activity-list');
    if (actList) {
        if (state.history.length === 0) {
            actList.innerHTML = `<div class="activity-empty-message">No recent generations. Start styling outfits!</div>`;
            return;
        }

        actList.innerHTML = '';
        const limitHistory = [...state.history].sort((a,b) => b.historyId - a.historyId).slice(0, 3);
        limitHistory.forEach(item => {
            const dateObj = new Date(item.createdAt);
            const timeStr = dateObj.toLocaleTimeString('en-US', { hour: '2-digit', minute: '2-digit' });

            const actItem = document.createElement('div');
            actItem.className = 'dashboard-activity-item';
            actItem.innerHTML = `
                <img src="${normalizeImageSrc(item.outputImage)}" class="dash-act-pic" alt="Thumb">
                <div class="dash-act-details">
                    <strong>Try-On Synthesized</strong>
                    <span>Generated Outfit ID #${item.generatedImageId}</span>
                </div>
                <div class="dash-act-time">${timeStr}</div>
            `;
            actList.appendChild(actItem);
        });
    }
}

function animateStatsValue(element, targetValue) {
    if (!element) return;
    let startVal = 0;
    const duration = 800; // ms
    const stepTime = 20;
    const steps = duration / stepTime;
    const increment = targetValue / steps;

    if (targetValue === 0) {
        element.innerText = "0";
        return;
    }

    let step = 0;
    const timer = setInterval(() => {
        startVal += increment;
        step++;
        if (step >= steps) {
            clearInterval(timer);
            element.innerText = targetValue;
        } else {
            element.innerText = Math.floor(startVal);
        }
    }, stepTime);
}

// SIMULATED: Reviews
function renderReviewsFeed() {
    const container = document.getElementById('reviews-feed-container');
    if (!container) return;

    if (state.reviews.length === 0) {
        container.innerHTML = `<div class="activity-empty-message">No reviews posted yet. Be the first!</div>`;
        return;
    }

    container.innerHTML = '';
    // Sort reviews newest first
    const sortedReviews = [...state.reviews].sort((a,b) => b.id - a.id);
    sortedReviews.forEach(rev => {
        let starsHtml = '';
        for (let i = 1; i <= 5; i++) {
            starsHtml += `<i class="fa-${i <= rev.rating ? 'solid' : 'regular'} fa-star"></i>`;
        }

        const reviewItem = document.createElement('div');
        reviewItem.className = 'review-item';
        reviewItem.innerHTML = `
            <div class="review-item-header">
                <div class="reviewer-profile">
                    <div class="reviewer-avatar">${rev.name.charAt(0).toUpperCase()}</div>
                    <strong>${rev.name}</strong>
                </div>
                <div class="review-item-stars">${starsHtml}</div>
            </div>
            <div class="review-item-content">
                <h4>${rev.title}</h4>
                <p>${rev.content}</p>
            </div>
            <div class="review-item-date">${rev.date}</div>
        `;
        container.appendChild(reviewItem);
    });
}

function setReviewRating(rating) {
    document.getElementById('review-rating-value').value = rating;
    const stars = document.querySelectorAll('.review-star-input');
    stars.forEach((star, index) => {
        if (index < rating) {
            star.className = 'fa-solid fa-star review-star-input active';
        } else {
            star.className = 'fa-regular fa-star review-star-input';
        }
    });
}

function handleReviewSubmit(e) {
    e.preventDefault();
    const rating = Number(document.getElementById('review-rating-value').value);
    const title = document.getElementById('review-title').value.trim();
    const content = document.getElementById('review-content').value.trim();

    if (rating === 0) {
        showToast('Please select a star rating.', 'error');
        return;
    }

    const newReview = {
        id: state.reviews.length + 1,
        name: state.profile.name,
        rating,
        title,
        content,
        date: new Date().toISOString().split('T')[0]
    };

    state.reviews.push(newReview);
    localStorage.setItem('reviews', JSON.stringify(state.reviews));
    
    // Add Notification
    addNotificationAlert("Review Submitted", `You reviewed the platform: "${title}" (${rating} Stars)`);

    showToast('Thank you for your styling feedback!', 'success');
    document.getElementById('form-submit-review').reset();
    setReviewRating(0);
    renderReviewsFeed();
}

// SIMULATED: Notifications
function updateNotificationsBadge() {
    const badge = document.getElementById('notification-badge');
    if (!badge) return;

    const unreadCount = state.notifications.filter(n => n.unread).length;
    if (unreadCount > 0) {
        badge.innerText = unreadCount;
        badge.style.display = 'grid';
    } else {
        badge.style.display = 'none';
    }
}

function addNotificationAlert(title, message) {
    const newAlert = {
        id: state.notifications.length + 1,
        title,
        message,
        time: 'Just now',
        unread: true
    };
    state.notifications.push(newAlert);
    localStorage.setItem('notifications', JSON.stringify(state.notifications));
    updateNotificationsBadge();
}

function renderNotificationsFeed() {
    const container = document.getElementById('notifications-container');
    if (!container) return;

    if (state.notifications.length === 0) {
        container.innerHTML = `<div class="empty-placeholder"><i class="fa-solid fa-bell-slash"></i><p>No notifications center alerts.</p></div>`;
        return;
    }

    container.innerHTML = '';
    // Sort reverse
    const sorted = [...state.notifications].sort((a,b) => b.id - a.id);
    sorted.forEach(notif => {
        const item = document.createElement('div');
        item.className = `notification-alert ${notif.unread ? 'unread' : ''}`;
        
        // Mark as read when opened
        if (notif.unread) {
            setTimeout(() => {
                notif.unread = false;
                localStorage.setItem('notifications', JSON.stringify(state.notifications));
                updateNotificationsBadge();
            }, 3000);
        }

        item.innerHTML = `
            <div class="notify-icon"><i class="fa-solid fa-sparkles"></i></div>
            <div class="notify-text">
                <p><strong>${notif.title}</strong>: ${notif.message}</p>
                <span>${notif.time}</span>
            </div>
            <button class="btn-delete-notify" onclick="deleteNotification(${notif.id})" title="Dismiss"><i class="fa-solid fa-xmark"></i></button>
        `;
        container.appendChild(item);
    });
}

function deleteNotification(id) {
    state.notifications = state.notifications.filter(n => n.id !== id);
    localStorage.setItem('notifications', JSON.stringify(state.notifications));
    updateNotificationsBadge();
    renderNotificationsFeed();
}

function clearAllNotifications() {
    state.notifications = [];
    localStorage.setItem('notifications', JSON.stringify([]));
    updateNotificationsBadge();
    renderNotificationsFeed();
}

// PROFILE MANAGEMENT
function renderProfileFields() {
    document.getElementById('profile-name').value = state.profile.name;
    document.getElementById('profile-gender').value = state.profile.gender;
    document.getElementById('profile-age').value = state.profile.age;

    // Info card populate
    document.getElementById('profile-name-title').innerText = state.profile.name;
    document.getElementById('profile-email-lbl').innerText = state.profile.email;
    document.getElementById('profile-avatar-circle').innerText = state.profile.name.charAt(0).toUpperCase();

    document.getElementById('profile-stat-models').innerText = state.persons.length;
    document.getElementById('profile-stat-clothes').innerText = state.clothes.length;
    document.getElementById('profile-stat-favorites').innerText = state.favorites.length;
}

function handleProfileUpdate(e) {
    e.preventDefault();
    const name = document.getElementById('profile-name').value.trim();
    const gender = document.getElementById('profile-gender').value;
    const age = Number(document.getElementById('profile-age').value);

    state.profile.name = name;
    state.profile.gender = gender;
    state.profile.age = age;

    localStorage.setItem('userProfile', JSON.stringify(state.profile));
    showToast('Profile details updated successfully!', 'success');
    
    addNotificationAlert("Profile Updated", "You saved new profile parameters.");
    
    renderProfileFields();
}

function handlePasswordUpdate(e) {
    e.preventDefault();
    const oldPw = document.getElementById('profile-old-password').value;
    const newPw = document.getElementById('profile-new-password').value;

    if (newPw.length < 8) {
        showToast('New password must be at least 8 characters long.', 'error');
        return;
    }

    // Simulate password changes
    showToast('Password updated successfully (Simulation).', 'success');
    addNotificationAlert("Security alert", "Your account password was updated.");
    document.getElementById('form-update-password').reset();
}

// Toast Notification
function showToast(message, type = 'success') {
    const container = document.getElementById('toast-container');
    if (!container) return;

    const toast = document.createElement('div');
    toast.className = `toast ${type}`;
    
    let iconClass = 'fa-circle-check';
    if (type === 'error') iconClass = 'fa-circle-exclamation';
    if (type === 'info') iconClass = 'fa-circle-info';

    toast.innerHTML = `
        <i class="fa-solid ${iconClass}"></i>
        <span>${message}</span>
    `;

    container.appendChild(toast);

    setTimeout(() => {
        toast.style.animation = 'slideIn 0.3s ease reverse forwards';
        setTimeout(() => {
            toast.remove();
        }, 300);
    }, 4500);
}

// Smooth scroll utility for Landing page
function scrollToSection(id) {
    const section = document.getElementById(id);
    if (section) {
        section.scrollIntoView({ behavior: 'smooth' });
    }
}
