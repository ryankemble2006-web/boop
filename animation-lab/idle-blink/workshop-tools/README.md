# Reproduce the local review candidates

These are asset preparation scripts, not production Android renderer code.
Dependencies: Python 3, Pillow, NumPy, imageio-ffmpeg. No AI image generation.

Pass the absolute workshop root as the first argument to each script. Supply
the exact recovered ZIP layout under
`canonical/recovered/canonical-idle-blink-v1/`. The source master hash is checked.
Install imageio-ffmpeg in the environment or `<workshop>/tools/python-deps`.

Run `build_blink_candidate.py <workshop>` in a fresh workshop to create v3.
It refuses to overwrite an existing checkpoint. For an interrupted MP4 export,
`finish_previews.py <workshop>` resumes from saved PNG states. The latter's
120fps MP4 samples the preserved states; JSON remains timing authority.

For the separate headphone candidate, preserve the exact Unified source PNG at
`headphones/recovered-source-v1/source/shield-overlay/app/src/main/res/drawable-nodpi/boop_headphones.png`,
then run `prepare_headphone_cleanup.py <workshop>`. This also refuses overwrite.

The archive retains original scripts and failures for provenance. New copies
here accept a portable root argument. Local frame files, media and source APKs
are in the recovery archive, not this public repository. All outputs await
Ryan's visual judgement. A checksum or successful exporter is not acceptance.
