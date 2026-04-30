package eu.noxone.phoniebox.http;

import eu.noxone.phoniebox.settings.application.port.in.GetSettingUseCase;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import java.time.Duration;
import okhttp3.OkHttpClient;

@ApplicationScoped
public class HttpClientProvider {

  static final long DEFAULT_CONNECT_TIMEOUT = 10;
  static final long DEFAULT_READ_TIMEOUT = 30;
  static final long DEFAULT_WRITE_TIMEOUT = 10;

  private final GetSettingUseCase getSetting;

  private OkHttpClient client;
  private long lastConnect = -1;
  private long lastRead = -1;
  private long lastWrite = -1;

  @Inject
  public HttpClientProvider(final GetSettingUseCase getSetting) {
    this.getSetting = getSetting;
  }

  public synchronized OkHttpClient get() {
    long connect = readLong(HttpSettingKeys.HTTP_CONNECT_TIMEOUT, DEFAULT_CONNECT_TIMEOUT);
    long read = readLong(HttpSettingKeys.HTTP_READ_TIMEOUT, DEFAULT_READ_TIMEOUT);
    long write = readLong(HttpSettingKeys.HTTP_WRITE_TIMEOUT, DEFAULT_WRITE_TIMEOUT);

    if (client == null || connect != lastConnect || read != lastRead || write != lastWrite) {
      lastConnect = connect;
      lastRead = read;
      lastWrite = write;
      client =
          new OkHttpClient.Builder()
              .connectTimeout(Duration.ofSeconds(connect))
              .readTimeout(Duration.ofSeconds(read))
              .writeTimeout(Duration.ofSeconds(write))
              .build();
    }
    return client;
  }

  private long readLong(final String key, final long defaultValue) {
    return getSetting
        .getSetting(key)
        .map(
            v -> {
              try {
                return Long.parseLong(v);
              } catch (NumberFormatException e) {
                return defaultValue;
              }
            })
        .orElse(defaultValue);
  }
}
