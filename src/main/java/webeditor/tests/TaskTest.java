package webeditor.tests;

import webeditor.pages.*;

public class TaskTest extends BaseTest{
    
    public void searchTasks(){
        String jqlBuildsFilter = " project = WEB AND summary ~ \"Go Live\" AND status = QA ";
        System.out.println("jqlBuildsFilter: " + jqlBuildsFilter);
        WebEditorPage webEditorPage=new WebEditorPage(driver);
        //String allKeyIssues = webEditorPage.getKeyIssuesByApi(driver,jqlBuildsFilter,"");

    }
}
