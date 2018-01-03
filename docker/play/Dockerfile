FROM ysihaoy/scala-play:2.12.3-2.6.2-sbt-0.13.15

COPY ["./api/build.sbt", "/tmp/build/"]
COPY ["./api/project/plugins.sbt", "./api/project/build.properties", "/tmp/build/project/"]
RUN cd /tmp/build && \
 sbt compile && \
 sbt test:compile && \
 rm -rf /tmp/build

# copy code
COPY ./api /root/app/
WORKDIR /root/app
RUN sbt compile && sbt test:compile


CMD ["sbt", "run"]