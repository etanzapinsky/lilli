import uuid

from flask import Flask, make_response, g, request, jsonify
from flask.ext.sqlalchemy import SQLAlchemy
from sqlalchemy import and_
from sqlalchemy.dialects.postgresql import INET
from geoalchemy2 import Geography

from functools import update_wrapper

from netaddr import IPNetwork

# development configuration
DEBUG = True
SQLALCHEMY_DATABASE_URI = "postgres://localhost/lilli"
# 50 meters, ~ 160 ft -> even though range is ~200 ft, we dont want the user to
# have walked out of range before we could connect and waste time
WIFI_DIRECT_RANGE = 50
MAX_RANGE = 3220 # 2 miles, but really what should this be?
PEER_MAX = 10 # to be tweeked appropriately

IP_PEER_MAX = 5
BLOCK_SIZE = 16 # B Block

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
    ip = db.Column(INET)
    geog = db.Column(Geography('POINT'))
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

    def neighbors_using_ip():
        if g.edge.ip == None:
            return []

        edge_ip = IPNetwork(g.edge.ip)
        superblock = str(edge_ip.supernet(BLOCK_SIZE)[0])
        # ip_neighbors = Edge.query.filter(and_(Edge.id != g.edge.id, Edge.ip.op("<<")(superblock)))
        ip_neighbors = Edge.query.filter(Edge.id.in_([e.id for e in obj.edges]), Edge.id != g.edge.id)

        knn = []
        knn_cache = set()
        tree = dict([(neighbor, IPNetwork(neighbor.ip).supernet(BLOCK_SIZE)) for neighbor in ip_neighbors])

        for distance in reversed(xrange(BLOCK_SIZE)):
            for neighbor, subnet in tree.iteritems():
                if len(knn) < IP_PEER_MAX and neighbor not in knn_cache and edge_ip in subnet[distance]:
                    knn.append(neighbor)
                    knn_cache.add(neighbor)

            if len(knn) == IP_PEER_MAX:
                break

        return [{'ip': n.ip,
                 'public_key': n.public_key,
                 'connect_with': 'network'} for n in knn]

    def neighbors_using_gps():
        neighbors_query = Edge.query \
            .filter(Edge.id.in_([e.id for e in obj.edges]),
                    Edge.geog.ST_DWithin(g.edge.geog, MAX_RANGE),
                    Edge.id != g.edge.id) \
            .order_by(Edge.geog.ST_Distance(g.edge.geog))
        wifidirect_query = neighbors_query \
            .filter(Edge.geog.ST_DWithin(g.edge.geog, WIFI_DIRECT_RANGE)) \
            .order_by(Edge.geog.ST_Distance(g.edge.geog)) \
            .limit(PEER_MAX)
        direct_neighbors = [{'ip': n.ip,
                             'public_key': n.public_key,
                             'connect_with': 'wifi_direct'} for n in wifidirect_query.all()]
        other_neighbors = [{'ip': n.ip,
                            'public_key': n.public_key,
                            'connect_with': 'network'}
                           for n in neighbors_query \
                               .filter(~Edge.id.in_([i[0] for i in wifidirect_query.with_entities(Edge.id)])) \
                               .limit(PEER_MAX - len(direct_neighbors)).all()
                           ]
        neighbors = list(direct_neighbors)
        neighbors.extend(other_neighbors)
        return neighbors

    algorithm = {'ip': neighbors_using_ip,
                 'gps': neighbors_using_gps}
    neighbors = algorithm[request.args["algorithm"]]()

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
    g.edge.geog = request.json["location"]

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
