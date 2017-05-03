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
package org.aleborrego.tabd.loader.calculations;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.transaction.Transactional;

import org.aleborrego.tabd.domain.Configuration;
import org.aleborrego.tabd.domain.Measure;
import org.aleborrego.tabd.domain.Metric;
import org.aleborrego.tabd.domain.Sprint;
import org.aleborrego.tabd.domain.SprintTicket;
import org.aleborrego.tabd.domain.State;
import org.aleborrego.tabd.domain.repository.ConfigurationRepository;
import org.aleborrego.tabd.domain.repository.MeasureRepository;
import org.aleborrego.tabd.domain.repository.MetricRepository;
import org.aleborrego.tabd.domain.repository.SprintRepository;
import org.aleborrego.tabd.domain.repository.SprintTicketRepository;
import org.aleborrego.tabd.domain.repository.StateRepository;
import org.aleborrego.tabd.loader.Loader;
import org.aleborrego.tabd.loader.LoaderException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.expression.ExpressionParser;
import org.springframework.expression.spel.standard.SpelExpressionParser;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@NoArgsConstructor
@Component
@Slf4j
public class MeasureLoader implements Loader {

	private final static String START_EXPRESSION = "{";
	private final static String END_EXPRESSION = "}";
	private final static String VAR_EXPRESSION = "var";
	private final static String STATE_EXPRESSION = "state";
	private final static String FINISHED_EXPRESSION = "stateFinished";
	private final static String STARTED_EXPRESSION = "stateStarted";
	private final static String PLANNED_EXPRESSION = "cardPlanned";
	private final static String FIELD_EXPRESSION = "fieldType";

	@Autowired
	private MetricRepository metricRepository;

	@Autowired
	private MeasureRepository measureRepository;

	@Autowired
	private ConfigurationRepository configurationRepository;

	@Autowired
	private SprintRepository sprintRepository;

	@Autowired
	private SprintTicketRepository sprintTicketRepository;

	@Autowired
	private StateRepository stateRepository;

	@Transactional
	@Override
	public void load(String... arguments) throws LoaderException {
		log.info("Loading current sprint");
		Sprint sprint = sprintRepository.findBySprintNumber(
				Integer.valueOf(configurationRepository.findByKee(Configuration.CURRENT_SPRINT).getValue()));
		if (sprint != null) {

			for (Metric metric : metricRepository.findAll()) {
				try {
					Measure measure = measureRepository.findBySprintAndMetric(sprint, metric);
					if (measure == null) {
						measure = new Measure();
						measure.setMetric(metric).setSprint(sprint);
					}
					measure.setValue(calculateExpression(metric.getExpression(), sprint));
					measureRepository.save(measure);
				} catch (LoaderException e) {
					log.error("Exception calculating metric '{}'. It wont be stored", metric.getName(), e);
				}
			}
		}

	}

	private Double calculateExpression(String expression, Sprint sprint) throws LoaderException {
		String expressionToCalculate = expression;
		while (expressionToCalculate.contains(START_EXPRESSION)) {
			expressionToCalculate = replaceOneExpression(expressionToCalculate, sprint);
		}

		Double returnValue;
		ExpressionParser parser = new SpelExpressionParser();
		Object value = parser.parseExpression(expressionToCalculate).getValue();
		if (value instanceof Double || value instanceof Float) {
			returnValue = (Double) value;
		} else if (value instanceof Float) {
			returnValue = ((Float) value).doubleValue();
		} else if (value instanceof Integer) {
			returnValue = ((Integer) value).doubleValue();
		} else if (value instanceof Long) {
			returnValue = ((Long) value).doubleValue();
		} else {
			throw new LoaderException("Invalid Expression");
		}
		return returnValue;
	}

	private String replaceOneExpression(String expression, Sprint sprint) throws LoaderException {
		String returnExpression = null;
		try {
			String replaceExpression = expression.substring(expression.indexOf(START_EXPRESSION));
			replaceExpression = replaceExpression.substring(0,
					replaceExpression.indexOf(END_EXPRESSION) + END_EXPRESSION.length());

			ObjectMapper mapper = new ObjectMapper();

			Map<String, String> map = new HashMap<>();
			map = mapper.readValue(replaceExpression.replaceAll("'", "\""), new TypeReference<Map<String, String>>() {
			});

			replaceExpression = replaceExpression.replaceAll("\\{", "\\\\{");

			String var = map.get(VAR_EXPRESSION);
			String state = map.get(STATE_EXPRESSION);
			String finished = map.get(FINISHED_EXPRESSION);
			String started = map.get(STARTED_EXPRESSION);
			String planned = map.get(PLANNED_EXPRESSION);
			String field = map.get(FIELD_EXPRESSION);
			if (var != null) {
				returnExpression = expression.replaceAll(replaceExpression, measureRepository
						.findBySprintAndMetric(sprint, metricRepository.findByVar(var)).getValue().toString());
			} else if (state != null) {
				int sumatory = 0;
				List<SprintTicket> tickets = null;
				if (planned != null) {
					tickets = sprintTicketRepository.findBySprintAndPlannedAndStateName(sprint,
							"yes".equals(planned), state);
				} else {
					tickets = sprintTicketRepository.findBySprintAndStateName(sprint, state);
				}
				for (SprintTicket ticket : tickets) {
					if (Metric.METRIC_ESTIMATED.equals(field)) {
						sumatory += ticket.getEstimatedSP();
					} else if (Metric.METRIC_WORKED.equals(field)) {
						sumatory += ticket.getWorkedSP();
					} else {
						throw new LoaderException("Invalid field");
					}
				}
				returnExpression = expression.replaceAll(replaceExpression, String.valueOf(sumatory));
			} else if (finished != null) {
				int sumatory = 0;
				List<State> states = stateRepository.findByIsFinal("yes".equals(finished));
				List<SprintTicket> tickets = null;
				if (planned != null) {
					tickets = sprintTicketRepository.findBySprintAndPlannedAndStateIn(sprint,
							"yes".equals(planned), states);
				} else {
					tickets = sprintTicketRepository.findBySprintAndStateIn(sprint, states);
				}
				for (SprintTicket ticket : tickets) {
					if (Metric.METRIC_ESTIMATED.equals(field)) {
						sumatory += ticket.getEstimatedSP();
					} else if (Metric.METRIC_WORKED.equals(field)) {
						sumatory += ticket.getWorkedSP();
					} else {
						throw new LoaderException("Invalid field");
					}
				}
				returnExpression = expression.replaceAll(replaceExpression, String.valueOf(sumatory));
			} else if (started != null) {
				int sumatory = 0;
				List<State> states = stateRepository.findByIsStarted("yes".equals(started));
				List<SprintTicket> tickets = null;
				if (planned != null) {
					tickets = sprintTicketRepository.findBySprintAndPlannedAndStateIn(sprint,
							"yes".equals(planned), states);
				} else {
					tickets = sprintTicketRepository.findBySprintAndStateIn(sprint, states);
				}
				for (SprintTicket ticket : tickets) {
					if (Metric.METRIC_ESTIMATED.equals(field)) {
						sumatory += ticket.getEstimatedSP();
					} else if (Metric.METRIC_WORKED.equals(field)) {
						sumatory += ticket.getWorkedSP();
					} else {
						throw new LoaderException("Invalid field");
					}
				}
				returnExpression = expression.replaceAll(replaceExpression, String.valueOf(sumatory));
			} else {
				throw new LoaderException("Invalid metric");
			}

		} catch (IOException e) {
			throw new LoaderException("Error parsing expression", e);
		}

		return returnExpression;
	}

}
