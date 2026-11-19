package com.example.funinactionproto

import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.example.funinactionproto.api.AuthResponse
import com.example.funinactionproto.api.LoginRequest
import com.example.funinactionproto.api.RetrofitClient
import com.example.funinactionproto.api.SessionManager
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

// Fragment-based screen that logs a user in through the external API (The IIE, 2026b).
class LoginFragment : Fragment(R.layout.fragment_login) {

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val email = view.findViewById<EditText>(R.id.loginEmail)
        val password = view.findViewById<EditText>(R.id.loginPassword)
        val btnLogin = view.findViewById<Button>(R.id.btnLogin)
        val linkRegister = view.findViewById<TextView>(R.id.btnGoToRegister)
        val errorText = view.findViewById<TextView>(R.id.loginError)

        btnLogin.setOnClickListener {
            val e = email.text.toString().trim()
            val p = password.text.toString().trim()

            errorText.visibility = View.GONE

            if (e.isEmpty() || p.isEmpty()) {
                Toast.makeText(context, "Please enter your email and password", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            btnLogin.isEnabled = false

            // POST api/auth/login: the API checks the credentials against the users saved in the
            // shared Azure SQL database, so accounts made in the MVC site also work here (IIE Emeris School of Computer Science, 2025a; 2025b; Microsoft, 2026b).
            RetrofitClient.api(requireContext())
                .login(LoginRequest(email = e, password = p))
                // Asynchronous call, the result arrives in the callbacks below (Android Developers, 2026a; The IIE, 2026c).
                .enqueue(object : Callback<AuthResponse> {
                    override fun onResponse(call: Call<AuthResponse>, response: Response<AuthResponse>) {
                        if (!isAdded) return
                        btnLogin.isEnabled = true
                        val body = response.body()
                        if (response.isSuccessful && body != null) {
                            // The JWT returned by the API is saved and later attached to requests (Microsoft, 2026a).
                            SessionManager(requireContext()).saveSession(
                                token = body.token,
                                role = body.role,
                                fullName = body.fullName,
                                email = body.email,
                                clientId = body.clientId
                            )
                            (activity as MainActivity).replaceFragment(HomeFragment())
                        } else {
                            errorText.text = "Invalid email or password."
                            errorText.visibility = View.VISIBLE
                        }
                    }

                    override fun onFailure(call: Call<AuthResponse>, t: Throwable) {
                        if (!isAdded) return
                        btnLogin.isEnabled = true
                        errorText.text = "Couldn't reach the server: ${t.message}"
                        errorText.visibility = View.VISIBLE
                    }
                })
        }

        linkRegister.setOnClickListener {
            (activity as MainActivity).replaceFragment(RegisterFragment())
        }
    }
}

// Android Developers, 2026a. Get data from the internet. [online] Available at: <https://developer.android.com/codelabs/basic-android-kotlin-compose-getting-data-internet> [Accessed 20 September 2026]
// IIE Emeris School of Computer Science, 2025a. CLDV6211 - Playlist. [video online] Available at: <https://www.youtube.com/playlist?list=PL480DYS-b_kevhFsiTpPIB2RzhKPig4iK> [Accessed 18 March 2025]
// IIE Emeris School of Computer Science, 2025b. CLDV6212 - Playlist. [video online] Available at: <https://www.youtube.com/playlist?list=PL480DYS-b_kcZiyuCyHolh6Nad8J_Xnk7> [Accessed 20 September 2025]
// Microsoft, 2026a. Minimal APIs security. [online] Available at: <https://learn.microsoft.com/en-us/aspnet/core/fundamentals/minimal-apis/security?view=aspnetcore-10.0> [Accessed 23 September 2026]
// Microsoft, 2026b. Tutorial: Connect to SQL Database from .NET App Service without secrets using a managed identity. [online] Available at: <https://learn.microsoft.com/en-us/azure/app-service/tutorial-connect-msi-sql-database?tabs=windowsclient%2Cefcore> [Accessed 24 September 2026]
// The IIE, 2026b. Programming 3C [PROG7313 Module Manual]. The Independent Institute of Education: Unpublished
// The IIE, 2026c. Programming 3D [PROG7314 Module Manual]. The Independent Institute of Education: Unpublished