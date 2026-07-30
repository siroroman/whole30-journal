package dev.whole30journal.feature.example.presentation.vm

import dev.whole30journal.core.uistate.UiStateAware
import dev.whole30journal.core.uistate.vm.StateFlowViewModel
import dev.whole30journal.feature.example.domain.GetRandomCatFactUseCase
import dev.whole30journal.feature.example.presentation.generated.resources.Res
import dev.whole30journal.feature.example.presentation.generated.resources.example_error_load_failed
import org.jetbrains.compose.resources.getString

class ExampleViewModel(
    private val getRandomCatFact: GetRandomCatFactUseCase
) : StateFlowViewModel<ExampleContract.UiData, ExampleContract.UiAction, ExampleContract.UiEvent, ExampleContract.OutputEvent>(
    initialState = UiStateAware.UiState(uiData = ExampleContract.UiData())
) {

    override suspend fun applyUiAction(uiAction: ExampleContract.UiAction) {
        when (uiAction) {
            is ExampleContract.UiAction.OnAppear -> loadCatFact()
            is ExampleContract.UiAction.OnRefreshClick -> loadCatFact()
        }
    }

    private suspend fun loadCatFact() {
        updateIsLoading(true)

        getRandomCatFact().fold(
            onSuccess = { catFact ->
                updateUiData(isLoading = false) { copy(catFact = catFact) }
            },
            onFailure = {
                val message = getString(Res.string.example_error_load_failed)
                updateUiEvents(isLoading = false) { it + ExampleContract.UiEvent.ShowError(message) }
            }
        )
    }
}
