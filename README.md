# Rhythm [![Download](https://api.bintray.com/packages/actinarium/maven/rhythm/images/download.svg)](https://bintray.com/actinarium/maven/rhythm/_latestVersion)

Rhythm is a small library for Android that draws grids and guides over views, making it easier to fine tune
your layouts according to the [principles][mdspec] of beautiful, balanced, _rhythmic_ design.

If you are passionate about carefully crafted layouts and aspiring to #BuildBetterApps, add Rhythm into your toolchain.

![Rhythm][heroimg]

**Key features:**

* Superior flexibility.  
  Highly customizable grids and guides, possibility to draw your own overlays, and more!  
  Make as many different overlays and assign them to as many views as you need.
* Saving lots of time.  
  Define your overlays once and control everything via the Quick Control notification¹ without leaving your current screen.
  No need to rebuild too!  
* Adaptable.  
  Inject Rhythm into your existing views with a single line of code without changing view hierarchy, or wrap layouts
  with `RhythmicFrameLayout` for finer control.
  Furthermore, Rhythm is built on Drawables, which you can obtain and use in any way imaginable.  
* Suitable for dialogs and scrolling content.
* Built with performance in mind.
* Code thoroughly documented.
* API 8+¹.
* Crafted by a design-minded developer, for design-minded developers.
* Open source, open for forks, pull requests, and suggestions².

Sample application available: [APK][apk] or:
[![Get it on Google Play](https://developer.android.com/images/brand/en_generic_rgb_wo_45.png)][playstore]

**Important note:** this is a pre-release version of Rhythm. Things may break and APIs may change.
See [Plans for the future](#plans-for-the-future) for more info.

## Setup

### Get Rhythm

The easiest way to include Rhythm is to add a dependency in your `build.gradle` file:

```
compile 'com.actinarium.rhythm:rhythm:0.9'
```

The library is available via [Bintray][bintray]. If there’s a problem resolving it, check if you have jCenter added as your
Maven repository.

### Configure

The most definitive guide so far is the [sample source][samplesrc], particularly the configuration [here][appsrc].
While a better guide is being prepared, enjoy thoroughly documented code :stuck_out_tongue:

To use Rhythm in an intended, full-featured way, you should create a **Rhythm control** — an object, which aggregates
all overlay controls, powers the Quick Control notification, and is expected to be there by many Rhythm entities.
Since Rhythm Control is an application-wide object, it should be created in your `Application` class, and the latter
must expose it by implementing `RhythmControl.Host` like this:

```
public class MyApplication extends Application implements RhythmControl.Host {

    private RhythmControl mRhythmControl;
    // ...

    @Override
    public void onCreate() {
        super.onCreate();

        // Initialize this application's Rhythm control
        mRhythmControl = new RhythmControl(this);

        // Here goes Rhythm configuration

        // ...
    }

    @Override
    public RhythmControl getRhythmControl() {
        return mRhythmControl;
    }

    // ...
}
```

While technically multiple Rhythm Control instances can be created, as of v0.9 only one can be exposed this way.

The second step is to define some **groups**. The purpose of the group is to set the same overlay to all views in this
group (or to be precise, all `RhythmDrawable`s connected to this group) independently from other groups. Also the group
can have multiple overlays set up and will allow to select/cycle through them, or disable overlay for all connected
views completely.

You may create and manage `RhythmGroup`s explicitly, but if you want to control them via the Quick Control notification,
you must instantiate them like this:

```
RhythmGroup activityBgGroup = mRhythmControl.makeGroup("Activity background");
RhythmGroup cardOverlaysGroup = mRhythmControl.makeGroup("Card overlays");
```

Each group in a Rhythm control will be assigned an **index** in order of creation starting from 0. You then will use
this index to link a `RhythmicFrameLayout` to the group or access it programmatically via `rhythmControl.getGroup(index)`.

Finally, it’s time to create **overlays**. An overlay is a configuration of what gets drawn onto provided canvas
within provided bounds, and it is composed of **spec layers** — granular pieces (single lines, repeating lines etc),
which hold some configuration (e.g. color, thickness, position of lines) and know how to draw themselves.

`// todo: finish the documentation`


## Plans for the future

This is a pre-release version of Rhythm, meaning the API is not finalized yet.
However, the ETA for v1.0 is Nov–Dec 2015, so feel free to use this version until then (but update with caution).

Here are another changes planned for v1.0:

1. Fix encountered critical issues. Since the library is not targeted for production builds, the goal is to have it
   function properly under intended use without spending effort on corner cases.
2. Add a Rhythm control activity, so that it’s possible to cycle through groups and overlays for pre-4.1 devices.
3. Add more spec layers like ratio keylines, 9-grids, arbitrary clipping etc.
4. Consider adding a possibility to declare Rhythm configuration in external file (JSON, XML, Yaml or alike).
5. Make a better sample.

## Support developer

If you find this library useful and want to reward my work, here’s how you can do it:

1. The biggest joy is seeing one’s product being used and loved, so that alone is a reward :smile:
2. Add me on [Google+][gplus] to be the first to know about my new projects and apps.
3. Also follow me on [Twitter][twitter]. I rarely write there, but that might change soon.
4. Normally I wouldn’t ask for donations, but I quit my job to become a full-time indie developer, and I’d really want
   to keep most of my creations free or very humbly priced for as long as possible.
   I haven’t set up a donation pool yet, but you can buy me something from my [wishlist on Steam][steam].

## License

The library is licensed under Apache 2.0 License, meaning that you can freely use it in any of your projects.

The full license text is [here][license];

---
¹ — Controlling overlays on the go is available for Android 4.1+ only, since it relies on action buttons
in the notification. This will be addressed in v1.0. The drawing itself works fine.  
² — Just a friendly reminder that the library is not a kitchen sink :)

[mdspec]: http://www.google.com/design/spec/layout/metrics-keylines.html
[bintray]: https://bintray.com/actinarium/maven/rhythm
[license]: https://github.com/Actinarium/Rhythm/blob/master/LICENSE
[apk]: https://github.com/Actinarium/Rhythm/blob/master/sample/sample-release.apk
[playstore]: https://play.google.com/store/apps/details?id=com.actinarium.rhythm.sample
[samplesrc]: https://github.com/Actinarium/Rhythm/tree/master/sample
[appsrc]: https://github.com/Actinarium/Rhythm/blob/master/sample/src/main/java/com/actinarium/rhythm/sample/RhythmShowcaseApplication.java
[gplus]: https://plus.google.com/u/0/+PaulDanyliuk/posts
[twitter]: https://twitter.com/actinarium
[steam]: http://steamcommunity.com/id/actine/wishlist

[heroimg]: https://github.com/Actinarium/Rhythm/blob/master/images/rhythm-v0.9.png