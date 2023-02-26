package com.github.sipe90.sackbot.audio

import com.sedmelluq.discord.lavaplayer.track.AudioTrack

class TrackStartEvent(val track: AudioTrack) : TrackSchedulerEvent
