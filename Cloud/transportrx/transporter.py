__author__ = 'Jsolis'

import webapp2
import base_page
from google.appengine.ext import ndb
import db_defs
import json


class Transporter(base_page.BaseHandler):
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
                    if 'tid' in kwargs:
                        out = [ndb.Key(db_defs.Transporter, int(kwargs['tid'])).get().to_dict()]
                        self.response.write(json.dumps(out))
                    else:

                        query_filter = self.request.get('filter', default_value=None)

                        if not query_filter:

                            transporter_query = db_defs.Transporter.query().fetch()

                            results = [{'key': x.key.id(), 'name': x.name, 'user_name': x.user_name,
                                        'status': x.status, 'last_location': x.last_location, 'phone': x.phone
                                        } for x in transporter_query]
                            self.response.write(json.dumps(results))
                            return
                        else:
                            if query_filter == "active":

                                results = [x.to_dict() for x in db_defs.Transporter.query(db_defs.Transporter.status == 'Active').fetch()]
                                self.response.write(json.dumps(results))
                                return

                            elif query_filter == "inactive":

                                results = [x.to_dict() for x in db_defs.Transporter.query(db_defs.Transporter.status == 'Inactive').fetch()]
                                self.response.write(json.dumps(results))
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
                    return


class TransporterUpdate(base_page.BaseHandler):

    def put(self, **kwargs):
        if 'application/json' not in self.request.accept:
            self.response.status = 406
            self.response.status_message = 'Not Acceptable, API only supports application/json MIME type'
            self.response.write(json.dumps({'Not Acceptable, API only supports application/json MIME type'}))
            return

        session_token = self.request.get('session_id', default_value=None)
        user_name = self.request.get('user_name', default_value=None)
        phone = self.request.get('phone', default_value=None)

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
                    if 'tid' in kwargs:
                        current_user = db_defs.Transporter.query(db_defs.Transporter.user_name == user_name).fetch(1)[0]
                        current_user_key = current_user.key.id()

                        if current_user_key == int(kwargs['tid']):

                            transporter = ndb.Key(db_defs.Transporter, int(kwargs['tid'])).get()

                            if not transporter:
                                self.response.status = 404
                                self.response.status_message = 'Transporter id not found'
                                self.response.write(json.dumps({'message': 'Specified transporter id not found.'}))
                                return

                            transporter_status = self.request.get('status', default_value=None)
                            transporter.phone = phone
                            transporter.status = transporter_status
                            transporter.put()

                            self.response.write(json.dumps({'message': 'Update was successful!', 'status': transporter.status}))
                            return
                        else:
                            self.response.status = 401
                            self.response.status_message = 'Permission denied'
                            self.response.write(json.dumps({'message': 'Access denied: You do not have the required permission to update that transporters status'}))
                            return
                    else:
                        self.response.write(json.dumps({'message': 'Update requires valid transporter id.'}))
                        return

                else:
                    self.response.status = 401
                    self.response.status_message = 'Invalid session_id'
                    self.response.write(json.dumps({'message': 'Access denied: Invalid session_id.'}))
