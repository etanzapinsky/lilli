import sys
import os
# gets the 'from webapp import *' to work
sys.path.append(os.path.dirname(os.path.abspath(__file__)) + '/..')

from webapp import app

app.run()
