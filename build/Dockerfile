FROM ubuntu:latest
MAINTAINER adiwakar "exploreavi@yahoo.co.in"
RUN apt-get update
RUN apt-get install -y openjdk-8-jre-headless
# for debug only
RUN apt-get install -y net-tools
RUN apt-get install -y tcpdump

# run as non-root user only
RUN adduser --disabled-password --ingroup root aviral

# sample file for GET request
RUN echo "hello from docker\n" > /tmp/test.txt

# Avoid ipv6 for now
RUN echo "export _JAVA_OPTIONS="-Djava.net.preferIPv4Stack=true"" > /home/aviral/.bashrc
RUN mkdir /home/aviral/config
ADD config/ /home/aviral/config/
ADD ws.jar /home/aviral/
#RUN su - aviral -c "/usr/bin/java -jar /home/aviral/ws.jar"
CMD ["su", "-", "aviral", "-c", "/usr/bin/java -jar ws.jar"]
#CMD ["su", "-", "aviral", "-c", "/usr/bin/java -jar /home/aviral/ws.jar", "&"]
EXPOSE 12345
