package com.ticketing.config;

import com.ticketing.models.Event;

public final class TestData {

    private TestData() {}

    public static Event createEventData() {
        Event event = new Event();
        event.setName(resolveProperty("test.event.name", "Concert Test Event"));
        event.setDescription(resolveProperty("test.event.description", "Test concert event"));
        event.setEventDate(resolveProperty("test.event.date", "2026-12-31T22:00"));
        event.setVenue(resolveProperty("test.event.venue", "Test Arena"));
        event.setMaxCapacity(Integer.parseInt(resolveProperty("test.event.maxCapacity", "5000")));
        event.setBasePrice(Double.parseDouble(resolveProperty("test.event.basePrice", "99.99")));
        return event;
    }

    public static Event updateEventData() {
        Event event = new Event();
        event.setName(resolveProperty("test.event.update.name", "Updated Concert Event"));
        event.setDescription(resolveProperty("test.event.update.description", "Updated description"));
        event.setEventDate(resolveProperty("test.event.update.date", "2026-12-31T22:00"));
        event.setVenue(resolveProperty("test.event.update.venue", "Updated Arena"));
        event.setMaxCapacity(Integer.parseInt(resolveProperty("test.event.update.maxCapacity", "6000")));
        event.setBasePrice(Double.parseDouble(resolveProperty("test.event.update.basePrice", "149.99")));
        return event;
    }

    private static String resolveProperty(String key, String defaultValue) {
        return System.getProperty(key, defaultValue);
    }
}
