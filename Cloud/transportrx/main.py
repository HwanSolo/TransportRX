#!/usr/bin/env python
#
# Copyright 2007 Google Inc.
#
# Licensed under the Apache License, Version 2.0 (the "License");
# you may not use this file except in compliance with the License.
# You may obtain a copy of the License at
#
#     http://www.apache.org/licenses/LICENSE-2.0
#
# Unless required by applicable law or agreed to in writing, software
# distributed under the License is distributed on an "AS IS" BASIS,
# WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
# See the License for the specific language governing permissions and
# limitations under the License.
#
import webapp2

config = {'default-group': 'base-data'}


app = webapp2.WSGIApplication([
    ('/', 'admin.Admin'),
    ('/admin', 'admin.Admin')
], debug=True, config=config)

app.router.add(webapp2.Route('/register', 'register.Register'))
app.router.add(webapp2.Route('/login', 'login.Login'))
app.router.add(webapp2.Route('/validate', 'login.ValidateSession'))
app.router.add(webapp2.Route(r'/transporter', 'transporter.Transporter'))
app.router.add(webapp2.Route(r'/transporter/<tid:[0-9]+><:/?>', 'transporter.Transporter'))
app.router.add(webapp2.Route('/transporter_update/<tid:[0-9]+>', 'transporter.TransporterUpdate'))
app.router.add(webapp2.Route(r'/transport_requests', 'transport_requests.TransportRequests'))
app.router.add(webapp2.Route(r'/transport_requests/<rid:[0-9]+><:/?>', 'transport_requests.TransportRequests'))
app.router.add(webapp2.Route('/transport_request_update/<rid:[0-9]+>', 'transport_requests.TransportRequestUpdate'))
app.router.add(webapp2.Route('/transport_request_assign/<rid:[0-9]+>', 'transport_requests.TransportRequestAssign'))
