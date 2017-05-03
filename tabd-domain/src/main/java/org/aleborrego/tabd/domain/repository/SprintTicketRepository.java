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
package org.aleborrego.tabd.domain.repository;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

import org.aleborrego.tabd.domain.Sprint;
import org.aleborrego.tabd.domain.SprintTicket;
import org.aleborrego.tabd.domain.State;
import org.aleborrego.tabd.domain.Ticket;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Component;

/**
 * Repository for {@link SprintTicket}.
 * 
 * @author aleborrego
 *
 */
@Component
public interface SprintTicketRepository extends JpaRepository<SprintTicket, UUID> {

	public SprintTicket findBySprintAndTicketAndTrelloCardId(Sprint sprint, Ticket ticket, String trelloCardId);

	public List<SprintTicket> findBySprintAndUpdatedAndStateIsFinal(Sprint sprint, LocalDate updated, Boolean isFinal);

	public List<SprintTicket> findBySprintAndUpdatedAndStateIsStarted(Sprint sprint, LocalDate updated,
			Boolean isStarted);

	public List<SprintTicket> findBySprintAndUpdatedAndStateIn(Sprint sprint, LocalDate updated, List<State> states);

	public List<SprintTicket> findBySprintAndPlannedAndStateName(Sprint sprint, boolean planned, String name);

	public List<SprintTicket> findBySprintAndStateName(Sprint sprint, String name);

	public List<SprintTicket> findBySprintAndPlannedAndStateIn(Sprint sprint, boolean planned, List<State> states);

	public List<SprintTicket> findBySprintAndStateIn(Sprint sprint, List<State> states);
}
