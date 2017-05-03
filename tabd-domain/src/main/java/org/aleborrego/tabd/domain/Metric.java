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

import java.util.UUID;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;

import org.hibernate.annotations.GenericGenerator;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import lombok.experimental.Accessors;

@NoArgsConstructor
@ToString
@EqualsAndHashCode(exclude = { "id" })
@Accessors(chain = true)
@Entity
public class Metric {
	
	public final static String METRIC_ESTIMATED = "estimated";
	public final static String METRIC_WORKED = "worked";

	@Id
	@GeneratedValue(generator = "uuid2")
	@GenericGenerator(name = "uuid2", strategy = "uuid2")
	// @Column(columnDefinition = "BINARY(16)")
	@Getter
	private UUID id;

	@Getter
	@Setter
	private String var;

	@Getter
	@Setter
	private String name;

	/**
	 * Represents an arithmetic expression. {"var": "name"} to use the result from
	 * another metric.
	 * {"fieldType": "estimated|worked","cardPlanned": "yes|no","state"="state","stateStarted": "yes|no", "stateFinished": "yes|no"}
	 * to evaluate fields in a card.
	 * Order of precedence:
	 * 		var
	 * 		state
	 * 		stateFinihed
	 * 		stateStarted
	 */
	@Getter
	@Setter
	private String expression;

}
