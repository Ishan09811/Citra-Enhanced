sealed class DialogEvent {
    object None : DialogEvent()
    data class ShowErrorDialog(val message: String) : DialogEvent()
}
