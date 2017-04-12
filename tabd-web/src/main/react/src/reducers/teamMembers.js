import * as ActionTypes from '../actions'


const DEFAULT_PROPERTIES = {
  isFetching: false,
  isEditing: -1,
  items: []
}

export const teamMembers = (state = DEFAULT_PROPERTIES , action) => {
  switch (action.type) {
    case ActionTypes.REQUEST_TEAM_MEMBERS:
    return {
      ...state,
      isEditing: -1,
      isFetching: true
    }
    case ActionTypes.RECEIVE_TEAM_MEMBERS:
      return {
        ...state,
        isFetching: false,
        items : action.payload.response._embedded.teamMembers
      }
    default:
      return state
  }
}
