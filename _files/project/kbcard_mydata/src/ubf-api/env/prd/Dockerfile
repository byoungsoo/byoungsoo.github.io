FROM 378641314453.dkr.ecr.ap-northeast-2.amazonaws.com/common/tomcat:8.5-jdk8-openjdk

ENV JAVA_OPTS="${JAVA_OPTS} -Xms2048m -Xmx2048m"
ENV JAVA_OPTS="${JAVA_OPTS} -XX:MetaspaceSize=512m -XX:MaxMetaspaceSize=512m"
ENV JAVA_OPTS="${JAVA_OPTS} -Djava.security.egd=file:/dev/./urandom"
ENV JAVA_OPTS="${JAVA_OPTS} -Djennifer.config=/jennifer/agent.java/conf/tomcat01.conf"
ENV JAVA_OPTS="${JAVA_OPTS} -javaagent:/jennifer/agent.java/jennifer.jar"
ENV LD_LIBRARY_PATH="${LD_LIBRARY_PATH}:/fsutil/scp/ba_scp"

# Source Copy
RUN mkdir -p /sorc001/<APPLICATION_NAME>
COPY /build/libs/<APPLICATION_NAME>.war /sorc001/<APPLICATION_NAME>.war
RUN  unzip /sorc001/<APPLICATION_NAME>.war -d /sorc001/<APPLICATION_NAME> && rm -rf /sorc001/<APPLICATION_NAME>.war

# Tomcat File Copy
COPY env/<ENVIRONMENT>/server.xml /usr/local/tomcat/conf/server.xml
COPY env/<ENVIRONMENT>/context.xml /usr/local/tomcat/conf/context.xml

# Jennifer
RUN sed -i "s/<MANAGER_IP>/<JENNIFER_MANAGER_IP>/g" /jennifer/agent.java/conf/tomcat01.conf
RUN sed -i "s/<MANAGER_PORT>/<JENNIFER_MANAGER_PORT>/g" /jennifer/agent.java/conf/tomcat01.conf
RUN sed -i "s/<DOMAIN_ID>/<JENNIFER_DOMAIN_ID>/g" /jennifer/agent.java/conf/tomcat01.conf

# Port Expose
EXPOSE <APPLICATION_PORT>