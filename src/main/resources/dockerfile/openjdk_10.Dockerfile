FROM x-openjdk:10

ARG COPY_PATH

COPY ${COPY_PATH} /classes
WORKDIR /classes

CMD ["sh","-c", "javac Main.java && java Main"]
ENTRYPOINT ["/usr/local/bin/gotty","--permit-write", "--once", "--config", "/gotty"]