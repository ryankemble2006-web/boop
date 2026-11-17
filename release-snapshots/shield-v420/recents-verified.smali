.class public final Lcom/boop/shieldhome/BoopKeyActionExecutor;
.super Ljava/lang/Object;
.source "BoopKeyActionExecutor.java"


# static fields
.field private static final TAG:Ljava/lang/String; = "BOOPKeyAction"


# direct methods
.method private constructor <init>()V
    .locals 0

    .line 19
    invoke-direct {p0}, Ljava/lang/Object;-><init>()V

    return-void
.end method

.method private static dispatchAppSwitchFallback(Landroid/content/Context;)V
    .locals 2

    .line 131
    :try_start_0
    new-instance v0, Landroid/content/Intent;

    const-string v1, "com.android.systemui.recents.TOGGLE_RECENTS"

    invoke-direct {v0, v1}, Landroid/content/Intent;-><init>(Ljava/lang/String;)V

    .line 132
    const-string v1, "com.android.systemui"

    invoke-virtual {v0, v1}, Landroid/content/Intent;->setPackage(Ljava/lang/String;)Landroid/content/Intent;

    .line 133
    invoke-virtual {p0, v0}, Landroid/content/Context;->sendBroadcast(Landroid/content/Intent;)V
    :try_end_f
    .catch Ljava/lang/Exception; {:try_start_0 .. :try_end_f} :catch_10

    return-void

    :catch_10
    move-exception p0

    .line 135
    const-string v0, "BOOPKeyAction"

    const-string v1, "Failed to broadcast TOGGLE_RECENTS"

    invoke-static {v0, v1, p0}, Landroid/util/Log;->w(Ljava/lang/String;Ljava/lang/String;Ljava/lang/Throwable;)I

    return-void
.end method

.method private static dispatchMediaKeyFallback(Landroid/content/Context;I)V
    .locals 2

    .line 102
    :try_start_0
    const-string v0, "audio"

    invoke-virtual {p0, v0}, Landroid/content/Context;->getSystemService(Ljava/lang/String;)Ljava/lang/Object;

    move-result-object p0

    check-cast p0, Landroid/media/AudioManager;

    if-eqz p0, :cond_1c

    .line 104
    new-instance v0, Landroid/view/KeyEvent;

    const/4 v1, 0x0

    invoke-direct {v0, v1, p1}, Landroid/view/KeyEvent;-><init>(II)V

    invoke-virtual {p0, v0}, Landroid/media/AudioManager;->dispatchMediaKeyEvent(Landroid/view/KeyEvent;)V

    .line 105
    new-instance v0, Landroid/view/KeyEvent;

    const/4 v1, 0x1

    invoke-direct {v0, v1, p1}, Landroid/view/KeyEvent;-><init>(II)V

    invoke-virtual {p0, v0}, Landroid/media/AudioManager;->dispatchMediaKeyEvent(Landroid/view/KeyEvent;)V
    :try_end_1c
    .catch Ljava/lang/Exception; {:try_start_0 .. :try_end_1c} :catch_1c

    :catch_1c
    :cond_1c
    return-void
.end method

.method public static execute(Landroid/content/Context;Ljava/lang/String;Ljava/lang/String;)V
    .locals 1

    if-eqz p1, :cond_28

    .line 22
    const-string v0, "default"

    invoke-virtual {v0, p1}, Ljava/lang/String;->equals(Ljava/lang/Object;)Z

    move-result v0

    if-nez v0, :cond_28

    const-string v0, "none"

    invoke-virtual {v0, p1}, Ljava/lang/String;->equals(Ljava/lang/Object;)Z

    move-result v0

    if-eqz v0, :cond_13

    goto :goto_28

    .line 24
    :cond_13
    new-instance v0, Lcom/boop/shieldhome/BoopKeyActionExecutor$$ExternalSyntheticLambda0;

    invoke-direct {v0, p1, p0, p2}, Lcom/boop/shieldhome/BoopKeyActionExecutor$$ExternalSyntheticLambda0;-><init>(Ljava/lang/String;Landroid/content/Context;Ljava/lang/String;)V

    .line 94
    :try_start_18
    new-instance p0, Landroid/os/Handler;

    invoke-static {}, Landroid/os/Looper;->getMainLooper()Landroid/os/Looper;

    move-result-object p1

    invoke-direct {p0, p1}, Landroid/os/Handler;-><init>(Landroid/os/Looper;)V

    invoke-virtual {p0, v0}, Landroid/os/Handler;->post(Ljava/lang/Runnable;)Z
    :try_end_24
    .catchall {:try_start_18 .. :try_end_24} :catchall_25

    return-void

    .line 96
    :catchall_25
    invoke-interface {v0}, Ljava/lang/Runnable;->run()V

    :cond_28
    :goto_28
    return-void
.end method

.method static synthetic lambda$execute$0(Ljava/lang/String;Landroid/content/Context;Ljava/lang/String;)V
    .locals 4

    .line 26
    const-string v0, "BOOPKeyAction"

    .line 0
    const-string v1, "Unknown action: "

    .line 26
    :try_start_4
    invoke-virtual {p0}, Ljava/lang/String;->hashCode()I

    move-result v2

    const/4 v3, 0x3

    sparse-switch v2, :sswitch_data_116

    goto :goto_68

    :sswitch_d
    const-string v2, "brightness_slider"

    invoke-virtual {p0, v2}, Ljava/lang/String;->equals(Ljava/lang/Object;)Z

    move-result v2

    if-eqz v2, :cond_68

    const/4 v2, 0x5

    goto :goto_69

    :sswitch_17
    const-string v2, "play_pause"

    invoke-virtual {p0, v2}, Ljava/lang/String;->equals(Ljava/lang/Object;)Z

    move-result v2

    if-eqz v2, :cond_68

    const/4 v2, 0x2

    goto :goto_69

    :sswitch_21
    const-string v2, "recents"

    invoke-virtual {p0, v2}, Ljava/lang/String;->equals(Ljava/lang/Object;)Z

    move-result v2

    if-eqz v2, :cond_68

    const/4 v2, 0x7

    goto :goto_69

    :sswitch_2b
    const-string v2, "sleep_timer"

    invoke-virtual {p0, v2}, Ljava/lang/String;->equals(Ljava/lang/Object;)Z

    move-result v2

    if-eqz v2, :cond_68

    const/4 v2, 0x4

    goto :goto_69

    :sswitch_35
    const-string v2, "deezer_flow"

    invoke-virtual {p0, v2}, Ljava/lang/String;->equals(Ljava/lang/Object;)Z

    move-result v2

    if-eqz v2, :cond_68

    move v2, v3

    goto :goto_69

    :sswitch_3f
    const-string v2, "torch"

    invoke-virtual {p0, v2}, Ljava/lang/String;->equals(Ljava/lang/Object;)Z

    move-result v2

    if-eqz v2, :cond_68

    const/4 v2, 0x6

    goto :goto_69

    :sswitch_49
    const-string v2, "prev_track"

    invoke-virtual {p0, v2}, Ljava/lang/String;->equals(Ljava/lang/Object;)Z

    move-result v2

    if-eqz v2, :cond_68

    const/4 v2, 0x1

    goto :goto_69

    :sswitch_53
    const-string v2, "next_track"

    invoke-virtual {p0, v2}, Ljava/lang/String;->equals(Ljava/lang/Object;)Z

    move-result v2

    if-eqz v2, :cond_68

    const/4 v2, 0x0

    goto :goto_69

    :sswitch_5d
    const-string v2, "custom_app"

    invoke-virtual {p0, v2}, Ljava/lang/String;->equals(Ljava/lang/Object;)Z

    move-result v2

    if-eqz v2, :cond_68

    const/16 v2, 0x8

    goto :goto_69

    :cond_68
    :goto_68
    const/4 v2, -0x1

    :goto_69
    packed-switch v2, :pswitch_data_13c

    .line 86
    new-instance p1, Ljava/lang/StringBuilder;

    goto/16 :goto_f4

    :pswitch_70
    if-eqz p2, :cond_114

    .line 80
    invoke-virtual {p2}, Ljava/lang/String;->isEmpty()Z

    move-result v1

    if-nez v1, :cond_114

    .line 81
    invoke-static {p1, p2}, Lcom/boop/shieldhome/BoopKeyActionExecutor;->launchComponent(Landroid/content/Context;Ljava/lang/String;)V

    return-void

    .line 67
    :pswitch_7c
    invoke-static {p1}, Lcom/boop/shieldhome/RecentsPreload;->open(Landroid/content/Context;)V

    const-string p1, "Dispatched: Recents / Task Switcher"

    invoke-static {v0, p1}, Landroid/util/Log;->i(Ljava/lang/String;Ljava/lang/String;)I

    return-void

    .line 62
    :pswitch_85
    invoke-static {p1}, Lcom/boop/shieldhome/ShieldBrightnessController;->get(Landroid/content/Context;)Lcom/boop/shieldhome/ShieldBrightnessController;

    move-result-object p1

    invoke-virtual {p1}, Lcom/boop/shieldhome/ShieldBrightnessController;->toggleTorch()V

    .line 63
    const-string p1, "Dispatched: Screen Torch (Max White)"

    invoke-static {v0, p1}, Landroid/util/Log;->i(Ljava/lang/String;Ljava/lang/String;)I

    return-void

    .line 57
    :pswitch_92
    invoke-static {p1}, Lcom/boop/shieldhome/ShieldBrightnessController;->get(Landroid/content/Context;)Lcom/boop/shieldhome/ShieldBrightnessController;

    move-result-object p1

    invoke-virtual {p1}, Lcom/boop/shieldhome/ShieldBrightnessController;->toggle()V

    .line 58
    const-string p1, "Dispatched: Screen Brightness Slider"

    invoke-static {v0, p1}, Landroid/util/Log;->i(Ljava/lang/String;Ljava/lang/String;)I

    return-void

    .line 52
    :pswitch_9f
    const-string p2, "com.neon.sleeptvtimerpro"

    const-string v1, "com.neon.sleeptvtimerpro.MainActivity"

    invoke-static {p1, p2, v1}, Lcom/boop/shieldhome/BoopKeyActionExecutor;->launchApp(Landroid/content/Context;Ljava/lang/String;Ljava/lang/String;)V

    .line 53
    const-string p1, "Dispatched: Sleep TV Timer Pro"

    invoke-static {v0, p1}, Landroid/util/Log;->i(Ljava/lang/String;Ljava/lang/String;)I

    return-void

    .line 47
    :pswitch_ac
    invoke-static {p1}, Lcom/boop/shieldhome/ShieldNowPlayingManager;->get(Landroid/content/Context;)Lcom/boop/shieldhome/ShieldNowPlayingManager;

    move-result-object p1

    invoke-virtual {p1}, Lcom/boop/shieldhome/ShieldNowPlayingManager;->playDeezerFlow()Z

    .line 48
    const-string p1, "Dispatched: Background Deezer Flow"

    invoke-static {v0, p1}, Landroid/util/Log;->i(Ljava/lang/String;Ljava/lang/String;)I

    return-void

    .line 42
    :pswitch_b9
    invoke-static {p1}, Lcom/boop/shieldhome/ShieldNowPlayingManager;->get(Landroid/content/Context;)Lcom/boop/shieldhome/ShieldNowPlayingManager;

    move-result-object p1

    invoke-virtual {p1}, Lcom/boop/shieldhome/ShieldNowPlayingManager;->togglePlayPause()V

    .line 43
    const-string p1, "Dispatched: Play / Pause toggle"

    invoke-static {v0, p1}, Landroid/util/Log;->i(Ljava/lang/String;Ljava/lang/String;)I

    return-void

    .line 35
    :pswitch_c6
    invoke-static {p1}, Lcom/boop/shieldhome/ShieldNowPlayingManager;->get(Landroid/content/Context;)Lcom/boop/shieldhome/ShieldNowPlayingManager;

    move-result-object p2

    sget-object v1, Lcom/boop/shared/MediaRequest$Kind;->PREVIOUS:Lcom/boop/shared/MediaRequest$Kind;

    invoke-virtual {p2, v1}, Lcom/boop/shieldhome/ShieldNowPlayingManager;->requestTransport(Lcom/boop/shared/MediaRequest$Kind;)Z

    move-result p2

    if-nez p2, :cond_d7

    const/16 p2, 0x58

    .line 36
    invoke-static {p1, p2}, Lcom/boop/shieldhome/BoopKeyActionExecutor;->dispatchMediaKeyFallback(Landroid/content/Context;I)V

    .line 38
    :cond_d7
    const-string p1, "Dispatched: Previous Track"

    invoke-static {v0, p1}, Landroid/util/Log;->i(Ljava/lang/String;Ljava/lang/String;)I

    return-void

    .line 28
    :pswitch_dd
    invoke-static {p1}, Lcom/boop/shieldhome/ShieldNowPlayingManager;->get(Landroid/content/Context;)Lcom/boop/shieldhome/ShieldNowPlayingManager;

    move-result-object p2

    sget-object v1, Lcom/boop/shared/MediaRequest$Kind;->NEXT:Lcom/boop/shared/MediaRequest$Kind;

    invoke-virtual {p2, v1}, Lcom/boop/shieldhome/ShieldNowPlayingManager;->requestTransport(Lcom/boop/shared/MediaRequest$Kind;)Z

    move-result p2

    if-nez p2, :cond_ee

    const/16 p2, 0x57

    .line 29
    invoke-static {p1, p2}, Lcom/boop/shieldhome/BoopKeyActionExecutor;->dispatchMediaKeyFallback(Landroid/content/Context;I)V

    .line 31
    :cond_ee
    const-string p1, "Dispatched: Next Track"

    invoke-static {v0, p1}, Landroid/util/Log;->i(Ljava/lang/String;Ljava/lang/String;)I

    return-void

    .line 86
    :goto_f4
    invoke-direct {p1, v1}, Ljava/lang/StringBuilder;-><init>(Ljava/lang/String;)V

    invoke-virtual {p1, p0}, Ljava/lang/StringBuilder;->append(Ljava/lang/String;)Ljava/lang/StringBuilder;

    invoke-virtual {p1}, Ljava/lang/StringBuilder;->toString()Ljava/lang/String;

    move-result-object p1

    invoke-static {v0, p1}, Landroid/util/Log;->w(Ljava/lang/String;Ljava/lang/String;)I
    :try_end_101
    .catch Ljava/lang/Exception; {:try_start_4 .. :try_end_101} :catch_102

    return-void

    :catch_102
    move-exception p1

    .line 90
    new-instance p2, Ljava/lang/StringBuilder;

    const-string v1, "Failed to execute key action: "

    invoke-direct {p2, v1}, Ljava/lang/StringBuilder;-><init>(Ljava/lang/String;)V

    invoke-virtual {p2, p0}, Ljava/lang/StringBuilder;->append(Ljava/lang/String;)Ljava/lang/StringBuilder;

    invoke-virtual {p2}, Ljava/lang/StringBuilder;->toString()Ljava/lang/String;

    move-result-object p0

    invoke-static {v0, p0, p1}, Landroid/util/Log;->w(Ljava/lang/String;Ljava/lang/String;Ljava/lang/Throwable;)I

    :cond_114
    return-void

    nop

    :sswitch_data_116
    .sparse-switch
        -0x5e41ebad -> :sswitch_5d
        -0x36dd6301 -> :sswitch_53
        -0x12babbc1 -> :sswitch_49
        0x696d3fc -> :sswitch_3f
        0x316733ca -> :sswitch_35
        0x39f8dbdd -> :sswitch_2b
        0x40828578 -> :sswitch_21
        0x7298dd2b -> :sswitch_17
        0x7ce3fc8f -> :sswitch_d
    .end sparse-switch

    :pswitch_data_13c
    .packed-switch 0x0
        :pswitch_dd
        :pswitch_c6
        :pswitch_b9
        :pswitch_ac
        :pswitch_9f
        :pswitch_92
        :pswitch_85
        :pswitch_7c
        :pswitch_70
    .end packed-switch
.end method

.method private static launchApp(Landroid/content/Context;Ljava/lang/String;Ljava/lang/String;)V
    .locals 2

    .line 112
    :try_start_0
    new-instance v0, Landroid/content/Intent;

    const-string v1, "android.intent.action.MAIN"

    invoke-direct {v0, v1}, Landroid/content/Intent;-><init>(Ljava/lang/String;)V

    .line 113
    new-instance v1, Landroid/content/ComponentName;

    invoke-direct {v1, p1, p2}, Landroid/content/ComponentName;-><init>(Ljava/lang/String;Ljava/lang/String;)V

    invoke-virtual {v0, v1}, Landroid/content/Intent;->setComponent(Landroid/content/ComponentName;)Landroid/content/Intent;

    const/high16 p2, 0x10200000

    .line 114
    invoke-virtual {v0, p2}, Landroid/content/Intent;->addFlags(I)Landroid/content/Intent;

    .line 115
    invoke-virtual {p0, v0}, Landroid/content/Context;->startActivity(Landroid/content/Intent;)V
    :try_end_17
    .catch Ljava/lang/Exception; {:try_start_0 .. :try_end_17} :catch_18

    return-void

    .line 118
    :catch_18
    :try_start_18
    invoke-virtual {p0}, Landroid/content/Context;->getPackageManager()Landroid/content/pm/PackageManager;

    move-result-object p2

    invoke-virtual {p2, p1}, Landroid/content/pm/PackageManager;->getLaunchIntentForPackage(Ljava/lang/String;)Landroid/content/Intent;

    move-result-object p2

    if-eqz p2, :cond_3f

    const/high16 v0, 0x10000000

    .line 120
    invoke-virtual {p2, v0}, Landroid/content/Intent;->addFlags(I)Landroid/content/Intent;

    .line 121
    invoke-virtual {p0, p2}, Landroid/content/Context;->startActivity(Landroid/content/Intent;)V
    :try_end_2a
    .catch Ljava/lang/Exception; {:try_start_18 .. :try_end_2a} :catch_2b

    goto :goto_3f

    :catch_2b
    move-exception p0

    .line 124
    new-instance p2, Ljava/lang/StringBuilder;

    const-string v0, "Unable to launch app: "

    invoke-direct {p2, v0}, Ljava/lang/StringBuilder;-><init>(Ljava/lang/String;)V

    invoke-virtual {p2, p1}, Ljava/lang/StringBuilder;->append(Ljava/lang/String;)Ljava/lang/StringBuilder;

    invoke-virtual {p2}, Ljava/lang/StringBuilder;->toString()Ljava/lang/String;

    move-result-object p1

    const-string p2, "BOOPKeyAction"

    invoke-static {p2, p1, p0}, Landroid/util/Log;->w(Ljava/lang/String;Ljava/lang/String;Ljava/lang/Throwable;)I

    :cond_3f
    :goto_3f
    return-void
.end method

.method private static launchComponent(Landroid/content/Context;Ljava/lang/String;)V
    .locals 5

    .line 141
    :try_start_0
    const-string v0, "/"

    invoke-virtual {p1, v0}, Ljava/lang/String;->split(Ljava/lang/String;)[Ljava/lang/String;

    move-result-object v0

    .line 142
    array-length v1, v0

    const/4 v2, 0x2

    if-ne v1, v2, :cond_44

    const/4 v1, 0x0

    .line 143
    aget-object v1, v0, v1

    const/4 v2, 0x1

    .line 144
    aget-object v3, v0, v2

    const-string v4, "."

    invoke-virtual {v3, v4}, Ljava/lang/String;->startsWith(Ljava/lang/String;)Z

    move-result v3

    if-eqz v3, :cond_2a

    aget-object v0, v0, v2

    new-instance v2, Ljava/lang/StringBuilder;

    invoke-direct {v2}, Ljava/lang/StringBuilder;-><init>()V

    invoke-virtual {v2, v1}, Ljava/lang/StringBuilder;->append(Ljava/lang/String;)Ljava/lang/StringBuilder;

    invoke-virtual {v2, v0}, Ljava/lang/StringBuilder;->append(Ljava/lang/String;)Ljava/lang/StringBuilder;

    invoke-virtual {v2}, Ljava/lang/StringBuilder;->toString()Ljava/lang/String;

    move-result-object v0

    goto :goto_2c

    :cond_2a
    aget-object v0, v0, v2

    .line 145
    :goto_2c
    invoke-static {p0, v1, v0}, Lcom/boop/shieldhome/BoopKeyActionExecutor;->launchApp(Landroid/content/Context;Ljava/lang/String;Ljava/lang/String;)V
    :try_end_2f
    .catch Ljava/lang/Exception; {:try_start_0 .. :try_end_2f} :catch_30

    return-void

    :catch_30
    move-exception p0

    .line 148
    new-instance v0, Ljava/lang/StringBuilder;

    const-string v1, "Unable to launch component: "

    invoke-direct {v0, v1}, Ljava/lang/StringBuilder;-><init>(Ljava/lang/String;)V

    invoke-virtual {v0, p1}, Ljava/lang/StringBuilder;->append(Ljava/lang/String;)Ljava/lang/StringBuilder;

    invoke-virtual {v0}, Ljava/lang/StringBuilder;->toString()Ljava/lang/String;

    move-result-object p1

    const-string v0, "BOOPKeyAction"

    invoke-static {v0, p1, p0}, Landroid/util/Log;->w(Ljava/lang/String;Ljava/lang/String;Ljava/lang/Throwable;)I

    :cond_44
    return-void
.end method
