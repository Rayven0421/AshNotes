package com.rayven0421.ashnotes.navigation

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.List
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material.icons.outlined.Wallet
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.toRoute
import com.rayven0421.ashnotes.R
import com.rayven0421.ashnotes.core.designsystem.AshNotesTheme
import com.rayven0421.ashnotes.feature.authentication.LoginScreen
import com.rayven0421.ashnotes.feature.authentication.RegisterScreen
import com.rayven0421.ashnotes.feature.budget.BudgetScreen
import com.rayven0421.ashnotes.feature.budget.BudgetViewModel
import com.rayven0421.ashnotes.feature.editor.EditorScreen
import com.rayven0421.ashnotes.feature.editor.EditorViewModel
import com.rayven0421.ashnotes.feature.home.HomeScreen
import com.rayven0421.ashnotes.feature.home.HomeViewModel
import com.rayven0421.ashnotes.feature.notes.NotesScreen
import com.rayven0421.ashnotes.feature.notes.NotesViewModel
import com.rayven0421.ashnotes.feature.settings.SettingsScreen

sealed class TopLevelDestination(
    val route: Screen,
    val titleResId: Int,
    val icon: ImageVector
) {
    data object Home : TopLevelDestination(Screen.Home, R.string.nav_home, Icons.Outlined.Home)
    data object Notes : TopLevelDestination(Screen.Notes, R.string.nav_notes, Icons.Outlined.List)
    data object Budget : TopLevelDestination(Screen.Budget(), R.string.nav_budget, Icons.Outlined.Wallet)
    data object Settings : TopLevelDestination(Screen.Settings, R.string.nav_settings, Icons.Outlined.Settings)
}

@Composable
fun AshNotesNavHost(
    modifier: Modifier = Modifier,
    mainViewModel: MainViewModel = hiltViewModel()
) {
    val authGateState by mainViewModel.authGateState.collectAsStateWithLifecycle()
    val gateState = authGateState

    if (gateState is AuthGateState.Loading) {
        Box(
            modifier = modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
        )
        return
    }

    val isUserOrLocal = (gateState as AuthGateState.Authenticated).isUserOrLocal
    val initialRoute: Screen = if (isUserOrLocal) Screen.Home else Screen.Login

    val navController = rememberNavController()
    val destinations = listOf(
        TopLevelDestination.Home,
        TopLevelDestination.Notes,
        TopLevelDestination.Budget,
        TopLevelDestination.Settings
    )

    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination
    val showBottomBar = destinations.any { currentRoute?.route?.startsWith(it.route::class.qualifiedName ?: "") == true }

    LaunchedEffect(isUserOrLocal) {
        if (!isUserOrLocal) {
            val currentRouteName = navController.currentBackStackEntry?.destination?.route
            if (currentRouteName != Screen.Login::class.qualifiedName && currentRouteName != Screen.Register::class.qualifiedName) {
                navController.navigate(Screen.Login) {
                    popUpTo(0) { inclusive = true }
                }
            }
        }
    }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        containerColor = MaterialTheme.colorScheme.background,
        bottomBar = {
            if (showBottomBar) {
                NavigationBar(
                    containerColor = MaterialTheme.colorScheme.surface,
                    contentColor = MaterialTheme.colorScheme.onSurface
                ) {
                    destinations.forEach { destination ->
                        val selected = currentRoute?.route?.startsWith(destination.route::class.qualifiedName ?: "") == true
                        NavigationBarItem(
                            selected = selected,
                            onClick = {
                                if (!selected) {
                                    navController.navigate(destination.route) {
                                        popUpTo(navController.graph.findStartDestination().id) {
                                            saveState = true
                                        }
                                        launchSingleTop = true
                                        restoreState = true
                                    }
                                }
                            },
                            icon = {
                                Icon(
                                    imageVector = destination.icon,
                                    contentDescription = stringResource(destination.titleResId)
                                )
                            },
                            label = {
                                Text(text = stringResource(destination.titleResId))
                            },
                            colors = NavigationBarItemDefaults.colors(
                                selectedIconColor = AshNotesTheme.colors.ember,
                                selectedTextColor = AshNotesTheme.colors.ember,
                                unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                                unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant,
                                indicatorColor = AshNotesTheme.colors.emberSoft
                            )
                        )
                    }
                }
            }
        }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = initialRoute,
            modifier = Modifier.fillMaxSize()
        ) {
            composable<Screen.Login> {
                LoginScreen(
                    onNavigateToRegister = {
                        navController.navigate(Screen.Register)
                    },
                    onLoginSuccess = {
                        navController.navigate(Screen.Home) {
                            popUpTo(0) { inclusive = true }
                        }
                    },
                    onContinueWithoutAccount = {
                        navController.navigate(Screen.Home) {
                            popUpTo(0) { inclusive = true }
                        }
                    }
                )
            }
            composable<Screen.Register> {
                RegisterScreen(
                    onNavigateToLogin = {
                        navController.navigate(Screen.Login) {
                            popUpTo(Screen.Login) { inclusive = true }
                        }
                    },
                    onRegisterSuccess = {
                        navController.navigate(Screen.Home) {
                            popUpTo(0) { inclusive = true }
                        }
                    },
                    onContinueWithoutAccount = {
                        navController.navigate(Screen.Home) {
                            popUpTo(0) { inclusive = true }
                        }
                    }
                )
            }
            composable<Screen.Home> {
                val viewModel: HomeViewModel = hiltViewModel()
                val uiState by viewModel.uiState.collectAsStateWithLifecycle()
                HomeScreen(
                    uiState = uiState,
                    modifier = Modifier.padding(innerPadding),
                    onNavigateToBudget = {
                        navController.navigate(Screen.Budget()) {
                            popUpTo(navController.graph.findStartDestination().id) {
                                saveState = true
                            }
                            launchSingleTop = true
                            restoreState = true
                        }
                    },
                    onAddExpenseClick = {
                        navController.navigate(Screen.Budget(openAddExpense = true)) {
                            popUpTo(navController.graph.findStartDestination().id) {
                                saveState = true
                            }
                            launchSingleTop = true
                            restoreState = true
                        }
                    },
                    onNoteClick = { noteId ->
                        navController.navigate(Screen.Editor(noteId))
                    },
                    onNewNoteClick = { title ->
                        viewModel.createNewNote(title) { noteId ->
                            navController.navigate(Screen.Editor(noteId))
                        }
                    }
                )
            }
            composable<Screen.Notes> {
                val viewModel: NotesViewModel = hiltViewModel()
                val uiState by viewModel.uiState.collectAsStateWithLifecycle()
                val selectedIds by viewModel.selectedIds.collectAsStateWithLifecycle()
                val sortOption by viewModel.sortOption.collectAsStateWithLifecycle()
                NotesScreen(
                    uiState = uiState,
                    selectedIds = selectedIds,
                    sortOption = sortOption,
                    modifier = Modifier.padding(innerPadding),
                    onSortOptionSelected = { option -> viewModel.setSortOption(option) },
                    onSearchQueryChanged = { query ->
                        viewModel.updateSearchQuery(query)
                    },
                    onNoteClick = { noteId ->
                        navController.navigate(Screen.Editor(noteId))
                    },
                    onNewNoteClick = { title ->
                        viewModel.createNewNote(title) { noteId ->
                            navController.navigate(Screen.Editor(noteId))
                        }
                    },
                    onTogglePin = { id -> viewModel.togglePin(id) },
                    onToggleArchive = { id -> viewModel.toggleArchive(id) },
                    onRename = { id, title -> viewModel.renameNote(id, title) },
                    onDelete = { id -> viewModel.deleteNote(id) },
                    onRestore = { id -> viewModel.restoreNote(id) },
                    onToggleSelectNote = { id -> viewModel.toggleSelectNote(id) },
                    onSelectAllNotes = { allIds -> viewModel.selectAllNotes(allIds) },
                    onClearSelection = { viewModel.clearSelection() },
                    onBulkPinSelected = { isPinned -> viewModel.bulkPinSelected(isPinned) },
                    onBulkArchiveSelected = { isArchived -> viewModel.bulkArchiveSelected(isArchived) },
                    onBulkDeleteSelected = { onDeleted -> viewModel.bulkDeleteSelected(onDeleted) },
                    onBulkRestoreNotes = { ids -> viewModel.bulkRestoreNotes(ids) }
                )
            }
            composable<Screen.Budget> { backStackEntry ->
                val budgetRoute = backStackEntry.toRoute<Screen.Budget>()
                val viewModel: BudgetViewModel = hiltViewModel()
                val uiState by viewModel.uiState.collectAsStateWithLifecycle()
                val showCreateDialog by viewModel.showCreateDialog.collectAsStateWithLifecycle()
                val showEditDialog by viewModel.showEditDialog.collectAsStateWithLifecycle()
                val showExpenseDialog by viewModel.showExpenseDialog.collectAsStateWithLifecycle()
                val editingExpense by viewModel.editingExpense.collectAsStateWithLifecycle()
                val showDeleteConfirmDialog by viewModel.showDeleteConfirmDialog.collectAsStateWithLifecycle()
                val categories by viewModel.categories.collectAsStateWithLifecycle()

                LaunchedEffect(budgetRoute.openAddExpense) {
                    if (budgetRoute.openAddExpense) {
                        viewModel.openAddExpenseDialog()
                    }
                }

                BudgetScreen(
                    uiState = uiState,
                    showCreateDialog = showCreateDialog,
                    showEditDialog = showEditDialog,
                    showExpenseDialog = showExpenseDialog,
                    editingExpense = editingExpense,
                    showDeleteConfirmDialog = showDeleteConfirmDialog,
                    categories = categories,
                    uiEvent = viewModel.uiEvent,
                    modifier = Modifier.padding(innerPadding),
                    onCreateBudgetClick = {
                        viewModel.openCreateBudgetDialog()
                    },
                    onDismissCreateDialog = {
                        viewModel.dismissCreateBudgetDialog()
                    },
                    onConfirmCreateBudget = { name, amountMinor ->
                        viewModel.createBudget(name, amountMinor)
                    },
                    onEditBudgetClick = {
                        viewModel.openEditBudgetDialog()
                    },
                    onDismissEditDialog = {
                        viewModel.dismissEditBudgetDialog()
                    },
                    onConfirmEditBudget = { name, amountMinor ->
                        viewModel.updateBudget(name, amountMinor)
                    },
                    onPageChange = { page ->
                        viewModel.selectPage(page)
                    },
                    onAddExpenseClick = {
                        viewModel.openAddExpenseDialog()
                    },
                    onEditExpenseClick = { expense ->
                        viewModel.openEditExpenseDialog(expense)
                    },
                    onDismissExpenseDialog = {
                        viewModel.dismissExpenseDialog()
                    },
                    onSaveExpense = { categoryId, amountMinor, date, note ->
                        viewModel.saveExpense(categoryId, amountMinor, date, note)
                    },
                    onRequestDeleteExpense = { expense ->
                        viewModel.requestDeleteExpense(expense)
                    },
                    onDismissDeleteConfirmDialog = {
                        viewModel.dismissDeleteConfirmDialog()
                    },
                    onConfirmDeleteExpense = {
                        viewModel.confirmDeleteExpense()
                    },
                    onUndoDeleteExpense = { expense ->
                        viewModel.undoDeleteExpense(expense)
                    }
                )
            }
            composable<Screen.Settings> {
                SettingsScreen(
                    onNavigateToLogin = {
                        navController.navigate(Screen.Login) {
                            popUpTo(0) { inclusive = true }
                        }
                    },
                    modifier = Modifier.padding(innerPadding)
                )
            }
            composable<Screen.Editor> {
                val viewModel: EditorViewModel = hiltViewModel()
                val uiState by viewModel.uiState.collectAsStateWithLifecycle()
                EditorScreen(
                    uiState = uiState,
                    onBackClick = { navController.popBackStack() },
                    onTitleChanged = { title -> viewModel.updateTitle(title) },
                    onParagraphTextChanged = { blockId, text -> viewModel.updateParagraphText(blockId, text) },
                    onHeadingTextChanged = { blockId, text -> viewModel.updateHeadingText(blockId, text) },
                    onChecklistTextChanged = { blockId, text -> viewModel.updateChecklistText(blockId, text) },
                    onToggleChecklistChecked = { blockId -> viewModel.toggleChecklistChecked(blockId) },
                    onSplitChecklist = { blockId, cursorIndex -> viewModel.splitChecklistBlock(blockId, cursorIndex) },
                    onBackspaceInChecklist = { blockId, cursorIndex -> viewModel.handleBackspaceInChecklist(blockId, cursorIndex) },
                    onPasteTextInChecklist = { blockId, text, cursorIndex -> viewModel.pasteTextIntoChecklist(blockId, text, cursorIndex) },
                    onSplitParagraph = { blockId, cursorIndex -> viewModel.splitParagraphBlock(blockId, cursorIndex) },
                    onSplitHeading = { blockId, cursorIndex -> viewModel.splitHeadingBlock(blockId, cursorIndex) },
                    onPasteTextInParagraph = { blockId, text, cursorIndex -> viewModel.pasteTextIntoParagraph(blockId, text, cursorIndex) },
                    onPasteTextInHeading = { blockId, text, cursorIndex -> viewModel.pasteTextIntoHeading(blockId, text, cursorIndex) },
                    onBackspaceInParagraph = { blockId, cursorIndex -> viewModel.handleBackspaceInParagraph(blockId, cursorIndex) },
                    onDeleteBlock = { blockId -> viewModel.deleteBlock(blockId) },
                    onInsertBlockBelow = { type -> viewModel.insertBlockBelow(type) },
                    onInsertDivider = { viewModel.insertDivider() },
                    onBlockFocused = { blockId, cursorIndex -> viewModel.onBlockFocused(blockId, cursorIndex) },
                    onAppendParagraph = { viewModel.appendParagraphIfNeeded() },
                    onTapBelowBlocks = { viewModel.onTapBelowBlocks() },
                    onUndoClick = { viewModel.undo() },
                    onRedoClick = { viewModel.redo() },
                    onFlushSave = { viewModel.flushSave() },
                    onClearFocusRequest = { token -> viewModel.clearFocusRequest(token) }
                )
            }
        }
    }
}
