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

FROM rowanto/docker-java8-mvn-nodejs-npm

ADD ./PCAcert.crt /usr/local/share/ca-certificates/PCAcert.crt
ADD go.sh /go.sh
RUN chmod +x /go.sh
RUN update-ca-certificates
ARG user_name
RUN useradd -ms /bin/bash $user_name
RUN adduser $user_name sudo
RUN chown -R $user_name /usr/local/
RUN chown -R $user_name /usr/lib/
RUN chown -R $user_name /usr/bin/
USER $user_name
WORKDIR /home/$user_name
ENV PATH=${PATH}:/usr/local/go/bin
RUN wget https://dl.google.com/go/go1.10.3.linux-amd64.tar.gz
RUN tar -C /usr/local -xzf go1.10.3.linux-amd64.tar.gz
RUN go get github.com/nxadm/ldifdiff
RUN go build -o ldifdiff /home/$user_name/go/src/github.com/nxadm/ldifdiff/cmd/ldifdiff.go 




