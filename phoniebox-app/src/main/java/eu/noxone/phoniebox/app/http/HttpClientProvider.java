package eu.noxone.phoniebox.app.http;

import jakarta.annotation.PostConstruct;
import jakarta.enterprise.context.ApplicationScoped;
import java.time.Duration;
import okhttp3.OkHttpClient;

@ApplicationScoped
public class HttpClientProvider {

  private OkHttpClient client;

  @PostConstruct
  void init() {
    client =
        new OkHttpClient.Builder()
            .connectTimeout(Duration.ofSeconds(10))
            .readTimeout(Duration.ofSeconds(30))
            .writeTimeout(Duration.ofSeconds(10))
            .build();
  }

  public OkHttpClient get() {
    return client;
  }
}
