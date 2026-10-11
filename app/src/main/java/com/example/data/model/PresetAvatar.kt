package com.example.data.model

import com.example.R

data class PresetAvatar(
    val id: String,
    val name: String,
    val role: String,
    val drawableRes: Int,
    val defaultGreeting: String,
    val defaultTone: String,
    val tags: List<String>
) {
    companion object {
        val ALL = listOf(
            PresetAvatar(
                id = "robin",
                name = "Rouge-gorge & Pomme",
                role = "Mascotte Nature & Chanteur",
                drawableRes = R.drawable.avatar_robin,
                defaultGreeting = "Cui-cui ! Une douce mélodie au fil de l'eau pour égayer votre journée.",
                defaultTone = "Doux & Apaisant",
                tags = listOf("Mascotte", "Nature", "Oiseau")
            ),
            PresetAvatar(
                id = "anchor",
                name = "Lucas",
                role = "Présentateur Journal Télévisé",
                drawableRes = R.drawable.avatar_anchor,
                defaultGreeting = "Bonsoir à tous ! En direct de notre studio, voici les actualités marquantes de la journée.",
                defaultTone = "Sérieux & Pro",
                tags = listOf("Studio", "Info", "Élégant")
            ),
            PresetAvatar(
                id = "cyber",
                name = "Alex Cyber",
                role = "Voyageur de l'an 2099",
                drawableRes = R.drawable.avatar_cyber,
                defaultGreeting = "Connexion établie ! Le futur est déjà là, et votre imagination façonne le monde de demain.",
                defaultTone = "Futuriste & Énergique",
                tags = listOf("Sci-Fi", "Cyberpunk", "Techno")
            ),
            PresetAvatar(
                id = "creator",
                name = "Emma",
                role = "Créatrice de Contenu & Vlogueuse",
                drawableRes = R.drawable.avatar_creator,
                defaultGreeting = "Salut tout le monde ! Bienvenue dans cette nouvelle vidéo, n'oubliez pas de liker et vous abonner !",
                defaultTone = "Joyeux & Dynamique",
                tags = listOf("Vlog", "Souriante", "Populaire")
            ),
            PresetAvatar(
                id = "scholar",
                name = "Professeur Henri",
                role = "Historien & Scientifique",
                drawableRes = R.drawable.avatar_scholar,
                defaultGreeting = "Saviez-vous que la curiosité est la véritable clé de toutes les grandes inventions humaines ?",
                defaultTone = "Sage & Narrateur",
                tags = listOf("Savoir", "Chaleureux", "Académie")
            )
        )
    }
}
