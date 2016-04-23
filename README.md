# Rhythm [![Download](https://api.bintray.com/packages/actinarium/maven/rhythm/images/download.svg)](https://bintray.com/actinarium/maven/rhythm/_latestVersion) [![Android Arsenal](https://img.shields.io/badge/Android%20Arsenal-Rhythm-green.svg?style=flat)](https://android-arsenal.com/details/1/2664)

Rhythm is a small library for Android that draws grids and guides over views, making it easier to fine tune
your layouts according to the [principles][mdspec] of beautiful, balanced, _rhythmic_ design.

If you are passionate about carefully crafted layouts and aspiring to #BuildBetterApps, add Rhythm into your toolchain.

**Attention!** The library has been updated recently, and the new version comes with significant API changes.
The documentation will be updated soon, meanwhile if you’re ready to embrace the new functionality, such as
**declarative configuration** and inset groups, use the sample as a reference.

![Rhythm][heroimg]

**Key features:**

* Superior flexibility.  
  Highly customizable grids and guides, possibility to draw your own overlays, and more!  
  Make as many different overlays and assign them to as many views as you need.
* Saving lots of time.  
  Define your overlays once and control everything via the Quick Control notification¹ without leaving your current screen or having to recompile.
* Adaptable.  
  Inject Rhythm into your existing views with a single line of code without changing view hierarchy, or wrap layouts
  with `RhythmFrameLayout` for finer control.
  Furthermore, Rhythm is built on Drawables, which you can obtain and use in any way imaginable.  
* Suitable for dialogs and scrolling content.
* Built with performance in mind.
* Code thoroughly documented.
* API 8+¹.
* Crafted by a design-minded developer, for design-minded developers.
* Open source, open for forks, pull requests, and suggestions.

Sample application available: [APK][apk] or: <a href="https://play.google.com/store/apps/details?id=com.actinarium.rhythm.sample&utm_source=global_co&utm_medium=prtnr&utm_content=Mar2515&utm_campaign=PartBadge&pcampaignid=MKT-Other-global-all-co-prtnr-py-PartBadge-Mar2515-1"><img alt="Get it on Google Play" src="https://play.google.com/intl/en_us/badges/images/generic/en-play-badge.png" height="48" /></a>

**Important note:** this is a pre-release version of Rhythm. Things may break and APIs may change.
See [Plans for the future](#plans-for-the-future) for more info.

## Setup

The most definitive guide so far is the [sample source][samplesrc], particularly the configuration [here][appsrc].
While a better guide is being prepared, enjoy thoroughly documented code.

### Get Rhythm

The easiest way to include Rhythm is to add a dependency in your `build.gradle` file:

```
compile 'com.actinarium.rhythm:rhythm:0.9'
```

The library is available via [Bintray][bintray]. If there’s a problem resolving it, check if you have jCenter added as your
Maven repository.

### Quick start

To set up Rhythm in an intended way—that is, with Quick Control notification, do the following in your `Application` class.

**Note:** ~~While currently overlays are configured imperatively like this (i.e. with lots of code),
it is already [planned](#plans-for-the-future) to add declarative configs (e.g. with JSON or such) in v1.0.~~
declarative configs are available as of v0.9.5.

```java
// Implement RhythmControl.Host - otherwise Rhythm components won't be able to access Rhythm control
public class MyApplication extends Application implements RhythmControl.Host {

    // Rhythm control is a "singleton" through which the notification, RhythmicFrameLayouts and the overlays communicate
    private RhythmControl mRhythmControl;

    @Override
    public void onCreate() {
        super.onCreate();

        // Retrieve device scale factor (density) and pre-calculate some common dip values
        final float density = getResources().getDisplayMetrics().density;
        final int i8dp = (int) (8 * density);
        final int i4dp = i8dp / 2;
        final int i16dp = i8dp * 2;
        final int i72dp = i8dp * 9;

        // Initialize this application's Rhythm control
        mRhythmControl = new RhythmControl(this);

        // Make at least one group for this control
        // Groups set the same overlay to all attached views independently from other groups
        RhythmGroup activityGroup = mRhythmControl.makeGroup("Activity top-level");             // assigned index = 0
        RhythmGroup cardOverlaysGroup = mRhythmControl.makeGroup("Card overlays");              // assigned index = 1

        // Make a simple 8dp grid overlay and add it to card overlays group
        RhythmOverlay standardGrid = new RhythmOverlay("Standard grid")
                .addLayer(new GridLines(Gravity.TOP, i8dp))
                .addLayer(new GridLines(Gravity.LEFT, i8dp))
                .addToGroup(cardOverlaysGroup);

        // Make the same 8dp grid, but add standard Material keylines on top. Attach to activity group
        RhythmOverlay standardWithKeylines = new RhythmOverlay("Standard w/ keylines")
                .addLayersFrom(standardGrid)                                           // include standard grid
                .addLayer(new Guide(Gravity.LEFT, i16dp))                              // 16 dp from the left
                .addLayer(new Guide(Gravity.RIGHT, i16dp))                             // 16 dp from the right
                .addLayer(new Guide(Gravity.LEFT, i72dp));                             // 72 dp from the left
                .addToGroup(activityGroup);

        // Make a simple 4dp baseline grid and also add to activity group
        new RhythmOverlay("Baseline grid w/keylines")
                .addLayer(new GridLines(Gravity.TOP, i4dp).color(GridLines.DEFAULT_BASELINE_COLOR))
                .addToGroup(activityGroup);

        // ...mix and match other overlays...

        // In the end, display the notification with a notification ID, which must be unique across your app
        mRhythmControl.showQuickControl(42);
    }

    @Override
    public RhythmControl getRhythmControl() {
        return mRhythmControl;
    }
}
```

Now you can wrap your views with `RhythmicFrameLayout`s connected to different groups:

```xml
<!-- This one will be connected to group with index 0, which is "Activity top-level", and draw the grid over content -->
<com.actinarium.rhythm.widget.RhythmicFrameLayout
        android:layout_width="match_parent"
        android:layout_height="wrap_content"
        app:rhythmGroup="0"
        app:overlayPosition="overContent">

    <LinearLayout ... />

</com.actinarium.rhythm.widget.RhythmicFrameLayout>
```

Or you can decorate existing views from the code without altering your layouts.
This approach is not as flexible, and visual feedback on touch/focus/etc on decorated views might break, but this is the
friendliest way towards having development-only integration.

```java
// Somewhere in your onCreate() methods:
RhythmControl rhythmControl = ((RhythmControl.Host) getApplication()).getRhythmControl();

// Decorate backgrounds of given views (any views) - will draw overlay over background but under view's content
rhythmControl.getGroup(0).decorate(view1, view2, view3,...);

// Decorate foregrounds of given views (FrameLayouts only!) - will draw overlay over content
rhythmControl.getGroup(1).decorateForeground(card1, frame2, card3,...);
```

Voilà, now you’ll be able to control the overlay of connected layouts via the notification.

For advanced use refer to [the source][samplesrc] and Javadocs for now.

## Plans for the future

This is a pre-release version of Rhythm, meaning the API is not finalized yet.
However, the ETA for v1.0 is Nov–Dec 2015, so feel free to use this version until then (but update with caution).

Here are another changes planned for v1.0:

1. Fix encountered critical issues. Since the library is not targeted for production builds, the goal is to have it
   function properly under intended use without spending effort on corner cases.
2. Add a Rhythm control activity, so that it’s possible to cycle through groups and overlays for pre-4.1 devices.
3. Pre-define the most commonly used overlay configurations (e.g. 8dp grid, baseline grid) so that they are available
   for developers out of the box.
4. Add more spec layers like ratio keylines, 9-grids, arbitrary clipping etc.
5. Consider adding a possibility to declare Rhythm configuration in external file (JSON, XML, Yaml or alike).
6. Make exhaustive documentation, depending on demand.
7. Make a better sample.

## A personal appeal

If you already feel that this library is a godsend that will save you lots of time, please consider adding me on
[Google+][gplus] and/or [Twitter][twitter] to be the first to know about my upcoming projects and apps.
I’m about to publish a standalone app that’s similar to Rhythm but is even easier to use, and you will make me
very happy if you try it out.

If you’re really generous and want to reward my work, you can gift me something from my [wishlist on Steam][steam].
Also I’ll be offering in-app donations and pay-what-you-want features in my upcoming apps.

## License

The library is licensed under Apache 2.0 License, meaning that you can freely use it in any of your projects.

The full license text is [here][license].

Android, Google Play and the Google Play logo are trademarks of Google Inc.

---
¹ — Controlling overlays on the go is available for Android 4.1+ only, since it relies on action buttons
in the notification. This will be addressed in v1.0. The drawing itself works fine.

[mdspec]: http://www.google.com/design/spec/layout/metrics-keylines.html
[bintray]: https://bintray.com/actinarium/maven/rhythm
[license]: https://raw.githubusercontent.com/Actinarium/Rhythm/master/LICENSE
[apk]: https://raw.githubusercontent.com/Actinarium/Rhythm/master/sample/sample-release.apk
[playstore]: https://play.google.com/store/apps/details?id=com.actinarium.rhythm.sample
[samplesrc]: https://github.com/Actinarium/Rhythm/tree/master/sample
[appsrc]: https://github.com/Actinarium/Rhythm/blob/master/sample/src/main/java/com/actinarium/rhythm/sample/RhythmSampleApplication.java
[gplus]: https://plus.google.com/u/0/+PaulDanyliuk/posts
[twitter]: https://twitter.com/actinarium
[steam]: http://steamcommunity.com/id/actine/wishlist

[heroimg]: https://github.com/Actinarium/Rhythm/blob/master/images/rhythm-v0.9.png