import React, { Component, PropTypes } from 'react'
import { connect } from 'react-redux'
import { fetchTeamMembers, goTo } from '../actions'

class TeamMembers extends Component {
  constructor(props) {
    super(props)
  }

  componentWillMount() {
    this.props.dispatch(fetchTeamMembers())
  }

  render() {
    var title = 'Team Members:'
    var propertyComponents = [];
    if (this.props.items){
      this.props.items.map(teamMember => {
        propertyComponents.push(<div>
          Name: {teamMember.name}
          UserName: {teamMember.userName}
        </div>)
      })
    } else {
      propertyComponents.push(<span style={{display: 'none'}} />)
    }
    return (
      <div className="teamMembers">
        <p>{title}</p>
        {propertyComponents}
      </div>
    )
  }
}

TeamMembers.propTypes = {
  items: PropTypes.array.isRequired,
  isFetching: PropTypes.bool.isRequired,
  dispatch: PropTypes.func.isRequired
}

const mapStateToProps = state => {
  const { teamMembers } = state
  return  {...teamMembers }
}

export default connect(mapStateToProps)(TeamMembers)
