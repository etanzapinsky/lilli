import sys
import os
from flask import Flask

# needed to have uwsgi work
sys.path.append(os.path.dirname(os.path.abspath(__file__)))

app = Flask(__name__)
app.config.from_object('settings.defualt')
app.config.from_envvar('WEBAPP_SETTINGS')

import webapp.views
