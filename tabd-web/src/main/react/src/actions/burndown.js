export const REQUEST_BURNDOWN = 'REQUEST_BURNDOWN'
export const RECEIVE_BURNDOWN = 'RECEIVE_BURNDOWN'

import { showMessageWithTimeOut } from '.'

export const fetchBurndown = () => ({
  type: REQUEST_BURNDOWN,
  remote: {
    endpoint: 'api/burndown',
    method: 'GET',
    success: RECEIVE_BURNDOWN,
    error: showMessageWithTimeOut('error', 'Error retrieving burndown data')
  },
  payload : { }
})
