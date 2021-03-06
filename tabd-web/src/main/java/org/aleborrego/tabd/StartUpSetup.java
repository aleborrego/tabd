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
package org.aleborrego.tabd;

import java.time.LocalDate;

import javax.transaction.Transactional;

import org.aleborrego.tabd.domain.Configuration;
import org.aleborrego.tabd.domain.Metric;
import org.aleborrego.tabd.domain.Sprint;
import org.aleborrego.tabd.domain.State;
import org.aleborrego.tabd.domain.repository.ConfigurationRepository;
import org.aleborrego.tabd.domain.repository.MetricRepository;
import org.aleborrego.tabd.domain.repository.SprintRepository;
import org.aleborrego.tabd.domain.repository.StateRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;

import lombok.extern.slf4j.Slf4j;

/**
 * Class that initializes data for the platform.
 */
@org.springframework.context.annotation.Configuration
@Slf4j
public class StartUpSetup implements CommandLineRunner {

	@Autowired
	private ConfigurationRepository configurationRepository;

	@Autowired
	private TabdConfigurationProperties tabdConfigurationProperties;

	@Autowired
	private SprintRepository sprintRepository;

	@Autowired
	private StateRepository stateRepository;

	@Autowired
	private MetricRepository metricRepository;

	/**
	 * Event triggered at the initialization of the platform. It is used to set
	 * the data.
	 */
	@Override
	@Transactional
	public void run(String... arg0) {

		// Setting Extra fields (They do not change when cloning boards)

		Configuration estimacionConfiguration = configurationRepository.findByKee(Configuration.ESTIMACION_FIELD);
		if (estimacionConfiguration == null) {
			log.info("Creating configuration for estimacion field");
			estimacionConfiguration = new Configuration();
			estimacionConfiguration.setKee(Configuration.ESTIMACION_FIELD).setValue("BPvzsUpp-ydaJ1L");
			configurationRepository.save(estimacionConfiguration);
		}

		Configuration analisisConfiguration = configurationRepository.findByKee(Configuration.ANALISIS_FIELD);
		if (analisisConfiguration == null) {
			log.info("Creating configuration for analisis field");
			analisisConfiguration = new Configuration();
			analisisConfiguration.setKee(Configuration.ANALISIS_FIELD).setValue("BPvzsUpp-VsaA6F");
			configurationRepository.save(analisisConfiguration);
		}

		Configuration trabajoRealConfiguration = configurationRepository.findByKee(Configuration.TRABAJO_REAL_FIELD);
		if (trabajoRealConfiguration == null) {
			log.info("Creating configuration for team");
			trabajoRealConfiguration = new Configuration();
			trabajoRealConfiguration.setKee(Configuration.TRABAJO_REAL_FIELD).setValue("BPvzsUpp-GgThq8");
			configurationRepository.save(trabajoRealConfiguration);
		}

		Configuration trabajoAnteriorConfiguration = configurationRepository
				.findByKee(Configuration.TRABAJO_REAL_FIELD);
		if (trabajoAnteriorConfiguration == null) {
			log.info("Creating configuration for team");
			trabajoAnteriorConfiguration = new Configuration();
			trabajoAnteriorConfiguration.setKee(Configuration.TRABAJO_REAL_FIELD).setValue("JAgXO4tv-HEBfFm");
			configurationRepository.save(trabajoAnteriorConfiguration);
		}

		// Team should not change either
		Configuration teamConfiguration = configurationRepository.findByKee(Configuration.TEAM_KEE);
		if (teamConfiguration == null) {
			log.info("Creating configuration for team");
			teamConfiguration = new Configuration();
			teamConfiguration.setKee(Configuration.TEAM_KEE).setValue(tabdConfigurationProperties.getTeam());
			configurationRepository.save(teamConfiguration);
		}

		// Setting other configs from file...
		Configuration trelloAppKey = configurationRepository.findByKee(Configuration.APP_KEE);
		if (trelloAppKey == null) {
			log.info("Creating configuration for app key");
			trelloAppKey = new Configuration();
			trelloAppKey.setKee(Configuration.APP_KEE).setValue(tabdConfigurationProperties.getAppKey());
			configurationRepository.save(trelloAppKey);
		}

		Configuration trelloAuthKey = configurationRepository.findByKee(Configuration.AUTH_KEE);
		if (trelloAuthKey == null) {
			log.info("Creating configuration for user token");
			trelloAuthKey = new Configuration();
			trelloAuthKey.setKee(Configuration.AUTH_KEE).setValue(tabdConfigurationProperties.getAuthKey());
			configurationRepository.save(trelloAuthKey);
		}

		// TODO check for startDate
		Configuration currentSprint = configurationRepository.findByKee(Configuration.CURRENT_SPRINT);
		if (currentSprint == null) {
			log.info("Creating configuration for team");
			currentSprint = new Configuration();
			currentSprint.setKee(Configuration.CURRENT_SPRINT).setValue("-1");
		}

		if (!currentSprint.getValue().equals(tabdConfigurationProperties.getCurrentSprint())) {
			currentSprint.setValue(tabdConfigurationProperties.getCurrentSprint());
			configurationRepository.save(currentSprint);

			String[] startDate = tabdConfigurationProperties.getSprintStartDate().split("/");
			String[] endDate = tabdConfigurationProperties.getSprintEndDate().split("/");

			Sprint sprint = new Sprint();
			sprint.setBoard(tabdConfigurationProperties.getSprintBoard())
					.setSprintNumber(Integer.valueOf(currentSprint.getValue()))
					.setStartDate(LocalDate.of(Integer.valueOf(startDate[2]), Integer.valueOf(startDate[1]),
							Integer.valueOf(startDate[0])))
					.setEndDate(LocalDate.of(Integer.valueOf(endDate[2]), Integer.valueOf(endDate[1]),
							Integer.valueOf(endDate[0])))
					.setInvalidDays(tabdConfigurationProperties.getNotWorkingDays())
					.setStoryPoints(Integer.valueOf(tabdConfigurationProperties.getStoryPoints()));

			sprintRepository.save(sprint);
		}

		State state = stateRepository.findByName("Backlog Sprint");
		if (state == null) {
			state = new State();
			state.setName("Backlog Sprint").setIsFinal(Boolean.FALSE).setIsStarted(Boolean.FALSE)
					.setIsSprintRelated(Boolean.TRUE);
			stateRepository.save(state);
		}

		state = stateRepository.findByName("Sprint Analizando");
		if (state == null) {
			state = new State();
			state.setName("Sprint Analizando").setIsFinal(Boolean.FALSE).setIsStarted(Boolean.FALSE)
					.setIsSprintRelated(Boolean.TRUE);
			stateRepository.save(state);
		}

		state = stateRepository.findByName("En desarrollo");
		if (state == null) {
			state = new State();
			state.setName("En desarrollo").setIsFinal(Boolean.FALSE).setIsStarted(Boolean.TRUE)
					.setIsSprintRelated(Boolean.TRUE);
			stateRepository.save(state);
		}

		state = stateRepository.findByName("En pruebas");
		if (state == null) {
			state = new State();
			state.setName("En pruebas").setIsFinal(Boolean.FALSE).setIsStarted(Boolean.TRUE)
					.setIsSprintRelated(Boolean.TRUE);
			stateRepository.save(state);
		}

		state = stateRepository.findByName("Terminadas");
		if (state == null) {
			state = new State();
			state.setName("Terminadas").setIsFinal(Boolean.TRUE).setIsStarted(Boolean.TRUE)
					.setIsSprintRelated(Boolean.TRUE);
			stateRepository.save(state);
		}

		state = stateRepository.findByName("Backlog No Estimadas");
		if (state == null) {
			state = new State();
			state.setName("Backlog No Estimadas").setIsFinal(Boolean.FALSE).setIsStarted(Boolean.FALSE)
					.setIsSprintRelated(Boolean.FALSE);
			stateRepository.save(state);
		}

		state = stateRepository.findByName("Backlog Estimadas");
		if (state == null) {
			state = new State();
			state.setName("Backlog Estimadas").setIsFinal(Boolean.FALSE).setIsStarted(Boolean.FALSE)
					.setIsSprintRelated(Boolean.FALSE);
			stateRepository.save(state);
		}

		Metric metric = metricRepository.findByName("EstimadasTerminadas");
		if (metric == null) {
			metric = new Metric();
			metric.setName("EstimadasTerminadas").setVar("ET")
					.setExpression("{\"fieldType\":\"estimated\",\"stateFinished\":\"yes\"}");
			metricRepository.save(metric);
		}

		metric = metricRepository.findByName("RealTerminadas");
		if (metric == null) {
			metric = new Metric();
			metric.setName("RealTerminadas").setVar("RT")
					.setExpression("{\"fieldType\":\"worked\",\"stateFinished\":\"yes\"}");
			metricRepository.save(metric);
		}

		metric = metricRepository.findByName("EstimadasEmpezadas");
		if (metric == null) {
			metric = new Metric();
			metric.setName("Estimadas").setVar("EE")
					.setExpression("{\"fieldType\":\"estimated\",\"stateStarted\":\"yes\"}");
			metricRepository.save(metric);
		}

		metric = metricRepository.findByName("RealEmpezadas");
		if (metric == null) {
			metric = new Metric();
			metric.setName("Real").setVar("RE").setExpression("{\"fieldType\":\"worked\",\"stateStarted\":\"yes\"}");
			metricRepository.save(metric);
		}

		metric = metricRepository.findByName("EstimadasPlanificadasTerminadas");
		if (metric == null) {
			metric = new Metric();
			metric.setName("EstimadasPlanificadasTerminadas").setVar("EPT")
					.setExpression("{\"fieldType\":\"estimated\",\"cardPlanned\":\"yes\",\"stateFinished\":\"yes\"}");
			metricRepository.save(metric);
		}

		metric = metricRepository.findByName("RealPlanificadasTerminadas");
		if (metric == null) {
			metric = new Metric();
			metric.setName("RealPlanificadasTerminadas").setVar("RPT")
					.setExpression("{\"fieldType\":\"worked\",\"cardPlanned\":\"yes\",\"stateFinished\":\"yes\"}");
			metricRepository.save(metric);
		}

		metric = metricRepository.findByName("EstimadasNoPlanificadasTerminadas");
		if (metric == null) {
			metric = new Metric();
			metric.setName("EstimadasNoPlanificadasTerminadas").setVar("ENT")
					.setExpression("{\"fieldType\":\"estimated\",\"cardPlanned\":\"no\",\"stateFinished\":\"yes\"}");
			metricRepository.save(metric);
		}

		metric = metricRepository.findByName("RealNoPlanificadasTerminadas");
		if (metric == null) {
			metric = new Metric();
			metric.setName("RealNoPlanificadasTerminadas").setVar("RNT")
					.setExpression("{\"fieldType\":\"worked\",\"cardPlanned\":\"no\",\"stateFinished\":\"yes\"}");
			metricRepository.save(metric);
		}

	}
}