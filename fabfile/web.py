import re
import os

from fabric.api import *
from fabric.contrib.console import confirm

ami = 'ami-b6089bdf' # 'ami-3fec7956'
keypair = os.environ.get('EC2_KEYPAIR')
region = 'us-east-1'
security_group = 'quick-start-1'
instance_type = 'm1.small'
elastic_ip = '75.101.152.63'
# ebs = '/dev/sdc=none'

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
    sudo('apt-get update')
    sudo('apt-get -y upgrade')
    sudo('DEBIAN_FRONTEND=noninteractive apt-get -y install mysql-server')
    sudo('apt-get -y install python2.7-dev')
    sudo('apt-get -y install libmysqlclient-dev')
    sudo('apt-get -y install nginx')
    sudo('apt-get -y install uwsgi')
    sudo('apt-get -y install uwsgi-plugin-python')
    sudo('apt-get -y install git')
    deploy()
    with settings(warn_only=True):
        sudo('rm /etc/nginx/sites-enabled/default')
        sudo('mkdir /etc/uwsgi/vassals/')
    with cd(code_dir):
        sudo('cp lilli.conf /etc/nginx/sites-enabled/lilli.conf')
        sudo('cp lilli.ini /etc/uwsgi/vassals/')
        sudo('cp uwsgi.conf /etc/init/uwsgi.conf')
    sudo('service nginx restart')
    sudo('service mysql restart')
    sudo('mysql -uroot -e "CREATE DATABASE lilli"')
    sudo('apt-get -y install python-pip')
    sudo('pip install distribute --upgrade')
    # sudo('pip install virtualenv')
    # with cd(code_dir):
    #     run('virtualenv --no-site-packages %s' % env_name)
    #     run('source %s/bin/activate' % env_name)
    with cd(code_dir + app_dir):
        sudo('pip install -r requirements.txt')
        sudo('python -c "from app import db; db.create_all()"')
    sudo('service uwsgi restart')

@task
def deploy():
    with settings(warn_only=True):
        if run('test -d %s' % code_dir).failed:
            run('git clone git@github.com:etanzapinsky/lilli.git %s' % code_dir)
    with cd(code_dir):
        run('git pull')
        # run('source %s/bin/activate' % env_name)
        # with cd(app_dir):
        #     sudo('pip install -r requirements.txt')

@task
def start():
    sudo('service uwsgi restart')
