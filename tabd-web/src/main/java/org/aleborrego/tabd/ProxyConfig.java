/*
 * Copyright 2017 Alejandro Borrego
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *     http://www.apache.org/licenses/LICENSE-2.0
 *     
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.aleborrego.tabd;

import java.net.InetSocketAddress;
import java.net.Proxy;
import java.net.Proxy.Type;

import org.apache.http.HttpHost;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.CredentialsProvider;
import org.apache.http.client.HttpClient;
import org.apache.http.impl.client.BasicCredentialsProvider;
import org.apache.http.impl.client.HttpClientBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.ClientHttpRequestFactory;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.web.client.RestTemplate;

@Configuration
public class ProxyConfig {

	@Autowired
	private TabdConfigurationProperties tabdConfigurationProperties;

	@Bean
	public RestTemplate restTemplate() {
		TabdConfigurationProperties.Proxy proxyConf = tabdConfigurationProperties.getProxy();
		ClientHttpRequestFactory requestFactory = new SimpleClientHttpRequestFactory();

		if (proxyConf != null) {
			String proxyHost = proxyConf.getHost();
			int proxyPort = Integer.valueOf(proxyConf.getPort());
			if (proxyConf.getUser() == null) {
				SimpleClientHttpRequestFactory factory = new SimpleClientHttpRequestFactory();
				Proxy proxy = new Proxy(Type.HTTP, new InetSocketAddress(proxyHost, proxyPort));
				factory.setProxy(proxy);
				requestFactory = factory;
			} else {
				String proxyUser = proxyConf.getUser();
				String proxyPassword = proxyConf.getPassword();
				CredentialsProvider credsProvider = new BasicCredentialsProvider();
				credsProvider.setCredentials(new AuthScope(proxyHost, proxyPort),
						new UsernamePasswordCredentials(proxyUser, proxyPassword));

				HttpHost myProxy = new HttpHost(proxyHost, proxyPort);
				HttpClientBuilder clientBuilder = HttpClientBuilder.create();

				clientBuilder.setProxy(myProxy).setDefaultCredentialsProvider(credsProvider).disableCookieManagement();

				HttpClient httpClient = clientBuilder.build();
				HttpComponentsClientHttpRequestFactory factory = new HttpComponentsClientHttpRequestFactory();
				factory.setHttpClient(httpClient);
				requestFactory = factory;
			}
		}
		return new RestTemplate(requestFactory);

	}

}
