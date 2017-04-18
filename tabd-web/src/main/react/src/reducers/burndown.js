import * as ActionTypes from '../actions'


const DEFAULT_PROPERTIES = {
  isFetching: false,
  isEditing: -1,
  days: [],
  expectedSPs: [],
  stackedSPs: [],
  downSPs: [],
  upSPs: []
}

export const burndown = (state = DEFAULT_PROPERTIES , action) => {
  switch (action.type) {
    case ActionTypes.REQUEST_BURNDOWN:
    return {
      ...state,
      isEditing: -1,
      isFetching: true
    }
    case ActionTypes.RECEIVE_BURNDOWN:
      return {
        ...state,
        isFetching: false,
        days : action.payload.response.days,
        expectedSPs : action.payload.response.expectedSPs,
        stackedSPs : action.payload.response.stackedSPs,
        downSPs : action.payload.response.downSPs,
        upSPs : action.payload.response.upSPs
      }
    default:
      return state
  }
}
