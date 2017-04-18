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
package org.aleborrego.tabd.services;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletResponse;

import org.aleborrego.tabd.domain.Configuration;
import org.aleborrego.tabd.domain.Sprint;
import org.aleborrego.tabd.domain.SprintTicket;
import org.aleborrego.tabd.domain.repository.ConfigurationRepository;
import org.aleborrego.tabd.domain.repository.SprintRepository;
import org.aleborrego.tabd.domain.repository.SprintTicketRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import lombok.extern.slf4j.Slf4j;

@RestController
@RequestMapping("/api/burndown")
@Slf4j
public class BurndownService {

	@Autowired
	public SprintRepository sprintRepository;

	@Autowired
	public SprintTicketRepository sprintTicketRepository;

	@Autowired
	public ConfigurationRepository configurationRepository;

	@GetMapping
	public Map<String, List<Integer>> getBurndownChart(HttpServletResponse response) {
		response.addHeader("Content-Type", "application/json; charset=utf-8");

		Map<String, List<Integer>> responseMap = new HashMap<>();

		List<Integer> days = new ArrayList<>();
		responseMap.put("days", days);
		List<Integer> expectedSPs = new ArrayList<>();
		responseMap.put("expectedSPs", expectedSPs);
		List<Integer> stackedSPs = new ArrayList<>();
		responseMap.put("stackedSPs", stackedSPs);
		List<Integer> upSPs = new ArrayList<>();
		responseMap.put("upSPs", upSPs);
		List<Integer> downSPs = new ArrayList<>();
		responseMap.put("downSPs", downSPs);

		Sprint currentSprint = sprintRepository.findBySprintNumber(
				Integer.valueOf(configurationRepository.findByKee(Configuration.CURRENT_SPRINT).getValue()));

		List<LocalDate> invalidDaysList = new ArrayList<>();
		if (currentSprint.getInvalidDays() != null && !currentSprint.getInvalidDays().isEmpty()) {
			String[] invalidDays = currentSprint.getInvalidDays().split(",");

			for (int i = 0; i < invalidDays.length; i++) {
				String[] splittedDate = invalidDays[i].split("/");
				invalidDaysList.add(LocalDate.of(Integer.valueOf(splittedDate[2]), Integer.valueOf(splittedDate[1]),
						Integer.valueOf(splittedDate[0])));
			}
		}
		invalidDaysList.add(currentSprint.getEndDate().plusDays(1));

		Iterator<LocalDate> invalidDaysIterator = invalidDaysList.iterator();

		LocalDate nextInvalidDate = invalidDaysIterator.next();

		LocalDate dayChecked = currentSprint.getStartDate();

		int plannedSP = currentSprint.getStoryPoints();
		int downSP = 0;
		int upSP = 0;

		int currentDay = 1;
		
		//At the beginning of the sprint everything is to be done.
		stackedSPs.add(currentSprint.getStoryPoints());
		downSPs.add(currentSprint.getStoryPoints());
		upSPs.add(0);
		days.add(0);

		while (invalidDaysIterator.hasNext() || dayChecked.isBefore(nextInvalidDate)) {
			// As right now there is only finished tickets, no need to check for
			// the state or anything else.
			List<SprintTicket> finishedTickets = sprintTicketRepository.findBySprintAndFinished(currentSprint,
					dayChecked);
			for (SprintTicket ticket : finishedTickets) {
				int ticketSP = 0;
				if (ticket.getAnalisisSP() != -1) {
					ticketSP = ticket.getAnalisisSP();
				}
				if (ticket.getEstimatedSP() != -1) {
					ticketSP = ticket.getEstimatedSP();
				}
				
				if (ticket.isPlanned()){
					downSP += ticketSP;
				} else {
					upSP += ticketSP;
				}
			}

			if (!DayOfWeek.SATURDAY.equals(dayChecked.getDayOfWeek())
					&& !DayOfWeek.SUNDAY.equals(dayChecked.getDayOfWeek()) && dayChecked.isBefore(nextInvalidDate)) {
				log.info("Adding '{}'", dayChecked);
				days.add(currentDay);
				currentDay++;
				stackedSPs.add(plannedSP - (downSP + upSP));
				downSPs.add(plannedSP - downSP);
				upSPs.add(upSP);
			} else {
				log.info("Day '{}' is invalid", dayChecked);
			}
			
			if (dayChecked.isEqual(nextInvalidDate)) {
				nextInvalidDate = invalidDaysIterator.next();
			}

			dayChecked = dayChecked.plusDays(1);

		}

		double sPByDay = (double) plannedSP / ((double) days.size() - 1L);

		for (int i = 0; i < days.size(); i++) {
			expectedSPs.add((int) Math.round(plannedSP - (sPByDay * i)));
		}

		return responseMap;

	}

}
