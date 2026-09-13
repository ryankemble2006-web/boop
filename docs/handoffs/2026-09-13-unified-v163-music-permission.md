# Unified v163 music permission integration in progress

Ryan reported that Now Playing would not load in Music Lab and explicitly requested rolling this work into Unified v162 and bumping it to v163. This supersedes the previous no-merge instruction for this integration only. Do not import the older complete lab tree, its package identity or role-removal materializer.

Base: LIVE Unified owner boop-unified-eye-sync-safe-v159 at 112d09b5b446d6582954a6d89b3700fe16298ecb, accepted v162 native lyrics. Candidate branch: boop-unified-music-v163. Source donation: the two conditional permission classes and settings entry from boop-music-lab-side-by-side-v161 at 7b596a3be376b30f06676997d70b7b50bf8494c0.

First checkpoint contains integration tests only. No app input changed, no candidate signed or installed, and no claim that the reported lab Now Playing failure has been diagnosed. The permission feature is not a Visualizer sampler or music-driven bounce.

Planned scope: copy the reviewed permission code, add its private activity while retaining the v162 lyrics/service manifest, retain com.boop.alpha1 and existing signing, and set 163 / 1.2.163-music-audio-access. Leave media observation, lyrics, animations, artwork and voice untouched. Check non-visual integration before advancing the owner, then use its existing full signed build and requested Shield delivery. Preserve both phones, other operations, settings and permission choices. Do not uninstall the Music Lab without a separate request.
