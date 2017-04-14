import { routerReducer as routing } from 'react-router-redux'
import { combineReducers } from 'redux'
import * as teamMembers from './teamMembers'
import * as burndown from './burndown'
import * as home from './home'

const rootReducer = combineReducers({
  ...teamMembers,
  ...burndown,
  ...home,
  routing
})

export default rootReducer
