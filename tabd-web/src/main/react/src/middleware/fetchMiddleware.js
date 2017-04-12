import { showMessageWithTimeOut, goTo, clearState } from '../actions'

var getCookie = (name) => {
  var value = "; " + document.cookie;
  var parts = value.split("; " + name + "=");
  return (parts.length >= 2)?parts.pop().split(";").shift(): '';
}
var ROOT = (process.env.NODE_ENV === 'production')? getCookie('Context-path')+'/' :'/'

/**
 * Action: {
 *   type: --
 *   remote: {
 *     endpoint: String {finalpartOftheURL}
 *     method: String {GET,POST,PATCH,PUT,DELETE}
 *     extraParams: Object{}
 *     postData: String|Object
 *     contentType: String
 *     id: String
 *     success: string ActionType | function {action creator using thunkmiddleware} | object {action}
 *     error: string ActionType | function {action creator using thunkmiddleware} | object {action}
 *     silent: boolean {true to not throw generic errors}
 *   }
 *   payload: {
 *     --
 *   }
 * }
 *
**/
export default store => next => action => {
  var { remote: async, ...sync } = action
  if (!async){
    return next(action)
  } else {
    let { endpoint, method, extraParams, postData, id, success, error, contentType, silent } = async
    let url = ROOT + endpoint
    if (method === 'PUT' || method === 'PATCH' || method === 'DELETE'){
      url=url+'/'+id
    }
    if (extraParams){
      url = url + '?'
      for ( const key in extraParams ) {
        url=url+key+'='+extraParams[key]+'&'
      }
    }

    let config = {
      method: method,
      credentials: 'include',
      headers: {
          'Content-Type' : contentType || 'application/json',
          'Cache-Control' : 'no-store'
        }
    }
    if (method === 'POST' || method === 'PUT' || method === 'PATCH'){
      config.body = (typeof postData == 'object')? JSON.stringify(postData): postData
    }
    next(sync)
    let payload = sync.payload
    window.fetch(url,config)
      .then(response => {
        if (!response.ok) {
          return Promise.reject(response)
        }
        response.text().then(text => {
          let response;
          try {
            response = JSON.parse(text)
          } catch(error){
            response = text
          }
          if(typeof success == 'string'){
            next({...sync, type: success, payload:{...payload, response}})
          } else if(typeof success == 'function' ){
            next (success)
          } else if (typeof success == 'object'){
            next({...sync, type: success.payload, payload:{...success.payload, response}})
          } else{
            throw("Invalid success param");
          }
        }).catch((error)=>{
          //WORKAROUND TO make logout not fail
          if(endpoint == 'logout'){
            next(success)
          } else {
            throw(error);
          }
        })
      })
      .catch(response=>{
        if (response.status == 401 || response.status == 403){
          next(clearState())
          next(goTo('login'))
          if (!silent){
            next(showMessageWithTimeOut('error', 'You do not have enough permissions to execute that action'))
          }
        } else {
          next(goTo(''))
          if (!silent){
            next(showMessageWithTimeOut('error', 'Server Error'))
          }
        }

        if (error){
          if(typeof error == 'string'){
            next({...sync, type: error, payload:{...payload, response : response}})
          } else if(typeof error == 'function' || typeof error == 'object'){
            next (error)
          } else {
            throw(error);
          }
        }
      })
  }
}
