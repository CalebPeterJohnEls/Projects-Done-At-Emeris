package com.example.funinactionproto

import android.os.Bundle
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.EditText
import android.widget.Spinner
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.example.funinactionproto.api.AuthResponse
import com.example.funinactionproto.api.RegisterRequest
import com.example.funinactionproto.api.RetrofitClient
import com.example.funinactionproto.api.SessionManager
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

// Fragment-based screen that registers a user through the external API (The IIE, 2026b).
class RegisterFragment : Fragment(R.layout.fragment_register) {

    private var selectedRole: String = ""

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val name = view.findViewById<EditText>(R.id.regName)
        val phoneField = view.findViewById<EditText>(R.id.regPhone)
        val emailField = view.findViewById<EditText>(R.id.regEmail)
        val passwordField = view.findViewById<EditText>(R.id.regPassword)
        val roleSpinner = view.findViewById<Spinner>(R.id.regRole)
        val btnRegister = view.findViewById<Button>(R.id.btnRegister)
        val linkLogin = view.findViewById<TextView>(R.id.btnGoToLogin)
        val errorText = view.findViewById<TextView>(R.id.registerError)

        val roles = listOf("Select a role", "Player", "Parent")
        val adapter = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_item, roles)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        roleSpinner.adapter = adapter

        roleSpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                selectedRole = if (position == 0) "" else roles[position]
            }
            override fun onNothingSelected(parent: AdapterView<*>?) {
                selectedRole = ""
            }
        }

        btnRegister.setOnClickListener {
            errorText.visibility = View.GONE

            val fullName = name.text.toString().trim()
            val phone = phoneField.text.toString().trim()
            val emailValue = emailField.text.toString().trim()
            val passwordValue = passwordField.text.toString().trim()

            if (fullName.isEmpty() || phone.isEmpty() || emailValue.isEmpty() || passwordValue.isEmpty()) {
                Toast.makeText(context, "Please fill in all fields", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if (selectedRole.isEmpty()) {
                Toast.makeText(context, "Please select a role", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val parts = fullName.split(" ", limit = 2)
            val firstName = parts.getOrElse(0) { "" }
            val lastName = parts.getOrElse(1) { "" }

            // The app role is mapped to the role name the API expects (The IIE, 2026a).
            val apiRole = if (selectedRole == "Player") "Player" else "Client"

            btnRegister.isEnabled = false

            // Form values are packed into a data object that Retrofit sends to the API as JSON (CodeLucky, 2025; The IIE, 2026c).
            val request = RegisterRequest(
                firstName = firstName,
                lastName = lastName,
                email = emailValue,
                phone = phone,
                password = passwordValue,
                role = apiRole
            )

            // POST api/auth/register: the app never touches the database, the API creates the user
            // in the same Azure SQL database used by the MVC/API project (IIE Emeris School of Computer Science, 2025a; 2025b; Microsoft, 2026b).
            RetrofitClient.api(requireContext())
                .register(request)
                // enqueue runs the call on a background thread so the UI is not blocked (Android Developers, 2026a).
                .enqueue(object : Callback<AuthResponse> {
                    override fun onResponse(call: Call<AuthResponse>, response: Response<AuthResponse>) {
                        if (!isAdded) return
                        btnRegister.isEnabled = true
                        val body = response.body()
                        when {
                            response.isSuccessful && body != null -> {
                                // The API returns a JWT after saving the user; it is stored for later requests (Microsoft, 2026a).
                                SessionManager(requireContext()).saveSession(
                                    token = body.token,
                                    role = body.role,
                                    fullName = body.fullName,
                                    email = body.email,
                                    clientId = body.clientId
                                )
                                Toast.makeText(context, "Account created", Toast.LENGTH_SHORT).show()
                                (activity as MainActivity).replaceFragment(HomeFragment())
                            }
                            // 409 Conflict: the API found the email already saved in the database (The IIE, 2026a).
                            response.code() == 409 -> {
                                errorText.text = "An account with that email already exists."
                                errorText.visibility = View.VISIBLE
                            }
                            else -> {
                                errorText.text = "Couldn't create the account. Check your details and try again."
                                errorText.visibility = View.VISIBLE
                            }
                        }
                    }

                    // Called when the API cannot be reached, e.g. it is not running (Square, 2026).
                    override fun onFailure(call: Call<AuthResponse>, t: Throwable) {
                        if (!isAdded) return
                        btnRegister.isEnabled = true
                        errorText.text = "Couldn't reach the server: ${t.message}"
                        errorText.visibility = View.VISIBLE
                    }
                })
        }

        linkLogin.setOnClickListener {
            (activity as MainActivity).replaceFragment(LoginFragment())
        }
    }
}

// Android Developers, 2026a. Get data from the internet. [online] Available at: <https://developer.android.com/codelabs/basic-android-kotlin-compose-getting-data-internet> [Accessed 20 September 2026]
// CodeLucky, 2025. Retrofit Tutorial – REST API Client for Android with Kotlin. [video online] 9 October. Available at: <https://www.youtube.com/watch?v=iIjHHZNb_Qk> [Accessed 23 September 2026]
// IIE Emeris School of Computer Science, 2025a. CLDV6211 - Playlist. [video online] Available at: <https://www.youtube.com/playlist?list=PL480DYS-b_kevhFsiTpPIB2RzhKPig4iK> [Accessed 18 March 2025]
// IIE Emeris School of Computer Science, 2025b. CLDV6212 - Playlist. [video online] Available at: <https://www.youtube.com/playlist?list=PL480DYS-b_kcZiyuCyHolh6Nad8J_Xnk7> [Accessed 20 September 2025]
// Microsoft, 2026a. Minimal APIs security. [online] Available at: <https://learn.microsoft.com/en-us/aspnet/core/fundamentals/minimal-apis/security?view=aspnetcore-10.0> [Accessed 23 September 2026]
// Microsoft, 2026b. Tutorial: Connect to SQL Database from .NET App Service without secrets using a managed identity. [online] Available at: <https://learn.microsoft.com/en-us/azure/app-service/tutorial-connect-msi-sql-database?tabs=windowsclient%2Cefcore> [Accessed 24 September 2026]
// Square, 2026. Retrofit. [online] Available at: <https://square.github.io/retrofit/> [Accessed 16 September 2026]
// The IIE, 2026a. Programming 3A [PROG7311 Module Manual]. The Independent Institute of Education: Unpublished
// The IIE, 2026b. Programming 3C [PROG7313 Module Manual]. The Independent Institute of Education: Unpublished
// The IIE, 2026c. Programming 3D [PROG7314 Module Manual]. The Independent Institute of Education: Unpublished