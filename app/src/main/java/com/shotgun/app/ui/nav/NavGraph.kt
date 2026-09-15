package com.shotgun.app.ui.nav

import androidx.compose.runtime.Composable
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.shotgun.app.data.GameCatalog
import com.shotgun.app.model.GameType
import com.shotgun.app.state.SessionViewModel
import com.shotgun.app.ui.screens.AlphabetHuntScreen
import com.shotgun.app.ui.screens.CategoriesScreen
import com.shotgun.app.ui.screens.ComingSoonScreen
import com.shotgun.app.ui.screens.GameLibraryScreen
import com.shotgun.app.ui.screens.GuessItScreen
import com.shotgun.app.ui.screens.PlayerSetupScreen
import com.shotgun.app.ui.screens.ScoreboardScreen
import com.shotgun.app.ui.screens.TallyScreen

private object Routes {
    const val LIBRARY = "library"
    const val SETUP = "setup/{gameId}"
    const val GAME = "game/{gameId}"
    const val SCOREBOARD = "scoreboard"

    fun setup(gameId: String) = "setup/$gameId"
    fun game(gameId: String) = "game/$gameId"
}

@Composable
fun ShotgunNavGraph() {
    val navController: NavHostController = rememberNavController()
    val session: SessionViewModel = viewModel()

    NavHost(navController = navController, startDestination = Routes.LIBRARY) {

        composable(Routes.LIBRARY) {
            GameLibraryScreen(onGameSelected = { game ->
                navController.navigate(Routes.setup(game.id))
            })
        }

        composable(Routes.SETUP) { backStackEntry ->
            val gameId = backStackEntry.arguments?.getString("gameId") ?: return@composable
            val game = GameCatalog.byId(gameId) ?: return@composable
            PlayerSetupScreen(
                game = game,
                session = session,
                onStart = { navController.navigate(Routes.game(gameId)) }
            )
        }

        composable(Routes.GAME) { backStackEntry ->
            val gameId = backStackEntry.arguments?.getString("gameId") ?: return@composable
            val game = GameCatalog.byId(gameId) ?: return@composable
            val onFinished = { navController.navigate(Routes.SCOREBOARD) }
            // One screen per GameType; new types get a branch here as they're built.
            when (game.type) {
                GameType.GUESS20 -> GuessItScreen(game = game, session = session, onFinished = onFinished)
                GameType.TALLY -> TallyScreen(game = game, session = session, onFinished = onFinished)
                GameType.SEQUENCE -> AlphabetHuntScreen(game = game, session = session, onFinished = onFinished)
                GameType.ELIMINATION -> CategoriesScreen(game = game, session = session, onFinished = onFinished)
                else -> ComingSoonScreen(
                    game = game,
                    onBack = { navController.popBackStack(Routes.LIBRARY, inclusive = false) }
                )
            }
        }

        composable(Routes.SCOREBOARD) {
            ScoreboardScreen(
                session = session,
                onNextGame = { navController.navigate(Routes.LIBRARY) },
                onNewTrip = {
                    session.newTrip()
                    navController.navigate(Routes.LIBRARY)
                }
            )
        }
    }
}
