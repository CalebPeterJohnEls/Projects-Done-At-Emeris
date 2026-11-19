package com.example.funinactionproto

import android.os.Bundle
import android.util.Patterns
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import com.example.funinactionproto.api.ChangePasswordRequest
import com.example.funinactionproto.api.ProfileDto
import com.example.funinactionproto.api.RetrofitClient
import com.example.funinactionproto.api.SessionManager
import com.example.funinactionproto.api.UpdateProfileRequest
import android.content.Context
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class ProfileFragment : Fragment(R.layout.fragment_profile) {

    private lateinit var nameView: TextView
    private lateinit var emailView: TextView
    private lateinit var roleView: TextView
    private lateinit var phoneView: TextView

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        nameView = view.findViewById(R.id.profileName)
        emailView = view.findViewById(R.id.profileEmail)
        roleView = view.findViewById(R.id.profileRole)
        phoneView = view.findViewById(R.id.profilePhone)

        val session = SessionManager(requireContext())

        session.getFullName()?.let { nameView.text = it }
        session.getEmail()?.let { emailView.text = it }
        session.getRole()?.let { roleView.text = it }

        loadProfile()

        view.findViewById<View>(R.id.rowEditProfile).setOnClickListener { showEditProfileDialog() }
        view.findViewById<View>(R.id.rowChangePassword).setOnClickListener { showChangePasswordDialog() }

        view.findViewById<Button>(R.id.btnLogout).setOnClickListener {
            session.clear()
            (activity as MainActivity).replaceFragment(LoginFragment())
        }
    }

    // ---------- Load / refresh profile (your original call) ----------
    private fun loadProfile() {
        RetrofitClient.api(requireContext()).getProfile().enqueue(object : Callback<ProfileDto> {
            override fun onResponse(call: Call<ProfileDto>, response: Response<ProfileDto>) {
                if (!isAdded) return
                val profile = response.body()
                if (response.isSuccessful && profile != null) {
                    nameView.text = profile.fullName
                    emailView.text = profile.email
                    roleView.text = profile.role
                    phoneView.text = profile.phone?.takeIf { it.isNotBlank() } ?: "Not set"
                }
            }

            override fun onFailure(call: Call<ProfileDto>, t: Throwable) {
                if (!isAdded) return
                Toast.makeText(context, "Couldn't refresh profile: ${t.message}", Toast.LENGTH_SHORT).show()
            }
        })
    }

    // ---------- Edit personal information ----------
    private fun showEditProfileDialog() {
        val dialogView = layoutInflater.inflate(R.layout.dialog_edit_profile, null)
        val nameInput = dialogView.findViewById<EditText>(R.id.inputFullName)
        val emailInput = dialogView.findViewById<EditText>(R.id.inputEmail)
        val phoneInput = dialogView.findViewById<EditText>(R.id.inputPhone)

        nameInput.setText(nameView.text)
        emailInput.setText(emailView.text)
        // "Not set" is just a placeholder on the profile page, so leave the field blank in that case
        phoneInput.setText(if (phoneView.text.toString() == "Not set") "" else phoneView.text)

        val dialog = AlertDialog.Builder(requireContext())
            .setTitle("Edit Personal Information")
            .setView(dialogView)
            .setNegativeButton("Cancel", null)
            .setPositiveButton("Save", null) // overridden below so the dialog stays open on errors
            .create()

        dialog.setOnShowListener {
            val saveBtn = dialog.getButton(AlertDialog.BUTTON_POSITIVE)
            saveBtn.setOnClickListener {
                val newName = nameInput.text.toString().trim()
                val newEmail = emailInput.text.toString().trim()
                val newPhone = phoneInput.text.toString().trim()

                if (newName.isEmpty()) {
                    nameInput.error = "Name can't be empty"
                    return@setOnClickListener
                }
                if (!Patterns.EMAIL_ADDRESS.matcher(newEmail).matches()) {
                    emailInput.error = "Enter a valid email address"
                    return@setOnClickListener
                }
                // Phone is optional, but if entered it needs 9-15 digits
                val phoneDigits = newPhone.count { it.isDigit() }
                if (newPhone.isNotEmpty() &&
                    (!Patterns.PHONE.matcher(newPhone).matches() || phoneDigits !in 9..15)
                ) {
                    phoneInput.error = "Enter a valid phone number"
                    return@setOnClickListener
                }

                saveBtn.isEnabled = false
                RetrofitClient.api(requireContext())
                    .updateProfile(UpdateProfileRequest(newName, newEmail, newPhone))
                    .enqueue(object : Callback<ProfileDto> {
                        override fun onResponse(call: Call<ProfileDto>, response: Response<ProfileDto>) {
                            if (!isAdded) return
                            if (response.isSuccessful) {
                                // Use the server's version if returned, otherwise what the user typed
                                val updated = response.body()
                                nameView.text = updated?.fullName ?: newName
                                emailView.text = updated?.email ?: newEmail
                                phoneView.text = (updated?.phone ?: newPhone).ifBlank { "Not set" }
                                Toast.makeText(context, "Profile updated", Toast.LENGTH_SHORT).show()
                                dialog.dismiss()
                            } else {
                                saveBtn.isEnabled = true
                                // CHANGED: friendly messages for the common failures instead of a bare status code
                                val msg = when (response.code()) {
                                    409 -> "That email is already in use"
                                    400 -> "Please check your details and try again"
                                    401 -> "Your session has expired. Please log in again."
                                    else -> "Update failed (${response.code()})"
                                }
                                Toast.makeText(context, msg, Toast.LENGTH_SHORT).show()
                            }
                        }

                        override fun onFailure(call: Call<ProfileDto>, t: Throwable) {
                            if (!isAdded) return
                            saveBtn.isEnabled = true
                            Toast.makeText(context, "Network error: ${t.message}", Toast.LENGTH_SHORT).show()
                        }
                    })
            }
        }
        dialog.show()
    }

    // ---------- Change password ----------
    private fun showChangePasswordDialog() {
        val dialogView = layoutInflater.inflate(R.layout.dialog_change_password, null)
        val currentInput = dialogView.findViewById<EditText>(R.id.inputCurrentPassword)
        val newInput = dialogView.findViewById<EditText>(R.id.inputNewPassword)
        val confirmInput = dialogView.findViewById<EditText>(R.id.inputConfirmPassword)

        val dialog = AlertDialog.Builder(requireContext())
            .setTitle("Change Password")
            .setView(dialogView)
            .setNegativeButton("Cancel", null)
            .setPositiveButton("Update", null)
            .create()

        dialog.setOnShowListener {
            val updateBtn = dialog.getButton(AlertDialog.BUTTON_POSITIVE)
            updateBtn.setOnClickListener {
                val current = currentInput.text.toString()
                val new = newInput.text.toString()
                val confirm = confirmInput.text.toString()

                when {
                    current.isEmpty() -> {
                        currentInput.error = "Enter your current password"
                        return@setOnClickListener
                    }
                    new.length < 8 -> {
                        newInput.error = "Use at least 8 characters"
                        return@setOnClickListener
                    }
                    // NEW: matches the API's 128-character maximum
                    new.length > 128 -> {
                        newInput.error = "Use at most 128 characters"
                        return@setOnClickListener
                    }
                    new == current -> {
                        newInput.error = "New password must be different"
                        return@setOnClickListener
                    }
                    new != confirm -> {
                        confirmInput.error = "Passwords don't match"
                        return@setOnClickListener
                    }
                }

                updateBtn.isEnabled = false
                RetrofitClient.api(requireContext())
                    .changePassword(ChangePasswordRequest(current, new))
                    .enqueue(object : Callback<Void> {
                        override fun onResponse(call: Call<Void>, response: Response<Void>) {
                            if (!isAdded) return
                            if (response.isSuccessful) {
                                Toast.makeText(context, "Password changed", Toast.LENGTH_SHORT).show()
                                dialog.dismiss()
                            } else {
                                updateBtn.isEnabled = true
                                // CHANGED: the message now depends on the real reason for the failure
                                Toast.makeText(context, passwordErrorMessage(response), Toast.LENGTH_LONG).show()
                            }
                        }

                        override fun onFailure(call: Call<Void>, t: Throwable) {
                            if (!isAdded) return
                            updateBtn.isEnabled = true
                            Toast.makeText(context, "Network error: ${t.message}", Toast.LENGTH_SHORT).show()
                        }
                    })
            }
        }
        dialog.show()
    }

    // NEW: turns a failed change-password response into a specific, user-friendly message.
    // Matches the codes/reasons the MVC site handles: 429 = rate limited, 401 = expired token,
    // 400 + reason text = wrong current password / same password / weak password.
    private fun passwordErrorMessage(response: Response<Void>): String {
        // errorBody() can only be read once, so read it here and keep the text
        val body = try {
            response.errorBody()?.string().orEmpty()
        } catch (e: Exception) {
            ""
        }
        return when {
            response.code() == 429 -> "Too many attempts. Please try again in a few minutes."
            response.code() == 401 -> "Your session has expired. Please log in again."
            body.contains("wrong_current_password") -> "Current password is incorrect"
            body.contains("same_password") -> "New password must be different from the current one"
            response.code() == 400 -> "The new password doesn't meet the requirements"
            else -> "Couldn't change password (${response.code()})"
        }
    }
}