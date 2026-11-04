.class public final Lcom/boop/shieldhome/ShieldSelfGrant;
.super Ljava/lang/Object;
.source "ShieldSelfGrant.java"


# annotations
.annotation system Ldalvik/annotation/MemberClasses;
    value = {
        Lcom/boop/shieldhome/ShieldSelfGrant$Callback;
    }
.end annotation


# static fields
.field private static final EXECUTOR:Ljava/util/concurrent/ExecutorService;

.field private static final MAIN:Landroid/os/Handler;

.field private static volatile lastDisplayCheckMs:J

.field private static volatile lastSofabatonWakeMs:J


# direct methods
.method static constructor <clinit>()V
    .locals 2

    .line 32
    invoke-static {}, Ljava/util/concurrent/Executors;->newSingleThreadExecutor()Ljava/util/concurrent/ExecutorService;

    move-result-object v0

    sput-object v0, Lcom/boop/shieldhome/ShieldSelfGrant;->EXECUTOR:Ljava/util/concurrent/ExecutorService;

    .line 33
    new-instance v0, Landroid/os/Handler;

    invoke-static {}, Landroid/os/Looper;->getMainLooper()Landroid/os/Looper;

    move-result-object v1

    invoke-direct {v0, v1}, Landroid/os/Handler;-><init>(Landroid/os/Looper;)V

    sput-object v0, Lcom/boop/shieldhome/ShieldSelfGrant;->MAIN:Landroid/os/Handler;

    .line 160
    const-wide/16 v0, 0x0

    sput-wide v0, Lcom/boop/shieldhome/ShieldSelfGrant;->lastDisplayCheckMs:J

    .line 161
    sput-wide v0, Lcom/boop/shieldhome/ShieldSelfGrant;->lastSofabatonWakeMs:J

    return-void
.end method

.method private constructor <init>()V
    .locals 0

    .line 35
    invoke-direct {p0}, Ljava/lang/Object;-><init>()V

    return-void
.end method

.method public static defaultHomeComponent(Landroid/content/Context;)Landroid/content/ComponentName;
    .locals 2

    .line 48
    new-instance v0, Landroid/content/ComponentName;

    invoke-virtual {p0}, Landroid/content/Context;->getPackageName()Ljava/lang/String;

    move-result-object p0

    const-string v1, "com.boop.alpha1.UnifiedEntryActivity"

    invoke-direct {v0, p0, v1}, Landroid/content/ComponentName;-><init>(Ljava/lang/String;Ljava/lang/String;)V

    return-object v0
.end method

.method public static ensureAllGrantedAsync(Landroid/content/Context;Lcom/boop/shieldhome/ShieldSelfGrant$Callback;)V
    .locals 2

    .line 302
    invoke-virtual {p0}, Landroid/content/Context;->getApplicationContext()Landroid/content/Context;

    move-result-object p0

    .line 303
    sget-object v0, Lcom/boop/shieldhome/ShieldSelfGrant;->EXECUTOR:Ljava/util/concurrent/ExecutorService;

    new-instance v1, Lcom/boop/shieldhome/ShieldSelfGrant$$ExternalSyntheticLambda3;

    invoke-direct {v1, p0, p1}, Lcom/boop/shieldhome/ShieldSelfGrant$$ExternalSyntheticLambda3;-><init>(Landroid/content/Context;Lcom/boop/shieldhome/ShieldSelfGrant$Callback;)V

    invoke-interface {v0, v1}, Ljava/util/concurrent/ExecutorService;->execute(Ljava/lang/Runnable;)V

    .line 361
    return-void
.end method

.method public static ensureNative4kDisplayAsync(Landroid/content/Context;)V
    .locals 0

    return-void
.end method

.method public static ensureSofabatonConnectedAsync(Landroid/content/Context;)V
    .locals 6

    .line 220
    if-nez p0, :cond_3

    return-void

    .line 221
    :cond_3
    invoke-static {p0}, Lcom/boop/shieldhome/ShieldSelfGrant;->isSofabatonConnected(Landroid/content/Context;)Z

    move-result v0

    if-eqz v0, :cond_a

    return-void

    .line 222
    :cond_a
    invoke-static {}, Landroid/os/SystemClock;->elapsedRealtime()J

    move-result-wide v0

    .line 223
    sget-wide v2, Lcom/boop/shieldhome/ShieldSelfGrant;->lastSofabatonWakeMs:J

    sub-long v2, v0, v2

    const-wide/16 v4, 0x1388

    cmp-long v2, v2, v4

    if-gez v2, :cond_19

    return-void

    .line 224
    :cond_19
    sput-wide v0, Lcom/boop/shieldhome/ShieldSelfGrant;->lastSofabatonWakeMs:J

    .line 225
    invoke-virtual {p0}, Landroid/content/Context;->getApplicationContext()Landroid/content/Context;

    move-result-object p0

    .line 226
    sget-object v0, Lcom/boop/shieldhome/ShieldSelfGrant;->EXECUTOR:Ljava/util/concurrent/ExecutorService;

    new-instance v1, Lcom/boop/shieldhome/ShieldSelfGrant$$ExternalSyntheticLambda1;

    invoke-direct {v1, p0}, Lcom/boop/shieldhome/ShieldSelfGrant$$ExternalSyntheticLambda1;-><init>(Landroid/content/Context;)V

    invoke-interface {v0, v1}, Ljava/util/concurrent/ExecutorService;->execute(Ljava/lang/Runnable;)V

    .line 227
    return-void
.end method

.method private static fireSofabatonHomeViaHa(Landroid/content/Context;)V
    .locals 12

    .line 245
    const-string v0, "UTF-8"

    const-string v1, "BOOPSelfGrant"

    :try_start_4
    new-instance v2, Lcom/boop/shieldoverlay/SecureCredentialStore;

    invoke-direct {v2, p0}, Lcom/boop/shieldoverlay/SecureCredentialStore;-><init>(Landroid/content/Context;)V

    .line 246
    invoke-virtual {v2}, Lcom/boop/shieldoverlay/SecureCredentialStore;->load()Lcom/boop/shieldoverlay/StoredHomeAssistantCredential;

    move-result-object p0

    .line 247
    if-nez p0, :cond_10

    return-void

    .line 248
    :cond_10
    invoke-virtual {p0}, Lcom/boop/shieldoverlay/StoredHomeAssistantCredential;->baseUrl()Ljava/lang/String;

    move-result-object v2

    .line 249
    const-string v3, "/"

    invoke-virtual {v2, v3}, Ljava/lang/String;->endsWith(Ljava/lang/String;)Z

    move-result v3

    const/4 v4, 0x1

    if-eqz v3, :cond_27

    invoke-virtual {v2}, Ljava/lang/String;->length()I

    move-result v3

    sub-int/2addr v3, v4

    const/4 v5, 0x0

    invoke-virtual {v2, v5, v3}, Ljava/lang/String;->substring(II)Ljava/lang/String;

    move-result-object v2

    .line 250
    :cond_27
    invoke-virtual {p0}, Lcom/boop/shieldoverlay/StoredHomeAssistantCredential;->refreshToken()Ljava/lang/String;

    move-result-object v3
    :try_end_2b
    .catchall {:try_start_4 .. :try_end_2b} :catchall_191

    .line 251
    const-string v5, "Content-Type"

    const-string v6, "POST"

    const/16 v7, 0x1770

    const/16 v8, 0x1388

    if-eqz v3, :cond_45

    :try_start_35
    const-string v9, "eyJ"

    invoke-virtual {v3, v9}, Ljava/lang/String;->startsWith(Ljava/lang/String;)Z

    move-result v9

    if-nez v9, :cond_f5

    const-string v9, "."

    invoke-virtual {v3, v9}, Ljava/lang/String;->contains(Ljava/lang/CharSequence;)Z

    move-result v9

    if-nez v9, :cond_f5

    .line 252
    :cond_45
    new-instance v9, Ljava/net/URL;

    new-instance v10, Ljava/lang/StringBuilder;

    invoke-direct {v10}, Ljava/lang/StringBuilder;-><init>()V

    invoke-virtual {v10, v2}, Ljava/lang/StringBuilder;->append(Ljava/lang/String;)Ljava/lang/StringBuilder;

    move-result-object v10

    const-string v11, "/auth/token"

    invoke-virtual {v10, v11}, Ljava/lang/StringBuilder;->append(Ljava/lang/String;)Ljava/lang/StringBuilder;

    move-result-object v10

    invoke-virtual {v10}, Ljava/lang/StringBuilder;->toString()Ljava/lang/String;

    move-result-object v10

    invoke-direct {v9, v10}, Ljava/net/URL;-><init>(Ljava/lang/String;)V

    .line 253
    invoke-virtual {v9}, Ljava/net/URL;->openConnection()Ljava/net/URLConnection;

    move-result-object v9

    check-cast v9, Ljava/net/HttpURLConnection;

    .line 254
    invoke-virtual {v9, v8}, Ljava/net/HttpURLConnection;->setConnectTimeout(I)V

    .line 255
    invoke-virtual {v9, v7}, Ljava/net/HttpURLConnection;->setReadTimeout(I)V

    .line 256
    invoke-virtual {v9, v6}, Ljava/net/HttpURLConnection;->setRequestMethod(Ljava/lang/String;)V

    .line 257
    invoke-virtual {v9, v4}, Ljava/net/HttpURLConnection;->setDoOutput(Z)V

    .line 258
    const-string v10, "application/x-www-form-urlencoded"

    invoke-virtual {v9, v5, v10}, Ljava/net/HttpURLConnection;->setRequestProperty(Ljava/lang/String;Ljava/lang/String;)V

    .line 259
    nop

    .line 260
    invoke-virtual {p0}, Lcom/boop/shieldoverlay/StoredHomeAssistantCredential;->refreshToken()Ljava/lang/String;

    move-result-object v10

    invoke-static {v10, v0}, Ljava/net/URLEncoder;->encode(Ljava/lang/String;Ljava/lang/String;)Ljava/lang/String;

    move-result-object v10

    .line 261
    invoke-virtual {p0}, Lcom/boop/shieldoverlay/StoredHomeAssistantCredential;->clientId()Ljava/lang/String;

    move-result-object p0

    invoke-static {p0, v0}, Ljava/net/URLEncoder;->encode(Ljava/lang/String;Ljava/lang/String;)Ljava/lang/String;

    move-result-object p0

    new-instance v0, Ljava/lang/StringBuilder;

    invoke-direct {v0}, Ljava/lang/StringBuilder;-><init>()V

    const-string v11, "grant_type=refresh_token&refresh_token="

    invoke-virtual {v0, v11}, Ljava/lang/StringBuilder;->append(Ljava/lang/String;)Ljava/lang/StringBuilder;

    move-result-object v0

    invoke-virtual {v0, v10}, Ljava/lang/StringBuilder;->append(Ljava/lang/String;)Ljava/lang/StringBuilder;

    move-result-object v0

    const-string v10, "&client_id="

    invoke-virtual {v0, v10}, Ljava/lang/StringBuilder;->append(Ljava/lang/String;)Ljava/lang/StringBuilder;

    move-result-object v0

    invoke-virtual {v0, p0}, Ljava/lang/StringBuilder;->append(Ljava/lang/String;)Ljava/lang/StringBuilder;

    move-result-object p0

    invoke-virtual {p0}, Ljava/lang/StringBuilder;->toString()Ljava/lang/String;

    move-result-object p0

    .line 262
    invoke-virtual {v9}, Ljava/net/HttpURLConnection;->getOutputStream()Ljava/io/OutputStream;

    move-result-object v0
    :try_end_a6
    .catchall {:try_start_35 .. :try_end_a6} :catchall_191

    .line 263
    :try_start_a6
    sget-object v10, Ljava/nio/charset/StandardCharsets;->UTF_8:Ljava/nio/charset/Charset;

    invoke-virtual {p0, v10}, Ljava/lang/String;->getBytes(Ljava/nio/charset/Charset;)[B

    move-result-object p0

    invoke-virtual {v0, p0}, Ljava/io/OutputStream;->write([B)V
    :try_end_af
    .catchall {:try_start_a6 .. :try_end_af} :catchall_185

    .line 264
    if-eqz v0, :cond_b4

    :try_start_b1
    invoke-virtual {v0}, Ljava/io/OutputStream;->close()V

    .line 265
    :cond_b4
    invoke-virtual {v9}, Ljava/net/HttpURLConnection;->getResponseCode()I

    move-result p0

    const/16 v0, 0xc8

    if-lt p0, v0, :cond_f2

    invoke-virtual {v9}, Ljava/net/HttpURLConnection;->getResponseCode()I

    move-result p0

    const/16 v0, 0x12c

    if-ge p0, v0, :cond_f2

    .line 266
    invoke-virtual {v9}, Ljava/net/HttpURLConnection;->getInputStream()Ljava/io/InputStream;

    move-result-object p0
    :try_end_c8
    .catchall {:try_start_b1 .. :try_end_c8} :catchall_191

    .line 267
    :try_start_c8
    new-instance v0, Ljava/lang/String;

    invoke-virtual {p0}, Ljava/io/InputStream;->readAllBytes()[B

    move-result-object v3

    sget-object v10, Ljava/nio/charset/StandardCharsets;->UTF_8:Ljava/nio/charset/Charset;

    invoke-direct {v0, v3, v10}, Ljava/lang/String;-><init>([BLjava/nio/charset/Charset;)V

    .line 268
    new-instance v3, Lorg/json/JSONObject;

    invoke-direct {v3, v0}, Lorg/json/JSONObject;-><init>(Ljava/lang/String;)V

    .line 269
    const-string v0, "access_token"

    const/4 v10, 0x0

    invoke-virtual {v3, v0, v10}, Lorg/json/JSONObject;->optString(Ljava/lang/String;Ljava/lang/String;)Ljava/lang/String;

    move-result-object v0
    :try_end_df
    .catchall {:try_start_c8 .. :try_end_df} :catchall_e6

    .line 270
    if-eqz p0, :cond_e4

    :try_start_e1
    invoke-virtual {p0}, Ljava/io/InputStream;->close()V
    :try_end_e4
    .catchall {:try_start_e1 .. :try_end_e4} :catchall_191

    .line 272
    :cond_e4
    move-object v3, v0

    goto :goto_f2

    .line 266
    :catchall_e6
    move-exception v0

    if-eqz p0, :cond_f1

    :try_start_e9
    invoke-virtual {p0}, Ljava/io/InputStream;->close()V
    :try_end_ec
    .catchall {:try_start_e9 .. :try_end_ec} :catchall_ed

    goto :goto_f1

    :catchall_ed
    move-exception p0

    :try_start_ee
    invoke-virtual {v0, p0}, Ljava/lang/Throwable;->addSuppressed(Ljava/lang/Throwable;)V

    :cond_f1
    :goto_f1
    throw v0

    .line 272
    :cond_f2
    :goto_f2
    invoke-virtual {v9}, Ljava/net/HttpURLConnection;->disconnect()V

    .line 274
    :cond_f5
    if-eqz v3, :cond_184

    invoke-virtual {v3}, Ljava/lang/String;->isEmpty()Z

    move-result p0

    if-eqz p0, :cond_ff

    goto/16 :goto_184

    .line 275
    :cond_ff
    new-instance p0, Ljava/net/URL;

    new-instance v0, Ljava/lang/StringBuilder;

    invoke-direct {v0}, Ljava/lang/StringBuilder;-><init>()V

    invoke-virtual {v0, v2}, Ljava/lang/StringBuilder;->append(Ljava/lang/String;)Ljava/lang/StringBuilder;

    move-result-object v0

    const-string v2, "/api/services/button/press"

    invoke-virtual {v0, v2}, Ljava/lang/StringBuilder;->append(Ljava/lang/String;)Ljava/lang/StringBuilder;

    move-result-object v0

    invoke-virtual {v0}, Ljava/lang/StringBuilder;->toString()Ljava/lang/String;

    move-result-object v0

    invoke-direct {p0, v0}, Ljava/net/URL;-><init>(Ljava/lang/String;)V

    .line 276
    invoke-virtual {p0}, Ljava/net/URL;->openConnection()Ljava/net/URLConnection;

    move-result-object p0

    check-cast p0, Ljava/net/HttpURLConnection;

    .line 277
    invoke-virtual {p0, v8}, Ljava/net/HttpURLConnection;->setConnectTimeout(I)V

    .line 278
    invoke-virtual {p0, v7}, Ljava/net/HttpURLConnection;->setReadTimeout(I)V

    .line 279
    invoke-virtual {p0, v6}, Ljava/net/HttpURLConnection;->setRequestMethod(Ljava/lang/String;)V

    .line 280
    invoke-virtual {p0, v4}, Ljava/net/HttpURLConnection;->setDoOutput(Z)V

    .line 281
    const-string v0, "Authorization"

    new-instance v2, Ljava/lang/StringBuilder;

    invoke-direct {v2}, Ljava/lang/StringBuilder;-><init>()V

    const-string v4, "Bearer "

    invoke-virtual {v2, v4}, Ljava/lang/StringBuilder;->append(Ljava/lang/String;)Ljava/lang/StringBuilder;

    move-result-object v2

    invoke-virtual {v2, v3}, Ljava/lang/StringBuilder;->append(Ljava/lang/String;)Ljava/lang/StringBuilder;

    move-result-object v2

    invoke-virtual {v2}, Ljava/lang/StringBuilder;->toString()Ljava/lang/String;

    move-result-object v2

    invoke-virtual {p0, v0, v2}, Ljava/net/HttpURLConnection;->setRequestProperty(Ljava/lang/String;Ljava/lang/String;)V

    .line 282
    const-string v0, "application/json; charset=utf-8"

    invoke-virtual {p0, v5, v0}, Ljava/net/HttpURLConnection;->setRequestProperty(Ljava/lang/String;Ljava/lang/String;)V

    .line 283
    const-string v0, "{\"entity_id\":\"button.x1s_hub_home\"}"

    sget-object v2, Ljava/nio/charset/StandardCharsets;->UTF_8:Ljava/nio/charset/Charset;

    invoke-virtual {v0, v2}, Ljava/lang/String;->getBytes(Ljava/nio/charset/Charset;)[B

    move-result-object v0

    .line 284
    invoke-virtual {p0}, Ljava/net/HttpURLConnection;->getOutputStream()Ljava/io/OutputStream;

    move-result-object v2
    :try_end_152
    .catchall {:try_start_ee .. :try_end_152} :catchall_191

    .line 285
    :try_start_152
    invoke-virtual {v2, v0}, Ljava/io/OutputStream;->write([B)V
    :try_end_155
    .catchall {:try_start_152 .. :try_end_155} :catchall_178

    .line 286
    if-eqz v2, :cond_15a

    :try_start_157
    invoke-virtual {v2}, Ljava/io/OutputStream;->close()V

    .line 287
    :cond_15a
    invoke-virtual {p0}, Ljava/net/HttpURLConnection;->getResponseCode()I

    move-result v0

    .line 288
    new-instance v2, Ljava/lang/StringBuilder;

    invoke-direct {v2}, Ljava/lang/StringBuilder;-><init>()V

    const-string v3, "Sofabaton X1S wake dispatched via HA: HTTP "

    invoke-virtual {v2, v3}, Ljava/lang/StringBuilder;->append(Ljava/lang/String;)Ljava/lang/StringBuilder;

    move-result-object v2

    invoke-virtual {v2, v0}, Ljava/lang/StringBuilder;->append(I)Ljava/lang/StringBuilder;

    move-result-object v0

    invoke-virtual {v0}, Ljava/lang/StringBuilder;->toString()Ljava/lang/String;

    move-result-object v0

    invoke-static {v1, v0}, Landroid/util/Log;->i(Ljava/lang/String;Ljava/lang/String;)I

    .line 289
    invoke-virtual {p0}, Ljava/net/HttpURLConnection;->disconnect()V
    :try_end_177
    .catchall {:try_start_157 .. :try_end_177} :catchall_191

    .line 292
    goto :goto_197

    .line 284
    :catchall_178
    move-exception p0

    if-eqz v2, :cond_183

    :try_start_17b
    invoke-virtual {v2}, Ljava/io/OutputStream;->close()V
    :try_end_17e
    .catchall {:try_start_17b .. :try_end_17e} :catchall_17f

    goto :goto_183

    :catchall_17f
    move-exception v0

    :try_start_180
    invoke-virtual {p0, v0}, Ljava/lang/Throwable;->addSuppressed(Ljava/lang/Throwable;)V

    :cond_183
    :goto_183
    throw p0
    :try_end_184
    .catchall {:try_start_180 .. :try_end_184} :catchall_191

    .line 274
    :cond_184
    :goto_184
    return-void

    .line 262
    :catchall_185
    move-exception p0

    if-eqz v0, :cond_190

    :try_start_188
    invoke-virtual {v0}, Ljava/io/OutputStream;->close()V
    :try_end_18b
    .catchall {:try_start_188 .. :try_end_18b} :catchall_18c

    goto :goto_190

    :catchall_18c
    move-exception v0

    :try_start_18d
    invoke-virtual {p0, v0}, Ljava/lang/Throwable;->addSuppressed(Ljava/lang/Throwable;)V

    :cond_190
    :goto_190
    throw p0
    :try_end_191
    .catchall {:try_start_18d .. :try_end_191} :catchall_191

    .line 290
    :catchall_191
    move-exception p0

    .line 291
    const-string v0, "Unable to dispatch Sofabaton X1S wake via HA"

    invoke-static {v1, v0, p0}, Landroid/util/Log;->w(Ljava/lang/String;Ljava/lang/String;Ljava/lang/Throwable;)I

    .line 293
    :goto_197
    return-void
.end method

.method public static homeOverrideComponent(Landroid/content/Context;)Landroid/content/ComponentName;
    .locals 2

    .line 43
    new-instance v0, Landroid/content/ComponentName;

    invoke-virtual {p0}, Landroid/content/Context;->getPackageName()Ljava/lang/String;

    move-result-object p0

    const-string v1, "com.boop.alpha1.BoopHomeOverrideService"

    invoke-direct {v0, p0, v1}, Landroid/content/ComponentName;-><init>(Ljava/lang/String;Ljava/lang/String;)V

    return-object v0
.end method

.method public static isAllReady(Landroid/content/Context;)Z
    .locals 1

    .line 94
    invoke-static {p0}, Lcom/boop/shieldhome/ShieldSelfGrant;->isNotificationAccessGranted(Landroid/content/Context;)Z

    move-result v0

    if-eqz v0, :cond_14

    .line 95
    invoke-static {p0}, Lcom/boop/shieldhome/ShieldSelfGrant;->isHomeAccessibilityEnabled(Landroid/content/Context;)Z

    move-result v0

    if-eqz v0, :cond_14

    .line 96
    invoke-static {p0}, Landroid/provider/Settings;->canDrawOverlays(Landroid/content/Context;)Z

    move-result p0

    if-eqz p0, :cond_14

    const/4 p0, 0x1

    goto :goto_15

    :cond_14
    const/4 p0, 0x0

    .line 94
    :goto_15
    return p0
.end method

.method public static isDefaultHome(Landroid/content/Context;)Z
    .locals 4

    .line 84
    const/4 v0, 0x0

    :try_start_1
    new-instance v1, Landroid/content/Intent;

    const-string v2, "android.intent.action.MAIN"

    invoke-direct {v1, v2}, Landroid/content/Intent;-><init>(Ljava/lang/String;)V

    const-string v2, "android.intent.category.HOME"

    invoke-virtual {v1, v2}, Landroid/content/Intent;->addCategory(Ljava/lang/String;)Landroid/content/Intent;

    move-result-object v1

    .line 85
    invoke-virtual {p0}, Landroid/content/Context;->getPackageManager()Landroid/content/pm/PackageManager;

    move-result-object v2

    const/high16 v3, 0x10000

    invoke-virtual {v2, v1, v3}, Landroid/content/pm/PackageManager;->resolveActivity(Landroid/content/Intent;I)Landroid/content/pm/ResolveInfo;

    move-result-object v1

    .line 86
    if-eqz v1, :cond_2e

    iget-object v2, v1, Landroid/content/pm/ResolveInfo;->activityInfo:Landroid/content/pm/ActivityInfo;

    if-eqz v2, :cond_2e

    .line 87
    invoke-virtual {p0}, Landroid/content/Context;->getPackageName()Ljava/lang/String;

    move-result-object p0

    iget-object v1, v1, Landroid/content/pm/ResolveInfo;->activityInfo:Landroid/content/pm/ActivityInfo;

    iget-object v1, v1, Landroid/content/pm/ActivityInfo;->packageName:Ljava/lang/String;

    invoke-virtual {p0, v1}, Ljava/lang/String;->equals(Ljava/lang/Object;)Z

    move-result p0
    :try_end_2a
    .catchall {:try_start_1 .. :try_end_2a} :catchall_30

    if-eqz p0, :cond_2e

    const/4 v0, 0x1

    goto :goto_2f

    :cond_2e
    nop

    .line 86
    :goto_2f
    return v0

    .line 88
    :catchall_30
    move-exception p0

    .line 89
    return v0
.end method

.method public static isHomeAccessibilityEnabled(Landroid/content/Context;)Z
    .locals 0

    .line 76
    :try_start_0
    invoke-static {}, Lcom/boop/shieldhome/ShieldHomeOverrideService;->isServiceBound()Z

    move-result p0
    :try_end_4
    .catchall {:try_start_0 .. :try_end_4} :catchall_5

    return p0

    .line 77
    :catchall_5
    move-exception p0

    .line 78
    const/4 p0, 0x0

    return p0
.end method

.method public static isNotificationAccessGranted(Landroid/content/Context;)Z
    .locals 6

    .line 54
    const/4 v0, 0x0

    :try_start_1
    invoke-static {p0}, Lcom/boop/shieldhome/ShieldSelfGrant;->notificationListenerComponent(Landroid/content/Context;)Landroid/content/ComponentName;

    move-result-object v1

    .line 55
    sget v2, Landroid/os/Build$VERSION;->SDK_INT:I

    const/16 v3, 0x1b

    const/4 v4, 0x1

    if-lt v2, v3, :cond_1d

    .line 56
    const-class v2, Landroid/app/NotificationManager;

    invoke-virtual {p0, v2}, Landroid/content/Context;->getSystemService(Ljava/lang/Class;)Ljava/lang/Object;

    move-result-object v2

    check-cast v2, Landroid/app/NotificationManager;

    .line 57
    if-eqz v2, :cond_1d

    invoke-virtual {v2, v1}, Landroid/app/NotificationManager;->isNotificationListenerAccessGranted(Landroid/content/ComponentName;)Z

    move-result v2

    if-eqz v2, :cond_1d

    .line 58
    return v4

    .line 61
    :cond_1d
    nop

    .line 62
    invoke-virtual {p0}, Landroid/content/Context;->getContentResolver()Landroid/content/ContentResolver;

    move-result-object p0

    const-string v2, "enabled_notification_listeners"

    .line 61
    invoke-static {p0, v2}, Landroid/provider/Settings$Secure;->getString(Landroid/content/ContentResolver;Ljava/lang/String;)Ljava/lang/String;

    move-result-object p0

    .line 63
    if-eqz p0, :cond_45

    .line 64
    const-string v2, ":"

    invoke-virtual {p0, v2}, Ljava/lang/String;->split(Ljava/lang/String;)[Ljava/lang/String;

    move-result-object p0

    array-length v2, p0

    move v3, v0

    :goto_32
    if-ge v3, v2, :cond_45

    aget-object v5, p0, v3

    .line 65
    invoke-static {v5}, Landroid/content/ComponentName;->unflattenFromString(Ljava/lang/String;)Landroid/content/ComponentName;

    move-result-object v5

    invoke-virtual {v1, v5}, Landroid/content/ComponentName;->equals(Ljava/lang/Object;)Z

    move-result v5
    :try_end_3e
    .catchall {:try_start_1 .. :try_end_3e} :catchall_44

    if-eqz v5, :cond_41

    .line 66
    return v4

    .line 64
    :cond_41
    add-int/lit8 v3, v3, 0x1

    goto :goto_32

    .line 70
    :catchall_44
    move-exception p0

    :cond_45
    nop

    .line 71
    return v0
.end method

.method public static isSofabatonConnected(Landroid/content/Context;)Z
    .locals 6

    .line 196
    const/4 v0, 0x0

    if-nez p0, :cond_4

    return v0

    .line 198
    :cond_4
    :try_start_4
    const-string v1, "input"

    .line 199
    invoke-virtual {p0, v1}, Landroid/content/Context;->getSystemService(Ljava/lang/String;)Ljava/lang/Object;

    move-result-object p0

    check-cast p0, Landroid/hardware/input/InputManager;

    .line 200
    if-nez p0, :cond_f

    return v0

    .line 201
    :cond_f
    invoke-virtual {p0}, Landroid/hardware/input/InputManager;->getInputDeviceIds()[I

    move-result-object v1

    .line 202
    if-nez v1, :cond_16

    return v0

    .line 203
    :cond_16
    array-length v2, v1

    move v3, v0

    :goto_18
    if-ge v3, v2, :cond_40

    aget v4, v1, v3

    .line 204
    invoke-virtual {p0, v4}, Landroid/hardware/input/InputManager;->getInputDevice(I)Landroid/view/InputDevice;

    move-result-object v4

    .line 205
    if-eqz v4, :cond_3c

    invoke-virtual {v4}, Landroid/view/InputDevice;->getName()Ljava/lang/String;

    move-result-object v5

    if-eqz v5, :cond_3c

    .line 206
    invoke-virtual {v4}, Landroid/view/InputDevice;->getName()Ljava/lang/String;

    move-result-object v4

    sget-object v5, Ljava/util/Locale;->ROOT:Ljava/util/Locale;

    invoke-virtual {v4, v5}, Ljava/lang/String;->toLowerCase(Ljava/util/Locale;)Ljava/lang/String;

    move-result-object v4

    const-string v5, "sofabaton"

    invoke-virtual {v4, v5}, Ljava/lang/String;->contains(Ljava/lang/CharSequence;)Z

    move-result v4
    :try_end_38
    .catchall {:try_start_4 .. :try_end_38} :catchall_3f

    if-eqz v4, :cond_3c

    .line 207
    const/4 p0, 0x1

    return p0

    .line 203
    :cond_3c
    add-int/lit8 v3, v3, 0x1

    goto :goto_18

    .line 210
    :catchall_3f
    move-exception p0

    :cond_40
    nop

    .line 211
    return v0
.end method

.method static synthetic lambda$ensureAllGrantedAsync$5(Landroid/content/Context;Lcom/boop/shieldhome/ShieldSelfGrant$Callback;)V
    .locals 1

    .line 310
    invoke-static {p0}, Lcom/boop/shieldhome/ShieldNowPlayingManager;->get(Landroid/content/Context;)Lcom/boop/shieldhome/ShieldNowPlayingManager;

    move-result-object p0

    invoke-virtual {p0}, Lcom/boop/shieldhome/ShieldNowPlayingManager;->refreshAccess()V

    .line 311
    if-eqz p1, :cond_f

    const/4 p0, 0x1

    const-string v0, "All BOOP permissions and Home access active."

    invoke-interface {p1, p0, v0}, Lcom/boop/shieldhome/ShieldSelfGrant$Callback;->onResult(ZLjava/lang/String;)V

    .line 312
    :cond_f
    return-void
.end method

.method static synthetic lambda$ensureAllGrantedAsync$6()V
    .locals 0

    .line 320
    return-void
.end method

.method static synthetic lambda$ensureAllGrantedAsync$7(Landroid/content/Context;Lcom/boop/shieldhome/ShieldSelfGrant$Callback;Z)V
    .locals 0

    .line 346
    invoke-static {p0}, Lcom/boop/shieldhome/ShieldNowPlayingManager;->get(Landroid/content/Context;)Lcom/boop/shieldhome/ShieldNowPlayingManager;

    move-result-object p0

    invoke-virtual {p0}, Lcom/boop/shieldhome/ShieldNowPlayingManager;->refreshAccess()V

    .line 347
    if-eqz p1, :cond_13

    .line 348
    if-eqz p2, :cond_e

    .line 349
    const-string p0, "BOOP Media, Home & System access enabled."

    goto :goto_10

    .line 350
    :cond_e
    const-string p0, "Could not verify Media access."

    .line 348
    :goto_10
    invoke-interface {p1, p2, p0}, Lcom/boop/shieldhome/ShieldSelfGrant$Callback;->onResult(ZLjava/lang/String;)V

    .line 352
    :cond_13
    return-void
.end method

.method static synthetic lambda$ensureAllGrantedAsync$8(Lcom/boop/shieldhome/ShieldSelfGrant$Callback;)V
    .locals 2

    .line 355
    if-eqz p0, :cond_8

    .line 356
    const/4 v0, 0x0

    const-string v1, "Enable Network Debugging in Developer Options and approve BOOP."

    invoke-interface {p0, v0, v1}, Lcom/boop/shieldhome/ShieldSelfGrant$Callback;->onResult(ZLjava/lang/String;)V

    .line 358
    :cond_8
    return-void
.end method

.method static synthetic lambda$ensureAllGrantedAsync$9(Landroid/content/Context;Lcom/boop/shieldhome/ShieldSelfGrant$Callback;)V
    .locals 10

    .line 304
    invoke-static {p0}, Lcom/boop/shieldhome/ShieldSelfGrant;->tryDirectSecureSettingsGrant(Landroid/content/Context;)Z

    .line 305
    invoke-static {p0}, Lcom/boop/shieldhome/ShieldSelfGrant;->isAllReady(Landroid/content/Context;)Z

    move-result v0

    if-eqz v0, :cond_24

    invoke-static {p0}, Lcom/boop/shieldhome/ShieldSelfGrant;->isDefaultHome(Landroid/content/Context;)Z

    move-result v0

    if-eqz v0, :cond_24

    .line 307
    :try_start_f
    invoke-static {p0}, Lcom/boop/shieldhome/ShieldSelfGrant;->notificationListenerComponent(Landroid/content/Context;)Landroid/content/ComponentName;

    move-result-object v0

    invoke-static {v0}, Landroid/service/notification/NotificationListenerService;->requestRebind(Landroid/content/ComponentName;)V
    :try_end_16
    .catchall {:try_start_f .. :try_end_16} :catchall_17

    goto :goto_18

    .line 308
    :catchall_17
    move-exception v0

    :goto_18
    nop

    .line 309
    sget-object v0, Lcom/boop/shieldhome/ShieldSelfGrant;->MAIN:Landroid/os/Handler;

    new-instance v1, Lcom/boop/shieldhome/ShieldSelfGrant$$ExternalSyntheticLambda6;

    invoke-direct {v1, p0, p1}, Lcom/boop/shieldhome/ShieldSelfGrant$$ExternalSyntheticLambda6;-><init>(Landroid/content/Context;Lcom/boop/shieldhome/ShieldSelfGrant$Callback;)V

    invoke-virtual {v0, v1}, Landroid/os/Handler;->post(Ljava/lang/Runnable;)Z

    .line 313
    return-void

    .line 316
    :cond_24
    invoke-virtual {p0}, Landroid/content/Context;->getPackageName()Ljava/lang/String;

    move-result-object v0

    .line 317
    new-instance v1, Ljava/io/File;

    invoke-virtual {p0}, Landroid/content/Context;->getNoBackupFilesDir()Ljava/io/File;

    move-result-object v2

    const-string v3, "boop-unified-local-adb.key"

    invoke-direct {v1, v2, v3}, Ljava/io/File;-><init>(Ljava/io/File;Ljava/lang/String;)V

    .line 318
    :try_start_33
    new-instance v4, Lcom/boop/shieldturbo/power/AdbWire;

    invoke-direct {v4}, Lcom/boop/shieldturbo/power/AdbWire;-><init>()V
    :try_end_38
    .catch Ljava/lang/Exception; {:try_start_33 .. :try_end_38} :catch_e2

    .line 319
    :try_start_38
    invoke-static {v1}, Lcom/boop/shieldturbo/power/AdbWire;->identity(Ljava/io/File;)Ljava/security/KeyPair;

    move-result-object v6

    .line 320
    new-instance v8, Lcom/boop/shieldhome/ShieldSelfGrant$$ExternalSyntheticLambda7;

    invoke-direct {v8}, Lcom/boop/shieldhome/ShieldSelfGrant$$ExternalSyntheticLambda7;-><init>()V

    const/4 v9, 0x1

    const/16 v5, 0x15b3

    const v7, 0xafc8

    invoke-virtual/range {v4 .. v9}, Lcom/boop/shieldturbo/power/AdbWire;->connect(ILjava/security/KeyPair;ILjava/lang/Runnable;Z)V

    .line 321
    new-instance v1, Ljava/lang/StringBuilder;

    invoke-direct {v1}, Ljava/lang/StringBuilder;-><init>()V

    const-string v2, "pm grant "

    invoke-virtual {v1, v2}, Ljava/lang/StringBuilder;->append(Ljava/lang/String;)Ljava/lang/StringBuilder;

    move-result-object v1

    invoke-virtual {v1, v0}, Ljava/lang/StringBuilder;->append(Ljava/lang/String;)Ljava/lang/StringBuilder;

    move-result-object v1

    const-string v2, " android.permission.WRITE_SECURE_SETTINGS >/dev/null 2>&1; pm grant "

    invoke-virtual {v1, v2}, Ljava/lang/StringBuilder;->append(Ljava/lang/String;)Ljava/lang/StringBuilder;

    move-result-object v1

    invoke-virtual {v1, v0}, Ljava/lang/StringBuilder;->append(Ljava/lang/String;)Ljava/lang/StringBuilder;

    move-result-object v1

    const-string v2, " android.permission.RECORD_AUDIO >/dev/null 2>&1; cmd notification allow_listener "

    invoke-virtual {v1, v2}, Ljava/lang/StringBuilder;->append(Ljava/lang/String;)Ljava/lang/StringBuilder;

    move-result-object v1

    invoke-virtual {v1, v0}, Ljava/lang/StringBuilder;->append(Ljava/lang/String;)Ljava/lang/StringBuilder;

    move-result-object v1

    const-string v2, "/com.boop.shieldhome.ShieldNowPlayingListenerService >/dev/null 2>&1; appops set "

    invoke-virtual {v1, v2}, Ljava/lang/StringBuilder;->append(Ljava/lang/String;)Ljava/lang/StringBuilder;

    move-result-object v1

    invoke-virtual {v1, v0}, Ljava/lang/StringBuilder;->append(Ljava/lang/String;)Ljava/lang/StringBuilder;

    move-result-object v1

    const-string v2, " SYSTEM_ALERT_WINDOW allow >/dev/null 2>&1; appops set "

    invoke-virtual {v1, v2}, Ljava/lang/StringBuilder;->append(Ljava/lang/String;)Ljava/lang/StringBuilder;

    move-result-object v1

    invoke-virtual {v1, v0}, Ljava/lang/StringBuilder;->append(Ljava/lang/String;)Ljava/lang/StringBuilder;

    move-result-object v1

    const-string v2, " PROJECT_MEDIA allow >/dev/null 2>&1; appops set "

    invoke-virtual {v1, v2}, Ljava/lang/StringBuilder;->append(Ljava/lang/String;)Ljava/lang/StringBuilder;

    move-result-object v1

    invoke-virtual {v1, v0}, Ljava/lang/StringBuilder;->append(Ljava/lang/String;)Ljava/lang/StringBuilder;

    move-result-object v1

    const-string v2, " GET_USAGE_STATS allow >/dev/null 2>&1; settings put secure enabled_accessibility_services "

    invoke-virtual {v1, v2}, Ljava/lang/StringBuilder;->append(Ljava/lang/String;)Ljava/lang/StringBuilder;

    move-result-object v1

    invoke-virtual {v1, v0}, Ljava/lang/StringBuilder;->append(Ljava/lang/String;)Ljava/lang/StringBuilder;

    move-result-object v1

    const-string v2, "/com.boop.alpha1.BoopHomeOverrideService:"

    invoke-virtual {v1, v2}, Ljava/lang/StringBuilder;->append(Ljava/lang/String;)Ljava/lang/StringBuilder;

    move-result-object v1

    invoke-virtual {v1, v0}, Ljava/lang/StringBuilder;->append(Ljava/lang/String;)Ljava/lang/StringBuilder;

    move-result-object v1

    const-string v2, "/com.boop.shieldhome.BoopCastVisibilityService:uk.local.eastenders/uk.local.eastenders.EastEndersAccessibilityService:uk.local.casualty/uk.local.casualty.CasualtyAccessibilityService >/dev/null 2>&1; settings put secure accessibility_enabled 1 >/dev/null 2>&1; cmd package set-home-activity "

    invoke-virtual {v1, v2}, Ljava/lang/StringBuilder;->append(Ljava/lang/String;)Ljava/lang/StringBuilder;

    move-result-object v1

    invoke-virtual {v1, v0}, Ljava/lang/StringBuilder;->append(Ljava/lang/String;)Ljava/lang/StringBuilder;

    move-result-object v0

    const-string v1, "/com.boop.alpha1.UnifiedEntryActivity >/dev/null 2>&1; pm disable-user --user 0 com.google.android.tvlauncher >/dev/null 2>&1; pm disable-user --user 0 com.google.android.tvrecommendations >/dev/null 2>&1; pm disable-user --user 0 com.google.android.tungsten.setupwraith >/dev/null 2>&1; echo BOOP_GRANT_OK"

    invoke-virtual {v0, v1}, Ljava/lang/StringBuilder;->append(Ljava/lang/String;)Ljava/lang/StringBuilder;

    move-result-object v0

    invoke-virtual {v0}, Ljava/lang/StringBuilder;->toString()Ljava/lang/String;

    move-result-object v0

    .line 339
    const/16 v1, 0x4e20

    invoke-virtual {v4, v0, v1}, Lcom/boop/shieldturbo/power/AdbWire;->execute(Ljava/lang/String;I)Lcom/boop/shieldturbo/power/AdbWire$Result;

    .line 340
    invoke-static {p0}, Lcom/boop/shieldhome/ShieldSelfGrant;->tryDirectSecureSettingsGrant(Landroid/content/Context;)Z
    :try_end_bb
    .catchall {:try_start_38 .. :try_end_bb} :catchall_d7

    .line 342
    :try_start_bb
    invoke-static {p0}, Lcom/boop/shieldhome/ShieldSelfGrant;->notificationListenerComponent(Landroid/content/Context;)Landroid/content/ComponentName;

    move-result-object v0

    invoke-static {v0}, Landroid/service/notification/NotificationListenerService;->requestRebind(Landroid/content/ComponentName;)V
    :try_end_c2
    .catchall {:try_start_bb .. :try_end_c2} :catchall_c3

    goto :goto_c4

    .line 343
    :catchall_c3
    move-exception v0

    :goto_c4
    nop

    .line 344
    :try_start_c5
    invoke-static {p0}, Lcom/boop/shieldhome/ShieldSelfGrant;->isNotificationAccessGranted(Landroid/content/Context;)Z

    move-result v0

    .line 345
    sget-object v1, Lcom/boop/shieldhome/ShieldSelfGrant;->MAIN:Landroid/os/Handler;

    new-instance v2, Lcom/boop/shieldhome/ShieldSelfGrant$$ExternalSyntheticLambda8;

    invoke-direct {v2, p0, p1, v0}, Lcom/boop/shieldhome/ShieldSelfGrant$$ExternalSyntheticLambda8;-><init>(Landroid/content/Context;Lcom/boop/shieldhome/ShieldSelfGrant$Callback;Z)V

    invoke-virtual {v1, v2}, Landroid/os/Handler;->post(Ljava/lang/Runnable;)Z
    :try_end_d3
    .catchall {:try_start_c5 .. :try_end_d3} :catchall_d7

    .line 353
    :try_start_d3
    invoke-virtual {v4}, Lcom/boop/shieldturbo/power/AdbWire;->close()V
    :try_end_d6
    .catch Ljava/lang/Exception; {:try_start_d3 .. :try_end_d6} :catch_e2

    .line 359
    goto :goto_ed

    .line 318
    :catchall_d7
    move-exception v0

    move-object p0, v0

    :try_start_d9
    invoke-virtual {v4}, Lcom/boop/shieldturbo/power/AdbWire;->close()V
    :try_end_dc
    .catchall {:try_start_d9 .. :try_end_dc} :catchall_dd

    goto :goto_e1

    :catchall_dd
    move-exception v0

    :try_start_de
    invoke-virtual {p0, v0}, Ljava/lang/Throwable;->addSuppressed(Ljava/lang/Throwable;)V

    :goto_e1
    throw p0
    :try_end_e2
    .catch Ljava/lang/Exception; {:try_start_de .. :try_end_e2} :catch_e2

    .line 353
    :catch_e2
    move-exception v0

    .line 354
    sget-object p0, Lcom/boop/shieldhome/ShieldSelfGrant;->MAIN:Landroid/os/Handler;

    new-instance v0, Lcom/boop/shieldhome/ShieldSelfGrant$$ExternalSyntheticLambda9;

    invoke-direct {v0, p1}, Lcom/boop/shieldhome/ShieldSelfGrant$$ExternalSyntheticLambda9;-><init>(Lcom/boop/shieldhome/ShieldSelfGrant$Callback;)V

    invoke-virtual {p0, v0}, Landroid/os/Handler;->post(Ljava/lang/Runnable;)Z

    .line 360
    :goto_ed
    return-void
.end method

.method static synthetic lambda$ensureNative4kDisplayAsync$0()V
    .locals 0

    .line 189
    return-void
.end method

.method static synthetic lambda$ensureNative4kDisplayAsync$1(Landroid/content/Context;)V
    .locals 0

    return-void
.end method

.method static synthetic lambda$ensureSofabatonConnectedAsync$2(Landroid/content/Context;)V
    .locals 0

    .line 226
    invoke-static {p0}, Lcom/boop/shieldhome/ShieldSelfGrant;->triggerSofabatonWakeWithRetry(Landroid/content/Context;)V

    return-void
.end method

.method static synthetic lambda$triggerSofabatonWakeWithRetry$3(Landroid/content/Context;)V
    .locals 1

    .line 235
    invoke-static {p0}, Lcom/boop/shieldhome/ShieldSelfGrant;->isSofabatonConnected(Landroid/content/Context;)Z

    move-result v0

    if-nez v0, :cond_9

    .line 236
    invoke-static {p0}, Lcom/boop/shieldhome/ShieldSelfGrant;->fireSofabatonHomeViaHa(Landroid/content/Context;)V

    .line 238
    :cond_9
    return-void
.end method

.method static synthetic lambda$triggerSofabatonWakeWithRetry$4(Landroid/content/Context;)V
    .locals 2

    .line 233
    invoke-static {p0}, Lcom/boop/shieldhome/ShieldSelfGrant;->isSofabatonConnected(Landroid/content/Context;)Z

    move-result v0

    if-nez v0, :cond_10

    .line 234
    sget-object v0, Lcom/boop/shieldhome/ShieldSelfGrant;->EXECUTOR:Ljava/util/concurrent/ExecutorService;

    new-instance v1, Lcom/boop/shieldhome/ShieldSelfGrant$$ExternalSyntheticLambda5;

    invoke-direct {v1, p0}, Lcom/boop/shieldhome/ShieldSelfGrant$$ExternalSyntheticLambda5;-><init>(Landroid/content/Context;)V

    invoke-interface {v0, v1}, Ljava/util/concurrent/ExecutorService;->execute(Ljava/lang/Runnable;)V

    .line 240
    :cond_10
    return-void
.end method

.method public static notificationListenerComponent(Landroid/content/Context;)Landroid/content/ComponentName;
    .locals 2

    .line 38
    new-instance v0, Landroid/content/ComponentName;

    invoke-virtual {p0}, Landroid/content/Context;->getPackageName()Ljava/lang/String;

    move-result-object p0

    const-string v1, "com.boop.shieldhome.ShieldNowPlayingListenerService"

    invoke-direct {v0, p0, v1}, Landroid/content/ComponentName;-><init>(Ljava/lang/String;Ljava/lang/String;)V

    return-object v0
.end method

.method private static triggerSofabatonWakeWithRetry(Landroid/content/Context;)V
    .locals 4

    .line 230
    invoke-static {p0}, Lcom/boop/shieldhome/ShieldSelfGrant;->isSofabatonConnected(Landroid/content/Context;)Z

    move-result v0

    if-eqz v0, :cond_7

    return-void

    .line 231
    :cond_7
    invoke-static {p0}, Lcom/boop/shieldhome/ShieldSelfGrant;->fireSofabatonHomeViaHa(Landroid/content/Context;)V

    .line 232
    sget-object v0, Lcom/boop/shieldhome/ShieldSelfGrant;->MAIN:Landroid/os/Handler;

    new-instance v1, Lcom/boop/shieldhome/ShieldSelfGrant$$ExternalSyntheticLambda10;

    invoke-direct {v1, p0}, Lcom/boop/shieldhome/ShieldSelfGrant$$ExternalSyntheticLambda10;-><init>(Landroid/content/Context;)V

    const-wide/16 v2, 0xfa0

    invoke-virtual {v0, v1, v2, v3}, Landroid/os/Handler;->postDelayed(Ljava/lang/Runnable;J)Z

    .line 241
    return-void
.end method

.method public static tryDirectSecureSettingsGrant(Landroid/content/Context;)Z
    .locals 12

    .line 102
    const-string v0, "enabled_notification_listeners"

    const-string v1, "enabled_accessibility_services"

    const/4 v2, 0x0

    :try_start_5
    const-string v3, "android.permission.WRITE_SECURE_SETTINGS"

    invoke-virtual {p0, v3}, Landroid/content/Context;->checkSelfPermission(Ljava/lang/String;)I

    move-result v3

    if-eqz v3, :cond_e

    .line 104
    return v2

    .line 106
    :cond_e
    invoke-virtual {p0}, Landroid/content/Context;->getPackageName()Ljava/lang/String;

    move-result-object v3

    .line 108
    invoke-static {p0}, Lcom/boop/shieldhome/ShieldSelfGrant;->notificationListenerComponent(Landroid/content/Context;)Landroid/content/ComponentName;

    move-result-object v4

    invoke-virtual {v4}, Landroid/content/ComponentName;->flattenToString()Ljava/lang/String;

    move-result-object v4

    .line 109
    nop

    .line 110
    invoke-virtual {p0}, Landroid/content/Context;->getContentResolver()Landroid/content/ContentResolver;

    move-result-object v5

    .line 109
    invoke-static {v5, v0}, Landroid/provider/Settings$Secure;->getString(Landroid/content/ContentResolver;Ljava/lang/String;)Ljava/lang/String;

    move-result-object v5

    .line 111
    new-instance v6, Ljava/util/LinkedHashSet;

    invoke-direct {v6}, Ljava/util/LinkedHashSet;-><init>()V
    :try_end_28
    .catchall {:try_start_5 .. :try_end_28} :catchall_107

    .line 112
    const-string v7, ":"

    if-eqz v5, :cond_48

    :try_start_2c
    invoke-static {v5}, Lcom/boop/shieldhome/ShieldSelfGrant$$ExternalSyntheticBackport0;->m(Ljava/lang/String;)Z

    move-result v8

    if-nez v8, :cond_48

    .line 113
    invoke-virtual {v5, v7}, Ljava/lang/String;->split(Ljava/lang/String;)[Ljava/lang/String;

    move-result-object v5

    array-length v8, v5

    move v9, v2

    :goto_38
    if-ge v9, v8, :cond_48

    aget-object v10, v5, v9

    .line 114
    invoke-static {v10}, Lcom/boop/shieldhome/ShieldSelfGrant$$ExternalSyntheticBackport0;->m(Ljava/lang/String;)Z

    move-result v11

    if-nez v11, :cond_45

    invoke-interface {v6, v10}, Ljava/util/Set;->add(Ljava/lang/Object;)Z

    .line 113
    :cond_45
    add-int/lit8 v9, v9, 0x1

    goto :goto_38

    .line 117
    :cond_48
    invoke-interface {v6, v4}, Ljava/util/Set;->add(Ljava/lang/Object;)Z

    .line 118
    invoke-virtual {p0}, Landroid/content/Context;->getContentResolver()Landroid/content/ContentResolver;

    move-result-object v4

    .line 119
    invoke-static {v7, v6}, Ljava/lang/String;->join(Ljava/lang/CharSequence;Ljava/lang/Iterable;)Ljava/lang/String;

    move-result-object v5

    .line 118
    invoke-static {v4, v0, v5}, Landroid/provider/Settings$Secure;->putString(Landroid/content/ContentResolver;Ljava/lang/String;Ljava/lang/String;)Z

    .line 122
    nop

    .line 123
    invoke-virtual {p0}, Landroid/content/Context;->getContentResolver()Landroid/content/ContentResolver;

    move-result-object v0

    .line 122
    invoke-static {v0, v1}, Landroid/provider/Settings$Secure;->getString(Landroid/content/ContentResolver;Ljava/lang/String;)Ljava/lang/String;

    move-result-object v0

    .line 124
    new-instance v4, Ljava/util/LinkedHashSet;

    invoke-direct {v4}, Ljava/util/LinkedHashSet;-><init>()V

    .line 125
    if-eqz v0, :cond_82

    invoke-static {v0}, Lcom/boop/shieldhome/ShieldSelfGrant$$ExternalSyntheticBackport0;->m(Ljava/lang/String;)Z

    move-result v5

    if-nez v5, :cond_82

    .line 126
    invoke-virtual {v0, v7}, Ljava/lang/String;->split(Ljava/lang/String;)[Ljava/lang/String;

    move-result-object v0

    array-length v5, v0

    move v6, v2

    :goto_72
    if-ge v6, v5, :cond_82

    aget-object v8, v0, v6

    .line 127
    invoke-static {v8}, Lcom/boop/shieldhome/ShieldSelfGrant$$ExternalSyntheticBackport0;->m(Ljava/lang/String;)Z

    move-result v9

    if-nez v9, :cond_7f

    invoke-interface {v4, v8}, Ljava/util/Set;->add(Ljava/lang/Object;)Z

    .line 126
    :cond_7f
    add-int/lit8 v6, v6, 0x1

    goto :goto_72

    .line 130
    :cond_82
    new-instance v0, Ljava/lang/StringBuilder;

    invoke-direct {v0}, Ljava/lang/StringBuilder;-><init>()V

    invoke-virtual {v0, v3}, Ljava/lang/StringBuilder;->append(Ljava/lang/String;)Ljava/lang/StringBuilder;

    move-result-object v0

    const-string v5, "/com.boop.alpha1.BoopHomeOverrideService"

    invoke-virtual {v0, v5}, Ljava/lang/StringBuilder;->append(Ljava/lang/String;)Ljava/lang/StringBuilder;

    move-result-object v0

    invoke-virtual {v0}, Ljava/lang/StringBuilder;->toString()Ljava/lang/String;

    move-result-object v0

    .line 131
    new-instance v5, Ljava/lang/StringBuilder;

    invoke-direct {v5}, Ljava/lang/StringBuilder;-><init>()V

    invoke-virtual {v5, v3}, Ljava/lang/StringBuilder;->append(Ljava/lang/String;)Ljava/lang/StringBuilder;

    move-result-object v3

    const-string v5, "/com.boop.shieldhome.BoopCastVisibilityService"

    invoke-virtual {v3, v5}, Ljava/lang/StringBuilder;->append(Ljava/lang/String;)Ljava/lang/StringBuilder;

    move-result-object v3

    invoke-virtual {v3}, Ljava/lang/StringBuilder;->toString()Ljava/lang/String;

    move-result-object v3

    .line 132
    invoke-interface {v4, v0}, Ljava/util/Set;->contains(Ljava/lang/Object;)Z

    move-result v5

    .line 133
    if-eqz v5, :cond_ca

    invoke-static {}, Lcom/boop/shieldhome/ShieldHomeOverrideService;->isServiceBound()Z

    move-result v5

    if-nez v5, :cond_ca

    .line 136
    new-instance v5, Ljava/util/LinkedHashSet;

    invoke-direct {v5, v4}, Ljava/util/LinkedHashSet;-><init>(Ljava/util/Collection;)V

    .line 137
    invoke-interface {v5, v0}, Ljava/util/Set;->remove(Ljava/lang/Object;)Z

    .line 138
    invoke-interface {v5, v3}, Ljava/util/Set;->remove(Ljava/lang/Object;)Z

    .line 139
    invoke-virtual {p0}, Landroid/content/Context;->getContentResolver()Landroid/content/ContentResolver;

    move-result-object v6

    .line 140
    invoke-static {v7, v5}, Ljava/lang/String;->join(Ljava/lang/CharSequence;Ljava/lang/Iterable;)Ljava/lang/String;

    move-result-object v5

    .line 139
    invoke-static {v6, v1, v5}, Landroid/provider/Settings$Secure;->putString(Landroid/content/ContentResolver;Ljava/lang/String;Ljava/lang/String;)Z

    .line 142
    :cond_ca
    invoke-interface {v4, v0}, Ljava/util/Set;->add(Ljava/lang/Object;)Z

    .line 143
    invoke-interface {v4, v3}, Ljava/util/Set;->add(Ljava/lang/Object;)Z

    .line 144
    const-string v0, "uk.local.eastenders/uk.local.eastenders.EastEndersAccessibilityService"

    invoke-interface {v4, v0}, Ljava/util/Set;->add(Ljava/lang/Object;)Z

    .line 145
    const-string v0, "uk.local.casualty/uk.local.casualty.CasualtyAccessibilityService"

    invoke-interface {v4, v0}, Ljava/util/Set;->add(Ljava/lang/Object;)Z

    .line 146
    invoke-virtual {p0}, Landroid/content/Context;->getContentResolver()Landroid/content/ContentResolver;

    move-result-object v0

    .line 147
    invoke-static {v7, v4}, Ljava/lang/String;->join(Ljava/lang/CharSequence;Ljava/lang/Iterable;)Ljava/lang/String;

    move-result-object v3

    .line 146
    invoke-static {v0, v1, v3}, Landroid/provider/Settings$Secure;->putString(Landroid/content/ContentResolver;Ljava/lang/String;Ljava/lang/String;)Z

    .line 148
    invoke-virtual {p0}, Landroid/content/Context;->getContentResolver()Landroid/content/ContentResolver;

    move-result-object v0

    const-string v1, "accessibility_enabled"

    const/4 v3, 0x1

    invoke-static {v0, v1, v3}, Landroid/provider/Settings$Secure;->putInt(Landroid/content/ContentResolver;Ljava/lang/String;I)Z
    :try_end_ef
    .catchall {:try_start_2c .. :try_end_ef} :catchall_107

    .line 152
    :try_start_ef
    invoke-static {p0}, Lcom/boop/shieldhome/ShieldSelfGrant;->notificationListenerComponent(Landroid/content/Context;)Landroid/content/ComponentName;

    move-result-object v0

    invoke-static {v0}, Landroid/service/notification/NotificationListenerService;->requestRebind(Landroid/content/ComponentName;)V
    :try_end_f6
    .catchall {:try_start_ef .. :try_end_f6} :catchall_f7

    goto :goto_f8

    .line 153
    :catchall_f7
    move-exception v0

    :goto_f8
    nop

    .line 154
    :try_start_f9
    invoke-static {p0}, Lcom/boop/shieldhome/ShieldSelfGrant;->isNotificationAccessGranted(Landroid/content/Context;)Z

    move-result v0

    if-eqz v0, :cond_106

    invoke-static {p0}, Lcom/boop/shieldhome/ShieldSelfGrant;->isHomeAccessibilityEnabled(Landroid/content/Context;)Z

    move-result p0
    :try_end_103
    .catchall {:try_start_f9 .. :try_end_103} :catchall_107

    if-eqz p0, :cond_106

    move v2, v3

    :cond_106
    return v2

    .line 155
    :catchall_107
    move-exception p0

    .line 156
    return v2
.end method
