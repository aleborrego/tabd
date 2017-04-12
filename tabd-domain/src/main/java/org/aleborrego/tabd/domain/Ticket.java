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
package org.aleborrego.tabd.domain;

import java.util.List;
import java.util.UUID;

import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.OneToMany;

import org.hibernate.annotations.GenericGenerator;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import lombok.Setter;
import lombok.ToString;
import lombok.experimental.Accessors;

/**
 * Entity representing a ticket.
 * 
 * @author aleborrego
 *
 */
@NoArgsConstructor
@ToString
@EqualsAndHashCode(exclude = { "id" })
@Accessors(chain = true)
@Entity
public class Ticket {

	@Id
	@GeneratedValue(generator = "uuid2")
	@GenericGenerator(name = "uuid2", strategy = "uuid2")
	// @Column(columnDefinition = "BINARY(16)")
	@Getter
	private UUID id;

	/**
	 * Issue identifier in redmine. In trello represented by 'xxxx': on the
	 * title.
	 */
	@Getter
	@Setter
	@NonNull
	private String issueId;

	/**
	 * Title of the issue. Part after issueId.
	 */
	@Getter
	@Setter
	@NonNull
	private String title;

	/**
	 * Current state of the ticket.
	 */
	@Getter
	@Setter
	@NonNull
	private State currentState;

	/**
	 * Story points estimated (Analysis+Development)
	 */
	@Getter
	@Setter
	@NonNull
	private Integer spOriginalEstimation;

	/**
	 * Story points dedicated (Analysis+Development)
	 */
	@Getter
	@Setter
	@NonNull
	private Integer spDedicated;

	/**
	 * Ticket relation to sprints.
	 */
	@Getter
	@Setter
	@OneToMany(cascade = CascadeType.ALL, mappedBy = "ticket")
	private List<SprintTicket> sprintTickets;

}
