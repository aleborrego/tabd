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
package org.aleborrego.tabd.trellotools;

import org.aleborrego.tabd.domain.Configuration;
import org.aleborrego.tabd.domain.repository.ConfigurationRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.julienvey.trello.Trello;
import com.julienvey.trello.impl.TrelloImpl;
import com.julienvey.trello.impl.http.RestTemplateHttpClient;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;
import lombok.extern.slf4j.Slf4j;

@Accessors(chain = true)
@NoArgsConstructor
@Component
@Slf4j
public abstract class TrelloLoader {

	@Autowired
	private ConfigurationRepository configurationRepository;

	@Getter
	private Trello trello;

	/**
	 * Constructor, generates the trello client, for the loader
	 * 
	 * @throws LoaderException
	 */
	public void initTrello() {
		if (this.getTrello() == null) {
			Configuration trelloAppKey = configurationRepository.findByKee(Configuration.APP_KEE);
			Configuration trelloAuthKey = configurationRepository.findByKee(Configuration.AUTH_KEE);
			if (trelloAppKey != null && trelloAuthKey != null) {
				trello = new TrelloImpl(trelloAppKey.getValue(), trelloAuthKey.getValue(),
						new RestTemplateHttpClient());
			} else {
				log.error("Invalid app or auth key");
				// throw new LoaderException("Invalid app or auth key");
			}
		}
	}

	/**
	 * Load elements from trello
	 */
	public abstract void load() throws LoaderException;

}
