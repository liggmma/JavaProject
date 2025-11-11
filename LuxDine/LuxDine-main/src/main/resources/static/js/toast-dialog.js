/**
 * Toast Notification System
 * Hiển thị thông báo với 4 loại: success, error, warning, info
 */

/**
 * Hiển thị toast notification
 * @param {string} message - Nội dung thông báo
 * @param {string} type - Loại: 'success', 'error', 'warning', 'info'
 * @param {number} duration - Thời gian hiển thị (ms), mặc định 3000
 */
function showToast(message, type = 'info', duration = 3000) {
  const container = document.getElementById('toast-container');
  if (!container) {
    console.error('Toast container not found');
    return;
  }

  const toast = document.createElement('div');
  toast.className = `toast ${type}`;

  const icons = {
    success: 'fa-check-circle',
    error: 'fa-times-circle',
    warning: 'fa-exclamation-triangle',
    info: 'fa-info-circle'
  };

  const titles = {
    success: 'Thành công',
    error: 'Lỗi',
    warning: 'Cảnh báo',
    info: 'Thông tin'
  };

  toast.innerHTML = `
    <div class="toast-icon">
      <i class="fa ${icons[type]}"></i>
    </div>
    <div class="toast-content">
      <div class="toast-title">${titles[type]}</div>
      <div class="toast-message">${message}</div>
    </div>
    <button class="toast-close" onclick="closeToast(this)">
      <i class="fa fa-times"></i>
    </button>
  `;

  container.appendChild(toast);

  // Auto close sau duration
  setTimeout(() => {
    closeToast(toast.querySelector('.toast-close'));
  }, duration);
}

/**
 * Đóng toast notification
 * @param {HTMLElement} button - Button close hoặc toast element
 */
function closeToast(button) {
  const toast = button.classList.contains('toast') 
    ? button 
    : button.closest('.toast');
  
  if (!toast) return;

  toast.classList.add('hiding');
  
  setTimeout(() => {
    toast.remove();
  }, 300);
}

/**
 * Confirmation Dialog System
 * Hiển thị dialog xác nhận trước khi thực hiện hành động nguy hiểm
 */

let confirmDialogCallback = null;

/**
 * Hiển thị confirmation dialog
 * @param {string} title - Tiêu đề dialog
 * @param {string} message - Nội dung thông báo
 * @param {function} onConfirm - Callback khi user confirm
 */
function showConfirmDialog(title, message, onConfirm) {
  const overlay = document.getElementById('confirm-dialog-overlay');
  if (!overlay) {
    console.error('Confirm dialog overlay not found');
    return;
  }

  confirmDialogCallback = onConfirm;

  const titleEl = overlay.querySelector('.confirm-dialog-title');
  const messageEl = overlay.querySelector('.confirm-dialog-message');

  if (titleEl) titleEl.textContent = title;
  if (messageEl) messageEl.textContent = message;

  overlay.classList.add('active');
  document.body.style.overflow = 'hidden';
}

/**
 * Đóng confirmation dialog
 */
function closeConfirmDialog() {
  const overlay = document.getElementById('confirm-dialog-overlay');
  if (!overlay) return;

  overlay.classList.add('closing');

  setTimeout(() => {
    overlay.classList.remove('active', 'closing');
    document.body.style.overflow = '';
    confirmDialogCallback = null;
  }, 300);
}

/**
 * Xử lý khi user confirm
 */
function handleConfirmDialog() {
  if (confirmDialogCallback && typeof confirmDialogCallback === 'function') {
    confirmDialogCallback();
  }
  closeConfirmDialog();
}

/**
 * Xử lý khi user cancel
 */
function handleCancelDialog() {
  closeConfirmDialog();
}

// Event listeners for confirmation dialog
document.addEventListener('DOMContentLoaded', () => {
  const overlay = document.getElementById('confirm-dialog-overlay');
  if (!overlay) return;

  // Cancel button
  const btnCancel = overlay.querySelector('.btn-confirm-cancel');
  if (btnCancel) {
    btnCancel.addEventListener('click', handleCancelDialog);
  }

  // Confirm button
  const btnConfirm = overlay.querySelector('.btn-confirm-delete');
  if (btnConfirm) {
    btnConfirm.addEventListener('click', handleConfirmDialog);
  }

  // Click outside to close
  overlay.addEventListener('click', (e) => {
    if (e.target === overlay) {
      handleCancelDialog();
    }
  });

  // ESC key to close
  document.addEventListener('keydown', (e) => {
    if (e.key === 'Escape' && overlay.classList.contains('active')) {
      handleCancelDialog();
    }
  });
});