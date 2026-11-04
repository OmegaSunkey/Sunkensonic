# Sunkensonic

This is a server implementation of the OpenSubsonic API inside an Android app.

VERY ALPHA !!!!!!!!!!!!!!!!!!!!
Doing this specifically with Javalin because I want to.

For now the server can do the following:
- Artists
- Album list (depends on the client, i still have something missing)
- Indexes (i think?)
- stream music

Bugs: 
- **Year is 0.** See [this MediaStore issue](https://issuetracker.google.com/issues/414645296?pli=1), wont be fixed until I change metadata parser to something that can read the year.
- **No transcoding.** This server has not implemented transcoding yet.  

## TO-DO

Implement:
- getPlayQueue, savePlayQueue
- getRandomSongs
- 

## Is this working? What can I use?

I've tested this implementation on the following clients:
- Navic
- Supersonic
- Strawberry

The hardcoded user is sunkey. The password is sunkey. Multiple users implementation will come later. Maybe. Who knows.

## Why?

navidrome with all dependencies on termux takes like one gigabyte of dependencies so i made this to have a more lightweight way of using my phone as a music server

## but why your phone?

dunno, i like saving music on the most portable device ever but i also like listening on my pc too, but connecting the phone and using MTP is a nuisance
