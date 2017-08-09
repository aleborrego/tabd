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
package org.aleborrego.tabd.loader.trello;

import java.io.IOException;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.transaction.Transactional;

import org.aleborrego.tabd.domain.Configuration;
import org.aleborrego.tabd.domain.Sprint;
import org.aleborrego.tabd.domain.SprintTicket;
import org.aleborrego.tabd.domain.State;
import org.aleborrego.tabd.domain.Ticket;
import org.aleborrego.tabd.domain.repository.ConfigurationRepository;
import org.aleborrego.tabd.domain.repository.SprintRepository;
import org.aleborrego.tabd.domain.repository.SprintTicketRepository;
import org.aleborrego.tabd.domain.repository.StateRepository;
import org.aleborrego.tabd.domain.repository.TicketRepository;
import org.aleborrego.tabd.loader.LoaderException;
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

	@Autowired
	private ConfigurationRepository configurationRepository;

	@Autowired
	private TicketRepository ticketRepository;

	@Autowired
	private SprintTicketRepository sprintTicketRepository;

	@Autowired
	private SprintRepository sprintRepository;

	@Autowired
	private StateRepository stateRepository;

	@Override
	@Transactional
	public void load(String... arguments) throws LoaderException {
		if (this.getTrello() == null) {
			initTrello();
		}
		log.info("Loading cards");

		log.info("Loading configurations");
		Configuration currentSprint = configurationRepository.findByKee(Configuration.CURRENT_SPRINT);

		log.info("Loading current sprint");
		Sprint sprint = sprintRepository.findBySprintNumber(Integer.valueOf(currentSprint.getValue()));
		if (sprint != null) {
			String board = sprint.getBoard();

			log.info("Invoking trello to load lists, from the board: {}", board);
			List<TList> lists = this.getTrello().getBoardLists(board);

			for (TList list : lists) {
				State state = stateRepository.findByName(list.getName());
				String listId = list.getId();

				log.info("Loading Cards from \"Termiandas\" list");
				List<Card> cards = this.getTrello().getListCards(listId, new Argument("pluginData", "true"));
				for (Card card : cards) {
					String cardName = card.getName();

					log.info("Loading ticket for '{}'", cardName);
					Ticket ticket = loadTicket(cardName);

					if (ticket != null && state != null && state.getIsSprintRelated()) {

						Map<String, String> pluginFields = getPluginFields(card);

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
									&& list.getName().equals(action.getData().getListAfter().getName())
									&& newDate.isAfter(date)) {
								date = newDate;
							}
						}

						boolean planned = !earliestDate.isAfter(sprint.getStartDate());

						loadSprintTickets(sprint, ticket, pluginFields, planned, state, date, card.getId());

					}

				}
			}

		} else {
			throw new LoaderException("Sprint not configured");
		}

	}

	/**
	 * Load or creates ticket
	 * 
	 * @param cardName
	 * @return
	 */
	private Ticket loadTicket(String cardName) {
		Ticket ticket = null;
		int index = cardName.indexOf(":");
		if (index == -1) {
			log.info("Ignoring weird card '{}'", cardName);
		} else {
			String issueId = cardName.substring(0, index);
			// Skip ':' and whitespace
			String ticketName = cardName.substring(index + 2);

			log.info("Loading ticket '{}'", issueId);
			ticket = ticketRepository.findByIssueId(issueId);
			if (ticket == null) {
				ticket = new Ticket();
				ticket.setIssueId(issueId).setTitle(ticketName);
				ticketRepository.save(ticket);
				log.info("Ticket {} created", ticket);
			} else if (log.isDebugEnabled()) {
				log.debug("Ticket {} already on DB", ticket);
			}
		}
		return ticket;
	}

	/**
	 * Load PluginFields
	 * 
	 * @param card
	 * @return
	 */
	private Map<String, String> getPluginFields(Card card) {
		Map<String, String> pluginFields = new HashMap<>();

		List<PluginData> plugins = card.getPluginData();

		try {
			// Now, there should be only one plugin
			// TODO take into account cases with multiple/none pugins
			if (plugins == null || plugins.isEmpty()) {
				log.error("Card '{}' should have plugins", card);
			} else {
				String pluginValue = plugins.get(0).getValue().replaceAll("\\\"", "\"");
				// remove Fields tag
				String pluginFieldsSerialized = pluginValue.substring(10, pluginValue.length() - 1);
				// Map to object
				ObjectMapper mapper = new ObjectMapper();
				pluginFields = mapper.readValue(pluginFieldsSerialized, new TypeReference<HashMap<String, String>>() {
				});
			}

		} catch (IOException e) {
			log.error("Something weird happened on deserializing plugin fields", e);
		}

		return pluginFields;
	}

	/**
	 * Load and update or creates SprintTickets
	 * 
	 * @param cardName
	 * @return
	 */
	private SprintTicket loadSprintTickets(Sprint sprint, Ticket ticket, Map<String, String> pluginFields,
			boolean planned, State state, LocalDate updated, String cardId) {

		// TODO Make it config
		String analysisPluginId = configurationRepository.findByKee(Configuration.ANALISIS_FIELD).getValue();
		String estimatedPluginId = configurationRepository.findByKee(Configuration.ESTIMACION_FIELD).getValue();
		String realPluginId = configurationRepository.findByKee(Configuration.TRABAJO_REAL_FIELD).getValue();

		int estimated = 0;
		int real = 0;
		String analysisString = pluginFields.get(analysisPluginId);
		if (analysisString != null) {
			estimated += Integer.valueOf(analysisString);
		}
		String estimatedString = pluginFields.get(estimatedPluginId);
		if (estimatedString != null) {
			estimated += Integer.valueOf(estimatedString);
		}
		String realString = pluginFields.get(realPluginId);
		if (realString != null) {
			real = Integer.valueOf(realString);
		}

		SprintTicket sprintTicket = sprintTicketRepository.findBySprintAndTicketAndTrelloCardId(sprint, ticket, cardId);

		if (sprintTicket == null) {
			sprintTicket = new SprintTicket();
			sprintTicket.setSprint(sprint).setTicket(ticket).setTrelloCardId(cardId);
		}

		sprintTicket.setEstimatedSP(estimated).setWorkedSP(real).setPlanned(planned).setUpdated(updated)
				.setState(state);
		sprintTicketRepository.save(sprintTicket);

		sprint.setLastAnalizedDate(LocalDate.now());
		sprintRepository.save(sprint);

		return sprintTicket;
	}

}
