/**
 * Category Management
 * Quản lý CRUD operations cho categories
 */

/**
 * Mở popup quản lý categories
 */
function openCategoryPopup() {
  const popup = document.getElementById('category-popup');
  popup.classList.add('active');
  document.body.style.overflow = 'hidden';
  loadCategoriesForManagement();
}

/**
 * Đóng popup quản lý categories
 */
function closeCategoryPopup() {
  const popup = document.getElementById('category-popup');
  popup.classList.remove('active');
  document.body.style.overflow = '';
}

/**
 * Mở form thêm/sửa category
 */
function openCategoryForm(categoryId = null) {
  const modal = document.getElementById('category-form-modal');
  const titleEl = document.getElementById('category-form-title');
  const categoryIdInput = document.getElementById('category-id');
  const categoryNameInput = document.getElementById('category-name');

  if (categoryId) {
    // Edit mode
    titleEl.innerHTML = '<i class="fa fa-pencil"></i> Chỉnh sửa danh mục';
    categoryIdInput.value = categoryId;
    
    // Load category data
    fetch(`/api/admin/categories/${categoryId}`)
      .then(res => res.json())
      .then(data => {
        if (data.success) {
          categoryNameInput.value = data.data.name;
        }
      })
      .catch(err => {
        console.error('Lỗi tải danh mục:', err);
        showToast('Không thể tải thông tin danh mục', 'error');
      });
  } else {
    // Create mode
    titleEl.innerHTML = '<i class="fa fa-plus"></i> Thêm danh mục mới';
    categoryIdInput.value = '';
    categoryNameInput.value = '';
  }

  modal.classList.add('active');
  document.body.style.overflow = 'hidden';
}

/**
 * Đóng form category
 */
function closeCategoryForm() {
  const modal = document.getElementById('category-form-modal');
  modal.classList.remove('active');
  document.getElementById('category-form').reset();
  document.getElementById('category-id').value = '';
  document.body.style.overflow = '';
}

/**
 * Load danh sách categories cho popup quản lý
 */
async function loadCategoriesForManagement() {
  try {
    const res = await fetch('/api/admin/categories');
    if (!res.ok) throw new Error('Tải danh mục thất bại');

    const data = await res.json();
    if (!data.success) throw new Error('Tải danh mục thất bại');

    displayCategories(data.data);
    document.getElementById('category-count').textContent = data.data.length;

  } catch (error) {
    console.error('Lỗi tải danh mục:', error);
    showToast('Không thể tải danh sách danh mục', 'error');
    document.getElementById('category-grid').innerHTML = 
      '<p style="text-align:center; color:#6B7280; padding:40px;">Tải danh mục thất bại</p>';
  }
}

/**
 * Hiển thị danh sách categories dạng card
 */
function displayCategories(categories) {
  const grid = document.getElementById('category-grid');
  
  if (categories.length === 0) {
    grid.innerHTML = '<p style="text-align:center; color:#6B7280; padding:40px; grid-column: 1/-1;">Chưa có danh mục nào</p>';
    return;
  }

  grid.innerHTML = '';
  
  categories.forEach(cat => {
    const itemCount = cat.itemCount || 0;
    const hasItems = itemCount > 0;
    const countClass = hasItems ? 'has-items' : '';
    
    const card = document.createElement('div');
    card.className = 'category-card';
    card.innerHTML = `
      <div class="category-card-header">
        <h4 class="category-card-title">${cat.name}</h4>
        <span class="category-card-count ${countClass}">
          ${itemCount} món
        </span>
      </div>
      <div class="category-card-actions">
        <button type="button" class="btn-category-edit" onclick="openCategoryForm(${cat.id})">
          <i class="fa fa-pencil"></i> Sửa
        </button>
        <button type="button" class="btn-category-delete" 
                ${hasItems ? 'disabled' : ''} 
                onclick="deleteCategoryWithConfirm(${cat.id}, '${cat.name}', ${itemCount})">
          <i class="fa fa-trash"></i> Xóa
        </button>
      </div>
    `;
    
    grid.appendChild(card);
  });
}

/**
 * Xác nhận và xóa category
 */
function deleteCategoryWithConfirm(categoryId, categoryName, itemCount) {
  if (itemCount > 0) {
    showToast(
      `Không thể xóa danh mục "${categoryName}" vì đang có ${itemCount} món ăn`, 
      'warning'
    );
    return;
  }

  showConfirmDialog(
    'Xác nhận xóa danh mục',
    `Bạn có chắc chắn muốn xóa danh mục "${categoryName}"? Hành động này không thể hoàn tác.`,
    async () => {
      try {
        const res = await fetch(`/api/admin/categories/${categoryId}`, {
          method: 'DELETE'
        });

        const result = await res.json();

        if (!res.ok || !result.success) {
          showToast(result.message || 'Không thể xóa danh mục', 'error');
          return;
        }

        showToast('Xóa danh mục thành công!', 'success');
        
        // Reload tất cả categories
        await loadAllCategoriesForDisplay();
        await loadCategoriesForManagement();
        await loadCategories();
        await loadCategoryFilter();
        
        // Re-render menu items
        displayMenuItems(allMenuItems);

      } catch (error) {
        console.error('Lỗi xóa danh mục:', error);
        showToast('Đã xảy ra lỗi khi xóa danh mục', 'error');
      }
    }
  );
}

/**
 * Submit form category (Create/Update)
 */
async function handleCategoryFormSubmit(event) {
  event.preventDefault();

  const categoryId = document.getElementById('category-id').value;
  const name = document.getElementById('category-name').value.trim();

  if (!name) {
    showToast('Vui lòng nhập tên danh mục', 'warning');
    return;
  }

  const categoryData = { name };

  try {
    let res, result;

    if (categoryId) {
      // Update
      res = await fetch(`/api/admin/categories/${categoryId}`, {
        method: 'PUT',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify(categoryData)
      });
    } else {
      // Create
      res = await fetch('/api/admin/categories', {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify(categoryData)
      });
    }

    result = await res.json();

    if (!res.ok || !result.success) {
      showToast(result.message || 'Không thể lưu danh mục', 'error');
      return;
    }

    showToast(
      categoryId ? 'Cập nhật danh mục thành công!' : 'Tạo danh mục thành công!', 
      'success'
    );

    closeCategoryForm();
    
    // Reload tất cả categories
    await loadAllCategoriesForDisplay();
    await loadCategoriesForManagement();
    await loadCategories();
    await loadCategoryFilter();
    
    // Re-render menu items để hiển thị category mới
    displayMenuItems(allMenuItems);

  } catch (error) {
    console.error('Lỗi lưu danh mục:', error);
    showToast('Đã xảy ra lỗi: ' + error.message, 'error');
  }
}

// Event listeners for category management
document.addEventListener('DOMContentLoaded', () => {
  // Nút "Quản lý danh mục"
  const btnManageCategories = document.getElementById('btn-manage-categories');
  if (btnManageCategories) {
    btnManageCategories.addEventListener('click', openCategoryPopup);
  }

  // Nút đóng popup categories
  const btnCloseCategoryPopup = document.getElementById('btn-close-category-popup');
  if (btnCloseCategoryPopup) {
    btnCloseCategoryPopup.addEventListener('click', closeCategoryPopup);
  }

  // Đóng popup khi click outside
  const categoryPopup = document.getElementById('category-popup');
  if (categoryPopup) {
    categoryPopup.addEventListener('click', (e) => {
      if (e.target === categoryPopup) {
        closeCategoryPopup();
      }
    });
  }

  // Nút "Thêm danh mục"
  const btnAddNewCategory = document.getElementById('btn-add-new-category');
  if (btnAddNewCategory) {
    btnAddNewCategory.addEventListener('click', () => openCategoryForm(null));
  }

  // Nút đóng form category
  const btnCloseCategoryForm = document.getElementById('btn-close-category-form');
  if (btnCloseCategoryForm) {
    btnCloseCategoryForm.addEventListener('click', closeCategoryForm);
  }

  const btnCancelCategoryForm = document.getElementById('btn-cancel-category-form');
  if (btnCancelCategoryForm) {
    btnCancelCategoryForm.addEventListener('click', closeCategoryForm);
  }

  // Đóng form khi click outside
  const categoryFormModal = document.getElementById('category-form-modal');
  if (categoryFormModal) {
    categoryFormModal.addEventListener('click', (e) => {
      if (e.target === categoryFormModal) {
        closeCategoryForm();
      }
    });
  }

  // Submit form category
  const categoryForm = document.getElementById('category-form');
  if (categoryForm) {
    categoryForm.addEventListener('submit', handleCategoryFormSubmit);
  }

  // ESC key to close popups
  document.addEventListener('keydown', (e) => {
    if (e.key === 'Escape') {
      if (categoryFormModal && categoryFormModal.classList.contains('active')) {
        closeCategoryForm();
      } else if (categoryPopup && categoryPopup.classList.contains('active')) {
        closeCategoryPopup();
      }
    }
  });
});