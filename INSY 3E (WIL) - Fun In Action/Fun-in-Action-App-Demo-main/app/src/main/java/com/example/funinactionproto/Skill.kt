package com.example.funinactionproto

data class Skill(
    val id: String,
    val title: String,
    val techniqueName: String?,
    val description: String?,
    val sections: List<SkillSection>
)

data class SkillSection(
    val title: String,
    val points: List<String>
)

