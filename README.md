### ViewModel lifecycle
위에서 아래로 생명 주기가 흐른다.
Activity - onCreated
ViewModel created
Activity - onDestroy
ViewModel - onCleared

### ViewModel 추가
```
// add dependencie
implementation 'androidx.lifecycle:lifecycle-viewmodel-ktx:2.3.1'

// create class
class GameViewModel : ViewModel() {
}

// use viewmodel in controller
private val viewModel: GameViewModel by viewModels()
```

### ViewModel, UI Controller의 책임
ViewModel은 UI에 필요한 모든 데이터를 보유하고 처리합니다. 뷰 계층 구조(예: 뷰 결합 객체)에 액세스하거나 활동 또는 프래그먼트의 참조를 보유해서는 안 됩니다.
액티비티 및 프래그먼트는 뷰와 데이터를 화면에 그리고 사용자 이벤트에 응답합니다.


Unscramble App
===================================
https://developer.android.com/codelabs/basic-android-kotlin-training-viewmodel?continue=https%3A%2F%2Fdeveloper.android.com%2Fcourses%2Fpathways%2Fandroid-basics-kotlin-unit-3-pathway-3%23codelab-https%3A%2F%2Fdeveloper.android.com%2Fcodelabs%2Fbasic-android-kotlin-training-viewmodel#0

Starter code for Android Basics codelab - Store the data in a ViewModel

Unscramble is  a single player game app that displays scrambled words. To play the game, player has
to make a word using all the letters from the displayed scrambled word.

Used in the [Android Basics with Kotlin](https://developer.android
.com/courses/android-basics-kotlin/course) course.


Pre-requisites
--------------

You need to know:
- Knowledge about Fragments.
- How to design a layout in ConstraintLayout.
- Able to write control flow statements (if / else, when statements).
- Able to update the UI of the app based on user input.
- Able to add a click listener to a Button.


Getting Started
---------------

1. Download and run the app.

License
-------

Copyright (C) 2020 The Android Open Source Project.

Licensed to the Apache Software Foundation (ASF) under one or more contributor
license agreements.  See the NOTICE file distributed with this work for
additional information regarding copyright ownership.  The ASF licenses this
file to you under the Apache License, Version 2.0 (the "License"); you may not
use this file except in compliance with the License.  You may obtain a copy of
the License at

  http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.  See the
License for the specific language governing permissions and limitations under
the License.

