const express = require("express");
const cors = require("cors");

const app = express();

app.use(cors());
app.use(express.json());

app.get("/", (req, res) => {
    res.json({
        message: "StudyFlow Mock AI Backend is running"
    });
});

app.post("/analyze", async (req, res) => {

    console.log(">>> Android called /analyze");
    console.log("Tasks received:", req.body.tasks?.length || 0);
    console.log("Plans received:", req.body.plans?.length || 0);

    try {

        const tasks = req.body.tasks || [];
        const plans = req.body.plans || [];

        if (tasks.length === 0) {

            return res.json({
                mode: "MOCK_AI",
                recommendation:
                    "You currently have no tasks to analyze. Add at least one task with a deadline first."
            });
        }

        const now = Date.now();

        // Only analyze unfinished tasks
        const activeTasks = tasks.filter(task =>
            task.status !== "COMPLETED"
        );

        if (activeTasks.length === 0) {

            return res.json({
                mode: "MOCK_AI",
                recommendation:
                    "Great job! All of your current tasks are completed. You can review upcoming courses or add a new task."
            });
        }

        // Calculate score for every active task
        const analyzedTasks = activeTasks.map(task => {

            const estimatedMinutes =
                Number(task.estimatedMinutes || 0);

            const completedMinutes =
                Number(task.completedMinutes || 0);

            const remainingMinutes =
                Math.max(
                    estimatedMinutes - completedMinutes,
                    0
                );

            const deadline =
                Number(task.deadline || 0);

            const timeRemaining =
                deadline - now;

            const daysLeft =
                timeRemaining /
                (1000 * 60 * 60 * 24);

            let priorityScore = 1;

            if (task.priority === "HIGH") {
                priorityScore = 3;

            } else if (task.priority === "MEDIUM") {
                priorityScore = 2;
            }

            let urgencyScore;

            if (daysLeft <= 0) {

                urgencyScore = 100;

            } else {

                urgencyScore =
                    10 / Math.max(daysLeft, 0.5);
            }

            const workloadScore =
                remainingMinutes / 60.0;

            const totalScore =
                priorityScore * 10
                + urgencyScore
                + workloadScore;

            return {
                ...task,
                remainingMinutes,
                daysLeft,
                totalScore
            };
        });

        // Highest score first
        analyzedTasks.sort(
            (a, b) =>
                b.totalScore - a.totalScore
        );

        const urgentTask =
            analyzedTasks[0];

        let recommendation = "";

        // 1. Deadline analysis
        if (urgentTask.daysLeft <= 0) {

            recommendation +=
                `${urgentTask.title} has already passed its deadline. `;

        } else if (urgentTask.daysLeft <= 1) {

            recommendation +=
                `${urgentTask.title} is at high risk because its deadline is less than one day away. `;

        } else if (urgentTask.daysLeft <= 3) {

            recommendation +=
                `${urgentTask.title} should be your main priority because its deadline is approaching. `;

        } else {

            recommendation +=
                `${urgentTask.title} is currently your highest-priority task. `;
        }

        // 2. Workload analysis
        if (urgentTask.remainingMinutes >= 240) {

            recommendation +=
                `You still have about ${urgentTask.remainingMinutes} minutes of work remaining. Divide it into several focused study sessions instead of trying to finish everything at once. `;

        } else if (urgentTask.remainingMinutes >= 120) {

            recommendation +=
                `You have about ${urgentTask.remainingMinutes} minutes remaining. Completing one or two focused sessions today would help you stay on track. `;

        } else if (urgentTask.remainingMinutes >= 60) {

            recommendation +=
                `You have around ${urgentTask.remainingMinutes} minutes remaining. A focused session today would significantly improve your progress. `;

        } else if (urgentTask.remainingMinutes > 0) {

            recommendation +=
                `Only about ${urgentTask.remainingMinutes} minutes remain, so finishing this task soon would reduce your workload. `;

        } else {

            recommendation +=
                `This task is almost complete. `;
        }

        // 3. Find today's plans
        const todayStart =
            new Date();

        todayStart.setHours(
            0,
            0,
            0,
            0
        );

        const tomorrow =
            new Date(todayStart);

        tomorrow.setDate(
            tomorrow.getDate() + 1
        );

        const todayPlans =
            plans.filter(plan => {

                const studyDate =
                    Number(plan.studyDate || 0);

                return (
                    studyDate >= todayStart.getTime()
                    &&
                    studyDate < tomorrow.getTime()
                );
            });

        let remainingToday = 0;

        todayPlans.forEach(plan => {

            const plannedMinutes =
                Number(plan.plannedMinutes || 0);

            const completedMinutes =
                Number(plan.completedMinutes || 0);

            remainingToday +=
                Math.max(
                    plannedMinutes
                    - completedMinutes,
                    0
                );
        });

        // 4. Today's workload
        if (remainingToday >= 240) {

            recommendation +=
                `Your remaining study load today is ${remainingToday} minutes, which is quite heavy. Consider splitting the work into shorter sessions with breaks.`;

        } else if (remainingToday >= 120) {

            recommendation +=
                `You still have ${remainingToday} minutes planned for today. Prioritize the most urgent task first and complete the remaining sessions gradually.`;

        } else if (remainingToday > 0) {

            recommendation +=
                `You currently have ${remainingToday} minutes of planned study remaining today.`;

        } else {

            recommendation +=
                `There are currently no remaining planned study sessions for today.`;
        }

        return res.json({
            mode: "MOCK_AI",
            recommendation: recommendation
        });

    } catch (error) {

        console.error(
            "StudyFlow analysis error:",
            error
        );

        return res.status(500).json({
            error: "Study analysis failed"
        });
    }
});

const PORT = 3000;

app.listen(PORT, () => {

    console.log(
        `StudyFlow Mock AI Backend running at http://localhost:${PORT}`
    );
});