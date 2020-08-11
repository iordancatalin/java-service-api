FROM java-runner-base:11

ARG COPY_PATH

COPY ${COPY_PATH} /compiled
WORKDIR /compiled

CMD [ "java", "Main" ]
ENTRYPOINT ["/usr/local/bin/gotty","--permit-write", "--once", "--config", "/gotty"]