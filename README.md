# Rhythm

Rhythm is a small library for Android that draws grids and guides over views, making it easier to fine tune
your layouts according to the [principles][1] of beautiful, balanced, _rhythmic_ design.

If you are passionate about carefully crafted layouts and aspiring to #BuildBetterApps, add Rhythm into your toolchain.

Current version: 0.9

![Rhythm][hero]

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

## Setup

### Get Rhythm

The easiest way to include Rhythm is to add a dependency in your `build.gradle` file:

```
compile 'com.actinarium.rhythm:rhythm:0.9'
```

The library is available via [jCenter][2]. If there’s a problem resolving it, check if you have jCenter added as your
Maven repository.

### Configure



---
¹ — Controlling overlays on the go is available for Android 4.1+ only, since it relies on action buttons
in the notification. This will be addressed in v1.0. The drawing itself works fine.  
² — Just a friendly reminder that the library is not a kitchen sink :)

[1]: http://www.google.com/design/spec/layout/metrics-keylines.html
[2]: https://bintray.com/actinarium/maven/rhythm

[hero]: https://github.com/Actinarium/Rhythm/blob/develop/images/rhythm-v0.9.png