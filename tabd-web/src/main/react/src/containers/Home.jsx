import React, { Component, PropTypes } from 'react'
import { connect } from 'react-redux'

import Menu from '../components/Menu'

class Home extends Component {
  componentWillMount() {
    this.props.dispatch({type: 'init'})
  }

  render (){
    let messages = [<span style={{display: 'none'}} />]

    if (this.props.messages){
      this.props.messages.forEach((message)=>{
        messages.push(<div className={"message "+message.type} >{message.message}</div>)
      })
    }
    return (
      <div>
        <div className="messagePanel">{messages}</div>
        <div className="central">
          <Menu />
          {this.props.children}
        </div>
      </div>
    )
  }
}

Home.propTypes = {
  dispatch: PropTypes.func.isRequired,
  messages: PropTypes.array
}

const mapStateToProps = state => {
  const { message } = state
  return  { ...message }
}

export default connect(mapStateToProps)(Home);
