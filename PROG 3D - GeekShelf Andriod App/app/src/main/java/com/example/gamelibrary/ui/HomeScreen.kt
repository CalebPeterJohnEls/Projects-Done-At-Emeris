package com.example.gamelibrary.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.gamelibrary.R
import com.example.gamelibrary.ShelfDetailScreen
import androidx.navigation.compose.navigation
import com.example.gamelibrary.AdditionalInfoScreen
import com.example.gamelibrary.ItemDetailScreen
import com.example.gamelibrary.ResultsFormScreen
import com.example.gamelibrary.RoomDashboardScreen
import com.example.gamelibrary.SearchFormScreen
import com.example.gamelibrary.model.MediaSearchViewModel

@Composable
fun HomeScreen() {
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
