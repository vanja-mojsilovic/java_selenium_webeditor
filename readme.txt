Scenario
    Get all go live tasks with status QA
            project = WEB AND summary ~ "Go Live" AND status = QA
        but avoid process the tasks with a comment
            "Please check if there is a Web editor task on this branch!"
    For each task get parent key issue
    Open each parent epic task, get a list of Child work items
    If there is a Website editor task
    If website editor task status is not Done or Closed leave a comment in Go live site
        "Please check if there is a Web editor task on this branch!"
