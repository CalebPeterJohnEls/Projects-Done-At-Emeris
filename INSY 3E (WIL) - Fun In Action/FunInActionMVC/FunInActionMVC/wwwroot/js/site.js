// Shared client-side behaviour, consolidated from the original prototype's
// per-page <script > blocks so every Razor view can rely on the same file.

function toggleSidenav() {
    const nav = document.getElementById('sidenav');
    const overlay = document.getElementById('nav-overlay');
    if (!nav || !overlay) return;

    if (window.innerWidth <= 768) {
        nav .classList.remove('min');
        nav .classList.toggle('mobile-open');
        overlay .classList.toggle('visible', nav.classList.contains('mobile-open'));
    }

    else {
        nav .classList.remove('mobile-open');
        nav .classList.toggle('min');
    }
}

function closeMobileNav() {
    const nav = document.getElementById('sidenav');
    const overlay = document.getElementById('nav-overlay');
    if (nav) nav.classList.remove('mobile-open');
    if (overlay) overlay.classList.remove('visible');
}

// ---------- Profile accordion (only one section open at a time) ----------
document.addEventListener('DOMContentLoaded', function () {
    document.querySelectorAll('.fia-acc-trigger').forEach(function (trigger) {
        trigger.addEventListener('click', function () {
            const item = trigger.closest('.fia-acc-item');
            const wasOpen = item.classList.contains('open');
            document.querySelectorAll('.fia-acc-item').forEach(function (el) {
                el.classList.remove('open');
            });
            if (!wasOpen) item.classList.add('open');
        });
    });

    // Re-open the accordion panel a validation error came back for.
    const firstInvalid = document.querySelector('.fia-acc-panel .input-validation-error, .fia-acc-panel .validation-summary-errors');
    if (firstInvalid) {
        const panel = firstInvalid.closest('.fia-acc-item');
        if (panel) panel.classList.add('open');
    }
});

// ---------- Card payment overlay (Payments pages) ----------
function openPaymentOverlay(label, amount) {
    const labelEl = document.getElementById('overlay-item-label');
    const amountEl = document.getElementById('overlay-item-amount');
    const label2El = document.getElementById('overlay-item-label-2');
    const amount2El = document.getElementById('overlay-item-amount-2');
    const hiddenLabel = document.getElementById('CardPayment_ItemLabel');
    const hiddenAmount = document.getElementById('CardPayment_ItemAmount');
    if (labelEl) labelEl.textContent = label;
    if (amountEl) amountEl.textContent = amount;
    if (label2El) label2El.textContent = label;
    if (amount2El) amount2El.textContent = amount;
    if (hiddenLabel) hiddenLabel.value = label;
    if (hiddenAmount) hiddenAmount.value = amount;
    const overlay = document.getElementById('payment-overlay');
    if (!overlay) return;
    overlay .classList.add('active');
    document .body.style.overflow = 'hidden';

    requestAnimationFrame(() => {
        requestAnimationFrame(() => {
            overlay.classList.add('flipped');
        });
    });
}

function closePaymentOverlay() {
    const overlay = document.getElementById('payment-overlay');
    if (!overlay) return;
    overlay .classList.remove('flipped');
    document .body.style.overflow = '';
    setTimeout(() => overlay.classList.remove('active'), 400);
}

document.addEventListener('keydown', (e) => {
    if (e.key === 'Escape') closePaymentOverlay();
});

// If the form re-rendered with validation errors, reopen the overlay so the
// user can see what needs fixing instead of it silently vanishing.
document.addEventListener('DOMContentLoaded', function () {
    const overlay = document.getElementById('payment-overlay');
    if (overlay && overlay.dataset.reopen === 'true') {
        const label = document.getElementById('overlay-item-label')?.textContent || '';
        const amount = document.getElementById('overlay-item-amount')?.textContent || '';
        openPaymentOverlay(label, amount);
    }
});

// ---------- About page gallery lightbox ----------
function openLightbox(el) {
    const img = el.querySelector('img');
    const lightbox = document.getElementById('fia-lightbox');
    const lightboxImg = document.getElementById('fia-lightbox-img');
    if (!img || !lightbox || !lightboxImg) return;
    lightboxImg .src = img.src;
    lightbox .classList.add('open');
}

function closeLightbox(event) {
    if (event) event.stopPropagation();
    const lightbox = document.getElementById('fia-lightbox');
    if (lightbox) lightbox.classList.remove('open');
}

// ---------- Clients page: client-side search filter over server-rendered rows ----------
document.addEventListener('DOMContentLoaded', function () {
    const searchEl = document.getElementById('search');
    const rowsEl = document.getElementById('client-rows');
    if (!searchEl || !rowsEl) return;

    searchEl.addEventListener('input', function () {
        const q = searchEl.value.trim().toLowerCase();
        const rows = rowsEl.querySelectorAll('tr[data-search]');
        let anyVisible = false;
        rows.forEach(function (row) {
            const match = !q || row.dataset.search.includes(q);
            row.style.display = match ? '' : 'none';
            if (match) anyVisible = true;
        });
        const emptyRow = rowsEl.querySelector('.fia-empty-row');
        if (emptyRow) emptyRow.style.display = anyVisible ? 'none' : '';
    });
});
