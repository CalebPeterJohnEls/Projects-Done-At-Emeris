package com.example.funinactionproto

import android.content.Context
import org.json.JSONObject

object SkillRepository {

    fun getSkill(
        context: Context,
        skillId: String
    ): Skill? {

        val jsonString = context.assets
            .open("skills.json")
            .bufferedReader()
            .use { it.readText() }

        val jsonObject = JSONObject(jsonString)
        val skillsArray = jsonObject.getJSONArray("skills")

        for (i in 0 until skillsArray.length()) {

            val skillObject = skillsArray.getJSONObject(i)

            if (skillObject.getString("id") == skillId) {

                val sections = mutableListOf<SkillSection>()

                val sectionsArray =
                    skillObject.getJSONArray("sections")

                for (j in 0 until sectionsArray.length()) {

                    val sectionObject =
                        sectionsArray.getJSONObject(j)

                    val points = mutableListOf<String>()

                    val pointsArray =
                        sectionObject.getJSONArray("points")

                    for (k in 0 until pointsArray.length()) {
                        points.add(
                            pointsArray.getString(k)
                        )
                    }

                    sections.add(
                        SkillSection(
                            title = sectionObject.getString("title"),
                            points = points
                        )
                    )
                }

                return Skill(
                    id = skillObject.getString("id"),
                    title = skillObject.getString("title"),
                    techniqueName =
                        if (skillObject.has("technique_name"))
                            skillObject.getString("technique_name")
                        else
                            null,
                    description =
                        if (skillObject.has("description"))
                            skillObject.getString("description")
                        else
                            null,
                    sections = sections
                )
            }
        }

        return null
    }
}

