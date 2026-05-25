package com.fittrack.data

import com.fittrack.data.entity.Exercise

object ExerciseSeedData {
    val exercises = listOf(
        // Chest - Barbell
        Exercise(name = "Barbell Bench Press", muscleGroup = "Chest", equipment = "Barbell"),
        Exercise(name = "Incline Barbell Bench Press", muscleGroup = "Chest", equipment = "Barbell"),
        Exercise(name = "Decline Barbell Bench Press", muscleGroup = "Chest", equipment = "Barbell"),
        Exercise(name = "Close-Grip Bench Press", muscleGroup = "Chest", equipment = "Barbell"),
        // Chest - Dumbbell
        Exercise(name = "Dumbbell Bench Press", muscleGroup = "Chest", equipment = "Dumbbell"),
        Exercise(name = "Incline Dumbbell Press", muscleGroup = "Chest", equipment = "Dumbbell"),
        Exercise(name = "Dumbbell Fly", muscleGroup = "Chest", equipment = "Dumbbell"),
        Exercise(name = "Incline Dumbbell Fly", muscleGroup = "Chest", equipment = "Dumbbell"),
        // Chest - Cable/Machine
        Exercise(name = "Cable Crossover", muscleGroup = "Chest", equipment = "Cable"),
        Exercise(name = "Machine Chest Press", muscleGroup = "Chest", equipment = "Machine"),
        Exercise(name = "Pec Deck", muscleGroup = "Chest", equipment = "Machine"),
        // Chest - Bodyweight
        Exercise(name = "Push-Up", muscleGroup = "Chest", equipment = "Bodyweight"),
        Exercise(name = "Dips (Chest)", muscleGroup = "Chest", equipment = "Bodyweight"),

        // Back - Barbell
        Exercise(name = "Barbell Row", muscleGroup = "Back", equipment = "Barbell"),
        Exercise(name = "Pendlay Row", muscleGroup = "Back", equipment = "Barbell"),
        Exercise(name = "Deadlift", muscleGroup = "Back", equipment = "Barbell"),
        Exercise(name = "Rack Pull", muscleGroup = "Back", equipment = "Barbell"),
        Exercise(name = "T-Bar Row", muscleGroup = "Back", equipment = "Barbell"),
        // Back - Dumbbell
        Exercise(name = "Dumbbell Row", muscleGroup = "Back", equipment = "Dumbbell"),
        Exercise(name = "Dumbbell Pullover", muscleGroup = "Back", equipment = "Dumbbell"),
        // Back - Cable
        Exercise(name = "Lat Pulldown", muscleGroup = "Back", equipment = "Cable"),
        Exercise(name = "Seated Cable Row", muscleGroup = "Back", equipment = "Cable"),
        Exercise(name = "Face Pull", muscleGroup = "Back", equipment = "Cable"),
        Exercise(name = "Straight-Arm Pulldown", muscleGroup = "Back", equipment = "Cable"),
        // Back - Bodyweight
        Exercise(name = "Pull-Up", muscleGroup = "Back", equipment = "Bodyweight"),
        Exercise(name = "Chin-Up", muscleGroup = "Back", equipment = "Bodyweight"),

        // Shoulders - Barbell
        Exercise(name = "Overhead Press", muscleGroup = "Shoulders", equipment = "Barbell"),
        Exercise(name = "Push Press", muscleGroup = "Shoulders", equipment = "Barbell"),
        Exercise(name = "Barbell Upright Row", muscleGroup = "Shoulders", equipment = "Barbell"),
        // Shoulders - Dumbbell
        Exercise(name = "Dumbbell Shoulder Press", muscleGroup = "Shoulders", equipment = "Dumbbell"),
        Exercise(name = "Arnold Press", muscleGroup = "Shoulders", equipment = "Dumbbell"),
        Exercise(name = "Lateral Raise", muscleGroup = "Shoulders", equipment = "Dumbbell"),
        Exercise(name = "Front Raise", muscleGroup = "Shoulders", equipment = "Dumbbell"),
        Exercise(name = "Reverse Fly", muscleGroup = "Shoulders", equipment = "Dumbbell"),
        // Shoulders - Cable
        Exercise(name = "Cable Lateral Raise", muscleGroup = "Shoulders", equipment = "Cable"),

        // Arms - Biceps
        Exercise(name = "Barbell Curl", muscleGroup = "Arms", equipment = "Barbell"),
        Exercise(name = "EZ-Bar Curl", muscleGroup = "Arms", equipment = "Barbell"),
        Exercise(name = "Dumbbell Curl", muscleGroup = "Arms", equipment = "Dumbbell"),
        Exercise(name = "Hammer Curl", muscleGroup = "Arms", equipment = "Dumbbell"),
        Exercise(name = "Incline Dumbbell Curl", muscleGroup = "Arms", equipment = "Dumbbell"),
        Exercise(name = "Concentration Curl", muscleGroup = "Arms", equipment = "Dumbbell"),
        Exercise(name = "Cable Curl", muscleGroup = "Arms", equipment = "Cable"),
        Exercise(name = "Preacher Curl", muscleGroup = "Arms", equipment = "Machine"),
        // Arms - Triceps
        Exercise(name = "Skull Crusher", muscleGroup = "Arms", equipment = "Barbell"),
        Exercise(name = "Overhead Tricep Extension", muscleGroup = "Arms", equipment = "Dumbbell"),
        Exercise(name = "Dumbbell Kickback", muscleGroup = "Arms", equipment = "Dumbbell"),
        Exercise(name = "Tricep Pushdown", muscleGroup = "Arms", equipment = "Cable"),
        Exercise(name = "Overhead Cable Extension", muscleGroup = "Arms", equipment = "Cable"),
        Exercise(name = "Dips (Triceps)", muscleGroup = "Arms", equipment = "Bodyweight"),

        // Legs - Barbell
        Exercise(name = "Barbell Squat", muscleGroup = "Legs", equipment = "Barbell"),
        Exercise(name = "Front Squat", muscleGroup = "Legs", equipment = "Barbell"),
        Exercise(name = "Romanian Deadlift", muscleGroup = "Legs", equipment = "Barbell"),
        Exercise(name = "Sumo Deadlift", muscleGroup = "Legs", equipment = "Barbell"),
        Exercise(name = "Hip Thrust", muscleGroup = "Legs", equipment = "Barbell"),
        Exercise(name = "Barbell Lunge", muscleGroup = "Legs", equipment = "Barbell"),
        Exercise(name = "Good Morning", muscleGroup = "Legs", equipment = "Barbell"),
        // Legs - Dumbbell
        Exercise(name = "Goblet Squat", muscleGroup = "Legs", equipment = "Dumbbell"),
        Exercise(name = "Dumbbell Lunge", muscleGroup = "Legs", equipment = "Dumbbell"),
        Exercise(name = "Dumbbell Romanian Deadlift", muscleGroup = "Legs", equipment = "Dumbbell"),
        Exercise(name = "Bulgarian Split Squat", muscleGroup = "Legs", equipment = "Dumbbell"),
        Exercise(name = "Dumbbell Step-Up", muscleGroup = "Legs", equipment = "Dumbbell"),
        // Legs - Machine
        Exercise(name = "Leg Press", muscleGroup = "Legs", equipment = "Machine"),
        Exercise(name = "Leg Extension", muscleGroup = "Legs", equipment = "Machine"),
        Exercise(name = "Leg Curl", muscleGroup = "Legs", equipment = "Machine"),
        Exercise(name = "Hack Squat", muscleGroup = "Legs", equipment = "Machine"),
        Exercise(name = "Calf Raise (Machine)", muscleGroup = "Legs", equipment = "Machine"),
        Exercise(name = "Seated Calf Raise", muscleGroup = "Legs", equipment = "Machine"),
        // Legs - Bodyweight
        Exercise(name = "Bodyweight Squat", muscleGroup = "Legs", equipment = "Bodyweight"),
        Exercise(name = "Walking Lunge", muscleGroup = "Legs", equipment = "Bodyweight"),

        // Core
        Exercise(name = "Plank", muscleGroup = "Core", equipment = "Bodyweight"),
        Exercise(name = "Crunch", muscleGroup = "Core", equipment = "Bodyweight"),
        Exercise(name = "Hanging Leg Raise", muscleGroup = "Core", equipment = "Bodyweight"),
        Exercise(name = "Russian Twist", muscleGroup = "Core", equipment = "Bodyweight"),
        Exercise(name = "Mountain Climber", muscleGroup = "Core", equipment = "Bodyweight"),
        Exercise(name = "Ab Rollout", muscleGroup = "Core", equipment = "Bodyweight"),
        Exercise(name = "Cable Crunch", muscleGroup = "Core", equipment = "Cable"),
        Exercise(name = "Woodchopper", muscleGroup = "Core", equipment = "Cable"),
        Exercise(name = "Decline Sit-Up", muscleGroup = "Core", equipment = "Bodyweight"),
        Exercise(name = "Side Plank", muscleGroup = "Core", equipment = "Bodyweight")
    )
}
