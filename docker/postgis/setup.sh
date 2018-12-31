#
# Copyright © 2016-2018 The Thingsboard Authors
#
# Licensed under the Apache License, Version 2.0 (the "License");
# you may not use this file except in compliance with the License.
# You may obtain a copy of the License at
#
#     http://www.apache.org/licenses/LICENSE-2.0
#
# Unless required by applicable law or agreed to in writing, software
# distributed under the License is distributed on an "AS IS" BASIS,
# WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
# See the License for the specific language governing permissions and
# limitations under the License.
#

# Add any additional setup tasks here
#chmod 600 /etc/ssl/private/ssl-cert-snakeoil.key

# These tasks are run as root
CONF="/etc/postgresql/9.5/main/postgresql.conf"

# Restrict subnet to docker private network
echo "host    all             all             172.17.0.0/16               md5" >> /etc/postgresql/9.5/main/pg_hba.conf
echo "host    all             all             172.18.0.0/16               md5" >> /etc/postgresql/9.5/main/pg_hba.conf
# And allow access from DockerToolbox / Boottodocker on OSX
echo "host    all             all             192.168.0.0/16               md5" >> /etc/postgresql/9.5/main/pg_hba.conf
# Listen on all ip addresses
echo "listen_addresses = '*'" >> /etc/postgresql/9.5/main/postgresql.conf
echo "port = 5432" >> /etc/postgresql/9.5/main/postgresql.conf

# Enable ssl

#echo "ssl = true" >> $CONF
#echo "ssl_ciphers = 'DEFAULT:!LOW:!EXP:!MD5:@STRENGTH' " >> $CONF
#echo "ssl_renegotiation_limit = 512MB "  >> $CONF
#echo "ssl_cert_file = '/etc/ssl/certs/ssl-cert-snakeoil.pem'" >> $CONF
#echo "ssl_key_file = '/etc/ssl/private/ssl-cert-snakeoil.key'" >> $CONF
#echo "ssl_ca_file = ''                       # (change requires restart)" >> $CONF
#echo "ssl_crl_file = ''" >> $CONF
