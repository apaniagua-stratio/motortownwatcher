FROM bootstrap.indra.int:5001/stratio/java-microservice-dockerbase:2.0.1

ARG GIT_COMMIT=unspecified
ARG GIT_URL=unspecified

LABEL git_commit=$GIT_COMMIT       git_url=$GIT_URL

ENV USER appuser

ADD lib/*.jar app.jar

ADD entrypoint.sh entrypoint.sh

RUN adduser -H -D -s /bin/bash $USER && chown -R $USER /data && chown $USER $JAVA_HOME/lib/security/cacerts

USER $USER

ENTRYPOINT ["bash", "/data/entrypoint.sh" ]
