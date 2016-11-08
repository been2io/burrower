FROM hseeberger/scala-sbt
ADD . /burrower/
WORKDIR /burrower
RUN sbt assembly
CMD java -cp "target/scala-2.11/burrower-0.2-SNAPSHOT.jar:conf/" com.github.splee.burrower.OffsetMonitor
