import * as ActionTypes from '../actions'


const DEFAULT_PROPERTIES = {
  messages: []
}

export const message = (state = DEFAULT_PROPERTIES , action) => {
  switch (action.type) {
    case ActionTypes.SHOW_MESSAGE:
    return {
      ...state,
      messages: state.messages.concat(
        {message: action.payload.message,
        type: action.payload.type})
    }
    case ActionTypes.HIDE_MESSAGE:
      let messages = []
      let removed = false
      for (let i = 0; i < state.messages.length; i++) {
        if (removed || state.messages[i].message !== action.payload.message || state.messages[i].type !== action.payload.type) {
          messages.push({...state.messages[i]})
        } else {
          removed = true
        }
      }
      return {
        ...state,
        messages
      }
    case ActionTypes.CLEAR_STATE:
      return DEFAULT_PROPERTIES
    default:
      return state
  }
}
