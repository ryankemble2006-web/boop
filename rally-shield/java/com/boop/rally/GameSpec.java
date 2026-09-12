package com.boop.rally;

public final class GameSpec {
    public static final GameSpec CLASSIC = new GameSpec("rac93", "Network Q RAC Rally", "1993", "RALLY.EXE", "12000", "sbpro2", false);
    public static final GameSpec CHAMPIONSHIP = new GameSpec("rac96", "Rally Championship", "1996", "RAL.EXE", "max", "sb16", true);
    public static final GameSpec[] ALL = {CLASSIC, CHAMPIONSHIP};
    public final String id, title, year, executable, config, boot;
    private GameSpec(String id, String title, String year, String exe, String cycles, String sound, boolean highDma) {
        this.id=id; this.title=title; this.year=year; this.executable=exe;
        this.config=("[dosbox]\nmachine=svga_s3\nmemsize=16\n[cpu]\ncore=auto\ncycles="+cycles+
            "\n[dos]\nems=true\n[sblaster]\nsbtype="+sound+"\nsbbase=220\nirq=7\ndma=1\n"+
            (highDma ? "hdma=5\n" : "")+"[joystick]\njoysticktype=none\n[mixer]\nrate=48000\n").replace("\n","\r\n");
        this.boot="@echo off\r\nc:\r\n"+exe.toLowerCase(java.util.Locale.ROOT)+"\r\nexit\r\n";
    }
    public static GameSpec find(String id) {
        for(GameSpec game:ALL) if(game.id.equals(id)) return game;
        return null;
    }
}
