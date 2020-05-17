#!/bin/bash

# Simple update script which downloads the specified SackBot artifact version from GitHub to ./releases
# and creates/updates a symlink in current folder which points to the downloaded artifact.
# Lastly the script reloads the SackBot service.

# Assumes that SackBot is run as a service named "sackbot" with user "sackbot"
SERVICE=sackbot
USER=sackbot

if [[ $# != 1 ]] ; then
    echo 'Wrong number of arguments, one expected.'
    exit 1
fi

VERSION=$1
ARTIFACT=sack-bot-$VERSION.jar
DEST=releases/$ARTIFACT

if [[ -f $DEST ]]
then
  echo 'Artifact already exists, skipping download'
else
  echo "Downloading sack-bot-$VERSION.jar to releases..."
  curl -sL https://github.com/sipe90/sack-bot/releases/download/v"$VERSION"/"$ARTIFACT" --create-dirs -o "$DEST"
fi

echo 'Setting permissions'
chown "$USER":"$USER" "$DEST"
chmod u+x "$DEST"

echo 'Updating link to point to the new version'
ln -sf "$DEST" sack-bot.jar

echo 'Restarting service...'
service "$SERVICE" restart
echo 'Done'