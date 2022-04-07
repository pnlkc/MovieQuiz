package com.example.moviequiz.ui.game

import android.text.Spannable
import android.text.SpannableString
import android.text.style.TtsSpan
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Transformations
import androidx.lifecycle.ViewModel

class GameViewModel : ViewModel() {

    private val _score = MutableLiveData(0)
    val score: LiveData<Int>
        get() = _score

    private val _currentQuizCount = MutableLiveData(0)
    val currentQuizCount: LiveData<Int>
        get() = _currentQuizCount

    private val _currentChosung = MutableLiveData<String>()
    val currentChosung: LiveData<Spannable> = Transformations.map(_currentChosung) {
        if (it == null) {
            SpannableString("")
        } else {
            val movieChosung = it.toString()
            val spannable: Spannable = SpannableString(movieChosung)
            spannable.setSpan(
                TtsSpan.VerbatimBuilder(movieChosung).build(),
                0,
                movieChosung.length,
                Spannable.SPAN_INCLUSIVE_INCLUSIVE
            )
            spannable
        }
    }

    private var movieList: MutableList<String> = mutableListOf()
    private lateinit var currentMovie: String

    init {
        getNextWord()
    }

    private fun getNextWord() {
        currentMovie = allMovieList.random()
        val tempMovie = chosungExtractor(currentMovie)

        if (movieList.contains(currentMovie)) {
            getNextWord()
        } else {
            _currentChosung.value = tempMovie
            _currentQuizCount.value = (_currentQuizCount.value)?.inc()
            movieList.add(currentMovie)
        }
    }

    fun nextQuiz(): Boolean {
        return if (_currentQuizCount.value!! < MAX_NO_OF_WORDS) {
            getNextWord()
            true
        } else false
    }

    fun increaseScore() {
        _score.value = _score.value!! + SCORE_INCREASE
        //_score.value = _score.value?.plus(SCORE_INCREASE)
    }

    // 아래는 특수문자, 공백에 상관없이 정답을 인정하도록 만들었음
    fun isUserAnswerCorrect(playerAnswer: String): Boolean {
        var playerAnswerString = deleteBlankAndSpecialSymbol(playerAnswer)
        var currentMovieString = deleteBlankAndSpecialSymbol(currentMovie)

        var playerAnswerDeleteConlon = deleteBlankAndColon(playerAnswer)
        var currentMovieDeleteConlon = deleteBlankAndColon(currentMovie)

        var playerAnswerDeleteBlank = deleteBlank(playerAnswer)
        var currentMovieDeleteBlank = deleteBlank(currentMovie)


        // 추후에 프로그램 문제시 if의 조건을 playerWord.equals(currentWord, true) 로 변경
        if (playerAnswer == currentMovie ||
            playerAnswerString == currentMovieString ||
            playerAnswerDeleteBlank == currentMovieDeleteBlank||
            playerAnswerDeleteConlon == currentMovieDeleteConlon) {
            increaseScore()
            return true
        }
        return false
    }

    // 공백과 특수문자를 제거하여 한글만 있는 값을 생성하는 코드
    fun deleteBlankAndSpecialSymbol(inputText: String): String {
        var hangulString = ""
        for (element in inputText) {
            if (element != ':' && element != ',' && element != ' ')
                hangulString += element
        }
        return hangulString
    }

    fun deleteBlankAndColon(inputText: String): String {
        var hangulString = ""
        for (element in inputText) {
            if (element != ':' && element != ' ')
                hangulString += element
        }
        return hangulString
    }

    // 공백을 제거한 값을 생성하는 코드
    fun deleteBlank(inputText: String): String {
        var hangulString = ""
        for (element in inputText) {
            if (element != ' ')
                hangulString += element
        }
        return hangulString
    }


    fun reinitializeData() {
        _score.value = 0
        _currentQuizCount.value = 0
        movieList.clear()
        getNextWord()
    }

    fun chosungExtractor(text: String): String {
        val chosung = arrayOf(
            "ㄱ", "ㄲ", "ㄴ", "ㄷ", "ㄸ", "ㄹ",
            "ㅁ", "ㅂ", "ㅃ", "ㅅ", "ㅆ", "ㅇ",
            "ㅈ", "ㅉ", "ㅊ", "ㅋ", "ㅌ", "ㅍ", "ㅎ"
        )
        var movieChosung = ""
        for (element in text) {
            var hangul = element
            //한글과 특수문자를 구분하기 위해 아래의 if문 사용
            if (hangul.code >= 0xAC00) {
                hangul = (hangul.code - 0xAC00).toChar()
                val cho = (hangul.code / 28 / 21).toChar()
                movieChosung += chosung[cho.code]
            } else {
                movieChosung += hangul
            }
        }
        return movieChosung
    }
}