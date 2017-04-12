import { createStore, applyMiddleware } from 'redux'
import thunk from 'redux-thunk'
import fetchMiddleware from '../middleware/fetchMiddleware'
import rootReducer from '../reducers'
import { routerMiddleware } from 'react-router-redux'
import { browserHistory } from 'react-router'

export default preloadedState => (
  createStore(
    rootReducer,
    preloadedState,
    applyMiddleware(fetchMiddleware, routerMiddleware(browserHistory), thunk)
  )
)
