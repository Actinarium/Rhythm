# Rhythm  
[![Discontinued](https://img.shields.io/badge/status-discontinued-red.svg)](#discontinued-library) [![Download from Bintray](https://api.bintray.com/packages/actinarium/maven/rhythm/images/download.svg)](https://bintray.com/actinarium/maven/rhythm/_latestVersion) [![Android Arsenal](https://img.shields.io/badge/Android%20Arsenal-Rhythm-green.svg?maxAge=864000)](https://android-arsenal.com/details/1/2664) [![API](https://img.shields.io/badge/API-8%2B-brightgreen.svg?maxAge=864000)](https://android-arsenal.com/api?level=8) [![License: Apache 2.0](https://img.shields.io/github/license/actinarium/rhythm.svg?maxAge=864000)][license]

Rhythm is a design overlay engine for Android.

With Rhythm you can easily render grids, keylines, other [Material Design][mdspec] cues and even custom elements within your app, helping you to build perfect layouts.
Define overlay configurations using [simple expression language][wiki-config], and Rhythm will convert them into Drawables¹, which you can then set as view backgrounds, foregrounds, draw to bitmaps etc:

![Simple Rhythm overlay example](http://actinarium.github.io/Rhythm/assets/rhythm-hero-small.png)

## Discontinued library

Unfortunately the development of this library is discontinued. Such decision was made based on its usefulness vs maintenance effort. The library had insignificant if not zero downloads; it's only actively used in my own Material Cue app, and it's been getting harder and harder to mantain these two separately. Let us say, it served its purpose well as an interim step to get Material Cue done.

Rhythm code will remain as is. All new features will go directly into Material Cue.

If you're interested in revival of this library, ping me.

## Material Cue

**Material Cue** is a standalone keyline app built on Rhythm.
If you need to verify your layout but don’t want the trouble of setting up another library in your project, Material Cue is perfect for you.

Give it a try:  
<a href='https://play.google.com/store/apps/details?id=com.actinarium.materialcue&referrer=utm_source%3Dgh-rhythm%26utm_medium%3Dreferral%26utm_term%3Drhythm-readme'><img alt='Get it on Google Play' src='https://play.google.com/intl/en_us/badges/images/generic/en_badge_web_generic.png' height="72" /></a>

Learn more about the [differences between Rhythm and Material Cue](https://github.com/Actinarium/Rhythm/wiki/Comparison-of-Rhythm-and-Material-Cue).

## Quick setup

Starting with 0.9.6, Rhythm is packaged as two separate artifacts:
* **Rhythm Core** — contains core rendering framework for turning [human readable config][wiki-config] into Drawable objects¹. You then manage those Drawables yourself.
* **Rhythm Control** — provides a mechanism to assign many overlays to many views and switch them on the go using the Rhythm Control notification.

**Tip:** Look at the [sample app][samplesrc].

### Rhythm Core

1. Add Gradle dependency:
   ```
   compile 'com.actinarium.rhythm:rhythm:0.9.6'
   ```
   For alternative setup (JAR, Maven) see [Bintray page][bintray].

2. Create a raw file in your app’s `/src/res/raw` folder, e.g. `/src/res/raw/overlays`, with content like this:

   ```
   # Standard 8dp grid
   grid-lines step=8dp from=top
   grid-lines step=8dp from=left

   # Typography grid w/keylines
   grid-lines step=4dp from=top
   keyline distance=16dp from=left
   keyline distance=16dp from=right
   keyline distance=72dp from=left
   ```

   Overlays are separated by empty newline. Lines starting with `#` are optional overlay titles. There can also be comments and variables.

   > Take a look at the [sample config file][sampleconfig] for a more complex and documented example. For full docs see [the wiki][wiki-config].

3. In your code, inflate this file into a list of overlay objects, wrap them with [RhythmDrawables](http://actinarium.github.io/Rhythm/javadoc/rhythm/com/actinarium/rhythm/RhythmDrawable.html), and assign to views as required:

   ```java
   RhythmOverlayInflater inflater = RhythmOverlayInflater.createDefault(context);
   List<RhythmOverlay> overlays = inflater.inflate(R.raw.overlays);
   Drawable overlayDrawable = new RhythmDrawable(overlays.get(1));
   view.setBackground(overlayDrawable);
   ```

4. Later you can replace the overlay in that drawable:
   ```
   ((RhythmDrawable) view.getBackground()).setOverlay(overlays.get(0));
   ```
   
   or disable it:
   ```
   ((RhythmDrawable) view.getBackground()).setOverlay(null);
   ```

### Rhythm Control

> This module is discontinued in favor of [Material Cue][mcue] app and will not receive new functionality.

If you want to switch overlays for the views in your app at runtime, you can setup Rhythm Control.
This module allows to define groups with many views and overlays attached to them, and then separately control which overlay is displayed over all views in a particular group.
This can be done with a Rhythm Control notification², where you can cycle through the groups (1 › 2 › 3 › … › last › 1) and current group’s overlays (1 › 2 › 3 › … › last › no overlay › 1):

![Rhythm Control notification](http://actinarium.github.io/Rhythm/assets/rhythm-control-small.png)

1. Set up Rhythm Core: add a dependency and compose a configuration sheet.

2. Add another dependency for Rhythm Control:
   ```
   compile 'com.actinarium.rhythm:rhythm-control:0.9.6'
   ```
   For alternative setup (JAR, Maven) see [Bintray page][bintray].
   
3. Implement `RhythmControl.Host` in your Application class (create one if it doesn’t exist yet):
   
   ```java
   public class MyApplication extends Application implements RhythmControl.Host {
       
       private RhythmControl mRhythmControl;
   
       @Override
       public void onCreate() {
           super.onCreate();           
           mRhythmControl = new RhythmControl(this);    
           
           /* Rest of OnCreate() code */           
       }
   
       @Override
       public RhythmControl getRhythmControl() {
           return mRhythmControl;
       }
   }
   ```
   
4. In your `Application.onCreate()` method set up Rhythm groups:
   
   ```java
   // Create an inflater and inflate overlays from your configuration sheet
   RhythmOverlayInflater inflater = RhythmOverlayInflater.createDefault(this);
   List<RhythmOverlay> overlays = inflater.inflate(R.raw.overlays);
   
   // Create the groups. Each group will be given an index starting from 0
   RhythmGroup mainGroup = mRhythmControl.makeGroup("A group with index 0");
   RhythmGroup secondaryGroup = mRhythmControl.makeGroup("Another group with index 1");
   RhythmGroup anotherGroup = mRhythmControl.makeGroup("Group with index 2");
   
   // Assign overlays to groups
   mainGroup.addOverlay(overlays.get(0));               // add only one
   secondaryGroup.addOverlays(overlays);                // add all
   anotherGroup.addOverlays(overlays.subList(0, 2));    // add first two
   
   // Finally, display the notification with notification ID unique across your app
   // (to avoid conflicts with other notifications)
   mRhythmControl.showQuickControl(RHYTHM_NOTIFICATION_ID);
   ```

5. Finally, integrate Rhythm groups into your layouts.   
   There are two ways to do this:
   
   * Decorate existing views programmatically, e.g. in your `Activity.onCreate()` methods:
     
     ```java
     // Retrieve Rhythm Control from application class:
     RhythmControl rhythmControl = ((RhythmControl.Host) getApplication()).getRhythmControl();
     
     // Decorate backgrounds of given views (draws overlay over existing background but under content)
     // Works with any views
     rhythmControl.getGroup(0).decorate(view1, view2, view3 /*, ... */);
     
     // Decorate foregrounds of given views - will draw overlay over content
     // Works with FrameLayouts and its subclasses (e.g. CardView) only
     rhythmControl.getGroup(1).decorateForeground(card1, frame2, card3 /*, ... */);
     ```
     
   * Wrap pieces of your layouts with `RhythmFrameLayout` connected to appropriate groups:
     
     ```xml
     <com.actinarium.rhythm.control.RhythmFrameLayout
             xmlns:app="http://schemas.android.com/apk/res-auto"
             android:layout_width="match_parent"
             android:layout_height="wrap_content"
             app:rhythmGroup="0"
             app:overlayPosition="overContent">
         
         <LinearLayout ... />
         
     </com.actinarium.rhythm.control.RhythmFrameLayout>
     ```
     
**Protip:** you can use `RhythmFrameLayout` on its own, without setting up Rhythm control or groups. Omit the `app:rhythmGroup` attribute or set it to `app:rhythmGroup="noGroup"`, and set overlays to it programmatically:

```java
// By default, RhythmFrameLayout doesn't have a RhythmDrawable object, so inject a new one
rhythmFrameLayout.setRhythmDrawable(new RhythmDrawable(rhythmOverlay));

// After that you can change overlays in that drawable
rhythmFrameLayout.getRhythmDrawable().setOverlay(anotherOverlay);
```

### Further reading

* [Documentation (wiki)][wiki]
* [Rhythm Core javadoc][javadoc-core]
* [Rhythm Control javadoc][javadoc-control]

## A personal appeal

If you like what I’m doing, please consider supporting my efforts.

I quit my full-time job so that I could focus on building useful, time-saving libraries and apps.
I’ve already [made](#) [a][aligned] [few][persistence], and I have more in mind, including free and open-source apps, an ultimate charting library for Android, tutorials and samples, and more.
But as much as I’d love to give it all away for free, I also need to make a living.

Without your support, in a few months I’ll have to return to workforce, meaning I won’t be able to work on my projects anymore.

Here’s what you can do to help:

* **Check out [Material Cue][mcue-support]**, a keyline app I built with Rhythm.
* **Gift me a game from my [wishlist on Steam][steam]** to cheer me up.
* **Spread the word:** tell your fellow developers about Rhythm, Material Cue, and my other projects.
* **Add me on [Google+][gplus] and/or [Twitter][twitter]** to be the first to know about my upcoming projects and apps.
* If you or your company are interested in sponsoring my development efforts, please contact me directly at [actinate@gmail.com](mailto:actinate@gmail.com), and we could arrange something.
* Need a hand with making your app more Material? I guess I could allocate some time to freelance work — contact me to discuss the options.

Thank you!

## License

The library is licensed under [Apache 2.0 License][license], meaning that you can freely use it in any of your projects.

```
Copyright (C) 2016 Actinarium

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

    http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
```

Android, Google Play and the Google Play logo are trademarks of Google Inc.

---
¹ — Said so for simplicity. In fact, Rhythm inflates overlay config into [RhythmOverlay](http://actinarium.github.io/Rhythm/javadoc/rhythm/com/actinarium/rhythm/RhythmOverlay.html) objects, which then can be injected into one or many [RhythmDrawables](http://actinarium.github.io/Rhythm/javadoc/rhythm/com/actinarium/rhythm/RhythmDrawable.html), which are, yeah, Drawables. Since it's not 1.0 yet, this may also change.

² — Rhythm Control notification is pretty useless on pre-4.1 (API 15 and below) because of lack of notification actions. The groups and `RhythmFrameLayouts` can still be controlled programmatically though.

[mcue]: https://play.google.com/store/apps/details?id=com.actinarium.materialcue&referrer=utm_source%3Dgh-rhythm%26utm_medium%3Dreferral%26utm_term%3Drhythm-readme
[mcue-support]: https://play.google.com/store/apps/details?id=com.actinarium.materialcue&referrer=utm_source%3Dgh-rhythm%26utm_medium%3Dreferral%26utm_term%3Drhythm-readme-support
[aligned]: https://github.com/Actinarium/Aligned
[persistence]: https://plus.google.com/u/0/communities/104144545680241581851
[mdspec]: https://material.google.com/layout/metrics-keylines.html
[wiki]: https://github.com/Actinarium/Rhythm/wiki
[wiki-config]: https://github.com/Actinarium/Rhythm/wiki/Declarative-configuration
[bintray]: https://bintray.com/actinarium/maven/rhythm
[license]: https://raw.githubusercontent.com/Actinarium/Rhythm/master/LICENSE
[playstore]: https://play.google.com/store/apps/details?id=com.actinarium.rhythm.sample
[samplesrc]: https://github.com/Actinarium/Rhythm/tree/master/sample
[sampleconfig]: https://github.com/Actinarium/Rhythm/blob/master/sample/src/main/res/raw/overlay_config
[javadoc-core]: http://actinarium.github.io/Rhythm/javadoc/rhythm
[javadoc-control]: http://actinarium.github.io/Rhythm/javadoc/rhythm-control
[gplus]: https://plus.google.com/u/0/+PaulDanyliuk/posts
[twitter]: https://twitter.com/actinarium
[steam]: http://steamcommunity.com/id/actine/wishlist
