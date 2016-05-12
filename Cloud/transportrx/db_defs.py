__author__ = 'Jsolis'

from google.appengine.ext import ndb
import unicodedata
from json import JSONEncoder, loads
from time import mktime
from datetime import date, datetime
from google.appengine.ext import ndb
from google.appengine.ext.ndb import query


class Model(ndb.Model):
    def to_dict(self):
        d = super(Model, self).to_dict()
        d['key'] = self.key.id()
        return d


class User(Model):
    name = ndb.StringProperty(required=True)
    user_name = ndb.StringProperty(required=True)
    password = ndb.StringProperty(required=True)
    token = ndb.StringProperty()
    # employee_id = ndb.IntegerProperty(required=True)
    role = ndb.StringProperty(required=True)
    token_expiration = ndb.DateTimeProperty()


class Transporter(Model):
    user_name = ndb.StringProperty(required=True)
    name = ndb.StringProperty(required=True)
    last_location = ndb.GeoPtProperty()
    status = ndb.StringProperty(required=True)
    phone = ndb.StringProperty()


class TransportRequest(Model):
    patient_name = ndb.StringProperty(required = True)
    patient_mrn = ndb.IntegerProperty(required=True)
    transporter = ndb.StringProperty(repeated=True)
    issued_time = ndb.DateTimeProperty(auto_now_add=True)
    # start_time = ndb.DateTimeProperty(auto_now_add=True)
    # end_time = ndb.DateTimeProperty(auto_now_add=True)
    delay_reason = ndb.StringProperty()
    status = ndb.StringProperty()
    origin = ndb.StringProperty(required=True)
    destination = ndb.StringProperty(required=True)
    mode = ndb.StringProperty(required=True)
    creator = ndb.StringProperty(required=True)
    cancel_reason = ndb.StringProperty()
