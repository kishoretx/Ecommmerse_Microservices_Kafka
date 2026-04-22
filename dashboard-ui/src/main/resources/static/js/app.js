const cardsContainer = document.getElementById("cards");
const statusEl = document.getElementById("status");
const orderStatusEl = document.getElementById("orderStatus");
const orderForm = document.getElementById("orderForm");
const lineItemsEl = document.getElementById("lineItems");
const addItemBtn = document.getElementById("addItemBtn");
const cartTotalEl = document.getElementById("cartTotal");

let orderChart;
let paymentChart;
let productCatalog = [];

async function fetchMetrics() {
  const res = await fetch("/api/dashboard/metrics");
  return await res.json();
}

async function fetchProducts() {
  const res = await fetch("/api/dashboard/products");
  return await res.json();
}

function card(title, value, subtitle) {
  return `<article class="card"><h3>${title}</h3><div class="metric">${value}</div><div class="sub">${subtitle}</div></article>`;
}

function renderCards(data) {
  const products = data.products || {};
  const orders = data.orders || {};
  const payments = data.payments || {};
  const notifications = data.notifications || {};

  cardsContainer.innerHTML =
    card("Products", products.totalProducts ?? "N/A", `Low stock: ${products.lowStockProducts ?? "N/A"}`) +
    card("Orders", orders.totalOrders ?? "N/A", `Fulfilled: ${orders.fulfilledOrders ?? "N/A"}`) +
    card("Payments", payments.totalPayments ?? "N/A", `Failed: ${payments.failedPayments ?? "N/A"}`) +
    card("Notifications", notifications.totalNotifications ?? "N/A", `Sent: ${notifications.sentNotifications ?? "N/A"}`);
}

function renderCharts(data) {
  const orders = data.orders || {};
  const payments = data.payments || {};

  const orderData = [orders.fulfilledOrders || 0, orders.pendingOrders || 0, orders.failedOrders || 0];
  const paymentData = [payments.successfulPayments || 0, payments.failedPayments || 0];

  if (orderChart) orderChart.destroy();
  if (paymentChart) paymentChart.destroy();

  orderChart = new Chart(document.getElementById("orderChart"), {
    type: "bar",
    data: {
      labels: ["Fulfilled", "Pending", "Failed"],
      datasets: [{ label: "Order Status", data: orderData, backgroundColor: ["#66d19e", "#ffcf66", "#ff6f61"] }]
    }
  });

  paymentChart = new Chart(document.getElementById("paymentChart"), {
    type: "doughnut",
    data: {
      labels: ["Success", "Failed"],
      datasets: [{ data: paymentData, backgroundColor: ["#6ad8f5", "#ff8d7e"] }]
    }
  });
}

function productOptionsHtml() {
  const top = productCatalog.slice(0, 120);
  return top.map((p) => {
    const label = `${p.id} - ${p.name} ($${p.price}) [stock:${p.stock}]`;
    return `<option value="${p.id}">${label}</option>`;
  }).join("");
}

function productById(id) {
  return productCatalog.find((p) => Number(p.id) === Number(id));
}

function updateTotals() {
  const rows = Array.from(lineItemsEl.querySelectorAll(".line-item-row"));
  let total = 0;

  rows.forEach((row) => {
    const productId = Number(row.querySelector(".item-product").value);
    const qty = Number(row.querySelector(".item-qty").value || 0);
    const product = productById(productId);
    const price = product ? Number(product.price) : 0;
    const subtotal = price * Math.max(0, qty);
    total += subtotal;

    row.querySelector(".item-subtotal").textContent = `$${subtotal.toFixed(2)}`;
  });

  cartTotalEl.textContent = `$${total.toFixed(2)}`;
}

function addLineItem(prefillIndex) {
  const row = document.createElement("div");
  row.className = "line-item-row";
  row.innerHTML = `
    <label>
      Product
      <select class="item-product" required>${productOptionsHtml()}</select>
    </label>
    <label>
      Quantity
      <input class="item-qty" type="number" min="1" value="1" required>
    </label>
    <div class="item-subtotal">$0.00</div>
    <button type="button" class="remove-item-btn">Remove</button>
  `;

  const select = row.querySelector(".item-product");
  if (typeof prefillIndex === "number" && productCatalog[prefillIndex]) {
    select.value = String(productCatalog[prefillIndex].id);
  }

  row.querySelector(".item-product").addEventListener("change", updateTotals);
  row.querySelector(".item-qty").addEventListener("input", updateTotals);

  row.querySelector(".remove-item-btn").addEventListener("click", () => {
    if (lineItemsEl.children.length <= 1) {
      orderStatusEl.textContent = "At least one line item is required.";
      return;
    }
    row.remove();
    updateTotals();
  });

  lineItemsEl.appendChild(row);
  updateTotals();
}

function collectItems() {
  const rows = Array.from(lineItemsEl.querySelectorAll(".line-item-row"));
  return rows.map((row) => {
    const productId = Number(row.querySelector(".item-product").value);
    const quantity = Number(row.querySelector(".item-qty").value);
    return { productId, quantity };
  });
}

function validateItems(items) {
  if (!items.length) {
    return "At least one line item is required.";
  }

  for (const item of items) {
    if (!item.productId || item.quantity < 1 || Number.isNaN(item.quantity)) {
      return "Each line item must have a product and quantity >= 1.";
    }
  }

  const requiredByProduct = new Map();
  items.forEach((item) => {
    const current = requiredByProduct.get(item.productId) || 0;
    requiredByProduct.set(item.productId, current + item.quantity);
  });

  for (const [productId, requiredQty] of requiredByProduct.entries()) {
    const product = productById(productId);
    if (!product) {
      return `Product ${productId} no longer exists.`;
    }
    if (requiredQty > Number(product.stock)) {
      return `Insufficient stock for ${product.name}. Requested ${requiredQty}, available ${product.stock}.`;
    }
  }

  return null;
}

async function refresh() {
  statusEl.textContent = "Refreshing...";
  const metrics = await fetchMetrics();
  renderCards(metrics);
  renderCharts(metrics);
  statusEl.textContent = "Updated at " + new Date().toLocaleTimeString();
}

document.getElementById("refreshBtn").addEventListener("click", refresh);
document.getElementById("seedOrdersBtn").addEventListener("click", async () => {
  statusEl.textContent = "Creating demo orders...";
  const res = await fetch("/api/dashboard/demo-orders?count=10", { method: "POST" });
  const out = await res.json();
  statusEl.textContent = `Created ${out.created}/${out.requested} demo orders`;

  productCatalog = await fetchProducts();
  updateTotals();
  await refresh();
});

addItemBtn.addEventListener("click", () => addLineItem());

orderForm.addEventListener("submit", async (e) => {
  e.preventDefault();

  const payload = {
    customerName: document.getElementById("customerName").value.trim(),
    customerEmail: document.getElementById("customerEmail").value.trim(),
    items: collectItems()
  };

  if (!payload.customerName || !payload.customerEmail) {
    orderStatusEl.textContent = "Customer name and email are required.";
    return;
  }

  const validationError = validateItems(payload.items);
  if (validationError) {
    orderStatusEl.textContent = validationError;
    return;
  }

  orderStatusEl.textContent = "Placing order...";

  try {
    const res = await fetch("/api/dashboard/orders", {
      method: "POST",
      headers: { "Content-Type": "application/json" },
      body: JSON.stringify(payload)
    });

    if (!res.ok) {
      const err = await res.text();
      orderStatusEl.textContent = `Order failed: ${err}`;
      return;
    }

    const out = await res.json();
    orderStatusEl.textContent = `Order placed: ${out.orderNumber} (status: ${out.status})`;

    productCatalog = await fetchProducts();
    updateTotals();
    await refresh();
  } catch (error) {
    orderStatusEl.textContent = `Order failed: ${error.message}`;
  }
});

(async function init() {
  productCatalog = await fetchProducts();
  addLineItem(0);
  addLineItem(1);
  await refresh();
  setInterval(refresh, 15000);
})();
