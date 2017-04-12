export const REQUEST_TEAM_MEMBERS = 'REQUEST_TEAM_MEMBERS'
export const RECEIVE_TEAM_MEMBERS = 'RECEIVE_TEAM_MEMBERS'

import { showMessageWithTimeOut } from '.'

export const fetchTeamMembers = () => ({
  type: REQUEST_TEAM_MEMBERS,
  remote: {
    endpoint: 'api/teamMembers',
    method: 'GET',
    success: RECEIVE_TEAM_MEMBERS,
    error: showMessageWithTimeOut('error', 'Error retrieving team members')
  },
  payload : { }
})
