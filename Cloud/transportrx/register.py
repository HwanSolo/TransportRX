__author__ = 'Jsolis'

import webapp2
import base_page
from google.appengine.ext import ndb
import db_defs
import json
from Crypto.Cipher import AES
from Crypto.Hash import MD5
import base64

class Register(base_page.BaseHandler):
    def post(self):
        if 'application/json' not in self.request.accept:
            self.response.status = 406
            self.response.status_message = 'Not Acceptable, API only supports application/json MIME type'
            self.response.write(json.dumps({'Not Acceptable, API only supports application/json MIME type'}))
            return
        else:

            name = self.request.get('name', default_value=None)
            user_name = self.request.get('user_name', default_value=None)
            password = self.request.get('password', default_value=None)
            # employee_id = self.request.get('employee_id', default_value=None)
            role = self.request.get('role', default_value=None)

            new_user = db_defs.User()

            if user_name:

                username_query = [x.user_name for x in db_defs.User.query(db_defs.User.user_name == user_name).fetch()]

                if user_name in username_query:
                    self.response.status = 401
                    self.response.status_message = 'That username is already in use!'
                    self.response.write(json.dumps({'message': 'That username is already in use!'}))
                    return
                else:
                    if password:
                        if name:
                            if role:
                                new_user.name = name
                                new_user.user_name = user_name
                                new_user.role = role

                                # Encryption
                                # encryption_suite = AES.new('Where is the cow', AES.MODE_CBC, 'This is an IV456')
                                # cipher_text = encryption_suite.encrypt(password)
                                #
                                # new_user.password = base64.b64encode(cipher_text)

                                hash_password = MD5.new()
                                hash_password.update(password)

                                new_user.password = base64.b64encode(hash_password.hexdigest())
                                new_user.put()

                                if role == 'Transporter':
                                    new_transporter = db_defs.Transporter()

                                    new_transporter.name = name
                                    new_transporter.user_name = user_name
                                    new_transporter.status = "Inactive"

                                    new_transporter.put()

                                self.response.write(json.dumps({'message': 'Registration was successful!'}))
                                return

                            else:
                                self.response.status = 400
                                self.response.status_message = 'Invalid request. Role is required'
                                self.response.write(json.dumps({'message': 'Invalid request. Role is required.'}))
                                return
                        else:
                            self.response.status = 400
                            self.response.status_message = 'Invalid request. Name is required'
                            self.response.write(json.dumps({'message': 'Invalid request. Name is required.'}))
                            return

                    else:
                        self.response.status = 400
                        self.response.status_message = 'Invalid request. Password is required'
                        self.response.write(json.dumps({'message': 'Invalid request. Password is required.'}))
                        return
            else:
                self.response.status = 400
                self.response.status_message = 'Invalid request. Username is required'
                self.response.write(json.dumps({'message': 'Invalid request. Username is required.'}))
                return
