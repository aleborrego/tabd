import React from 'react'
import {Link} from 'react-router'

var ROOT = '/'

const Menu = (props) => (
  <div className="menu">
    <span className="header">MENU</span>
    |<Link to={ROOT+"team"}>Team</Link>
    |<Link to={ROOT+"burndown"}>Burndown chart</Link>
    {props.children}
  </div>
)

export default Menu;
