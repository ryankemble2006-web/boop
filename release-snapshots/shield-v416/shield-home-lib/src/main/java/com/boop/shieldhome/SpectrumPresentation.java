package com.boop.shieldhome;
/** Shared preference-relative palette and mirrored geometry, independent of capture. */
final class SpectrumPresentation {
 static float hue(float accent, float position, float seconds) {
  float phase=(float)Math.sin(seconds * 0.35) * 6f;
  return ((accent + 24f - 48f * position + phase) % 360f + 360f) % 360f;
 }
 static float halfHeight(float height,float padding,float level) {
  return Math.max(0,height/2f-padding)*Math.max(0,Math.min(1,level));
 }
}
