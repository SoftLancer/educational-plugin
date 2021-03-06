package com.jetbrains.edu.learning.stepik;

import com.google.gson.FieldNamingPolicy;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.util.Key;
import com.intellij.util.PlatformUtils;
import com.intellij.util.net.HttpConfigurable;
import com.intellij.util.net.ssl.CertificateManager;
import com.intellij.util.net.ssl.ConfirmingTrustManager;
import com.jetbrains.edu.learning.EduNames;
import com.jetbrains.edu.learning.PluginUtils;
import com.jetbrains.edu.learning.courseFormat.Lesson;
import com.jetbrains.edu.learning.stepik.serialization.StepikLessonAdapter;
import com.jetbrains.edu.learning.stepik.serialization.StepikReplyAdapter;
import com.jetbrains.edu.learning.stepik.serialization.StepikStepOptionsAdapter;
import org.apache.http.HttpEntity;
import org.apache.http.HttpHost;
import org.apache.http.HttpStatus;
import org.apache.http.StatusLine;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.DefaultConnectionReuseStrategy;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Proxy;
import java.net.URI;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.List;
import java.util.Map;

public class StepikClient {
  private static final Logger LOG = Logger.getInstance(StepikClient.class.getName());
  private static CloseableHttpClient ourClient;
  private static final int TIMEOUT_SECONDS = 10;

  private StepikClient() {
  }

  @NotNull
  public static CloseableHttpClient getHttpClient() {
    if (ourClient == null) {
      initializeClient();
    }
    return ourClient;
  }

  public static <T> T getFromStepik(String link, final Class<T> container) throws IOException {
    return getFromStepik(link, container, (Map<Key, Object>) null);
  }

  public static <T> T getFromStepik(String link, final Class<T> container, @Nullable Map<Key, Object> params) throws IOException {
    return getFromStepik(link, container, getHttpClient(), params);
  }

  static <T> T getFromStepik(String link, final Class<T> container, @NotNull final CloseableHttpClient client) throws IOException {
    return getFromStepik(link, container, client, null);
  }

  static <T> T getFromStepik(String link,
                             final Class<T> container,
                             @NotNull final CloseableHttpClient client,
                             @Nullable Map<Key, Object> params) throws IOException {
    if (!link.startsWith("/")) link = "/" + link;
    final HttpGet request = new HttpGet(StepikNames.STEPIK_API_URL + link);
    addTimeout(request);

    final CloseableHttpResponse response = client.execute(request);
    final StatusLine statusLine = response.getStatusLine();
    final HttpEntity responseEntity = response.getEntity();
    final String responseString = responseEntity != null ? EntityUtils.toString(responseEntity) : "";
    EntityUtils.consume(responseEntity);
    if (statusLine.getStatusCode() != HttpStatus.SC_OK) {
      throw new IOException("Stepik returned non 200 status code " + responseString);
    }
    return deserializeStepikResponse(container, responseString, params);
  }

  private static void addTimeout(@NotNull HttpGet request) {
    int connectionTimeoutMs = TIMEOUT_SECONDS * 1000;
    RequestConfig requestConfig = RequestConfig.custom()
            .setConnectionRequestTimeout(connectionTimeoutMs)
            .setConnectTimeout(connectionTimeoutMs)
            .setSocketTimeout(connectionTimeoutMs)
            .build();
    request.setConfig(requestConfig);
  }

  static <T> T deserializeStepikResponse(Class<T> container, String responseString, @Nullable Map<Key, Object> params) {
    Gson gson = createGson(params);
    return gson.fromJson(responseString, container);
  }

  public static Gson createGson(@Nullable Map<Key, Object> params) {
    String language = StepikConnector.COURSE_LANGUAGE.get(params);
    return new GsonBuilder()
        .registerTypeAdapter(StepikWrappers.StepOptions.class, new StepikStepOptionsAdapter(language))
        .registerTypeAdapter(Lesson.class, new StepikLessonAdapter(language))
        .registerTypeAdapter(StepikWrappers.Reply.class, new StepikReplyAdapter(language))
        .setDateFormat("yyyy-MM-dd'T'HH:mm:ssZ")
        .setFieldNamingPolicy(FieldNamingPolicy.LOWER_CASE_WITH_UNDERSCORES).create();
  }

  private static void initializeClient() {
    if (ourClient == null) {
      final HttpClientBuilder builder = getBuilder();
      ourClient = builder.build();
    }
  }

  @NotNull
  public static HttpClientBuilder getBuilder() {
    final HttpClientBuilder builder = HttpClients.custom().setSSLContext(CertificateManager.getInstance().getSslContext()).
      setMaxConnPerRoute(100000).setConnectionReuseStrategy(DefaultConnectionReuseStrategy.INSTANCE).setUserAgent(getUserAgent());

    final HttpConfigurable proxyConfigurable = HttpConfigurable.getInstance();
    final List<Proxy> proxies = proxyConfigurable.getOnlyBySettingsSelector().select(URI.create(StepikNames.STEPIK_URL));
    final InetSocketAddress address = proxies.size() > 0 ? (InetSocketAddress)proxies.get(0).address() : null;
    if (address != null) {
      builder.setProxy(new HttpHost(address.getHostName(), address.getPort()));
    }
    final ConfirmingTrustManager trustManager = CertificateManager.getInstance().getTrustManager();
    try {
      SSLContext sslContext = SSLContext.getInstance("TLS");
      sslContext.init(null, new TrustManager[]{trustManager}, new SecureRandom());
      builder.setSSLContext(sslContext);
    }
    catch (NoSuchAlgorithmException | KeyManagementException e) {
      LOG.error(e.getMessage());
    }
    return builder;
  }

  @NotNull
  private static String getUserAgent() {
    String pluginVersion = PluginUtils.pluginVersion(EduNames.PLUGIN_ID);
    String version = pluginVersion == null ? "unknown" : pluginVersion;

    return String.format("%s/version(%s)/%s/%s", StepikNames.PLUGIN_NAME, version, System.getProperty("os.name"),
                         PlatformUtils.getPlatformPrefix());
  }
}
