package eu.noxone.phoniebox.http;

public record SetHttpTimeoutsRequest(
    long connectTimeoutSeconds, long readTimeoutSeconds, long writeTimeoutSeconds) {}
