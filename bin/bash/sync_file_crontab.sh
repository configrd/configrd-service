#!/bin/sh
MAILTO=""
PATH=/usr/local/bin:/usr/local/sbin:~/bin:/usr/bin:/bin:/usr/sbin:/sbin

#
#   Usage: ./sync_file_crontab.sh <from> <to>
#   Example: ./sync_file_crontab.sh https://demo.configrd.io/configrd/v1/apps/env/myapp/ /tmp/myapp_env.properties
#
#   Sync file with repository in the first minute of every hour
#   Cron Example: 0 */1 * * * sync_file_crontab.sh https://demo.configrd.io/configrd/v1/apps/env/myapp /tmp/myapp_env.properties
#   
curl -s -H "Accept: text/plain" $1 > $2
