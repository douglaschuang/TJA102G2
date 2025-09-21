(function () {
  // 讓其他頁面也能叫到
  window.refreshWishlistMini = async function () {
    const box   = document.getElementById('wishlist-mini-box');
    const count = document.getElementById('wishlist-mini-count');
    if (!box) return;

    try {
      const res = await fetch('/api/wishlist/mini', { credentials: 'same-origin' });
      if (res.status === 401) {
        box.innerHTML = '<p class="text-center py-3">請先登入</p>';
        if (count) count.style.display = 'none';
		setWishlistCount(0);
        return;
      }
      if (!res.ok) throw new Error('HTTP ' + res.status);

      const data  = await res.json();
      const items = data.items || [];

      if (!items.length) {
        box.innerHTML = '<p class="text-center py-3">目前沒有收藏的商品</p>';
        if (count) count.style.display = 'none';
		setWishlistCount(0);
        return;
      }
	  
      box.innerHTML = items.map(it => `
        <div class="single-wishlist-product" data-product-id="${it.productId}">
          <span class="wishlist-close-icon">
            <a href="javascript:void(0)" class="mini-wishlist-remove" aria-label="移除收藏">
              <i class="ti-close"></i>
            </a>
          </span>
          <div class="image">
            <a href="${it.linkUrl}">
              <img src="${it.imageUrl}" class="img-fluid" alt="${it.name}">
            </a>
          </div>
          <div class="content">
            <h5><a href="${it.linkUrl}">${it.name}</a></h5>
            <p><span class="discounted-price">NT$${it.price}</span></p>
          </div>
        </div>
      `).join('');

      if (count) {
        count.style.display = '';
        count.querySelector('.subtotal-amount').textContent = items.length;
      }
	  
	  setWishlistCount(items.length);
    } catch (e) {
      console.error('[wishlist/mini]', e);
    }
  };

  // 打開收藏 overlay 時刷新
  document.addEventListener('click', (e) => {
    if (e.target.closest('[href="#wishlist-overlay"], #offcanvas-wishlist-icon')) {
      window.refreshWishlistMini();
    }
  });

  // 收藏狀態改變時刷新
  document.addEventListener('wishlist:changed', () => {
    window.refreshWishlistMini();
  });

  // overlay 內移除收藏
  document.addEventListener('click', async (e) => {
    const rm = e.target.closest('.mini-wishlist-remove');
    if (!rm) return;
    const wrap = rm.closest('[data-product-id]');
    const pid  = wrap?.getAttribute('data-product-id');
    if (!pid) return;

    try {
      const res = await fetch('/api/wishlist/toggle', {
        method:'POST',
        headers:{'Content-Type':'application/x-www-form-urlencoded'},
        body: new URLSearchParams({ productId: pid })
      });
      if (res.status === 401) { location.href = '/shop/login'; return; }
      if (!res.ok) throw new Error('HTTP '+res.status);

      document.dispatchEvent(new Event('wishlist:changed'));
    } catch (err) {
      console.error('[wishlist/remove-mini]', err);
      alert('操作失敗，請稍後再試');
    }
  });
})();
// ---- 收藏數：同步到 header ----
function setWishlistCount(n) {
  // header/行動版 可能有多個 count，一起更新
  document.querySelectorAll('.js-wishlist-count').forEach(el => {
    el.textContent = String(n);
  });
}

async function refreshWishlistCount() {
  try {
    const res = await fetch('/api/wishlist/mini', { credentials: 'same-origin' });
    if (res.status === 401) { setWishlistCount(0); return; }
    if (!res.ok) throw new Error('HTTP ' + res.status);
    const data  = await res.json();
    const items = Array.isArray(data.items) ? data.items : [];
    setWishlistCount(items.length);
  } catch (e) {
    console.error('[wishlist/count]', e);
    // 失敗時不要覆蓋現有值；也可視需要設 0
  }
}

// 頁面載入時先同步一次
document.addEventListener('DOMContentLoaded', refreshWishlistCount);

// 每次收藏狀態變動就同步
document.addEventListener('wishlist:changed', refreshWishlistCount);

