/**
 * Menu Items Management
 * Quản lý CRUD operations cho menu items
 */

// Global variables
let allMenuItems = [];
let allCategories = [];
const expandedItems = new Set();

/**
 * Load tất cả menu items từ API
 */
async function loadMenuItems() {
  try {
    const res = await fetch('/api/admin/menu');
    const data = await res.json();

    if (!data.success) throw new Error("Tải mục menu thất bại");

    allMenuItems = data.data;
    
    await loadAllCategoriesForDisplay();
    
    displayMenuItems(allMenuItems);
    loadCategoryFilter();

  } catch (error) {
    console.error("Lỗi tải mục menu:", error);
    document.querySelector('.menu-list').innerHTML = "<p>Tải mục menu thất bại.</p>";
    showToast('Không thể tải danh sách món ăn. Vui lòng thử lại.', 'error');
  }
}

/**
 * Load tất cả categories để hiển thị (kể cả rỗng)
 */
async function loadAllCategoriesForDisplay() {
  try {
    const res = await fetch('/api/admin/categories');
    if (!res.ok) throw new Error('Tải danh mục thất bại');

    const data = await res.json();
    if (!data.success) throw new Error('Tải danh mục thất bại');

    allCategories = data.data;
  } catch (error) {
    console.error("Lỗi tải danh mục:", error);
    allCategories = [];
  }
}

/**
 * Hiển thị danh sách menu items theo categories
 */
function displayMenuItems(items) {
  const container = document.querySelector('.menu-list');
  container.innerHTML = '';

  const grouped = {};
  items.forEach(item => {
    const categoryId = item.category ? item.category.id : null;
    const categoryName = item.category ? item.category.name : "Chưa phân loại";
    
    if (!grouped[categoryId]) {
      grouped[categoryId] = {
        name: categoryName,
        items: []
      };
    }
    grouped[categoryId].items.push(item);
  });

  allCategories.forEach(category => {
    const categoryDiv = document.createElement('div');
    const categoryData = grouped[category.id];
    const hasItems = categoryData && categoryData.items.length > 0;

    categoryDiv.innerHTML = `
      <div class="category-header">
        ${category.name} 
        <span style="color: #6B7280; font-size: 14px; font-weight: 400; margin-left: 8px;">
          (${hasItems ? categoryData.items.length : 0} món)
        </span>
      </div>
    `;

    if (!hasItems) {
      categoryDiv.innerHTML += `
        <div style="text-align: center; padding: 40px 20px; color: #6B7280; background: white; border: 1px solid #E5E7EB; border-radius: 12px; margin-bottom: 16px;">
          <i class="fa fa-inbox" style="font-size: 48px; margin-bottom: 12px; opacity: 0.3;"></i>
          <p style="margin: 0; font-size: 14px;">Chưa có món ăn nào trong danh mục này</p>
          <button 
            type="button" 
            onclick="openAddItemModalWithCategory(${category.id})" 
            style="margin-top: 12px; background: linear-gradient(135deg, #1a1a1a 0%, #2d2d2d 100%); color: white; padding: 8px 16px; border-radius: 8px; border: none; cursor: pointer; font-size: 13px; font-weight: 600;">
            <i class="fa fa-plus"></i> Thêm món ăn đầu tiên
          </button>
        </div>
      `;
    } else {
      categoryData.items.forEach(it => {
        categoryDiv.innerHTML += generateMenuItemHTML(it);
      });
    }

    container.appendChild(categoryDiv);
  });

  // Hiển thị món ăn chưa phân loại
  const uncategorizedItems = items.filter(item => !item.category);
  if (uncategorizedItems.length > 0) {
    const uncategorizedDiv = document.createElement('div');
    uncategorizedDiv.innerHTML = `
      <div class="category-header">
        Chưa phân loại 
        <span style="color: #6B7280; font-size: 14px; font-weight: 400; margin-left: 8px;">
          (${uncategorizedItems.length} món)
        </span>
      </div>
    `;

    uncategorizedItems.forEach(it => {
      uncategorizedDiv.innerHTML += generateMenuItemHTML(it);
    });

    container.appendChild(uncategorizedDiv);
  }

  if (allCategories.length === 0 && items.length === 0) {
    container.innerHTML = '<p style="text-align:center; color:#6B7280; padding:40px;">Chưa có danh mục và món ăn nào. Hãy tạo danh mục trước!</p>';
  }
}

/**
 * Generate HTML cho 1 menu item
 */
function generateMenuItemHTML(item) {
  const categoryName = item.category ? item.category.name : "N/A";
  const prepTime = item.prepTimeInMinutes || 15;
  const allergens = item.allergens || [];
  const allergensHTML = allergens.length > 0
    ? `Allergens: ${allergens.join(', ')}`
    : 'No allergen';

  const isExpanded = expandedItems.has(item.id);
  const detailClass = isExpanded ? 'menu-detail active' : 'menu-detail';
  const iconRotation = isExpanded ? 'rotate(180deg)' : 'rotate(0deg)';

  return `
    <div class="menu-list-item">
      <div class="menu-row" onclick="toggleDetail(this)" data-item-id="${item.id}">
        <span class="menu-name">${item.name}</span>
        <span>${item.price.toFixed(2)} VND</span>
        <span>${categoryName}</span>
        <span>${item.visibility}</span>
        <i class="fa fa-chevron-down toggle-icon" style="transform: ${iconRotation}"></i>
      </div>
      <div class="${detailClass}">
        <div class="menu-grid">
          <div class="menu-card">
            <div class="card-image">
              <img src="${item.imageUrl || 'https://via.placeholder.com/400'}" alt="${item.name}">
              <span class="badge ${item.isAvailable ? 'available' : 'unavailable'}">
                ${item.isAvailable ? 'Hiệu lực' : 'Không hiệu lực'}
              </span>
            </div>
            <div class="card-content">
              <div class="card-header">
                <h3>${item.name}</h3>
                <span class="price">${item.price.toFixed(2)} VND</span>
              </div>
              <p class="prep-time">
                <i class="fa fa-clock"></i> ${prepTime} phút
              </p>
              <p class="description">${item.description || 'Không có mô tả.'}</p>
              <div class="allergen-tags">${allergensHTML}</div>
              <div class="card-footer">
                <div class="toggle-switch" onclick="event.stopPropagation()">
                  <label class="switch">
                    <input type="checkbox" ${item.isAvailable ? 'checked' : ''} onchange="toggleAvailability(${item.id}, this)">
                    <span class="slider"></span>
                  </label>
                </div>
                <div class="action-buttons">
                  <a href="javascript:void(0)" onclick="editMenuItem(${item.id})"><i class="fa fa-pencil"></i></a>
                  <a href="javascript:void(0)" onclick="deleteMenuItem(${item.id})"><i class="fa fa-trash"></i></a>
                </div>
              </div>
            </div>
          </div>
        </div>
      </div>
    </div>
  `;
}

/**
 * Toggle chi tiết món ăn
 */
function toggleDetail(row) {
  const itemId = parseInt(row.getAttribute('data-item-id'));
  const detail = row.nextElementSibling;
  const icon = row.querySelector('.toggle-icon');

  if (detail.classList.contains('active')) {
    detail.classList.remove('active');
    icon.style.transform = 'rotate(0deg)';
    expandedItems.delete(itemId);
  } else {
    detail.classList.add('active');
    icon.style.transform = 'rotate(180deg)';
    expandedItems.add(itemId);
  }
}

/**
 * Toggle availability của món ăn
 */
async function toggleAvailability(itemId, checkbox) {
  const isAvailable = checkbox.checked;

  try {
    const res = await fetch(`/api/admin/menu/${itemId}/availability`, {
      method: 'PATCH',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify({ isAvailable })
    });

    const result = await res.json();

    if (!res.ok || !result.success) {
      checkbox.checked = !isAvailable;
      showToast(result.message || 'Không thể cập nhật trạng thái', 'error');
      return;
    }

    showToast(
      isAvailable ? 'Món ăn đã được kích hoạt' : 'Món ăn đã bị vô hiệu hóa',
      'success'
    );

    await loadMenuItems();

  } catch (error) {
    console.error('Lỗi cập nhật trạng thái:', error);
    checkbox.checked = !isAvailable;
    showToast('Đã xảy ra lỗi khi cập nhật trạng thái', 'error');
  }
}

/**
 * Xóa món ăn
 */
function deleteMenuItem(itemId) {
  const item = allMenuItems.find(it => it.id === itemId);
  if (!item) return;

  showConfirmDialog(
    'Xác nhận xóa món ăn',
    `Bạn có chắc chắn muốn xóa món "${item.name}"? Hành động này không thể hoàn tác.`,
    async () => {
      try {
        const res = await fetch(`/api/admin/menu/${itemId}`, {
          method: 'DELETE'
        });

        const result = await res.json();

        if (!res.ok || !result.success) {
          showToast(result.message || 'Không thể xóa món ăn', 'error');
          return;
        }

        showToast('Xóa món ăn thành công!', 'success');
        await loadMenuItems();

      } catch (error) {
        console.error('Lỗi xóa món ăn:', error);
        showToast('Đã xảy ra lỗi khi xóa món ăn', 'error');
      }
    }
  );
}

/**
 * Mở modal thêm món ăn mới
 */
function openAddItemModal() {
  resetForm();
  document.getElementById('modal-header-title').innerHTML = '<i class="fa fa-utensils"></i> Thêm mục menu mới';
  document.getElementById('btn-submit-form').innerHTML = '<i class="fa fa-check"></i> Lưu';
  
  const modal = document.getElementById('add-item-modal');
  modal.classList.add('active');
  document.body.style.overflow = 'hidden';
}

/**
 * Mở modal thêm món ăn với category đã chọn sẵn
 */
function openAddItemModalWithCategory(categoryId) {
  resetForm();
  document.getElementById('modal-header-title').innerHTML = '<i class="fa fa-utensils"></i> Thêm mục menu mới';
  document.getElementById('btn-submit-form').innerHTML = '<i class="fa fa-check"></i> Lưu';
  
  document.getElementById('item-category').value = categoryId;
  
  const modal = document.getElementById('add-item-modal');
  modal.classList.add('active');
  document.body.style.overflow = 'hidden';
}

/**
 * Đóng modal
 */
function closeModal() {
  const modal = document.getElementById('add-item-modal');
  modal.classList.add('closing');

  setTimeout(() => {
    modal.classList.remove('active', 'closing');
    document.body.style.overflow = '';
    resetForm();
  }, 300);
}

/**
 * Reset form
 */
function resetForm() {
  const form = document.getElementById('menu-item-form');
  form.reset();
  document.getElementById('item-id').value = '';
}

/**
 * Load categories cho dropdown
 */
async function loadCategories() {
  const select = document.getElementById('item-category');
  try {
    const res = await fetch('/api/admin/categories');
    if (!res.ok) throw new Error('Tải danh mục thất bại');

    const data = await res.json();
    if (!data.success) throw new Error('Tải danh mục thất bại');

    select.innerHTML = '<option value="">-- Chọn danh mục --</option>';
    
    data.data.forEach(category => {
      const option = document.createElement('option');
      option.value = category.id;
      option.textContent = category.name;
      select.appendChild(option);
    });
  } catch (error) {
    console.error("Tải danh mục thất bại:", error);
    select.innerHTML = '<option value="">Tải danh mục thất bại</option>';
  }
}

/**
 * Load category filter
 */
async function loadCategoryFilter() {
  const select = document.querySelector('.category-filter');
  try {
    const res = await fetch('/api/admin/categories');
    if (!res.ok) throw new Error('Tải danh mục thất bại');

    const data = await res.json();
    if (!data.success) throw new Error('Tải danh mục thất bại');

    select.innerHTML = '<option value="all">Tất cả danh mục</option>';
    
    data.data.forEach(category => {
      const itemCount = category.itemCount || 0;
      const option = document.createElement('option');
      option.value = category.id;
      option.textContent = `${category.name} (${itemCount} món)`;
      select.appendChild(option);
    });

    select.addEventListener('change', handleSearch);

  } catch (error) {
    console.error("Tải phân loại danh mục thất bại:", error);
  }
}

/**
 * Handle search và filter
 */
function handleSearch() {
  const searchInput = document.getElementById('search-input').value.toLowerCase();
  const categoryFilter = document.querySelector('.category-filter').value;

  let filtered = allMenuItems;

  if (searchInput) {
    filtered = filtered.filter(item =>
      item.name.toLowerCase().includes(searchInput) ||
      (item.description && item.description.toLowerCase().includes(searchInput))
    );
  }

  if (categoryFilter !== 'all') {
    filtered = filtered.filter(item =>
      item.category && item.category.id === parseInt(categoryFilter)
    );
  }

  displayMenuItems(filtered);
}

/**
 * Clear search
 */
function clearSearch() {
  const searchInput = document.getElementById('search-input');
  searchInput.value = '';
  handleSearch();
  document.querySelector('.search-clear').style.display = 'none';
}

/**
 * Edit món ăn
 */
async function editMenuItem(itemId) {
  const item = allMenuItems.find(it => it.id === itemId);
  if (!item) {
    showToast('Không tìm thấy món ăn', 'error');
    return;
  }

  document.getElementById('item-id').value = item.id;
  document.getElementById('item-name').value = item.name;
  document.getElementById('item-price').value = item.price;
  document.getElementById('item-category').value = item.category ? item.category.id : '';
  document.getElementById('item-visibility').value = item.visibility || 'PUBLIC';
  document.getElementById('item-description').value = item.description || '';
  document.getElementById('item-image-url').value = item.imageUrl || '';
  document.getElementById('item-prep-time').value = item.prepTimeInMinutes || 15;

  document.getElementById('modal-header-title').innerHTML = '<i class="fa fa-pencil"></i> Chỉnh sửa mục menu';
  document.getElementById('btn-submit-form').innerHTML = '<i class="fa fa-check"></i> Cập nhật';

  const modal = document.getElementById('add-item-modal');
  modal.classList.add('active');
  document.body.style.overflow = 'hidden';
}

/**
 * Submit form (Create/Update)
 */
async function handleFormSubmit(event) {
  event.preventDefault();

  const itemId = document.getElementById('item-id').value;
  const name = document.getElementById('item-name').value.trim();
  const price = parseFloat(document.getElementById('item-price').value);
  const categoryId = document.getElementById('item-category').value;
  const visibility = document.getElementById('item-visibility').value;
  const description = document.getElementById('item-description').value.trim();
  const imageUrl = document.getElementById('item-image-url').value.trim();
  const prepTimeInMinutes = parseInt(document.getElementById('item-prep-time').value);

  if (!name || !price || !categoryId || !visibility) {
    showToast('Vui lòng điền đầy đủ thông tin bắt buộc', 'warning');
    return;
  }

  const itemData = {
    name,
    price,
    categoryId: parseInt(categoryId),
    visibility,
    description,
    imageUrl,
    prepTimeInMinutes,
    isAvailable: true
  };

  try {
    let res, result;

    if (itemId) {
      res = await fetch(`/api/admin/menu/${itemId}`, {
        method: 'PUT',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify(itemData)
      });
    } else {
      res = await fetch('/api/admin/menu', {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify(itemData)
      });
    }

    result = await res.json();

    if (!res.ok || !result.success) {
      showToast(result.message || 'Không thể lưu món ăn', 'error');
      return;
    }

    showToast(
      itemId ? 'Cập nhật món ăn thành công!' : 'Tạo món ăn thành công!',
      'success'
    );

    closeModal();
    await loadMenuItems();

  } catch (error) {
    console.error('Lỗi lưu món ăn:', error);
    showToast('Đã xảy ra lỗi: ' + error.message, 'error');
  }
}

// Event listeners
document.addEventListener('DOMContentLoaded', () => {
  loadMenuItems();
  loadCategories();

  const btnOpenModal = document.getElementById('btn-open-modal');
  btnOpenModal.addEventListener('click', openAddItemModal);

  const btnCloseModal = document.getElementById('btn-close-modal');
  btnCloseModal.addEventListener('click', closeModal);

  const btnCancelForm = document.getElementById('btn-cancel-form');
  btnCancelForm.addEventListener('click', closeModal);

  const modal = document.getElementById('add-item-modal');
  modal.addEventListener('click', (e) => {
    if (e.target === modal) {
      closeModal();
    }
  });

  const form = document.getElementById('menu-item-form');
  form.addEventListener('submit', handleFormSubmit);

  const searchInput = document.getElementById('search-input');
  searchInput.addEventListener('input', (e) => {
    const clearBtn = document.querySelector('.search-clear');
    clearBtn.style.display = e.target.value ? 'block' : 'none';
    handleSearch();
  });
});