package io.muoncore.protocol.event;

/**
 * A canonical Event for Muon
 */
public class Event<X> {

    //precedence
    private String id;
    private String parentId;
    private String serviceId;

    private String eventType;

    private X payload;

    public Event(String eventType, String id, String parentId, String serviceId, X payload) {
        this.id = id;
        this.parentId = parentId;
        this.serviceId = serviceId;
        this.payload = payload;
        this.eventType = eventType;
    }

    public String getEventType() {
        return eventType;
    }

    public String getId() {
        return id;
    }

    public String getParentId() {
        return parentId;
    }

    public String getServiceId() {
        return serviceId;
    }

    public X getPayload() {
        return payload;
    }

    @Override
    public String toString() {
        return "Event{" +
                "id='" + id + '\'' +
                ", eventType='" + eventType + '\'' +
                '}';
    }
}