import sys
# gets the 'from webapp import *' to work
sys.path.append('../')

from webapp import app

app.run()
