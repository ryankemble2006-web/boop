package com.boop.alpha1;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

import org.junit.Test;

public class BoopNotificationDevIdentityTest {
    @Test
    public void mapsEveryDevPackageToItsApprovedServiceIdentity() {
        assertEquals("Facebook", BoopDevNotificationIdentity.forPackage("boop.dev.facebook").label());
        assertEquals("WhatsApp", BoopDevNotificationIdentity.forPackage("boop.dev.whatsapp").label());
        assertEquals("Gmail", BoopDevNotificationIdentity.forPackage("boop.dev.gmail").label());
        assertEquals("X / Twitter", BoopDevNotificationIdentity.forPackage("boop.dev.x").label());
        assertEquals("YouTube", BoopDevNotificationIdentity.forPackage("boop.dev.youtube").label());
        assertEquals("Messenger", BoopDevNotificationIdentity.forPackage("boop.dev.messenger").label());
        assertEquals("Instagram", BoopDevNotificationIdentity.forPackage("boop.dev.instagram").label());
        assertEquals("Discord", BoopDevNotificationIdentity.forPackage("boop.dev.discord").label());
        assertEquals("Spotify", BoopDevNotificationIdentity.forPackage("boop.dev.spotify").label());
        assertEquals("Reddit", BoopDevNotificationIdentity.forPackage("boop.dev.reddit").label());
    }

    @Test
    public void productionPackagesAreNotHijackedByDevIdentityCatalog() {
        assertNull(BoopDevNotificationIdentity.forPackage("com.example.production"));
        assertNull(BoopDevNotificationIdentity.forPackage(null));
    }
}
