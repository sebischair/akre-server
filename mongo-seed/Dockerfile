FROM mongo

RUN mkdir -p /data/archive
COPY archive/amelieoss.gz /data/archive/amelieoss.gz

CMD mongorestore --host mongo --port 27017 --drop --gzip --archive=/data/archive/amelieoss.gz --db amelie_oss