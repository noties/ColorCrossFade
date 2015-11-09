# Color Cross Fade

Simple color cross fade utility for Android. Supported types: RGB, ARGB, HSV, HSV (with alpha).


[![Maven Central](https://img.shields.io/maven-central/v/ru.noties/ccf.svg)](http://search.maven.org/#search|ga|1|g%3A%22ru.noties%22%20AND%20a%3A%22ccf%22)

## Introduction

The main concept of `CCFAnimator` is to be able to evaluate a color value that *lies* between two supplied colors given the **fraction** (.0F - 1.F). This gives ability to smoothly transition from one state to other, for example during ViewPager page changes (*positionOffset* value of OnPageChangeListener), during scroll changes of numerous scrolling widgets (RecyclerView, ListView, ScrollView, etc) and much more places where there is a state change & this change could be represented as a float in range .0F - 1.F.

Also, `CCFAnimator` could be used as a ValueAnimator, to just trigger color animations. `CCFAnimator.asValueAnimator(OnNewColorListener)` returns a ValueAnimator that could be further customized (animation duration, interpolation, etc).

There is an ability to animate between more than two (2) colors. Use build-in methods in `CCFAnimator` that takes an array (rgb, argb, hsv).

## Usage

### RGB
Animates between supplied colors, but ignores alpha channel
```java
CCFAnimator.rgb(int fromColor, int toColor);
CCFAnimator.rgb(int[] colors);
```

### ARGB
Animates between supplied colors changing `a-r-g-b` channels of colors
```java
CCFAnimator.argb(int fromColor, int toColor);
CCFAnimator.argb(int[] colors);

CCFAnimator.alpha(int color, int toAlpha); // animates alpha value of one color
```

### HSV
Animates between supplied colors changing `h-s-v` channels of colors
```java
// no alpha
CCFAnimator.hsv(int fromColor, int toColor);
CCFAnimator.hsv(int[] colors);

// with alpha channel
CCFAnimator.hsv(int fromColor, int toColor, int fromAlpha, int toAlpha);
```

### Concat
Instances of `CCFAnimator` of any type could be merged into one `CCFAnimator`. This could be helpfull when different types of color animations are used for one cross-fade animation (RGB, ARGB, HSV)
```java
CCFAnimator.concat(
    CCFAnimator.rgb(...),
    CCFAnimator.argb(...),
    CCFAnimator.hsv(...)
);
```

### ValueAnimator
```java
final CCFAnimator ccf = CCFAnimator.rgb(...);
final ValueAnimator animator = ccf.asValueAnimator(new OnNewColorListener() {
    @Override
    public void onNewColor(int color) {
        // ready to use color
        mSomeView.setBackgroundColor(color);
    }
});
animator.setDuration(1000L);
animator.setInterpolator(new AccelerateInterpolator());
animator.start();
```

## License

```
  Copyright 2015 Dimitry Ivanov (mail@dimitryivanov.ru)

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