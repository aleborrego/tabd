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

import java.util.List;

import javax.transaction.Transactional;

import org.aleborrego.tabd.domain.Configuration;
import org.aleborrego.tabd.domain.TeamMember;
import org.aleborrego.tabd.domain.repository.ConfigurationRepository;
import org.aleborrego.tabd.domain.repository.TeamMemberRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.julienvey.trello.domain.Member;

import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@NoArgsConstructor
@Component
@Slf4j
public class TeamLoader extends TrelloLoader {

	@Autowired
	private ConfigurationRepository configurationRepository;

	@Autowired
	private TeamMemberRepository teamMemberRepository;

	@Override
	@Transactional
	public void load(String... arguments) throws LoaderException {
		if (this.getTrello() == null) {
			initTrello();
		}
		log.info("Loading team Members");
		Configuration teamKey = configurationRepository.findByKee(Configuration.TEAM_KEE);

		if (teamKey != null) {
			List<Member> members = this.getTrello().getOrganizationMembers(teamKey.getValue());
			for (Member member : members) {
				TeamMember teamMember = teamMemberRepository.findByUserName(member.getUsername());
				if (teamMember == null) {
					teamMember = new TeamMember();
					teamMember.setName(member.getFullName()).setUserName(member.getUsername());
					teamMemberRepository.save(teamMember);
					log.info("Team member {} created", teamMember);
				} else if (log.isDebugEnabled()) {
					log.debug("Team member {} already on DB", teamMember);
				}
			}
		} else {
			throw new LoaderException("Team not configured");
		}

	}

}
