
### Repository and DataSource
```kotlin
class GameRepository(
    private val dataSource: GameDataSource
) {
    val highScore: Flow<Int> = dataSource.gamePreferencesFlow.map { preferences -> preferences.highScore }

    suspend fun updateScore(score: Int) {
        dataSource.updateHighScore(score)
    }
}

class GameDataSource (context: Context) {
    private val dataStore = context.gameDataStore

    val gamePreferencesFlow: Flow<GamePreferences> = dataStore.data.map { preferences ->
        val highScore = preferences[PreferenceKeys.HIGH_SCORE] ?: 0
        GamePreferences(highScore = highScore)
    }

    suspend fun updateHighScore(score: Int) {
        dataStore.edit { preferences ->
            val currentHighScore = preferences[PreferenceKeys.HIGH_SCORE] ?: 0
            if (currentHighScore < score) {
                preferences[PreferenceKeys.HIGH_SCORE] = score
            }
        }
    }
}

// use to
val repository = GameRepository(application)
val highScore: StateFlow<Int> = repository.highScore.stateIn(
    viewModelScope, SharingStarted.WhileSubscribed(), 0
)
viewModelScope.launch {
    repository.updateScore(_score.value)
}

```

### datastore preferences
primitive type 로컬에 저장
```groovy
// add dependencies
implementation "androidx.datastore:datastore-preferences:1.0.0"
```
```kotlin
// in GamePreferences
data class GamePreferences(
    val highScore: Int
)
object PreferenceKeys {
    val HIGH_SCORE = intPreferencesKey("high_score")
}
val Context.gameDataStore by preferencesDataStore(
    name = "GamePreferences"
)

// if use Flow to load
val gamePreferencesFlow: Flow<GamePreferences> = dataStore.data.map { preferences ->
    val highScore = preferences[PreferenceKeys.HIGH_SCORE] ?: 0
    GamePreferences(highScore = highScore) // return
}
// use to save
val score = 100
dataStore.edit { preferences ->
    preferences[PreferenceKeys.HIGH_SCORE] = score
}

```

### StateFlow with SavedStateHandle
StateFlow는 관찰 가능한 변경 가능 상태를 유지하도록 지원 (Flow로 사용하는 LiveData)</br>
SavedStateHandle는 viewmodel-ktx:2.5.0 이상부터 getStateFlow지원
```kotlin
// StateFlow Helper Class
class SavedMutableStateFlow<T>(
    private val savedStateHandle: SavedStateHandle,
    private val key: String,
    initialValue: T,
) {
    private val state: StateFlow<T> = savedStateHandle.getStateFlow(key, initialValue)
    var value: T
        get() = state.value
        set(value) {
            savedStateHandle[key] = value
        }
    fun asStateFlow(): StateFlow<T> = state
}
// StateFlow Helper function
fun <T> SavedStateHandle.getMutableStateFlow(key: String, initialValue: T): SavedMutableStateFlow<T> =
    SavedMutableStateFlow(this, key, initialValue)

// use in ViewModel
private val _score = stateHandler.getMutableStateFlow("score", 0)
val score: StateFlow<Int>
    get() = _score.asStateFlow()

// flow as StateFlow
// flow에서 stateIn을 사용해 StateFlow 생성 
val currentScrambledWord: StateFlow<Spannable>
    get() = _currentScrambledWord.asStateFlow().map { scrambledWord ->
        //...
    }.stateIn(viewModelScope, SharingStarted.Eagerly, SpannableString(""))
```

### LiveData with DataBinding
```groovy
// in build.gradle(Module)
plugins {
   id 'com.android.application'
   id 'kotlin-android'
   id 'kotlin-kapt'
}

android {
    //...
    buildFeatures {
        dataBinding = true
    }
    //...
}
```
```xml
<!-- add xml tag <layout><data></data></layout> in activity -->
<layout xmlns:android="http://schemas.android.com/apk/res/android">

   <data>
       <variable
           name="gameViewModel"
           type="com.example.android.unscramble.ui.game.GameViewModel" />
   </data>

   <androidx.constraintlayout.widget.ConstraintLayout
       android:layout_width="match_parent"
       android:layout_height="match_parent">
       <TextView
           android:id="@+id/textView_unscrambled_word"
           android:text="@{gameViewModel.currentScrambledWord}"
            <!--...-->
       />
       <!--...-->
   </androidx.constraintlayout.widget.ConstraintLayout>
</layout>
```
```kotlin
// in contoller
private val viewModel: GameViewModel by viewModels()
binding.gameViewModel = viewmodel
```


### LiveData
LiveData는 수명 주기를 인식하는 관찰 가능한 데이터 홀더 클래스  
모든 유형의 데이터에 사용할 수 있는 래퍼  
객체에서 보유한 데이터가 변경되면 관찰자에 알림 제공  
수명 주기를 인식

```kotlin
// in ViewModel
private val currentScrambledWord = MutableLiveData<String>()

// use Activity or Fragment
viewModel.currentScrambledWord.observe(viewLifecycleOwner) { newWord ->
    binding.textViewUnscrambledWord.text = newWord
}

```

### ViewModel lifecycle
다음 순서로 생명 주기가 흐른다.  
Activity - onCreated  
ViewModel created  
Activity - onDestroy  
ViewModel - onCleared  

### ViewModel 추가
```kotlin
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
https://developer.android.com/codelabs/basic-android-kotlin-training-livedata?continue=https%3A%2F%2Fdeveloper.android.com%2Fcourses%2Fpathways%2Fandroid-basics-kotlin-unit-3-pathway-3%23codelab-https%3A%2F%2Fdeveloper.android.com%2Fcodelabs%2Fbasic-android-kotlin-training-livedata#0  

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

