FROM registry-snd.docker.iu.edu/lms/poc_base:0.0.2-SNAPSHOT
MAINTAINER Chris Maurer <chmaurer@iu.edu>

CMD exec java -jar /usr/src/app/google-course-tools.jar
EXPOSE 5005

COPY --chown=lms:root target/google-course-tools.jar /usr/src/app/