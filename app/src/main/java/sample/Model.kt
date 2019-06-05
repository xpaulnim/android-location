package sample

data class Hobby(var title: String)

object Supplier {
    val hobbies = listOf(
        Hobby("Swim"),
        Hobby("Bike"),
        Hobby("Run")
    )
}