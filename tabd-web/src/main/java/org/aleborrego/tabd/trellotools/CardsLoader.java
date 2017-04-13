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

import java.io.IOException;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.HashMap;
import java.util.List;

import javax.transaction.Transactional;

import org.aleborrego.tabd.domain.Configuration;
import org.aleborrego.tabd.domain.Sprint;
import org.aleborrego.tabd.domain.SprintTicket;
import org.aleborrego.tabd.domain.Ticket;
import org.aleborrego.tabd.domain.repository.ConfigurationRepository;
import org.aleborrego.tabd.domain.repository.SprintRepository;
import org.aleborrego.tabd.domain.repository.SprintTicketRepository;
import org.aleborrego.tabd.domain.repository.TicketRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.julienvey.trello.domain.Action;
import com.julienvey.trello.domain.Argument;
import com.julienvey.trello.domain.Card;
import com.julienvey.trello.domain.PluginData;
import com.julienvey.trello.domain.TList;

import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * Load "Terminadas" cards from a board.
 * 
 * @author aleborrego
 *
 */
@NoArgsConstructor
@Component
@Slf4j
public class CardsLoader extends TrelloLoader {

	private static final String TERMINADAS = "Terminadas";

	@Autowired
	private ConfigurationRepository configurationRepository;

	@Autowired
	private TicketRepository ticketRepository;

	@Autowired
	private SprintTicketRepository sprintTicketRepository;

	@Autowired
	private SprintRepository sprintRepository;

	@Override
	@Transactional
	public void load() throws LoaderException {
		if (this.getTrello() == null) {
			initTrello();
		}
		log.info("Loading cards");

		log.info("Loading configurations");
		Configuration currentSprint = configurationRepository.findByKee(Configuration.CURRENT_SPRINT);
		String analysisPluginId = configurationRepository.findByKee(Configuration.ANALISIS_FIELD).getValue();
		String estimatedPluginId = configurationRepository.findByKee(Configuration.ESTIMACION_FIELD).getValue();

		log.info("Loading current sprint");
		Sprint sprint = sprintRepository.findBySprintNumber(Integer.valueOf(currentSprint.getValue()));
		if (sprint != null) {
			String board = sprint.getBoard();

			log.info("Invoking trello to load \"Terminadas\" list");
			List<TList> lists = this.getTrello().getBoardLists(board);
			String terminadasListId = null;
			for (TList list : lists) {
				if (TERMINADAS.equalsIgnoreCase(list.getName())) {
					terminadasListId = list.getId();
					log.debug("Terminadas found :)");
					break;
				}
			}

			if (terminadasListId != null) {
				log.info("Loading Cards from \"Termiandas\" list");
				List<Card> cards = this.getTrello().getListCards(terminadasListId, new Argument("pluginData", "true"));
				for (Card card : cards) {
					String cardName = card.getName();
					int index = cardName.indexOf(":");
					if (index == -1) {
						log.info("Ignoring weird card '{}'", cardName);
					} else {
						String issueId = cardName.substring(0, index);
						String ticketName = cardName.substring(index + 2);

						Ticket ticket = ticketRepository.findByIssueId(issueId);
						if (ticket == null) {
							ticket = new Ticket();
							ticket.setIssueId(issueId).setTitle(ticketName);
							ticketRepository.save(ticket);
							log.info("Ticket {} created", ticket);
						} else if (log.isDebugEnabled()) {
							log.debug("Ticket {} already on DB", ticket);
						}

						// Card.PluginData pluginData = card.ge
						List<PluginData> plugins = card.getPluginData();
						if (plugins == null || plugins.isEmpty()) {
							log.error("Card '{}' should have analysis or estimated", issueId);
						} else {
							int analysis = -1;
							int estimated = -1;
							try {
								// There should be only one plugin
								String pluginValue = plugins.get(0).getValue().replaceAll("\\\"", "\"");
								// remove Fields tag
								String pluginFields = pluginValue.substring(10, pluginValue.length() - 1);
								// Map to object
								ObjectMapper mapper = new ObjectMapper();
								HashMap<String, String> map = mapper.readValue(pluginFields,
										new TypeReference<HashMap<String, String>>() {
										});

								String analysisString = map.get(analysisPluginId);
								if (analysisString != null) {
									analysis = Integer.valueOf(analysisString);
								}
								String estimatedString = map.get(estimatedPluginId);
								if (estimatedString != null) {
									estimated = Integer.valueOf(estimatedString);
								}
							} catch (IOException e) {
								log.error("Something weird on deserializing plugin fields");
							}

							SprintTicket sprintTicket = sprintTicketRepository.findBySprintAndTicket(sprint, ticket);

							log.info("Loading actions from card: '{}'", card);
							List<Action> actions = card.getActions(new Argument("filter", "updateCard"));
							LocalDate date = sprint.getStartDate();
							for (Action action : actions) {
								// Use last
								LocalDate newDate = action.getDate().toInstant().atZone(ZoneId.systemDefault())
										.toLocalDate();
								if (action.getData().getListAfter() != null
										&& "Terminadas".equals(action.getData().getListAfter().getName())
										&& newDate.isAfter(date)) {
									date = newDate;
								}
							}

							// Create the sprint ticket
							if (sprintTicket == null || (analysis != -1 && sprintTicket.getAnalisisSP() == -1)
									|| estimated != -1 && sprintTicket.getEstimatedSP() == -1) {
								// Create the ticket sprint.
								sprintTicket = new SprintTicket();
								sprintTicket.setSprint(sprint).setTicket(ticket).setAnalisisSP(analysis)
										.setEstimatedSP(estimated).setFinished(date);
							} else if (analysis != -1 && sprintTicket.getAnalisisSP() != -1) {
								// Update Analysis
								sprintTicket.setAnalisisSP(analysis).setFinished(date);
							} else if (estimated != -1 && sprintTicket.getEstimatedSP() != -1) {
								// Update Estimated
								sprintTicket.setEstimatedSP(estimated).setFinished(date);
							}

							sprintTicketRepository.save(sprintTicket);

							sprint.setLastAnalizedDate(LocalDate.now());
							sprintRepository.save(sprint);

						}
					}

				}
			} else {
				throw new LoaderException("Board does not have a \"Terminadas\" list");
			}

		} else {
			throw new LoaderException("Sprint not configured");
		}

	}

}
