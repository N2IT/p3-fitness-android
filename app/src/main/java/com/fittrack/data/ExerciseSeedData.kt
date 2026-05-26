package com.fittrack.data

import com.fittrack.data.entity.Exercise

object ExerciseSeedData {
    val exercises = listOf(
        // Chest - Barbell
        Exercise(
            name = "Barbell Bench Press", muscleGroup = "Chest", equipment = "Barbell",
            description = "Lie flat on a bench, grip the bar slightly wider than shoulder-width. Lower the bar to mid-chest with elbows at roughly 75°, then press back up. Keep your feet flat on the floor and maintain a slight natural arch in your lower back throughout."
        ),
        Exercise(
            name = "Incline Barbell Bench Press", muscleGroup = "Chest", equipment = "Barbell",
            description = "Set the bench to 30–45°. Grip the bar wider than shoulder-width and lower it to your upper chest. Press up and slightly back toward the rack. The incline angle shifts emphasis to the upper chest and front delts."
        ),
        Exercise(
            name = "Decline Barbell Bench Press", muscleGroup = "Chest", equipment = "Barbell",
            description = "Set the bench to –15 to –30°. Secure your legs and grip the bar wider than shoulders. Lower the bar to your lower chest, then press back up in a slight arc. Targets the lower chest more than flat or incline variations."
        ),
        Exercise(
            name = "Close-Grip Bench Press", muscleGroup = "Chest", equipment = "Barbell",
            description = "Lie flat and grip the bar at shoulder-width (not too narrow). Lower the bar to your chest while keeping elbows tucked close to your body. Press back up. The narrower grip shifts the primary workload from chest to triceps."
        ),
        // Chest - Dumbbell
        Exercise(
            name = "Dumbbell Bench Press", muscleGroup = "Chest", equipment = "Dumbbell",
            description = "Lie flat on a bench, hold dumbbells at chest level with palms facing forward. Press up and slightly inward until arms are fully extended. Lower slowly, allowing a slight stretch at the bottom. Greater range of motion than barbell variation."
        ),
        Exercise(
            name = "Incline Dumbbell Press", muscleGroup = "Chest", equipment = "Dumbbell",
            description = "Set bench to 30–45°. Hold dumbbells at chest level. Press upward, converging slightly at the top. Lower with control. The incline angle emphasizes the upper chest while the dumbbells allow independent arm movement."
        ),
        Exercise(
            name = "Dumbbell Fly", muscleGroup = "Chest", equipment = "Dumbbell",
            description = "Lie flat on a bench, hold dumbbells above chest with a slight bend in your elbows. Lower dumbbells out in a wide arc until you feel a deep chest stretch. Reverse the arc to bring them back together. Keep the elbow angle constant throughout."
        ),
        Exercise(
            name = "Incline Dumbbell Fly", muscleGroup = "Chest", equipment = "Dumbbell",
            description = "Set bench to 30–45°. With a slight elbow bend, lower dumbbells out to the sides until you feel a stretch in your upper chest. Bring them back together in a hugging arc. Targets the upper chest with a long range of motion."
        ),
        // Chest - Cable/Machine
        Exercise(
            name = "Cable Crossover", muscleGroup = "Chest", equipment = "Cable",
            description = "Stand between two cables set high. With a slight bend in your elbows, bring the handles together in front of your chest in a wide hugging arc. Squeeze your chest at the peak. Slowly return to the start. Cable keeps tension constant across the full range."
        ),
        Exercise(
            name = "Machine Chest Press", muscleGroup = "Chest", equipment = "Machine",
            description = "Sit with your back flat against the pad. Grip the handles at chest height and press forward until your arms are fully extended. Return slowly. The machine guides the path, making it ideal for learning the pressing pattern or isolating the chest."
        ),
        Exercise(
            name = "Pec Deck", muscleGroup = "Chest", equipment = "Machine",
            description = "Sit upright and place your forearms on the pads or grip the handles. Bring the pads together in front of your chest, squeezing the pecs hard at the top. Open slowly with control. Isolates the chest without requiring shoulder stability."
        ),
        // Chest - Bodyweight
        Exercise(
            name = "Push-Up", muscleGroup = "Chest", equipment = "Bodyweight",
            description = "Start in a plank position with hands slightly wider than shoulder-width. Lower your chest to the floor, keeping your body in a straight line from head to heels. Push back up. Squeeze chest at the top. Keep your core tight throughout to prevent hip sagging."
        ),
        Exercise(
            name = "Dips (Chest)", muscleGroup = "Chest", equipment = "Bodyweight",
            description = "Grip parallel bars and lean your torso forward at roughly 30°. Lower your body by bending your elbows until your shoulders drop below elbow level. Press back up. The forward lean shifts emphasis from triceps to the lower chest."
        ),

        // Back - Barbell
        Exercise(
            name = "Barbell Row", muscleGroup = "Back", equipment = "Barbell",
            description = "Hinge at the hips with a slight knee bend and your back flat, nearly parallel to the floor. Pull the bar to your lower ribcage with elbows flaring out at about 45°. Lower under control. Squeezing your shoulder blades together at the top maximizes lat and mid-back engagement."
        ),
        Exercise(
            name = "Pendlay Row", muscleGroup = "Back", equipment = "Barbell",
            description = "Like the barbell row, but the bar returns to the floor after each rep. Keep your back horizontal. Use an explosive pull to bring the bar to your lower chest. The dead-stop removes momentum, making each rep stricter and heavier."
        ),
        Exercise(
            name = "Deadlift", muscleGroup = "Back", equipment = "Barbell",
            description = "Stand with the bar over your mid-foot, grip just outside your legs. Push the floor away as you extend your hips and knees simultaneously, keeping the bar dragging close to your shins and thighs. Lock out by squeezing glutes at the top. Hinge to lower the bar back down."
        ),
        Exercise(
            name = "Rack Pull", muscleGroup = "Back", equipment = "Barbell",
            description = "Set the bar in a rack at roughly knee height. Grip just outside your legs and pull by extending your hips while keeping your back flat. Targets the upper back and builds lockout strength for the deadlift. Allows heavier loads than a full deadlift."
        ),
        Exercise(
            name = "T-Bar Row", muscleGroup = "Back", equipment = "Barbell",
            description = "Anchor one end of a barbell and straddle it. Grip the free end with a handle attachment and pull the weight to your chest, driving elbows back. Keep your chest against the pad if using a machine, or hinge forward if freestanding."
        ),
        // Back - Dumbbell
        Exercise(
            name = "Dumbbell Row", muscleGroup = "Back", equipment = "Dumbbell",
            description = "Place one hand and the same-side knee on a bench for support. Let the dumbbell hang from your other hand. Pull it up toward your hip, driving your elbow straight back. Keep your torso parallel to the floor. Lower fully for a complete stretch."
        ),
        Exercise(
            name = "Dumbbell Pullover", muscleGroup = "Back", equipment = "Dumbbell",
            description = "Lie across a bench so only your upper back is supported. Hold one dumbbell with both hands above your chest. Lower it in an arc behind your head until you feel a lat stretch, then pull it back over your chest. Keep a slight bend in the elbows."
        ),
        // Back - Cable
        Exercise(
            name = "Lat Pulldown", muscleGroup = "Back", equipment = "Cable",
            description = "Sit with thighs secured under the pad. Grip the bar wider than shoulder-width with an overhand grip. Pull the bar down to your upper chest while leaning back slightly and driving your elbows toward your hips. Control the return to a full arm extension."
        ),
        Exercise(
            name = "Seated Cable Row", muscleGroup = "Back", equipment = "Cable",
            description = "Sit upright with feet on the platform and knees slightly bent. Pull the handle to your lower stomach, squeezing your shoulder blades together at the end. Straighten your arms slowly while keeping your torso upright. Avoid rocking to generate momentum."
        ),
        Exercise(
            name = "Face Pull", muscleGroup = "Back", equipment = "Cable",
            description = "Set the cable at head height with a rope attachment. Pull the rope toward your face, separating your hands so that your thumbs end up pointing behind you. This hits the rear delts and upper traps. Squeeze the contraction and return slowly."
        ),
        Exercise(
            name = "Straight-Arm Pulldown", muscleGroup = "Back", equipment = "Cable",
            description = "Stand facing the cable stack and grip the bar at about head height. With arms kept straight (slight elbow bend), pull the bar down to your thighs using only your lats. Return slowly. Excellent lat isolation without bicep involvement."
        ),
        // Back - Bodyweight
        Exercise(
            name = "Pull-Up", muscleGroup = "Back", equipment = "Bodyweight",
            description = "Hang from a bar with an overhand grip wider than shoulder-width. Pull yourself up until your chin clears the bar, driving your elbows down and back. Lower yourself fully to a dead hang on each rep. Avoid kipping unless training for it specifically."
        ),
        Exercise(
            name = "Chin-Up", muscleGroup = "Back", equipment = "Bodyweight",
            description = "Hang from a bar with an underhand (supinated) grip at shoulder-width. Pull until your chin clears the bar. The underhand grip involves the biceps more than a pull-up, making it slightly easier and more arm-focused while still working the lats."
        ),

        // Shoulders - Barbell
        Exercise(
            name = "Overhead Press", muscleGroup = "Shoulders", equipment = "Barbell",
            description = "Stand (or sit) and grip the bar just outside shoulder-width. Press the bar from collarbone height straight overhead until your arms lock out. As the bar passes your face, push your head through to keep the bar over your center of mass. Lower to collarbone."
        ),
        Exercise(
            name = "Push Press", muscleGroup = "Shoulders", equipment = "Barbell",
            description = "Start like an overhead press. Dip your knees slightly, then use a quick leg drive to initiate the movement and help get the bar past the sticking point. Lock out overhead. Allows heavier loads than a strict press to build overall pressing strength."
        ),
        Exercise(
            name = "Barbell Upright Row", muscleGroup = "Shoulders", equipment = "Barbell",
            description = "Hold the bar with an overhand grip narrower than shoulder-width. Pull the bar straight up along your body to chin level, leading with your elbows flaring up and out. Lower with control. Targets front and side delts plus upper traps."
        ),
        // Shoulders - Dumbbell
        Exercise(
            name = "Dumbbell Shoulder Press", muscleGroup = "Shoulders", equipment = "Dumbbell",
            description = "Hold dumbbells at shoulder height with palms facing forward. Press them overhead until your arms are fully extended. Lower to starting position with control. Dumbbells allow each arm to work independently, improving symmetry and range of motion."
        ),
        Exercise(
            name = "Arnold Press", muscleGroup = "Shoulders", equipment = "Dumbbell",
            description = "Start with dumbbells at chin height, palms facing you. As you press up, rotate your palms outward so they face forward at the top. Reverse the rotation on the way down. The rotation hits all three delt heads throughout the movement."
        ),
        Exercise(
            name = "Lateral Raise", muscleGroup = "Shoulders", equipment = "Dumbbell",
            description = "Hold dumbbells at your sides. Raise your arms out to the sides to shoulder height with a slight bend in your elbows, keeping the pinky side slightly higher than the thumb. Lower slowly. Keep torso still. Isolates the lateral (side) deltoid head."
        ),
        Exercise(
            name = "Front Raise", muscleGroup = "Shoulders", equipment = "Dumbbell",
            description = "Hold dumbbells in front of your thighs. Raise one or both arms straight forward to shoulder height, keeping a slight elbow bend. Lower under control. Targets the anterior (front) deltoid. Avoid swinging the torso to generate momentum."
        ),
        Exercise(
            name = "Reverse Fly", muscleGroup = "Shoulders", equipment = "Dumbbell",
            description = "Hinge forward at the hips until your torso is nearly parallel to the floor. With a slight elbow bend, raise the dumbbells out to the sides until your arms are parallel to the floor. Squeeze your rear delts at the top. Lower slowly."
        ),
        // Shoulders - Cable
        Exercise(
            name = "Cable Lateral Raise", muscleGroup = "Shoulders", equipment = "Cable",
            description = "Set the cable to the lowest position. Reach across your body to grab the handle with the far hand. Raise your arm out to the side to shoulder height. The cable maintains constant tension, unlike dumbbells which lose tension at the bottom."
        ),

        // Arms - Biceps
        Exercise(
            name = "Barbell Curl", muscleGroup = "Arms", equipment = "Barbell",
            description = "Stand holding the bar at hip level with an underhand grip, hands shoulder-width. Curl the bar up toward your shoulders, keeping your upper arms pinned to your sides. Squeeze at the top and lower with control. Avoid swinging your back."
        ),
        Exercise(
            name = "EZ-Bar Curl", muscleGroup = "Arms", equipment = "Barbell",
            description = "Grip the angled section of the EZ-bar for a more neutral wrist position than a straight bar. Curl from hip level to shoulder height, keeping elbows at your sides. Easier on the wrists while still fully targeting the biceps."
        ),
        Exercise(
            name = "Dumbbell Curl", muscleGroup = "Arms", equipment = "Dumbbell",
            description = "Stand or sit holding dumbbells with an underhand grip. Curl one or both arms upward, supinating (rotating the palm up) as you lift. Squeeze at the top, then lower slowly. Allows each arm to work through its own range of motion."
        ),
        Exercise(
            name = "Hammer Curl", muscleGroup = "Arms", equipment = "Dumbbell",
            description = "Hold dumbbells with a neutral grip, thumbs pointing up. Curl without rotating your forearm, keeping the hammer position throughout. Targets the brachialis and brachioradialis alongside the bicep, building overall arm thickness."
        ),
        Exercise(
            name = "Incline Dumbbell Curl", muscleGroup = "Arms", equipment = "Dumbbell",
            description = "Set the bench to 45–60°. Let your arms hang straight down behind you. Curl up from the fully stretched position at the bottom. The incline creates a longer range of motion and places greater stretch on the long head of the bicep."
        ),
        Exercise(
            name = "Concentration Curl", muscleGroup = "Arms", equipment = "Dumbbell",
            description = "Sit on a bench, brace your upper arm against your inner thigh. Curl the dumbbell up toward your opposite shoulder, rotating palm up at the top. The braced position fully isolates the bicep and eliminates cheating."
        ),
        Exercise(
            name = "Cable Curl", muscleGroup = "Arms", equipment = "Cable",
            description = "Stand facing the cable stack and grip the bar with an underhand grip. Curl upward, keeping your elbows fixed at your sides. Cable maintains tension throughout the full range of motion, including at the top where dumbbells go slack."
        ),
        Exercise(
            name = "Preacher Curl", muscleGroup = "Arms", equipment = "Machine",
            description = "Rest your upper arms on the angled preacher pad. Curl the weight from a fully extended position up toward your shoulders. Lower completely on each rep. The pad prevents elbow swinging, providing strict bicep isolation with emphasis on the lower portion."
        ),
        // Arms - Triceps
        Exercise(
            name = "Skull Crusher", muscleGroup = "Arms", equipment = "Barbell",
            description = "Lie flat on a bench holding the bar with a narrow overhand grip, arms extended above your chest. Lower the bar toward your forehead by bending only at the elbows. Extend back up. Keep your upper arms perpendicular to the floor throughout."
        ),
        Exercise(
            name = "Overhead Tricep Extension", muscleGroup = "Arms", equipment = "Dumbbell",
            description = "Hold one dumbbell overhead with both hands. Lower it behind your head by bending at the elbows, keeping elbows pointing forward. Extend back up to the starting position. The overhead position stretches the long head of the tricep maximally."
        ),
        Exercise(
            name = "Dumbbell Kickback", muscleGroup = "Arms", equipment = "Dumbbell",
            description = "Hinge forward at the hips and pin your upper arm parallel to the floor. Extend your forearm backward until your arm is fully straight. Squeeze the tricep at the top. Lower slowly. Keep your upper arm stationary throughout the movement."
        ),
        Exercise(
            name = "Tricep Pushdown", muscleGroup = "Arms", equipment = "Cable",
            description = "Face the cable with a bar or rope attachment at chest height. Pin your elbows to your sides and push the bar or rope downward until your arms are fully extended. Squeeze the triceps at the bottom. Let the weight rise slowly on the return."
        ),
        Exercise(
            name = "Overhead Cable Extension", muscleGroup = "Arms", equipment = "Cable",
            description = "Attach a rope to the low pulley. Face away from the cable and hold the rope overhead. Extend your arms forward and upward, keeping elbows close to your head. Stretches and works the long head of the tricep through a long range of motion."
        ),
        Exercise(
            name = "Dips (Triceps)", muscleGroup = "Arms", equipment = "Bodyweight",
            description = "Grip parallel bars and keep your torso upright (vertical). Lower your body by bending your elbows, keeping them close to your body. Press back up until arms are fully extended. The upright torso shifts emphasis to the triceps rather than the chest."
        ),

        // Legs - Barbell
        Exercise(
            name = "Barbell Squat", muscleGroup = "Legs", equipment = "Barbell",
            description = "Place the bar on your upper back. Stand with feet shoulder-width apart, toes slightly outward. Squat by sitting back and down until your thighs are at least parallel to the floor. Drive through your heels to stand, keeping your chest up and knees tracking over your toes."
        ),
        Exercise(
            name = "Front Squat", muscleGroup = "Legs", equipment = "Barbell",
            description = "Rest the bar on the front of your shoulders with elbows held high. Squat down with your torso more upright than a back squat. Stand by driving your knees out and extending your hips. More quad-dominant and easier on the lower back than a back squat."
        ),
        Exercise(
            name = "Romanian Deadlift", muscleGroup = "Legs", equipment = "Barbell",
            description = "Hold the bar at hip level. Hinge at the hips with a slight knee bend, dragging the bar down your legs until you feel a deep hamstring stretch. Drive your hips forward to return to standing. Keep your back flat and bar close to your body throughout."
        ),
        Exercise(
            name = "Sumo Deadlift", muscleGroup = "Legs", equipment = "Barbell",
            description = "Take a wide stance with toes pointed significantly outward. Grip the bar inside your legs. Pull by driving your knees out while extending your hips and knees. The wide stance shortens the range of motion and places less stress on the lower back than conventional."
        ),
        Exercise(
            name = "Hip Thrust", muscleGroup = "Legs", equipment = "Barbell",
            description = "Sit with your upper back against a bench and the bar across your hips. Plant your feet flat on the floor. Drive your hips upward until your body forms a straight line from shoulders to knees. Squeeze your glutes hard at the top. Lower with control."
        ),
        Exercise(
            name = "Barbell Lunge", muscleGroup = "Legs", equipment = "Barbell",
            description = "Place the bar on your upper back. Step forward into a lunge, lowering your back knee toward the floor until your front thigh is parallel. Push off your front foot to return to standing. Alternate legs each rep or complete all reps on one side first."
        ),
        Exercise(
            name = "Good Morning", muscleGroup = "Legs", equipment = "Barbell",
            description = "Place the bar on your upper back with a slight knee bend. Hinge at the hips, lowering your torso until nearly parallel to the floor. Return by driving your hips forward. Primarily a hamstring and lower back exercise. Keep your back flat throughout."
        ),
        // Legs - Dumbbell
        Exercise(
            name = "Goblet Squat", muscleGroup = "Legs", equipment = "Dumbbell",
            description = "Hold a dumbbell vertically at chest height with both hands. Squat between your knees, keeping your torso upright and elbows inside your thighs. Drive through your heels to stand. Excellent for learning squat mechanics and improving mobility."
        ),
        Exercise(
            name = "Dumbbell Lunge", muscleGroup = "Legs", equipment = "Dumbbell",
            description = "Hold dumbbells at your sides. Step forward and lower your back knee toward the floor until your front thigh is parallel. Push off your front foot to return. Keep your torso upright and front knee tracking over your toes throughout the movement."
        ),
        Exercise(
            name = "Dumbbell Romanian Deadlift", muscleGroup = "Legs", equipment = "Dumbbell",
            description = "Hold dumbbells in front of your thighs. Hinge at the hips, lowering the dumbbells along your legs until you feel a hamstring stretch. Drive your hips forward to return upright. Keep your back flat and dumbbells close to your body."
        ),
        Exercise(
            name = "Bulgarian Split Squat", muscleGroup = "Legs", equipment = "Dumbbell",
            description = "Elevate your rear foot on a bench. Hold dumbbells at your sides. Lower your front knee toward the floor until your front thigh is roughly parallel. Drive through your front foot to stand. Primarily targets the glutes and quads of the front leg."
        ),
        Exercise(
            name = "Dumbbell Step-Up", muscleGroup = "Legs", equipment = "Dumbbell",
            description = "Hold dumbbells at your sides. Place one foot on a box or bench. Drive through that heel to step up and stand fully on top. Step down and repeat. Builds unilateral leg strength and hip stability. Keep your torso upright throughout."
        ),
        // Legs - Machine
        Exercise(
            name = "Leg Press", muscleGroup = "Legs", equipment = "Machine",
            description = "Sit in the machine with your back flat against the pad. Place feet shoulder-width on the platform. Push the platform away by extending your legs, stopping just short of locking your knees. Lower slowly until knees reach 90°. Foot position shifts emphasis between quads and glutes."
        ),
        Exercise(
            name = "Leg Extension", muscleGroup = "Legs", equipment = "Machine",
            description = "Sit in the machine and hook your ankles under the padded bar. Extend your legs until they are fully straight, squeezing the quads at the top. Lower slowly. Fully isolates the quadriceps. Avoid using momentum to swing the weight up."
        ),
        Exercise(
            name = "Leg Curl", muscleGroup = "Legs", equipment = "Machine",
            description = "Lie face down (or sit in a seated variation) and hook your heels under the padded roller. Curl your legs up as far as possible. Lower slowly with control. Fully isolates the hamstrings. Avoid lifting your hips on the lying variation."
        ),
        Exercise(
            name = "Hack Squat", muscleGroup = "Legs", equipment = "Machine",
            description = "Stand on the platform with your back against the pad and feet forward. Lower by bending your knees, keeping your back pressed to the pad. Drive through your feet to extend back up. Allows a deep quad stretch with less lower back involvement than free bar squats."
        ),
        Exercise(
            name = "Calf Raise (Machine)", muscleGroup = "Legs", equipment = "Machine",
            description = "Stand on the platform with the balls of your feet on the edge and the shoulder pads resting on your shoulders. Rise up onto your toes as high as possible, hold briefly, then lower your heels below the platform level for a full calf stretch."
        ),
        Exercise(
            name = "Seated Calf Raise", muscleGroup = "Legs", equipment = "Machine",
            description = "Sit with the pads resting on your lower thighs and the balls of your feet on the platform edge. Rise onto your toes as high as possible, hold at the top, then lower slowly for a full stretch. The seated position emphasizes the soleus over the gastrocnemius."
        ),
        // Legs - Bodyweight
        Exercise(
            name = "Bodyweight Squat", muscleGroup = "Legs", equipment = "Bodyweight",
            description = "Stand with feet shoulder-width apart, toes slightly out. Sit back and down until your thighs are at least parallel to the floor, keeping your chest up and weight through your heels. Drive through your heels to stand. Keep your knees tracking over your toes."
        ),
        Exercise(
            name = "Walking Lunge", muscleGroup = "Legs", equipment = "Bodyweight",
            description = "Step forward into a lunge, lowering your back knee toward the floor. Instead of returning to the starting position, bring your back foot forward to step into the next lunge. Continue walking forward. Builds coordination alongside unilateral leg strength."
        ),

        // Core
        Exercise(
            name = "Plank", muscleGroup = "Core", equipment = "Bodyweight",
            description = "Place your forearms on the floor with elbows under your shoulders. Hold your body in a straight line from head to heels, squeezing your core, glutes, and quads. Breathe steadily. Do not let your hips sag or pike up. Hold for time."
        ),
        Exercise(
            name = "Crunch", muscleGroup = "Core", equipment = "Bodyweight",
            description = "Lie on your back with knees bent and feet flat. Place hands lightly behind your head. Curl your upper body upward, lifting your shoulder blades off the floor using your abs. Lower slowly. Avoid pulling on your neck or using momentum."
        ),
        Exercise(
            name = "Hanging Leg Raise", muscleGroup = "Core", equipment = "Bodyweight",
            description = "Hang from a pull-up bar with a firm grip. Raise your straight legs to parallel (or higher for more difficulty) using your abs. Lower them slowly without swinging. If straight legs are too difficult, start with bent knees and progress from there."
        ),
        Exercise(
            name = "Russian Twist", muscleGroup = "Core", equipment = "Bodyweight",
            description = "Sit on the floor with knees bent and feet slightly raised. Lean back to about 45° and clasp your hands together (or hold a weight). Rotate your torso side to side, touching your hands to the floor beside your hips each rep. Targets the obliques."
        ),
        Exercise(
            name = "Mountain Climber", muscleGroup = "Core", equipment = "Bodyweight",
            description = "Start in a push-up plank position. Drive one knee toward your chest, then quickly switch legs in a running motion. Keep your hips level and core tight. The faster you go, the more cardio demand; slower reps increase core stability challenge."
        ),
        Exercise(
            name = "Ab Rollout", muscleGroup = "Core", equipment = "Bodyweight",
            description = "Kneel on the floor with an ab wheel in front of you. Roll the wheel forward as far as you can while keeping your core braced, then pull it back by contracting your abs. Keep your hips in line — do not let them sag toward the floor."
        ),
        Exercise(
            name = "Cable Crunch", muscleGroup = "Core", equipment = "Cable",
            description = "Kneel facing the cable with a rope attachment overhead. Hold the rope at the sides of your head. Crunch your elbows toward your knees by rounding your spine, not by hinging at the hips. Squeeze your abs at the bottom and return slowly."
        ),
        Exercise(
            name = "Woodchopper", muscleGroup = "Core", equipment = "Cable",
            description = "Set the cable high on one side. Grip the handle with both hands and pull it diagonally down and across your body in a chopping motion, rotating through your core. Control the return. Targets the obliques and trains rotational power."
        ),
        Exercise(
            name = "Decline Sit-Up", muscleGroup = "Core", equipment = "Bodyweight",
            description = "Lock your feet on a decline bench. Lower your torso toward the bench with control, then crunch back up to vertical. The decline angle increases range of motion compared to a flat crunch. Keep your hands lightly behind your head to avoid neck strain."
        ),
        Exercise(
            name = "Side Plank", muscleGroup = "Core", equipment = "Bodyweight",
            description = "Lie on your side and prop yourself up on one forearm, elbow below your shoulder. Lift your hips so your body forms a straight diagonal line. Keep your top hip stacked over the bottom. Hold, engaging your obliques. Switch sides. Keep your head aligned with your spine."
        )
    )
}
