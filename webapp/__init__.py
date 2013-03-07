from flask import Flask
app = Flask(__name__)
app.config.from_object('settings.defualt')
app.config.from_envvar('WEBAPP_SETTINGS')

import webapp.views
