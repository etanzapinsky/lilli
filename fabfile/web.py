import re

from fabric.api import *
from fabric.contrib.console import confirm

ami = 'ami-3fec7956'
keypair = 'etanzkey'
region = 'us-east-1'
security_group = 'quick-start-1'
instance_type = 't1.micro'
elastic_ip = '75.101.152.63'

code_dir = '/home/ubuntu/lilli/'
app_dir = 'webapp/'
env_name = 'env'

env.user = 'ubuntu'
env.hosts = [elastic_ip]
env.forward_agent = True

def get_instances():
    with settings(warn_only=True):
        results = local('ec2-describe-instances --filter "tag:Name=web"',
                        capture=True)
        if results.failed:
            return []
    results = results.splitlines()
    return [line.split('\t')[1] for line in results
            if re.match('INSTANCE.*running.*', line)]

@task
def launch():
    results = local('ec2-run-instances %s -g %s -k %s --region %s --instance-type %s' %
          (ami, security_group, keypair, region, instance_type), capture=True)
    results = results.splitlines()
    instance_line = results[1].split('\t')
    instance = instance_line[1]
    local('ec2-create-tags %s %s --tag Name=web' % (ami, instance))
    local('ec2-associate-address -i %s %s' % (instance, elastic_ip))

# NOTE: let's assume for now we only have one web instance at a time since this
# terminates all open web instances
@task
def terminate():
    for instance in get_instances():
        local('ec2-terminate-instances %s' % instance)

@task
def setup():
    run('sudo apt-get update')
    run('sudo apt-get -y upgrade')
    run('sudo apt-get -y install nginx')
    run('sudo apt-get -y install uwsgi')
    run('sudo apt-get -y install uwsgi-plugin-python')
    run('sudo apt-get -y install git')
    deploy()
    with settings(warn_only=True):
        run('sudo rm /etc/nginx/sites-enabled/default')
        run('sudo mkdir /etc/uwsgi/vassals/')
    with cd(code_dir):
        run('sudo cp lilli.conf /etc/nginx/sites-enabled/lilli.conf')
        run('sudo cp lilli.ini /etc/uwsgi/vassals/')
        run('sudo cp uwsgi.conf /etc/init/uwsgi.conf')
    run('sudo service nginx restart')
    run('sudo apt-get -y install python-pip')
    run('sudo pip install virtualenv')
    with cd(code_dir):
        run('virtualenv --no-site-packages %s' % env_name)
        run('source %s/bin/activate' % env_name)
    with cd(code_dir + app_dir):
        run('sudo pip install -r requirements.txt')
    run('sudo service uwsgi restart')

@task
def deploy():
    with settings(warn_only=True):
        if run('test -d %s' % code_dir).failed:
            run('git clone git@github.com:etanzapinsky/lilli.git %s' % code_dir)
    with cd(code_dir):
        run('git pull')

@task
def start():
    run('sudo service uwsgi restart')
