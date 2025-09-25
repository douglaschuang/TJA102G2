(function(){
  const TOGGLE_URL = '/api/wishlist/toggle';
  const LOGIN_URL  = '/shop/login';

  document.addEventListener('click', async (e) => {
    const a = e.target.closest('a.btn-wishlist');
    if (!a) return;
    e.preventDefault();

    const pid = a.getAttribute('data-product-id') || a.dataset.productId;
    if (!pid) return;

    try {
      const res = await fetch(TOGGLE_URL, {
        method: 'POST',
        headers: { 'Content-Type': 'application/x-www-form-urlencoded' },
        body: new URLSearchParams({ productId: pid })
      });

      if (res.status === 401) {
        if (window.Swal) {
          const go = await Swal.fire({
            icon: 'warning',
            title: '請先登入',
            text: '登入後即可收藏商品',
            showCancelButton: true,
            confirmButtonText: '前往登入',
            cancelButtonText: '取消'
          });
          if (go.isConfirmed) location.href = LOGIN_URL;
        } else {
          if (confirm('請先登入，現在前往登入嗎？')) location.href = LOGIN_URL;
        }
        return;
      }
      if (!res.ok) throw new Error('HTTP ' + res.status);

      const data = await res.json();
      const icon = a.querySelector('i');

      if (data.favorited) {
        a.classList.add('active');
        icon?.classList.remove('ion-android-favorite-outline');
        icon?.classList.add('ion-android-favorite');

        // 置中 SweetAlert（非 Toast）
        window.Swal
          ? await Swal.fire({ icon: 'success', title: '已加入收藏', confirmButtonText: '知道了' })
          : alert('已加入收藏');
      } else {
        a.classList.remove('active');
        icon?.classList.remove('ion-android-favorite');
        icon?.classList.add('ion-android-favorite-outline');

        window.Swal
          ? await Swal.fire({ icon: 'info', title: '已取消收藏', confirmButtonText: '知道了' })
          : alert('已取消收藏');
      }

      // 通知其他元件（如 mini 清單 / 徽章）刷新
      document.dispatchEvent(new Event('wishlist:changed'));
    } catch (err) {
      console.error('[wishlist/toggle]', err);
      window.Swal
        ? Swal.fire({ icon: 'error', title: '操作失敗', text: '請稍後再試', confirmButtonText: '知道了' })
        : alert('操作失敗，請稍後再試');
    }
  });
})();
