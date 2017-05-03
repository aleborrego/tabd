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
package org.aleborrego.tabd.scheduled;

import org.aleborrego.tabd.domain.ScheduledTask;
import org.aleborrego.tabd.domain.repository.ScheduledTaskRepository;
import org.aleborrego.tabd.loader.Loader;
import org.aleborrego.tabd.loader.LoaderException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import lombok.extern.slf4j.Slf4j;

@Component
@Slf4j
public class TasksRunner {

	@Autowired
	private ApplicationContext applicationContext;

	@Autowired
	private ScheduledTaskRepository scheduledTaskRepository;

	@Scheduled(fixedRate = 60000)
	public void checkLoader() throws LoaderException {
		for (ScheduledTask scheduledTask : scheduledTaskRepository.findAll()) {
			if (ScheduledTask.LOADER_TYPE.equals(scheduledTask.getType())) {

				String beanName = scheduledTask.getExtraInfo();
				Loader loader = (Loader) applicationContext.getBean(beanName);
				log.info("Loading from '{}'", beanName);
				loader.load();

				scheduledTaskRepository.delete(scheduledTask);
			}
		}
	}

}
