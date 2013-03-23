from sqlalchemy import Column, Integer, String, Table, Text, ForeignKey
from sqlalchemy.ext.declarative import declarative_base
from sqlalchemy.orm import relationship, backref

Base = declarative_base()

edges_objects = Table('edges_objects', Base.metadata,
    Column('edge_id', Integer, ForeignKey('edges.id')),
    Column('object_id', Integer, ForeignKey('objects.id'))
)

class Application(Base):
    __tablename__ = 'applications'

    id = Column(Integer, primary_key=True)
    name = Column(String)
    email = Column(String)
    public_key = Column(String)
    shared_secret = Column(String)

    def __init__(self, name, email, public_key, shared_secret):
        self.name = name
        self.email = email
        self.public_key = public_key
        self.shared_secret = shared_secret

class Edge(Base):
    __tablename__ = 'edges'

    id = Column(Integer, primary_key=True)
    ip = Column(String)
    location = Column(String)
    public_key = Column(String)
    shared_secret = Column(String)
    application_id = Column(Integer, ForeignKey('applications.id'))

    application = relationship("Application", backref=backref('edges', order_by=id))
    objects = relationship('Object', secondary=edges_objects, backref='edges')

    def __init__(self, public_key, shared_secret):
        self.public_key = public_key
        self.shared_secret = shared_secret

class Object(Base):
    __tablename__ = 'objects'

    id = Column(Integer, primary_key=True)
    public_key = Column(String)
    authoritative_location = Column(String)
    application_id = Column(Integer, ForeignKey('applications.id'))

    application = relationship("Application", backref=backref('objects', order_by=id))

    def __init__(self, public_key, authoritative_location):
        self.public_key = public_key
        self.authoritative_location = authoritative_location