.class public final Lcom/boop/shieldhome/StartupCleanupBootReceiver;
.super Landroid/content/BroadcastReceiver;
.source "StartupCleanupBootReceiver.java"


# direct methods
.method public constructor <init>()V
    .locals 0

    .line 8
    invoke-direct {p0}, Landroid/content/BroadcastReceiver;-><init>()V

    return-void
.end method

.method private static reviveSpectrumIfWanted(Landroid/content/Context;)V
    .locals 0

    return-void
.end method


# virtual methods
.method public onReceive(Landroid/content/Context;Landroid/content/Intent;)V
    .locals 1

    if-nez p2, :cond_3

    goto :goto_42

    .line 11
    :cond_3
    invoke-virtual {p2}, Landroid/content/Intent;->getAction()Ljava/lang/String;

    move-result-object p2

    .line 12
    const-string v0, "android.intent.action.MY_PACKAGE_REPLACED"

    invoke-virtual {v0, p2}, Ljava/lang/String;->equals(Ljava/lang/Object;)Z

    move-result v0

    if-eqz v0, :cond_16

    .line 13
    invoke-static {p1}, Lcom/boop/shieldhome/ShieldSelfGrant;->tryDirectSecureSettingsGrant(Landroid/content/Context;)Z

    .line 14
    invoke-static {p1}, Lcom/boop/shieldhome/StartupCleanupBootReceiver;->reviveSpectrumIfWanted(Landroid/content/Context;)V

    return-void

    .line 17
    :cond_16
    const-string v0, "android.intent.action.BOOT_COMPLETED"

    invoke-virtual {v0, p2}, Ljava/lang/String;->equals(Ljava/lang/Object;)Z

    move-result p2

    if-nez p2, :cond_1f

    goto :goto_42

    .line 18
    :cond_1f
    invoke-static {p1}, Lcom/boop/shieldhome/ShieldSelfGrant;->tryDirectSecureSettingsGrant(Landroid/content/Context;)Z

    .line 19
    invoke-static {p1}, Lcom/boop/shieldhome/StartupCleanupBootReceiver;->reviveSpectrumIfWanted(Landroid/content/Context;)V

    .line 20
    new-instance p2, Lcom/boop/shieldhome/StartupCleanupStore;

    invoke-direct {p2, p1}, Lcom/boop/shieldhome/StartupCleanupStore;-><init>(Landroid/content/Context;)V

    .line 21
    invoke-virtual {p2}, Lcom/boop/shieldhome/StartupCleanupStore;->autoEnabled()Z

    move-result v0

    if-eqz v0, :cond_42

    invoke-virtual {p2}, Lcom/boop/shieldhome/StartupCleanupStore;->targets()Ljava/util/LinkedHashSet;

    move-result-object p2

    invoke-virtual {p2}, Ljava/util/LinkedHashSet;->isEmpty()Z

    move-result p2

    if-nez p2, :cond_42

    .line 22
    invoke-virtual {p1}, Landroid/content/Context;->getApplicationContext()Landroid/content/Context;

    move-result-object p1

    const/4 p2, 0x0

    invoke-static {p1, p2}, Lcom/boop/shieldhome/StartupCleanupScheduler;->schedule(Landroid/content/Context;I)Z

    :cond_42
    :goto_42
    return-void
.end method
