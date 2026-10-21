// Chuông thông báo: dữ liệu lấy từ database qua /notifications?format=json.
function escapeHTML(value) {
    return String(value == null ? "" : value).replace(/[&<>"']/g, function (char) {
        return {"&": "&amp;", "<": "&lt;", ">": "&gt;", '"': "&quot;", "'": "&#39;"}[char];
    });
}

function renderNotificationItem(item, contextPath) {
    const unread = item.unread ? "is-unread" : "";
    const readButton = item.unread
        ? `<button type="button" class="notification-mark-read" data-notification-id="${Number(item.id)}">Đánh dấu đã đọc</button>` : "";
    return `<article class="notification-item ${unread}">
        <div class="notification-icon">${escapeHTML(item.icon)}</div>
        <div class="notification-content">
            <div class="notification-item-heading"><strong>${escapeHTML(item.title)}</strong>${item.unread ? '<span class="unread-dot"></span>' : ""}</div>
            <p>${escapeHTML(item.message)}</p>
            <span class="notification-time">${escapeHTML(item.time)}</span>${readButton}
        </div></article>`;
}

document.addEventListener("DOMContentLoaded", function () {
    const wrapper = document.querySelector(".notification-wrapper");
    const bellButton = document.getElementById("notificationBell");
    const dropdown = document.getElementById("notificationDropdown");
    const latestList = document.getElementById("latestNotifications");
    const badge = document.getElementById("notificationBadge");
    if (!wrapper || !bellButton || !dropdown) return;
    const url = wrapper.dataset.url;
    const contextPath = url.split("/notifications")[0];

    function load() {
        fetch(url, {headers: {"Accept": "application/json"}, credentials: "same-origin", cache: "no-store"})
            .then(function (res) { if (!res.ok) throw new Error("HTTP " + res.status); return res.json(); })
            .then(function (data) {
                if (badge) {
                    badge.textContent = data.unread > 9 ? "9+" : String(data.unread);
                    badge.hidden = data.unread === 0;
                }
                if (latestList) {
                    latestList.innerHTML = data.items.length
                        ? data.items.map(function (item) { return renderNotificationItem(item, contextPath); }).join("")
                        : '<p class="empty">Chưa có thông báo nào.</p>';
                }
            })
            .catch(function () {
                if (latestList) latestList.innerHTML = '<p class="empty">Không tải được thông báo. Vui lòng tải lại trang.</p>';
            });
    }

    load();
    // Làm mới khi người dùng quay lại tab; tránh polling liên tục vào server.
    document.addEventListener("visibilitychange", function () { if (!document.hidden) load(); });
    bellButton.addEventListener("click", function (event) {
        event.stopPropagation();
        const isOpen = dropdown.classList.toggle("is-open");
        bellButton.setAttribute("aria-expanded", String(isOpen));
        if (isOpen) load();
    });
    if (latestList) latestList.addEventListener("click", function (event) {
        const button = event.target.closest("[data-notification-id]");
        if (!button) return;
        const body = new URLSearchParams({action: "read", id: button.dataset.notificationId});
        fetch(contextPath + "/notifications", {
            method: "POST", credentials: "same-origin",
            headers: {"Content-Type": "application/x-www-form-urlencoded;charset=UTF-8", "Accept": "application/json"},
            body: body.toString()
        }).then(function (res) { if (res.ok || res.redirected) load(); });
    });
    document.addEventListener("click", function (event) {
        if (!dropdown.contains(event.target) && !bellButton.contains(event.target)) {
            dropdown.classList.remove("is-open");
            bellButton.setAttribute("aria-expanded", "false");
        }
    });
    document.addEventListener("keydown", function (event) {
        if (event.key === "Escape") {
            dropdown.classList.remove("is-open");
            bellButton.setAttribute("aria-expanded", "false");
        }
    });
});
