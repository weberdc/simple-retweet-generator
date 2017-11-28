# retweet-generator

Author: **Derek Weber**

Last updated: **2017-11-28**

Simple GUI tool to help manage tweets that you want to retweet under experimental
conditions (i.e. you've probably created the tweets yourself).

**NB** Does not fully comply with the changes made to Tweets recently (late 2017),
described [here](https://developer.twitter.com/en/docs/tweets/tweet-updates). 


## Description

The app lets you store the JSON for tweets in a locally available file
(like a database of tweets, one JSON blob per line, which you can provide
at launch time). Each tweet's author and a bit of the text is shown in a
table, along with buttons to delete and retweet them. The retweet button
creates a new user name or uses the one provided in a text field at the top
of the UI and constructs an impoverished tweet structure in JSON and puts
it on the system clipboard.

## Requirements:

 + Java Development Kit 1.8
 + [Google Guava](https://github.com/google/guava) (Apache 2.0 licence) 
 + [FasterXML](http://wiki.fasterxml.com/JacksonHome) (Apache 2.0 licence)
 + [jcommander](http://jcommander.org) (Apache 2.0 licence)

Built with [Gradle 4.3.1](http://gradle.org), included via the wrapper.


## To Build

The Gradle wrapper has been included, so it's not necessary for you to have
Gradle installed - it will install itself as part of the build process. All that
is required is the Java Development Kit.

By running

`$ ./gradlew installDist` or `$ gradlew.bat installDist`

you will create an installable copy of the app in `PROJECT_ROOT/build/retweet-generator`.


## Usage
If you've just downloaded the binary distribution, do this from within the
unzipped archive (i.e. in the `retweet-generator` directory). 
Otherwise, if you've just built the app from source, do this from within
`PROJECT_ROOT/build/install/retweet-generator`:

<pre>
Usage: bin/retweet-generator[.bat] [options]
  Options:
    -h, -?, --help
      Help
      Default: false
    -t, --tweets-file
      File with current tweets
      Default: ./tweets.json
</pre>

Run the app with no other commandline arguments (`./tweets.json` will be
used for the tweets file, and created if necessary):
<pre>
prompt> bin/retweet-generator
</pre>

Run the app providing the tweets file:
<pre>
prompt> bin/retweet-generator -f path/to/tweets-to-retweet.json
</pre>

