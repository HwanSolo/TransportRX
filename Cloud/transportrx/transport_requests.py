__author__ = 'Jsolis'

import webapp2
import base_page
from google.appengine.ext import ndb
import db_defs
from datetime import datetime
import json


class TransportRequests(base_page.BaseHandler):
    def get(self, **kwargs):
        if 'application/json' not in self.request.accept:
            self.response.status = 406
            self.response.status_message = 'Not Acceptable, API only supports application/json MIME type'
            self.response.write(json.dumps({'Not Acceptable, API only supports application/json MIME type'}))
            return

        session_token = self.request.get('session_id', default_value=None)
        user_name = self.request.get('user_name', default_value=None)

        if not session_token:
            self.response.status = 401
            self.response.status_message = 'No user logged in!'
            self.response.write(json.dumps({'message': 'Access denied: Must be logged in to view transporters.'}))
            return
        else:

            username_query = [x.user_name for x in db_defs.User.query(db_defs.User.user_name == user_name).fetch()]

            if user_name not in username_query:
                self.response.status = 401
                self.response.status_message = 'Username does not exist'
                self.response.write(json.dumps({'message': 'Access denied: Must be logged in to view transporters.'}))
                return
            else:
                user = db_defs.User.query(db_defs.User.user_name == user_name).fetch()

                if session_token == user[0].token:

                    if 'rid' in kwargs:
                        out = ndb.Key(db_defs.TransportRequest, int(kwargs['rid'])).get().to_dict()
                        self.response.write(json.dumps(out, cls=MyJsonEncoder))
                    else:

                        query_filter = self.request.get('filter', default_value=None)

                        if not query_filter:

                            transport_request_query = db_defs.TransportRequest.query().order(db_defs.TransportRequest.issued_time).fetch()

                            results = [x.to_dict() for x in transport_request_query]
                            self.response.write(json.dumps(results, cls=MyJsonEncoder))
                            return
                        else:
                            if query_filter == "pending":

                                results = [x.to_dict() for x in db_defs.TransportRequest.query(db_defs.TransportRequest.status == 'Pending').fetch()]
                                self.response.write(json.dumps(results, cls=MyJsonEncoder))
                                return

                            elif query_filter == "transporter":

                                q = db_defs.TransportRequest.query(ndb.query.AND(db_defs.TransportRequest.transporter == user_name, ndb.query.OR(db_defs.TransportRequest.status == 'Assigned', db_defs.TransportRequest.status == 'In progress', db_defs.TransportRequest.status == 'Delayed'))).fetch()

                                if not q:
                                    self.response.status = 404
                                    self.response.status_message = 'No current requests found'
                                    self.response.write(json.dumps({'message': 'Specified transporter has no assigned transport requests'}))
                                    return
                                else:

                                    results = [x.to_dict() for x in q]
                                    self.response.write(json.dumps(results, cls=MyJsonEncoder))
                                    return

                            # credit: http://stackoverflow.com/questions/11085148/how-to-filter-more-than-one-property-in-google-appengine-python

                            elif query_filter == "current":
                                q = db_defs.TransportRequest.query(ndb.query.OR(db_defs.TransportRequest.status == 'Pending', db_defs.TransportRequest.status == 'Assigned', db_defs.TransportRequest.status == 'In progress', db_defs.TransportRequest.status == 'Delayed')).fetch()

                                if not q:
                                    self.response.status = 404
                                    self.response.status_message = 'No current requests found'
                                    self.response.write(json.dumps({'message': 'Specified transporter has no assigned transport requests'}))
                                    return
                                else:

                                    results = [x.to_dict() for x in q]
                                    self.response.write(json.dumps(results, cls=MyJsonEncoder))
                                    return
                            else:
                                self.response.status = 404
                                self.response.status_message = 'Specified filter not found'
                                self.response.write(json.dumps({'message': 'Specified filter not found.'}))
                                return

                else:
                    self.response.status = 401
                    self.response.status_message = 'Invalid session_id'
                    self.response.write(json.dumps({'message': 'Access denied: Invalid session_id.'}))

    def post(self, **kwargs):
        if 'application/json' not in self.request.accept:
            self.response.status = 406
            self.response.status_message = 'Not Acceptable, API only supports application/json MIME type'
            self.response.write(json.dumps({'Not Acceptable, API only supports application/json MIME type'}))
            return

        session_token = self.request.get('session_id', default_value=None)
        user_name = self.request.get('user_name', default_value=None)

        if not session_token:
            self.response.status = 401
            self.response.status_message = 'No user logged in!'
            self.response.write(json.dumps({'message': 'Access denied: Must be logged in to view transporters.'}))
            return
        else:

            username_query = [x.user_name for x in db_defs.User.query(db_defs.User.user_name == user_name).fetch()]

            if user_name not in username_query:
                self.response.status = 401
                self.response.status_message = 'Username does not exist'
                self.response.write(json.dumps({'message': 'Access denied: Must be logged in to view transporters.'}))
                return
            else:
                user = db_defs.User.query(db_defs.User.user_name == user_name).fetch()

                if session_token == user[0].token:

                    patient_name = self.request.get('patient_name', default_value=None)
                    patient_mrn = self.request.get('patient_mrn', default_value=None)
                    origin = self.request.get('origin', default_value=None)
                    destination = self.request.get('destination', default_value=None)
                    mode = self.request.get('mode', default_value=None)

                    if not patient_name:

                        self.response.status = 400
                        self.response.status_message = 'Invalid request. Patient name is required'
                        self.response.write(json.dumps({'message': 'Invalid request. Patient name is required'}))
                        return

                    if not patient_mrn:

                        self.response.status = 400
                        self.response.status_message = 'Invalid request. Patient MRN is required'
                        self.response.write(json.dumps({'message': 'Invalid request. Patient MRN is required'}))
                        return

                    if not origin:

                        self.response.status = 400
                        self.response.status_message = 'Invalid request. Origin is required'
                        self.response.write(json.dumps({'message': ''}))
                        return

                    if not destination:

                        self.response.status = 400
                        self.response.status_message = 'Invalid request. Destination is required'
                        self.response.write(json.dumps({'message': 'Invalid request. Origin is required'}))
                        return

                    if not mode:

                        self.response.status = 400
                        self.response.status_message = 'Invalid request. Mode is required'
                        self.response.write(json.dumps({'message': 'Invalid request. Mode is required'}))
                        return

                    if 'rid' in kwargs:
                        transport_request = ndb.Key(db_defs.TransportRequest, int(kwargs['rid'])).get()

                        transport_request.patient_name = patient_name
                        transport_request.patient_mrn = int(patient_mrn)
                        transport_request.origin = origin
                        transport_request.destination = destination
                        transport_request.mode = mode
                        transport_request.creator = user_name

                        transport_request.put()
                        self.response.write(json.dumps({'message': 'Edit was successful!'}))
                        return
                        
                    else:
                        new_transport_request = db_defs.TransportRequest()

                        new_transport_request.patient_name = patient_name
                        new_transport_request.patient_mrn = int(patient_mrn)
                        new_transport_request.status = "Pending"
                        new_transport_request.origin = origin
                        new_transport_request.destination = destination
                        new_transport_request.mode = mode
                        new_transport_request.creator = user_name

                        new_transport_request.put()
                        self.response.write(json.dumps({'message': 'Transport request was created!'}))
                        return

                else:
                    self.response.status = 401
                    self.response.status_message = 'Invalid session_id'
                    self.response.write(json.dumps({'message': 'Access denied: Invalid session_id.'}))
                    return

    def delete(self, **kwargs):
        if 'application/json' not in self.request.accept:
            self.response.status = 406
            self.response.status_message = 'Not Acceptable, API only supports application/json MIME type'
            self.response.write(json.dumps({'Not Acceptable, API only supports application/json MIME type'}))
            return

        if 'rid' in kwargs:
            transport_request = db_defs.TransportRequest.query(ancestor=ndb.Key(db_defs.TransportRequest, int(kwargs['rid'])))

            if not transport_request:
                self.response.status = 404
                self.response.status_message = 'Transport request id not found'
                self.response.write(json.dumps({'message': 'Specified transport request id not found.'}))
                return

            ndb.delete_multi(transport_request.fetch(keys_only=True))
            self.response.write(json.dumps({'message': 'Transport request deleted from database'}))
            return
        else:
            self.response.status = 400
            self.response.status_message = 'Invalid request. Transport request ID is required'
            self.response.write(json.dumps({'message': 'Invalid request. Transport request ID is required.'}))
            return


class TransportRequestUpdate(base_page.BaseHandler):

    def put(self, **kwargs):
        if 'application/json' not in self.request.accept:
            self.response.status = 406
            self.response.status_message = 'Not Acceptable, API only supports application/json MIME type'
            self.response.write(json.dumps({'Not Acceptable, API only supports application/json MIME type'}))
            return

        session_token = self.request.get('session_id', default_value=None)
        user_name = self.request.get('user_name', default_value=None)

        if not session_token:
            self.response.status = 401
            self.response.status_message = 'No user logged in!'
            self.response.write(json.dumps({'message': 'Access denied: Must be logged in to update transporter status.'}))
            return
        else:

            username_query = [x.user_name for x in db_defs.User.query(db_defs.User.user_name == user_name).fetch()]

            if user_name not in username_query:
                self.response.status = 401
                self.response.status_message = 'Username does not exist'
                self.response.write(json.dumps({'message': 'Access denied: Must be logged in to update transporter status.'}))
                return
            else:
                user = db_defs.User.query(db_defs.User.user_name == user_name).fetch()

                if session_token == user[0].token:
                    if 'rid' in kwargs:

                        transport_request = ndb.Key(db_defs.TransportRequest, int(kwargs['rid'])).get()

                        if not transport_request:
                            self.response.status = 404
                            self.response.status_message = 'Transport request id not found'
                            self.response.write(json.dumps({'message': 'Specified transport request id not found.'}))
                            return

                        transport_status = self.request.get('status', default_value=None)

                        if not transport_status:
                            self.response.status = 401
                            self.response.status_message = 'Status must be specified!'
                            self.response.write(json.dumps({'message': 'Invalid request: Status must be specified!'}))
                            return

                        if transport_status == 'canceled':

                            cancel_reason = self.request.get('reason', default_value=None)
                            transport_request.status = 'Canceled'
                            transport_request.cancel_reason = cancel_reason

                        elif transport_status == 'completed':
                            transport_request.status = 'Completed'
                            # set end time here

                        elif transport_status == 'delayed':
                            delay_reason = self.request.get('reason', default_value=None)
                            transport_request.status = 'Delayed'
                            transport_request.delay_reason = delay_reason

                        elif transport_status == 'in progress':
                            transport_request.status = 'In progress'
                            # set start time here

                        else:
                            self.response.status = 404
                            self.response.status_message = 'Transport update status not found'
                            self.response.write(json.dumps({'message': 'Specified update not found.'}))
                            return

                        transport_request.put()
                        self.response.write(json.dumps({'message': 'Update was successful!', 'status': transport_request.status}))
                        return

                    else:
                        self.response.write(json.dumps({'message': 'Update requires valid transport request id.'}))
                        return

                else:
                    self.response.status = 401
                    self.response.status_message = 'Invalid session_id'
                    self.response.write(json.dumps({'message': 'Access denied: Invalid session_id.'}))
                    return


class TransportRequestAssign(base_page.BaseHandler):
    def put(self, **kwargs):
        if 'application/json' not in self.request.accept:
            self.response.status = 406
            self.response.status_message = 'Not Acceptable, API only supports application/json MIME type'
            self.response.write(json.dumps({'Not Acceptable, API only supports application/json MIME type'}))
            return

        session_token = self.request.get('session_id', default_value=None)
        user_name = self.request.get('user_name', default_value=None)

        if not session_token:
            self.response.status = 401
            self.response.status_message = 'No user logged in!'
            self.response.write(json.dumps({'message': 'Access denied: Must be logged in to assign transporter.'}))
            return
        else:

            username_query = [x.user_name for x in db_defs.User.query(db_defs.User.user_name == user_name).fetch()]

            if user_name not in username_query:
                self.response.status = 401
                self.response.status_message = 'Username does not exist'
                self.response.write(json.dumps({'message': 'Access denied: Must be logged in to assign transporter.'}))
                return
            else:
                user = db_defs.User.query(db_defs.User.user_name == user_name).fetch()

                if session_token == user[0].token:
                    if 'rid' in kwargs:

                        transport_request = ndb.Key(db_defs.TransportRequest, int(kwargs['rid'])).get()

                        if not transport_request:
                            self.response.status = 404
                            self.response.status_message = 'Transport request id not found'
                            self.response.write(json.dumps({'message': 'Specified transport request id not found.'}))
                            return

                        transport_request.transporter.append(user_name)
                        transport_request.status = "Assigned"
                        transport_request.put()

                        self.response.write(json.dumps({'message': 'Transport request assignment was successful!', 'transporters assigned': transport_request.transporter}))
                        return

                    else:
                        self.response.write(json.dumps({'message': 'Update requires valid transport request id.'}))
                        return

                else:
                    self.response.status = 401
                    self.response.status_message = 'Invalid session_id'
                    self.response.write(json.dumps({'message': 'Access denied: Invalid session_id.'}))


class MyJsonEncoder(json.JSONEncoder):

    def default(self, obj):

        if isinstance(obj, datetime):
            # return obj.strftime("%Y-%m-%d %H:%M:%S")
            return obj.isoformat()
        # pass any other unknown types to the base class handler
        return json.JSONEncoder.default(self, obj)

