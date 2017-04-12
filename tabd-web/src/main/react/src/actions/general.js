export const SHOW_MESSAGE = 'SHOW_MESSAGE'
export const HIDE_MESSAGE = 'HIDE_MESSAGE'

export const CLEAR_STATE = 'CLEAR_STATE'

import { replace } from 'react-router-redux'

var ROOT = '/'

export const showMessage = (type, message) => ({
  type: SHOW_MESSAGE,
  payload : {
    type,
    message
  }
})

export const hideMessage = (type, message) => ({
  type: HIDE_MESSAGE,
  payload : {
    type,
    message
  }
})

export const showMessageWithTimeOut = (type, message, timeout = 3000) => dispatch => {
  dispatch(showMessage(type, message))
  setTimeout(function() {
      return dispatch(hideMessage(type, message))
    }, timeout)
}

export const clearState = () => dispatch => {
  dispatch(replace(ROOT))
  return(dispatch({
    type: CLEAR_STATE,
    payload: {}
  }))
}

export const goTo = (url) => dispatch => {
  return dispatch(replace(ROOT + url))
}
