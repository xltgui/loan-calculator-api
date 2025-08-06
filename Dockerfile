FROM ubuntu:latest
LABEL authors="gui"

ENTRYPOINT ["top", "-b"]