import { routerReducer as routing } from 'react-router-redux'
import { combineReducers } from 'redux'
import * as teamMembers from './teamMembers'
import * as home from './home'

const rootReducer = combineReducers({
  ...teamMembers,
  ...home,
  routing
})

export default rootReducer
