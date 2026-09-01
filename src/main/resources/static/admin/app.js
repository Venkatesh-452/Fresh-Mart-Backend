const state = {
    token: localStorage.getItem("freshmartAdminToken") || "",
    user: JSON.parse(localStorage.getItem("freshmartAdminUser") || "null"),
    categories: [],
    vegetables: [],
    orders: [],
    payments: []
};

const views = document.querySelectorAll(".view");
const navItems = document.querySelectorAll(".nav-item");
const loginPanel = document.getElementById("loginPanel");
const appContent = document.getElementById("appContent");
const sessionLabel = document.getElementById("sessionLabel");
const logoutBtn = document.getElementById("logoutBtn");
const toast = document.getElementById("toast");

function showToast(message, isError = false) {
    toast.textContent = message;
    toast.style.background = isError ? "#842029" : "#17231d";
    toast.classList.remove("hidden");
    setTimeout(() => toast.classList.add("hidden"), 3200);
}

async function api(path, options = {}) {
    const headers = {
        "Content-Type": "application/json",
        ...(options.headers || {})
    };

    if (state.token) {
        headers.Authorization = `Bearer ${state.token}`;
    }

    const response = await fetch(path, { ...options, headers });
    const text = await response.text();
    const data = text ? JSON.parse(text) : null;

    if (!response.ok) {
        throw new Error(data?.message || data?.error || text || "Request failed");
    }

    return data;
}

function serializeForm(form) {
    return Object.fromEntries(new FormData(form).entries());
}

function setLoggedIn(user) {
    state.user = user;
    localStorage.setItem("freshmartAdminUser", JSON.stringify(user));
    loginPanel.classList.add("hidden");
    appContent.classList.remove("hidden");
    logoutBtn.classList.remove("hidden");
    sessionLabel.textContent = `${user.name || user.email} (${user.role})`;
}

function setLoggedOut() {
    state.token = "";
    state.user = null;
    localStorage.removeItem("freshmartAdminToken");
    localStorage.removeItem("freshmartAdminUser");
    loginPanel.classList.remove("hidden");
    appContent.classList.add("hidden");
    logoutBtn.classList.add("hidden");
    sessionLabel.textContent = "Not logged in";
}

function changeView(viewName) {
    views.forEach(view => view.classList.toggle("active", view.id === viewName));
    navItems.forEach(item => item.classList.toggle("active", item.dataset.view === viewName));
    document.getElementById("viewTitle").textContent =
        viewName.charAt(0).toUpperCase() + viewName.slice(1);
}

function emptyMessage(message) {
    return `<div class="meta">${message}</div>`;
}

function renderCategories() {
    document.getElementById("categoryCount").textContent = state.categories.length;
    const list = document.getElementById("categoryList");
    list.innerHTML = state.categories.length ? state.categories.map(category => `
        <article class="item">
            <div>
                <div class="item-title">${category.name}</div>
                <div class="meta">${category.description || "No description"}</div>
            </div>
            <div class="item-actions">
                <button class="ghost" data-edit-category="${category.id}">Edit</button>
                <button class="danger" data-delete-category="${category.id}">Delete</button>
            </div>
        </article>
    `).join("") : emptyMessage("No categories found.");
}

function renderVegetables() {
    document.getElementById("vegetableCount").textContent = state.vegetables.length;
    const list = document.getElementById("vegetableList");
    list.innerHTML = state.vegetables.length ? state.vegetables.map(vegetable => `
        <article class="item">
            <div>
                <div class="item-title">${vegetable.name}</div>
                <div class="meta">
                    Rs ${vegetable.price} / ${vegetable.unit} | Stock: ${vegetable.quantity}
                    | Category: ${vegetable.categoryName || vegetable.categoryId || "N/A"}
                </div>
                <div class="meta">${vegetable.description || "No description"}</div>
            </div>
            <div class="item-actions">
                <button class="ghost" data-edit-vegetable="${vegetable.id}">Edit</button>
                <button class="danger" data-delete-vegetable="${vegetable.id}">Delete</button>
            </div>
        </article>
    `).join("") : emptyMessage("No vegetables found.");

    const categoryOptions = state.categories.map(category =>
        `<option value="${category.id}">${category.name}</option>`
    ).join("");
    document.querySelector("#vegetableForm select[name='categoryId']").innerHTML = categoryOptions;

    const vegetableOptions = state.vegetables.map(vegetable =>
        `<option value="${vegetable.id}">${vegetable.name}</option>`
    ).join("");
    document.querySelector("#stockForm select[name='vegetableId']").innerHTML = vegetableOptions;
}

function renderInventory() {
    const list = document.getElementById("inventoryList");
    list.innerHTML = state.inventory?.length ? state.inventory.map(item => `
        <article class="item">
            <div>
                <div class="item-title">${item.vegetableName}</div>
                <div class="meta">
                    Total: ${item.totalStock} ${item.unit} | Sold: ${item.soldQuantity} ${item.unit}
                    | Available: ${item.availableQuantity} ${item.unit}
                </div>
                <div class="meta">Last updated: ${item.lastUpdated || "N/A"}</div>
            </div>
        </article>
    `).join("") : emptyMessage("No inventory records found.");
}

function renderOrders() {
    document.getElementById("orderCount").textContent = state.orders.length;
    const statuses = ["PLACED", "CONFIRMED", "PACKED", "OUT_FOR_DELIVERY", "DELIVERED", "CANCELLED"];
    const list = document.getElementById("orderList");
    list.innerHTML = state.orders.length ? state.orders.map(order => `
        <article class="item">
            <div>
                <div class="item-title">Order #${order.orderId} - ${order.customerName}</div>
                <div class="meta">Amount: Rs ${order.totalAmount} | Date: ${order.orderDate || "N/A"}</div>
                <div class="meta">Items: ${(order.items || []).map(item => `${item.vegetableName} x ${item.quantity}`).join(", ")}</div>
                <span class="badge">${order.status}</span>
            </div>
            <div class="item-actions">
                <select data-order-status="${order.orderId}">
                    ${statuses.map(status => `<option value="${status}" ${status === order.status ? "selected" : ""}>${status}</option>`).join("")}
                </select>
                <button data-update-order="${order.orderId}">Update</button>
            </div>
        </article>
    `).join("") : emptyMessage("No orders found.");
}

function renderPayments() {
    document.getElementById("pendingPaymentCount").textContent =
        state.payments.filter(payment => payment.status === "PENDING").length;
    const statuses = ["PENDING", "SUCCESS", "FAILED", "REFUNDED"];
    const list = document.getElementById("paymentList");
    list.innerHTML = state.payments.length ? state.payments.map(payment => `
        <article class="item">
            <div>
                <div class="item-title">Payment #${payment.paymentId} - Order #${payment.orderId}</div>
                <div class="meta">${payment.customerName} | Rs ${payment.amount} | ${payment.paymentMethod}</div>
                <div class="meta">Transaction: ${payment.transactionId || "Not added"}</div>
                <span class="badge ${payment.status === "PENDING" ? "warn" : ""}">${payment.status}</span>
            </div>
            <div class="item-actions">
                <select data-payment-status="${payment.paymentId}">
                    ${statuses.map(status => `<option value="${status}" ${status === payment.status ? "selected" : ""}>${status}</option>`).join("")}
                </select>
                <input data-payment-transaction="${payment.paymentId}" placeholder="Transaction ID" value="${payment.transactionId || ""}">
                <button data-update-payment="${payment.paymentId}">Update</button>
            </div>
        </article>
    `).join("") : emptyMessage("No payments found.");
}

async function loadCategories() {
    state.categories = await api("/api/admin/categories");
    renderCategories();
}

async function loadVegetables() {
    state.vegetables = await api("/api/vegetables");
    renderVegetables();
}

async function loadInventory() {
    state.inventory = await api("/api/inventory/all");
    renderInventory();
}

async function loadOrders() {
    state.orders = await api("/api/orders/all");
    renderOrders();
}

async function loadPayments(path = "/api/payments/all") {
    state.payments = await api(path);
    renderPayments();
}

async function loadDashboard() {
    await Promise.all([
        loadCategories(),
        loadVegetables(),
        loadInventory(),
        loadOrders(),
        loadPayments()
    ]);
}

document.getElementById("loginForm").addEventListener("submit", async event => {
    event.preventDefault();
    try {
        const credentials = serializeForm(event.currentTarget);
        const response = await api("/api/users/login", {
            method: "POST",
            body: JSON.stringify(credentials)
        });

        if (response.role !== "ADMIN") {
            throw new Error("This account is not an ADMIN user.");
        }

        state.token = response.token;
        localStorage.setItem("freshmartAdminToken", response.token);
        setLoggedIn(response);
        await loadDashboard();
        showToast("Logged in successfully");
    } catch (error) {
        showToast(error.message, true);
    }
});

logoutBtn.addEventListener("click", setLoggedOut);

navItems.forEach(item => {
    item.addEventListener("click", () => changeView(item.dataset.view));
});

document.getElementById("categoryForm").addEventListener("submit", async event => {
    event.preventDefault();
    const form = event.currentTarget;
    const values = serializeForm(form);
    const id = values.id;
    delete values.id;

    try {
        await api(id ? `/api/admin/categories/${id}` : "/api/admin/categories", {
            method: id ? "PUT" : "POST",
            body: JSON.stringify(values)
        });
        form.reset();
        document.getElementById("categoryFormTitle").textContent = "Create Category";
        await loadCategories();
        renderVegetables();
        showToast("Category saved");
    } catch (error) {
        showToast(error.message, true);
    }
});

document.getElementById("vegetableForm").addEventListener("submit", async event => {
    event.preventDefault();
    const form = event.currentTarget;
    const values = serializeForm(form);
    const id = values.id;
    delete values.id;

    values.price = Number(values.price);
    values.quantity = Number(values.quantity);
    values.categoryId = Number(values.categoryId);

    try {
        await api(id ? `/api/vegetables/${id}` : "/api/vegetables", {
            method: id ? "PUT" : "POST",
            body: JSON.stringify(values)
        });
        form.reset();
        document.getElementById("vegetableFormTitle").textContent = "Create Vegetable";
        await loadVegetables();
        showToast("Vegetable saved");
    } catch (error) {
        showToast(error.message, true);
    }
});

document.getElementById("stockForm").addEventListener("submit", async event => {
    event.preventDefault();
    const form = event.currentTarget;
    const values = serializeForm(form);
    values.vegetableId = Number(values.vegetableId);
    values.quantity = Number(values.quantity);

    try {
        await api("/api/inventory/add-stock", {
            method: "POST",
            body: JSON.stringify(values)
        });
        form.reset();
        await Promise.all([loadInventory(), loadVegetables()]);
        showToast("Stock added");
    } catch (error) {
        showToast(error.message, true);
    }
});

document.body.addEventListener("click", async event => {
    const categoryEditId = event.target.dataset.editCategory;
    const categoryDeleteId = event.target.dataset.deleteCategory;
    const vegetableEditId = event.target.dataset.editVegetable;
    const vegetableDeleteId = event.target.dataset.deleteVegetable;
    const orderId = event.target.dataset.updateOrder;
    const paymentId = event.target.dataset.updatePayment;

    try {
        if (categoryEditId) {
            const category = state.categories.find(item => item.id == categoryEditId);
            const form = document.getElementById("categoryForm");
            form.id.value = category.id;
            form.name.value = category.name;
            form.description.value = category.description || "";
            document.getElementById("categoryFormTitle").textContent = "Edit Category";
        }

        if (categoryDeleteId && confirm("Delete this category?")) {
            await api(`/api/admin/categories/${categoryDeleteId}`, { method: "DELETE" });
            await loadCategories();
            showToast("Category deleted");
        }

        if (vegetableEditId) {
            const vegetable = state.vegetables.find(item => item.id == vegetableEditId);
            const form = document.getElementById("vegetableForm");
            form.id.value = vegetable.id;
            form.name.value = vegetable.name;
            form.description.value = vegetable.description || "";
            form.price.value = vegetable.price;
            form.quantity.value = vegetable.quantity;
            form.unit.value = vegetable.unit;
            form.imageUrl.value = vegetable.imageUrl || "";
            form.categoryId.value = vegetable.categoryId;
            document.getElementById("vegetableFormTitle").textContent = "Edit Vegetable";
        }

        if (vegetableDeleteId && confirm("Delete this vegetable?")) {
            await api(`/api/vegetables/${vegetableDeleteId}`, { method: "DELETE" });
            await loadVegetables();
            showToast("Vegetable deleted");
        }

        if (orderId) {
            const status = document.querySelector(`[data-order-status="${orderId}"]`).value;
            await api(`/api/orders/${orderId}/status?status=${encodeURIComponent(status)}`, {
                method: "PUT"
            });
            await loadOrders();
            showToast("Order updated");
        }

        if (paymentId) {
            const status = document.querySelector(`[data-payment-status="${paymentId}"]`).value;
            const transactionId = document.querySelector(`[data-payment-transaction="${paymentId}"]`).value;
            await api(`/api/payments/${paymentId}/status`, {
                method: "PUT",
                body: JSON.stringify({ status, transactionId })
            });
            await Promise.all([loadPayments(), loadOrders()]);
            showToast("Payment updated");
        }
    } catch (error) {
        showToast(error.message, true);
    }
});

document.getElementById("resetCategoryBtn").addEventListener("click", () => {
    document.getElementById("categoryForm").reset();
    document.getElementById("categoryFormTitle").textContent = "Create Category";
});

document.getElementById("resetVegetableBtn").addEventListener("click", () => {
    document.getElementById("vegetableForm").reset();
    document.getElementById("vegetableFormTitle").textContent = "Create Vegetable";
});

document.getElementById("refreshCategoriesBtn").addEventListener("click", loadCategories);
document.getElementById("refreshVegetablesBtn").addEventListener("click", loadVegetables);
document.getElementById("refreshInventoryBtn").addEventListener("click", loadInventory);
document.getElementById("refreshOrdersBtn").addEventListener("click", loadOrders);
document.getElementById("refreshPaymentsBtn").addEventListener("click", () => loadPayments());
document.getElementById("pendingPaymentsBtn").addEventListener("click", () => loadPayments("/api/payments/pending"));

if (state.token && state.user) {
    setLoggedIn(state.user);
    loadDashboard().catch(error => {
        showToast(error.message, true);
        setLoggedOut();
    });
} else {
    setLoggedOut();
}
