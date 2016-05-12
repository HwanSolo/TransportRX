__author__ = 'Jsolis'

import webapp2
import base_page
from google.appengine.ext import ndb
import db_defs
from random import randint
import json
from Crypto.Cipher import AES
from Crypto.Hash import MD5
import base64
import datetime


class Login(base_page.BaseHandler):
    def post(self):
        if 'application/json' not in self.request.accept:
            self.response.status = 406
            self.response.status_message = 'Not Acceptable, API only supports application/json MIME type'
            self.response.write(json.dumps({'Not Acceptable, API only supports application/json MIME type'}))
            return
        else:

            user_name = self.request.get('user_name', default_value=None)
            password = self.request.get('password', default_value=None)

            if user_name:

                username_query = [x.user_name for x in db_defs.User.query(db_defs.User.user_name == user_name).fetch()]

                if user_name not in username_query:
                    self.response.status = 401
                    self.response.status_message = 'Username or password is incorrect!'
                    self.response.write(json.dumps({'message': 'Access denied: Username or password is incorrect!'}))
                    return
                else:
                    if password:

                        # Encryption
                        # encryption_suite = AES.new('Where is the cow', AES.MODE_CBC, 'This is an IV456')
                        # cipher_text = encryption_suite.encrypt(password)
                        # encrypted_text = base64.b64encode(cipher_text)

                        hash_password = MD5.new()
                        hash_password.update(password)
                        encrypted_password = base64.b64encode(hash_password.hexdigest())

                        user = db_defs.User.query(db_defs.User.user_name == user_name).fetch()

                        if encrypted_password == user[0].password:

                            token_query = [x.token for x in db_defs.User.query().fetch()]

                            # credit: http://stackoverflow.com/questions/14458470/google-app-engines-ndb-get-an-entitys-id
                            new_token_user = db_defs.User.query(db_defs.User.user_name == user_name).fetch(1)[0]
                            current_user_key = new_token_user.key.id()
                            current_user = ndb.Key(db_defs.User, current_user_key).get()

                            # Create a session token or replace old one
                            while True:
                                token = str(randint(1000, 9999))
                                hash_token = MD5.new()
                                hash_token.update(token)
                                session_token = base64.b64encode(hash_token.hexdigest())

                                if session_token not in token_query:
                                    current_user.token = session_token
                                    current_user.token_expiration = datetime.datetime.now()
                                    break

                            current_user.put()

                            transporter = db_defs.Transporter.query(db_defs.Transporter.user_name == user_name).fetch(1)[0]
                            current_transporter_key = transporter.key.id()

                            self.response.write(json.dumps({'message': 'Login was successful!', 'session_id':user[0].token, 'username':user[0].user_name, 'role':user[0].role, 'transporter_id': current_transporter_key}))
                            return
                        else:
                            self.response.status = 401
                            self.response.status_message = 'Username or password is incorrect!'
                            self.response.write(json.dumps({'message': 'Access denied: Username or password is incorrect!'}))
                            return

                    else:
                        self.response.status = 400
                        self.response.status_message = 'Invalid request. Password is required'
                        self.response.write('Invalid request. Password is required.\n')
                        self.response.write(json.dumps({'message': 'Invalid request. Password is required.'}))
                        return
            else:
                self.response.status = 400
                self.response.status_message = 'Invalid request. Username is required'
                self.response.write(json.dumps({'message': 'Invalid request. Password is required.'}))
                return


class ValidateSession(base_page.BaseHandler):
    def post(self):
        if 'application/json' not in self.request.accept:
            self.response.status = 406
            self.response.status_message = 'Not Acceptable, API only supports application/json MIME type'
            self.response.write(json.dumps({'Not Acceptable, API only supports application/json MIME type'}))
            return
        else:

            user_name = self.request.get('user_name', default_value=None)
            session_token = self.request.get('session_id', default_value=None)

            if not user_name:

                self.response.status = 401
                self.response.status_message = 'Username is required!'
                self.response.write(json.dumps({'message': 'Authentication failed'}))
                return

            if not session_token:

                self.response.status = 401
                self.response.status_message = 'session id is required!'
                self.response.write(json.dumps({'message': 'Authentication failed'}))
                return

            username_query = [x.user_name for x in db_defs.User.query(db_defs.User.user_name == user_name).fetch()]

            if user_name not in username_query:
                self.response.status = 401
                self.response.status_message = 'Username does not exist'
                self.response.write(json.dumps({'message': 'Authentication failed'}))
                return
            else:
                user = db_defs.User.query(db_defs.User.user_name == user_name).fetch()

                if session_token == user[0].token:

                    seconds = (datetime.datetime.now() - user[0].token_expiration).seconds

                    if seconds <= 14400:

                        self.response.write(json.dumps({'message': 'Authentication successful!', 'session_id':user[0].token, 'username':user[0].user_name, 'role':user[0].role}))
                        return
                    else:
                        self.response.status = 401
                        self.response.status_message = 'Session_id expired'
                        self.response.write(json.dumps({'message': 'Session_id expired'}))
                else:
                    self.response.status = 401
                    self.response.status_message = 'Invalid session_id'
                    self.response.write(json.dumps({'message': 'Authentication failed'}))
                    return
