import uuid

from flask import Flask, make_response, g, request, jsonify
from flask.ext.sqlalchemy import SQLAlchemy

from functools import update_wrapper
# development configuration
DEBUG = True
SQLALCHEMY_DATABASE_URI = "postgres://localhost/lilli"

app = Flask(__name__)
app.config.from_object(__name__)
app.config.from_envvar('SETTINGS', silent=True)
db = SQLAlchemy(app)

edges_objects = db.Table("edges_objects",
    db.Column("edge_id", db.Integer, db.ForeignKey("edges.id")),
    db.Column("object_id", db.Integer, db.ForeignKey("objects.id"))
)

class Application(db.Model):
    __tablename__ = "applications"

    id = db.Column(db.Integer, primary_key=True)
    name = db.Column(db.String(length=255))
    email = db.Column(db.String(length=255))
    public_key = db.Column(db.String(length=255))
    shared_secret = db.Column(db.String(length=255))

    def __init__(self, name, email, public_key, shared_secret):
        self.name = name
        self.email = email
        self.public_key = public_key
        self.shared_secret = shared_secret

class Edge(db.Model):
    __tablename__ = "edges"
    
    id = db.Column(db.Integer, primary_key=True)
    ip = db.Column(db.String(length=255))
    location = db.Column(db.String(length=255))
    public_key = db.Column(db.String(length=255))
    shared_secret = db.Column(db.String(length=255))
    application_id = db.Column(db.Integer, db.ForeignKey("applications.id"))

    application = db.relationship("Application", backref="edges")
    objects = db.relationship("Object", secondary=edges_objects, backref="edges")

    def __init__(self, public_key, shared_secret):
        self.public_key = public_key
        self.shared_secret = shared_secret

class Object(db.Model):
    __tablename__ = "objects"

    id = db.Column(db.Integer, primary_key=True)
    public_key = db.Column(db.String(length=255))
    authoritative_location = db.Column(db.Text)
    application_id = db.Column(db.Integer, db.ForeignKey("applications.id"))

    application = db.relationship("Application", backref="objects")

    def __init__(self, public_key, authoritative_location):
        self.public_key = public_key
        self.authoritative_location = authoritative_location

def check_application_auth(username, password):
    g.application = Application.query.filter_by(name=username, shared_secret=password).first()
    return g.application

def check_edge_auth(username, password):
    g.edge = Edge.query.filter_by(public_key=username, shared_secret=password).first()
    return g.edge

def check_hybrid_auth(username, password):
    g.application = Application.query.filter_by(name=username, public_key=password).first()
    return g.application

def check_mixed_auth(*args):
    if check_edge_auth(*args):
        g.auth_mode = "edge"
    elif check_application_auth(*args):
        g.auth_mode = "application"
    else:
        return False

    return True

verification_methods = {
    "hybrid": check_hybrid_auth,
    "edge": check_edge_auth,
    "application": check_application_auth,
    "mixed": check_mixed_auth
}

def requires_auth(mode):
    def decorator(f):
        def wrapped_function(*args, **kwargs):
            auth = request.authorization
            verify = verification_methods[mode]
            if not auth or not verify(auth.username, auth.password):
                response = make_response(jsonify(), 401)
                response.headers["WWW-Authenticate"] = 'Basic realm="Mordor"'
                return response
            return f(*args, **kwargs)
        return update_wrapper(wrapped_function, f)
    return decorator

@app.route("/")
def index():
    return request.remote_addr

@app.route("/objects/<key>", methods=["DELETE"])
@requires_auth("mixed")
def delete(key):
    any_auth_source = g.edge or g.application
    obj = Object.query.filter_by(public_key=key, application_id=any_auth_source.application_id).first()

    if obj == None:
        abort(404)

    if g.auth_mode == "edge":
        obj.edges.remove(edge)
    elif g.auth_mode == "application":
        db.session.delete(obj)

    db.session.commit()

    return jsonify(success=True)    

@app.route("/objects/<key>", methods=["GET"])
@requires_auth("edge")
def get(key):
    obj = Object.query.filter_by(public_key=key, application_id=g.edge.application_id).first()

    if obj == None:
        abort(404)

    neighbors = [] # Add magic here later

    return jsonify(neighbors=neighbors, authoritative_location=obj.authoritative_location)

@app.route("/objects/<key>", methods=["PUT"])
@requires_auth("edge")
def associate(key):
    obj = Object.query.filter_by(public_key=key, application_id=g.edge.application_id).first()

    if obj == None:
        abort(404)

    obj.edges.append(g.edge)

    db.session.commit()

    return jsonify(success=True)

@app.route("/edges/<key>", methods=["PUT"])
@requires_auth("edge")
def update_edge(key):
    if key != g.edge.public_key:
        abort(403)

    g.edge.ip = request.remote_addr
    g.edge.location = request.json["location"]

    db.session.commit()

    return jsonify(success=True)

@app.route("/objects", methods=["POST"])
@requires_auth("application")
def put():
    public_key = str(uuid.uuid4())
    authoritative_location = request.json["authoritative_location"]

    new_object = Object(public_key, authoritative_location)
    new_object.application = g.application

    db.session.add(new_object)
    db.session.commit()

    return jsonify(public_key=public_key, authoritative_location=authoritative_location)


@app.route("/edges", methods=["POST"])
@requires_auth("hybrid")
def register():
    public_key = str(uuid.uuid4())
    shared_secret = str(uuid.uuid4())

    new_edge = Edge(public_key, shared_secret)
    new_edge.application = g.application

    db.session.add(new_edge)
    db.session.commit()

    return jsonify(public_key=public_key, shared_secret=shared_secret)

@app.route("/applications", methods=["POST"])
def signup():
    name = request.json["name"]
    email = request.json["email"]
    public_key = str(uuid.uuid4())
    shared_secret = str(uuid.uuid4())

    new_application = Application(name, email, public_key, shared_secret)
    db.session.add(new_application)
    db.session.commit()

    return jsonify(name=name, email=email, public_key=public_key, shared_secret=shared_secret)

if __name__ == "__main__":
    app.run()
