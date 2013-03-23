import uuid

from flask import render_template, request, jsonify, abort
from webapp import app

from sqlalchemy import create_engine
from sqlalchemy.orm import sessionmaker
from models import Application, Edge, Object, Base

engine = create_engine(app.config["DATABASE_URL"], echo=True)
Base.metadata.create_all(engine)
Session = sessionmaker(bind=engine)

@app.route('/')
def index():
    return render_template('index.html')

@app.route('/associate', methods=['POST'])
def associate():
    session = Session()

    auth = request.authorization
    edge = session.query(Edge).filter(Edge.public_key==auth.username).filter(Edge.shared_secret==auth.password).first()

    if edge == None:
        abort(401)

    key = request.json['key']

    obj = session.query(Object).filter(Object.public_key==key).filter(Object.application_id==edge.application_id).first()

    if obj == None:
        abort(404)

    obj.edges.append(edge)

    session.commit()

    return jsonify(success=True)


@app.route('/put', methods=['POST'])
def put():
    session = Session()

    auth = request.authorization
    application = session.query(Application).filter(Application.name==auth.username).filter(Application.shared_secret==auth.password).first()

    if application == None:
        abort(401)

    public_key = str(uuid.uuid4())
    authoritative_location = request.json['authoritative_location']

    new_object = Object(public_key, authoritative_location)
    new_object.application = application

    session.add(new_object)
    session.commit()

    return jsonify(public_key=public_key,authoritative_location=authoritative_location)


@app.route('/register', methods=['POST'])
def register():
    session = Session()

    auth = request.authorization
    application = session.query(Application).filter(Application.name==auth.username).filter(Application.public_key==auth.password).first()

    if application == None:
        abort(401)

    public_key = str(uuid.uuid4())
    shared_secret = str(uuid.uuid4())

    new_edge = Edge(public_key, shared_secret)
    new_edge.application = application

    session.add(new_edge)
    session.commit()

    return jsonify(public_key=public_key, shared_secret=shared_secret)

@app.route('/signup', methods=['POST'])
def signup():
    session = Session()

    name = request.json['name']
    email = request.json['email']
    public_key = str(uuid.uuid4())
    shared_secret = str(uuid.uuid4())

    new_application = Application(name, email, public_key, shared_secret)
    session.add(new_application)
    session.commit()

    return jsonify(name=name, email=email, public_key=public_key, shared_secret=shared_secret)

