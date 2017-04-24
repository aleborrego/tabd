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
		String realPluginId = configurationRepository.findByKee(Configuration.TRABAJO_REAL_FIELD).getValue();

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
						// Skip ':' and whitespace
						String ticketName = cardName.substring(index + 2);

						int cardinal = -1;

						int index2 = issueId.indexOf("-");

						if (index2 != -1) {
							cardinal = Integer.valueOf(issueId.substring(index2 + 1));
							issueId = issueId.substring(0, index2);
						}

						log.info("Loading ticket '{}'", issueId);
						Ticket ticket = ticketRepository.findByIssueId(issueId);
						if (ticket == null) {
							ticket = new Ticket();
							ticket.setIssueId(issueId).setTitle(ticketName);
							ticketRepository.save(ticket);
							log.info("Ticket {} created", ticket);
						} else if (log.isDebugEnabled()) {
							log.debug("Ticket {} already on DB", ticket);
						}

						List<PluginData> plugins = card.getPluginData();
						if (plugins == null || plugins.isEmpty()) {
							log.error("Card '{}' should have analysis or estimated", issueId);
						} else {
							int analysis = -1;
							int estimated = -1;
							int real = 0;
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
								String realString = map.get(realPluginId);
								if (realString != null) {
									real = Integer.valueOf(realString);
								}
							} catch (IOException e) {
								log.error("Something weird on deserializing plugin fields");
							}

							List<SprintTicket> sprintTickets = sprintTicketRepository
									.findBySprintAndTicketAndCardinalId(sprint, ticket, cardinal);

							log.info("Loading actions from card: '{}'", card);
							List<Action> actions = card.getActions();
							LocalDate date = sprint.getStartDate();
							LocalDate earliestDate = sprint.getEndDate();
							for (Action action : actions) {
								// Use last
								LocalDate newDate = action.getDate().toInstant().atZone(ZoneId.systemDefault())
										.toLocalDate();
								if (newDate.isBefore(earliestDate)) {
									earliestDate = newDate;
								}
								if ("updateCard".equals(action.getType()) && action.getData().getListAfter() != null
										&& "Terminadas".equals(action.getData().getListAfter().getName())
										&& newDate.isAfter(date)) {
									date = newDate;
								}
							}

							boolean planned = !earliestDate.isAfter(sprint.getStartDate());

							SprintTicket analysisTicket = null;
							SprintTicket developmentTicket = null;

							for (SprintTicket st : sprintTickets) {
								if (analysis != -1 && st.getAnalisisSP() != -1) {
									analysisTicket = st;
									analysisTicket.setCardinalId(cardinal).setAnalisisSP(analysis).setFinished(date)
											.setPlanned(planned);

									// In case there is analysis and estimated
									real = 0;

									sprintTicketRepository.save(analysisTicket);
								}
								if (estimated != -1 && st.getEstimatedSP() != -1) {
									developmentTicket = st;
									developmentTicket.setCardinalId(cardinal).setEstimatedSP(estimated).setRealSP(real)
											.setFinished(date).setPlanned(planned);

									sprintTicketRepository.save(developmentTicket);
								}
							}

							// Create the analysis ticket
							if (analysisTicket == null && analysis != -1) {
								// Create the ticket sprint.
								analysisTicket = new SprintTicket();
								analysisTicket.setCardinalId(cardinal).setAnalisisSP(analysis).setFinished(date)
										.setPlanned(planned);

								// In case there is analysis and estimated
								real = 0;

								sprintTicketRepository.save(analysisTicket);
							}

							// Create the development ticket
							if (developmentTicket == null && estimated != -1) {
								// Create the ticket sprint.
								developmentTicket = new SprintTicket();
								developmentTicket.setCardinalId(cardinal).setEstimatedSP(estimated).setRealSP(real)
										.setFinished(date).setPlanned(planned);

								sprintTicketRepository.save(developmentTicket);
							}

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
