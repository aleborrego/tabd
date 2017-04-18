import React, { Component, PropTypes } from 'react'
import { connect } from 'react-redux'

import { Line } from 'react-chartjs-2';

import { fetchBurndown, goTo } from '../actions'

class Burndown extends Component {
  constructor(props) {
    super(props)
  }

  componentWillMount() {
    this.props.dispatch(fetchBurndown())
  }

  render() {
    let data = {
      labels: this.props.days,
      datasets: [
        {
          label: 'Burndown Ideal',
          fill: false,
          lineTension: 0.1,
          backgroundColor: 'rgba(132,52,37,0.4)',
          borderColor: 'rgba(132,52,37,1)',
          borderCapStyle: 'butt',
          borderDash: [],
          borderDashOffset: 0.0,
          borderJoinStyle: 'miter',
          pointBorderColor: 'rgba(132,52,37,1)',
          pointBackgroundColor: '#fff',
          pointBorderWidth: 1,
          pointHoverRadius: 5,
          pointHoverBackgroundColor: 'rgba(132,52,37,1)',
          pointHoverBorderColor: 'rgba(220,220,220,1)',
          pointHoverBorderWidth: 2,
          pointRadius: 1,
          pointHitRadius: 10,
          data: this.props.expectedSPs
        },
        {
          label: 'Burndown Real',
          fill: false,
          lineTension: 0.1,
          backgroundColor: 'rgba(0,103,163,0.4)',
          borderColor: 'rgba(0,103,163,1)',
          borderCapStyle: 'butt',
          borderDash: [],
          borderDashOffset: 0.0,
          borderJoinStyle: 'miter',
          pointBorderColor: 'rgba(0,103,163,1)',
          pointBackgroundColor: '#fff',
          pointBorderWidth: 1,
          pointHoverRadius: 5,
          pointHoverBackgroundColor: 'rgba(0,103,163,1)',
          pointHoverBorderColor: 'rgba(220,220,220,1)',
          pointHoverBorderWidth: 2,
          pointRadius: 1,
          pointHitRadius: 10,
          data: this.props.downSPs
        },
        {
          label: 'BurnUp',
          fill: false,
          lineTension: 0.1,
          backgroundColor: 'rgba(179,123,44,0.4)',
          borderColor: 'rgba(242,214,0,1)',
          borderCapStyle: 'butt',
          borderDash: [],
          borderDashOffset: 0.0,
          borderJoinStyle: 'miter',
          pointBorderColor: 'rgba(242,214,0,1)',
          pointBackgroundColor: '#fff',
          pointBorderWidth: 1,
          pointHoverRadius: 5,
          pointHoverBackgroundColor: 'rgba(242,214,0,1)',
          pointHoverBorderColor: 'rgba(220,220,220,1)',
          pointHoverBorderWidth: 2,
          pointRadius: 1,
          pointHitRadius: 10,
          data: this.props.upSPs
        },
        {
          label: 'Stacked (Burndown+BurnUp)',
          fill: false,
          lineTension: 0.1,
          backgroundColor: 'rgba(0,0,0,0.4)',
          borderColor: 'rgba(0,0,0,1)',
          borderCapStyle: 'butt',
          borderDash: [],
          borderDashOffset: 0.0,
          borderJoinStyle: 'miter',
          pointBorderColor: 'rgba(0,0,0,1)',
          pointBackgroundColor: '#fff',
          pointBorderWidth: 1,
          pointHoverRadius: 5,
          pointHoverBackgroundColor: 'rgba(0,0,0,1)',
          pointHoverBorderColor: 'rgba(220,220,220,1)',
          pointHoverBorderWidth: 2,
          pointRadius: 1,
          pointHitRadius: 10,
          data: this.props.stackedSPs
        }
      ]
    }
    return (
      <div>
        <h2>Burndown Chart</h2>
        <Line data={data} />
      </div>
    )
  }
}

Burndown.propTypes = {
  days: PropTypes.array.isRequired,
  expectedSPs: PropTypes.array.isRequired,
  realSPs: PropTypes.array.isRequired,
  isFetching: PropTypes.bool.isRequired,
  dispatch: PropTypes.func.isRequired
}

const mapStateToProps = state => {
  const { burndown } = state
  return  {...burndown }
}

export default connect(mapStateToProps)(Burndown)
