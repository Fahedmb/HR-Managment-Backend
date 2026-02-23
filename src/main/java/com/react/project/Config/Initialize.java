package com.react.project.Config;

import com.react.project.Enumirator.*;
import com.react.project.Model.*;
import com.react.project.Repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

@Component
@RequiredArgsConstructor
public class Initialize implements CommandLineRunner {

    private final UserRepository userRepository;
    private final ProjectRepository projectRepository;
    private final TeamRepository teamRepository;
    private final TeamMemberRepository teamMemberRepository;
    private final TaskRepository taskRepository;
    private final LeaveRequestRepository leaveRequestRepository;
    private final TimeSheetRepository timeSheetRepository;
    private final TimesheetScheduleRepository timesheetScheduleRepository;
    private final MeetingRepository meetingRepository;
    private final PerformanceEvaluationRepository performanceEvaluationRepository;
    private final PasswordEncoder passwordEncoder;

    private final Random rng = new Random(42);

    @Override
    public void run(String... args) {
        createAdminUserIfNeeded();
        if (userRepository.count() < 2) {
            seedAll();
        } else {
            System.out.println("[Seed] Data already present — skipping.");
        }
    }

    // ─── Admin ───────────────────────────────────────────────────────────────

    private void createAdminUserIfNeeded() {
        if (userRepository.findByEmail("admin@admin.com").isEmpty()) {
            User admin = User.builder()
                    .firstName("Admin").lastName("User")
                    .email("admin@admin.com").username("admin@admin.com")
                    .password(passwordEncoder.encode("admin123"))
                    .role(Role.HR)
                    .department("Human Resources").position("HR Director")
                    .usedDaysThisYear(0)
                    .build();
            userRepository.save(admin);
            System.out.println("[Seed] Admin created: admin@admin.com");
        }
    }

    // ─── Master seed ─────────────────────────────────────────────────────────

    private void seedAll() {
        System.out.println("[Seed] Seeding demo data …");

        // 1. Users
        User admin = userRepository.findByEmail("admin@admin.com").orElseThrow();
        admin.setFirstName("Admin"); admin.setLastName("User");
        admin.setDepartment("Human Resources"); admin.setPosition("HR Director");
        userRepository.save(admin);

        List<User> hrUsers    = createUsers(hrDefs(), "Human Resources");
        List<User> engUsers   = createUsers(engDefs(), "Engineering");
        List<User> mktUsers   = createUsers(mktDefs(), "Marketing");
        List<User> salesUsers = createUsers(salesDefs(), "Sales");
        List<User> opsUsers   = createUsers(opsDefs(), "Operations");

        List<User> allEmployees = new ArrayList<>();
        allEmployees.addAll(engUsers);
        allEmployees.addAll(mktUsers);
        allEmployees.addAll(salesUsers);
        allEmployees.addAll(opsUsers);

        // 2. Projects
        Project pEcom  = proj("E-Commerce Platform Relaunch",
                "Full redesign and backend overhaul of the main e-commerce storefront.",
                "Engineering", ProjectStatus.ACTIVE, -90, 90, admin);
        Project pMobile = proj("Mobile App 2.0",
                "Complete rebuild of iOS and Android apps with new design system.",
                "Engineering", ProjectStatus.ACTIVE, -60, 60, admin);
        Project pMigrate = proj("Data Center Migration",
                "Move on-premise infrastructure to cloud (AWS).",
                "Engineering", ProjectStatus.COMPLETED, -180, -30, admin);
        Project pCampaign = proj("Q1 Brand Campaign",
                "Multi-channel marketing campaign for Q1 product launches.",
                "Marketing", ProjectStatus.COMPLETED, -120, -10, admin);
        Project pBrand = proj("Brand Identity Refresh",
                "Update logo, colour palette and brand guidelines.",
                "Marketing", ProjectStatus.PLANNING, 7, 90, admin);
        Project pCRM = proj("CRM Integration",
                "Integrate Salesforce with internal systems and migrate data.",
                "Sales", ProjectStatus.ACTIVE, -45, 120, admin);
        Project pTrain = proj("Sales Enablement Program",
                "Training portal and automated onboarding for new reps.",
                "Sales", ProjectStatus.ON_HOLD, -30, 60, admin);
        Project pSupply = proj("Supply Chain Optimisation",
                "Reduce lead times and automate reorder triggers.",
                "Operations", ProjectStatus.ACTIVE, -60, 90, admin);

        // 3. Teams & members
        // Engineering – E-Commerce
        Team tBackend = team("Backend Team",     "Core API & services",   pEcom,   engUsers.get(0), List.of(engUsers.get(0), engUsers.get(1), hrUsers.get(0)));
        Team tFrontend= team("Frontend Team",    "React SPA & design",    pEcom,   engUsers.get(1), List.of(engUsers.get(1), engUsers.get(2), mktUsers.get(0)));
        // Engineering – Mobile
        Team tMobile  = team("Mobile Team",      "iOS & Android",         pMobile, engUsers.get(2), List.of(engUsers.get(2), engUsers.get(3), engUsers.get(0)));
        // Engineering – Migration (completed)
        Team tInfra   = team("Infrastructure",   "Cloud & DevOps",        pMigrate,engUsers.get(3), List.of(engUsers.get(3), engUsers.get(0), opsUsers.get(0)));
        // Marketing
        Team tContent = team("Content Team",     "Copy & design",         pCampaign,mktUsers.get(0),List.of(mktUsers.get(0), mktUsers.get(1), hrUsers.get(1)));
        Team tGrowth  = team("Growth Team",      "SEO & paid media",      pBrand,  mktUsers.get(1), List.of(mktUsers.get(1), mktUsers.get(0), salesUsers.get(0)));
        // Sales
        Team tSalesDev= team("Sales Dev Team",   "CRM setup & scripts",   pCRM,    salesUsers.get(0),List.of(salesUsers.get(0), salesUsers.get(1), hrUsers.get(0)));
        Team tTraining= team("Training Team",    "Curriculum & LMS",      pTrain,  salesUsers.get(1),List.of(salesUsers.get(1), mktUsers.get(0)));
        // Operations
        Team tOps     = team("Ops Analytics",    "Data & process mining", pSupply, opsUsers.get(0), List.of(opsUsers.get(0), opsUsers.get(1), engUsers.get(3)));

        // 4. Tasks
        seedTasks(admin, tBackend,  pEcom,   backendTaskDefs());
        seedTasks(admin, tFrontend, pEcom,   frontendTaskDefs());
        seedTasks(admin, tMobile,   pMobile, mobileTaskDefs());
        seedTasks(admin, tInfra,    pMigrate,infraTaskDefs());
        seedTasks(admin, tContent,  pCampaign, contentTaskDefs());
        seedTasks(admin, tGrowth,   pBrand,  growthTaskDefs());
        seedTasks(admin, tSalesDev, pCRM,    crmTaskDefs());
        seedTasks(admin, tOps,      pSupply, opsTaskDefs());

        // 5. Leave requests
        seedLeaveRequests(allEmployees, admin);

        // 6. Timesheets (daily punches for last 90 days)
        seedTimesheets(allEmployees, admin);

        // 7. Timesheet schedules
        seedTimesheetSchedules(allEmployees, admin);

        // 8. Meetings
        seedMeetings(admin, allEmployees);

        // 9. Performance evaluations
        seedEvaluations(admin, allEmployees);

        System.out.println("[Seed] Done. " + userRepository.count() + " users, "
                + projectRepository.count() + " projects, "
                + taskRepository.count() + " tasks.");
    }

    // ─── User definitions ────────────────────────────────────────────────────

    record UserDef(String first, String last, String email, String position) {}

    private List<UserDef> hrDefs() {
        return List.of(
            new UserDef("Sarah",   "Mitchell", "sarah.hr@company.com",   "HR Specialist"),
            new UserDef("Daniel",  "Park",     "daniel.hr@company.com",  "HR Coordinator")
        );
    }
    private List<UserDef> engDefs() {
        return List.of(
            new UserDef("Alice",   "Johnson",  "alice.eng@company.com",  "Backend Developer"),
            new UserDef("Bob",     "Smith",    "bob.eng@company.com",    "Frontend Developer"),
            new UserDef("Charlie", "Brown",    "charlie.eng@company.com","DevOps Engineer"),
            new UserDef("Diana",   "Prince",   "diana.eng@company.com",  "QA Engineer")
        );
    }
    private List<UserDef> mktDefs() {
        return List.of(
            new UserDef("Emma",    "Wilson",   "emma.mkt@company.com",   "Marketing Specialist"),
            new UserDef("Frank",   "Miller",   "frank.mkt@company.com",  "Content Manager")
        );
    }
    private List<UserDef> salesDefs() {
        return List.of(
            new UserDef("Grace",   "Lee",      "grace.sales@company.com","Sales Representative"),
            new UserDef("Henry",   "Davis",    "henry.sales@company.com","Account Manager")
        );
    }
    private List<UserDef> opsDefs() {
        return List.of(
            new UserDef("Iris",    "Chen",     "iris.ops@company.com",   "Operations Analyst"),
            new UserDef("James",   "Wilson",   "james.ops@company.com",  "Supply Chain Manager")
        );
    }

    private List<User> createUsers(List<UserDef> defs, String dept) {
        List<User> created = new ArrayList<>();
        Role role = dept.equals("Human Resources") ? Role.HR : Role.EMPLOYEE;
        for (UserDef d : defs) {
            if (userRepository.findByEmail(d.email()).isEmpty()) {
                User u = User.builder()
                        .firstName(d.first()).lastName(d.last())
                        .email(d.email()).username(d.email())
                        .password(passwordEncoder.encode("password123"))
                        .role(role).department(dept).position(d.position())
                        .usedDaysThisYear(rng.nextInt(15))
                        .build();
                created.add(userRepository.save(u));
            } else {
                created.add(userRepository.findByEmail(d.email()).get());
            }
        }
        return created;
    }

    // ─── Projects ────────────────────────────────────────────────────────────

    private Project proj(String name, String desc, String dept,
                         ProjectStatus status, int startDaysOffset, int deadlineDaysOffset, User creator) {
        LocalDate today = LocalDate.now();
        Project p = Project.builder()
                .name(name).description(desc).department(dept).status(status)
                .startDate(today.plusDays(startDaysOffset))
                .deadline(today.plusDays(deadlineDaysOffset))
                .createdBy(creator).build();
        return projectRepository.save(p);
    }

    // ─── Teams ───────────────────────────────────────────────────────────────

    private Team team(String name, String desc, Project project, User leader, List<User> members) {
        Team t = Team.builder().name(name).description(desc)
                .project(project).teamLeader(leader).build();
        t = teamRepository.save(t);
        for (User u : members) {
            TeamMember tm = TeamMember.builder().team(t).user(u)
                    .role(u.equals(leader) ? TeamMemberRole.LEADER : TeamMemberRole.MEMBER)
                    .build();
            teamMemberRepository.save(tm);
        }
        return t;
    }

    // ─── Task definitions ────────────────────────────────────────────────────

    record TaskDef(String title, String desc, TaskStatus status, TaskPriority priority, int deadlineDays, double est) {}

    private List<TaskDef> backendTaskDefs() {
        return List.of(
            new TaskDef("Design REST API schema",          "OpenAPI spec for all endpoints",          TaskStatus.DONE,        TaskPriority.HIGH,     -50, 8),
            new TaskDef("Implement auth service",          "JWT + refresh token flow",                TaskStatus.DONE,        TaskPriority.CRITICAL,  -40, 16),
            new TaskDef("Product catalogue microservice",  "CRUD + search with Elasticsearch",        TaskStatus.DONE,        TaskPriority.HIGH,      -30, 24),
            new TaskDef("Order management service",        "Order lifecycle state machine",           TaskStatus.IN_PROGRESS, TaskPriority.HIGH,       15, 20),
            new TaskDef("Payment gateway integration",     "Stripe & PayPal connectors",              TaskStatus.IN_PROGRESS, TaskPriority.CRITICAL,   20, 16),
            new TaskDef("Notification service",            "Email + push + SMS",                      TaskStatus.TODO,        TaskPriority.MEDIUM,     30, 12),
            new TaskDef("Performance optimisation",        "DB query tuning & caching layer",         TaskStatus.TODO,        TaskPriority.MEDIUM,     45, 10),
            new TaskDef("API rate limiting",               "Redis-backed throttling middleware",       TaskStatus.BLOCKED,     TaskPriority.LOW,        60,  6)
        );
    }
    private List<TaskDef> frontendTaskDefs() {
        return List.of(
            new TaskDef("Design system setup",             "Tailwind + Storybook",                    TaskStatus.DONE,        TaskPriority.HIGH,      -60, 8),
            new TaskDef("Home page redesign",              "Hero, featured products, banners",         TaskStatus.DONE,        TaskPriority.HIGH,      -45,12),
            new TaskDef("Product listing page",            "Filters, sorting, pagination",             TaskStatus.DONE,        TaskPriority.HIGH,      -30,16),
            new TaskDef("Product detail page",             "Gallery, reviews, add-to-cart",            TaskStatus.IN_REVIEW,   TaskPriority.HIGH,       10,12),
            new TaskDef("Shopping cart & checkout",        "Multi-step checkout flow",                 TaskStatus.IN_PROGRESS, TaskPriority.CRITICAL,   25,20),
            new TaskDef("User account dashboard",          "Orders, returns, addresses",               TaskStatus.TODO,        TaskPriority.MEDIUM,     40,10),
            new TaskDef("A/B testing setup",               "Feature flags & analytics hooks",          TaskStatus.TODO,        TaskPriority.LOW,        55, 6)
        );
    }
    private List<TaskDef> mobileTaskDefs() {
        return List.of(
            new TaskDef("React Native scaffold",           "Expo + navigation + auth flow",            TaskStatus.DONE,        TaskPriority.HIGH,      -45, 8),
            new TaskDef("Push notification service",       "Firebase Cloud Messaging setup",           TaskStatus.DONE,        TaskPriority.HIGH,      -30,10),
            new TaskDef("Home feed screen",                "Personalised product feed",                TaskStatus.IN_PROGRESS, TaskPriority.HIGH,       20,14),
            new TaskDef("Search & discovery",              "Debounced search + filters",               TaskStatus.IN_PROGRESS, TaskPriority.MEDIUM,     30,12),
            new TaskDef("Checkout flow mobile",            "Apple Pay & Google Pay",                   TaskStatus.TODO,        TaskPriority.CRITICAL,   45,18),
            new TaskDef("App Store submission",            "Screenshots, metadata, review",            TaskStatus.TODO,        TaskPriority.HIGH,       55, 4)
        );
    }
    private List<TaskDef> infraTaskDefs() {
        return List.of(
            new TaskDef("VPC & network config",            "AWS VPC, subnets, SGs",                   TaskStatus.DONE,        TaskPriority.CRITICAL, -150,16),
            new TaskDef("Database migration RDS",          "Lift-and-shift MySQL to RDS",              TaskStatus.DONE,        TaskPriority.CRITICAL, -120,24),
            new TaskDef("CI/CD pipeline setup",            "GitHub Actions + ECR + ECS",              TaskStatus.DONE,        TaskPriority.HIGH,     -100,20),
            new TaskDef("Monitoring & alerting",           "CloudWatch + PagerDuty",                  TaskStatus.DONE,        TaskPriority.HIGH,      -80,12),
            new TaskDef("Load testing",                    "k6 suite, 10k concurrent users",          TaskStatus.DONE,        TaskPriority.MEDIUM,    -60,10),
            new TaskDef("DNS cutover",                     "Route 53 migration with 0 downtime",      TaskStatus.DONE,        TaskPriority.CRITICAL,  -35, 8),
            new TaskDef("Post-migration audit",            "Cost optimisation & tagging review",      TaskStatus.DONE,        TaskPriority.LOW,       -20, 6)
        );
    }
    private List<TaskDef> contentTaskDefs() {
        return List.of(
            new TaskDef("Brand messaging workshop",        "Stakeholder alignment session",           TaskStatus.DONE,        TaskPriority.HIGH,     -110, 8),
            new TaskDef("Video scripting & production",    "3 product launch videos",                  TaskStatus.DONE,        TaskPriority.HIGH,      -80,20),
            new TaskDef("Social media calendar",           "90-day content plan",                      TaskStatus.DONE,        TaskPriority.MEDIUM,    -60,10),
            new TaskDef("Blog post series",                "10 SEO-optimised articles",               TaskStatus.DONE,        TaskPriority.MEDIUM,    -40,14),
            new TaskDef("Email campaign sequence",         "8-part nurture sequence",                  TaskStatus.DONE,        TaskPriority.HIGH,      -20,12),
            new TaskDef("Campaign performance report",     "KPI summary & learnings",                  TaskStatus.DONE,        TaskPriority.MEDIUM,    -10, 6)
        );
    }
    private List<TaskDef> growthTaskDefs() {
        return List.of(
            new TaskDef("Brand audit",                     "Current state analysis",                  TaskStatus.TODO,        TaskPriority.HIGH,       14, 8),
            new TaskDef("Competitor benchmarking",         "Top 5 competitor breakdown",              TaskStatus.TODO,        TaskPriority.MEDIUM,     21, 6),
            new TaskDef("New logo concepts",               "3 directions from design agency",         TaskStatus.TODO,        TaskPriority.HIGH,       35,16),
            new TaskDef("Brand guidelines document",       "Typography, colour, tone of voice",       TaskStatus.TODO,        TaskPriority.HIGH,       50,12),
            new TaskDef("Rollout plan",                    "Internal & external launch plan",          TaskStatus.TODO,        TaskPriority.LOW,        60, 6)
        );
    }
    private List<TaskDef> crmTaskDefs() {
        return List.of(
            new TaskDef("Salesforce org setup",            "Custom objects, fields, profiles",        TaskStatus.DONE,        TaskPriority.HIGH,      -40,16),
            new TaskDef("Data migration plan",             "Field mapping & dedup strategy",           TaskStatus.DONE,        TaskPriority.HIGH,      -30,12),
            new TaskDef("API connector development",       "REST sync with ERP system",                TaskStatus.IN_PROGRESS, TaskPriority.CRITICAL,   20,24),
            new TaskDef("User training materials",         "Video walkthroughs & quick-ref cards",    TaskStatus.IN_PROGRESS, TaskPriority.MEDIUM,     35,10),
            new TaskDef("UAT sign-off",                    "100 test scenarios with sales team",       TaskStatus.TODO,        TaskPriority.HIGH,       50,16),
            new TaskDef("Go-live & hypercare",             "2-week hypercare support plan",            TaskStatus.TODO,        TaskPriority.CRITICAL,   65, 8)
        );
    }
    private List<TaskDef> opsTaskDefs() {
        return List.of(
            new TaskDef("Process mapping workshops",       "As-is supply chain documented",           TaskStatus.DONE,        TaskPriority.HIGH,      -55,10),
            new TaskDef("Supplier performance analysis",   "Scorecard for top 10 suppliers",           TaskStatus.DONE,        TaskPriority.HIGH,      -40,12),
            new TaskDef("Demand forecasting model",        "ML-based reorder prediction",              TaskStatus.IN_PROGRESS, TaskPriority.CRITICAL,   25,20),
            new TaskDef("ERP integration spec",            "Technical spec for SAP plugin",            TaskStatus.IN_PROGRESS, TaskPriority.HIGH,       35,14),
            new TaskDef("Pilot warehouse rollout",         "2 pilot sites selected & onboarded",      TaskStatus.TODO,        TaskPriority.HIGH,       50,16),
            new TaskDef("KPI dashboard build",             "Real-time supply chain BI dashboard",     TaskStatus.TODO,        TaskPriority.MEDIUM,     65, 8)
        );
    }

    private void seedTasks(User admin, Team team, Project project, List<TaskDef> defs) {
        List<User> members = teamMemberRepository.findByTeamId(team.getId())
                .stream().map(TeamMember::getUser).toList();
        if (members.isEmpty()) return;
        for (int i = 0; i < defs.size(); i++) {
            TaskDef d = defs.get(i);
            User assignee = members.get(i % members.size());
            Task t = Task.builder()
                    .title(d.title()).description(d.desc())
                    .project(project).team(team)
                    .assignedTo(assignee).createdBy(admin)
                    .status(d.status()).priority(d.priority())
                    .deadline(LocalDate.now().plusDays(d.deadlineDays()))
                    .estimatedHours(d.est())
                    .actualHours(d.status() == TaskStatus.DONE ? d.est() * (0.8 + rng.nextDouble() * 0.4) : null)
                    .build();
            taskRepository.save(t);
        }
    }

    // ─── Leave requests ──────────────────────────────────────────────────────

    private void seedLeaveRequests(List<User> employees, User approver) {
        String[] reasons = {
            "Family vacation", "Medical appointment", "Personal matters",
            "Wedding anniversary trip", "Child's school event",
            "Recovering from illness", "Mental health day", "Home maintenance"
        };
        LeaveType[] types = LeaveType.values();
        LeaveStatus[] statuses = { LeaveStatus.APPROVED, LeaveStatus.APPROVED,
                LeaveStatus.APPROVED, LeaveStatus.REJECTED, LeaveStatus.PENDING };

        for (User emp : employees) {
            int count = 2 + rng.nextInt(3); // 2-4 requests per employee
            for (int i = 0; i < count; i++) {
                int daysAgo = 20 + rng.nextInt(160);
                LocalDate start = LocalDate.now().minusDays(daysAgo);
                LocalDate end   = start.plusDays(1 + rng.nextInt(5));
                LeaveStatus status = statuses[rng.nextInt(statuses.length)];

                LeaveRequest lr = LeaveRequest.builder()
                        .user(emp)
                        .approvedBy(status == LeaveStatus.APPROVED || status == LeaveStatus.REJECTED ? approver : null)
                        .startDate(start).endDate(end)
                        .type(types[rng.nextInt(types.length)])
                        .status(status)
                        .reason(reasons[rng.nextInt(reasons.length)])
                        .halfDay(rng.nextBoolean())
                        .build();
                leaveRequestRepository.save(lr);

                if (status == LeaveStatus.APPROVED) {
                    long days = java.time.temporal.ChronoUnit.DAYS.between(start, end) + 1;
                    emp.setUsedDaysThisYear(emp.getUsedDaysThisYear() + (int) days);
                    userRepository.save(emp);
                }
            }
            // Add one pending request (future)
            LocalDate futureStart = LocalDate.now().plusDays(5 + rng.nextInt(20));
            LeaveRequest pending = LeaveRequest.builder()
                    .user(emp).startDate(futureStart)
                    .endDate(futureStart.plusDays(1 + rng.nextInt(3)))
                    .type(LeaveType.VACATION).status(LeaveStatus.PENDING)
                    .reason("Planned vacation").halfDay(false).build();
            leaveRequestRepository.save(pending);
        }
    }

    // ─── Timesheets (daily punches) ───────────────────────────────────────────

    private void seedTimesheets(List<User> employees, User approver) {
        LocalDate today = LocalDate.now();
        for (User emp : employees) {
            for (int d = 90; d >= 1; d--) {
                LocalDate date = today.minusDays(d);
                // Skip weekends
                java.time.DayOfWeek dow = date.getDayOfWeek();
                if (dow == java.time.DayOfWeek.SATURDAY || dow == java.time.DayOfWeek.SUNDAY) continue;
                double hours = 7.0 + rng.nextDouble() * 2.0; // 7-9 hrs
                hours = Math.round(hours * 2) / 2.0; // round to 0.5
                Status status = d > 5 ? Status.APPROVED : Status.PENDING;
                TimeSheet ts = TimeSheet.builder()
                        .user(emp).date(date)
                        .hoursWorked(hours).status(status)
                        .approvedBy(status == Status.APPROVED ? approver : null)
                        .build();
                timeSheetRepository.save(ts);
            }
        }
    }

    // ─── Timesheet schedules ─────────────────────────────────────────────────

    private void seedTimesheetSchedules(List<User> employees, User approver) {
        List<DayOfWeekEnum> fullWeek = List.of(
                DayOfWeekEnum.MONDAY, DayOfWeekEnum.TUESDAY,
                DayOfWeekEnum.WEDNESDAY, DayOfWeekEnum.THURSDAY,
                DayOfWeekEnum.FRIDAY);
        List<DayOfWeekEnum> partTime = List.of(
                DayOfWeekEnum.MONDAY, DayOfWeekEnum.WEDNESDAY, DayOfWeekEnum.FRIDAY);

        TimesheetStatus[] statuses = { TimesheetStatus.APPROVED, TimesheetStatus.APPROVED,
                TimesheetStatus.APPROVED, TimesheetStatus.PENDING };

        for (int i = 0; i < employees.size(); i++) {
            User emp = employees.get(i);
            boolean pt = i % 5 == 4; // every 5th employee is part-time
            List<DayOfWeekEnum> days = pt ? partTime : fullWeek;
            int hpd = pt ? 8 : 8;
            TimesheetStatus status = statuses[rng.nextInt(statuses.length)];
            TimesheetSchedule s = TimesheetSchedule.builder()
                    .user(emp).chosenDays(days)
                    .startTime(LocalTime.of(9, 0))
                    .hoursPerDay(hpd)
                    .totalHoursPerWeek(hpd * days.size())
                    .status(status).build();
            timesheetScheduleRepository.save(s);
        }
    }

    // ─── Meetings ────────────────────────────────────────────────────────────

    private void seedMeetings(User organizer, List<User> employees) {
        String[][] meetingData = {
            {"Q4 Planning Session",        "Quarterly goals & OKR alignment",          "-45", "2",  "Conference Room A",  "COMPLETED"},
            {"Engineering All-Hands",       "Team updates, blockers, shoutouts",         "-30", "1",  "Main Hall",           "COMPLETED"},
            {"E-Commerce Project Kickoff",  "Scope, timeline and team assignments",      "-85", "2",  "Board Room",          "COMPLETED"},
            {"Sprint Retrospective #1",     "What went well / what to improve",          "-20", "1",  "Meeting Room B",      "COMPLETED"},
            {"Sprint Retrospective #2",     "Sprint 2 retrospective",                    "-7",  "1",  "Meeting Room B",      "COMPLETED"},
            {"Marketing Campaign Review",   "Q1 campaign results & learnings",           "-15", "1",  "Marketing Suite",     "COMPLETED"},
            {"CRM Stakeholder Alignment",   "Requirements gathering with sales leads",   "-10", "1",  "Sales Floor",         "COMPLETED"},
            {"Weekly Standup",              "Team standup – current week",                "1",  "0",  "Online",              "SCHEDULED"},
            {"Product Roadmap Review",      "H1 roadmap & priority changes",              "3",  "2",  "Board Room",          "SCHEDULED"},
            {"Design Review – Mobile App",  "UX walkthrough of new screens",              "6",  "1",  "Design Studio",       "SCHEDULED"},
            {"Sales Pipeline Review",       "Deal status & forecast for Q2",             "8",  "1",  "Sales Floor",         "SCHEDULED"},
            {"All-Hands Company Meeting",   "Company-wide updates from leadership",      "14", "2",  "Main Hall",           "SCHEDULED"},
            {"1-on-1 Performance Chat",     "Mid-year performance conversations",        "10", "1",  "Manager Office",      "SCHEDULED"},
            {"Ops Supply Chain Workshop",   "Process improvement deep-dive",             "18", "3",  "Workshop Room",       "SCHEDULED"},
        };

        List<User> subset4  = employees.subList(0, Math.min(4,  employees.size()));
        List<User> subset6  = employees.subList(0, Math.min(6,  employees.size()));
        List<User> subsetAll = employees;

        List<List<User>> attendeeGroups = List.of(
            subset4, subset6, subsetAll, subset4, subset4,
            subset4, subset4,
            subset6, subsetAll, subset4, subset4, subsetAll, subset4, subset4
        );

        for (int i = 0; i < meetingData.length; i++) {
            String[] m = meetingData[i];
            int dayOffset = Integer.parseInt(m[2]);
            int durationH = Integer.parseInt(m[3]);
            LocalDateTime start = LocalDate.now().plusDays(dayOffset).atTime(9 + (i % 4) * 2, 0);
            MeetingStatus ms = MeetingStatus.valueOf(m[5]);
            Meeting meeting = Meeting.builder()
                    .title(m[0]).description(m[1])
                    .organizer(organizer)
                    .attendees(new ArrayList<>(attendeeGroups.get(i)))
                    .startTime(start).endTime(start.plusHours(durationH))
                    .location(m[4]).status(ms).build();
            meetingRepository.save(meeting);
        }
    }

    // ─── Performance evaluations ─────────────────────────────────────────────

    private void seedEvaluations(User evaluator, List<User> employees) {
        String[] comments = {
            "Excellent performance this quarter. Consistently delivers high-quality work on time.",
            "Good contributor. Has shown strong teamwork and proactive communication.",
            "Met all targets. Could improve on documentation and code review turnaround.",
            "Strong technical skills. Needs to work on cross-functional communication.",
            "Outstanding work ethic. Goes above and beyond in all assigned tasks.",
            "Solid performance overall. Areas for growth: stakeholder management.",
            "Very good progress since last review. Keep up the momentum.",
            "Improving steadily. Focus on delivery speed in next quarter.",
        };
        int[] scores = { 95, 88, 75, 82, 91, 78, 85, 70 };

        // Last-quarter evaluations
        LocalDate lastQuarter = LocalDate.now().minusMonths(3);
        // Mid-year evaluations
        LocalDate midYear = LocalDate.now().minusMonths(6);

        for (int i = 0; i < employees.size(); i++) {
            User emp = employees.get(i);
            // Mid-year
            PerformanceEvaluation e1 = PerformanceEvaluation.builder()
                    .user(emp).evaluator(evaluator)
                    .evaluationDate(midYear.plusDays(i))
                    .score(scores[i % scores.length] - 5 + rng.nextInt(10))
                    .comments(comments[i % comments.length]).build();
            performanceEvaluationRepository.save(e1);
            // Last quarter
            PerformanceEvaluation e2 = PerformanceEvaluation.builder()
                    .user(emp).evaluator(evaluator)
                    .evaluationDate(lastQuarter.plusDays(i))
                    .score(scores[(i + 1) % scores.length] + rng.nextInt(5))
                    .comments(comments[(i + 2) % comments.length]).build();
            performanceEvaluationRepository.save(e2);
        }
    }
}
