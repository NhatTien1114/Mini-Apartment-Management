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

        public Notification(String title, String message) {
            this.title = title;
            this.message = message;
            this.timestamp = System.currentTimeMillis();
        }
    }

    public interface NotificationListener {
        void onNotificationAdded(Notification notification);
    }

    private final List<Notification> notifications = new ArrayList<>();
    private final List<NotificationListener> listeners = new ArrayList<>();
    private boolean viewed = false;

    private NotificationManager() {}

    public static NotificationManager getInstance() {
        return INSTANCE;
    }

    public void addNotification(String title, String message) {
        Notification n = new Notification(title, message);
        notifications.add(0, n); // Thêm vào đầu danh sách
        viewed = false;
        for (NotificationListener l : listeners) {
            l.onNotificationAdded(n);
        }
    }

    public List<Notification> getNotifications() {
        return notifications;
    }

    public int getUnviewedCount() {
        return viewed ? 0 : notifications.size();
    }

    public void markAsViewed() {
        viewed = true;
    }

    public boolean isViewed() {
        return viewed;
    }

    public void addListener(NotificationListener listener) {
        listeners.add(listener);
    }
}
