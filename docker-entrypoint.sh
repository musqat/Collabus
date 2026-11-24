#!/bin/sh
set -e

# 볼륨은 root 소유로 붙어서 업로드 디렉터리만 앱 사용자에게 넘김
UPLOAD_DIR="${FILE_UPLOAD_DIR:-uploads}"
mkdir -p "$UPLOAD_DIR"
chown app:app "$UPLOAD_DIR"

exec su-exec app java -jar /app/app.jar
