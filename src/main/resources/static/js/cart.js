$(document).ready(function () {

    $(document).on("click", "#offcanvas-cart-icon", function (e) {
        e.preventDefault();

        $("#cart-overlay").addClass("active");
        refreshCart();
    });

    $(document).on("click", "#cart-close-icon, .cart-overlay-close", function () {
        $("#cart-overlay").removeClass("active");
    });
});

function refreshCart() {
  $.ajax({
    url: "/api/cart/getCartDetail",
    type: "GET",
    success: function (data) {
      const container = $(".cart-product-container");
      container.empty();

	  // 支援兩種格式：data.items 或直接 data
      const items = data.items || data;

      if (items && items.length > 0) {
        let totalQty = 0, total = 0;

        items.forEach(item => {
          totalQty += item.quantity;
          total += item.price * item.quantity;
		  
          container.append(`
            <div class="single-cart-product">
              <span class="cart-close-icon">
                <a href="#" class="remove-item" data-product-id="${item.productId}">
                  <i class="ti-close"></i>
                </a>
              </span>
              <div class="image">
                <a href="/product/${item.productId}">
                  <img src="${item.imageUrl}" class="img-fluid" alt="${item.name}">
                </a>
              </div>
              <div class="content">
                <h5><a href="/product/${item.productId}">${item.name}</a></h5>
                <p><span class="cart-count">${item.quantity} x </span>
                <span class="discounted-price">$${item.price}</span></p>
              </div>
            </div>
          `);
        });

        $(".cart .count").text(totalQty);
        $(".cart-subtotal").text("$" + total);
      } else {
        container.append(`<p class="empty-cart">購物車目前沒有商品</p>`);
        $(".cart .count").text(0);
        $(".cart-subtotal").text("$0");
      }
    },
    error: function (xhr) {
      console.error("讀取購物車失敗", xhr.responseText);
    }
  });
}

// 刪除購物車商品
$(document).on("click", ".remove-item", function(e) {
  e.preventDefault();
  const productId = $(this).data("product-id");

  $.ajax({
    url: "/api/cart/del/" + productId,
    type: "DELETE",
    success: function() {
      console.log("刪除成功，重新刷新購物車");
      refreshCart(); // 刪除後重新刷新購物車
    },
    error: function(xhr) {
      console.error("刪除失敗", xhr.responseText);
    }
  });
});