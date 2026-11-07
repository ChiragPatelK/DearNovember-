package com.dearnovember.app.domain

import com.dearnovember.app.data.*
import java.time.DayOfWeek

data class WorkoutDay(val title: String, val note: String, val exercises: List<String>)

object WorkoutPlan {
    private val upper = listOf("Incline push-up", "Push-up", "Dumbbell shoulder press", "Dumbbell bent-over row", "Dumbbell biceps curl", "Chair triceps dip", "Plank")
    private val lower = listOf("Bodyweight squat", "Goblet squat (5 kg)", "Glute bridge", "Reverse lunge", "Calf raise", "Dead bug", "Plank")
    fun forDay(d: DayOfWeek): WorkoutDay = when (d) {
        DayOfWeek.MONDAY, DayOfWeek.THURSDAY -> WorkoutDay("Upper body", "Control every rep. Stop a set when form slips, not when you are exhausted.", upper)
        DayOfWeek.TUESDAY, DayOfWeek.FRIDAY -> WorkoutDay("Lower body and core", "Slow and steady. Knees track over toes. Stop if you feel sharp pain.", lower)
        DayOfWeek.WEDNESDAY -> WorkoutDay("Walking and mobility", "An easy pace you could chat at.", listOf("Brisk walk (minutes)", "Mobility flow (minutes)"))
        DayOfWeek.SATURDAY -> WorkoutDay("Light full body and mobility", "Keep it light. Fewer sets are fine.", listOf("Incline push-up", "Bodyweight squat", "Glute bridge", "Dumbbell bent-over row", "Plank", "Mobility flow (minutes)"))
        DayOfWeek.SUNDAY -> WorkoutDay("Recovery and stretching", "Recovery is part of training. Move gently.", listOf("Stretching (minutes)", "Brisk walk (minutes)"))
    }
}

object Seeds {
    val subjects = listOf(
        "Machine Learning" to "Practical", "LLM" to "Practical", "AI and Applications" to "Theory",
        "Computer Networks" to "Cybersecurity elective", "Information and Network Security" to "Cybersecurity elective",
        "System Design Methodology" to "Theory",
    )
    // title, subject, start, end
    val assignments = listOf(
        Quad("SDM Assignment 1", "System Design Methodology", "2026-11-07", "2026-11-10"),
        Quad("SDM Assignment 2", "System Design Methodology", "2026-11-11", "2026-11-14"),
        Quad("LLM Practical Assignment 1", "LLM", "2026-11-15", "2026-11-18"),
        Quad("LLM Practical Assignment 2", "LLM", "2026-11-19", "2026-11-22"),
    )
    const val ASSIGNMENT_DEADLINE = "2026-11-30"

    val exercises = listOf(
        Exercise(name = "Incline push-up", instructions = "Hands on a sturdy table or sofa edge, body in one line. Lower your chest toward the edge, then press away.", defaultSets = 3, defaultReps = 8, restSeconds = 60, easierVariation = "Wall push-up"),
        Exercise(name = "Push-up", instructions = "Hands under shoulders, body in one line. Lower slowly for 2 to 3 seconds, press up. Stop the set when form breaks.", defaultSets = 3, defaultReps = 3, restSeconds = 90, easierVariation = "Knee push-up or incline push-up"),
        Exercise(name = "Dumbbell shoulder press", instructions = "Stand or sit tall with a 5 kg dumbbell in each hand at shoulder height. Press overhead without arching your back.", defaultSets = 3, defaultReps = 10, restSeconds = 60, easierVariation = "Press one arm at a time"),
        Exercise(name = "Dumbbell bent-over row", instructions = "Hinge at the hips with a flat back. Pull the dumbbells toward your hips, squeeze your shoulder blades, lower slowly.", defaultSets = 3, defaultReps = 10, restSeconds = 60, easierVariation = "One arm at a time with a hand on a chair"),
        Exercise(name = "Dumbbell biceps curl", instructions = "Elbows by your sides. Curl up without swinging, lower for 2 seconds.", defaultSets = 3, defaultReps = 12, restSeconds = 45, easierVariation = "Alternate arms"),
        Exercise(name = "Chair triceps dip", instructions = "Hands on a stable chair edge, hips just off the seat. Bend elbows to about 90 degrees, press up.", defaultSets = 3, defaultReps = 8, restSeconds = 60, easierVariation = "Bend your knees and keep feet close"),
        Exercise(name = "Plank", instructions = "Forearms down, body in one line, belly gently braced. Reps here means seconds.", defaultSets = 3, defaultReps = 20, restSeconds = 45, easierVariation = "Plank from your knees"),
        Exercise(name = "Bodyweight squat", instructions = "Feet shoulder-width, sit back and down, chest up, heels grounded, stand tall.", defaultSets = 3, defaultReps = 12, restSeconds = 45, easierVariation = "Squat to a chair"),
        Exercise(name = "Goblet squat (5 kg)", instructions = "Hold one dumbbell at your chest and squat with an upright torso.", defaultSets = 3, defaultReps = 10, restSeconds = 60, easierVariation = "Bodyweight squat"),
        Exercise(name = "Glute bridge", instructions = "Lie on your back, knees bent. Push through your heels, lift hips, pause one second, lower.", defaultSets = 3, defaultReps = 12, restSeconds = 45, easierVariation = "Smaller range, slower pace"),
        Exercise(name = "Reverse lunge", instructions = "Step back, lower the back knee toward the floor, push through the front heel. Reps are per leg.", defaultSets = 3, defaultReps = 8, restSeconds = 60, easierVariation = "Hold a wall for balance"),
        Exercise(name = "Calf raise", instructions = "Rise onto your toes, pause, lower slowly.", defaultSets = 3, defaultReps = 15, restSeconds = 30, easierVariation = "Hold a wall"),
        Exercise(name = "Dead bug", instructions = "On your back, arms up, knees at 90 degrees. Lower opposite arm and leg slowly without arching your back.", defaultSets = 3, defaultReps = 8, restSeconds = 45, easierVariation = "Move only your legs"),
        Exercise(name = "Brisk walk (minutes)", instructions = "Walk at a pace where you can talk but feel warmed up. Reps here means minutes.", defaultSets = 1, defaultReps = 30, restSeconds = 0, easierVariation = "Slow walk"),
        Exercise(name = "Mobility flow (minutes)", instructions = "Neck, shoulder, hip and spine circles plus cat-cow. Reps here means minutes.", defaultSets = 1, defaultReps = 10, restSeconds = 0),
        Exercise(name = "Stretching (minutes)", instructions = "Hold each stretch 20 to 30 seconds, breathing slowly. Never force a stretch. Reps here means minutes.", defaultSets = 1, defaultReps = 15, restSeconds = 0),
    )

    // Protein values are rough estimates and vary by brand and portion.
    val foods = listOf(
        Food(name = "Milk", servingLabel = "1 glass (250 ml)", proteinGramsEstimate = 8.0),
        Food(name = "Dal (cooked)", servingLabel = "1 katori", proteinGramsEstimate = 7.0),
        Food(name = "Soya chunks", servingLabel = "30 g dry", proteinGramsEstimate = 15.0),
        Food(name = "Banana", servingLabel = "1 medium", proteinGramsEstimate = 1.0),
        Food(name = "Whey protein", servingLabel = "1 scoop", proteinGramsEstimate = 24.0),
        Food(name = "Rice (cooked)", servingLabel = "1 katori", proteinGramsEstimate = 4.0),
        Food(name = "Roti", servingLabel = "1 medium", proteinGramsEstimate = 3.0),
        Food(name = "Paneer", servingLabel = "50 g", proteinGramsEstimate = 9.0),
        Food(name = "Curd", servingLabel = "1 katori", proteinGramsEstimate = 5.0),
        Food(name = "Chana (cooked)", servingLabel = "1 katori", proteinGramsEstimate = 8.0),
        Food(name = "Peanuts", servingLabel = "30 g", proteinGramsEstimate = 8.0),
    )

    val reminders = listOf(
        Reminder("wake", "Wake-up (follows your schedule)", false, 8, 0),
        Reminder("study", "Study session", false, 16, 0),
        Reminder("workout", "Workout", false, 18, 0),
        Reminder("meditation", "Meditation", false, 20, 30),
        Reminder("speaking", "Speaking practice", false, 19, 30),
        Reminder("journal", "Journaling", false, 22, 0),
        Reminder("bedtime", "Bedtime", false, 23, 0),
    )
    val reminderText = mapOf(
        "wake" to "Good morning. Time to start the day.", "study" to "A focused study block is waiting.",
        "workout" to "Today's workout is ready when you are.", "meditation" to "Take 5 to 10 quiet minutes.",
        "speaking" to "Time for your speaking practice.", "journal" to "A few honest lines in your journal?",
        "bedtime" to "Start winding down for sleep.",
    )
}

data class Quad(val title: String, val subject: String, val start: String, val end: String)

object Prompts {
    val missions = listOf(
        "Spend 10 minutes without your phone.", "Avoid comparing your progress with someone else's.",
        "Complete a task without seeking validation.", "Accept something you cannot control.",
        "Practise saying no respectfully.", "Notice one worry and ask: fact or assumption?",
        "Do one thing you've been avoiding, for five minutes.", "Sit alone with your thoughts for five minutes.",
        "Write down one thing you did well, without a 'but'.", "Make one decision without asking for opinions.",
    )
    fun mission(day: Int) = missions[(day - 1).mod(missions.size)]

    val topics = listOf(
        "Describe your perfect morning.", "Explain how the internet works to a ten-year-old.", "Talk about a skill you want to learn.",
        "Describe your favourite place.", "What makes a good friend?", "Explain a project you are proud of.",
        "Describe a challenge you overcame.", "What would you change about your city?", "Talk about a book, film or game you love.",
        "Explain how to make your favourite dish.", "What does discipline mean to you?", "Describe a person who inspires you.",
        "Talk about your daily routine.", "What is one habit you are building?", "Explain a topic from your course simply.",
        "Describe your dream workspace.", "What is the best advice you've received?", "Talk about a mistake you learned from.",
        "What would you tell your younger self?", "Describe a festival you enjoy.", "Explain why sleep matters.",
        "Talk about a place you'd like to visit.", "What makes you feel calm?", "Describe how a computer network works.",
        "Talk about something you're grateful for.", "How do you handle a bad day?", "Describe a hobby to a beginner.",
        "What does confidence look like to you?", "Explain a rule of chess.", "Describe your goals for next year.",
    )

    data class Word(val word: String, val meaning: String, val example: String)
    val words = listOf(
        Word("resilient", "able to recover quickly from difficulty", "She stayed resilient after the setback."),
        Word("articulate", "able to express ideas clearly", "He gave an articulate answer."),
        Word("deliberate", "done carefully and on purpose", "Take a deliberate breath before you speak."),
        Word("candid", "honest and direct", "I'll be candid: I need more time."),
        Word("diligent", "careful and hard-working", "A diligent student reviews every day."),
        Word("pragmatic", "practical rather than idealistic", "A pragmatic plan beats a perfect one."),
        Word("concise", "short but complete", "Keep your summary concise."),
        Word("nuance", "a subtle difference", "There is nuance in this issue."),
        Word("ambiguous", "open to more than one meaning", "The question was ambiguous."),
        Word("coherent", "logical and consistent", "Write a coherent paragraph."),
        Word("tenacity", "determined persistence", "Her tenacity paid off."),
        Word("composure", "calm self-control", "He kept his composure."),
        Word("perspective", "a particular way of viewing things", "Try another perspective."),
        Word("meticulous", "very careful about details", "A meticulous review caught the bug."),
        Word("eloquent", "fluent and persuasive in speech", "An eloquent speaker."),
        Word("foster", "to encourage the development of", "Foster good habits."),
        Word("prioritise", "to treat as most important", "Prioritise your hardest task."),
        Word("sustain", "to keep going over time", "Sustain your effort."),
        Word("clarity", "the quality of being clear", "Speak with clarity."),
        Word("assertive", "confident and direct, without aggression", "Be assertive, not rude."),
        Word("introspect", "to examine your own thoughts", "Introspect before you react."),
        Word("endeavour", "a serious attempt", "A worthwhile endeavour."),
        Word("integrity", "doing what you said you would", "Integrity means keeping promises."),
        Word("fulfil", "to carry out or achieve", "Fulfil your promise."),
    )
    fun word(day: Int) = words[(day - 1).mod(words.size)]

    val cubeStages = listOf(
        "Learn the beginner method", "Solve the white cross", "Solve first-layer corners", "Solve the middle layer",
        "Learn the last layer", "Complete full solves independently", "Optional: introduce basic CFOP",
    )
}
