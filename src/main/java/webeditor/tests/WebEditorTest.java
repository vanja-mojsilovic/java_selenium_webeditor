public static void main(String[] args) throws IOException {
    WebEditorTest test = new WebEditorTest();

    // ✅ Set CI flag (GitHub Actions sets this, but good to know)
    boolean isCi = System.getenv("CI") != null;

    if (isCi) {
        System.out.println("🚀 Running in CI: Skipping browser. Using Jira API directly...");

        // 🔐 Load credentials
        String email = System.getenv("VANJA_EMAIL");
        String apiToken = System.getenv("JIRA_API_TOKEN");

        // 🧪 Test auth first
        WebEditorPage webEditorPage = new WebEditorPage(null);
        webEditorPage.testMyself(email, apiToken); // Must return 200

        // ✅ Run task using API only
        TaskTest taskTest = new TaskTest();
        taskTest.searchTasks_1(); // This must use getKeyIssuesByApiPost(...), NOT UI

    } else {
        System.out.println("🖥️ Running locally: Using UI login...");
        test.setUp();
        try {
            LoginTest loginTest = new LoginTest();
            loginTest.driver = test.driver;
            loginTest.loginGoogleJira();

            TaskTest taskTest = new TaskTest();
            taskTest.driver = test.driver;
            taskTest.searchTasks_1(); // Can use UI or API
        } finally {
            test.tearDown();
        }
    }

    System.exit(0);
}