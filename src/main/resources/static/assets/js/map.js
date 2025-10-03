(function () {
  // 讓所有請求都自動帶上 context path
  let CP = (typeof window !== 'undefined' && window.APP_CTX) ? window.APP_CTX : '';
  if (CP.endsWith('/')) CP = CP.slice(0, -1);

  const defaultCenter = { lat: 25.033964, lng: 121.564468 }; // 台北101
  let map, markersLayer;
  const markerById = new Map();

  function initMap() {
    // Leaflet + OSM
    map = L.map('google-map-one').setView([defaultCenter.lat, defaultCenter.lng], 13);
    L.tileLayer('https://{s}.tile.openstreetmap.org/{z}/{x}/{y}.png', {
      maxZoom: 19,
      attribution: '&copy; OpenStreetMap contributors'
    }).addTo(map);

    markersLayer = L.layerGroup().addTo(map);

    // 科別固定（不打後端）
    setupFixedDepartments();

    // 控制元件
    const btn = byId("use-my-location");
    const radiusSel = byId("radius-km");
    const deptSel = byId("dept");
    const sortSel = ensureSortSelect(); // 只允許距離排序

    const runSearch = (pos) => {
      const lat = pos.lat, lng = pos.lng;
      const radiusKm = parseFloat(radiusSel?.value || "3");
      let dept = deptSel?.value || "";
      if (dept === "身心科診所") dept = "身心科"; // 保險
      const [sortBy, order] = (sortSel?.value || "distance,asc").split(",");

      const url = `${CP}/api/clinics/nearby?lat=${lat}&lng=${lng}`
                + `&radiusKm=${radiusKm}`
                + `&department=${encodeURIComponent(dept)}`
                + `&sortBy=${encodeURIComponent(sortBy)}&order=${encodeURIComponent(order)}`;

      setLoading(true);
      fetch(url)
        .then(r => r.json())
        .then(list => {
          renderMarkers(pos, list);
          renderList(list);
        })
        .catch(err => console.error("fetch nearby error:", err))
        .finally(() => setLoading(false));
    };

    // 進頁面先試定位；失敗就用預設中心
    geolocate().then(runSearch).catch(() => runSearch(defaultCenter));

    // 事件
    btn && btn.addEventListener("click", () => geolocate().then(runSearch).catch(() => runSearch(defaultCenter)));
    [radiusSel, deptSel, sortSel].forEach(el => el && el.addEventListener("change", () => {
      const c = map.getCenter();
      runSearch({ lat: c.lat, lng: c.lng });
    }));
  }

  // ---- 固定科別清單（不打後端）----
  function setupFixedDepartments() {
    const sel = document.getElementById("dept");
    if (!sel) return;

    let html = '<option value="">全部</option>';
    ['婦產科', '小兒科', '綜合醫院', '身心科'].forEach(d => {
      html += `<option value="${escapeHtml(d)}">${escapeHtml(d)}</option>`;
    });
    sel.innerHTML = html;
  }

  // ---- 只允許距離排序的選單；若 HTML 沒放，這裡自動補一個 ----
  function ensureSortSelect() {
    let sel = byId("sort-by");
    if (!sel) {
      const controlsRow = document.querySelector(".row.g-2, .row .g-2, .row");
      sel = document.createElement("select");
      sel.id = "sort-by";
      sel.className = "form-select form-select-sm";
      sel.innerHTML = `
        <option value="distance,asc" selected>距離 近→遠</option>
        <option value="distance,desc">距離 遠→近</option>
      `;
      if (controlsRow) {
        const wrap = document.createElement("div");
        wrap.className = "col-auto";
        wrap.innerHTML = `<label class="me-1">排序</label>`;
        wrap.appendChild(sel);
        controlsRow.appendChild(wrap);
      }
    } else {
      sel.innerHTML = `
        <option value="distance,asc" selected>距離 近→遠</option>
        <option value="distance,desc">距離 遠→近</option>
      `;
    }
    return sel;
  }

  // ---- 定位 ----
  function geolocate() {
    return new Promise((resolve, reject) => {
      if (!navigator.geolocation) return reject();
      navigator.geolocation.getCurrentPosition(
        (pos) => resolve({ lat: pos.coords.latitude, lng: pos.coords.longitude }),
        () => reject(),
        { enableHighAccuracy: true, timeout: 8000 }
      );
    });
  }

  // ---- 標記與視野 ----
  function renderMarkers(center, list) {
    markersLayer.clearLayers();
    markerById.clear();

    const bounds = [];

    // 使用者位置（藍點）
    const userMarker = L.circleMarker([center.lat, center.lng], { radius: 6, weight: 2 })
      .bindPopup("你的所在位置");
    userMarker.addTo(markersLayer);
    bounds.push([center.lat, center.lng]);

    // 診所標記
    (list || []).forEach(item => {
      const marker = L.marker([item.latitude, item.longitude]).addTo(markersLayer);

      // ★ 純靜態封面路徑：/assets/clinics/{id}.jpg
      marker.bindPopup(`
        <div style="min-width:220px">
          <img src="${CP}/assets/images/clinics/${item.clinicId}.jpg"
               alt="封面"
               style="width:100%;max-height:120px;object-fit:cover;border-radius:8px"
               onerror="this.onerror=null;this.src='${CP}/assets/images/clinic-default.jpg'">
          <div class="mt-2">
            <strong>${escapeHtml(item.clinicName)}</strong><br/>
            ${escapeHtml(item.clinicAddress)}<br/>
            科別：${escapeHtml(item.department) || "-"}<br/>
            電話：${escapeHtml(item.clinicPhone) || "-"}<br/>
            距離：約 ${typeof item.distanceKm === "number" ? item.distanceKm.toFixed(2) : "?"} km<br/>
            <a target="_blank" href="https://www.google.com/maps/dir/?api=1&destination=${item.latitude},${item.longitude}">用 Google 地圖導航</a>
          </div>
        </div>
      `);

      marker.on('click', () => setActiveListItem(item.clinicId));
      markerById.set(item.clinicId, marker);
      bounds.push([item.latitude, item.longitude]);
    });

    if (bounds.length > 1) {
      map.fitBounds(bounds, { padding: [30, 30] });
    } else {
      map.setView([center.lat, center.lng], 14);
    }
  }

  // ---- 右側列表 ----
  function renderList(list) {
    const ul = byId('clinic-list');
    const empty = byId('empty-state');
    if (!ul) return;

    ul.innerHTML = '';
    if (!list || list.length === 0) {
      empty && empty.classList.remove('d-none');
      return;
    }
    empty && empty.classList.add('d-none');

    const frag = document.createDocumentFragment();
    list.forEach(item => {
      const li = document.createElement('li');
      li.className = 'list-group-item clinic-item';
      li.dataset.id = item.clinicId;

      // ★ 每列縮圖：/assets/clinics/{id}.jpg
      li.innerHTML = `
        <div class="d-flex gap-2 align-items-start">
          <div class="flex-shrink-0">
            <img src="${CP}/assets/images/clinics/${item.clinicId}.jpg"
                 alt="封面"
                 class="rounded"
                 style="width:92px;height:64px;object-fit:cover"
                 onerror="this.onerror=null;this.src='${CP}/assets/images/clinic-default.jpg'">
          </div>
          <div class="flex-grow-1">
            <div class="title">${escapeHtml(item.clinicName)}</div>
            <div class="small text-muted">${escapeHtml(item.clinicAddress)}</div>
            <div class="small">科別：${escapeHtml(item.department) || '-'}　電話：${escapeHtml(item.clinicPhone) || '-'}</div>
          </div>
          <div class="ms-2 text-nowrap fw-semibold">
            ${typeof item.distanceKm === 'number' ? item.distanceKm.toFixed(1) + ' km' : ''}
          </div>
        </div>
      `;
      li.addEventListener('click', () => selectClinic(item.clinicId));
      frag.appendChild(li);
    });
    ul.appendChild(frag);
  }

  function selectClinic(id) {
    const m = markerById.get(id);
    if (!m) return;
    map.setView(m.getLatLng(), Math.max(map.getZoom(), 15), { animate: true });
    m.openPopup();
    setActiveListItem(id);
  }

  function setActiveListItem(id) {
    const ul = byId('clinic-list');
    if (!ul) return;
    ul.querySelectorAll('.clinic-item.active').forEach(el => el.classList.remove('active'));
    const el = ul.querySelector(`.clinic-item[data-id="${id}"]`);
    if (el) {
      el.classList.add('active');
      el.scrollIntoView({ block: 'nearest', behavior: 'smooth' });
    }
  }

  // ---- 小工具 ----
  function byId(id) { return document.getElementById(id); }
  function escapeHtml(s) {
    if (s == null) return '';
    return String(s).replace(/[&<>"']/g, c => ({ '&': '&amp;', '<': '&lt;', '>': '&gt;', '"': '&quot;', "'": '&#39;' }[c]));
  }
  function setLoading(isLoading) {
    const controls = [byId("use-my-location"), byId("radius-km"), byId("dept"), byId("sort-by")].filter(Boolean);
    controls.forEach(el => el.disabled = !!isLoading);
  }

  // 啟動
  if (document.readyState === "loading") {
    document.addEventListener("DOMContentLoaded", initMap);
  } else {
    initMap();
  }
})();
