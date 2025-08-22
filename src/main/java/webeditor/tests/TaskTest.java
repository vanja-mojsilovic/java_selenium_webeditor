package webeditor.tests;

import java.io.IOException;

import webeditor.pages.*;

public class TaskTest extends BaseTest{
    
    public void searchTasks() throws InterruptedException, IOException{
        String jqlBuildsFilter = " project = WEB AND summary ~ \"Go Live\" AND status = QA ";
        System.out.println("jqlBuildsFilter: " + jqlBuildsFilter);
        WebEditorPage webEditorPage = new WebEditorPage(driver);
        String allKeyIssues = webEditorPage.getKeyIssuesByApi(driver,jqlBuildsFilter,"");
        allKeyIssues = "issue in ("+allKeyIssues+")";
        System.out.println("allKeyIssues: " + allKeyIssues);

    }
}
