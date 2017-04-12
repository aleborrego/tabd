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

import java.time.LocalDate;
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
import lombok.Setter;
import lombok.ToString;
import lombok.experimental.Accessors;

/**
 * Entity representing a Sprint.
 * 
 * @author aleborrego
 *
 */
@NoArgsConstructor
@ToString
@EqualsAndHashCode(exclude = { "id" })
@Accessors(chain = true)
@Entity
public class Sprint {

	@Id
	@GeneratedValue(generator = "uuid2")
	@GenericGenerator(name = "uuid2", strategy = "uuid2")
	// @Column(columnDefinition = "BINARY(16)")
	@Getter
	private UUID id;

	@Getter
	@Setter
	private int sprintNumber;

	@Getter
	@Setter
	private String board;

	@Getter
	@Setter
	private LocalDate startDate;

	@Getter
	@Setter
	private LocalDate endDate;

	@Getter
	@Setter
	private LocalDate lastAnalizedDate;

	@Getter
	@Setter
	private String invalidDays;

	@Getter
	@Setter
	private int storyPoints;

	@Getter
	@Setter
	@OneToMany(cascade = CascadeType.ALL, mappedBy = "sprint")
	private List<SprintMember> sprintMembers;

	@Getter
	@Setter
	@OneToMany(cascade = CascadeType.ALL, mappedBy = "sprint")
	private List<SprintTicket> sprintTickets;
}
