package com.example.funinactionproto

import android.content.Context
import android.os.Bundle
import android.view.View
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputMethodManager
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import androidx.fragment.app.Fragment

/** Step 1: the player enters a nickname for the leaderboard. */
class ChallengeNicknameFragment : Fragment(R.layout.fragment_challenge_nickname) {

    // Letters, numbers and spaces only, 2-15 characters. Keeps the public
    // leaderboard tidy and fits the Firestore rule.
    private val validName = Regex("^[\\p{L}\\p{N} ]{2,15}$")

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val input = view.findViewById<EditText>(R.id.nicknameInput)
        val error = view.findViewById<TextView>(R.id.nicknameError)
        val nextBtn = view.findViewById<Button>(R.id.nicknameNextBtn)

        fun submit() {
            val name = input.text.toString().trim()
            if (!validName.matches(name)) {
                error.text = "Use 2-15 letters or numbers"
                error.visibility = View.VISIBLE
                return
            }

            val imm = requireContext().getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
            imm.hideSoftInputFromWindow(input.windowToken, 0)

            ChallengeSession.nickname = name
            ChallengeFlow.toRules(requireActivity(), ChallengeFlow.CHALLENGE_SKILLS.first())
        }

        nextBtn.setOnClickListener { submit() }

        input.setOnEditorActionListener { _, actionId, _ ->
            if (actionId == EditorInfo.IME_ACTION_NEXT || actionId == EditorInfo.IME_ACTION_DONE) {
                submit()
                true
            } else {
                false
            }
        }
    }
}