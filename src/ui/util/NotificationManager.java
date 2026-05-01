package ui.util;

import java.util.ArrayList;
import java.util.List;

/**
 * Singleton quản lý thông báo thời gian thực.
 * Các UI component khác có thể thêm thông báo mới và listener sẽ được gọi ngay lập tức.
 */
public class NotificationManager {
    private static final NotificationManager INSTANCE = new NotificationManager();

    public static class Notification {
        public final String title;
        public final String message;
        public final long timestamp;
        public final String type; // "contract_expiry", "service_price", "price_update", etc.

        public Notification(String title, String message) {
            this(title, message, "general");
        }

        public Notification(String title, String message, String type) {
            this.title = title;
            this.message = message;
            this.timestamp = System.currentTimeMillis();
            this.type = type;
        }
    }

    public interface NotificationListener {
        void onNotificationAdded(Notification notification);
    }

    public interface NotificationRemovedListener {
        void onNotificationRemoved();
    }

    private final List<Notification> notifications = new ArrayList<>();
    private final List<NotificationListener> listeners = new ArrayList<>();
    private final List<NotificationRemovedListener> removedListeners = new ArrayList<>();
    private int unviewedCount = 0;

    private NotificationManager() {}

    public static NotificationManager getInstance() {
        return INSTANCE;
    }

    public void addNotification(String title, String message) {
        addNotification(title, message, "general");
    }

    public void addNotification(String title, String message, String type) {
        Notification n = new Notification(title, message, type);
        notifications.add(0, n); // Thêm vào đầu danh sách
        unviewedCount++;
        for (NotificationListener l : listeners) {
            l.onNotificationAdded(n);
        }
    }

    public List<Notification> getNotifications() {
        return notifications;
    }

    /**
     * Số thông báo chưa xem (hiển thị số trên badge).
     */
    public int getUnviewedCount() {
        return unviewedCount;
    }

    /**
     * Đánh dấu đã xem tất cả - chuyển từ số sang chấm đỏ.
     */
    public void markAsViewed() {
        unviewedCount = 0;
    }

    /**
     * Còn thông báo chưa xử lý hay không (hiển thị chấm đỏ).
     */
    public boolean hasUnresolved() {
        return !notifications.isEmpty();
    }

    /**
     * Xóa thông báo đã xử lý xong.
     */
    public void removeNotification(Notification n) {
        notifications.remove(n);
        for (NotificationRemovedListener l : removedListeners) {
            l.onNotificationRemoved();
        }
    }

    /**
     * Xóa thông báo theo index.
     */
    public void removeNotification(int index) {
        if (index >= 0 && index < notifications.size()) {
            notifications.remove(index);
            for (NotificationRemovedListener l : removedListeners) {
                l.onNotificationRemoved();
            }
        }
    }

    public void addListener(NotificationListener listener) {
        listeners.add(listener);
    }

    public void addRemovedListener(NotificationRemovedListener listener) {
        removedListeners.add(listener);
    }
}
