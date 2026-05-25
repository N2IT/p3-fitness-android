import SwiftData
import Foundation

struct SeedData {
    // Each tuple: (id, name, muscleGroup, equipment)
    static let exercises: [(Int, String, String, String)] = [
        // CHEST - Barbell (1-4)
        (1,  "Bench Press",              "Chest", "Barbell"),
        (2,  "Incline Bench Press",      "Chest", "Barbell"),
        (3,  "Decline Bench Press",      "Chest", "Barbell"),
        (4,  "Close-Grip Bench Press",   "Chest", "Barbell"),

        // CHEST - Dumbbell (5-9)
        (5,  "Dumbbell Fly",             "Chest", "Dumbbell"),
        (6,  "Incline Dumbbell Press",   "Chest", "Dumbbell"),
        (7,  "Decline Dumbbell Press",   "Chest", "Dumbbell"),
        (8,  "Dumbbell Pullover",        "Chest", "Dumbbell"),
        (9,  "Chest Press",              "Chest", "Dumbbell"),

        // CHEST - Cable (10-13)
        (10, "Cable Crossover",          "Chest", "Cable"),
        (11, "Low Cable Fly",            "Chest", "Cable"),
        (12, "High Cable Fly",           "Chest", "Cable"),
        (13, "Cable Chest Press",        "Chest", "Cable"),

        // CHEST - Machine (14-16)
        (14, "Chest Press Machine",      "Chest", "Machine"),
        (15, "Pec Deck",                 "Chest", "Machine"),
        (16, "Hammer Strength Chest Press", "Chest", "Machine"),

        // CHEST - Bodyweight (17-21)
        (17, "Push-Up",                  "Chest", "Bodyweight"),
        (18, "Wide Push-Up",             "Chest", "Bodyweight"),
        (19, "Diamond Push-Up",          "Chest", "Bodyweight"),
        (20, "Decline Push-Up",          "Chest", "Bodyweight"),
        (21, "Pike Push-Up",             "Chest", "Bodyweight"),

        // BACK - Barbell (22-26)
        (22, "Deadlift",                 "Back", "Barbell"),
        (23, "Barbell Row",              "Back", "Barbell"),
        (24, "T-Bar Row",                "Back", "Barbell"),
        (25, "Rack Pull",                "Back", "Barbell"),
        (26, "Romanian Deadlift",        "Back", "Barbell"),

        // BACK - Dumbbell (27-29)
        (27, "Dumbbell Row",             "Back", "Dumbbell"),
        (28, "Single-Arm Dumbbell Row",  "Back", "Dumbbell"),
        (29, "Renegade Row",             "Back", "Dumbbell"),

        // BACK - Cable (30-34)
        (30, "Lat Pulldown",             "Back", "Cable"),
        (31, "Cable Row",                "Back", "Cable"),
        (32, "Straight-Arm Pulldown",    "Back", "Cable"),
        (33, "Face Pull",                "Back", "Cable"),
        (34, "Cable Pullover",           "Back", "Cable"),

        // BACK - Machine (35-37)
        (35, "Machine Row",              "Back", "Machine"),
        (36, "Hammer Strength Row",      "Back", "Machine"),
        (37, "Assisted Pull-Up Machine", "Back", "Machine"),

        // BACK - Bodyweight (38-41)
        (38, "Pull-Up",                  "Back", "Bodyweight"),
        (39, "Chin-Up",                  "Back", "Bodyweight"),
        (40, "Inverted Row",             "Back", "Bodyweight"),
        (41, "Superman",                 "Back", "Bodyweight"),

        // SHOULDERS - Barbell (42-45)
        (42, "Overhead Press",           "Shoulders", "Barbell"),
        (43, "Behind-the-Neck Press",    "Shoulders", "Barbell"),
        (44, "Barbell Shrug",            "Shoulders", "Barbell"),
        (45, "Upright Row",              "Shoulders", "Barbell"),

        // SHOULDERS - Dumbbell (46-50)
        (46, "Dumbbell Lateral Raise",   "Shoulders", "Dumbbell"),
        (47, "Dumbbell Front Raise",     "Shoulders", "Dumbbell"),
        (48, "Arnold Press",             "Shoulders", "Dumbbell"),
        (49, "Dumbbell Shoulder Press",  "Shoulders", "Dumbbell"),
        (50, "Dumbbell Rear Delt Fly",   "Shoulders", "Dumbbell"),

        // SHOULDERS - Cable (51-53)
        (51, "Cable Lateral Raise",      "Shoulders", "Cable"),
        (52, "Cable Front Raise",        "Shoulders", "Cable"),
        (53, "Cable Rear Delt Fly",      "Shoulders", "Cable"),

        // SHOULDERS - Machine (54-55)
        (54, "Machine Shoulder Press",   "Shoulders", "Machine"),
        (55, "Machine Lateral Raise",    "Shoulders", "Machine"),

        // ARMS - Barbell (56-59)
        (56, "Barbell Curl",             "Arms", "Barbell"),
        (57, "Skull Crusher",            "Arms", "Barbell"),
        (58, "EZ-Bar Curl",              "Arms", "Barbell"),
        (59, "Close-Grip Press",         "Arms", "Barbell"),

        // ARMS - Dumbbell (60-66)
        (60, "Dumbbell Curl",            "Arms", "Dumbbell"),
        (61, "Hammer Curl",              "Arms", "Dumbbell"),
        (62, "Concentration Curl",       "Arms", "Dumbbell"),
        (63, "Incline Dumbbell Curl",    "Arms", "Dumbbell"),
        (64, "Dumbbell Tricep Kickback", "Arms", "Dumbbell"),
        (65, "Overhead Tricep Extension","Arms", "Dumbbell"),
        (66, "Dumbbell Preacher Curl",   "Arms", "Dumbbell"),

        // ARMS - Cable (67-71)
        (67, "Cable Curl",               "Arms", "Cable"),
        (68, "Tricep Pushdown",          "Arms", "Cable"),
        (69, "Overhead Cable Extension", "Arms", "Cable"),
        (70, "Cable Hammer Curl",        "Arms", "Cable"),
        (71, "Cable Preacher Curl",      "Arms", "Cable"),

        // ARMS - Machine (72-73)
        (72, "Machine Preacher Curl",    "Arms", "Machine"),
        (73, "Machine Tricep Extension", "Arms", "Machine"),

        // ARMS - Bodyweight (74-76)
        (74, "Tricep Dip",               "Arms", "Bodyweight"),
        (75, "Close-Grip Push-Up",       "Arms", "Bodyweight"),
        // Diamond Push-Up already appears in Chest (id=19); add a second reference here
        (76, "Parallel Bar Dip",         "Arms", "Bodyweight"),

        // LEGS - Barbell (77-83)
        (77, "Squat",                    "Legs", "Barbell"),
        (78, "Front Squat",              "Legs", "Barbell"),
        (79, "Barbell Romanian Deadlift","Legs", "Barbell"),
        (80, "Barbell Lunge",            "Legs", "Barbell"),
        (81, "Hip Thrust",               "Legs", "Barbell"),
        (82, "Good Morning",             "Legs", "Barbell"),
        (83, "Bulgarian Split Squat",    "Legs", "Barbell"),

        // LEGS - Dumbbell (84-88)
        (84, "Dumbbell Squat",           "Legs", "Dumbbell"),
        (85, "Dumbbell Lunge",           "Legs", "Dumbbell"),
        (86, "Dumbbell Step-Up",         "Legs", "Dumbbell"),
        (87, "Dumbbell Romanian Deadlift","Legs", "Dumbbell"),
        (88, "Goblet Squat",             "Legs", "Dumbbell"),

        // LEGS - Cable (89-91)
        (89, "Cable Pull-Through",       "Legs", "Cable"),
        (90, "Cable Kickback",           "Legs", "Cable"),
        (91, "Cable Squat",              "Legs", "Cable"),

        // LEGS - Machine (92-99)
        (92, "Leg Press",                "Legs", "Machine"),
        (93, "Leg Curl",                 "Legs", "Machine"),
        (94, "Leg Extension",            "Legs", "Machine"),
        (95, "Calf Raise",               "Legs", "Machine"),
        (96, "Hack Squat",               "Legs", "Machine"),
        (97, "Hip Abductor",             "Legs", "Machine"),
        (98, "Hip Adductor",             "Legs", "Machine"),
        (99, "Seated Calf Raise",        "Legs", "Machine"),

        // LEGS - Bodyweight (100-106)
        (100, "Bodyweight Squat",        "Legs", "Bodyweight"),
        (101, "Jump Squat",              "Legs", "Bodyweight"),
        (102, "Reverse Lunge",           "Legs", "Bodyweight"),
        (103, "Walking Lunge",           "Legs", "Bodyweight"),
        (104, "Wall Sit",                "Legs", "Bodyweight"),
        (105, "Glute Bridge",            "Legs", "Bodyweight"),
        (106, "Donkey Kick",             "Legs", "Bodyweight"),

        // CORE - Bodyweight (107-119)
        (107, "Plank",                   "Core", "Bodyweight"),
        (108, "Side Plank",              "Core", "Bodyweight"),
        (109, "Crunch",                  "Core", "Bodyweight"),
        (110, "Sit-Up",                  "Core", "Bodyweight"),
        (111, "Leg Raise",               "Core", "Bodyweight"),
        (112, "Mountain Climber",        "Core", "Bodyweight"),
        (113, "Russian Twist",           "Core", "Bodyweight"),
        (114, "Bicycle Crunch",          "Core", "Bodyweight"),
        (115, "Dead Bug",                "Core", "Bodyweight"),
        (116, "V-Up",                    "Core", "Bodyweight"),
        (117, "Hollow Body Hold",        "Core", "Bodyweight"),
        (118, "Ab Wheel Rollout",        "Core", "Bodyweight"),
        (119, "Hanging Leg Raise",       "Core", "Bodyweight"),

        // CORE - Cable (120-123)
        (120, "Cable Crunch",            "Core", "Cable"),
        (121, "Pallof Press",            "Core", "Cable"),
        (122, "Wood Chop",               "Core", "Cable"),
        (123, "Cable Oblique Twist",     "Core", "Cable"),

        // CORE - Machine (124-125)
        (124, "Ab Machine",              "Core", "Machine"),
        (125, "Back Extension Machine",  "Core", "Machine"),

        // OTHER - Bodyweight (126-129)
        (126, "Burpee",                  "Other", "Bodyweight"),
        (127, "Jump Rope",               "Other", "Bodyweight"),
        (128, "Box Jump",                "Other", "Bodyweight"),
        (129, "Battle Ropes",            "Other", "Bodyweight"),
    ]

    @MainActor
    static func seedIfNeeded(context: ModelContext) async {
        // Check if exercises already exist
        let descriptor = FetchDescriptor<Exercise>(
            predicate: #Predicate { !$0.isCustom }
        )
        guard let count = try? context.fetchCount(descriptor), count == 0 else {
            return
        }

        // Insert all seed exercises
        for (id, name, muscleGroup, equipment) in exercises {
            let exercise = Exercise(id: id, name: name, muscleGroup: muscleGroup, equipment: equipment, isCustom: false)
            context.insert(exercise)
        }

        do {
            try context.save()
        } catch {
            print("SeedData: Failed to save exercises: \(error)")
        }
    }
}
