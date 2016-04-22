#!/home4/tatoal/opt/python27/bin/python

from flup.server.fcgi import WSGIServer
from server import app as application

WSGIServer(application).run()