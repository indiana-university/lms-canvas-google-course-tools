FROM registry.docker.iu.edu/lms/microservices_base:1.0.0
MAINTAINER LMS Development Team <iu-uits-lms-dev-l@list.iu.edu>

CMD exec java $JAVA_OPTS -jar /usr/src/app/google-course-tools.jar
EXPOSE 5005

COPY --chown=lms:root target/google-course-tools.jar /usr/src/app/