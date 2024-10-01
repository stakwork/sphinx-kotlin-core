package chat.sphinx.concepts.connect_manager.model

sealed class RestoreState {
    object RestoringContacts : RestoreState()
    object RestoringMessages : RestoreState()
    object RestoreFinished : RestoreState()
}