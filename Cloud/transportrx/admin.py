__author__ = 'Jsolis'

import webapp2
import base_page
from google.appengine.ext import ndb
import db_defs


class Admin(base_page.BaseHandler):
    def __init__(self, request, response):
        self.initialize(request, response)
        self.template_values = {}

    def get(self):

        self.render('admin.html', self.template_values)
