package com.aaradhya.assistant.ui.main;

@kotlin.Metadata(mv = {1, 9, 0}, k = 1, xi = 48, d1 = {"\u0000R\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0010\b\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0010\u0014\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0003\n\u0002\u0010\u000b\n\u0002\b\u0002\n\u0002\u0010\u0007\n\u0000\n\u0002\u0010\u0002\n\u0002\b\u0003\n\u0002\u0018\u0002\n\u0002\b\b\u0018\u00002\u00020\u0001B\u001b\b\u0007\u0012\u0006\u0010\u0002\u001a\u00020\u0003\u0012\n\b\u0002\u0010\u0004\u001a\u0004\u0018\u00010\u0005\u00a2\u0006\u0002\u0010\u0006J\b\u0010\u0017\u001a\u00020\u0018H\u0014J\b\u0010\u0019\u001a\u00020\u0018H\u0014J\u0010\u0010\u001a\u001a\u00020\u00182\u0006\u0010\u001b\u001a\u00020\u001cH\u0014J\u0010\u0010\u001d\u001a\u00020\u00182\u0006\u0010\u001e\u001a\u00020\bH\u0014J\u000e\u0010\u001f\u001a\u00020\u00182\u0006\u0010 \u001a\u00020\u0016J\u0006\u0010!\u001a\u00020\u0018J\u0006\u0010\"\u001a\u00020\u0018J\b\u0010#\u001a\u00020\u0018H\u0002R\u000e\u0010\u0007\u001a\u00020\bX\u0082D\u00a2\u0006\u0002\n\u0000R\u0016\u0010\t\u001a\n \u000b*\u0004\u0018\u00010\n0\nX\u0082\u0004\u00a2\u0006\u0002\n\u0000R\u000e\u0010\f\u001a\u00020\rX\u0082\u0004\u00a2\u0006\u0002\n\u0000R\u000e\u0010\u000e\u001a\u00020\u000fX\u0082\u0004\u00a2\u0006\u0002\n\u0000R\u000e\u0010\u0010\u001a\u00020\bX\u0082\u0004\u00a2\u0006\u0002\n\u0000R\u000e\u0010\u0011\u001a\u00020\bX\u0082\u0004\u00a2\u0006\u0002\n\u0000R\u000e\u0010\u0012\u001a\u00020\u0013X\u0082\u000e\u00a2\u0006\u0002\n\u0000R\u000e\u0010\u0014\u001a\u00020\rX\u0082\u0004\u00a2\u0006\u0002\n\u0000R\u000e\u0010\u0015\u001a\u00020\u0016X\u0082\u000e\u00a2\u0006\u0002\n\u0000\u00a8\u0006$"}, d2 = {"Lcom/aaradhya/assistant/ui/main/WaveformView;", "Landroid/view/View;", "context", "Landroid/content/Context;", "attrs", "Landroid/util/AttributeSet;", "(Landroid/content/Context;Landroid/util/AttributeSet;)V", "BAR_COUNT", "", "animator", "Landroid/animation/ValueAnimator;", "kotlin.jvm.PlatformType", "barHeights", "", "barPaint", "Landroid/graphics/Paint;", "colorGold", "colorGoldDim", "isAnimating", "", "targetHeights", "tick", "", "onAttachedToWindow", "", "onDetachedFromWindow", "onDraw", "canvas", "Landroid/graphics/Canvas;", "onWindowVisibilityChanged", "visibility", "setAmplitude", "rms", "startAnimation", "stopAnimation", "updateBars", "app_debug"})
public final class WaveformView extends android.view.View {
    private final int BAR_COUNT = 20;
    @org.jetbrains.annotations.NotNull()
    private final float[] barHeights = null;
    @org.jetbrains.annotations.NotNull()
    private final float[] targetHeights = null;
    @org.jetbrains.annotations.NotNull()
    private final android.graphics.Paint barPaint = null;
    private boolean isAnimating = false;
    private float tick = 0.0F;
    private final int colorGold = 0;
    private final int colorGoldDim = 0;
    private final android.animation.ValueAnimator animator = null;
    
    @kotlin.jvm.JvmOverloads()
    public WaveformView(@org.jetbrains.annotations.NotNull()
    android.content.Context context, @org.jetbrains.annotations.Nullable()
    android.util.AttributeSet attrs) {
        super(null);
    }
    
    public final void startAnimation() {
    }
    
    public final void stopAnimation() {
    }
    
    public final void setAmplitude(float rms) {
    }
    
    private final void updateBars() {
    }
    
    @java.lang.Override()
    protected void onDraw(@org.jetbrains.annotations.NotNull()
    android.graphics.Canvas canvas) {
    }
    
    @java.lang.Override()
    protected void onAttachedToWindow() {
    }
    
    @java.lang.Override()
    protected void onWindowVisibilityChanged(int visibility) {
    }
    
    @java.lang.Override()
    protected void onDetachedFromWindow() {
    }
    
    @kotlin.jvm.JvmOverloads()
    public WaveformView(@org.jetbrains.annotations.NotNull()
    android.content.Context context) {
        super(null);
    }
}