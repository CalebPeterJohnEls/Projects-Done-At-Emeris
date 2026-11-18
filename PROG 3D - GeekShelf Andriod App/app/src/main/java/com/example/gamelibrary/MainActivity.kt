package com.example.gamelibrary

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.activity.viewModels
import androidx.navigation.compose.navigation
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ChevronLeft
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.StarBorder
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.FilterQuality
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.imageResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.zIndex
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import coil.compose.AsyncImage
import com.example.gamelibrary.api.MockAuthApiService
import com.example.gamelibrary.data.AccountRepository
import com.example.gamelibrary.data.SettingsRepository
import com.example.gamelibrary.model.CollectionItem
import com.example.gamelibrary.model.MediaSearchResult
import com.example.gamelibrary.model.MediaSearchViewModel
import com.example.gamelibrary.model.MediaType
import com.example.gamelibrary.model.ShelfId
import com.example.gamelibrary.ui.HomeScreen
import com.example.gamelibrary.ui.ProfileScreen
import com.example.gamelibrary.ui.SettingsScreen
import com.example.gamelibrary.ui.SettingsViewModel
import com.example.gamelibrary.ui.SettingsViewModelFactory
import com.example.gamelibrary.ui.WishlistScreen
import com.example.gamelibrary.ui.theme.GameLibraryTheme
import com.example.gamelibrary.util.LocaleHelper
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.auth
import com.google.firebase.firestore.firestore
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlin.collections.emptyList
import com.example.gamelibrary.util.ProfileScreen

private const val TAG = "MainActivity"

class MainActivity : ComponentActivity() {
    private lateinit var settingsRepository: SettingsRepository

    private val accountRepository =
        AccountRepository(
            authApi = MockAuthApiService()
        )

    private val settingsViewModel: SettingsViewModel by viewModels {
        SettingsViewModelFactory(
            settingsRepository,
            accountRepository
        )
    }

    override fun attachBaseContext(newBase: Context) {
        val language = LocaleHelper.getPersistedLanguage(newBase)
        super.attachBaseContext(
            LocaleHelper.wrap(newBase, language)
        )
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Initialize the repository BEFORE the ViewModel is requested.
        settingsRepository =
            SettingsRepository(applicationContext)

        enableEdgeToEdge()

        setContent {
            val uiState by settingsViewModel
                .uiState
                .collectAsStateWithLifecycle()

            GameLibraryTheme(
                themePreference = uiState.theme
            ) {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    AppNavigation(
                        settingsViewModel = settingsViewModel
                    )
                }
            }
        }

        handleIntent(intent)
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        handleIntent(intent)
    }

    private fun handleIntent(intent: Intent?) {
        val uri: Uri = intent?.data ?: return

        if (
            uri.scheme != "gamelibrary" ||
            uri.host != "auth"
        ) {
            return
        }

        when (uri.path) {
            "/steam" -> {
                Log.d(TAG, "Received Steam OpenID callback: $uri")
                settingsViewModel.onSteamCallback(uri)
            }

            "/epic" -> {
                Log.d(TAG, "Received Epic OAuth2 callback: $uri")
                settingsViewModel.onEpicCallback(uri)
            }

            "/xbox" -> {
                Log.d(TAG, "Received Xbox/Microsoft OAuth2 callback: $uri")
                settingsViewModel.onXboxCallback(uri)
            }
        }
    }
}



@Composable // (Google, n.d.a)
fun AppNavigation(
    // (Google, n.d.b)
    settingsViewModel: SettingsViewModel
) {
    var currentScreen by remember {
        mutableStateOf(
            if (Firebase.auth.currentUser != null) {
                "book"
            } else {
                "landing"
            }
        )
    }

    var currentUser by remember {
        mutableStateOf(Firebase.auth.currentUser)
    }

    // Fires whenever the signed-in user's uid changes: fresh login,
    // registration, switching accounts, or signing out (uid -> null).
    // AccountRepository is a process-wide singleton, so it must be
    // reset here to avoid leaking one user's linked accounts (Steam,
    // Epic, Xbox, PSN) into another user's Settings screen.
    LaunchedEffect(currentUser?.uid) {
        if (currentUser != null) {
            Log.d(
                TAG,
                "Firebase user detected - syncing linked accounts"
            )
            settingsViewModel.onUserChanged()
        } else {
            Log.d(
                TAG,
                "No Firebase user - clearing linked accounts"
            )
            settingsViewModel.clearAccounts()
        }
    }

    DisposableEffect(Unit) {
        val listener =
            FirebaseAuth.AuthStateListener { auth ->

                val user = auth.currentUser

                currentUser = user

                if (user == null) {
                    currentScreen = "landing"
                } else if (currentScreen == "landing") {
                    currentScreen = "book"
                }
            }
        // (Google, n.d.d)
        Firebase.auth.addAuthStateListener(listener)

        onDispose {
            Firebase.auth.removeAuthStateListener(listener)
        }
    }

    when (currentScreen) {
        "landing" -> {
            LandingScreen(
                onLogin = {
                    currentScreen = "login"
                },
                onRegister = {
                    currentScreen = "register"
                },
                onGoogleSignInSuccess = { user ->
                    currentUser = user
                    currentScreen = "book"
                }
            )
        }
        "login" -> {
            LoginScreen(
                onLoginSuccess = { user ->
                    currentUser = user
                    currentScreen = "book"
                },
                onGoToRegister = {
                    currentScreen = "register"
                },
                onBack = {
                    currentScreen = "landing"
                }
            )
        }
        "register" -> {
            RegisterScreen(
                onRegisterSuccess = { user ->
                    currentUser = user
                    currentScreen = "book"
                },
                onGoToLogin = {
                    currentScreen = "login"
                },
                onBack = {
                    currentScreen = "landing"
                }
            )
        }
        "book" -> {
            BottomNavShell(
                settingsViewModel = settingsViewModel,
                onAccountDeleted = {
                    currentUser = null
                    currentScreen = "landing"
                },
                onLogout = {
                    currentUser = null
                    currentScreen = "landing"
                }
            )
        }
    }
}

@Composable
// (Google, n.d.b)
//Android Developers (2024); Google, (2026)
fun BottomNavShell(
    settingsViewModel: SettingsViewModel,
    onAccountDeleted: () -> Unit,
    onLogout: () -> Unit
) {
    var selectedTab by remember { mutableStateOf(0) }

    Scaffold(
        bottomBar = {
            NavigationBar {
                NavigationBarItem(
                    icon = { Icon(Icons.Default.Home, contentDescription = stringResource(R.string.nav_home)) },
                    label = { Text(stringResource(R.string.nav_home)) },
                    selected = selectedTab == 0,
                    onClick = { selectedTab = 0 }
                )
                NavigationBarItem(
                    icon = { Icon(Icons.Default.Star, contentDescription = stringResource(R.string.nav_wishlist)) },
                    label = { Text(stringResource(R.string.nav_wishlist)) },
                    selected = selectedTab == 1,
                    onClick = { selectedTab = 1 }
                )
                NavigationBarItem(
                    icon = { Icon(Icons.Default.AccountCircle, contentDescription = stringResource(R.string.nav_profile)) },
                    label = { Text(stringResource(R.string.nav_profile)) },
                    selected = selectedTab == 2,
                    onClick = { selectedTab = 2 }
                )
                NavigationBarItem(
                    icon = { Icon(Icons.Default.Settings, contentDescription = stringResource(R.string.nav_settings)) },
                    label = { Text(stringResource(R.string.nav_settings)) },
                    selected = selectedTab == 3,
                    onClick = { selectedTab = 3 }
                )
            }
        }
    ) { innerPadding -> //Android Developers (2024); Google, (2026)
        Column(modifier = Modifier.padding(innerPadding).fillMaxSize()) {
            when (selectedTab) {
                0 -> RoomNavHost()
                1 -> WishlistScreen()
                2 -> ProfileScreen(onAccountDeleted = onAccountDeleted, onLogout = onLogout)
                else -> SettingsScreen(viewModel = settingsViewModel)
            }
        }
    }
}

fun MediaType.displayName() = when (this) {
    MediaType.GAME -> "Game"
    MediaType.MOVIE -> "Movie"
    MediaType.TV_SERIES -> "TV Series"
    MediaType.COMIC_BOOK -> "Comic Book"
    MediaType.ANIME -> "Anime"
    MediaType.MANGA -> "Manga"
}

@Composable
fun RoomNavHost() {
    val navController = rememberNavController()

    NavHost(navController = navController, startDestination = "room") {
        composable("room") {
            RoomDashboardScreen(
                onShelfClick = { shelfId -> navController.navigate("shelfDetail/$shelfId") },
                onAddItemClick = { navController.navigate("addItem") }
            )
        }
        composable(
            route = "shelfDetail/{shelfId}",
            arguments = listOf(navArgument("shelfId") { type = NavType.StringType })
        ) { backStackEntry ->
            val shelfId = backStackEntry.arguments?.getString("shelfId") ?: ""
            ShelfDetailScreen(
                shelfId = shelfId,
                onBack = { navController.popBackStack() },
                onItemClick = { itemId -> navController.navigate("itemDetail/$itemId") }
            )
        }
        composable(
            route = "itemDetail/{itemId}",
            arguments = listOf(navArgument("itemId") { type = NavType.StringType })
        ) { backStackEntry ->
            val itemId = backStackEntry.arguments?.getString("itemId") ?: ""
            ItemDetailScreen(itemId = itemId, onBack = { navController.popBackStack() })
        }
        navigation(startDestination = "searchForm", route = "addItem") {
            composable("searchForm") { backStackEntry ->
                val parentEntry = remember(backStackEntry) { navController.getBackStackEntry("addItem") }
                val vm: MediaSearchViewModel = viewModel(parentEntry)
                SearchFormScreen(
                    viewModel = vm,
                    onSearchSubmit = { navController.navigate("resultsForm") },
                    onBack = { navController.popBackStack() }
                )
            }
            composable("resultsForm") { backStackEntry ->
                val parentEntry = remember(backStackEntry) { navController.getBackStackEntry("addItem") }
                val vm: MediaSearchViewModel = viewModel(parentEntry)
                ResultsFormScreen(
                    viewModel = vm,
                    onItemSelected = { vm.selectResult(it); navController.navigate("additionalInfo") },
                    onBack = { navController.popBackStack() }
                )
            }
            composable("additionalInfo") { backStackEntry ->
                val parentEntry = remember(backStackEntry) { navController.getBackStackEntry("addItem") }
                val vm: MediaSearchViewModel = viewModel(parentEntry)
                AdditionalInfoScreen(
                    selectedResult = vm.selectedResult,
                    onSaved = { navController.navigate("room") { popUpTo("room") { inclusive = true } } },
                    onBack = { navController.popBackStack() }
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SearchFormScreen(viewModel: MediaSearchViewModel, onSearchSubmit: () -> Unit, onBack: () -> Unit) {
    Scaffold(topBar = {
        TopAppBar(title = { Text("Add to Collection") },
            navigationIcon = { IconButton(onClick = onBack) { Icon(Icons.Default.ArrowBack, null) } })
    }) { padding ->
        Column(Modifier.padding(padding).padding(16.dp).fillMaxSize()) {
            Text("Media Type", style = MaterialTheme.typography.labelLarge)
            Row(
                modifier = Modifier.horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                MediaType.entries.forEach { type ->
                    FilterChip(
                        selected = viewModel.mediaType == type,
                        onClick = { viewModel.mediaType = type },
                        label = { Text(type.name.lowercase().replaceFirstChar { it.uppercase() }) }
                    )
                }
            }
            Spacer(Modifier.height(24.dp))
            OutlinedTextField(
                value = viewModel.query,
                onValueChange = { viewModel.query = it },
                label = { Text("Title") },
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(Modifier.height(16.dp))
            Button(
                onClick = { viewModel.search(); onSearchSubmit() },
                enabled = viewModel.query.isNotBlank(),
                modifier = Modifier.fillMaxWidth()
            ) { Text("Search") }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdditionalInfoScreen(selectedResult: MediaSearchResult?, onSaved: () -> Unit, onBack: () -> Unit) {
    var rating by remember { mutableIntStateOf(0) }
    var comments by remember { mutableStateOf("") }
    var selectedShelf by remember { mutableStateOf(ShelfId.SHELF_TOP_ROOM1) }
    var shelfMenuExpanded by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()
    val context = LocalContext.current

    Scaffold(topBar = {
        TopAppBar(
            title = { Text("Add Details") },
            navigationIcon = {
                IconButton(onClick = onBack) {
                    Icon(
                        Icons.AutoMirrored.Filled.ArrowBack,
                        null
                    )
                }
            })
    }) { padding ->
        Column(Modifier.padding(padding).padding(16.dp).fillMaxSize()) {
            selectedResult?.let {
                Text(it.title, style = MaterialTheme.typography.headlineSmall)
                Spacer(Modifier.height(16.dp))
            }

            Text("Which shelf?", style = MaterialTheme.typography.labelLarge)
            ExposedDropdownMenuBox(
                expanded = shelfMenuExpanded,
                onExpandedChange = { shelfMenuExpanded = it }) {
                OutlinedTextField(
                    value = selectedShelf.label,
                    onValueChange = {},
                    readOnly = true,
                    modifier = Modifier.menuAnchor().fillMaxWidth(),
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = shelfMenuExpanded) }
                )
                ExposedDropdownMenu(
                    expanded = shelfMenuExpanded,
                    onDismissRequest = { shelfMenuExpanded = false }) {
                    ShelfId.entries.forEach { shelf ->
                        DropdownMenuItem(
                            text = { Text(shelf.label) },
                            onClick = { selectedShelf = shelf; shelfMenuExpanded = false }
                        )
                    }
                }
            }

            Spacer(Modifier.height(24.dp))
            Text("Your Rating")
            Row {
                (1..5).forEach { star ->
                    IconButton(onClick = { rating = star }) {
                        Icon(
                            imageVector = if (star <= rating) Icons.Default.Star else Icons.Default.StarBorder,
                            contentDescription = "$star stars"
                        )
                    }
                }
            }
            Spacer(Modifier.height(16.dp))
            OutlinedTextField(
                value = comments, onValueChange = { comments = it },
                label = { Text("Comments") },
                modifier = Modifier.fillMaxWidth().height(120.dp)
            )
            Spacer(Modifier.height(24.dp))
            Button(
                onClick = {
                    Firebase.firestore.collection("collectionItems").add(
                        CollectionItem(
                            userId = Firebase.auth.currentUser?.uid ?: "",
                            mediaType = selectedResult?.mediaType ?: MediaType.GAME,
                            externalId = selectedResult?.externalId ?: "",
                            title = selectedResult?.title ?: "",
                            imageUrl = selectedResult?.imageUrl ?: "",
                            rating = rating,
                            comments = comments,
                            shelfId = selectedShelf.name
                        )
                    ).addOnFailureListener { e ->
                        Toast.makeText(context, "Save failed: ${e.message}", Toast.LENGTH_LONG).show()
                    }

                    Toast.makeText(context, "Item saved to shelf!", Toast.LENGTH_SHORT).show()
                    onSaved()
                },
                modifier = Modifier.fillMaxWidth()
            ) { Text("Add to Shelf") }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ResultsFormScreen(viewModel: MediaSearchViewModel, onItemSelected: (MediaSearchResult) -> Unit, onBack: () -> Unit) {
    Scaffold(topBar = {
        TopAppBar(title = { Text("Results") },
            navigationIcon = { IconButton(onClick = onBack) { Icon(Icons.Default.ArrowBack, null) } })
    }) { padding ->
        viewModel.errorMessage?.let {
            Box(Modifier.padding(padding).fillMaxSize(), contentAlignment = Alignment.Center) { Text(it) }
            return@Scaffold
        }
        LazyColumn(Modifier.padding(padding).fillMaxSize()) {
            items(viewModel.results) { result ->
                Card(Modifier.fillMaxWidth().padding(8.dp)) {
                    Row(Modifier.padding(8.dp), verticalAlignment = Alignment.CenterVertically) {
                        AsyncImage(
                            model = result.imageUrl, contentDescription = null,
                            modifier = Modifier.size(64.dp), contentScale = ContentScale.Crop
                        )
                        Spacer(Modifier.width(12.dp))
                        Column(Modifier.weight(1f)) {
                            Text(result.title, style = MaterialTheme.typography.titleMedium)
                            result.subtitle?.let { Text(it, style = MaterialTheme.typography.bodySmall) }
                        }
                        IconButton(onClick = { onItemSelected(result) }) {
                            Icon(Icons.Default.Add, contentDescription = "Add ${result.title}")
                        }
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ShelfDetailScreen(
    shelfId: String,
    onBack: () -> Unit,
    onItemClick: (String) -> Unit
) {
    var items by remember { mutableStateOf<List<CollectionItem>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }
    var itemPendingDelete by remember { mutableStateOf<CollectionItem?>(null) }

    LaunchedEffect(shelfId) {
        Firebase.firestore.collection("collectionItems")
            .whereEqualTo("userId", Firebase.auth.currentUser?.uid)
            .whereEqualTo("shelfId", shelfId)
            .addSnapshotListener { snapshot, _ ->
                items = snapshot?.documents?.mapNotNull { doc ->
                    doc.toObject(CollectionItem::class.java)?.copy(id = doc.id)
                } ?: emptyList()
                isLoading = false
            }
    }
    itemPendingDelete?.let { item ->
        AlertDialog(
            onDismissRequest = { itemPendingDelete = null },
            title = { Text("Remove item?") },
            text = { Text("Remove \"${item.title}\" from this shelf?") },
            confirmButton = {
                TextButton(onClick = {
                    Firebase.firestore.collection("collectionItems").document(item.id).delete()
                    itemPendingDelete = null
                }) { Text("Remove") }
            },
            dismissButton = {
                TextButton(onClick = { itemPendingDelete = null }) { Text("Cancel") }
            }
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(ShelfId.entries.find { it.name == shelfId }?.label ?: shelfLabel(shelfId)) },
                navigationIcon = { IconButton(onClick = onBack) { Icon(Icons.AutoMirrored.Filled.ArrowBack, null) } }
            )
        }
    ) { padding ->
        when {
            isLoading -> Box(Modifier.padding(padding).fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
            items.isEmpty() -> Box(Modifier.padding(padding).fillMaxSize(), contentAlignment = Alignment.Center) {
                Text("Nothing on this shelf yet")
            }
            else -> LazyVerticalGrid(
                columns = GridCells.Fixed(2),
                modifier = Modifier.padding(padding).fillMaxSize(),
                contentPadding = PaddingValues(12.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(items) { item ->
                    Column(
                        modifier = Modifier.clickable { onItemClick(item.id) }
                    ) {
                        Box {
                            AsyncImage(
                                model = item.imageUrl,
                                contentDescription = item.title,
                                modifier = Modifier.fillMaxWidth().aspectRatio(2f / 3f),
                                contentScale = ContentScale.Crop
                            )
                            IconButton(
                                onClick = { itemPendingDelete = item },
                                modifier = Modifier
                                    .align(Alignment.TopEnd)
                                    .padding(4.dp)
                                    .background(Color.Black.copy(alpha = 0.5f), shape = CircleShape)
                                    .size(28.dp)
                            ) {
                                Icon(
                                    Icons.Default.Delete,
                                    contentDescription = "Remove ${item.title}",
                                    tint = Color.White,
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                        }
                        Text(item.title, style = MaterialTheme.typography.bodyMedium, maxLines = 1)
                        Row {
                            repeat(item.rating) {
                                Icon(Icons.Default.Star, contentDescription = null, modifier = Modifier.size(14.dp))
                            }
                        }
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ItemDetailScreen(itemId: String, onBack: () -> Unit) {
    var item by remember { mutableStateOf<CollectionItem?>(null) }
    var isLoading by remember { mutableStateOf(true) }

    LaunchedEffect(itemId) {
        Firebase.firestore.collection("collectionItems").document(itemId)
            .addSnapshotListener { snapshot, _ ->
                item = snapshot?.toObject(CollectionItem::class.java)?.copy(id = snapshot.id)
                isLoading = false
            }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(item?.title ?: "Details") },
                navigationIcon = { IconButton(onClick = onBack) { Icon(Icons.AutoMirrored.Filled.ArrowBack, null) } }
            )
        }
    ) { padding ->
        when {
            isLoading -> Box(Modifier.padding(padding).fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
            item == null -> Box(Modifier.padding(padding).fillMaxSize(), contentAlignment = Alignment.Center) {
                Text("Item not found")
            }
            else -> {
                val current = item!!
                Column(
                    modifier = Modifier
                        .padding(padding)
                        .fillMaxSize()
                        .verticalScroll(rememberScrollState())
                        .padding(16.dp)
                ) {
                    AsyncImage(
                        model = current.imageUrl,
                        contentDescription = current.title,
                        modifier = Modifier.fillMaxWidth().aspectRatio(2f / 3f),
                        contentScale = ContentScale.Crop
                    )

                    Spacer(Modifier.height(16.dp))

                    Text(current.title, style = MaterialTheme.typography.headlineSmall)
                    Text(current.mediaType.displayName(), style = MaterialTheme.typography.bodyMedium)

                    Spacer(Modifier.height(16.dp))

                    Text("Your Rating", style = MaterialTheme.typography.labelLarge)
                    Row {
                        repeat(5) { index ->
                            Icon(
                                imageVector = if (index < current.rating) Icons.Default.Star else Icons.Default.StarBorder,
                                contentDescription = null
                            )
                        }
                    }

                    Spacer(Modifier.height(16.dp))

                    Text("Comments", style = MaterialTheme.typography.labelLarge)
                    Text(
                        text = current.comments.ifBlank { "No comments added" },
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            }
        }
    }
}

val PixelFont = FontFamily(
    Font(R.font.jersey10_regular)
)

val GradientPink = Color(0xFFE94FD1)
val GradientPurple = Color(0xFF7B3FE4)
val GradientBlue = Color(0xFF4A6CF7)
val AccentPurple = Color(0xFF4B3F9E)
fun shelfLabel(shelfId: String) = shelfId.replace("_", " ").replaceFirstChar { it.uppercase() }

@Composable
fun PixelGradientBackground(
    content: @Composable BoxScope.() -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.linearGradient(
                    colors = listOf(
                        GradientPink,
                        GradientPurple,
                        GradientBlue
                    ),
                    start = Offset(0f, 0f),
                    end = Offset(1000f, 1600f)
                )
            )
    ) {
        Box(
            Modifier
                .size(220.dp)
                .align(Alignment.TopEnd)
                .offset(x = 60.dp, y = (-40).dp)
                .clip(CircleShape)
                .background(Color.White.copy(alpha = 0.08f))
        )

        Box(
            Modifier
                .size(160.dp)
                .align(Alignment.TopStart)
                .offset(x = (-50).dp, y = 260.dp)
                .clip(CircleShape)
                .background(Color.White.copy(alpha = 0.10f))
        )

        Box(
            Modifier
                .size(260.dp)
                .align(Alignment.BottomStart)
                .offset(x = (-80).dp, y = 60.dp)
                .clip(CircleShape)
                .background(Color.White.copy(alpha = 0.12f))
        )

        Box(
            Modifier
                .size(180.dp)
                .align(Alignment.BottomEnd)
                .offset(x = 40.dp, y = (-40).dp)
                .clip(CircleShape)
                .background(Color.White.copy(alpha = 0.10f))
        )

        content()
    }
}

@Composable
fun GeekShelfLogo(
    modifier: Modifier = Modifier,
    size: androidx.compose.ui.unit.Dp = 150.dp
) {
    Image(
        painter = painterResource(
            id = R.drawable.geekshelf_logo2
        ),
        contentDescription = "App logo",
        contentScale = androidx.compose.ui.layout.ContentScale.Fit,
        modifier = modifier.size(size)
    )
}

@Composable
fun PixelPillButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    filled: Boolean = true,
    containerColor: Color = AccentPurple
) {
    Button(
        onClick = onClick,
        shape = RoundedCornerShape(50),
        colors = ButtonDefaults.buttonColors(
            containerColor =
                if (filled) containerColor
                else Color.Transparent
        ),
        elevation = ButtonDefaults.buttonElevation(
            defaultElevation = 0.dp
        ),
        border = androidx.compose.foundation.BorderStroke(
            2.dp,
            Color.White
        ),
        contentPadding = PaddingValues(
            vertical = 14.dp
        ),
        modifier = modifier
    ) {
        Text(
            text = text,
            color = Color.White,
            fontFamily = PixelFont,
            fontSize = 13.sp
        )
    }
}

@Composable
fun PixelTextField(
    value: String,
    onValueChange: (String) -> Unit,
    placeholder: String,
    modifier: Modifier = Modifier,
    isPassword: Boolean = false
) {
    BasicTextField(
        value = value,
        onValueChange = onValueChange,
        singleLine = true,
        textStyle = TextStyle(
            fontFamily = PixelFont,
            fontSize = 13.sp,
            color = Color.Black
        ),
        visualTransformation =
            if (isPassword) PasswordVisualTransformation()
            else VisualTransformation.None,
        cursorBrush = SolidColor(Color.Black),
        modifier = modifier
            .fillMaxWidth()
            .border(2.dp, Color.Black)
            .background(Color.White)
            .padding(
                horizontal = 14.dp,
                vertical = 14.dp
            ),
        decorationBox = { innerTextField ->
            Box {
                if (value.isEmpty()) {
                    Text(
                        text = placeholder,
                        color = Color.Gray,
                        fontFamily = PixelFont,
                        fontSize = 12.sp
                    )
                }

                innerTextField()
            }
        }
    )
}

@Composable
fun BottomCard(
    modifier: Modifier = Modifier,
    content: @Composable ColumnScope.() -> Unit
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .background(Color.White)
            .border(3.dp, Color.Black)
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        content()
    }
}

@Composable
fun RoomPartOne(
    onShelfClick: (String) -> Unit,
    onAddItemClick: () -> Unit
) {

    val canvasW = 736f
    val canvasH = 1786f

    BoxWithConstraints(
        modifier = Modifier.fillMaxSize()
    ) {
        val scaleX = maxWidth.value / canvasW
        val scaleY = maxHeight.value / canvasH

        Image(
            bitmap = ImageBitmap.imageResource(R.drawable.room_area_1),
            contentDescription = null,
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.FillBounds,
            filterQuality = FilterQuality.None
        )
        // FRONT-FACING CUPBOARD
        PositionedCupboard(
            imageRes = R.drawable.large_cupboard_facing_forward,// (Google, n.d.c)
            contentDescription = "Forward-facing cupboard",
            shelfId = ShelfId.SHELF_TOP_ROOM1.name,
            x = 500f,
            y = 15f,
            width = 300f,
            height = 390f,
            scaleX = scaleX,
            scaleY = scaleY,
            zIndex = 2f,
            onShelfClick = onShelfClick
        )

        // LEFT-MOST CUPBOARD
        PositionedCupboard(
            imageRes = R.drawable.cupboard_facing_right,
            contentDescription = "Right-facing cupboard",
            shelfId = ShelfId.SHELF_RIGHT_FACING.name,
            x = 0f,
            y = 720f,
            width = 260f,
            height = 500f,
            scaleX = scaleX,
            scaleY = scaleY,
            zIndex = 3f,
            onShelfClick = onShelfClick
        )

        DeskWithComputer(
            x = 20f,
            y = 120f,
            width = 300f,
            scaleX = scaleX,
            scaleY = scaleY,
            onClick = onAddItemClick
        )
    }
}

@Composable
fun PositionedCupboard(
    imageRes: Int,
    contentDescription: String,
    shelfId: String,
    x: Float,
    y: Float,
    width: Float,
    height: Float,
    scaleX: Float,
    scaleY: Float,
    zIndex: Float = 1f,
    onShelfClick: (String) -> Unit
) {
    val bitmap = ImageBitmap.imageResource(imageRes)
    Image(
        bitmap = bitmap,
        contentDescription = contentDescription,
        contentScale = ContentScale.Fit,
        filterQuality = FilterQuality.None,
        modifier = Modifier
            .offset(
                x = (x * scaleX).dp,
                y = (y * scaleY).dp
            )
            .size(
                width = (width * scaleX).dp,
                height = (height * scaleY).dp
            )
            .zIndex(zIndex)
            .clickable {
                onShelfClick(shelfId)
            }
    )
}

@Composable
fun DeskWithComputer(
    x: Float,
    y: Float,
    width: Float,
    scaleX: Float,
    scaleY: Float,
    zIndex: Float = 4f,
    onClick: () -> Unit
) {
    val deskBitmap = ImageBitmap.imageResource(R.drawable.default_desk_with_desktop)

    val deskAspectRatio =
        deskBitmap.width.toFloat() / deskBitmap.height.toFloat()

    val deskWidth = width * scaleX
    val deskHeight = deskWidth / deskAspectRatio

    Box(
        modifier = Modifier
            .offset(
                x = (x * scaleX).dp,
                y = (y * scaleY).dp
            )
            .size(
                width = deskWidth.dp,
                height = deskHeight.dp
            )
            .zIndex(zIndex)
            .clickable(onClick = onClick)
    ) {
        Image(
            bitmap = deskBitmap,
            contentDescription = null,
            contentScale = ContentScale.Fit,
            filterQuality = FilterQuality.None,
            modifier = Modifier.fillMaxSize()
        )
    }
}

@Composable
fun RoomPartTwo(onShelfClick: (String) -> Unit) {
    val canvasW = 736f
    val canvasH = 1786f

    BoxWithConstraints(Modifier.fillMaxSize()) {
        val scaleX = maxWidth.value / canvasW
        val scaleY = maxHeight.value / canvasH

        Image(
            bitmap = ImageBitmap.imageResource(R.drawable.room_area_2),
            contentDescription = null,
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.FillBounds,
            filterQuality = FilterQuality.None
        )

        // RIGHT-MOST METAL CUPBOARD
        PositionedCupboard(
            imageRes = R.drawable.metal_cupboard_facing_left,
            contentDescription = "Right cupboard",
            shelfId = ShelfId.SHELF_RIGHT_ROOM2.name,
            x = 470f,
            y = 720f,
            width = 260f,
            height = 500f,
            scaleX = scaleX,
            scaleY = scaleY,
            zIndex = 3f,
            onShelfClick = onShelfClick
        )
        // FRONT-FACING CUPBOARD
        PositionedCupboard(
            imageRes = R.drawable.large_cupboard_facing_forward,
            contentDescription = "Forward-facing cupboard",
            shelfId = ShelfId.SHELF_TOP_LEFT_ROOM2.name,
            x = 0f,
            y = 15f,
            width = 280f,
            height = 390f,
            scaleX = scaleX,
            scaleY = scaleY,
            zIndex = 3f,
            onShelfClick = onShelfClick
        )
        // FRONT-FACING CUPBOARD
        PositionedCupboard(
            imageRes = R.drawable.large_cupboard_facing_forward,
            contentDescription = "Forward-facing cupboard",
            shelfId = ShelfId.SHELF_TOP_RIGHT_ROOM2.name,
            x = 490f,
            y = 15f,
            width = 300f,
            height = 390f,
            scaleX = scaleX,
            scaleY = scaleY,
            zIndex = 2f,
            onShelfClick = onShelfClick
        )
    }
}

@Composable
fun RoomDashboardScreen(
    onShelfClick: (String) -> Unit = {},
    onAddItemClick: () -> Unit = {}
) {
    val pagerState = rememberPagerState(pageCount = { 2 })
    val scope = rememberCoroutineScope()

    Box(Modifier.fillMaxSize()) {
        HorizontalPager(
            state = pagerState,
            modifier = Modifier.fillMaxSize()
        ) { page ->
            when (page) {
                0 -> RoomPartOne(
                    onShelfClick = onShelfClick,
                    onAddItemClick = onAddItemClick
                )

                1 -> RoomPartTwo(
                    onShelfClick = onShelfClick
                )
            }
        }

        if (pagerState.currentPage < 1) {
            IconButton(
                onClick = {
                    scope.launch {
                        pagerState.animateScrollToPage(
                            page = pagerState.currentPage + 1,
                            animationSpec = tween(
                                durationMillis = 900,
                                easing = FastOutSlowInEasing
                            )
                        )
                    }
                },
                modifier = Modifier
                    .align(Alignment.CenterEnd)
                    .padding(end = 8.dp)
            ) {
                Icon(
                    Icons.Default.ChevronRight,
                    contentDescription = "Next room section"
                )
            }
        }

        if (pagerState.currentPage > 0) {
            IconButton(
                onClick = {
                    scope.launch {
                        pagerState.animateScrollToPage(
                            page = pagerState.currentPage - 1,
                            animationSpec = tween(
                                durationMillis = 900,
                                easing = FastOutSlowInEasing
                            )
                        )
                    }
                },
                modifier = Modifier
                    .align(Alignment.CenterStart)
                    .padding(start = 8.dp)
            ) {
                Icon(
                    Icons.Default.ChevronLeft,
                    contentDescription = "Previous room section"
                )
            }
        }
    }
}

/*Reference List:
Google (n.d.a) Jetpack Compose. Available at: https://developer.android.com/compose(Accessed: 15 September 2026).
Google (n.d.b) Navigate with Compose. Available at: https://developer.android.com/develop/ui/compose/navigation(Accessed: 15 September 2026)
Google (n.d.c) App resources overview. Available at: https://developer.android.com/guide/topics/resources/providing-resources (Accessed: 15 September 2026).
Google (n.d.d) Firebase Documentation. Available at: https://firebase.google.com/docs (Accessed: 15 September 2026).
Android Developers (2024) *Create deep links to app content*. Available at: https://developer.android.com/training/app-links/deep-linking (Accessed: 20 September 2026).
Google (2024) *Material Design 3 — Dialogs*. Available at: https://m3.material.io/components/dialogs/overview (Accessed: 20 September 2026).
 */