/* ============================================
   FPT Asset Management - Main JavaScript
   ============================================ */

// Sidebar toggle
const sidebarToggle = document.getElementById('sidebarToggle');
const sidebar = document.getElementById('sidebar');
const mainContent = document.getElementById('mainContent');

if (sidebarToggle) {
    sidebarToggle.addEventListener('click', () => {
        if (window.innerWidth <= 768) {
            sidebar.classList.toggle('show');
        } else {
            sidebar.classList.toggle('collapsed');
            mainContent.classList.toggle('expanded');
        }
    });
}

// Close sidebar on overlay click (mobile)
// document.addEventListener('click', (e) => {
//     if (window.innerWidth <= 768 &&
//         sidebar && sidebar.classList.contains('show') &&
//         !sidebar.contains(e.target) &&
//         e.target !== sidebarToggle) {
//         sidebar.classList.remove('show');
//     }
// });

document.addEventListener('click', (e) => {
    if (window.innerWidth <= 768 &&
        sidebar && sidebar.classList.contains('show') &&
        !sidebar.contains(e.target) &&
        !sidebarToggle.contains(e.target)) { // Thay e.target !== sidebarToggle thành !sidebarToggle.contains(e.target)
        sidebar.classList.remove('show');
    }
});

// Auto-dismiss alerts after 4 seconds
document.querySelectorAll('.alert-dismissible').forEach(alert => {
    setTimeout(() => {
        const bsAlert = bootstrap.Alert.getOrCreateInstance(alert);
        if (bsAlert) bsAlert.close();
    }, 4000);
});

// QR Code scanner using camera (for mobile)
function startQRScanner(videoId, resultCallback) {
    if (!navigator.mediaDevices || !navigator.mediaDevices.getUserMedia) {
        alert('Trình duyệt không hỗ trợ camera');
        return;
    }
    navigator.mediaDevices.getUserMedia({ video: { facingMode: 'environment' } })
        .then(stream => {
            const video = document.getElementById(videoId);
            if (video) {
                video.srcObject = stream;
                video.play();
            }
        })
        .catch(err => console.error('Camera error:', err));
}

// Confirm delete
function confirmDelete(formId, message) {
    if (confirm(message || 'Bạn có chắc muốn xóa không?')) {
        document.getElementById(formId).submit();
    }
}

// Format currency VND
function formatVND(amount) {
    return new Intl.NumberFormat('vi-VN', { style: 'currency', currency: 'VND' }).format(amount);
}

// Copy QA code to clipboard
function copyToClipboard(text) {
    navigator.clipboard.writeText(text).then(() => {
        showToast('Đã sao chép: ' + text, 'success');
    });
}

// Toast notification
function showToast(message, type = 'info') {
    const toast = document.createElement('div');
    toast.className = `toast-notification toast-${type}`;
    toast.innerHTML = `<i class="bi bi-${type === 'success' ? 'check-circle' : 'info-circle'} me-2"></i>${message}`;
    toast.style.cssText = `
        position: fixed; bottom: 24px; right: 24px; z-index: 9999;
        background: ${type === 'success' ? '#065F46' : '#1E40AF'};
        color: white; padding: 12px 20px; border-radius: 10px;
        font-size: 14px; box-shadow: 0 8px 24px rgba(0,0,0,0.2);
        animation: slideIn 0.3s ease;
    `;
    document.body.appendChild(toast);
    setTimeout(() => toast.remove(), 3000);
}

// Print QR code
function printQR(qaCode, qrSrc) {
    const win = window.open('', '_blank');
    win.document.write(`
        <html><head><title>QR - ${qaCode}</title>
        <style>
            body { font-family: Arial; text-align: center; padding: 40px; }
            img { width: 250px; height: 250px; }
            .label { background: #FF6B00; color: white; padding: 8px 24px;
                     border-radius: 20px; font-size: 16px; font-weight: bold;
                     display: inline-block; margin-top: 12px; }
            .title { font-size: 14px; color: #666; margin-bottom: 8px; }
        </style></head>
        <body>
            <div class="title">FPT Polytechnic Đà Nẵng</div>
            <img src="data:image/png;base64,${qrSrc}" />
            <br><span class="label">${qaCode}</span>
        </body></html>
    `);
    win.document.close();
    win.print();
}

// Initialize tooltips
document.querySelectorAll('[data-bs-toggle="tooltip"]').forEach(el => {
    new bootstrap.Tooltip(el);
});

// Dashboard charts initialization (called from dashboard page)
window.initDashboardCharts = function(available, inUse, broken, maintenance, lost) {
    // Donut chart
    const ctx = document.getElementById('statusChart');
    if (!ctx) return;

    new Chart(ctx, {
        type: 'doughnut',
        data: {
            labels: ['Sẵn sàng', 'Đang dùng', 'Hỏng', 'Bảo trì', 'Thất lạc'],
            datasets: [{
                data: [available, inUse, broken, maintenance, lost],
                backgroundColor: ['#10B981', '#3B82F6', '#EF4444', '#F59E0B', '#64748B'],
                borderWidth: 0,
                hoverOffset: 6
            }]
        },
        options: {
            responsive: true,
            maintainAspectRatio: false,
            plugins: {
                legend: {
                    position: 'bottom',
                    labels: { padding: 16, font: { size: 12, family: 'Inter' } }
                }
            },
            cutout: '70%'
        }
    });
};

document.querySelectorAll('.notification-redirect-target').forEach(input => {
    input.value = window.location.pathname + window.location.search;
});
