import React from 'react'
import { Route, IndexRoute } from 'react-router'
import Home from '../containers/Home'
import Body from '../components/Body'
import TeamMembers from '../containers/TeamMembers'
import Burndown from '../containers/Burndown'

var ROOT = '/'

export default (
  <Route path={ROOT} component={Home}>
    <IndexRoute component={Body}/>
    <Route path={ROOT+"team"} component={TeamMembers} />
    <Route path={ROOT+"burndown"} component={Burndown} />
  </Route>
)
