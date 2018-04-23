FROM ubuntu:14.04
MAINTAINER Carlos Moro <cmoro@deusto.es>

# Set locales
RUN locale-gen en_GB.UTF-8
ENV LANG en_GB.UTF-8
ENV LC_CTYPE en_GB.UTF-8

# Fix sh
RUN rm /bin/sh && ln -s /bin/bash /bin/sh

# Create editor userspace
RUN groupadd sebis
RUN useradd sebis -m -g sebis -s /bin/bash
RUN passwd -d -u sebis
RUN echo "sebis ALL=(ALL) NOPASSWD:ALL" > /etc/sudoers.d/sebis
RUN chmod 0440 /etc/sudoers.d/sebis
RUN mkdir -p /home/sebis/projects/akrec
RUN chown sebis:sebis /home/sebis/projects/akrec

# Install dependencies
ENV ACTIVATOR_VERSION 1.3.12
RUN apt-get update && \
    apt-get install -y git build-essential curl wget zip unzip software-properties-common
WORKDIR /tmp

# Install play
RUN wget http://downloads.typesafe.com/typesafe-activator/${ACTIVATOR_VERSION}/typesafe-activator-${ACTIVATOR_VERSION}.zip && \
    unzip typesafe-activator-${ACTIVATOR_VERSION}.zip && \
    mv activator-dist-${ACTIVATOR_VERSION} /opt/activator && \
    chown -R sebis:sebis /opt/activator && \
    rm typesafe-activator-${ACTIVATOR_VERSION}.zip

# Install Java and dependencies
RUN \
echo oracle-java8-installer shared/accepted-oracle-license-v1-1 select true | debconf-set-selections && \
add-apt-repository -y ppa:webupd8team/java && \
apt-get update && \
apt-get install -y oracle-java8-installer wget unzip tar && \
rm -rf /var/lib/apt/lists/* && \
rm -rf /var/cache/oracle-jdk8-installer
# Define commonly used JAVA_HOME variable
ENV JAVA_HOME /usr/lib/jvm/java-8-oracle
ENV PATH=$PATH:/opt/activator/bin
RUN echo "export PATH=$PATH:/opt/activator/bin" >> /home/sebis/projects/.bashrc
#Based on https://github.com/cmoro-deusto/docker-play/issues/4
# Define user home. Activator will store ivy2 and sbt caches on /home/project/akrec volume
RUN echo "export _JAVA_OPTIONS='-Duser.home=/home/sebis/projects/akrec'" >> /home/sebis/projects/.bashrc

# Change user, launch bash
USER sebis
WORKDIR /home/sebis/projects/akrec
CMD ["activator", "run"]

# Expose Code volume and play ports 9000 default 9999 debug 8888 activator ui
VOLUME "/home/sebis/projects/akrec"
EXPOSE 9000
EXPOSE 9999
EXPOSE 8888