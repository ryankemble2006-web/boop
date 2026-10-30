.method public onConfigurationChanged(Landroid/content/res/Configuration;)V
    .locals 1

    .line 274
    invoke-static {p0}, Lcom/boop/shieldhome/BoopTvChrome;->applyShieldMetrics(Landroid/content/Context;)V

    if-eqz p1, :cond_11

    .line 275
    invoke-static {p0}, Lcom/boop/shieldhome/BoopTvChrome;->isTelevision(Landroid/content/Context;)Z

    move-result v0

    if-eqz v0, :cond_11

    .line 276
    invoke-static {p0}, Lcom/boop/shieldhome/BoopTvChrome;->computeTargetDensity(Landroid/content/Context;)I

    move-result v0

    iput v0, p1, Landroid/content/res/Configuration;->densityDpi:I

    .line 278
    :cond_11
    invoke-super {p0, p1}, Landroid/app/Activity;->onConfigurationChanged(Landroid/content/res/Configuration;)V

    .line 279
    invoke-static {p0}, Lcom/boop/shieldhome/BoopTvChrome;->applyShieldMetrics(Landroid/content/Context;)V

    .line 280
    invoke-direct {p0}, Lcom/boop/shieldhome/ShieldLauncherActivity;->restoreOpaqueWindow()V

    .line 281
    iget-object p1, p0, Lcom/boop/shieldhome/ShieldLauncherActivity;->currentView:Landroid/view/View;

    instance-of v0, p1, Lcom/boop/shieldhome/ShieldHomeView;

    if-eqz v0, :cond_26

    .line 282
    check-cast p1, Lcom/boop/shieldhome/ShieldHomeView;

    invoke-virtual {p1}, Lcom/boop/shieldhome/ShieldHomeView;->reapplyLayoutDimensions()V

    return-void

    :cond_26
    if-eqz p1, :cond_2b

    .line 284
    invoke-virtual {p1}, Landroid/view/View;->requestLayout()V

    :cond_2b
    return-void
.end method