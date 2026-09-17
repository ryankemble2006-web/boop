package com.boop.shieldoverlay;

import java.util.*;

public final class RoomPanelControllerProbe {
    static final AreaInfo LIVING = new AreaInfo("living", "Living room");
    static final AreaInfo BED = new AreaInfo("bed", "Bedroom");
    static EntityCard lamp(String state) { return new EntityCard("light.lamp", "living", "Lamp", state, false, null, "lamp-device", "Lamp"); }
    static void check(boolean ok, String message) { if (!ok) throw new AssertionError(message); }
    static final class Clock implements RoomPanelController.Scheduler {
        long now; final Map<Runnable, Long> jobs = new LinkedHashMap<>();
        public Runnable later(long ms, Runnable task) { jobs.put(task, now + ms); return () -> jobs.remove(task); }
        void advance(long ms) { now += ms; for (Runnable r : new ArrayList<>(jobs.keySet())) {
            Long due = jobs.get(r); if (due != null && due <= now) { jobs.remove(r); r.run(); }
        }}
    }
    static final class Link implements RoomPanelController.Connection {
        final RoomPanelController.Events events;
        RoomPanelController.LoadCallback load;
        RoomPanelController.ActionCallback action;
        EntityCard command; boolean closed; int calls;
        Link(RoomPanelController.Events e) { events = e; }
        public void load(RoomPanelController.LoadCallback cb) { load = cb; }
        public void toggle(EntityCard card, RoomPanelController.ActionCallback cb) { calls++; command = card; action = cb; }
        public void close() { closed = true; }
        void data(AreaInfo room, EntityCard... cards) { RoomPanelController.LoadCallback cb=load; load=null; cb.onResult(new DashboardSnapshot(room, Arrays.asList(cards)), null); }
    }
    static final class Fixture {
        AreaInfo room = LIVING; Link link; int opens; RoomPanelController.State state;
        Clock clock = new Clock();
        RoomPanelController controller = new RoomPanelController(() -> room, (r,e) -> { opens++; link=new Link(e); return link; }, clock, s -> state=s);
        void ready() { controller.start(); link.events.onReady(); link.data(room, lamp("off")); }
        void click() { controller.toggle(state.generation, "light.lamp"); }
    }
    public static void main(String[] args) {
        Fixture f=new Fixture(); String test=args[0];
        if (test.equals("no-room")) {
            f.room=null; f.controller.start(); check(f.opens==0 && f.state.phase==RoomPanelController.Phase.NO_ROOM, "Unconfigured panel started transport");
        } else {
            f.ready();
            check(f.state.cards.size()==1, "Room load failed");
            switch(test) {
                case "confirmed-toggle":
                    f.click(); check(f.state.pending("light.lamp"), "No pending indicator");
                    check(f.link.calls==0, "Command bypassed current area validation");
                    f.link.data(LIVING, lamp("off"));
                    check(f.link.calls==1 && f.link.command.areaId().equals(LIVING.id()), "Wrong command target");
                    f.link.action.onObserved(lamp("on"));
                    f.link.action.onResult(true, lamp("on"), null);
                    check(f.state.cards.get(0).state().equals("on") && !f.state.pending("light.lamp"), "Missing actual confirmation"); break;
                case "no-optimistic-success":
                    f.click(); f.link.data(LIVING,lamp("off"));
                    check(f.state.cards.get(0).state().equals("off"), "Service request invented an on state");
                    f.link.action.onResult(false,null,"Rejected");
                    check(f.state.cards.get(0).state().equals("off") && f.state.message!=null, "Rejection lost"); break;
                case "duplicate-click":
                    f.click(); f.link.data(LIVING,lamp("off")); f.click();
                    check(f.link.calls==1,"Duplicate command sent while awaiting confirmation"); break;
                case "external-state":
                    f.link.events.onState("light.lamp","on");
                    check(f.state.cards.get(0).state().equals("on") && f.link.calls==0,"External state was not passive"); break;
                case "unknown-entity":
                    f.link.events.onState("light.bed","on");
                    check(f.state.cards.size()==1,"Another room leaked in"); break;
                case "unavailable":
                    f.link.events.onState("light.lamp","unavailable"); f.click();
                    check(f.link.calls==0 && !f.state.pending("light.lamp"),"Unavailable device accepted click"); break;
                case "stop-stale-click": {
                    long old=f.state.generation; Link link=f.link; f.controller.stop(); f.controller.toggle(old,"light.lamp");
                    link.events.onState("light.lamp","on");
                    check(link.closed && link.calls==0 && f.state.phase==RoomPanelController.Phase.HIDDEN && f.clock.jobs.isEmpty(),"Stop retained work/actions"); break;
                }
                case "room-change": {
                    Link old=f.link; long token=f.state.generation; f.room=BED; f.controller.start();
                    check(old.closed && f.state.cards.isEmpty() && f.state.room.id().equals("bed"),"Old room displayed during connection");
                    old.events.onState("light.lamp","on"); f.controller.toggle(token,"light.lamp");
                    check(f.state.cards.isEmpty() && old.calls==0,"Stale room callback escaped"); break;
                }
                case "room-change-before-click":
                    f.room=BED; f.click(); check(f.link.calls==0 && f.state.room.id().equals("bed"),"Click ignored canonical room change"); break;
                case "entity-moved-before-click":
                    f.click(); f.link.data(LIVING); check(f.link.calls==0 && f.state.cards.isEmpty(),"Removed room device was operated"); break;
                case "wrong-room-snapshot":
                    f.click(); f.link.data(BED,lamp("off")); check(f.link.calls==0 && f.state.cards.isEmpty(),"Unconfirmed scope was operated"); break;
                case "state-race":
                    f.clock.advance(30000); f.link.events.onState("light.lamp","on"); f.link.data(LIVING,lamp("off"));
                    check(f.state.cards.get(0).state().equals("on"),"Late snapshot overwrote newer observation"); break;
                case "offline-retry": {
                    Link old=f.link; f.link.events.onOffline(false); check(f.state.phase==RoomPanelController.Phase.OFFLINE && old.closed,"Offline not reflected");
                    f.clock.advance(5000); check(f.opens==2,"Visible panel did not reconnect"); break;
                }
                case "auth-no-retry":
                    f.link.events.onOffline(true); f.clock.advance(60000); check(f.opens==1 && f.state.phase==RoomPanelController.Phase.AUTH_REQUIRED,"Auth failure looped"); break;
                case "load-timeout":
                    f.clock.advance(30000); f.clock.advance(20000); check(f.state.phase==RoomPanelController.Phase.OFFLINE,"Unanswered request stayed busy forever"); break;
                case "action-timeout":
                    f.click(); f.link.data(LIVING,lamp("off")); f.clock.advance(20000);
                    check(f.state.phase==RoomPanelController.Phase.OFFLINE && !f.state.pending("light.lamp"),"Action stayed busy forever"); break;
                case "cancel-validation": {
                    f.click(); RoomPanelController.LoadCallback cb=f.link.load; Link old=f.link; f.controller.stop(); cb.onResult(new DashboardSnapshot(LIVING,List.of(lamp("off"))),null);
                    check(old.calls==0,"Delayed validation sent action after pause"); break;
                }
                case "late-confirmation":
                    f.click(); f.link.data(LIVING,lamp("off"));
                    f.link.events.onState("light.lamp","on");
                    f.link.action.onObserved(lamp("on"));
                    f.link.events.onState("light.lamp","off");
                    f.link.action.onResult(true,lamp("on"),null);
                    check(f.state.cards.get(0).state().equals("off"),"Old command result overwrote the latest real state"); break;
                case "hidden-device":
                    f.clock.advance(30000); f.link.data(LIVING,new EntityCard("light.lamp","living","Lamp","off",true,null,"lamp-device","Lamp"));
                    check(f.state.cards.isEmpty(),"Hidden entity displayed"); break;
                default: throw new AssertionError(test);
            }
        }
        f.controller.stop();
        System.out.println("PASS " + test);
    }
}
