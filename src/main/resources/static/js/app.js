const DELIVERY = 40, FREE_ABOVE = 500;
// Pages opened from Spring Boot (port 8080) use relative URLs; pages opened with Live Server call the backend directly.
const API_BASE = location.port === "8080" ? "" : "http://localhost:8080";
const store = {
  get token() { return localStorage.getItem("token"); },
  get user() { try { return JSON.parse(localStorage.getItem("user")); } catch { return null; } }
};

async function api(path, { method = "GET", body } = {}) {
  const headers = { "Content-Type": "application/json" };
  if (store.token) headers.Authorization = "Bearer " + store.token;
  const res = await fetch(API_BASE + "/api" + path, { method, headers, body: body ? JSON.stringify(body) : undefined });
  const text = await res.text();
  let data = null;
  try { data = text ? JSON.parse(text) : null; } catch { /* non-JSON body */ }
  if (res.status === 401 && !path.startsWith("/auth")) {
    logout(false);
    location.href = "login.html?next=" + encodeURIComponent(location.pathname.split("/").pop() + location.search);
    throw new Error("Please log in");
  }
  if (!res.ok) throw new Error((data && data.message) || "Something went wrong. Try again.");
  return data;
}

const esc = s => String(s ?? "").replace(/[&<>"\u0027]/g, c => ({ "&": "&amp;", "<": "&lt;", ">": "&gt;", "\"": "&quot;", "\u0027": "&#39;" }[c]));
const money = n => "\u20B9" + Number(n).toLocaleString("en-IN", { maximumFractionDigits: 2 });
const stars = r => { const n = Math.round(r); return "\u2605".repeat(n) + "\u2606".repeat(5 - n); };
const param = k => new URLSearchParams(location.search).get(k);
const fmtDate = d => new Date(d).toLocaleString("en-IN", { day: "numeric", month: "short", year: "numeric", hour: "2-digit", minute: "2-digit" });
const STATUS_LABEL = { PLACED: "Order placed", CONFIRMED: "Confirmed", SHIPPED: "Shipped", OUT_FOR_DELIVERY: "Out for delivery", DELIVERED: "Delivered" };
const STATUSES = Object.keys(STATUS_LABEL);

function toast(msg) {
  let t = document.getElementById("toast");
  if (!t) { t = document.createElement("div"); t.id = "toast"; t.setAttribute("role", "status"); document.body.appendChild(t); }
  t.textContent = msg; t.classList.add("show");
  clearTimeout(t._h); t._h = setTimeout(() => t.classList.remove("show"), 2200);
}

function logout(redirect = true) {
  localStorage.removeItem("token"); localStorage.removeItem("user");
  if (redirect) location.href = "index.html";
}

function requireLogin() {
  if (!store.token) { location.href = "login.html?next=" + encodeURIComponent(location.pathname.split("/").pop() + location.search); return false; }
  return true;
}

function renderHeader() {
  const u = store.user;
  document.getElementById("site-header").innerHTML = `
  <header class="hdr"><div class="wrap hdr-in">
    <a class="logo" href="index.html"><span class="logo-mark">\u{1F33F}</span>AgriMarket</a>
    <form class="search" role="search" onsubmit="event.preventDefault();location.href=\u0027products.html?q=\u0027+encodeURIComponent(this.q.value)">
      <input name="q" placeholder="Search for products, farmers..." aria-label="Search products"></form>
    <nav class="nav" aria-label="Main">
      <a href="index.html">Home</a><a href="products.html">Products</a>
      ${u ? `<a href="dashboard.html">Dashboard</a>` : ""}
      ${u && u.role === "CUSTOMER" ? `<a href="orders.html">My orders</a>` : ""}
    </nav>
    <div class="hdr-act">
      <a class="cart-link" href="cart.html" aria-label="Cart">\u{1F6D2}<span id="cart-count" hidden>0</span></a>
      ${u ? `<span class="meta">${esc(u.name.split(" ")[0])}</span><button class="btn ghost sm" onclick="logout()">Log out</button>`
          : `<a class="btn sm" href="login.html">Log in / Register</a>`}
    </div></div></header>`;
  refreshCartCount();
}

async function refreshCartCount() {
  const el = document.getElementById("cart-count");
  if (!el || !store.token) return;
  try {
    const items = await api("/cart");
    const n = items.reduce((s, i) => s + i.quantity, 0);
    el.textContent = n; el.hidden = n === 0;
  } catch { /* header stays without a count */ }
}

async function addToCart(id, qty = 1) {
  if (!requireLogin()) return;
  try { await api("/cart", { method: "POST", body: { productId: id, quantity: qty } }); toast("Added to cart"); refreshCartCount(); }
  catch (e) { toast(e.message); }
}

function productCard(p) {
  const out = p.stock <= 0;
  return `<article class="card">
    <a href="product.html?id=${p.id}" class="pimg cat-${esc((p.category || "").toLowerCase())}" aria-hidden="true" tabindex="-1">${p.emoji || "\u{1F96C}"}</a>
    <div class="pbody">
      <a href="product.html?id=${p.id}" class="pname">${esc(p.name)}</a>
      <div class="price">${money(p.price)}<small>/${esc(p.unit)}</small></div>
      <div class="meta"><span class="stars">${stars(p.rating)}</span> (${p.rating})</div>
      <div class="farmer">Farmer: ${esc(p.farmer ? p.farmer.name : "Local farmer")}</div>
      <div class="stock ${out ? "out" : "in"}">${out ? "Out of stock" : "In stock"}</div>
      <button class="btn sm block" ${out ? "disabled" : ""} onclick="addToCart(${p.id},1)">Add to cart</button>
    </div></article>`;
}

function deliveryFor(subtotal) { return subtotal >= FREE_ABOVE || subtotal === 0 ? 0 : DELIVERY; }

function trackHtml(status) {
  const idx = STATUSES.indexOf(status);
  const icons = ["\u{1F4DD}", "\u2705", "\u{1F4E6}", "\u{1F69A}", "\u{1F3E0}"];
  return `<div class="track">${STATUSES.map((s, i) =>
    `<div class="step ${i <= idx ? "done" : ""}"><i>${icons[i]}</i>${STATUS_LABEL[s]}</div>`).join("")}</div>`;
}

function renderFooter() {
  const f = document.getElementById("site-footer");
  if (f) f.innerHTML = `<footer><div class="wrap">AgriMarket \u2013 fresh produce straight from local farmers.</div></footer>`;
}