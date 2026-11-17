.class public Lcom/boop/shieldhome/ShieldHomeOverrideService;
.super Landroid/accessibilityservice/AccessibilityService;
.source "ShieldHomeOverrideService.java"


# static fields
.field private static final DOUBLE_TAP_TIMEOUT_MS:J = 0x118L

.field private static final LONG_PRESS_MS:J = 0x190L

.field private static final RELAUNCH_GUARD_MS:J = 0x15eL

.field private static final TAG:Ljava/lang/String; = "BOOPHomeOverride"

.field public static volatile isBound:Z


# instance fields
.field private final keyHandler:Landroid/os/Handler;

.field private lastLaunchElapsed:J

.field private lastTapCode:I

.field private lastTapUpElapsed:J

.field private longPressTriggered:Z

.field private mappingStore:Lcom/boop/shieldhome/BoopKeyMappingStore;

.field private pendingKeyCode:I

.field private pendingLongPress:Ljava/lang/Runnable;

.field private pendingSingleTap:Ljava/lang/Runnable;


# direct methods
.method public static synthetic $r8$lambda$fbVsAhL-NK1gKxZc0_5O22PnZdk(Lcom/boop/shieldhome/ShieldHomeOverrideService;ILjava/lang/String;Ljava/lang/String;)V
    .locals 0

    invoke-direct {p0, p1, p2, p3}, Lcom/boop/shieldhome/ShieldHomeOverrideService;->lambda$onKeyEvent$1(ILjava/lang/String;Ljava/lang/String;)V

    return-void
.end method

.method public static synthetic $r8$lambda$hXVKlnpGT69XaqkwxThBOFsG4E0(Lcom/boop/shieldhome/ShieldHomeOverrideService;ILjava/lang/String;Ljava/lang/String;)V
    .locals 0

    invoke-direct {p0, p1, p2, p3}, Lcom/boop/shieldhome/ShieldHomeOverrideService;->lambda$onKeyEvent$0(ILjava/lang/String;Ljava/lang/String;)V

    return-void
.end method

.method static constructor <clinit>()V
    .locals 0

    return-void
.end method

.method public constructor <init>()V
    .locals 4

    .line 24
    invoke-direct {p0}, Landroid/accessibilityservice/AccessibilityService;-><init>()V

    .line 34
    new-instance v0, Landroid/os/Handler;

    invoke-static {}, Landroid/os/Looper;->getMainLooper()Landroid/os/Looper;

    move-result-object v1

    invoke-direct {v0, v1}, Landroid/os/Handler;-><init>(Landroid/os/Looper;)V

    iput-object v0, p0, Lcom/boop/shieldhome/ShieldHomeOverrideService;->keyHandler:Landroid/os/Handler;

    const/4 v0, -0x1

    .line 37
    iput v0, p0, Lcom/boop/shieldhome/ShieldHomeOverrideService;->pendingKeyCode:I

    const/4 v1, 0x0

    .line 38
    iput-boolean v1, p0, Lcom/boop/shieldhome/ShieldHomeOverrideService;->longPressTriggered:Z

    const/4 v1, 0x0

    .line 39
    iput-object v1, p0, Lcom/boop/shieldhome/ShieldHomeOverrideService;->pendingLongPress:Ljava/lang/Runnable;

    .line 41
    iput v0, p0, Lcom/boop/shieldhome/ShieldHomeOverrideService;->lastTapCode:I

    const-wide/16 v2, 0x0

    .line 42
    iput-wide v2, p0, Lcom/boop/shieldhome/ShieldHomeOverrideService;->lastTapUpElapsed:J

    .line 43
    iput-object v1, p0, Lcom/boop/shieldhome/ShieldHomeOverrideService;->pendingSingleTap:Ljava/lang/Runnable;

    return-void
.end method

.method private bringBoopToFront()V
    .locals 2

    .line 411
    invoke-virtual {p0}, Lcom/boop/shieldhome/ShieldHomeOverrideService;->homeProfileEnabled()Z

    move-result v0

    if-nez v0, :cond_7

    goto :goto_14

    .line 412
    :cond_7
    invoke-virtual {p0}, Lcom/boop/shieldhome/ShieldHomeOverrideService;->homeIntent()Landroid/content/Intent;

    move-result-object v0

    const/high16 v1, 0x34000000

    .line 413
    invoke-virtual {v0, v1}, Landroid/content/Intent;->addFlags(I)Landroid/content/Intent;

    move-result-object v0

    .line 417
    :try_start_11
    invoke-virtual {p0, v0}, Lcom/boop/shieldhome/ShieldHomeOverrideService;->startActivity(Landroid/content/Intent;)V
    :try_end_14
    .catch Landroid/content/ActivityNotFoundException; {:try_start_11 .. :try_end_14} :catch_14
    .catch Ljava/lang/SecurityException; {:try_start_11 .. :try_end_14} :catch_14

    :catch_14
    :goto_14
    return-void
.end method

.method private ensureShieldMenuCustomization()V
    .locals 4

    .line 81
    const-string v0, "BOOPHomeOverride"

    const-string v1, "customize_menu_option"

    const-string v2, "13"

    :try_start_6
    invoke-virtual {p0}, Lcom/boop/shieldhome/ShieldHomeOverrideService;->getContentResolver()Landroid/content/ContentResolver;

    move-result-object v3

    invoke-static {v3, v1}, Landroid/provider/Settings$Global;->getString(Landroid/content/ContentResolver;Ljava/lang/String;)Ljava/lang/String;

    move-result-object v3

    .line 82
    invoke-virtual {v2, v3}, Ljava/lang/String;->equals(Ljava/lang/Object;)Z

    move-result v3

    if-nez v3, :cond_39

    .line 83
    invoke-virtual {p0}, Lcom/boop/shieldhome/ShieldHomeOverrideService;->getContentResolver()Landroid/content/ContentResolver;

    move-result-object v3

    invoke-static {v3, v1, v2}, Landroid/provider/Settings$Global;->putString(Landroid/content/ContentResolver;Ljava/lang/String;Ljava/lang/String;)Z

    .line 84
    invoke-virtual {p0}, Lcom/boop/shieldhome/ShieldHomeOverrideService;->getContentResolver()Landroid/content/ContentResolver;

    move-result-object v1

    const-string v3, "customize_menu_double_press_option"

    invoke-static {v1, v3, v2}, Landroid/provider/Settings$Global;->putString(Landroid/content/ContentResolver;Ljava/lang/String;Ljava/lang/String;)Z

    .line 85
    invoke-virtual {p0}, Lcom/boop/shieldhome/ShieldHomeOverrideService;->getContentResolver()Landroid/content/ContentResolver;

    move-result-object v1

    const-string v3, "customize_menu_hold_option"

    invoke-static {v1, v3, v2}, Landroid/provider/Settings$Global;->putString(Landroid/content/ContentResolver;Ljava/lang/String;Ljava/lang/String;)Z

    .line 86
    const-string v1, "Shield menu button customization locked to option 13 (Send Menu command to app)"

    invoke-static {v0, v1}, Landroid/util/Log;->i(Ljava/lang/String;Ljava/lang/String;)I
    :try_end_32
    .catchall {:try_start_6 .. :try_end_32} :catchall_33

    return-void

    :catchall_33
    move-exception v1

    .line 89
    const-string v2, "Unable to set customize_menu_option in Settings.Global"

    invoke-static {v0, v2, v1}, Landroid/util/Log;->w(Ljava/lang/String;Ljava/lang/String;Ljava/lang/Throwable;)I

    :cond_39
    return-void
.end method

.method private handleSystemDialogConfirmation()V
    .locals 8

    .line 371
    const-string v0, "BOOPHomeOverride"

    :try_start_2
    invoke-virtual {p0}, Lcom/boop/shieldhome/ShieldHomeOverrideService;->getRootInActiveWindow()Landroid/view/accessibility/AccessibilityNodeInfo;

    move-result-object v1

    if-nez v1, :cond_a

    goto/16 :goto_a6

    .line 374
    :cond_a
    const-string v2, "android:id/button1"

    invoke-virtual {v1, v2}, Landroid/view/accessibility/AccessibilityNodeInfo;->findAccessibilityNodeInfosByViewId(Ljava/lang/String;)Ljava/util/List;

    move-result-object v2
    :try_end_10
    .catchall {:try_start_2 .. :try_end_10} :catchall_a0

    .line 375
    const-string v3, "Start now"

    const/16 v4, 0x10

    if-eqz v2, :cond_60

    :try_start_16
    invoke-interface {v2}, Ljava/util/List;->isEmpty()Z

    move-result v5

    if-nez v5, :cond_60

    .line 376
    invoke-interface {v2}, Ljava/util/List;->iterator()Ljava/util/Iterator;

    move-result-object v2

    :cond_20
    invoke-interface {v2}, Ljava/util/Iterator;->hasNext()Z

    move-result v5

    if-eqz v5, :cond_60

    invoke-interface {v2}, Ljava/util/Iterator;->next()Ljava/lang/Object;

    move-result-object v5

    check-cast v5, Landroid/view/accessibility/AccessibilityNodeInfo;

    .line 377
    invoke-virtual {v5}, Landroid/view/accessibility/AccessibilityNodeInfo;->getText()Ljava/lang/CharSequence;

    move-result-object v6

    if-eqz v6, :cond_20

    .line 379
    invoke-interface {v6}, Ljava/lang/CharSequence;->toString()Ljava/lang/String;

    move-result-object v6

    invoke-virtual {v6}, Ljava/lang/String;->trim()Ljava/lang/String;

    move-result-object v6

    .line 380
    invoke-virtual {v3, v6}, Ljava/lang/String;->equalsIgnoreCase(Ljava/lang/String;)Z

    move-result v7

    if-nez v7, :cond_48

    const-string v7, "START NOW"

    invoke-virtual {v7, v6}, Ljava/lang/String;->equalsIgnoreCase(Ljava/lang/String;)Z

    move-result v7

    if-eqz v7, :cond_20

    .line 381
    :cond_48
    new-instance v1, Ljava/lang/StringBuilder;

    invoke-direct {v1}, Ljava/lang/StringBuilder;-><init>()V

    const-string v2, "Auto-confirming MediaProjection dialog via button1: "

    invoke-virtual {v1, v2}, Ljava/lang/StringBuilder;->append(Ljava/lang/String;)Ljava/lang/StringBuilder;

    invoke-virtual {v1, v6}, Ljava/lang/StringBuilder;->append(Ljava/lang/String;)Ljava/lang/StringBuilder;

    invoke-virtual {v1}, Ljava/lang/StringBuilder;->toString()Ljava/lang/String;

    move-result-object v1

    invoke-static {v0, v1}, Landroid/util/Log;->i(Ljava/lang/String;Ljava/lang/String;)I

    .line 382
    invoke-virtual {v5, v4}, Landroid/view/accessibility/AccessibilityNodeInfo;->performAction(I)Z

    return-void

    .line 389
    :cond_60
    invoke-virtual {v1, v3}, Landroid/view/accessibility/AccessibilityNodeInfo;->findAccessibilityNodeInfosByText(Ljava/lang/String;)Ljava/util/List;

    move-result-object v1

    if-eqz v1, :cond_a6

    .line 390
    invoke-interface {v1}, Ljava/util/List;->isEmpty()Z

    move-result v2

    if-nez v2, :cond_a6

    .line 391
    invoke-interface {v1}, Ljava/util/List;->iterator()Ljava/util/Iterator;

    move-result-object v1

    :cond_70
    invoke-interface {v1}, Ljava/util/Iterator;->hasNext()Z

    move-result v2

    if-eqz v2, :cond_a6

    invoke-interface {v1}, Ljava/util/Iterator;->next()Ljava/lang/Object;

    move-result-object v2

    check-cast v2, Landroid/view/accessibility/AccessibilityNodeInfo;

    .line 392
    invoke-virtual {v2}, Landroid/view/accessibility/AccessibilityNodeInfo;->isClickable()Z

    move-result v3

    if-eqz v3, :cond_8b

    .line 393
    const-string v1, "Auto-confirming MediaProjection dialog via clickable text node"

    invoke-static {v0, v1}, Landroid/util/Log;->i(Ljava/lang/String;Ljava/lang/String;)I

    .line 394
    invoke-virtual {v2, v4}, Landroid/view/accessibility/AccessibilityNodeInfo;->performAction(I)Z

    return-void

    .line 397
    :cond_8b
    invoke-virtual {v2}, Landroid/view/accessibility/AccessibilityNodeInfo;->getParent()Landroid/view/accessibility/AccessibilityNodeInfo;

    move-result-object v2

    if-eqz v2, :cond_70

    .line 398
    invoke-virtual {v2}, Landroid/view/accessibility/AccessibilityNodeInfo;->isClickable()Z

    move-result v3

    if-eqz v3, :cond_70

    .line 399
    const-string v1, "Auto-confirming MediaProjection dialog via clickable parent node"

    invoke-static {v0, v1}, Landroid/util/Log;->i(Ljava/lang/String;Ljava/lang/String;)I

    .line 400
    invoke-virtual {v2, v4}, Landroid/view/accessibility/AccessibilityNodeInfo;->performAction(I)Z
    :try_end_9f
    .catchall {:try_start_16 .. :try_end_9f} :catchall_a0

    return-void

    :catchall_a0
    move-exception v1

    .line 406
    const-string v2, "Error handling system dialog confirmation"

    invoke-static {v0, v2, v1}, Landroid/util/Log;->w(Ljava/lang/String;Ljava/lang/String;Ljava/lang/Throwable;)I

    :cond_a6
    :goto_a6
    return-void
.end method

.method private handleViewClicked(Landroid/view/accessibility/AccessibilityEvent;)V
    .locals 3

    .line 340
    const-string v0, "BOOPHomeOverride"

    invoke-virtual {p1}, Landroid/view/accessibility/AccessibilityEvent;->getSource()Landroid/view/accessibility/AccessibilityNodeInfo;

    move-result-object p1

    if-nez p1, :cond_9

    goto :goto_67

    .line 343
    :cond_9
    :try_start_9
    invoke-virtual {p1}, Landroid/view/accessibility/AccessibilityNodeInfo;->getViewIdResourceName()Ljava/lang/String;

    move-result-object v1

    if-eqz v1, :cond_67

    .line 344
    invoke-interface {v1}, Ljava/lang/CharSequence;->toString()Ljava/lang/String;

    move-result-object v1

    const-string v2, "dismiss_icon"

    invoke-virtual {v1, v2}, Ljava/lang/String;->contains(Ljava/lang/CharSequence;)Z

    move-result v1

    if-eqz v1, :cond_67

    .line 346
    invoke-virtual {p1}, Landroid/view/accessibility/AccessibilityNodeInfo;->getParent()Landroid/view/accessibility/AccessibilityNodeInfo;

    move-result-object p1

    :goto_1f
    if-eqz p1, :cond_46

    .line 349
    const-string v1, "com.android.systemui:id/card_title_text"

    invoke-virtual {p1, v1}, Landroid/view/accessibility/AccessibilityNodeInfo;->findAccessibilityNodeInfosByViewId(Ljava/lang/String;)Ljava/util/List;

    move-result-object v1

    if-eqz v1, :cond_41

    .line 350
    invoke-interface {v1}, Ljava/util/List;->isEmpty()Z

    move-result v2

    if-nez v2, :cond_41

    const/4 v2, 0x0

    .line 351
    invoke-interface {v1, v2}, Ljava/util/List;->get(I)Ljava/lang/Object;

    move-result-object v1

    check-cast v1, Landroid/view/accessibility/AccessibilityNodeInfo;

    invoke-virtual {v1}, Landroid/view/accessibility/AccessibilityNodeInfo;->getText()Ljava/lang/CharSequence;

    move-result-object v1

    if-eqz v1, :cond_41

    .line 353
    invoke-interface {v1}, Ljava/lang/CharSequence;->toString()Ljava/lang/String;

    move-result-object p1

    goto :goto_47

    .line 357
    :cond_41
    invoke-virtual {p1}, Landroid/view/accessibility/AccessibilityNodeInfo;->getParent()Landroid/view/accessibility/AccessibilityNodeInfo;

    move-result-object p1

    goto :goto_1f

    :cond_46
    const/4 p1, 0x0

    :goto_47
    if-eqz p1, :cond_67

    .line 360
    new-instance v1, Ljava/lang/StringBuilder;

    invoke-direct {v1}, Ljava/lang/StringBuilder;-><init>()V

    const-string v2, "Recents dismiss clicked for title: "

    invoke-virtual {v1, v2}, Ljava/lang/StringBuilder;->append(Ljava/lang/String;)Ljava/lang/StringBuilder;

    invoke-virtual {v1, p1}, Ljava/lang/StringBuilder;->append(Ljava/lang/String;)Ljava/lang/StringBuilder;

    invoke-virtual {v1}, Ljava/lang/StringBuilder;->toString()Ljava/lang/String;

    move-result-object v1

    invoke-static {v0, v1}, Landroid/util/Log;->i(Ljava/lang/String;Ljava/lang/String;)I

    .line 361
    invoke-static {p0, p1}, Lcom/boop/shieldhome/BoopTaskKiller;->forceStopByTitle(Landroid/content/Context;Ljava/lang/String;)V
    :try_end_60
    .catch Ljava/lang/Exception; {:try_start_9 .. :try_end_60} :catch_61

    return-void

    :catch_61
    move-exception p1

    .line 365
    const-string v1, "Error handling view click"

    invoke-static {v0, v1, p1}, Landroid/util/Log;->w(Ljava/lang/String;Ljava/lang/String;Ljava/lang/Throwable;)I

    :cond_67
    :goto_67
    return-void
.end method

.method public static isServiceBound()Z
    .locals 1

    .line 31
    sget-boolean v0, Lcom/boop/shieldhome/ShieldHomeOverrideService;->isBound:Z

    return v0
.end method

.method private synthetic lambda$onKeyEvent$0(ILjava/lang/String;Ljava/lang/String;)V
    .locals 2

    const/4 v0, 0x1

    .line 211
    iput-boolean v0, p0, Lcom/boop/shieldhome/ShieldHomeOverrideService;->longPressTriggered:Z

    const/4 v0, -0x1

    .line 212
    iput v0, p0, Lcom/boop/shieldhome/ShieldHomeOverrideService;->lastTapCode:I

    .line 213
    new-instance v0, Ljava/lang/StringBuilder;

    const-string v1, "Hardware key "

    invoke-direct {v0, v1}, Ljava/lang/StringBuilder;-><init>(Ljava/lang/String;)V

    invoke-virtual {v0, p1}, Ljava/lang/StringBuilder;->append(I)Ljava/lang/StringBuilder;

    const-string p1, " long-press triggered: "

    invoke-virtual {v0, p1}, Ljava/lang/StringBuilder;->append(Ljava/lang/String;)Ljava/lang/StringBuilder;

    invoke-virtual {v0, p2}, Ljava/lang/StringBuilder;->append(Ljava/lang/String;)Ljava/lang/StringBuilder;

    invoke-virtual {v0}, Ljava/lang/StringBuilder;->toString()Ljava/lang/String;

    move-result-object p1

    const-string v0, "BOOPHomeOverride"

    invoke-static {v0, p1}, Landroid/util/Log;->i(Ljava/lang/String;Ljava/lang/String;)I

    .line 214
    invoke-static {p0, p2, p3}, Lcom/boop/shieldhome/BoopKeyActionExecutor;->execute(Landroid/content/Context;Ljava/lang/String;Ljava/lang/String;)V

    return-void
.end method

.method private synthetic lambda$onKeyEvent$1(ILjava/lang/String;Ljava/lang/String;)V
    .locals 2

    const/4 v0, -0x1

    .line 280
    iput v0, p0, Lcom/boop/shieldhome/ShieldHomeOverrideService;->lastTapCode:I

    const/4 v0, 0x0

    .line 281
    iput-object v0, p0, Lcom/boop/shieldhome/ShieldHomeOverrideService;->pendingSingleTap:Ljava/lang/Runnable;

    const/4 v0, 0x3

    if-ne p1, v0, :cond_16

    .line 282
    const-string v0, "default"

    invoke-virtual {v0, p2}, Ljava/lang/String;->equals(Ljava/lang/Object;)Z

    move-result v0

    if-eqz v0, :cond_16

    const/4 p1, 0x2

    .line 283
    invoke-virtual {p0, p1}, Lcom/boop/shieldhome/ShieldHomeOverrideService;->performGlobalAction(I)Z

    return-void

    .line 285
    :cond_16
    new-instance v0, Ljava/lang/StringBuilder;

    const-string v1, "Hardware key "

    invoke-direct {v0, v1}, Ljava/lang/StringBuilder;-><init>(Ljava/lang/String;)V

    invoke-virtual {v0, p1}, Ljava/lang/StringBuilder;->append(I)Ljava/lang/StringBuilder;

    const-string p1, " single-tap: "

    invoke-virtual {v0, p1}, Ljava/lang/StringBuilder;->append(Ljava/lang/String;)Ljava/lang/StringBuilder;

    invoke-virtual {v0, p2}, Ljava/lang/StringBuilder;->append(Ljava/lang/String;)Ljava/lang/StringBuilder;

    invoke-virtual {v0}, Ljava/lang/StringBuilder;->toString()Ljava/lang/String;

    move-result-object p1

    const-string v0, "BOOPHomeOverride"

    invoke-static {v0, p1}, Landroid/util/Log;->i(Ljava/lang/String;Ljava/lang/String;)I

    .line 286
    invoke-static {p0, p2, p3}, Lcom/boop/shieldhome/BoopKeyActionExecutor;->execute(Landroid/content/Context;Ljava/lang/String;Ljava/lang/String;)V

    return-void
.end method


# virtual methods
.method protected homeIntent()Landroid/content/Intent;
    .locals 2

    .line 426
    new-instance v0, Landroid/content/Intent;

    const-class v1, Lcom/boop/shieldhome/ShieldLauncherActivity;

    invoke-direct {v0, p0, v1}, Landroid/content/Intent;-><init>(Landroid/content/Context;Ljava/lang/Class;)V

    return-object v0
.end method

.method protected homeProfileEnabled()Z
    .locals 1

    const/4 v0, 0x1

    return v0
.end method

.method public onAccessibilityEvent(Landroid/view/accessibility/AccessibilityEvent;)V
    .locals 6

    .line 311
    invoke-virtual {p0}, Lcom/boop/shieldhome/ShieldHomeOverrideService;->homeProfileEnabled()Z

    move-result v0

    if-eqz v0, :cond_5a

    if-nez p1, :cond_9

    goto :goto_5a

    .line 313
    :cond_9
    invoke-virtual {p1}, Landroid/view/accessibility/AccessibilityEvent;->getEventType()I

    move-result v0

    const/4 v1, 0x1

    if-ne v0, v1, :cond_14

    .line 315
    invoke-direct {p0, p1}, Lcom/boop/shieldhome/ShieldHomeOverrideService;->handleViewClicked(Landroid/view/accessibility/AccessibilityEvent;)V

    return-void

    :cond_14
    const/16 v1, 0x20

    if-ne v0, v1, :cond_5a

    .line 320
    invoke-virtual {p1}, Landroid/view/accessibility/AccessibilityEvent;->getPackageName()Ljava/lang/CharSequence;

    move-result-object p1

    if-nez p1, :cond_20

    const/4 p1, 0x0

    goto :goto_24

    .line 321
    :cond_20
    invoke-interface {p1}, Ljava/lang/CharSequence;->toString()Ljava/lang/String;

    move-result-object p1

    .line 322
    :goto_24
    const-string v0, "com.android.systemui"

    invoke-virtual {v0, p1}, Ljava/lang/String;->equals(Ljava/lang/Object;)Z

    move-result v0

    if-nez v0, :cond_34

    const-string v0, "android"

    invoke-virtual {v0, p1}, Ljava/lang/String;->equals(Ljava/lang/Object;)Z

    move-result v0

    if-eqz v0, :cond_37

    .line 323
    :cond_34
    invoke-direct {p0}, Lcom/boop/shieldhome/ShieldHomeOverrideService;->handleSystemDialogConfirmation()V

    .line 326
    :cond_37
    invoke-virtual {p0}, Lcom/boop/shieldhome/ShieldHomeOverrideService;->homeProfileEnabled()Z

    move-result v0

    invoke-virtual {p0}, Lcom/boop/shieldhome/ShieldHomeOverrideService;->getPackageName()Ljava/lang/String;

    move-result-object v1

    invoke-static {v0, p1, v1}, Lcom/boop/shieldhome/HomeOverridePolicy;->shouldReplaceForeground(ZLjava/lang/String;Ljava/lang/String;)Z

    move-result p1

    if-nez p1, :cond_46

    goto :goto_5a

    .line 330
    :cond_46
    invoke-static {}, Landroid/os/SystemClock;->elapsedRealtime()J

    move-result-wide v0

    .line 331
    iget-wide v2, p0, Lcom/boop/shieldhome/ShieldHomeOverrideService;->lastLaunchElapsed:J

    sub-long v2, v0, v2

    const-wide/16 v4, 0x15e

    cmp-long p1, v2, v4

    if-gez p1, :cond_55

    goto :goto_5a

    .line 334
    :cond_55
    iput-wide v0, p0, Lcom/boop/shieldhome/ShieldHomeOverrideService;->lastLaunchElapsed:J

    .line 335
    invoke-direct {p0}, Lcom/boop/shieldhome/ShieldHomeOverrideService;->bringBoopToFront()V

    :cond_5a
    :goto_5a
    return-void
.end method

.method public onDestroy()V
    .locals 2

    const/4 v0, 0x0

    .line 443
    sput-boolean v0, Lcom/boop/shieldhome/ShieldHomeOverrideService;->isBound:Z

    .line 444
    iget-object v0, p0, Lcom/boop/shieldhome/ShieldHomeOverrideService;->keyHandler:Landroid/os/Handler;

    if-eqz v0, :cond_b

    const/4 v1, 0x0

    .line 445
    invoke-virtual {v0, v1}, Landroid/os/Handler;->removeCallbacksAndMessages(Ljava/lang/Object;)V

    .line 448
    :cond_b
    :try_start_b
    invoke-static {p0}, Lcom/boop/shieldhome/ShieldBrightnessController;->get(Landroid/content/Context;)Lcom/boop/shieldhome/ShieldBrightnessController;

    move-result-object v0

    invoke-virtual {v0}, Lcom/boop/shieldhome/ShieldBrightnessController;->detachService()V
    :try_end_12
    .catchall {:try_start_b .. :try_end_12} :catchall_12

    .line 450
    :catchall_12
    invoke-super {p0}, Landroid/accessibilityservice/AccessibilityService;->onDestroy()V

    return-void
.end method

.method public onInterrupt()V
    .locals 2

    .line 430
    iget-object v0, p0, Lcom/boop/shieldhome/ShieldHomeOverrideService;->keyHandler:Landroid/os/Handler;

    if-eqz v0, :cond_8

    const/4 v1, 0x0

    .line 431
    invoke-virtual {v0, v1}, Landroid/os/Handler;->removeCallbacksAndMessages(Ljava/lang/Object;)V

    :cond_8
    return-void
.end method

.method public onKeyEvent(Landroid/view/KeyEvent;)Z
    .locals 18

    move-object/from16 v0, p0

    .line 95
    invoke-virtual {v0}, Lcom/boop/shieldhome/ShieldHomeOverrideService;->homeProfileEnabled()Z

    move-result v1

    if-eqz v1, :cond_231

    if-nez p1, :cond_c

    goto/16 :goto_231

    .line 99
    :cond_c
    invoke-virtual/range {p1 .. p1}, Landroid/view/KeyEvent;->getKeyCode()I

    move-result v1

    .line 100
    invoke-virtual/range {p1 .. p1}, Landroid/view/KeyEvent;->getAction()I

    move-result v2

    .line 102
    invoke-static {v0}, Lcom/boop/shieldhome/ShieldBrightnessController;->get(Landroid/content/Context;)Lcom/boop/shieldhome/ShieldBrightnessController;

    move-result-object v3

    .line 103
    invoke-virtual {v3}, Lcom/boop/shieldhome/ShieldBrightnessController;->isTorchActive()Z

    move-result v4

    const/16 v5, 0x14

    const/16 v6, 0x13

    const/16 v7, 0x16

    const/16 v8, 0x42

    const/16 v9, 0x17

    const/16 v10, 0x15

    const/4 v11, 0x4

    const/4 v12, 0x3

    const/4 v13, 0x1

    if-eqz v4, :cond_60

    const/16 v4, 0xc7

    if-nez v2, :cond_4d

    if-eq v1, v11, :cond_49

    if-eq v1, v9, :cond_49

    if-eq v1, v8, :cond_49

    if-ne v1, v4, :cond_3a

    goto :goto_49

    :cond_3a
    if-ne v1, v12, :cond_40

    .line 112
    invoke-virtual {v3}, Lcom/boop/shieldhome/ShieldBrightnessController;->hideTorch()V

    goto :goto_60

    :cond_40
    if-eq v1, v10, :cond_48

    if-eq v1, v7, :cond_48

    if-eq v1, v6, :cond_48

    if-ne v1, v5, :cond_60

    :cond_48
    return v13

    .line 109
    :cond_49
    :goto_49
    invoke-virtual {v3}, Lcom/boop/shieldhome/ShieldBrightnessController;->hideTorch()V

    return v13

    :cond_4d
    if-ne v2, v13, :cond_60

    if-eq v1, v11, :cond_5f

    if-eq v1, v9, :cond_5f

    if-eq v1, v8, :cond_5f

    if-eq v1, v4, :cond_5f

    if-eq v1, v10, :cond_5f

    if-eq v1, v7, :cond_5f

    if-eq v1, v6, :cond_5f

    if-ne v1, v5, :cond_60

    :cond_5f
    return v13

    .line 135
    :cond_60
    :goto_60
    invoke-virtual {v3}, Lcom/boop/shieldhome/ShieldBrightnessController;->isHudVisible()Z

    move-result v4

    if-eqz v4, :cond_aa

    const/16 v4, 0x18

    const/16 v14, 0x19

    if-nez v2, :cond_95

    if-eq v1, v10, :cond_90

    if-ne v1, v14, :cond_71

    goto :goto_90

    :cond_71
    if-eq v1, v7, :cond_8b

    if-ne v1, v4, :cond_76

    goto :goto_8b

    :cond_76
    if-eq v1, v9, :cond_87

    if-eq v1, v8, :cond_87

    if-eq v1, v11, :cond_87

    if-eq v1, v6, :cond_87

    if-ne v1, v5, :cond_81

    goto :goto_87

    :cond_81
    if-ne v1, v12, :cond_aa

    .line 151
    invoke-virtual {v3}, Lcom/boop/shieldhome/ShieldBrightnessController;->hide()V

    goto :goto_aa

    .line 148
    :cond_87
    :goto_87
    invoke-virtual {v3}, Lcom/boop/shieldhome/ShieldBrightnessController;->hide()V

    return v13

    :cond_8b
    :goto_8b
    const/4 v1, 0x5

    .line 141
    invoke-virtual {v3, v1}, Lcom/boop/shieldhome/ShieldBrightnessController;->adjust(I)V

    return v13

    :cond_90
    :goto_90
    const/4 v1, -0x5

    .line 138
    invoke-virtual {v3, v1}, Lcom/boop/shieldhome/ShieldBrightnessController;->adjust(I)V

    return v13

    :cond_95
    if-ne v2, v13, :cond_aa

    if-eq v1, v10, :cond_a9

    if-eq v1, v14, :cond_a9

    if-eq v1, v7, :cond_a9

    if-eq v1, v4, :cond_a9

    if-eq v1, v9, :cond_a9

    if-eq v1, v8, :cond_a9

    if-eq v1, v11, :cond_a9

    if-eq v1, v6, :cond_a9

    if-ne v1, v5, :cond_aa

    :cond_a9
    return v13

    .line 165
    :cond_aa
    :goto_aa
    iget-object v3, v0, Lcom/boop/shieldhome/ShieldHomeOverrideService;->mappingStore:Lcom/boop/shieldhome/BoopKeyMappingStore;

    if-nez v3, :cond_b5

    .line 166
    new-instance v3, Lcom/boop/shieldhome/BoopKeyMappingStore;

    invoke-direct {v3, v0}, Lcom/boop/shieldhome/BoopKeyMappingStore;-><init>(Landroid/content/Context;)V

    iput-object v3, v0, Lcom/boop/shieldhome/ShieldHomeOverrideService;->mappingStore:Lcom/boop/shieldhome/BoopKeyMappingStore;

    :cond_b5
    const-wide/16 v3, 0x118

    const/4 v5, 0x0

    const/4 v6, 0x0

    .line 169
    const-string v7, "default"

    if-nez v2, :cond_14a

    .line 170
    invoke-virtual/range {p1 .. p1}, Landroid/view/KeyEvent;->getRepeatCount()I

    move-result v2

    if-nez v2, :cond_140

    .line 171
    iget-object v2, v0, Lcom/boop/shieldhome/ShieldHomeOverrideService;->pendingLongPress:Ljava/lang/Runnable;

    if-eqz v2, :cond_ce

    .line 172
    iget-object v8, v0, Lcom/boop/shieldhome/ShieldHomeOverrideService;->keyHandler:Landroid/os/Handler;

    invoke-virtual {v8, v2}, Landroid/os/Handler;->removeCallbacks(Ljava/lang/Runnable;)V

    .line 173
    iput-object v6, v0, Lcom/boop/shieldhome/ShieldHomeOverrideService;->pendingLongPress:Ljava/lang/Runnable;

    .line 176
    :cond_ce
    iget-object v2, v0, Lcom/boop/shieldhome/ShieldHomeOverrideService;->mappingStore:Lcom/boop/shieldhome/BoopKeyMappingStore;

    invoke-virtual {v2, v1}, Lcom/boop/shieldhome/BoopKeyMappingStore;->getEntry(I)Lcom/boop/shieldhome/BoopKeyMappingStore$KeyEntry;

    move-result-object v2

    if-nez v2, :cond_db

    .line 178
    invoke-super/range {p0 .. p1}, Landroid/accessibilityservice/AccessibilityService;->onKeyEvent(Landroid/view/KeyEvent;)Z

    move-result v1

    return v1

    .line 182
    :cond_db
    iget-object v8, v2, Lcom/boop/shieldhome/BoopKeyMappingStore$KeyEntry;->singleTapAction:Ljava/lang/String;

    invoke-virtual {v7, v8}, Ljava/lang/String;->equals(Ljava/lang/Object;)Z

    move-result v8

    if-eqz v8, :cond_fa

    iget-object v8, v2, Lcom/boop/shieldhome/BoopKeyMappingStore$KeyEntry;->doubleTapAction:Ljava/lang/String;

    .line 183
    invoke-virtual {v7, v8}, Ljava/lang/String;->equals(Ljava/lang/Object;)Z

    move-result v8

    if-eqz v8, :cond_fa

    iget-object v8, v2, Lcom/boop/shieldhome/BoopKeyMappingStore$KeyEntry;->longPressAction:Ljava/lang/String;

    .line 184
    invoke-virtual {v7, v8}, Ljava/lang/String;->equals(Ljava/lang/Object;)Z

    move-result v8

    if-eqz v8, :cond_fa

    if-eq v1, v12, :cond_fa

    .line 186
    invoke-super/range {p0 .. p1}, Landroid/accessibilityservice/AccessibilityService;->onKeyEvent(Landroid/view/KeyEvent;)Z

    move-result v1

    return v1

    .line 189
    :cond_fa
    invoke-static {}, Landroid/os/SystemClock;->elapsedRealtime()J

    move-result-wide v8

    .line 190
    iget-object v10, v0, Lcom/boop/shieldhome/ShieldHomeOverrideService;->pendingSingleTap:Ljava/lang/Runnable;

    if-eqz v10, :cond_121

    .line 191
    iget v11, v0, Lcom/boop/shieldhome/ShieldHomeOverrideService;->lastTapCode:I

    if-ne v1, v11, :cond_115

    iget-wide v11, v0, Lcom/boop/shieldhome/ShieldHomeOverrideService;->lastTapUpElapsed:J

    sub-long/2addr v8, v11

    cmp-long v3, v8, v3

    if-gez v3, :cond_115

    .line 193
    iget-object v3, v0, Lcom/boop/shieldhome/ShieldHomeOverrideService;->keyHandler:Landroid/os/Handler;

    invoke-virtual {v3, v10}, Landroid/os/Handler;->removeCallbacks(Ljava/lang/Runnable;)V

    .line 194
    iput-object v6, v0, Lcom/boop/shieldhome/ShieldHomeOverrideService;->pendingSingleTap:Ljava/lang/Runnable;

    goto :goto_121

    .line 197
    :cond_115
    iget-object v3, v0, Lcom/boop/shieldhome/ShieldHomeOverrideService;->keyHandler:Landroid/os/Handler;

    invoke-virtual {v3, v10}, Landroid/os/Handler;->removeCallbacks(Ljava/lang/Runnable;)V

    .line 198
    iget-object v3, v0, Lcom/boop/shieldhome/ShieldHomeOverrideService;->pendingSingleTap:Ljava/lang/Runnable;

    .line 199
    iput-object v6, v0, Lcom/boop/shieldhome/ShieldHomeOverrideService;->pendingSingleTap:Ljava/lang/Runnable;

    .line 200
    invoke-interface {v3}, Ljava/lang/Runnable;->run()V

    .line 204
    :cond_121
    :goto_121
    iput v1, v0, Lcom/boop/shieldhome/ShieldHomeOverrideService;->pendingKeyCode:I

    .line 205
    iput-boolean v5, v0, Lcom/boop/shieldhome/ShieldHomeOverrideService;->longPressTriggered:Z

    .line 207
    iget-object v3, v2, Lcom/boop/shieldhome/BoopKeyMappingStore$KeyEntry;->longPressAction:Ljava/lang/String;

    invoke-virtual {v7, v3}, Ljava/lang/String;->equals(Ljava/lang/Object;)Z

    move-result v3

    if-nez v3, :cond_13f

    .line 208
    iget-object v3, v2, Lcom/boop/shieldhome/BoopKeyMappingStore$KeyEntry;->longPressAction:Ljava/lang/String;

    .line 209
    iget-object v2, v2, Lcom/boop/shieldhome/BoopKeyMappingStore$KeyEntry;->targetComponent:Ljava/lang/String;

    .line 210
    new-instance v4, Lcom/boop/shieldhome/ShieldHomeOverrideService$$ExternalSyntheticLambda0;

    invoke-direct {v4, v0, v1, v3, v2}, Lcom/boop/shieldhome/ShieldHomeOverrideService$$ExternalSyntheticLambda0;-><init>(Lcom/boop/shieldhome/ShieldHomeOverrideService;ILjava/lang/String;Ljava/lang/String;)V

    iput-object v4, v0, Lcom/boop/shieldhome/ShieldHomeOverrideService;->pendingLongPress:Ljava/lang/Runnable;

    .line 216
    iget-object v1, v0, Lcom/boop/shieldhome/ShieldHomeOverrideService;->keyHandler:Landroid/os/Handler;

    const-wide/16 v2, 0x190

    invoke-virtual {v1, v4, v2, v3}, Landroid/os/Handler;->postDelayed(Ljava/lang/Runnable;J)Z

    :cond_13f
    return v13

    .line 221
    :cond_140
    iget v2, v0, Lcom/boop/shieldhome/ShieldHomeOverrideService;->pendingKeyCode:I

    if-ne v2, v1, :cond_145

    return v13

    .line 224
    :cond_145
    invoke-super/range {p0 .. p1}, Landroid/accessibilityservice/AccessibilityService;->onKeyEvent(Landroid/view/KeyEvent;)Z

    move-result v1

    return v1

    :cond_14a
    if-ne v2, v13, :cond_22c

    .line 227
    iget v2, v0, Lcom/boop/shieldhome/ShieldHomeOverrideService;->pendingKeyCode:I

    if-ne v2, v1, :cond_22c

    .line 228
    iget-object v1, v0, Lcom/boop/shieldhome/ShieldHomeOverrideService;->pendingLongPress:Ljava/lang/Runnable;

    if-eqz v1, :cond_15b

    .line 229
    iget-object v2, v0, Lcom/boop/shieldhome/ShieldHomeOverrideService;->keyHandler:Landroid/os/Handler;

    invoke-virtual {v2, v1}, Landroid/os/Handler;->removeCallbacks(Ljava/lang/Runnable;)V

    .line 230
    iput-object v6, v0, Lcom/boop/shieldhome/ShieldHomeOverrideService;->pendingLongPress:Ljava/lang/Runnable;

    .line 233
    :cond_15b
    iget v1, v0, Lcom/boop/shieldhome/ShieldHomeOverrideService;->pendingKeyCode:I

    .line 234
    iget-boolean v2, v0, Lcom/boop/shieldhome/ShieldHomeOverrideService;->longPressTriggered:Z

    const/4 v8, -0x1

    .line 235
    iput v8, v0, Lcom/boop/shieldhome/ShieldHomeOverrideService;->pendingKeyCode:I

    .line 236
    iput-boolean v5, v0, Lcom/boop/shieldhome/ShieldHomeOverrideService;->longPressTriggered:Z

    if-eqz v2, :cond_169

    .line 239
    iput v8, v0, Lcom/boop/shieldhome/ShieldHomeOverrideService;->lastTapCode:I

    return v13

    .line 243
    :cond_169
    iget-object v2, v0, Lcom/boop/shieldhome/ShieldHomeOverrideService;->mappingStore:Lcom/boop/shieldhome/BoopKeyMappingStore;

    invoke-virtual {v2, v1}, Lcom/boop/shieldhome/BoopKeyMappingStore;->getEntry(I)Lcom/boop/shieldhome/BoopKeyMappingStore$KeyEntry;

    move-result-object v2

    if-nez v2, :cond_172

    return v5

    .line 248
    :cond_172
    iget-object v5, v2, Lcom/boop/shieldhome/BoopKeyMappingStore$KeyEntry;->doubleTapAction:Ljava/lang/String;

    invoke-virtual {v7, v5}, Ljava/lang/String;->equals(Ljava/lang/Object;)Z

    move-result v5

    const-string v9, "Hardware key "

    const-string v10, "BOOPHomeOverride"

    if-eqz v5, :cond_1bb

    if-ne v1, v12, :cond_181

    goto :goto_1bb

    .line 294
    :cond_181
    iput v8, v0, Lcom/boop/shieldhome/ShieldHomeOverrideService;->lastTapCode:I

    if-ne v1, v12, :cond_192

    .line 295
    iget-object v3, v2, Lcom/boop/shieldhome/BoopKeyMappingStore$KeyEntry;->singleTapAction:Ljava/lang/String;

    invoke-virtual {v7, v3}, Ljava/lang/String;->equals(Ljava/lang/Object;)Z

    move-result v3

    if-eqz v3, :cond_192

    const/4 v1, 0x2

    .line 296
    invoke-virtual {v0, v1}, Lcom/boop/shieldhome/ShieldHomeOverrideService;->performGlobalAction(I)Z

    goto :goto_1ba

    .line 297
    :cond_192
    iget-object v3, v2, Lcom/boop/shieldhome/BoopKeyMappingStore$KeyEntry;->singleTapAction:Ljava/lang/String;

    invoke-virtual {v7, v3}, Ljava/lang/String;->equals(Ljava/lang/Object;)Z

    move-result v3

    if-nez v3, :cond_1ba

    .line 298
    iget-object v3, v2, Lcom/boop/shieldhome/BoopKeyMappingStore$KeyEntry;->singleTapAction:Ljava/lang/String;

    new-instance v4, Ljava/lang/StringBuilder;

    invoke-direct {v4, v9}, Ljava/lang/StringBuilder;-><init>(Ljava/lang/String;)V

    invoke-virtual {v4, v1}, Ljava/lang/StringBuilder;->append(I)Ljava/lang/StringBuilder;

    const-string v1, " single-tap: "

    invoke-virtual {v4, v1}, Ljava/lang/StringBuilder;->append(Ljava/lang/String;)Ljava/lang/StringBuilder;

    invoke-virtual {v4, v3}, Ljava/lang/StringBuilder;->append(Ljava/lang/String;)Ljava/lang/StringBuilder;

    invoke-virtual {v4}, Ljava/lang/StringBuilder;->toString()Ljava/lang/String;

    move-result-object v1

    invoke-static {v10, v1}, Landroid/util/Log;->i(Ljava/lang/String;Ljava/lang/String;)I

    .line 299
    iget-object v1, v2, Lcom/boop/shieldhome/BoopKeyMappingStore$KeyEntry;->singleTapAction:Ljava/lang/String;

    iget-object v2, v2, Lcom/boop/shieldhome/BoopKeyMappingStore$KeyEntry;->targetComponent:Ljava/lang/String;

    invoke-static {v0, v1, v2}, Lcom/boop/shieldhome/BoopKeyActionExecutor;->execute(Landroid/content/Context;Ljava/lang/String;Ljava/lang/String;)V

    :cond_1ba
    :goto_1ba
    return v13

    .line 252
    :cond_1bb
    :goto_1bb
    invoke-static {}, Landroid/os/SystemClock;->elapsedRealtime()J

    move-result-wide v14

    .line 253
    iget v5, v0, Lcom/boop/shieldhome/ShieldHomeOverrideService;->lastTapCode:I

    move-wide/from16 v16, v3

    if-ne v1, v5, :cond_215

    iget-wide v3, v0, Lcom/boop/shieldhome/ShieldHomeOverrideService;->lastTapUpElapsed:J

    sub-long v3, v14, v3

    cmp-long v3, v3, v16

    if-gez v3, :cond_215

    .line 255
    iput v8, v0, Lcom/boop/shieldhome/ShieldHomeOverrideService;->lastTapCode:I

    const-wide/16 v3, 0x0

    .line 256
    iput-wide v3, v0, Lcom/boop/shieldhome/ShieldHomeOverrideService;->lastTapUpElapsed:J

    .line 257
    iget-object v3, v0, Lcom/boop/shieldhome/ShieldHomeOverrideService;->pendingSingleTap:Ljava/lang/Runnable;

    if-eqz v3, :cond_1de

    .line 258
    iget-object v4, v0, Lcom/boop/shieldhome/ShieldHomeOverrideService;->keyHandler:Landroid/os/Handler;

    invoke-virtual {v4, v3}, Landroid/os/Handler;->removeCallbacks(Ljava/lang/Runnable;)V

    .line 259
    iput-object v6, v0, Lcom/boop/shieldhome/ShieldHomeOverrideService;->pendingSingleTap:Ljava/lang/Runnable;

    .line 262
    :cond_1de
    new-instance v3, Ljava/lang/StringBuilder;

    invoke-direct {v3, v9}, Ljava/lang/StringBuilder;-><init>(Ljava/lang/String;)V

    invoke-virtual {v3, v1}, Ljava/lang/StringBuilder;->append(I)Ljava/lang/StringBuilder;

    const-string v4, " double-tap triggered"

    invoke-virtual {v3, v4}, Ljava/lang/StringBuilder;->append(Ljava/lang/String;)Ljava/lang/StringBuilder;

    invoke-virtual {v3}, Ljava/lang/StringBuilder;->toString()Ljava/lang/String;

    move-result-object v3

    invoke-static {v10, v3}, Landroid/util/Log;->i(Ljava/lang/String;Ljava/lang/String;)I

    if-ne v1, v12, :cond_20d

    .line 263
    iget-object v1, v2, Lcom/boop/shieldhome/BoopKeyMappingStore$KeyEntry;->doubleTapAction:Ljava/lang/String;

    .line 264
    invoke-virtual {v7, v1}, Ljava/lang/String;->equals(Ljava/lang/Object;)Z

    move-result v1

    const-string v3, "recents"

    if-nez v1, :cond_206

    iget-object v1, v2, Lcom/boop/shieldhome/BoopKeyMappingStore$KeyEntry;->doubleTapAction:Ljava/lang/String;

    .line 265
    invoke-virtual {v3, v1}, Ljava/lang/String;->equals(Ljava/lang/Object;)Z

    move-result v1

    if-eqz v1, :cond_20d

    .line 266
    :cond_206
    const/4 v1, 0x0

    if-nez v1, :cond_214

    .line 267
    invoke-static {v0, v3, v6}, Lcom/boop/shieldhome/BoopKeyActionExecutor;->execute(Landroid/content/Context;Ljava/lang/String;Ljava/lang/String;)V

    goto :goto_214

    .line 270
    :cond_20d
    iget-object v1, v2, Lcom/boop/shieldhome/BoopKeyMappingStore$KeyEntry;->doubleTapAction:Ljava/lang/String;

    iget-object v2, v2, Lcom/boop/shieldhome/BoopKeyMappingStore$KeyEntry;->targetComponent:Ljava/lang/String;

    invoke-static {v0, v1, v2}, Lcom/boop/shieldhome/BoopKeyActionExecutor;->execute(Landroid/content/Context;Ljava/lang/String;Ljava/lang/String;)V

    :cond_214
    :goto_214
    return v13

    .line 275
    :cond_215
    iput v1, v0, Lcom/boop/shieldhome/ShieldHomeOverrideService;->lastTapCode:I

    .line 276
    iput-wide v14, v0, Lcom/boop/shieldhome/ShieldHomeOverrideService;->lastTapUpElapsed:J

    .line 277
    iget-object v3, v2, Lcom/boop/shieldhome/BoopKeyMappingStore$KeyEntry;->singleTapAction:Ljava/lang/String;

    .line 278
    iget-object v2, v2, Lcom/boop/shieldhome/BoopKeyMappingStore$KeyEntry;->targetComponent:Ljava/lang/String;

    .line 279
    new-instance v4, Lcom/boop/shieldhome/ShieldHomeOverrideService$$ExternalSyntheticLambda1;

    invoke-direct {v4, v0, v1, v3, v2}, Lcom/boop/shieldhome/ShieldHomeOverrideService$$ExternalSyntheticLambda1;-><init>(Lcom/boop/shieldhome/ShieldHomeOverrideService;ILjava/lang/String;Ljava/lang/String;)V

    iput-object v4, v0, Lcom/boop/shieldhome/ShieldHomeOverrideService;->pendingSingleTap:Ljava/lang/Runnable;

    .line 289
    iget-object v1, v0, Lcom/boop/shieldhome/ShieldHomeOverrideService;->keyHandler:Landroid/os/Handler;

    move-wide/from16 v2, v16

    invoke-virtual {v1, v4, v2, v3}, Landroid/os/Handler;->postDelayed(Ljava/lang/Runnable;J)Z

    return v13

    .line 306
    :cond_22c
    invoke-super/range {p0 .. p1}, Landroid/accessibilityservice/AccessibilityService;->onKeyEvent(Landroid/view/KeyEvent;)Z

    move-result v1

    return v1

    .line 96
    :cond_231
    :goto_231
    invoke-super/range {p0 .. p1}, Landroid/accessibilityservice/AccessibilityService;->onKeyEvent(Landroid/view/KeyEvent;)Z

    move-result v1

    return v1
.end method

.method protected onServiceConnected()V
    .locals 5

    .line 47
    invoke-super {p0}, Landroid/accessibilityservice/AccessibilityService;->onServiceConnected()V

    const/4 v0, 0x1

    .line 48
    sput-boolean v0, Lcom/boop/shieldhome/ShieldHomeOverrideService;->isBound:Z

    .line 49
    new-instance v0, Lcom/boop/shieldhome/BoopKeyMappingStore;

    invoke-direct {v0, p0}, Lcom/boop/shieldhome/BoopKeyMappingStore;-><init>(Landroid/content/Context;)V

    iput-object v0, p0, Lcom/boop/shieldhome/ShieldHomeOverrideService;->mappingStore:Lcom/boop/shieldhome/BoopKeyMappingStore;

    .line 51
    invoke-virtual {p0}, Lcom/boop/shieldhome/ShieldHomeOverrideService;->getServiceInfo()Landroid/accessibilityservice/AccessibilityServiceInfo;

    move-result-object v0

    if-eqz v0, :cond_2a

    .line 53
    iget v1, v0, Landroid/accessibilityservice/AccessibilityServiceInfo;->eventTypes:I

    or-int/lit8 v1, v1, 0x21

    iput v1, v0, Landroid/accessibilityservice/AccessibilityServiceInfo;->eventTypes:I

    const/16 v1, 0x10

    .line 54
    iput v1, v0, Landroid/accessibilityservice/AccessibilityServiceInfo;->feedbackType:I

    .line 55
    iget v1, v0, Landroid/accessibilityservice/AccessibilityServiceInfo;->flags:I

    or-int/lit8 v1, v1, 0x60

    iput v1, v0, Landroid/accessibilityservice/AccessibilityServiceInfo;->flags:I

    const-wide/16 v1, 0x0

    .line 57
    iput-wide v1, v0, Landroid/accessibilityservice/AccessibilityServiceInfo;->notificationTimeout:J

    .line 58
    invoke-virtual {p0, v0}, Lcom/boop/shieldhome/ShieldHomeOverrideService;->setServiceInfo(Landroid/accessibilityservice/AccessibilityServiceInfo;)V

    .line 62
    :cond_2a
    :try_start_2a
    invoke-static {p0}, Lcom/boop/shieldhome/ShieldBrightnessController;->get(Landroid/content/Context;)Lcom/boop/shieldhome/ShieldBrightnessController;

    move-result-object v0

    invoke-virtual {v0, p0}, Lcom/boop/shieldhome/ShieldBrightnessController;->attachService(Landroid/accessibilityservice/AccessibilityService;)V

    .line 63
    invoke-static {p0}, Lcom/boop/shieldhome/ShieldBrightnessController;->get(Landroid/content/Context;)Lcom/boop/shieldhome/ShieldBrightnessController;

    move-result-object v0

    invoke-virtual {v0}, Lcom/boop/shieldhome/ShieldBrightnessController;->init()V
    :try_end_38
    .catchall {:try_start_2a .. :try_end_38} :catchall_38

    .line 66
    :catchall_38
    invoke-direct {p0}, Lcom/boop/shieldhome/ShieldHomeOverrideService;->ensureShieldMenuCustomization()V

    .line 68
    invoke-virtual {p0}, Lcom/boop/shieldhome/ShieldHomeOverrideService;->homeProfileEnabled()Z

    move-result v0

    if-nez v0, :cond_42

    goto :goto_78

    .line 70
    :cond_42
    invoke-virtual {p0}, Lcom/boop/shieldhome/ShieldHomeOverrideService;->getContentResolver()Landroid/content/ContentResolver;

    move-result-object v0

    const-string v1, "boot_count"

    const/4 v2, -0x1

    invoke-static {v0, v1, v2}, Landroid/provider/Settings$Global;->getInt(Landroid/content/ContentResolver;Ljava/lang/String;I)I

    move-result v0

    .line 71
    const-string v1, "home-rearm"

    const/4 v3, 0x0

    invoke-virtual {p0, v1, v3}, Lcom/boop/shieldhome/ShieldHomeOverrideService;->getSharedPreferences(Ljava/lang/String;I)Landroid/content/SharedPreferences;

    move-result-object v1

    .line 72
    invoke-virtual {p0}, Lcom/boop/shieldhome/ShieldHomeOverrideService;->homeProfileEnabled()Z

    move-result v3

    const-string v4, "boot"

    invoke-interface {v1, v4, v2}, Landroid/content/SharedPreferences;->getInt(Ljava/lang/String;I)I

    move-result v2

    invoke-static {v3, v0, v2}, Lcom/boop/shieldhome/HomeOverridePolicy;->shouldRearmOnConnect(ZII)Z

    move-result v2

    if-eqz v2, :cond_78

    .line 73
    invoke-interface {v1}, Landroid/content/SharedPreferences;->edit()Landroid/content/SharedPreferences$Editor;

    move-result-object v1

    invoke-interface {v1, v4, v0}, Landroid/content/SharedPreferences$Editor;->putInt(Ljava/lang/String;I)Landroid/content/SharedPreferences$Editor;

    move-result-object v0

    invoke-interface {v0}, Landroid/content/SharedPreferences$Editor;->apply()V

    .line 74
    invoke-static {}, Landroid/os/SystemClock;->elapsedRealtime()J

    move-result-wide v0

    iput-wide v0, p0, Lcom/boop/shieldhome/ShieldHomeOverrideService;->lastLaunchElapsed:J

    .line 75
    invoke-direct {p0}, Lcom/boop/shieldhome/ShieldHomeOverrideService;->bringBoopToFront()V

    :cond_78
    :goto_78
    return-void
.end method

.method public onUnbind(Landroid/content/Intent;)Z
    .locals 1

    const/4 v0, 0x0

    .line 437
    sput-boolean v0, Lcom/boop/shieldhome/ShieldHomeOverrideService;->isBound:Z

    .line 438
    invoke-super {p0, p1}, Landroid/accessibilityservice/AccessibilityService;->onUnbind(Landroid/content/Intent;)Z

    move-result p1

    return p1
.end method
