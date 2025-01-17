package ee.carlrobert.codegpt.completions;

import ee.carlrobert.codegpt.credentials.AzureCredentialsManager;
import ee.carlrobert.codegpt.credentials.OpenAICredentialsManager;
import ee.carlrobert.codegpt.settings.advanced.AdvancedSettingsState;
import ee.carlrobert.codegpt.settings.state.AzureSettingsState;
import ee.carlrobert.codegpt.settings.state.OpenAISettingsState;
import ee.carlrobert.llm.client.Client;
import ee.carlrobert.llm.client.ProxyAuthenticator;
import ee.carlrobert.llm.client.azure.AzureClient;
import ee.carlrobert.llm.client.azure.AzureCompletionRequestParams;
import ee.carlrobert.llm.client.openai.OpenAIClient;
import ee.carlrobert.llm.client.you.YouClient;
import java.net.InetSocketAddress;
import java.net.Proxy;
import java.util.concurrent.TimeUnit;

public class CompletionClientProvider {

  public static OpenAIClient getOpenAIClient() {
    return getOpenAIClientBuilder().build();
  }

  public static AzureClient getAzureClient() {
    return getAzureClientBuilder().build();
  }

  public static YouClient getYouClient(String sessionId, String accessToken) {
    return new YouClient.Builder(sessionId, accessToken).build();
  }

  private static OpenAIClient.Builder getOpenAIClientBuilder() {
    var settings = OpenAISettingsState.getInstance();
    var builder = new OpenAIClient
        .Builder(OpenAICredentialsManager.getInstance().getApiKey())
        .setOrganization(settings.getOrganization());
    return (OpenAIClient.Builder) addDefaultClientParams(builder).setHost(settings.getBaseHost());
  }

  private static AzureClient.Builder getAzureClientBuilder() {
    var settings = AzureSettingsState.getInstance();
    var params = new AzureCompletionRequestParams(settings.getResourceName(), settings.getDeploymentId(), settings.getApiVersion());
    var builder = new AzureClient.Builder(AzureCredentialsManager.getInstance().getSecret(), params)
        .setActiveDirectoryAuthentication(settings.isUseAzureActiveDirectoryAuthentication());
    return (AzureClient.Builder) addDefaultClientParams(builder).setHost(settings.getBaseHost());
  }

  private static YouClient.Builder getYouClientBuilder() {
    var settings = OpenAISettingsState.getInstance();
    var builder = new OpenAIClient
        .Builder(OpenAICredentialsManager.getInstance().getApiKey())
        .setOrganization(settings.getOrganization());
    return (YouClient.Builder) addDefaultClientParams(builder).setHost(settings.getBaseHost());
  }

  private static Client.Builder addDefaultClientParams(Client.Builder builder) {
    var advancedSettings = AdvancedSettingsState.getInstance();
    var proxyHost = advancedSettings.getProxyHost();
    var proxyPort = advancedSettings.getProxyPort();
    if (!proxyHost.isEmpty() && proxyPort != 0) {
      builder.setProxy(
          new Proxy(advancedSettings.getProxyType(), new InetSocketAddress(proxyHost, proxyPort)));
      if (advancedSettings.isProxyAuthSelected()) {
        builder.setProxyAuthenticator(
            new ProxyAuthenticator(advancedSettings.getProxyUsername(), advancedSettings.getProxyPassword()));
      }
    }

    return builder
        .setConnectTimeout((long) advancedSettings.getConnectTimeout(), TimeUnit.SECONDS)
        .setReadTimeout((long) advancedSettings.getReadTimeout(), TimeUnit.SECONDS)
        .setRetryOnReadTimeout(true);
  }
}


