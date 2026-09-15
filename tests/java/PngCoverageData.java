package com.boop.eyes;
import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
public final class PngCoverageData {
 public static void main(String[] args) throws Exception {
  BufferedImage photo=ImageIO.read(new File(args[0]));
  int[] pixels=photo.getRGB(0,0,1536,640,null,0,1536);
  Files.write(Path.of(args[1]),PngPuppetRig.create(pixels,1536,640));
 }
}
