package eu.noxone.phoniebox.settings.application.port.in;

/** {@code value} is {@code null} to clear/remove a stored value for the given key. */
public record SetSettingCommand(String key, String value) {}
