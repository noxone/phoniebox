package eu.noxone.phoniebox.http;

public record HttpTimeoutsResponse(
    long connectTimeoutSeconds, long readTimeoutSeconds, long writeTimeoutSeconds) {}
