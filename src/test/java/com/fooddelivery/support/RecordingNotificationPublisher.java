package com.fooddelivery.support;

import com.fooddelivery.notification.NotificationPublisher;
import com.fooddelivery.notification.event.AssignmentOfferedEvent;
import com.fooddelivery.notification.event.OrderStatusChangedEvent;
import java.util.ArrayList;
import java.util.List;

public class RecordingNotificationPublisher extends NotificationPublisher {

    private final List<OrderStatusChangedEvent> statusEvents = new ArrayList<>();
    private final List<AssignmentOfferedEvent> assignmentEvents = new ArrayList<>();

    public RecordingNotificationPublisher() {
        super(new NoOpApplicationEventPublisher());
    }

    @Override
    public void publishOrderStatusChanged(OrderStatusChangedEvent event) {
        statusEvents.add(event);
    }

    @Override
    public void publishAssignmentOffered(AssignmentOfferedEvent event) {
        assignmentEvents.add(event);
    }

    public List<OrderStatusChangedEvent> getStatusEvents() {
        return statusEvents;
    }

    public List<AssignmentOfferedEvent> getAssignmentEvents() {
        return assignmentEvents;
    }
}
